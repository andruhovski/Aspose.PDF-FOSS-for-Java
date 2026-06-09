package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfBoolean;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates graphics-related requirements for PDF/A compliance.
 *
 * <p>Checks the following ISO 19005 clauses:</p>
 * <ul>
 *   <li>6.2.2 &mdash; Device color spaces require OutputIntent</li>
 *   <li>6.2.4 &mdash; Image restrictions (no /Alternates, /OPI, /Interpolate must be false)</li>
 *   <li>6.2.5 &mdash; Form XObject restrictions (no /OPI, /Subtype2=PS, /PS)</li>
 *   <li>6.2.7 &mdash; No PostScript XObjects (/Subtype=/PS)</li>
 *   <li>6.2.8 &mdash; ExtGState: no /TR; /TR2 must be Default; /RI must be valid</li>
 * </ul>
 */
public final class GraphicsRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(GraphicsRules.class.getName());

    private static final Set<String> VALID_RENDERING_INTENTS = new HashSet<>(Arrays.asList(
            "AbsoluteColorimetric", "RelativeColorimetric", "Saturation", "Perceptual"
    ));

    /**
     * Creates a new graphics rules checker.
     */
    public GraphicsRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfA()) {
            return;
        }

        boolean hasOutputIntent = hasGtsPdfA1OutputIntent(parser);
        checkPages(parser, format, result, hasOutputIntent);
    }

    /**
     * Checks whether the catalog has an OutputIntent with /S = GTS_PDFA1.
     */
    private boolean hasGtsPdfA1OutputIntent(PDFParser parser) {
        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            return false;
        }

        PdfBase oiRef = catalog.get("OutputIntents");
        PdfArray outputIntents = resolveArray(oiRef);
        if (outputIntents == null) {
            return false;
        }

        for (int i = 0; i < outputIntents.size(); i++) {
            PdfDictionary oi = resolveDict(outputIntents.get(i));
            if (oi == null) {
                continue;
            }
            String s = oi.getNameAsString("S");
            if ("GTS_PDFA1".equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterates all pages and checks resources for graphics compliance.
     */
    private void checkPages(PDFParser parser, PdfFormat format,
                            PdfAValidationResult result, boolean hasOutputIntent) {
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

            checkDeviceColorSpaces(resources, pagePath, result, hasOutputIntent);
            checkXObjects(resources, pagePath, format, result);
            checkExtGState(resources, pagePath, result);
        }
    }

    /**
     * 6.2.2: If DeviceRGB/CMYK/Gray used without OutputIntent, report error.
     */
    private void checkDeviceColorSpaces(PdfDictionary resources, String pagePath,
                                         PdfAValidationResult result, boolean hasOutputIntent) {
        if (hasOutputIntent) {
            return; // device color spaces are acceptable when OutputIntent present
        }

        PdfDictionary colorSpaces = resolveDict(resources.get("ColorSpace"));
        if (colorSpaces == null) {
            return;
        }

        for (PdfName key : colorSpaces.keySet()) {
            PdfBase val = colorSpaces.get(key.getName());
            if (val instanceof PdfObjectReference) {
                try {
                    val = ((PdfObjectReference) val).dereference();
                } catch (IOException e) {
                    continue;
                }
            }
            if (val instanceof PdfName) {
                String name = ((PdfName) val).getName();
                if ("DeviceRGB".equals(name) || "DeviceCMYK".equals(name)
                        || "DeviceGray".equals(name)) {
                    result.addError("6.2.2",
                            "Device color space " + name
                                    + " used without GTS_PDFA1 OutputIntent",
                            pagePath + "/Resources/ColorSpace/" + key.getName(),
                            "6.2.2");
                }
            }
        }
    }

    /**
     * 6.2.4, 6.2.5, 6.2.7: Check XObject entries.
     */
    private void checkXObjects(PdfDictionary resources, String pagePath,
                                PdfFormat format, PdfAValidationResult result) {
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

            if ("Image".equals(subtype)) {
                checkImage(xObj, xObjPath, result);
            } else if ("Form".equals(subtype)) {
                checkFormXObject(xObj, xObjPath, result);
            } else if ("PS".equals(subtype)) {
                // 6.2.7: PostScript XObjects are forbidden
                result.addError("6.2.7",
                        "PostScript XObjects (/Subtype=/PS) are not permitted in PDF/A",
                        xObjPath, "6.2.7");
            }
        }
    }

    /**
     * 6.2.4: Image dicts: no /Alternates, no /OPI, /Interpolate must be false.
     */
    private void checkImage(PdfDictionary image, String path, PdfAValidationResult result) {
        if (image.get("Alternates") != null) {
            result.addError("6.2.4",
                    "Image XObject must not have /Alternates key",
                    path, "6.2.4");
        }
        if (image.get("OPI") != null) {
            result.addError("6.2.4",
                    "Image XObject must not have /OPI key",
                    path, "6.2.4");
        }
        PdfBase interpolate = image.get("Interpolate");
        if (interpolate instanceof PdfObjectReference) {
            try {
                interpolate = ((PdfObjectReference) interpolate).dereference();
            } catch (IOException e) {
                return;
            }
        }
        if (interpolate instanceof PdfBoolean && ((PdfBoolean) interpolate).getValue()) {
            result.addError("6.2.4",
                    "Image XObject /Interpolate must be false",
                    path, "6.2.4");
        }
    }

    /**
     * 6.2.5: Form XObjects: no /OPI, no /Subtype2=PS, no /PS.
     */
    private void checkFormXObject(PdfDictionary form, String path,
                                   PdfAValidationResult result) {
        if (form.get("OPI") != null) {
            result.addError("6.2.5",
                    "Form XObject must not have /OPI key",
                    path, "6.2.5");
        }
        if (form.get("PS") != null) {
            result.addError("6.2.5",
                    "Form XObject must not have /PS key",
                    path, "6.2.5");
        }
        String subtype2 = form.getNameAsString("Subtype2");
        if ("PS".equals(subtype2)) {
            result.addError("6.2.5",
                    "Form XObject must not have /Subtype2 = PS",
                    path, "6.2.5");
        }
    }

    /**
     * 6.2.8: ExtGState: no /TR; /TR2 must be Default; /RI must be valid.
     */
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

            // /TR is forbidden
            if (gs.get("TR") != null) {
                result.addError("6.2.8",
                        "ExtGState must not have /TR (transfer function)",
                        gsPath, "6.2.8");
            }

            // /TR2 must be "Default" if present
            PdfBase tr2 = gs.get("TR2");
            if (tr2 != null) {
                if (tr2 instanceof PdfObjectReference) {
                    try {
                        tr2 = ((PdfObjectReference) tr2).dereference();
                    } catch (IOException e) {
                        // skip
                    }
                }
                if (tr2 instanceof PdfName) {
                    if (!"Default".equals(((PdfName) tr2).getName())) {
                        result.addError("6.2.8",
                                "ExtGState /TR2 must be 'Default'",
                                gsPath, "6.2.8");
                    }
                } else {
                    result.addError("6.2.8",
                            "ExtGState /TR2 must be 'Default'",
                            gsPath, "6.2.8");
                }
            }

            // /RI must be a valid rendering intent
            String ri = gs.getNameAsString("RI");
            if (ri != null && !VALID_RENDERING_INTENTS.contains(ri)) {
                result.addError("6.2.8",
                        "ExtGState /RI contains invalid rendering intent: " + ri,
                        gsPath, "6.2.8");
            }
        }
    }

    /**
     * Resolves a PdfBase to a PdfDictionary, dereferencing indirect references.
     */
    private static PdfDictionary resolveDict(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof PdfDictionary) ? (PdfDictionary) val : null;
    }

    /**
     * Resolves a PdfBase to a PdfArray, dereferencing indirect references.
     */
    private static PdfArray resolveArray(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof PdfArray) ? (PdfArray) val : null;
    }
}
