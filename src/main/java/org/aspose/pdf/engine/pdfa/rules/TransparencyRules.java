package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Validates transparency requirements for PDF/A compliance.
///
/// Checks ISO 19005 clause 6.4 (PDF/A-1 only):
///
///   - /SMask must be None
///   - /BM must be Normal or Compatible
///   - /CA must be 1.0
///   - /ca must be 1.0
///   - No Group dict with /S=/Transparency in Form XObjects
///
/// PDF/A-2 and later allow transparency, so all checks are skipped for those formats.
public final class TransparencyRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(TransparencyRules.class.getName());

    /// Creates a new transparency rules checker.
    public TransparencyRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        // Transparency is only forbidden in PDF/A-1
        if (!format.isPdfA1()) {
            return;
        }
        checkPages(parser, format, result);
    }

    /// Iterates all pages and checks ExtGState and XObject resources.
    private void checkPages(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        PdfDictionary pages = resolveDict(catalog.get("Pages"));
        if (pages == null) {
            return;
        }
        PdfArray kids = pages.getArray("Kids");
        if (kids == null) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            PdfDictionary page = resolveDict(kids.get(i));
            if (page == null) {
                continue;
            }
            String pagePath = "page[" + i + "]";
            PdfDictionary resources = resolveDict(page.get("Resources"));
            if (resources == null) {
                continue;
            }
            checkExtGState(resources, pagePath, result);
            checkFormXObjects(resources, pagePath, result);
        }
    }

    /// Checks ExtGState entries for forbidden transparency parameters.
    private void checkExtGState(PdfDictionary resources, String pagePath,
                                 PdfAValidationResult result) {
        PdfDictionary extGStates = resolveDict(resources.get("ExtGState"));
        if (extGStates == null) {
            return;
        }

        for (PdfName key : extGStates.keySet()) {
            PdfDictionary gs = resolveDict(extGStates.get(key.getName()));
            if (gs == null) {
                continue;
            }
            String gsPath = pagePath + "/Resources/ExtGState/" + key.getName();

            // /SMask must be None (or absent)
            PdfBase smask = resolve(gs.get("SMask"));
            if (smask != null) {
                boolean isNone = (smask instanceof PdfName)
                        && "None".equals(((PdfName) smask).getName());
                if (!isNone) {
                    result.addError("6.4",
                            "PDF/A-1: ExtGState /SMask must be 'None'",
                            gsPath, "6.4");
                }
            }

            // /BM must be Normal or Compatible (or absent)
            PdfBase bm = resolve(gs.get("BM"));
            if (bm instanceof PdfName) {
                String bmName = ((PdfName) bm).getName();
                if (!"Normal".equals(bmName) && !"Compatible".equals(bmName)) {
                    result.addError("6.4",
                            "PDF/A-1: ExtGState /BM must be 'Normal' or 'Compatible', found: "
                                    + bmName,
                            gsPath, "6.4");
                }
            }

            // /CA (stroking alpha) must be 1.0 (or absent)
            checkAlpha(gs, "CA", gsPath, result);

            // /ca (non-stroking alpha) must be 1.0 (or absent)
            checkAlpha(gs, "ca", gsPath, result);
        }
    }

    /// Checks that an alpha value (/CA or /ca) is 1.0 if present.
    private void checkAlpha(PdfDictionary gs, String alphaKey, String gsPath,
                             PdfAValidationResult result) {
        PdfBase val = resolve(gs.get(alphaKey));
        if (val == null) {
            return;
        }

        float alpha = -1f;
        if (val instanceof PdfFloat) {
            alpha = ((PdfFloat) val).floatValue();
        } else if (val instanceof PdfInteger) {
            alpha = ((PdfInteger) val).intValue();
        }

        if (Math.abs(alpha - 1.0f) > 0.001f) {
            result.addError("6.4",
                    "PDF/A-1: ExtGState /" + alphaKey + " must be 1.0, found: " + alpha,
                    gsPath, "6.4");
        }
    }

    /// Checks Form XObjects for transparency group dictionaries
    /// and Image XObjects for /SMask.
    private void checkFormXObjects(PdfDictionary resources, String pagePath,
                                    PdfAValidationResult result) {
        PdfDictionary xObjects = resolveDict(resources.get("XObject"));
        if (xObjects == null) {
            return;
        }

        for (PdfName key : xObjects.keySet()) {
            PdfDictionary xObj = resolveDict(xObjects.get(key.getName()));
            if (xObj == null) {
                continue;
            }
            String subtype = xObj.getNameAsString("Subtype");
            String xObjPath = pagePath + "/Resources/XObject/" + key.getName();

            if ("Form".equals(subtype)) {
                // Check for /Group with /S = /Transparency
                PdfDictionary group = resolveDict(xObj.get("Group"));
                if (group != null) {
                    String groupS = group.getNameAsString("S");
                    if ("Transparency".equals(groupS)) {
                        result.addError("6.4",
                                "PDF/A-1: Form XObject must not have transparency group (/Group /S /Transparency)",
                                xObjPath, "6.4");
                    }
                }
            } else if ("Image".equals(subtype)) {
                // Check Image XObjects for /SMask (soft mask)
                PdfBase smask = resolve(xObj.get("SMask"));
                if (smask != null) {
                    boolean isNone = (smask instanceof PdfName)
                            && "None".equals(((PdfName) smask).getName());
                    if (!isNone) {
                        result.addError("6.4",
                                "PDF/A-1: Image XObject must not have /SMask",
                                xObjPath, "6.4");
                    }
                }
            }
        }
    }

    /// Resolves a PdfBase value, dereferencing indirect references.
    private static PdfBase resolve(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return val;
    }

    /// Resolves a PdfBase to a PdfDictionary, dereferencing indirect references.
    private static PdfDictionary resolveDict(PdfBase val) {
        PdfBase resolved = resolve(val);
        return (resolved instanceof PdfDictionary) ? (PdfDictionary) resolved : null;
    }
}
