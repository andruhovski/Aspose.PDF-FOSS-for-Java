package org.aspose.pdf.engine.render;

import org.aspose.pdf.ExtGState;
import org.aspose.pdf.Matrix;
import org.aspose.pdf.Operator;
import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.Resources;
import org.aspose.pdf.XForm;
import org.aspose.pdf.XImage;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.operators.*;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

/**
 * Core PDF page rendering engine (ISO 32000-1:2008, §8 &amp; §9).
 * <p>
 * Processes content stream operators and renders graphics, text, and images
 * onto a {@link java.awt.Graphics2D} context backed by a {@link BufferedImage}.
 * </p>
 * <p>
 * The renderer handles:
 * <ul>
 *   <li>Graphics state (q/Q, cm, w, J, j, M, d, gs)</li>
 *   <li>Color operators (rg, RG, g, G, k, K, cs, sc, scn, CS, SC, SCN)</li>
 *   <li>Path construction (m, l, c, v, y, re, h) and painting (S, s, f, F, f*, B, B*, b, b*, n)</li>
 *   <li>Clipping (W, W*)</li>
 *   <li>Text (BT, ET, Tf, Td, TD, Tm, T*, Tc, Tw, Tz, TL, Tr, Ts, Tj, TJ, ', ")</li>
 *   <li>XObjects — images (Do with /Image) and forms (Do with /Form)</li>
 * </ul>
 * </p>
 */
public class PdfPageRenderer {

    private static final Logger LOG = Logger.getLogger(PdfPageRenderer.class.getName());

    /** Maximum recursion depth for Form XObjects to prevent infinite loops. */
    private static final int MAX_FORM_DEPTH = 10;

    private final TextRenderer textRenderer = new TextRenderer();
    {
        // Type 3 glyphs are content streams (§9.6.5); the text renderer calls
        // back into this operator machinery to execute them.
        textRenderer.setType3Executor(this::executeType3GlyphStream);
    }

