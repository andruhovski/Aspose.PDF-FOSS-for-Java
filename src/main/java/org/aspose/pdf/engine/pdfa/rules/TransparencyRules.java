package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates transparency requirements for PDF/A compliance.
 *
 * <p>Checks ISO 19005 clause 6.4 (PDF/A-1 only):</p>
 * <ul>
 *   <li>/SMask must be None</li>
 *   <li>/BM must be Normal or Compatible</li>
 *   <li>/CA must be 1.0</li>
 *   <li>/ca must be 1.0</li>
 *   <li>No Group dict with /S=/Transparency in Form XObjects</li>
 * </ul>
 *
 * <p>PDF/A-2 and later allow transparency, so all checks are skipped for those formats.</p>
 */
public final class TransparencyRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(TransparencyRules.class.getName());

    /**
     * Creates a new transparency rules checker.
     */
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

    /**
     * Iterates all pages and checks ExtGState and XObject resources.
     */
    private void checkPages(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        COSDictionary pages = resolveDict(catalog.get("Pages"));
        if (pages == null) {
            return;
        }
        COSArray kids = pages.getArray("Kids");
        if (kids == null) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            COSDictionary page = resolveDict(kids.get(i));
            if (page == null) {
                continue;
            }
            String pagePath = "page[" + i + "]";
            COSDictionary resources = resolveDict(page.get("Resources"));
            if (resources == null) {
                continue;
            }
            checkExtGState(resources, pagePath, result);
            checkFormXObjects(resources, pagePath, result);
        }
    }

    /**
     * Checks ExtGState entries for forbidden transparency parameters.
     */
    private void checkExtGState(COSDictionary resources, String pagePath,
                                 PdfAValidationResult result) {
        COSDictionary extGStates = resolveDict(resources.get("ExtGState"));
        if (extGStates == null) {
            return;
        }

        for (COSName key : extGStates.keySet()) {
            COSDictionary gs = resolveDict(extGStates.get(key.getName()));
            if (gs == null) {
                continue;
            }
            String gsPath = pagePath + "/Resources/ExtGState/" + key.getName();

            // /SMask must be None (or absent)
            COSBase smask = resolve(gs.get("SMask"));
            if (smask != null) {
                boolean isNone = (smask instanceof COSName)
                        && "None".equals(((COSName) smask).getName());
                if (!isNone) {
                    result.addError("6.4",
                            "PDF/A-1: ExtGState /SMask must be 'None'",
                            gsPath, "6.4");
                }
            }

            // /BM must be Normal or Compatible (or absent)
            COSBase bm = resolve(gs.get("BM"));
            if (bm instanceof COSName) {
                String bmName = ((COSName) bm).getName();
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

    /**
     * Checks that an alpha value (/CA or /ca) is 1.0 if present.
     */
    private void checkAlpha(COSDictionary gs, String alphaKey, String gsPath,
                             PdfAValidationResult result) {
        COSBase val = resolve(gs.get(alphaKey));
        if (val == null) {
            return;
        }

        float alpha = -1f;
        if (val instanceof COSFloat) {
            alpha = ((COSFloat) val).floatValue();
        } else if (val instanceof COSInteger) {
            alpha = ((COSInteger) val).intValue();
        }

        if (Math.abs(alpha - 1.0f) > 0.001f) {
            result.addError("6.4",
                    "PDF/A-1: ExtGState /" + alphaKey + " must be 1.0, found: " + alpha,
                    gsPath, "6.4");
        }
    }

    /**
     * Checks Form XObjects for transparency group dictionaries
     * and Image XObjects for /SMask.
     */
    private void checkFormXObjects(COSDictionary resources, String pagePath,
                                    PdfAValidationResult result) {
        COSDictionary xObjects = resolveDict(resources.get("XObject"));
        if (xObjects == null) {
            return;
        }

        for (COSName key : xObjects.keySet()) {
            COSDictionary xObj = resolveDict(xObjects.get(key.getName()));
            if (xObj == null) {
                continue;
            }
            String subtype = xObj.getNameAsString("Subtype");
            String xObjPath = pagePath + "/Resources/XObject/" + key.getName();

            if ("Form".equals(subtype)) {
                // Check for /Group with /S = /Transparency
                COSDictionary group = resolveDict(xObj.get("Group"));
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
                COSBase smask = resolve(xObj.get("SMask"));
                if (smask != null) {
                    boolean isNone = (smask instanceof COSName)
                            && "None".equals(((COSName) smask).getName());
                    if (!isNone) {
                        result.addError("6.4",
                                "PDF/A-1: Image XObject must not have /SMask",
                                xObjPath, "6.4");
                    }
                }
            }
        }
    }

    /**
     * Resolves a COSBase value, dereferencing indirect references.
     */
    private static COSBase resolve(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return val;
    }

    /**
     * Resolves a COSBase to a COSDictionary, dereferencing indirect references.
     */
    private static COSDictionary resolveDict(COSBase val) {
        COSBase resolved = resolve(val);
        return (resolved instanceof COSDictionary) ? (COSDictionary) resolved : null;
    }
}
