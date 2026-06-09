package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.operators.Do;
import org.aspose.pdf.operators.SetCMYKColor;
import org.aspose.pdf.operators.SetCMYKColorStroke;
import org.aspose.pdf.operators.SetColor;
import org.aspose.pdf.operators.SetColorSpace;
import org.aspose.pdf.operators.SetColorSpaceStroke;
import org.aspose.pdf.operators.SetColorStroke;
import org.aspose.pdf.operators.SetGray;
import org.aspose.pdf.operators.SetGrayStroke;
import org.aspose.pdf.operators.SetRGBColor;
import org.aspose.pdf.operators.SetRGBColorStroke;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Determines a page's {@link ColorType} by walking its content stream and
 * referenced image XObjects, then collapsing every observed colour-family
 * signal into a single worst-case classification:
 *
 * <pre>
 *   RGB / CMYK content seen        →  Rgb
 *   gray seen but no chromatic     →  Grayscale (or BlackAndWhite if every
 *                                      observed gray was exactly 0 or 1)
 *   nothing seen                   →  Undefined
 * </pre>
 *
 * <p>Implementation is intentionally heuristic — matches Aspose.Pdf's
 * documented behaviour where a single colour image promotes the whole page
 * to {@link ColorType#Rgb}, and a "BW" page is one whose every gray fill /
 * stroke and every embedded image uses {@code DeviceGray} with components
 * snapping to 0 or 1.</p>
 */
final class PageColorClassifier {

    private static final Logger LOG = Logger.getLogger(PageColorClassifier.class.getName());

    /** Tolerance for the "0 or 1" snap that distinguishes B/W from gray. */
    private static final double EPSILON = 1.0 / 255.0;

    private PageColorClassifier() {
    }

    static ColorType classify(Page page) {
        boolean sawChromatic = false;
        boolean sawGray = false;
        boolean sawNonBwGray = false;
        try {
            OperatorCollection ops = page.getContents();
            for (int i = 0; i < ops.size(); i++) {
                Operator op = ops.getAt(i);
                Signal signal = inspect(op, page);
                switch (signal) {
                    case CHROMATIC:
                        sawChromatic = true;
                        break;
                    case GRAY_MIDTONE:
                        sawGray = true;
                        sawNonBwGray = true;
                        break;
                    case GRAY_BW:
                        sawGray = true;
                        break;
                    default:
                        break;
                }
                if (sawChromatic) {
                    break; // worst case already reached
                }
            }
        } catch (IOException e) {
            LOG.log(Level.FINE,
                    "PageColorClassifier could not read page contents — returning Undefined", e);
            return ColorType.Undefined;
        }
        if (sawChromatic) {
            return ColorType.Rgb;
        }
        if (sawGray) {
            return sawNonBwGray ? ColorType.Grayscale : ColorType.BlackAndWhite;
        }
        return ColorType.Undefined;
    }

    private enum Signal {
        /** Operator touches RGB or CMYK colour. */
        CHROMATIC,
        /** Operator touches a gray midtone (not pure 0 or 1). */
        GRAY_MIDTONE,
        /** Operator touches pure black or pure white. */
        GRAY_BW,
        /** Operator doesn't influence colour. */
        NONE,
    }

    private static Signal inspect(Operator op, Page page) {
        if (op instanceof SetRGBColor) {
            return classifyRgb(((SetRGBColor) op).getR(),
                    ((SetRGBColor) op).getG(),
                    ((SetRGBColor) op).getB());
        }
        if (op instanceof SetRGBColorStroke) {
            return classifyRgb(((SetRGBColorStroke) op).getR(),
                    ((SetRGBColorStroke) op).getG(),
                    ((SetRGBColorStroke) op).getB());
        }
        if (op instanceof SetCMYKColor) {
            SetCMYKColor c = (SetCMYKColor) op;
            return classifyCmyk(c.getC(), c.getM(), c.getY(), c.getK());
        }
        if (op instanceof SetCMYKColorStroke) {
            SetCMYKColorStroke c = (SetCMYKColorStroke) op;
            return classifyCmyk(c.getC(), c.getM(), c.getY(), c.getK());
        }
        if (op instanceof SetGray) {
            return classifyGray(((SetGray) op).getGray());
        }
        if (op instanceof SetGrayStroke) {
            return classifyGray(((SetGrayStroke) op).getGray());
        }
        if (op instanceof SetColorSpace) {
            return classifyColorSpaceName(((SetColorSpace) op).getColorSpaceName());
        }
        if (op instanceof SetColorSpaceStroke) {
            return classifyColorSpaceName(((SetColorSpaceStroke) op).getColorSpaceName());
        }
        if (op instanceof SetColor || op instanceof SetColorStroke) {
            // sc / SC follow the most recent SetColorSpace, so we can't be sure
            // here without tracking state. Treat as chromatic-or-gray based on
            // component count: 3+ components are RGB-like, 4 are CMYK, 1 is gray.
            double[] comps = (op instanceof SetColor)
                    ? ((SetColor) op).getComponents()
                    : ((SetColorStroke) op).getComponents();
            if (comps == null) {
                return Signal.NONE;
            }
            if (comps.length >= 3) {
                if (comps.length >= 4) {
                    return classifyCmyk(comps[0], comps[1], comps[2], comps[3]);
                }
                return classifyRgb(comps[0], comps[1], comps[2]);
            }
            if (comps.length == 1) {
                return classifyGray(comps[0]);
            }
            return Signal.NONE;
        }
        if (op instanceof Do) {
            return inspectImage(((Do) op).getXObjectName(), page);
        }
        return Signal.NONE;
    }

    private static Signal classifyRgb(double r, double g, double b) {
        if (approxEquals(r, g) && approxEquals(g, b)) {
            // R == G == B → effectively gray
            return classifyGray(r);
        }
        return Signal.CHROMATIC;
    }

    private static Signal classifyCmyk(double c, double m, double y, double k) {
        // Pure black (any K with C=M=Y=0) or pure white (everything 0) are gray.
        if (approxEquals(c, 0) && approxEquals(m, 0) && approxEquals(y, 0)) {
            return classifyGray(1.0 - k);
        }
        return Signal.CHROMATIC;
    }

    private static Signal classifyGray(double v) {
        if (Math.abs(v) <= EPSILON || Math.abs(v - 1.0) <= EPSILON) {
            return Signal.GRAY_BW;
        }
        return Signal.GRAY_MIDTONE;
    }

    private static Signal classifyColorSpaceName(String name) {
        if (name == null) {
            return Signal.NONE;
        }
        switch (name) {
            case "DeviceRGB":
            case "DeviceCMYK":
            case "CalRGB":
                return Signal.CHROMATIC;
            case "DeviceGray":
            case "CalGray":
            case "Pattern":
                // Bare colour-space switch alone doesn't paint anything; we wait
                // for the following sc/SC. Don't claim a colour here.
                return Signal.NONE;
            default:
                return Signal.NONE;
        }
    }

    private static Signal inspectImage(String xobjectName, Page page) {
        if (xobjectName == null) {
            return Signal.NONE;
        }
        try {
            Resources res = page.getResources();
            if (res == null) {
                return Signal.NONE;
            }
            PdfDictionary xobjects = res.getXObjects();
            if (xobjects == null) {
                return Signal.NONE;
            }
            PdfBase entry = xobjects.get(xobjectName);
            entry = resolveRef(entry);
            if (!(entry instanceof PdfStream)) {
                return Signal.NONE;
            }
            PdfStream xobject = (PdfStream) entry;
            PdfBase subtype = xobject.get(PdfName.of("Subtype"));
            if (subtype instanceof PdfName && "Image".equals(((PdfName) subtype).getName())) {
                PdfBase cs = resolveRef(xobject.get(PdfName.of("ColorSpace")));
                return classifyImageColorSpace(cs, xobject);
            }
            if (subtype instanceof PdfName && "Form".equals(((PdfName) subtype).getName())) {
                // Recurse into the form XObject's content stream and aggregate
                // the worst-case colour seen inside. A form's own /Resources
                // shadows the page's, so look up XObject references against
                // its own dictionary when present.
                return inspectFormXObject(xobject, page);
            }
            return Signal.NONE;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Image classification failed for /" + xobjectName, e);
            return Signal.NONE;
        }
    }

    private static Signal inspectFormXObject(PdfStream form, Page hostPage) {
        try {
            OperatorCollection ops = org.aspose.pdf.engine.parser.ContentStreamParser
                    .parseToCollection(form);
            Signal worst = Signal.NONE;
            for (int i = 0; i < ops.size(); i++) {
                Operator op = ops.getAt(i);
                Signal s = inspect(op, hostPage);
                if (s == Signal.CHROMATIC) {
                    return Signal.CHROMATIC;
                }
                if (s == Signal.GRAY_MIDTONE
                        || (s == Signal.GRAY_BW && worst == Signal.NONE)) {
                    worst = s;
                }
            }
            return worst;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Form XObject classification failed", e);
            return Signal.NONE;
        }
    }

    private static Signal classifyImageColorSpace(PdfBase cs, PdfStream xobject) {
        // Boolean ImageMask images are always one-bit stencils → BW.
        PdfBase imageMask = xobject.get(PdfName.of("ImageMask"));
        if (imageMask instanceof org.aspose.pdf.engine.pdfobjects.PdfBoolean
                && ((org.aspose.pdf.engine.pdfobjects.PdfBoolean) imageMask).getValue()) {
            return Signal.GRAY_BW;
        }
        if (cs instanceof PdfName) {
            String name = ((PdfName) cs).getName();
            return colorSpaceNameSignal(name, xobject);
        }
        if (cs instanceof PdfArray) {
            PdfArray arr = (PdfArray) cs;
            if (arr.size() > 0 && arr.get(0) instanceof PdfName) {
                String family = ((PdfName) arr.get(0)).getName();
                // Indexed / CalRGB / ICCBased / Lab → conservative classification.
                if ("Indexed".equals(family) && arr.size() >= 2) {
                    PdfBase base = resolveRef(arr.get(1));
                    if (base instanceof PdfName) {
                        return colorSpaceNameSignal(((PdfName) base).getName(), xobject);
                    }
                }
                if ("ICCBased".equals(family) && arr.size() >= 2) {
                    PdfBase iccStream = resolveRef(arr.get(1));
                    if (iccStream instanceof PdfStream) {
                        PdfBase nComp = ((PdfStream) iccStream).get(PdfName.of("N"));
                        if (nComp instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger) {
                            int n = ((org.aspose.pdf.engine.pdfobjects.PdfInteger) nComp).intValue();
                            if (n == 1) return imageMonoSignal(xobject);
                            if (n >= 3) return Signal.CHROMATIC;
                        }
                    }
                }
                return colorSpaceNameSignal(family, xobject);
            }
        }
        return Signal.NONE;
    }

    private static Signal colorSpaceNameSignal(String name, PdfStream xobject) {
        if (name == null) {
            return Signal.NONE;
        }
        switch (name) {
            case "DeviceGray":
            case "CalGray":
                return imageMonoSignal(xobject);
            case "DeviceRGB":
            case "CalRGB":
            case "DeviceCMYK":
            case "Lab":
                return Signal.CHROMATIC;
            default:
                return Signal.NONE;
        }
    }

    /**
     * For a {@code DeviceGray}/{@code CalGray}/{@code ICCBased(N=1)} image, this
     * sampler decodes a short prefix of the image stream and reports
     * {@link Signal#GRAY_BW} if every pixel snaps to 0 or to its max-value
     * (i.e. all black or white), or {@link Signal#GRAY_MIDTONE} if any
     * midtone is present. Falls back to {@code GRAY_BW} when the image is
     * declared as 1-bit-per-component, since those can't carry midtones.
     */
    private static Signal imageMonoSignal(PdfStream xobject) {
        PdfBase bpcRaw = xobject.get(PdfName.of("BitsPerComponent"));
        int bits = (bpcRaw instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger)
                ? ((org.aspose.pdf.engine.pdfobjects.PdfInteger) bpcRaw).intValue()
                : 8;
        if (bits <= 1) {
            return Signal.GRAY_BW;
        }
        // For multi-bit grayscale: sample the decoded bytes and check for
        // anything that isn't pure 0 or pure max. Decode only the first 8 KB
        // to keep this cheap on large images.
        try {
            byte[] data = xobject.getDecodedData();
            if (data == null || data.length == 0) {
                return Signal.GRAY_BW;
            }
            int max = (1 << bits) - 1;
            int samplesToCheck = Math.min(data.length, 8192);
            for (int i = 0; i < samplesToCheck; i++) {
                int v = data[i] & 0xFF;
                if (bits == 8) {
                    if (v != 0 && v != max) {
                        return Signal.GRAY_MIDTONE;
                    }
                } else {
                    // For 2/4-bit images the byte holds multiple samples — but
                    // the byte-level "all 0x00 or all 0xFF" check is still a
                    // good cheap proxy.
                    if (v != 0 && v != 0xFF) {
                        return Signal.GRAY_MIDTONE;
                    }
                }
            }
            return Signal.GRAY_BW;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Mono image sampling failed; defaulting to grayscale", e);
            return Signal.GRAY_MIDTONE;
        }
    }

    private static PdfBase resolveRef(PdfBase value) {
        if (value instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) value).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return value;
    }

    private static boolean approxEquals(double a, double b) {
        return Math.abs(a - b) <= EPSILON;
    }
}