    /**
     * Renders a PDF page to a BufferedImage at the specified DPI.
     *
     * @param page the PDF page to render
     * @param dpiX horizontal resolution in DPI
     * @param dpiY vertical resolution in DPI
     * @return the rendered image
     * @throws IOException if reading the content stream fails
     */
    public BufferedImage renderPage(Page page, double dpiX, double dpiY) throws IOException {
        Rectangle mediaBox = page.getMediaBox();
        if (mediaBox == null) {
            mediaBox = new Rectangle(0, 0, 612, 792); // US Letter default
        }

        double pageW = Math.abs(mediaBox.getWidth());
        double pageH = Math.abs(mediaBox.getHeight());

        // For 90/270 degree rotation, swap display dimensions
        int rotation = page.getRotate();
        double displayW = (rotation == 90 || rotation == 270) ? pageH : pageW;
        double displayH = (rotation == 90 || rotation == 270) ? pageW : pageH;

        // Round (not ceil) — matches the reference renderers' raster sizing.
        // ceil made e.g. a 1232.45pt page 1712px instead of 1711px and the
        // half-pixel scale skew drifted every thin stroke vs the reference
        // (corpus 25716-2: constant ~2800px changed region on every page).
        int pixelW = Math.max(1, (int) Math.floor(displayW * dpiX / 72.0 + 0.5));
        int pixelH = Math.max(1, (int) Math.floor(displayH * dpiY / 72.0 + 0.5));

        BufferedImage image = new BufferedImage(pixelW, pixelH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Rendering hints. Text anti-aliasing uses GASP (greyscale, font-driven)
        // not LCD subpixel — Aspose's gold renderings quantise to ~33 grey
        // levels (multiples of 32 brightness), which matches the GASP/greyscale
        // path. LCD subpixel AA produces 256 distinct colours per text glyph
        // and pushes pHash distance above threshold even when content is
        // pixel-aligned identical.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        // Smooth image scaling: the Java2D default (nearest neighbour) turns
        // downscaled scans into ragged strokes — corpus 25716-2 draws whole
        // pages as 3424px-wide CCITT fax images scaled ~2:1.
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // PDF coordinate system: origin at bottom-left; Java 2D: origin at top-left
        // Transform: flip Y axis and scale from user-space (72 DPI) to pixel space
        g2d.translate(0, pixelH);
        g2d.scale(dpiX / 72.0, -dpiY / 72.0);

        // Handle page rotation
        if (rotation != 0) {
            applyRotation(g2d, rotation, pageW, pageH);
        }

        // Offset for MediaBox origin (if not at 0,0)
        if (mediaBox.getLLX() != 0 || mediaBox.getLLY() != 0) {
            g2d.translate(-mediaBox.getLLX(), -mediaBox.getLLY());
        }

        // White background — use MediaBox coordinates in the (now offset) user space
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillRect((int) mediaBox.getLLX(), (int) mediaBox.getLLY(),
                (int) Math.ceil(pageW), (int) Math.ceil(pageH));

        // Process content stream
        try {
            OperatorCollection ops = page.getContents();
            if (ops != null) {
                Resources resources = page.getResources();
                processOperators(ops, resources, g2d, null, 0);
            }
        } catch (Exception e) {
            LOG.warning(() -> "Error rendering page: " + e.getMessage());
        }

        // Render annotation appearances on top of the page content
        // (ISO 32000-1 §12.5.5). For each Annot with a Normal Appearance
        // stream (/AP /N), map the appearance Form's /BBox to the
        // annotation's /Rect and render its content like an inline Form
        // XObject. Without this step pages that put their visible text in
        // FreeText annotations (e.g. PDFNEWNET_31744) come out blank.
        try {
            renderAnnotations(page, g2d);
        } catch (Exception e) {
            LOG.warning(() -> "Error rendering annotations: " + e.getMessage());
        }

        g2d.dispose();
        return image;
    }

    /** Iterates page annotations and draws each one's Normal Appearance stream. */
    private void renderAnnotations(Page page, Graphics2D g2d) {
        org.aspose.pdf.engine.pdfobjects.PdfDictionary pageDict = page.getPdfDictionary();
        if (pageDict == null) return;
        org.aspose.pdf.engine.pdfobjects.PdfBase annotsVal = pageDict.get(org.aspose.pdf.engine.pdfobjects.PdfName.ANNOTS);
        annotsVal = resolveRef(annotsVal);
        if (!(annotsVal instanceof org.aspose.pdf.engine.pdfobjects.PdfArray)) return;
        org.aspose.pdf.engine.pdfobjects.PdfArray annots = (org.aspose.pdf.engine.pdfobjects.PdfArray) annotsVal;
        Resources pageResources = page.getResources();
        for (int i = 0; i < annots.size(); i++) {
            org.aspose.pdf.engine.pdfobjects.PdfBase item = resolveRef(annots.get(i));
            if (!(item instanceof org.aspose.pdf.engine.pdfobjects.PdfDictionary)) continue;
            org.aspose.pdf.engine.pdfobjects.PdfDictionary annot =
                    (org.aspose.pdf.engine.pdfobjects.PdfDictionary) item;
            renderOneAnnotation(annot, pageResources, g2d);
        }
    }

    private void renderOneAnnotation(org.aspose.pdf.engine.pdfobjects.PdfDictionary annot,
                                      Resources pageResources, Graphics2D g2d) {
        // Skip hidden / invisible annotations (PDF flags bits 1, 2, 6).
        org.aspose.pdf.engine.pdfobjects.PdfBase fVal = annot.get("F");
        int flags = 0;
        if (fVal instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger) {
            flags = ((org.aspose.pdf.engine.pdfobjects.PdfInteger) fVal).intValue();
        }
        if ((flags & 0x02) != 0 || (flags & 0x01) != 0 || (flags & 0x20) != 0) return;

        org.aspose.pdf.engine.pdfobjects.PdfBase ap = resolveRef(annot.get("AP"));
        if (!(ap instanceof org.aspose.pdf.engine.pdfobjects.PdfDictionary)) return;
        org.aspose.pdf.engine.pdfobjects.PdfBase n =
                resolveRef(((org.aspose.pdf.engine.pdfobjects.PdfDictionary) ap).get("N"));
        // /N may be a stream (single appearance) or a dict keyed by AS state.
        if (n instanceof org.aspose.pdf.engine.pdfobjects.PdfDictionary) {
            org.aspose.pdf.engine.pdfobjects.PdfBase asName = annot.get("AS");
            if (asName instanceof org.aspose.pdf.engine.pdfobjects.PdfName) {
                n = resolveRef(((org.aspose.pdf.engine.pdfobjects.PdfDictionary) n)
                        .get(((org.aspose.pdf.engine.pdfobjects.PdfName) asName).getName()));
            }
        }
        if (!(n instanceof org.aspose.pdf.engine.pdfobjects.PdfStream)) return;
        org.aspose.pdf.engine.pdfobjects.PdfStream apStream =
                (org.aspose.pdf.engine.pdfobjects.PdfStream) n;

        org.aspose.pdf.engine.pdfobjects.PdfBase rectVal = resolveRef(annot.get("Rect"));
        if (!(rectVal instanceof org.aspose.pdf.engine.pdfobjects.PdfArray)
                || ((org.aspose.pdf.engine.pdfobjects.PdfArray) rectVal).size() != 4) return;
        Rectangle annotRect = Rectangle.fromPdfArray((org.aspose.pdf.engine.pdfobjects.PdfArray) rectVal);
        if (annotRect == null) return;

        org.aspose.pdf.engine.pdfobjects.PdfBase bboxVal = resolveRef(apStream.get("BBox"));
        Rectangle bbox = null;
        if (bboxVal instanceof org.aspose.pdf.engine.pdfobjects.PdfArray
                && ((org.aspose.pdf.engine.pdfobjects.PdfArray) bboxVal).size() == 4) {
            bbox = Rectangle.fromPdfArray((org.aspose.pdf.engine.pdfobjects.PdfArray) bboxVal);
        }
        if (bbox == null) bbox = annotRect;

        double bboxW = bbox.getWidth();
        double bboxH = bbox.getHeight();
        if (bboxW == 0 || bboxH == 0) return;

        double sx = annotRect.getWidth() / bboxW;
        double sy = annotRect.getHeight() / bboxH;
        double tx = annotRect.getLLX() - bbox.getLLX() * sx;
        double ty = annotRect.getLLY() - bbox.getLLY() * sy;

        // The appearance stream is a Form XObject. Wrap it via XForm so we
        // get an OperatorCollection and the form's /Resources. Render in a
        // pushed graphics state with the BBox→Rect CTM applied.
        try {
            XForm form = new XForm(apStream, "AP", null);
            OperatorCollection formOps = form.getContents();
            if (formOps == null) return;
            Resources formRes = form.getResources();
            if (formRes == null) formRes = pageResources;

            GraphicsState state = new GraphicsState();
            state.concatMatrix(new Matrix(sx, 0, 0, sy, tx, ty));

            Deque<GraphicsState> stack = new ArrayDeque<>();
            for (Operator op : formOps) {
                if (Thread.currentThread().isInterrupted()) break; // cancelled
                try {
                    state = processOperator(op, state, stack, formRes, g2d, null, 1);
                } catch (Exception ignore) { /* tolerate per-op errors */ }
            }
        } catch (Exception e) {
            LOG.fine(() -> "Annotation appearance render failed: " + e.getMessage());
        }
    }

    private static org.aspose.pdf.engine.pdfobjects.PdfBase resolveRef(
            org.aspose.pdf.engine.pdfobjects.PdfBase b) {
        if (b instanceof org.aspose.pdf.engine.pdfobjects.PdfObjectReference) {
            try { return ((org.aspose.pdf.engine.pdfobjects.PdfObjectReference) b).dereference(); }
            catch (Exception e) { return null; }
        }
        return b;
    }

    /**
     * Processes a sequence of content stream operators.
     */
    private void processOperators(OperatorCollection ops, Resources resources,
                                  Graphics2D g2d, PDFParser parser, int formDepth) {
        Deque<GraphicsState> stateStack = new ArrayDeque<>();
        GraphicsState state = new GraphicsState();

        for (Operator op : ops) {
            // Honour cancellation: render work is CPU-bound and never blocks
            // on I/O, so a cancelled worker (mass-testing timeout, UI abort)
            // otherwise spins for the rest of the page — observed as leaked
            // "zombie" worker threads pinning a core for 15+ minutes.
            if (Thread.currentThread().isInterrupted()) {
                LOG.fine("Render interrupted; abandoning remaining operators");
                break;
            }
            try {
                state = processOperator(op, state, stateStack, resources, g2d, parser, formDepth);
            } catch (Exception e) {
                LOG.fine(() -> "Skipping operator " + op.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Dispatches a single operator. Returns the (potentially replaced) state —
     * callers must use the returned value to handle Q (restore) correctly.
     */
    private GraphicsState processOperator(Operator op, GraphicsState state,
                                 Deque<GraphicsState> stateStack, Resources resources,
                                 Graphics2D g2d, PDFParser parser, int formDepth)
            throws IOException {

        // Use the typed operator subclasses where available
        String name = op.getName();

        switch (name) {
            // ======== Graphics State ========
            case "q":
                stateStack.push(state.clone());
                break;
            case "Q":
                if (!stateStack.isEmpty()) {
                    state = stateStack.pop();
                    // Re-apply clip from restored state
                    applyClip(g2d, state);
                }
                break;
            case "cm":
                if (op instanceof ConcatenateMatrix) {
                    state.concatMatrix(((ConcatenateMatrix) op).getMatrix());
                }
                break;
            case "w":
                if (op instanceof SetLineWidth) {
                    state.setLineWidth(((SetLineWidth) op).getWidth());
                }
                break;
            case "J":
                if (op instanceof SetLineCap) {
                    state.setLineCap(((SetLineCap) op).getLineCap());
                }
                break;
            case "j":
                if (op instanceof SetLineJoin) {
                    state.setLineJoin(((SetLineJoin) op).getLineJoin());
                }
                break;
            case "M":
                if (op instanceof SetMiterLimit) {
                    state.setMiterLimit(((SetMiterLimit) op).getMiterLimit());
                }
                break;
            case "d":
                if (op instanceof SetDash) {
                    SetDash sd = (SetDash) op;
                    double[] da = sd.getDashArray();
                    float[] fda = null;
                    if (da != null && da.length > 0) {
                        fda = new float[da.length];
                        for (int i = 0; i < da.length; i++) fda[i] = (float) da[i];
                    }
                    state.setDash(fda, (float) sd.getDashPhase());
                }
                break;
            case "gs":
                if (op instanceof GS) {
                    applyExtGState(state, resources, ((GS) op).getDictName());
                }
                break;

            // ======== Color — Fill ========
            case "rg":
                if (op instanceof SetRGBColor) {
                    SetRGBColor rgb = (SetRGBColor) op;
                    state.setFillColorRGB(rgb.getR(), rgb.getG(), rgb.getB());
                }
                break;
            case "g":
                if (op instanceof SetGray) {
                    state.setFillColorGray(((SetGray) op).getGray());
                }
                break;
            case "k":
                if (op instanceof SetCMYKColor) {
                    SetCMYKColor cmyk = (SetCMYKColor) op;
                    state.setFillColorCMYK(cmyk.getC(), cmyk.getM(), cmyk.getY(), cmyk.getK());
                }
                break;
            case "sc":
            case "scn":
                applyAdvancedColor(op, state, false);
                break;
            case "cs":
                // Color space selection (§8.6.8) — must be tracked: an scn in
                // a Separation/DeviceN/ICC space is NOT a gray/RGB value.
                // 29077.pdf paints its body text with "/CS1 cs 1 scn"; treating
                // the lone tint as gray rendered white-on-white (invisible).
                state.setFillColorSpace(resolveColorSpaceOperand(op, resources, parser));
                break;

            // ======== Color — Stroke ========
            case "RG":
                if (op instanceof SetRGBColorStroke) {
                    SetRGBColorStroke rgb = (SetRGBColorStroke) op;
                    state.setStrokeColorRGB(rgb.getR(), rgb.getG(), rgb.getB());
                }
                break;
            case "G":
                if (op instanceof SetGrayStroke) {
                    state.setStrokeColorGray(((SetGrayStroke) op).getGray());
                }
                break;
            case "K":
                if (op instanceof SetCMYKColorStroke) {
                    SetCMYKColorStroke cmyk = (SetCMYKColorStroke) op;
                    state.setStrokeColorCMYK(cmyk.getC(), cmyk.getM(), cmyk.getY(), cmyk.getK());
                }
                break;
            case "SC":
            case "SCN":
                applyAdvancedColor(op, state, true);
                break;
            case "CS":
                state.setStrokeColorSpace(resolveColorSpaceOperand(op, resources, parser));
                break;

            // ======== Path Construction ========
            case "m":
                if (op instanceof MoveTo) {
                    state.moveTo(((MoveTo) op).getX(), ((MoveTo) op).getY());
                }
                break;
            case "l":
                if (op instanceof LineTo) {
                    state.lineTo(((LineTo) op).getX(), ((LineTo) op).getY());
                }
                break;
            case "c":
                if (op instanceof CurveTo) {
                    CurveTo ct = (CurveTo) op;
                    state.curveTo(ct.getX1(), ct.getY1(), ct.getX2(), ct.getY2(), ct.getX3(), ct.getY3());
                }
                break;
            case "v":
                if (op instanceof CurveTo1) {
                    CurveTo1 ct = (CurveTo1) op;
                    state.curveToV(ct.getX2(), ct.getY2(), ct.getX3(), ct.getY3());
                }
                break;
            case "y":
                if (op instanceof CurveTo2) {
                    CurveTo2 ct = (CurveTo2) op;
                    state.curveToY(ct.getX1(), ct.getY1(), ct.getX3(), ct.getY3());
                }
                break;
            case "re":
                if (op instanceof Re) {
                    Re re = (Re) op;
                    state.rect(re.getX(), re.getY(), re.getWidth(), re.getHeight());
                }
                break;
            case "h":
                state.closePath();
                break;

            // ======== Path Painting ========
            case "S":
                strokePath(g2d, state);
                finishPathOp(g2d, state);
                break;
            case "s":
                state.closePath();
                strokePath(g2d, state);
                finishPathOp(g2d, state);
                break;
            case "f":
            case "F":
                fillPath(g2d, state, Path2D.WIND_NON_ZERO, resources, new int[]{formDepth}, parser);
                finishPathOp(g2d, state);
                break;
            case "f*":
                fillPath(g2d, state, Path2D.WIND_EVEN_ODD, resources, new int[]{formDepth}, parser);
                finishPathOp(g2d, state);
                break;
            case "B":
                fillPath(g2d, state, Path2D.WIND_NON_ZERO, resources, new int[]{formDepth}, parser);
                strokePath(g2d, state);
                finishPathOp(g2d, state);
                break;
            case "B*":
                fillPath(g2d, state, Path2D.WIND_EVEN_ODD, resources, new int[]{formDepth}, parser);
                strokePath(g2d, state);
                finishPathOp(g2d, state);
                break;
            case "b":
                state.closePath();
                fillPath(g2d, state, Path2D.WIND_NON_ZERO, resources, new int[]{formDepth}, parser);
                strokePath(g2d, state);
                finishPathOp(g2d, state);
                break;
            case "b*":
                state.closePath();
                fillPath(g2d, state, Path2D.WIND_EVEN_ODD, resources, new int[]{formDepth}, parser);
                strokePath(g2d, state);
                finishPathOp(g2d, state);
                break;
            case "n":
                // End path without painting
                finishPathOp(g2d, state);
                break;

            // ======== Clipping ========
            case "W":
                state.setPendingClip();
                break;
            case "W*":
                state.setPendingClipEvenOdd();
                break;

            // ======== Text ========
            case "BT":
                state.beginText();
                break;
            case "ET":
                break;
            case "Tf":
                if (op instanceof SelectFont) {
                    SelectFont sf = (SelectFont) op;
                    state.setFont(sf.getFontName(), sf.getSize());
                }
                break;
            case "Td":
                if (op instanceof MoveTextPosition) {
                    MoveTextPosition mtp = (MoveTextPosition) op;
                    state.moveTextPosition(mtp.getX(), mtp.getY());
                }
                break;
            case "TD": {
                // TD sets leading = -ty, then does Td
                List<PdfBase> operands = op.getOperands();
                if (operands.size() >= 2) {
                    double tx = getNumber(operands.get(0));
                    double ty = getNumber(operands.get(1));
                    state.setTextLeading(-ty);
                    state.moveTextPosition(tx, ty);
                }
                break;
            }
            case "Tm":
                if (op instanceof SetTextMatrix) {
                    state.setTextMatrix(((SetTextMatrix) op).getMatrix());
                }
                break;
            case "T*":
                state.nextLine();
                break;
            case "Tc":
                if (op instanceof SetCharacterSpacing) {
                    state.setCharSpacing(((SetCharacterSpacing) op).getCharSpace());
                }
                break;
            case "Tw":
                if (op instanceof SetWordSpacing) {
                    state.setWordSpacing(((SetWordSpacing) op).getWordSpace());
                }
                break;
            case "Tz":
                if (op instanceof SetHorizontalTextScaling) {
                    state.setHorizontalScaling(((SetHorizontalTextScaling) op).getScale());
                }
                break;
            case "TL":
                if (op instanceof SetTextLeading) {
                    state.setTextLeading(((SetTextLeading) op).getLeading());
                }
                break;
            case "Tr":
                if (op instanceof SetTextRenderingMode) {
                    state.setTextRenderingMode(((SetTextRenderingMode) op).getMode());
                }
                break;
            case "Ts":
                if (op instanceof SetTextRise) {
                    state.setTextRise(((SetTextRise) op).getRise());
                }
                break;
            case "Tj":
                if (op instanceof ShowText) {
                    PdfBase strOp = op.getOperands().isEmpty() ? null : op.getOperands().get(0);
                    byte[] raw = (strOp instanceof PdfString) ? ((PdfString) strOp).getBytes() : new byte[0];
                    textRenderer.renderText(g2d, state, raw, resources, parser);
                }
                break;
            case "TJ":
                if (op instanceof SetGlyphsPositionShowText) {
                    PdfArray arr = ((SetGlyphsPositionShowText) op).getArray();
                    textRenderer.renderTJArray(g2d, state, arr, resources, parser);
                }
                break;
            case "'":
                if (op instanceof MoveToNextLineShowText) {
                    state.nextLine();
                    PdfBase strOp = op.getOperands().isEmpty() ? null : op.getOperands().get(0);
                    byte[] raw = (strOp instanceof PdfString) ? ((PdfString) strOp).getBytes() : new byte[0];
                    textRenderer.renderText(g2d, state, raw, resources, parser);
                }
                break;
            case "\"":
                if (op instanceof SetSpacingMoveToNextLineShowText) {
                    SetSpacingMoveToNextLineShowText dq = (SetSpacingMoveToNextLineShowText) op;
                    state.setWordSpacing(dq.getWordSpacing());
                    state.setCharSpacing(dq.getCharSpacing());
                    state.nextLine();
                    List<PdfBase> operands = op.getOperands();
                    PdfBase strOp = operands.size() >= 3 ? operands.get(2) : null;
                    byte[] raw = (strOp instanceof PdfString) ? ((PdfString) strOp).getBytes() : new byte[0];
                    textRenderer.renderText(g2d, state, raw, resources, parser);
                }
                break;

            // ======== XObjects ========
            case "Do":
                if (op instanceof Do) {
                    renderXObject(g2d, state, ((Do) op).getXObjectName(), resources, parser, formDepth);
                }
                break;

            // ======== Marked Content (ignored for rendering) ========
            case "BMC":
            case "BDC":
            case "EMC":
            case "MP":
            case "DP":
                break;

            // ======== Type 3 glyph metrics (§9.6.5) ========
            // d0/d1 declare the glyph's width/bbox inside a CharProc stream;
            // the advance is taken from /Widths, so nothing to do here.
            case "d0":
            case "d1":
                break;

            // ======== Shading Fill ========
            case "sh": {
                if (op instanceof ShFill) {
                    String shadingName = ((ShFill) op).getShadingName();
                    PdfDictionary shadings = resources.getShadings();
                    if (shadings != null) {
                        PdfBase shadObj = shadings.get(shadingName);
                        if (shadObj instanceof PdfObjectReference) {
                            try { shadObj = ((PdfObjectReference) shadObj).dereference(); }
                            catch (IOException ex) { shadObj = null; }
                        }
                        if (shadObj instanceof PdfDictionary) {
                            try {
                                org.aspose.pdf.engine.pattern.Shading shading =
                                    org.aspose.pdf.engine.pattern.Shading.parse(shadObj, parser);
                                if (shading != null) {
                                    // Shading coordinates live in CURRENT user
                                    // space (§8.7.4.3) = base device transform ×
                                    // the state CTM. g2d only carries the base
                                    // transform (paths are CTM-transformed per
                                    // op), so compose the CTM in — otherwise a
                                    // shading inside a scaled form evaluates t
                                    // out of range and paints one flat color
                                    // (corpus 10734: gradient banners all dark).
                                    AffineTransform shadingToDevice =
                                        new AffineTransform(g2d.getTransform());
                                    shadingToDevice.concatenate(
                                        matrixToTransform(state.getCTM()));
                                    org.aspose.pdf.engine.pattern.ShadingRenderer.render(
                                        g2d, shading, shadingToDevice, g2d.getClipBounds());
                                }
                            } catch (IOException ex) {
                                LOG.fine(() -> "Failed to render shading: " + ex.getMessage());
                            }
                        }
                    }
                }
                break;
            }

            // ======== Inline Images ========
            case "BI":
                // The parser folds the whole BI..ID..EI object into one BI
                // operator: operands[0] = image dict, operands[1] = raw data.
                renderInlineImage(g2d, state, op, parser);
                break;
            case "ID":
            case "EI":
                // Consumed by the parser into BI's operands — nothing here.
                break;

            default:
                LOG.finest(() -> "Unhandled operator: " + op.getName());
                break;
        }
        return state;
    }

    // ======== Path Painting ========

    private void fillPath(Graphics2D g2d, GraphicsState state, int windingRule) {
        fillPath(g2d, state, windingRule, null, null, null);
    }

    /**
     * @param resources    page (or form/pattern) resources — needed to resolve
     *                     a fill Pattern by name. May be null when the
     *                     caller knows there's no pattern fill in flight.
     * @param formDepthBox single-element int[] holding the current form
     *                     recursion depth so pattern content streams can
     *                     guard against infinite recursion. May be null →
     *                     defaults to depth 0.
     */
    private void fillPath(Graphics2D g2d, GraphicsState state, int windingRule,
                           Resources resources, int[] formDepthBox, PDFParser parser) {
        GeneralPath path = state.getCurrentPath();
        if (path.getBounds2D().isEmpty()) return;

        AffineTransform saved = g2d.getTransform();
        Shape savedClip = g2d.getClip();
        try {
            applyCtmTransform(g2d, state);
            // Honours /ca and /BM (Multiply — corpus 30894 highlight annotations).
            g2d.setComposite(BlendComposite.fillComposite(state));
            path.setWindingRule(windingRule);

            String patternName = state.getFillPatternName();
            if (patternName != null && resources != null) {
                int depth = formDepthBox != null ? formDepthBox[0] : 0;
                // Shading patterns (PatternType 2) paint a gradient inside the
                // path; tiling patterns (PatternType 1) tile a cell.
                if (renderShadingPatternFill(g2d, state, path, resources, patternName, parser)) {
                    return;
                }
                if (renderTilingPatternFill(g2d, state, path, resources, patternName, depth)) {
                    return;
                }
                // Fall through to solid colour if pattern couldn't be rendered
            }
            g2d.setColor(state.getFillColor());
            g2d.fill(path);
        } finally {
            g2d.setTransform(saved);
            g2d.setClip(savedClip);
        }
    }

    /**
     * Fills {@code path} with a shading Pattern (PatternType 2, §8.7.4.3): the
     * pattern's /Shading is painted, clipped to the path, with the pattern
     * /Matrix mapping shading space into the current coordinate system. Returns
     * {@code true} on success, {@code false} so the caller can fall back.
     * <p>Without this, shading-pattern fills dropped to a solid fill colour —
     * black for the gradient-built emoji of corpus 59149.</p>
     */
    private boolean renderShadingPatternFill(Graphics2D g2d, GraphicsState state,
                                             GeneralPath path, Resources resources,
                                             String patternName, PDFParser parser) {
        if (Boolean.getBoolean("openpdf.shadingpattern.disable")) return false;
        try {
            PdfDictionary patterns = resources.getPdfDictionary() != null
                    ? (PdfDictionary) resolveRef(resources.getPdfDictionary().get("Pattern"))
                    : null;
            if (patterns == null) return false;
            PdfBase patBase = resolveRef(patterns.get(patternName));
            if (!(patBase instanceof PdfDictionary)) return false;
            PdfDictionary patDict = (PdfDictionary) patBase;
            if (intOf(patDict.get("PatternType"), 1) != 2) return false;

            PdfBase shadObj = resolveRef(patDict.get("Shading"));
            if (!(shadObj instanceof PdfDictionary)) return false;
            org.aspose.pdf.engine.pattern.Shading shading =
                    org.aspose.pdf.engine.pattern.Shading.parse(shadObj, parser);
            if (shading == null) return false;

            Matrix patMatrix = matrixFromPdfArray(resolveRef(patDict.get("Matrix")));
            if (patMatrix == null) patMatrix = new Matrix(1, 0, 0, 1, 0, 0);

            // g2d already carries base × CTM (applyCtmTransform ran). The pattern
            // matrix maps shading space into that current user space (no /cm runs
            // between setting the pattern and the fill in these streams), so the
            // shading→device map is the current transform × pattern matrix.
            AffineTransform shadingToDevice = new AffineTransform(g2d.getTransform());
            shadingToDevice.concatenate(matrixToTransform(patMatrix));

            Shape savedClip = g2d.getClip();
            try {
                g2d.clip(path);
                java.awt.Rectangle cb = g2d.getClipBounds();
                if (cb == null || cb.isEmpty()) return true; // nothing to paint
                org.aspose.pdf.engine.pattern.ShadingRenderer.render(g2d, shading, shadingToDevice, cb);
            } finally {
                g2d.setClip(savedClip);
            }
            return true;
        } catch (Exception e) {
            LOG.fine(() -> "Shading pattern fill failed for " + patternName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Paints {@code path} (in user space) with the named Tiling Pattern from
     * the current resources. Returns {@code true} on success.
     *
     * <p>The pattern's content stream is rendered inside the user-space clip
     * defined by {@code path}, with the pattern's /Matrix prepended to the
     * current transform. {@code XStep}/{@code YStep} are honoured by tiling
     * the content across the path's bounding box. Tiling beyond the clip is
     * cut off naturally by Java2D's clip; for the common single-tile case
     * (XStep ≥ BBox.W and YStep ≥ BBox.H) only one iteration runs.</p>
     */
    private boolean renderTilingPatternFill(Graphics2D g2d, GraphicsState state,
                                             GeneralPath path, Resources resources,
                                             String patternName, int formDepth) {
        if (Boolean.getBoolean("openpdf.pattern.disable")) return false;
        try {
            org.aspose.pdf.engine.pdfobjects.PdfDictionary patterns =
                    resources.getPdfDictionary() != null
                            ? (org.aspose.pdf.engine.pdfobjects.PdfDictionary) resolveRef(
                                    resources.getPdfDictionary().get("Pattern"))
                            : null;
            if (patterns == null) return false;
            org.aspose.pdf.engine.pdfobjects.PdfBase patBase = resolveRef(patterns.get(patternName));
            if (!(patBase instanceof org.aspose.pdf.engine.pdfobjects.PdfStream)) return false;
            org.aspose.pdf.engine.pdfobjects.PdfStream patStream =
                    (org.aspose.pdf.engine.pdfobjects.PdfStream) patBase;

            int patternType = intOf(patStream.get("PatternType"), 1);
            if (patternType != 1) return false; // Shading patterns (type 2) — TODO

            // Pattern matrix (default identity).
            Matrix patMatrix = matrixFromPdfArray(resolveRef(patStream.get("Matrix")));
            if (patMatrix == null) patMatrix = new Matrix(1, 0, 0, 1, 0, 0);
            // BBox + tiling intervals.
            Rectangle bbox = rectFromPdfArray(resolveRef(patStream.get("BBox")));
            double xStep = numberOf(resolveRef(patStream.get("XStep")), 0);
            double yStep = numberOf(resolveRef(patStream.get("YStep")), 0);
            if (bbox == null || xStep == 0 || yStep == 0) return false;

            // Pattern's own resources.
            org.aspose.pdf.engine.pdfobjects.PdfDictionary patResDict =
                    (org.aspose.pdf.engine.pdfobjects.PdfDictionary) resolveRef(patStream.get("Resources"));
            Resources patResources = patResDict != null
                    ? new Resources(patResDict, null)
                    : resources;

            // Parse pattern content stream into operators.
            byte[] patBytes = patStream.getDecodedData();
            java.util.List<Operator> patOps =
                    org.aspose.pdf.engine.parser.ContentStreamParser.parse(patBytes);
            if (patOps == null) return false;

            // Compute path bounds in PATTERN-SPACE coordinates so we know how
            // many tile iterations to run. clipBounds is in user space; pattern
            // space = user × patMatrix^-1.
            java.awt.geom.AffineTransform patAffine = new java.awt.geom.AffineTransform(
                    patMatrix.getA(), patMatrix.getB(),
                    patMatrix.getC(), patMatrix.getD(),
                    patMatrix.getE(), patMatrix.getF());
            java.awt.geom.AffineTransform inv;
            try { inv = patAffine.createInverse(); }
            catch (java.awt.geom.NoninvertibleTransformException e) { return false; }
            java.awt.geom.Rectangle2D userBounds = path.getBounds2D();
            java.awt.geom.Rectangle2D patBounds = inv.createTransformedShape(userBounds).getBounds2D();

            int iMin = (int) Math.floor((patBounds.getMinX() - bbox.getURX()) / xStep);
            int iMax = (int) Math.ceil((patBounds.getMaxX() - bbox.getLLX()) / xStep);
            int jMin = (int) Math.floor((patBounds.getMinY() - bbox.getURY()) / yStep);
            int jMax = (int) Math.ceil((patBounds.getMaxY() - bbox.getLLY()) / yStep);
            long tileCount = (long) (iMax - iMin + 1) * (jMax - jMin + 1);
            // Single-tile / page-background patterns (where one tile covers the
            // whole fill area) are the high-leverage case: the tile is the
            // visual content (e.g. PDFNEWNET_32223's purple gradient + ribbon
            // background). Fine repeating texture patterns (e.g. cross-hatch
            // shading at <50 user units / step) make small visual contribution
            // but have order-of-magnitude pixel-coverage AND order-of-magnitude
            // rendering cost — and Java's grayscale resampling of these tiny
            // tiles drifts from Aspose's. Cap iteration at a level that keeps
            // single-tile patterns intact and lets fine-texture patterns fall
            // back to the default fill color (matching pre-pattern rendering
            // for tests like PDFNEWNET_31977).
            if (tileCount > 25) {
                LOG.fine(() -> "Pattern tiling skipped for " + patternName + " (tileCount=" + tileCount + ")");
                return false;
            }
            // Clip to path (user space) and apply pattern matrix.
            g2d.clip(path);
            java.awt.geom.AffineTransform afterCtm = g2d.getTransform();
            g2d.transform(patAffine);

            for (int j = jMin; j <= jMax; j++) {
                for (int i = iMin; i <= iMax; i++) {
                    java.awt.geom.AffineTransform tile = new java.awt.geom.AffineTransform(g2d.getTransform());
                    g2d.translate(i * xStep, j * yStep);
                    // The cell content runs with a FRESH GraphicsState. Its
                    // clipPath must be seeded with the path clip — otherwise
                    // the first Q inside the cell calls applyClip(null) and
                    // BLOWS AWAY the clip, splattering the cell across the
                    // page (corpus 16222: a 123pt photo painted 20× over the
                    // article). Clip shapes pass through getClip()/setClip()
                    // in CURRENT user-space coordinates, so both the seed and
                    // the post-cell restore are taken at THIS tile transform —
                    // capturing once outside the loop would shift the clip by
                    // i·XStep/j·YStep per tile.
                    java.awt.Shape tileClip = g2d.getClip();
                    GraphicsState patState = new GraphicsState();
                    if (tileClip != null) {
                        patState.setClipPath(new java.awt.geom.GeneralPath(tileClip));
                    }
                    java.util.Deque<GraphicsState> stack = new java.util.ArrayDeque<>();
                    for (Operator po : patOps) {
                        if (Thread.currentThread().isInterrupted()) break; // cancelled
                        try {
                            patState = processOperator(po, patState, stack, patResources, g2d, null,
                                    formDepth + 1);
                        } catch (Exception e) { /* tolerate per-op */ }
                    }
                    g2d.setClip(tileClip); // undo any clip the cell left behind
                    g2d.setTransform(tile);
                }
            }
            g2d.setTransform(afterCtm);
            return true;
        } catch (Exception e) {
            LOG.fine(() -> "Tiling pattern fill failed for " + patternName + ": " + e.getMessage());
            return false;
        }
    }

    private static Matrix matrixFromPdfArray(org.aspose.pdf.engine.pdfobjects.PdfBase b) {
        if (!(b instanceof org.aspose.pdf.engine.pdfobjects.PdfArray)) return null;
        org.aspose.pdf.engine.pdfobjects.PdfArray a = (org.aspose.pdf.engine.pdfobjects.PdfArray) b;
        if (a.size() != 6) return null;
        return new Matrix(numberOf(a.get(0), 1), numberOf(a.get(1), 0),
                          numberOf(a.get(2), 0), numberOf(a.get(3), 1),
                          numberOf(a.get(4), 0), numberOf(a.get(5), 0));
    }

    private static Rectangle rectFromPdfArray(org.aspose.pdf.engine.pdfobjects.PdfBase b) {
        if (!(b instanceof org.aspose.pdf.engine.pdfobjects.PdfArray)) return null;
        org.aspose.pdf.engine.pdfobjects.PdfArray a = (org.aspose.pdf.engine.pdfobjects.PdfArray) b;
        if (a.size() != 4) return null;
        return Rectangle.fromPdfArray(a);
    }

    private static double numberOf(org.aspose.pdf.engine.pdfobjects.PdfBase b, double def) {
        if (b instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger)
            return ((org.aspose.pdf.engine.pdfobjects.PdfInteger) b).intValue();
        if (b instanceof org.aspose.pdf.engine.pdfobjects.PdfFloat)
            return ((org.aspose.pdf.engine.pdfobjects.PdfFloat) b).doubleValue();
        return def;
    }

    private static int intOf(org.aspose.pdf.engine.pdfobjects.PdfBase b, int def) {
        if (b instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger)
            return ((org.aspose.pdf.engine.pdfobjects.PdfInteger) b).intValue();
        return def;
    }

    private void strokePath(Graphics2D g2d, GraphicsState state) {
        GeneralPath path = state.getCurrentPath();
        // Skip only a path with no segments at all. Do NOT use
        // Rectangle2D.isEmpty() here: an axis-aligned line (the common
        // "table rule" `x y m  x2 y l  S`) has a zero-height/width bounding
        // box, isEmpty() reports true, and the stroke would be dropped.
        if (path.getPathIterator(null).isDone()) return;

        AffineTransform saved = g2d.getTransform();
        try {
            applyCtmTransform(g2d, state);
            if (state.getStrokingAlpha() < 1.0f) {
                g2d.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, state.getStrokingAlpha()));
            } else {
                g2d.setComposite(AlphaComposite.SrcOver);
            }
            g2d.setColor(state.getStrokeColor());
            g2d.setStroke(deviceClampedStroke(state.createStroke(), g2d.getTransform()));
            g2d.draw(path);
        } finally {
            g2d.setTransform(saved);
        }
    }

    /**
     * Clamps a stroke so it never rasterises thinner than one device pixel.
     * <p>
     * ISO 32000 §8.4.3.2: a line width of 0 denotes the thinnest line the
     * device can render — NOT an invisible line. Java2D does not honour that
     * convention (a 0-width stroke under the anti-aliased pipeline draws
     * nothing), and a small positive width under a down-scaling CTM (e.g.
     * 0.05 in corpus 29903.pdf) anti-aliases to invisibility. Reference
     * renderers clamp the effective device width instead; we use the same
     * 0.25-device-pixel floor as Adobe Reader and PDFBox, so a hairline
     * anti-aliases to the same ~25% coverage grey as the reference engine.
     * </p>
     *
     * @param stroke    the stroke built from the graphics state (user-space width)
     * @param transform the full current transform (CTM + device scale)
     * @return the original stroke, or a copy with the width raised to 0.25 device px
     */
    private static BasicStroke deviceClampedStroke(BasicStroke stroke, AffineTransform transform) {
        double scale = Math.sqrt(Math.abs(transform.getDeterminant()));
        if (scale <= 0 || !Double.isFinite(scale)) return stroke;
        float minUserWidth = (float) (0.25 / scale); // 0.25 device px in user units
        if (stroke.getLineWidth() >= minUserWidth) return stroke;
        return new BasicStroke(minUserWidth, stroke.getEndCap(), stroke.getLineJoin(),
                stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase());
    }

    /**
     * Finishes a path operation: apply pending clip, then clear the path.
     */
    private void finishPathOp(Graphics2D g2d, GraphicsState state) {
        if (state.hasPendingClip()) {
            GeneralPath path = (GeneralPath) state.getCurrentPath().clone();
            int rule = state.isPendingClipEvenOdd()
                    ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO;
            path.setWindingRule(rule);

            // Transform path by CTM for clipping in device space
            AffineTransform ctmTransform = matrixToTransform(state.getCTM());
            Shape transformedClip = ctmTransform.createTransformedShape(path);

            if (state.getClipPath() != null) {
                Area existing = new Area(state.getClipPath());
                existing.intersect(new Area(transformedClip));
                state.setClipPath(new GeneralPath(existing));
            } else {
                state.setClipPath(new GeneralPath(transformedClip));
            }
            applyClip(g2d, state);
            state.clearPendingClip();
        }
        state.clearPath();
    }

    // ======== XObjects ========

    private void renderXObject(Graphics2D g2d, GraphicsState state, String xobjName,
                               Resources resources, PDFParser parser, int formDepth) {
        if (resources == null || xobjName == null) return;
        PdfDictionary xobjects = resources.getXObjects();
        if (xobjects == null) return;

        PdfBase val = xobjects.get(xobjName);
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                LOG.fine(() -> "Failed to dereference XObject " + xobjName);
                return;
            }
        }
        if (!(val instanceof PdfStream)) return;
        PdfStream stream = (PdfStream) val;

        String subtype = stream.getNameAsString("Subtype");
        if ("Image".equals(subtype)) {
            renderImage(g2d, state, stream, xobjName, parser);
        } else if ("Form".equals(subtype)) {
            renderForm(g2d, state, stream, xobjName, resources, parser, formDepth);
        }
    }

    /**
     * Renders an inline image (BI..ID..EI, §8.9.7). The content stream parser
     * delivers it as a single BI operator whose operands are the image
     * dictionary and the raw (still encoded) data. Abbreviated keys/values
     * (Table 93/94) are expanded to their canonical names and the result is
     * wrapped in a synthetic {@link PdfStream} so the regular
     * {@link #renderImage} path (incl. stencil-mask handling for Type 3
     * bitmap glyphs) applies unchanged.
     */
    private void renderInlineImage(Graphics2D g2d, GraphicsState state,
                                   Operator op, PDFParser parser) {
        try {
            List<PdfBase> operands = op.getOperands();
            if (operands.size() < 2) return;
            PdfBase dictOp = operands.get(0);
            PdfBase dataOp = operands.get(1);
            if (!(dictOp instanceof PdfDictionary)
                    || !(dataOp instanceof org.aspose.pdf.engine.pdfobjects.PdfString)) {
                return;
            }
            PdfDictionary expanded = expandInlineImageDict((PdfDictionary) dictOp);
            byte[] raw = ((org.aspose.pdf.engine.pdfobjects.PdfString) dataOp).getBytes();
            PdfStream stream = new PdfStream(expanded, raw);
            renderImage(g2d, state, stream, "InlineImage", parser);
        } catch (Exception e) {
            LOG.fine(() -> "Failed to render inline image: " + e.getMessage());
        }
    }

    /** Abbreviated → full inline-image dictionary keys (§8.9.7, Table 93). */
    private static final java.util.Map<String, String> INLINE_KEYS = new java.util.HashMap<>();
    /** Abbreviated → full filter and colour-space names (Table 94 + §8.9.5.2). */
    private static final java.util.Map<String, String> INLINE_NAMES = new java.util.HashMap<>();
    static {
        INLINE_KEYS.put("W", "Width");
        INLINE_KEYS.put("H", "Height");
        INLINE_KEYS.put("BPC", "BitsPerComponent");
        INLINE_KEYS.put("CS", "ColorSpace");
        INLINE_KEYS.put("D", "Decode");
        INLINE_KEYS.put("DP", "DecodeParms");
        INLINE_KEYS.put("F", "Filter");
        INLINE_KEYS.put("IM", "ImageMask");
        INLINE_KEYS.put("I", "Interpolate");
        INLINE_KEYS.put("L", "Length");
        INLINE_NAMES.put("G", "DeviceGray");
        INLINE_NAMES.put("RGB", "DeviceRGB");
        INLINE_NAMES.put("CMYK", "DeviceCMYK");
        INLINE_NAMES.put("I", "Indexed");
        INLINE_NAMES.put("AHx", "ASCIIHexDecode");
        INLINE_NAMES.put("A85", "ASCII85Decode");
        INLINE_NAMES.put("LZW", "LZWDecode");
        INLINE_NAMES.put("Fl", "FlateDecode");
        INLINE_NAMES.put("RL", "RunLengthDecode");
        INLINE_NAMES.put("CCF", "CCITTFaxDecode");
        INLINE_NAMES.put("DCT", "DCTDecode");
    }

    /** Expands abbreviated inline-image keys and name values to canonical form. */
    private static PdfDictionary expandInlineImageDict(PdfDictionary src) {
        PdfDictionary out = new PdfDictionary();
        out.set(org.aspose.pdf.engine.pdfobjects.PdfName.of("Subtype"),
                org.aspose.pdf.engine.pdfobjects.PdfName.of("Image"));
        for (org.aspose.pdf.engine.pdfobjects.PdfName keyName : src.keySet()) {
            String key = keyName.getName();
            String fullKey = INLINE_KEYS.getOrDefault(key, key);
            PdfBase val = src.get(key);
            if (("ColorSpace".equals(fullKey) || "Filter".equals(fullKey))) {
                val = expandInlineName(val);
            }
            out.set(org.aspose.pdf.engine.pdfobjects.PdfName.of(fullKey), val);
        }
        return out;
    }

    /** Expands a name or an array of names via {@link #INLINE_NAMES}. */
    private static PdfBase expandInlineName(PdfBase val) {
        if (val instanceof org.aspose.pdf.engine.pdfobjects.PdfName) {
            String n = ((org.aspose.pdf.engine.pdfobjects.PdfName) val).getName();
            String full = INLINE_NAMES.get(n);
            return full != null ? org.aspose.pdf.engine.pdfobjects.PdfName.of(full) : val;
        }
        if (val instanceof PdfArray) {
            PdfArray in = (PdfArray) val;
            PdfArray out = new PdfArray();
            for (int i = 0; i < in.size(); i++) {
                out.add(expandInlineName(in.get(i)));
            }
            return out;
        }
        return val;
    }

    private void renderImage(Graphics2D g2d, GraphicsState state,
                             PdfStream stream, String name, PDFParser parser) {
        try {
            // Optional raster cap (system property, 0/absent = unlimited):
            // a single scanned image XObject can dwarf the page raster —
            // 13000×9000 RGB is ~470 MB of INT_RGB before scaling down to the
            // page. Mass-testing harnesses set this so a handful of oversized
            // scans across worker threads cannot OOM the shared heap; normal
            // library use is unaffected by default.
            long maxPx = Long.getLong("aspose.pdf.maxImageRasterPixels", 0L);
            if (maxPx > 0) {
                long w = stream.getInt("Width", 0);
                long h = stream.getInt("Height", 0);
                if (w * h > maxPx) {
                    LOG.warning(() -> "Skipping image " + name + ": " + w + "x" + h
                            + " exceeds aspose.pdf.maxImageRasterPixels=" + maxPx);
                    return;
                }
            }
            XImage ximg = new XImage(stream, name, parser);
            BufferedImage img;
            if (ximg.isImageMask()) {
                // Stencil mask (PDF §8.9.6.4): bit 0 paints with current non-stroking
                // color, bit 1 is transparent. The bare {@link XImage#toBufferedImage}
                // preview shows the inverted bit-pattern as opaque pixels — that is the
                // wrong thing to drop on the page. Build a real ARGB mask painted with
                // the current fill colour.
                img = buildStencilMaskImage(ximg, state.getFillColor());
            } else {
                img = ximg.toBufferedImage();
            }
            if (img == null) return;

            // The image is placed into a 1×1 unit square by default.
            // The CTM transforms this square to the desired location and size.
            Matrix ctm = state.getCTM();
            AffineTransform imgTransform = new AffineTransform(
                    ctm.getA(), ctm.getB(),
                    ctm.getC(), ctm.getD(),
                    ctm.getE(), ctm.getF());

            // Scale from unit square to image pixels (image maps to 1×1 in user space)
            imgTransform.concatenate(new AffineTransform(
                    1.0 / Math.max(1, img.getWidth()), 0,
                    0, -1.0 / Math.max(1, img.getHeight()),
                    0, 1));

            AffineTransform saved = g2d.getTransform();
            java.awt.Composite savedComposite = g2d.getComposite();
            try {
                // Extreme downscales (300-dpi scan drawn onto a thumbnail-
                // sized area) make the single native drawImage transform run
                // for minutes — an uninterruptible native call observed as a
                // leaked worker after timeout. Pre-halve the raster until it
                // is within 2× of its device footprint: each halving is a
                // fast pass over ever-smaller data, total work ~4/3 of one
                // pass, and the averaging improves quality vs. point
                // sampling. Only kicks in at ≥16× area ratio.
                BufferedImage toDraw = img;
                AffineTransform deviceTf = new AffineTransform(g2d.getTransform());
                deviceTf.concatenate(imgTransform);
                double devArea = Math.abs(deviceTf.getDeterminant())
                        * (double) img.getWidth() * img.getHeight();
                long imgArea = (long) img.getWidth() * img.getHeight();
                if (devArea >= 1 && imgArea > 16L * devArea) {
                    toDraw = halveToFit(img, devArea);
                    // Map the (smaller) raster into the same unit square.
                    imgTransform.concatenate(new AffineTransform(
                            (double) img.getWidth() / toDraw.getWidth(), 0,
                            0, (double) img.getHeight() / toDraw.getHeight(),
                            0, 0));
                }
                // Honours /ca and /BM (Multiply) for image paints (§11.3.5).
                g2d.setComposite(BlendComposite.fillComposite(state));
                g2d.drawImage(toDraw, imgTransform, null);
            } finally {
                g2d.setTransform(saved);
                g2d.setComposite(savedComposite);
            }
        } catch (Exception e) {
            LOG.fine(() -> "Failed to render image " + name + ": " + e.getMessage());
        }
    }

    /**
     * Repeatedly halves an image until its area is within 4× of the target
     * device area (so the final drawImage scales by at most ~2× per axis).
     * Interruptible between passes.
     */
    private static BufferedImage halveToFit(BufferedImage img, double devArea) {
        BufferedImage cur = img;
        while ((long) cur.getWidth() * cur.getHeight() > 4L * devArea
                && cur.getWidth() > 2 && cur.getHeight() > 2
                && !Thread.currentThread().isInterrupted()) {
            int nw = Math.max(1, cur.getWidth() / 2);
            int nh = Math.max(1, cur.getHeight() / 2);
            int type = cur.getColorModel().hasAlpha()
                    ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage half = new BufferedImage(nw, nh, type);
            Graphics2D hg = half.createGraphics();
            try {
                hg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                hg.drawImage(cur, 0, 0, nw, nh, null);
            } finally {
                hg.dispose();
            }
            cur = half;
        }
        return cur;
    }

    /**
     * Builds a stencil-mask BufferedImage: every "paint" sample (source bit 0)
     * gets the supplied fill colour with full opacity; every "transparent"
     * sample (source bit 1) gets alpha=0 so the page colour shows through.
     *
     * <p>Reads the raw decoded mask bytes directly to avoid the
     * preview-oriented {@link XImage#toBufferedImage} mapping.</p>
     */
    private static BufferedImage buildStencilMaskImage(XImage ximg, java.awt.Color fill) throws IOException {
        int w = ximg.getWidth();
        int h = ximg.getHeight();
        if (w <= 0 || h <= 0) return null;
        byte[] data = ximg.getDecodedData();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int rowBytes = (w + 7) / 8;
        // Honour /Decode [1 0] inversion — common for masks where the encoder
        // stored 1=paint instead of the spec default 0=paint.
        boolean invertDecode = false;
        org.aspose.pdf.engine.pdfobjects.PdfBase decode = ximg.getPdfStream().get("Decode");
        if (decode instanceof org.aspose.pdf.engine.pdfobjects.PdfArray) {
            org.aspose.pdf.engine.pdfobjects.PdfArray a = (org.aspose.pdf.engine.pdfobjects.PdfArray) decode;
            if (a.size() >= 1) {
                Object first = a.get(0);
                if (first instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger) {
                    invertDecode = ((org.aspose.pdf.engine.pdfobjects.PdfInteger) first).intValue() == 1;
                } else if (first instanceof org.aspose.pdf.engine.pdfobjects.PdfFloat) {
                    invertDecode = Math.round(((org.aspose.pdf.engine.pdfobjects.PdfFloat) first).floatValue()) == 1;
                }
            }
        }
        int paintArgb = (fill == null ? 0xFF000000 : (0xFF000000 | (fill.getRGB() & 0x00FFFFFF)));
        for (int y = 0; y < h; y++) {
            int rowBase = y * rowBytes;
            for (int x = 0; x < w; x++) {
                int byteIdx = rowBase + (x >> 3);
                if (byteIdx >= data.length) break;
                int bit = (data[byteIdx] >> (7 - (x & 7))) & 1;
                if (invertDecode) bit ^= 1;
                out.setRGB(x, y, bit == 0 ? paintArgb : 0x00000000);
            }
        }
        return out;
    }

    private void renderForm(Graphics2D g2d, GraphicsState state,
                            PdfStream stream, String name, Resources parentResources,
                            PDFParser parser, int formDepth) {
        if (formDepth >= MAX_FORM_DEPTH) {
            LOG.warning(() -> "Max form XObject recursion depth reached for " + name);
            return;
        }

        try {
            XForm form = new XForm(stream, name, parser);
            OperatorCollection formOps = form.getContents();
            if (formOps == null) return;

            // Form's own resources, falling back to parent
            Resources formRes = form.getResources();
            if (formRes == null) formRes = parentResources;

            // Apply form matrix
            Matrix formMatrix = form.getMatrix();
            GraphicsState formState = state.clone();
            formState.concatMatrix(formMatrix);

            // Process form content stream. processOperator returns the
            // (potentially replaced) state — it MUST be carried forward,
            // otherwise Q never restores state inside the form and q/W/Q
            // clip blocks accumulate by intersection until everything is
            // clipped away (llPDFLib-style per-cell clipping).
            Deque<GraphicsState> formStack = new ArrayDeque<>();
            for (Operator op : formOps) {
                if (Thread.currentThread().isInterrupted()) break; // cancelled
                try {
                    formState = processOperator(op, formState, formStack, formRes, g2d, parser, formDepth + 1);
                } catch (Exception e) {
                    LOG.fine(() -> "Error in form " + name + " operator " + op.getName());
                }
            }
            // The form may have left a narrower device clip behind (unbalanced
            // q/W without a closing Q) — restore the caller's clip.
            applyClip(g2d, state);
        } catch (Exception e) {
            LOG.fine(() -> "Failed to render form XObject " + name + ": " + e.getMessage());
        }
    }

    /**
     * Executes a Type 3 glyph-description content stream (§9.6.5). The glyph
     * state's CTM was pre-multiplied with the font matrix by the caller, so
     * the glyph's path/image operators land at the right spot on the page.
     * Mirrors {@link #renderForm}: per-operator tolerance, state carried
     * through Q, and the caller's device clip restored afterwards.
     */
    private void executeType3GlyphStream(Graphics2D g2d, GraphicsState glyphState,
                                         OperatorCollection ops, Resources resources,
                                         PDFParser parser) {
        java.awt.Shape savedClip = g2d.getClip();
        try {
            Deque<GraphicsState> stack = new ArrayDeque<>();
            GraphicsState state = glyphState;
            for (Operator op : ops) {
                try {
                    state = processOperator(op, state, stack, resources, g2d, parser, 1);
                } catch (Exception e) {
                    LOG.fine(() -> "Type3 glyph operator " + op.getName()
                            + " failed: " + e.getMessage());
                }
            }
        } finally {
            g2d.setClip(savedClip);
        }
    }

    // ======== ExtGState ========

    private void applyExtGState(GraphicsState state, Resources resources, String gsName) {
        if (resources == null || gsName == null) return;
        PdfDictionary gsDict = resources.getExtGState();
        if (gsDict == null) return;

        PdfBase val = gsDict.get(gsName);
        if (val instanceof PdfObjectReference) {
            try { val = ((PdfObjectReference) val).dereference(); }
            catch (IOException e) { return; }
        }
        if (!(val instanceof PdfDictionary)) return;

        ExtGState gs = new ExtGState((PdfDictionary) val);
        double lw = gs.getLineWidth();
        if (lw >= 0) state.setLineWidth(lw);
        int lc = gs.getLineCap();
        if (lc >= 0) state.setLineCap(lc);
        int lj = gs.getLineJoin();
        if (lj >= 0) state.setLineJoin(lj);
        double ml = gs.getMiterLimit();
        if (ml >= 0) state.setMiterLimit(ml);

        state.setStrokingAlpha((float) gs.getStrokingAlpha());
        state.setNonStrokingAlpha((float) gs.getNonStrokingAlpha());
        state.setBlendMode(gs.getBlendMode());
    }

    // ======== Advanced color ========

    /**
     * Resolves the cs/CS operand (a color-space name — either a device space
     * or a key into the resources /ColorSpace dictionary) to a ColorSpaceBase.
     * Returns null on failure so sc/scn falls back to by-count mapping.
     */
    private org.aspose.pdf.engine.colorspace.ColorSpaceBase resolveColorSpaceOperand(
            Operator op, Resources resources, PDFParser parser) {
        List<PdfBase> operands = op.getOperands();
        if (operands.isEmpty()) return null;
        try {
            return org.aspose.pdf.engine.colorspace.ColorSpaceBase.resolve(
                    operands.get(0), resources, parser);
        } catch (Exception e) {
            LOG.fine(() -> "Failed to resolve color space operand: " + e.getMessage());
            return null;
        }
    }

    private void applyAdvancedColor(Operator op, GraphicsState state, boolean stroke) {
        List<PdfBase> operands = op.getOperands();
        if (operands.isEmpty()) return;

        // Pattern colorspace: scn/SCN ends with the pattern resource name
        // (e.g. `/CS0 cs /P0 scn`). Stash the name so the next f/B/etc. can
        // look up the Tiling/Shading Pattern and paint with it.
        PdfBase last = operands.get(operands.size() - 1);
        if (last instanceof org.aspose.pdf.engine.pdfobjects.PdfName) {
            String name = ((org.aspose.pdf.engine.pdfobjects.PdfName) last).getName();
            if (stroke) state.setStrokePatternName(name);
            else state.setFillPatternName(name);
            return;
        }
        if (stroke) state.setStrokePatternName(null);
        else state.setFillPatternName(null);

        // Try to extract numeric components
        int numComponents = 0;
        for (PdfBase b : operands) {
            if (b instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger
                    || b instanceof org.aspose.pdf.engine.pdfobjects.PdfFloat) {
                numComponents++;
            }
        }

        // The components belong to the color space selected by cs/CS — a
        // single Separation tint or N DeviceN tints must run through the
        // tint transform, not be misread as gray/RGB by component count.
        org.aspose.pdf.engine.colorspace.ColorSpaceBase activeCs =
                stroke ? state.getStrokeColorSpace() : state.getFillColorSpace();
        if (activeCs != null && numComponents > 0
                && numComponents == activeCs.getNumberOfComponents()) {
            double[] comps = new double[numComponents];
            int ci = 0;
            for (PdfBase b : operands) {
                if (b instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger
                        || b instanceof org.aspose.pdf.engine.pdfobjects.PdfFloat) {
                    comps[ci++] = getNumber(b);
                }
            }
            try {
                java.awt.Color color = new java.awt.Color(activeCs.toRGBInt(comps), false);
                if (stroke) state.setStrokeColor(color);
                else state.setFillColor(color);
                return;
            } catch (Exception e) {
                LOG.fine(() -> "Color space conversion failed, falling back: " + e.getMessage());
            }
        }

        if (numComponents == 3) {
            double r = getNumber(operands.get(0));
            double g = getNumber(operands.get(1));
            double b = getNumber(operands.get(2));
            if (stroke) state.setStrokeColorRGB(r, g, b);
            else state.setFillColorRGB(r, g, b);
        } else if (numComponents == 4) {
            double c = getNumber(operands.get(0));
            double m = getNumber(operands.get(1));
            double y = getNumber(operands.get(2));
            double k = getNumber(operands.get(3));
            if (stroke) state.setStrokeColorCMYK(c, m, y, k);
            else state.setFillColorCMYK(c, m, y, k);
        } else if (numComponents == 1) {
            double gray = getNumber(operands.get(0));
            if (stroke) state.setStrokeColorGray(gray);
            else state.setFillColorGray(gray);
        }
    }

    // ======== Helpers ========

    private void applyCtmTransform(Graphics2D g2d, GraphicsState state) {
        Matrix ctm = state.getCTM();
        g2d.transform(matrixToTransform(ctm));
    }

    private AffineTransform matrixToTransform(Matrix m) {
        return new AffineTransform(m.getA(), m.getB(), m.getC(), m.getD(), m.getE(), m.getF());
    }

    private void applyClip(Graphics2D g2d, GraphicsState state) {
        GeneralPath clip = state.getClipPath();
        if (clip != null) {
            g2d.setClip(clip);
        } else {
            g2d.setClip(null);
        }
    }

    private void applyRotation(Graphics2D g2d, int degrees, double pageW, double pageH) {
        // /Rotate is the number of degrees the page is rotated CLOCKWISE for
        // display (ISO 32000-1 §7.7.3.3). Our enclosing transform already did
        // translate(0, pixelH); scale(s, -s); i.e. user space has Y pointing up
        // and visible region is [0, displayW] x [0, displayH] in user-space units
        // (where displayW/H were chosen to fit the rotated page).
        //
        // We map PDF user coords (px, py) ∈ [0, pageW] x [0, pageH] into that
        // visible region so the page appears rotated CW by /Rotate degrees.
        //
        // 90°  CW: (px,py) -> (py,            pageW - px)  rotate(-90°), translate(0, pageW)
        // 180°    : (px,py) -> (pageW - px,   pageH - py)  rotate(180°), translate(pageW, pageH)
        // 270° CW: (px,py) -> (pageH - py,    px       )  rotate(+90°), translate(pageH, 0)
        //
        // In Java2D, `translate(tx, ty); rotate(theta)` composes as M = T * R,
        // so points are rotated first then translated. Java2D's rotate(+theta)
        // is mathematically CCW (matrix [cos -sin; sin cos]); after our Y-flip
        // it appears as CW on screen, but for the matrix math here we work in
        // post-flip user space where Y is up and `rotate(+theta)` is CCW.
        switch (degrees) {
            case 90:
                g2d.translate(0, pageW);
                g2d.rotate(Math.toRadians(-90));
                break;
            case 180:
                g2d.translate(pageW, pageH);
                g2d.rotate(Math.toRadians(180));
                break;
            case 270:
                g2d.translate(pageH, 0);
                g2d.rotate(Math.toRadians(90));
                break;
            default:
                break;
        }
    }

    private static double getNumber(PdfBase val) {
        if (val instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger) {
            return ((org.aspose.pdf.engine.pdfobjects.PdfInteger) val).intValue();
        }
        if (val instanceof org.aspose.pdf.engine.pdfobjects.PdfFloat) {
            return ((org.aspose.pdf.engine.pdfobjects.PdfFloat) val).doubleValue();
        }
        return 0;
    }
}
