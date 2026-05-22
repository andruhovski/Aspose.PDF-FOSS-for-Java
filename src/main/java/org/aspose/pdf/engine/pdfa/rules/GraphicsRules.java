package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSBoolean;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
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
        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            return false;
        }

        COSBase oiRef = catalog.get("OutputIntents");
        COSArray outputIntents = resolveArray(oiRef);
        if (outputIntents == null) {
            return false;
        }

        for (int i = 0; i < outputIntents.size(); i++) {
            COSDictionary oi = resolveDict(outputIntents.get(i));
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

            checkDeviceColorSpaces(resources, pagePath, result, hasOutputIntent);
            checkXObjects(resources, pagePath, format, result);
            checkExtGState(resources, pagePath, result);
        }
    }

    /**
     * 6.2.2: If DeviceRGB/CMYK/Gray used without OutputIntent, report error.
     */
    private void checkDeviceColorSpaces(COSDictionary resources, String pagePath,
                                         PdfAValidationResult result, boolean hasOutputIntent) {
        if (hasOutputIntent) {
            return; // device color spaces are acceptable when OutputIntent present
        }

        COSDictionary colorSpaces = resolveDict(resources.get("ColorSpace"));
        if (colorSpaces == null) {
            return;
        }

        for (COSName key : colorSpaces.keySet()) {
            COSBase val = colorSpaces.get(key.getName());
            if (val instanceof COSObjectReference) {
                try {
                    val = ((COSObjectReference) val).dereference();
                } catch (IOException e) {
                    continue;
                }
            }
            if (val instanceof COSName) {
                String name = ((COSName) val).getName();
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
    private void checkXObjects(COSDictionary resources, String pagePath,
                                PdfFormat format, PdfAValidationResult result) {
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
    private void checkImage(COSDictionary image, String path, PdfAValidationResult result) {
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
        COSBase interpolate = image.get("Interpolate");
        if (interpolate instanceof COSObjectReference) {
            try {
                interpolate = ((COSObjectReference) interpolate).dereference();
            } catch (IOException e) {
                return;
            }
        }
        if (interpolate instanceof COSBoolean && ((COSBoolean) interpolate).getValue()) {
            result.addError("6.2.4",
                    "Image XObject /Interpolate must be false",
                    path, "6.2.4");
        }
    }

    /**
     * 6.2.5: Form XObjects: no /OPI, no /Subtype2=PS, no /PS.
     */
    private void checkFormXObject(COSDictionary form, String path,
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

            // /TR is forbidden
            if (gs.get("TR") != null) {
                result.addError("6.2.8",
                        "ExtGState must not have /TR (transfer function)",
                        gsPath, "6.2.8");
            }

            // /TR2 must be "Default" if present
            COSBase tr2 = gs.get("TR2");
            if (tr2 != null) {
                if (tr2 instanceof COSObjectReference) {
                    try {
                        tr2 = ((COSObjectReference) tr2).dereference();
                    } catch (IOException e) {
                        // skip
                    }
                }
                if (tr2 instanceof COSName) {
                    if (!"Default".equals(((COSName) tr2).getName())) {
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
     * Resolves a COSBase to a COSDictionary, dereferencing indirect references.
     */
    private static COSDictionary resolveDict(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof COSDictionary) ? (COSDictionary) val : null;
    }

    /**
     * Resolves a COSBase to a COSArray, dereferencing indirect references.
     */
    private static COSArray resolveArray(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof COSArray) ? (COSArray) val : null;
    }
}
