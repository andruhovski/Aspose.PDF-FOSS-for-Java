package org.aspose.pdf.engine.xfa.flatten.paint;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.font.StandardFonts;
import org.aspose.pdf.engine.layout.ContentStreamBuilder;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.binding.FormField;
import org.aspose.pdf.engine.xfa.flatten.XfaGeometry;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.ContentArea;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/// Paints positioned XFA content (Stage C, sprint C2) into a page content stream:
/// box model (fill + border edges + corners), field/caption text, honouring
/// `presence`. Coordinates come from the C1 resolver ([XfaGeometry]) —
/// paint lands exactly where C1 positioned it. Flowed content (no positioned
/// geometry) is NOT painted (deferred to C3); `presence` hidden/invisible is
/// NOT painted (this suppresses the draft-watermark exposure, ticket 46656).
///
/// Document order = Z-order: containers paint before their children, so a
/// subform background sits behind its fields. Reuses [ContentStreamBuilder]
/// and the standard-font metric tables — no second rendering stack.
public final class XfaPainter {

    /// Decoded Image XObject per `<image>` element, so a logo recurring on every page of a
    /// multi-page form is decoded+deflated once and its XObject shared (weak keys → GC'd with the DOM).
    private static final Map<Element, org.aspose.pdf.engine.pdfobjects.PdfStream> IMAGE_CACHE
            = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    private XfaPainter() {
    }

    /// Outcome of a paint pass.
    public static final class Result {
        /// Objects that produced marks.
        public int painted;
        /// Objects skipped because `presence` = hidden/invisible (incl. subtree).
        public int presenceHidden;
        /// Objects skipped because they are flowed (no positioned geometry — C3).
        public int flowedSkipped;
        /// Objects skipped because `relevant` excludes the print context (interactive-only).
        public int printSkipped;
        /// Box fills painted.
        public int fills;
        /// Box borders painted.
        public int borders;
        /// Text runs painted (field values).
        public int texts;
        /// Captions painted.
        public int captions;
        /// Image XObjects painted (logos / pictures).
        public int images;
    }

    /// Paints the positioned content of `dom` onto page 1 of `doc`.
    ///
    /// @param doc the document (a page is added if it has none)
    /// @param dom the merged Form DOM
    /// @param tpl the template (for the contentArea origin), or `null`
    /// @return the paint result
    /// @throws Exception on document access failure
    public static Result paint(Document doc, FormDom dom, Template tpl) throws Exception {
        return paint(doc, dom, tpl, XfaFontResolver.create(null));
    }

    /// Paints the positioned content of `dom`, embedding real fonts resolved by `resolver`
    /// (XFA-FONTEMBED) when available, else the standard-14 substitute.
    ///
    /// @param doc      the document
    /// @param dom      the merged Form DOM
    /// @param tpl      the template, or `null`
    /// @param resolver the font resolver (embedded>host>substitution); never `null`
    /// @return the paint result
    /// @throws Exception on document access failure
    public static Result paint(Document doc, FormDom dom, Template tpl, XfaFontResolver resolver) throws Exception {
        Result r = new Result();
        if (doc.getPages().getCount() == 0) {
            doc.getPages().add();
        }
        Page page = doc.getPages().get(1);
        // P2: size the page to the XFA <medium> (the form's declared page size), replacing
        // any placeholder/inverted MediaBox; Y-flip against the medium height.
        double pageH;
        if (tpl != null) {
            double[] medium = org.aspose.pdf.engine.xfa.flatten.XfaMedium.resolve(tpl);
            page.setPageSize(medium[0], medium[1]);
            pageH = medium[1];
        } else {
            pageH = Math.abs(page.getRect().getHeight());
        }

        ContentStreamBuilder b = buildContent(dom, tpl, pageH, r, resolver);
        byte[] bytes = b.toByteArray();
        if (bytes.length > 0) {
            page.appendToContentStream(bytes);
            mergeFonts(page, b);
            mergeImages(page, b);
        }
        return r;
    }

    /// Builds the paint content stream for `dom` at the given page height WITHOUT
    /// attaching it to any page (the geometry/self-consistency oracle inspects the bytes).
    ///
    /// @param dom    the merged Form DOM
    /// @param tpl    the template (contentArea origin), or `null`
    /// @param pageH  the page height in points (for the Y-flip)
    /// @param r      the result accumulator (filled in)
    /// @return the builder holding the painted bytes and font registrations
    public static ContentStreamBuilder buildContent(FormDom dom, Template tpl, double pageH, Result r) {
        return buildContent(dom, tpl, pageH, r, XfaFontResolver.disabled());
    }

    /// Builds the paint content stream, embedding fonts via `resolver` (XFA-FONTEMBED). The
    /// no-resolver overload uses a [XfaFontResolver#disabled()] resolver = the standard-14
    /// substitution behaviour (so the byte-level paint unit tests stay WinAnsi).
    ///
    /// @param resolver the font resolver (embedded>host>substitution)
    public static ContentStreamBuilder buildContent(FormDom dom, Template tpl, double pageH, Result r,
                                                    XfaFontResolver resolver) {
        double[] base = contentAreaOrigin(tpl);
        Map<Element, FormField> byElement = new IdentityHashMap<>();
        for (FormField f : dom.getFields()) {
            if (f.getFormNode() != null) {
                byElement.put(f.getFormNode().getElement(), f);
            }
        }
        ContentStreamBuilder b = new ContentStreamBuilder();
        if (dom.getRoot() != null) {
            paintNode(dom.getRoot().getElement(), byElement, pageH, base[0], base[1], b, r, resolver);
        }
        return b;
    }

    /* ------------------------- L3 per-object reuse hooks ------------------------- */

    /// Whether an object should paint in the print/flatten output: not `presence`
    /// hidden/invisible, and `relevant` does not exclude print. Exposed so the L3
    /// paginator can apply the SAME gating the C2 walk does, per Layout-DOM object.
    ///
    /// @param el the source element
    /// @return `true` if the object (and its subtree) should be painted
    public static boolean isPaintableForPrint(Element el) {
        if (el == null) {
            return false;
        }
        String presence = attr(el, "presence", "visible");
        if ("hidden".equals(presence) || "invisible".equals(presence)) {
            return false;
        }
        return relevantForPrint(el);
    }

    /// Paints one already-placed object (box fill/border + caption/value text) at a
    /// pre-resolved PDF rectangle, reusing the validated C2 paint primitives. The L3 paginator
    /// calls this per Layout-DOM node (with the page's Y-flip already applied) — no paint logic
    /// is re-implemented; this is exactly the body the C2 tree walk runs per positioned node.
    ///
    /// @param el   the source element (font/fill/border/caption read from it)
    /// @param ff   the bound field (value text), or `null`
    /// @param rect the object's rectangle in PDF user space (page-local, already flipped)
    /// @param b    the content stream being built
    /// @param r    the result accumulator
    /// @return `true` if any mark was produced
    public static boolean paintPlaced(Element el, FormField ff, Rectangle rect,
                                      ContentStreamBuilder b, Result r) {
        return paintPlaced(el, ff, rect, b, r, XfaFontResolver.disabled());
    }

    /// Paints one placed object, embedding real fonts via `resolver` (XFA-FONTEMBED) — the L3
    /// paginator's per-node entry.
    ///
    /// @param resolver the font resolver (embedded>host>substitution)
    public static boolean paintPlaced(Element el, FormField ff, Rectangle rect,
                                      ContentStreamBuilder b, Result r, XfaFontResolver resolver) {
        return paintPlaced(el, ff, java.util.Collections.emptyMap(), rect, b, r, resolver);
    }

    /// Paints one placed object with access to the element→field map, so a radio/checkbox can resolve
    /// its selected state from its enclosing `exclGroup`'s value.
    ///
    /// @param byElement element→[FormField] map (for the exclGroup selection lookup)
    public static boolean paintPlaced(Element el, FormField ff, Map<Element, FormField> byElement,
                                      Rectangle rect, ContentStreamBuilder b, Result r,
                                      XfaFontResolver resolver) {
        boolean any = false;
        if (rect.getWidth() > 0 && rect.getHeight() > 0) {
            any = paintBox(el, rect, b, r);
        }
        // A <value><line> shape is a zero-width (vertical) or zero-height (horizontal) rule — e.g. the
        // BillingAddress box's "Line2" divider (w=0) between the two mailing columns — so paint shapes
        // outside the positive-area guard (paintDrawShape itself ignores a degenerate rectangle).
        any |= paintDrawShape(el, rect, b, r);
        any |= paintText(el, ff, byElement, rect, b, r, resolver);
        if (any) {
            r.painted++;
        }
        return any;
    }

    /// Paints a field's static chrome only — its box (fill/border) and its `<caption>` label —
    /// but NOT its bound value. Used by the AcroForm converter: the value is carried by the interactive
    /// widget placed over this rect, so painting it here too would double-render it (the merged Form DOM
    /// keeps the value in `<value><text>`, which [#paintPlaced] would otherwise draw).
    ///
    /// @return `true` if anything was drawn
    public static boolean paintBoxAndCaption(Element el, Rectangle rect, ContentStreamBuilder b,
                                             Result r, XfaFontResolver resolver) {
        boolean any = false;
        if (rect.getWidth() > 0 && rect.getHeight() > 0) {
            any = paintBox(el, rect, b, r);
        }
        // Mirror the render track's caption/value split (paintText) so the converted AcroForm's static
        // chrome lines up with the rendered form: the caption goes in its reserved strip and the white
        // <ui> data-entry box is painted in the value strip — the editable widget then sits over that box
        // (it would otherwise show the section/field fill through an empty widget — the "blue value box").
        Element captionEl = firstChildOf(el, "caption");
        String caption = captionString(captionEl);
        boolean hasCaption = caption != null && !caption.isEmpty()
                && (captionEl == null || isVisiblePresence(captionEl));
        Rectangle captionRect = rect;
        Rectangle valueRect = rect;
        if (hasCaption && rect.getWidth() > 1 && rect.getHeight() > 1) {
            String placement = attr(captionEl, "placement", "left");
            double reserve = measure(attr(captionEl, "reserve", "0"));
            if (Double.isNaN(reserve) || reserve <= 0) {
                reserve = captionReserve(captionEl, el, caption, placement, resolver);
            }
            Element checkButton = checkButtonUi(el);
            double minValue = checkButton != null ? checkButtonSize(checkButton, rect) + 2 : 0;
            Rectangle[] split = splitForCaption(rect, placement, reserve, minValue);
            captionRect = split[0];
            valueRect = split[1];
        }
        if (hasCaption) {
            List<Para> capParas = captionRuns(captionEl);
            if (capParas != null) {
                if (drawRichText(captionEl, capParas, captionRect, b, resolver)) {
                    r.captions++;
                    any = true;
                }
            } else if (drawText(el, captionEl, caption, captionRect, b, resolver)) {
                r.captions++;
                any = true;
            }
        }
        if (paintUiBox(el, valueRect, b, r)) {
            any = true;
        }
        if (any) {
            r.painted++;
        }
        return any;
    }

    /// Attaches a built content stream to a page and registers its fonts (the exact attach
    /// step [#paint] performs), for the L3 per-page emit.
    ///
    /// @param page the target page
    /// @param b    the built content
    /// @throws Exception on document access failure
    public static void attach(Page page, ContentStreamBuilder b) throws Exception {
        byte[] bytes = b.toByteArray();
        if (bytes.length > 0) {
            page.appendToContentStream(bytes);
            mergeFonts(page, b);
            mergeImages(page, b);
        }
    }

    /* ------------------------------ tree walk ------------------------------ */

    private static void paintNode(Element el, Map<Element, FormField> byElement, double pageH,
                                  double baseX, double baseY, ContentStreamBuilder b, Result r,
                                  XfaFontResolver resolver) {
        String presence = attr(el, "presence", "visible");
        if ("hidden".equals(presence) || "invisible".equals(presence)) {
            r.presenceHidden++; // not painted, subtree suppressed (watermark fix)
            return;
        }
        if (!relevantForPrint(el)) {
            r.printSkipped++; // relevant="-print" → interactive-only (button, on-screen note); Adobe's
            return;           // print omits it, so the flatten/print output must too (subtree skipped).
        }
        if (isFlowed(el)) {
            r.flowedSkipped++; // flowed container -> its subtree is C3
            return;
        }

        // Paint this node's box + content if it has a resolvable positioned rect. A text-bearing
        // <draw> (a label like "Title"/"Date:") or auto-sized field often carries x/y but NO w/h —
        // it auto-sizes to its content. Such a node yields a zero-area rect; we must still paint its
        // text (the box is simply skipped). Gating text on a positive area was the cause of ~12 of
        // ~13 FLATTENTALL labels never painting (only the one draw with explicit w+h showed).
        if (el.hasAttribute("x") && el.hasAttribute("y")) {
            Rectangle rect = XfaGeometry.resolve(XfaNodeFactory.wrap(el, null), pageH, baseX, baseY);
            if (rect != null) {
                boolean any = false;
                if (rect.getWidth() > 0 && rect.getHeight() > 0) {
                    any = paintBox(el, rect, b, r);
                }
                any |= paintDrawShape(el, rect, b, r); // zero-w/h <line> rules paint outside the area guard
                FormField ff = byElement.get(el);
                any |= paintText(el, ff, byElement, rect, b, r, resolver);
                if (any) {
                    r.painted++;
                }
            }
        }

        // Recurse positioned children (containers + fields/draws).
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            String ln = local(c);
            if (LAYOUT.contains(ln)) {
                paintNode(c, byElement, pageH, baseX, baseY, b, r, resolver);
            }
        }
    }

    /* ------------------------------ box model ------------------------------ */

    private static boolean paintBox(Element el, Rectangle rect, ContentStreamBuilder b, Result r) {
        // A checkButton field draws its own circle/square widget (paintCheckButton) — skip the generic
        // box so we don't stroke a full-field rectangle over (or instead of) the small radio/checkbox.
        if (checkButtonUi(el) != null) {
            return false;
        }
        // The FIELD-level <border>/<fill> frame the WHOLE object (a subform section frame, or a field
        // that declares its own outer border). The data-entry box that Adobe sinks INTO the value area
        // lives on the <ui> widget instead and is painted by {@link #paintUiBox} around the value rect
        // only — drawing it here (around the full rect) would swallow the caption strip, collapsing the
        // caption+value into one cell (the "two-column instead of three" defect on 11367).
        Element fill = firstChildOf(el, "fill");
        Element border = firstChildOf(el, "border");
        if (fill == null && border != null) {
            fill = firstChildOf(border, "fill");
        }
        return drawBoxBorderFill(border, fill, rect, b, r);
    }

    /// Paints a `<draw>` whose `<value>` is a vector shape — a `<rectangle>` (optionally
    /// rounded and/or filled) or a straight `<line>`. XFA stores page-furniture frames and rules as
    /// a shape _value_ rather than a container `<border>`: e.g. 11902's full-page rounded
    /// `Rectangle1` frame lives in the `<pageArea>` as `<value><rectangle><edge/><corner
    /// radius="5.08mm"/>`. A `<rectangle>`'s `<edge>`+`<corner>` children are exactly a
    /// single-edge rounded border, so this reuses the box border/fill primitive.
    ///
    /// @return `true` if any mark was produced
    private static boolean paintDrawShape(Element el, Rectangle rect, ContentStreamBuilder b, Result r) {
        Element value = firstChildOf(el, "value");
        if (value == null) {
            return false;
        }
        Element rectShape = firstChildOf(value, "rectangle");
        if (rectShape != null) {
            // The rectangle element IS the border (its <edge>/<corner> children); its <fill> (if any)
            // fills the shape. drawBoxBorderFill reads exactly these children. Square corners: Adobe draws
            // these page-frame rectangles sharp even though the template carries a <corner radius>.
            return drawBoxBorderFill(rectShape, firstChildOf(rectShape, "fill"), rect, b, r, null, false);
        }
        Element line = firstChildOf(value, "line");
        if (line != null) {
            Element edge = firstChildOf(line, "edge");
            if (strokeEdge(b, edge != null ? edge : line)) {
                double llx = rect.getLLX(), lly = rect.getLLY(), w = rect.getWidth(), h = rect.getHeight();
                // <line slope>: "\" (default) runs top-left → bottom-right, "/" bottom-left → top-right.
                // A degenerate (zero-width/height) box is an orthogonal rule along its extent.
                boolean up = "/".equals(attr(line, "slope", "\\"));
                if (h == 0) {
                    b.moveTo(llx, lly);
                    b.lineTo(llx + w, lly);
                } else if (w == 0) {
                    b.moveTo(llx, lly);
                    b.lineTo(llx, lly + h);
                } else if (up) {
                    b.moveTo(llx, lly);
                    b.lineTo(llx + w, lly + h);
                } else {
                    b.moveTo(llx, lly + h);
                    b.lineTo(llx + w, lly);
                }
                b.stroke();
                b.restoreState();
                r.borders++;
                return true;
            }
        }
        return false;
    }

    /// Paints the `<ui>` widget's data-entry box (its `<border>`/`<fill>`) around the
    /// field's **value** rectangle — the sunken/edged input box Adobe draws for the editable area,
    /// NOT spanning the caption strip. A checkButton's widget is excluded (it draws its own mark via
    /// [#paintCheckButton]).
    ///
    /// @param el        the field element
    /// @param valueRect the value sub-rectangle (full rect minus the caption reserve)
    /// @return `true` if any mark was produced
    private static boolean paintUiBox(Element el, Rectangle valueRect, ContentStreamBuilder b, Result r) {
        Element ui = firstChildOf(el, "ui");
        Element widget = ui != null ? firstEl(ui) : null;
        if (widget == null || "checkButton".equals(local(widget))) {
            return false;
        }
        Element border = firstChildOf(widget, "border");
        Element fill = firstChildOf(widget, "fill");
        if (fill == null && border != null) {
            fill = firstChildOf(border, "fill");
        }
        if (border == null && fill == null) {
            return false;
        }
        // A data-entry widget's <fill> defaults to WHITE: an empty <fill/> (present, no <color>) is how
        // these forms author the white input box that sits on the grey section background. Without this
        // default the box stays unfilled and shows the section grey through it (the "fields not white").
        return drawBoxBorderFill(border, fill, valueRect, b, r, WHITE);
    }

    /// White, the default fill of a field data-entry box when its `<fill>` declares no colour.
    private static final float[] WHITE = {1f, 1f, 1f};

    /// Strokes/fills a box from a resolved `<border>`/`<fill>` pair within `rect`.
    private static boolean drawBoxBorderFill(Element border, Element fill, Rectangle rect,
                                             ContentStreamBuilder b, Result r) {
        return drawBoxBorderFill(border, fill, rect, b, r, null);
    }

    /// Strokes/fills a box from a resolved `<border>`/`<fill>` pair within `rect`.
    /// `defaultFill` (or `null`) is used when a present, visible `<fill>` declares no
    /// `<color>` — white for a field data-entry box, nothing for a generic container.
    private static boolean drawBoxBorderFill(Element border, Element fill, Rectangle rect,
                                             ContentStreamBuilder b, Result r, float[] defaultFill) {
        return drawBoxBorderFill(border, fill, rect, b, r, defaultFill, true);
    }

    /// As [#drawBoxBorderFill(Element, Element, Rectangle, ContentStreamBuilder, Result, float\[\])]
    /// but `roundCorners=false` forces square corners regardless of any `<corner radius>` —
    /// used for `<value><rectangle>` page-frame shapes, which Adobe draws with sharp corners even
    /// when the template declares a corner radius.
    private static boolean drawBoxBorderFill(Element border, Element fill, Rectangle rect,
                                             ContentStreamBuilder b, Result r, float[] defaultFill,
                                             boolean roundCorners) {
        boolean any = false;
        double llx = rect.getLLX(), lly = rect.getLLY(), w = rect.getWidth(), h = rect.getHeight();
        if (w <= 0 || h <= 0) {
            return false;
        }

        if (fill != null && !"0".equals(attr(fill, "presence", "1")) && isVisiblePresence(fill)) {
            float[] c = color(firstChildOf(fill, "color"), defaultFill);
            if (c != null) {
                b.saveState();
                b.setRGBFillColor(c[0], c[1], c[2]);
                b.rectangle(llx, lly, w, h);
                b.fill();
                b.restoreState();
                r.fills++;
                any = true;
            }
        }

        // border: one or more <edge> (uniform if single; per-side top/right/bottom/left).
        if (border != null && isVisiblePresence(border)) {
            List<Element> edges = childrenOf(border, "edge");
            List<Element> corners = childrenOf(border, "corner");
            double radius = (!roundCorners || corners.isEmpty()) ? 0
                    : measure(attr(firstChildOf(border, "corner"), "radius", "0pt"));
            if (!edges.isEmpty()) {
                if (edges.size() == 1 && radius <= 0) {
                    if (strokeEdge(b, edges.get(0))) {
                        b.rectangle(llx, lly, w, h);
                        b.stroke();
                        b.restoreState();
                        r.borders++;
                        any = true;
                    }
                } else if (edges.size() == 1) {
                    if (strokeEdge(b, edges.get(0))) {
                        roundedRect(b, llx, lly, w, h, Math.min(radius, Math.min(w, h) / 2));
                        b.stroke();
                        b.restoreState();
                        r.borders++;
                        any = true;
                    }
                } else {
                    // per-side: edges in document order = top, right, bottom, left
                    double[][] sides = {
                            {llx, lly + h, llx + w, lly + h}, // top
                            {llx + w, lly + h, llx + w, lly},  // right
                            {llx + w, lly, llx, lly},          // bottom
                            {llx, lly, llx, lly + h}           // left
                    };
                    boolean drew = false;
                    for (int i = 0; i < sides.length && i < edges.size(); i++) {
                        if (strokeEdge(b, edges.get(i))) {
                            b.moveTo(sides[i][0], sides[i][1]);
                            b.lineTo(sides[i][2], sides[i][3]);
                            b.stroke();
                            b.restoreState();
                            drew = true;
                        }
                    }
                    if (drew) {
                        r.borders++;
                        any = true;
                    }
                }
            }
        }
        return any;
    }

    /// Sets up state for one edge (color + width); returns false to skip an invisible edge.
    private static boolean strokeEdge(ContentStreamBuilder b, Element edge) {
        if (edge == null || !isVisiblePresence(edge)) {
            return false;
        }
        String stroke = attr(edge, "stroke", "solid");
        if ("none".equals(stroke)) {
            return false;
        }
        double thickness = measure(attr(edge, "thickness", "0.5pt"));
        if (thickness <= 0) {
            thickness = 0.5;
        }
        float[] c = color(firstChildOf(edge, "color"), new float[]{0, 0, 0});
        b.saveState();
        b.setLineWidth(thickness);
        b.setRGBStrokeColor(c[0], c[1], c[2]);
        return true;
    }

    /// Approximates a rounded rectangle subpath with four Bézier corners.
    private static void roundedRect(ContentStreamBuilder b, double x, double y, double w, double h, double rad) {
        double k = 0.5523 * rad;
        double xr = x + w, yt = y + h;
        b.moveTo(x + rad, y);
        b.lineTo(xr - rad, y);
        b.curveTo(xr - rad + k, y, xr, y + rad - k, xr, y + rad);
        b.lineTo(xr, yt - rad);
        b.curveTo(xr, yt - rad + k, xr - rad + k, yt, xr - rad, yt);
        b.lineTo(x + rad, yt);
        b.curveTo(x + rad - k, yt, x, yt - rad + k, x, yt - rad);
        b.lineTo(x, y + rad);
        b.curveTo(x, y + rad - k, x + rad - k, y, x + rad, y);
        b.closePath();
    }

    /* ------------------------------- image ---------------------------------- */

    /// The decoded bytes of an object's embedded `<value><image>` (a base64 logo/picture), or
    /// `null` when the object carries no image. XFA stores the image inline as the base64 text
    /// of an `<image contentType="image/...">` element (the `href`-referenced external form
    /// is not embedded and is not resolved here).
    private static byte[] imageBytes(Element el) {
        Element value = firstChildOf(el, "value");
        Element image = value == null ? null : firstChildOf(value, "image");
        if (image == null) {
            return null;
        }
        String b64 = image.getTextContent();
        if (b64 == null || b64.trim().isEmpty()) {
            return null;
        }
        try {
            // XFA wraps the base64 with whitespace/newlines for readability — strip it before decoding.
            return java.util.Base64.getMimeDecoder().decode(b64.replaceAll("\\s+", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /// Paints an embedded image into `box`, honouring the XFA `<image aspect>` fit policy:
    /// `none` stretches to the box; `actual` draws at native size (clamped to the box);
    /// everything else (`fit`, the default) scales the picture to fit inside the box preserving
    /// its aspect ratio, centred. The image is registered as a page Image XObject and painted with a
    /// `cm`+`Do`.
    private static boolean paintImage(Element el, byte[] raw, Rectangle box, ContentStreamBuilder b,
                                      Result r) {
        if (box.getWidth() <= 0 || box.getHeight() <= 0) {
            return false;
        }
        Element value = firstChildOf(el, "value");
        Element imageEl = value == null ? null : firstChildOf(value, "image");
        // Decode each <image> at most ONCE per document and share the XObject across pages — a master
        // header logo recurs on every physical page, so re-decoding+re-deflating it per page is the
        // dominant cost of an image-heavy multi-page form (it timed out 001-flat). The cached stream is
        // safely shared: each page's /Resources/XObject references the same object.
        org.aspose.pdf.engine.pdfobjects.PdfStream xobj;
        synchronized (IMAGE_CACHE) {
            xobj = imageEl != null ? IMAGE_CACHE.get(imageEl) : null;
            if (xobj == null) {
                xobj = XfaImageXObject.decode(raw);
                if (imageEl != null && xobj != null) {
                    IMAGE_CACHE.put(imageEl, xobj);
                }
            }
        }
        if (xobj == null) {
            return false;
        }
        double nativeW = xobj.getInt("Width", 0);
        double nativeH = xobj.getInt("Height", 0);
        String aspect = attr(imageEl, "aspect", "fit");

        double bw = box.getWidth(), bh = box.getHeight();
        double w = bw, h = bh; // default = fill the box ("none" = stretch)
        if (!"none".equals(aspect) && nativeW > 0 && nativeH > 0) {
            if ("actual".equals(aspect)) {
                w = Math.min(nativeW, bw);
                h = Math.min(nativeH, bh);
            } else { // fit (default) / height / width — scale preserving aspect to fit the box
                double scale = Math.min(bw / nativeW, bh / nativeH);
                w = nativeW * scale;
                h = nativeH * scale;
            }
        }
        // Centre within the box (XFA hAlign/vAlign default to centre for an image).
        double x = box.getLLX() + (bw - w) / 2;
        double y = box.getLLY() + (bh - h) / 2;

        String key = "xfaimg" + System.identityHashCode(imageEl) + "@" + raw.length;
        String resName = b.registerImage(key, xobj);
        b.saveState();
        b.concatMatrix(w, 0, 0, h, x, y);
        b.drawXObject(resName);
        b.restoreState();
        r.images++;
        return true;
    }

    /* --------------------------- checkButton / radio ---------------------------- */

    /// The checkButton widget's drawn side length: its `<checkButton size>` clamped to the box.
    private static double checkButtonSize(Element cb, Rectangle box) {
        double size = measure(attr(cb, "size", "10pt"));
        if (Double.isNaN(size) || size <= 0) {
            size = 10;
        }
        return Math.min(size, Math.min(box.getWidth(), box.getHeight()));
    }

    /// @return the field's `<ui><checkButton>` element (radio/checkbox widget), or null.
    private static Element checkButtonUi(Element el) {
        Element ui = firstChildOf(el, "ui");
        return ui == null ? null : firstChildOf(ui, "checkButton");
    }

    /// @return the field's `<ui><button>` element (push button), or null.
    private static Element buttonUi(Element el) {
        Element ui = firstChildOf(el, "ui");
        return ui == null ? null : firstChildOf(ui, "button");
    }

    /// Whether a field is an interactive push button (a `<ui><button>`) that is visible but
    /// `relevant="-print"` — i.e. the `+`/`-` row-add/remove controls and similar
    /// on-screen-only buttons. The print/flatten/render tracks omit these (Adobe's print does too), but
    /// the AcroForm converter realises them as clickable widgets, so the editable form keeps them.
    ///
    /// @param el the field element
    /// @return `true` for a visible `<ui><button>` field
    public static boolean isInteractiveButton(Element el) {
        if (el == null || buttonUi(el) == null) {
            return false;
        }
        String presence = attr(el, "presence", "visible");
        return !"hidden".equals(presence) && !"invisible".equals(presence) && !"inactive".equals(presence);
    }

    /// The JavaScript of a field's `click` event (`<event activity="click"><script>`), or
    /// `null`. Attached to the converted AcroForm button as a `/JavaScript` action so the
    /// control is wired (full dynamic add/remove still needs the XFA runtime).
    ///
    /// @param el the field element
    /// @return the click script source, or `null`
    public static String clickScript(Element el) {
        if (el == null) {
            return null;
        }
        for (Element ev : childrenOf(el, "event")) {
            if ("click".equals(attr(ev, "activity", ""))) {
                Element script = firstChildOf(ev, "script");
                if (script != null) {
                    String s = script.getTextContent();
                    if (s != null && !s.trim().isEmpty()) {
                        return s;
                    }
                }
            }
        }
        return null;
    }

    /// Convert-mode sink: while set, [#paintText] records every positioned _field_'s value
    /// rect here (and skips painting the value) so the AcroForm converter can realise an editable widget
    /// over it. Used for master-page furniture fields, which are otherwise painted as static (the header /
    /// conditions fields like "Vendor Contact", "Measure/Install Start Date" were thus not editable).
    private static final ThreadLocal<java.util.function.BiConsumer<Element, Rectangle>> FIELD_CAPTURE =
            new ThreadLocal<>();

    /// Starts capturing positioned-field value rects into `sink` on this thread (see
    /// [#FIELD\_CAPTURE]); the converter wraps its furniture paint with this. Field values are NOT
    /// painted while capturing — the editable widget carries them.
    ///
    /// @param sink receives each captured (field element, value rect)
    public static void beginFieldCapture(java.util.function.BiConsumer<Element, Rectangle> sink) {
        FIELD_CAPTURE.set(sink);
    }

    /// Stops field-rect capture on this thread.
    public static void endFieldCapture() {
        FIELD_CAPTURE.remove();
    }

    /// The text of a field's `<caption>` (e.g. a button's `+`/`-` label), or
    /// `null`. Exposed so the converter can set the AcroForm button's `/CA` caption.
    ///
    /// @param el the field element
    /// @return the caption text, or `null`
    public static String captionTextOf(Element el) {
        return el == null ? null : captionString(firstChildOf(el, "caption"));
    }

    /// Whether a field is mandatory — it has a `<validate nullTest="error">` (an empty value is a
    /// validation error). The AcroForm converter highlights such fields (red border) when empty, the way
    /// Adobe flags required fields in an interactive XFA form.
    ///
    /// @param el the field element
    /// @return `true` if the field is required
    public static boolean isMandatory(Element el) {
        Element validate = firstChildOf(el, "validate");
        return validate != null && "error".equals(attr(validate, "nullTest", ""));
    }

    /// Paints an interactive push button's static chrome — its raised/edged box and centred caption
    /// (`+` / `-`) — into the page content, so the control is visible in any viewer. The
    /// clickable widget is layered over this rect by the converter.
    ///
    /// @return `true` if anything was drawn
    public static boolean paintButton(Element el, Rectangle rect, ContentStreamBuilder b, Result r,
                                      XfaFontResolver resolver) {
        boolean any = false;
        if (rect.getWidth() > 0 && rect.getHeight() > 0) {
            any = paintBox(el, rect, b, r);
        }
        Element capEl = firstChildOf(el, "caption");
        String cap = captionString(capEl);
        if (cap != null && !cap.isEmpty() && drawText(el, capEl, cap, rect, b, resolver)) {
            r.captions++;
            any = true;
        }
        if (any) {
            r.painted++;
        }
        return any;
    }

    /// Strokes a red rectangle around `rect` — the converter's highlight for a mandatory field
    /// that is empty (or otherwise invalid), painted into page content so it shows in any viewer.
    ///
    /// @return `true` (a mark was produced) when the rect is non-degenerate
    public static boolean paintInvalidBorder(Rectangle rect, ContentStreamBuilder b, Result r) {
        if (rect.getWidth() <= 1 || rect.getHeight() <= 1) {
            return false;
        }
        b.saveState();
        b.setRGBStrokeColor(0.85, 0.0, 0.0);
        b.setLineWidth(1.0);
        b.rectangle(rect.getLLX() + 0.5, rect.getLLY() + 0.5,
                rect.getWidth() - 1.0, rect.getHeight() - 1.0);
        b.stroke();
        b.restoreState();
        r.borders++;
        return true;
    }

    /// @return the field's `<ui><barcode>` element (2D/1D barcode widget), or null.
    private static Element barcodeUi(Element el) {
        Element ui = firstChildOf(el, "ui");
        return ui == null ? null : firstChildOf(ui, "barcode");
    }

    /// Whether a field's value is a _generated visual_ (a `<ui><barcode>` symbol or an
    /// embedded `<value><image>` picture) rather than editable text. The AcroForm converter must
    /// paint such a field fully (via [#paintPlaced]) instead of placing an editable text widget,
    /// otherwise the QR symbol / logo is lost (a `TextBoxField` cannot render it). Exposed for the
    /// converter so its output matches the XFA render page-for-page.
    ///
    /// @param el the field element
    /// @return `true` for a barcode or embedded-image field
    public static boolean isGeneratedVisualField(Element el) {
        return el != null && (barcodeUi(el) != null || imageBytes(el) != null);
    }

    /// The authored value font size (points) of a field — its `<font size>` (default 10pt) — i.e.
    /// the size the render track draws the value at. The AcroForm converter sets this on the editable
    /// widget's `/DA` so the widget renders the value at the SAME size as the rendered XFA (a
    /// `/Helv 0 Tf` auto-size otherwise fills the box and prints visibly larger).
    ///
    /// @param el the field element
    /// @return the value font size in points (always > 0)
    public static double valueFontSize(Element el) {
        Element font = descend(el, "font");
        double size = font != null ? measure(attr(font, "size", "10pt")) : 10;
        return size <= 0 ? 10 : size;
    }

    /// The authored value font colour of a field (its `<font><color>`), as RGB in [0,1], or black
    /// when unspecified — paired with [#valueFontSize] to build the widget `/DA`.
    ///
    /// @param el the field element
    /// @return a 3-element RGB array
    public static float[] valueFontColor(Element el) {
        float[] c = fontColor(descend(el, "font"));
        return c != null ? c : new float[]{0f, 0f, 0f};
    }

    /// The field's value horizontal alignment as an AcroForm quadding code (`/Q`: 0=left, 1=centre,
    /// 2=right), read from its `<para hAlign>`. The AcroForm converter sets this on the editable
    /// widget so a right-aligned numeric column (`hAlign="right"`) reads the same as the rendered
    /// XFA — the widget appearance otherwise left-aligns every value.
    ///
    /// @param el the field element
    /// @return the quadding code (0/1/2)
    public static int valueQuadding(Element el) {
        Element para = descend(el, "para");
        String h = para != null ? attr(para, "hAlign", "left") : "left";
        if ("center".equals(h)) {
            return 1;
        }
        if ("right".equals(h)) {
            return 2;
        }
        return 0; // left / justify / radix → left
    }

    /// Renders a `<ui><barcode type="QRCode">` field's value as a real QR Code centred in
    /// `box`. The symbol is generated by [org.aspose.pdf.engine.barcode.QrEncoder] from the
    /// field's content (its computed/bound `rawValue`), rasterized to a 1-bit image and scaled to
    /// the largest square fitting the box (with the spec quiet zone). Only the QRCode type is rendered;
    /// other barcode symbologies (PDF417, DataMatrix, 1D) are not yet generated and leave the box empty.
    ///
    /// @param barcode the `<barcode>` widget element (for `type`/`errorCorrectionLevel`)
    /// @param content the data to encode (already the field's effective value)
    /// @return `true` if a symbol was painted
    private static boolean paintBarcode(Element barcode, String content, Rectangle box,
                                        ContentStreamBuilder b, Result r) {
        if (content == null || content.isEmpty() || box.getWidth() <= 1 || box.getHeight() <= 1) {
            return false;
        }
        String type = attr(barcode, "type", "");
        if (!"QRCode".equalsIgnoreCase(type)) {
            return false; // only QR Code is generated for now
        }
        org.aspose.pdf.engine.barcode.QrEncoder.Ecc ecc = qrEccLevel(barcode);
        boolean[][] matrix;
        try {
            matrix = org.aspose.pdf.engine.barcode.QrEncoder.encode(content, ecc);
        } catch (RuntimeException tooBig) {
            return false; // payload exceeds version-40 capacity — leave the frame empty
        }
        int quiet = 4;
        org.aspose.pdf.engine.pdfobjects.PdfStream xobj = XfaImageXObject.qrImage(matrix, quiet);

        // Size the symbol at its authored module size (<barcode moduleWidth/moduleHeight>), drawn at the
        // QR's natural dimensions and centred in the box — NOT stretched to fill it (Adobe renders the
        // 0.508mm modules these forms specify, so the symbol occupies only part of its large frame). The
        // drawn side spans modules + the quiet zone, so each module lands at exactly moduleWidth. Falls
        // back to the largest fitting square when no module size is declared, or if it would overflow.
        double maxSide = Math.min(box.getWidth(), box.getHeight());
        double module = measure(attr(barcode, "moduleWidth", attr(barcode, "moduleHeight", "")));
        double side = maxSide;
        if (!Double.isNaN(module) && module > 0) {
            double natural = (matrix.length + 2 * quiet) * module;
            if (natural > 0 && natural <= maxSide) {
                side = natural;
            }
        }
        double x = box.getLLX() + (box.getWidth() - side) / 2;
        double y = box.getLLY() + (box.getHeight() - side) / 2;
        String key = "xfaqr" + System.identityHashCode(barcode) + "@" + content.length() + "x" + matrix.length;
        String resName = b.registerImage(key, xobj);
        b.saveState();
        b.concatMatrix(side, 0, 0, side, x, y);
        b.drawXObject(resName);
        b.restoreState();
        r.images++;
        return true;
    }

    /// Maps an XFA `<barcode errorCorrectionLevel>` to a [QrEncoder.Ecc] level. The XFA
    /// attribute is a 0–8 index of increasing recovery; we bucket it into the four QR levels
    /// (L/M/Q/H), defaulting to M when unspecified.
    private static org.aspose.pdf.engine.barcode.QrEncoder.Ecc qrEccLevel(Element barcode) {
        String v = attr(barcode, "errorCorrectionLevel", "");
        int lvl;
        try {
            lvl = Integer.parseInt(v.trim());
        } catch (NumberFormatException ex) {
            return org.aspose.pdf.engine.barcode.QrEncoder.Ecc.MEDIUM;
        }
        if (lvl <= 1) {
            return org.aspose.pdf.engine.barcode.QrEncoder.Ecc.LOW;
        }
        if (lvl <= 3) {
            return org.aspose.pdf.engine.barcode.QrEncoder.Ecc.MEDIUM;
        }
        if (lvl <= 5) {
            return org.aspose.pdf.engine.barcode.QrEncoder.Ecc.QUARTILE;
        }
        return org.aspose.pdf.engine.barcode.QrEncoder.Ecc.HIGH;
    }

    /// Draws a checkButton widget in `box`: a circle (`shape="round"` = radio) or square
    /// (`shape="square"` = checkbox) outline at the button's size, plus a filled inner mark when
    /// this option is selected. The selected state is the field's own value, else the value of its
    /// enclosing `exclGroup` (radios bind `none` and share the group's single value).
    private static boolean paintCheckButton(Element el, FormField ff, Map<Element, FormField> byElement,
                                            Element cb, Rectangle box, ContentStreamBuilder b, Result r) {
        boolean round = "round".equals(attr(cb, "shape", "square"));
        double size = checkButtonSize(cb, box);
        if (size <= 0.5) {
            return false;
        }
        double x = box.getLLX() + 1;
        double y = box.getLLY() + (box.getHeight() - size) / 2;

        Element border = firstChildOf(cb, "border");
        Element edge = border != null ? firstChildOf(border, "edge") : null;
        double thick = edge != null ? measure(attr(edge, "thickness", "0.5pt")) : 0.5;
        if (Double.isNaN(thick) || thick <= 0) {
            thick = 0.5;
        }
        float[] ec = edge != null ? color(firstChildOf(edge, "color"), new float[]{0, 0, 0})
                : new float[]{0, 0, 0};

        b.saveState();
        b.setLineWidth(thick);
        b.setRGBStrokeColor(ec[0], ec[1], ec[2]);
        if (round) {
            roundedRect(b, x, y, size, size, size / 2);
        } else {
            b.rectangle(x, y, size, size);
        }
        b.stroke();
        b.restoreState();

        if (isChecked(el, ff, byElement)) {
            double inset = size * 0.28;
            double m = size - 2 * inset;
            b.saveState();
            b.setRGBFillColor(0, 0, 0);
            if (round) {
                roundedRect(b, x + inset, y + inset, m, m, m / 2);
            } else {
                b.rectangle(x + inset, y + inset, m, m);
            }
            b.fill();
            b.restoreState();
        }
        r.borders++;
        return true;
    }

    /// Whether this checkButton option is selected (its on-value matches the field/exclGroup value).
    private static boolean isChecked(Element el, FormField ff, Map<Element, FormField> byElement) {
        String onValue = firstItemText(el);
        if (onValue == null || onValue.isEmpty()) {
            onValue = "1";
        }
        String current = ff != null ? ff.getValue() : null;
        if (current == null || current.isEmpty()) {
            current = exclGroupValue(el, byElement);
        }
        return current != null && onValue.trim().equals(current.trim());
    }

    /// The first `<items>` entry of a field — a checkButton's checked ("on") value.
    ///  Items may be typed `<text>`, `<integer>` or `<decimal>`.
    private static String firstItemText(Element el) {
        Element items = firstChildOf(el, "items");
        if (items == null) {
            return null;
        }
        for (org.w3c.dom.Node c = items.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                String s = c.getTextContent();
                return s == null ? null : s.trim();
            }
        }
        return null;
    }

    /// True when `value` denotes the CHECKED state of the checkButton field `el`:
    /// it equals the field's authored ON value (first `<items>` entry). When the template
    /// declares no items the XFA default on-value is "1"; generic boolean spellings are accepted
    /// there too. Used by the AcroForm converter so a checkbox whose authored on-value is not
    /// literally "1" (e.g. "2", "03", "Y") still converts checked.
    ///
    /// @param el    the field element
    /// @param value the bound value
    /// @return whether the widget should be checked
    public static boolean isCheckedValue(Element el, String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String on = el != null ? firstItemText(el) : null;
        if (on != null && !on.isEmpty()) {
            return on.equals(value.trim());
        }
        String s = value.trim();
        return s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on")
                || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("checked");
    }

    /// The selected value of the nearest enclosing `exclGroup`, via its [FormField].
    private static String exclGroupValue(Element el, Map<Element, FormField> byElement) {
        org.w3c.dom.Node p = el.getParentNode();
        while (p instanceof Element) {
            Element pe = (Element) p;
            if ("exclGroup".equals(local(pe))) {
                FormField gf = byElement.get(pe);
                return gf != null ? gf.getValue() : null;
            }
            p = p.getParentNode();
        }
        return null;
    }

    /* ------------------------------- text ---------------------------------- */

    private static boolean paintText(Element el, FormField ff, Rectangle rect,
                                     ContentStreamBuilder b, Result r, XfaFontResolver resolver) {
        return paintText(el, ff, java.util.Collections.emptyMap(), rect, b, r, resolver);
    }

    /// The value sub-rectangle of a field box: the full rect minus the `<caption>` reserve, i.e.
    /// where the render track paints the field VALUE. The AcroForm converter places its editable widget
    /// here so the widget sits beside the caption (not over it, the "KSSTCAB" over "Soud" overlap).
    ///
    /// @param el       the field element
    /// @param rect     the field's full box rect
    /// @param resolver the font resolver (for auto caption-reserve measurement)
    /// @return the value sub-rectangle (equals `rect` when there is no visible caption)
    public static Rectangle valueRect(Element el, Rectangle rect, XfaFontResolver resolver) {
        Element captionEl = firstChildOf(el, "caption");
        String caption = captionString(captionEl);
        boolean hasCaption = caption != null && !caption.isEmpty()
                && (captionEl == null || isVisiblePresence(captionEl));
        if (!hasCaption || rect.getWidth() <= 1 || rect.getHeight() <= 1) {
            return rect;
        }
        String placement = attr(captionEl, "placement", "left");
        double reserve = measure(attr(captionEl, "reserve", "0"));
        if (Double.isNaN(reserve) || reserve <= 0) {
            reserve = captionReserve(captionEl, el, caption, placement, resolver);
        }
        Element checkButton = checkButtonUi(el);
        double minValue = checkButton != null ? checkButtonSize(checkButton, rect) + 2 : 0;
        return splitForCaption(rect, placement, reserve, minValue)[1];
    }

    /// The display string for a field's raw bound value: a dropdown/list save code mapped to its paired
    /// display item, then the numeric `<format><picture>` applied — the same value the render track
    /// paints. Used so the converter's widget shows "Krajský soud v Praze" / "0,00", not "KSSTCAB" / "0".
    ///
    /// @param el  the field element
    /// @param raw the raw bound (save) value
    /// @return the display value
    public static String displayValue(Element el, String raw) {
        return applyDisplayPicture(el, displayItem(el, raw));
    }

    private static boolean paintText(Element el, FormField ff, Map<Element, FormField> byElement,
                                     Rectangle rect, ContentStreamBuilder b, Result r,
                                     XfaFontResolver resolver) {
        boolean any = false;
        // The caption occupies a reserved strip of the field box (XFA <caption placement reserve>);
        // the value is drawn in the REMAINING area. Splitting the rect prevents the caption and value
        // colliding at the same left edge (the overlapping "Soud"+value text).
        Element captionEl = firstChildOf(el, "caption");
        String caption = captionString(captionEl);
        boolean hasCaption = caption != null && !caption.isEmpty()
                && (captionEl == null || isVisiblePresence(captionEl));

        String value = ff != null ? ff.getValue() : null;
        if (value == null || value.isEmpty()) {
            value = textOf(el);
        }
        // A list/dropdown field stores a SAVE code ("KSSTCAB", "CZ") but DISPLAYS the paired item
        // ("Krajský soud v Praze", "Česká republika"). Map the bound save value to its display item.
        value = displayItem(el, value);
        // Apply the field's display <format><picture> so a numeric value shows in its authored format
        // (e.g. "0" / "0.00" → "0,00" under the field's num{…zz9.99} picture in the cs_CZ locale). The
        // raw bound value carries no formatting; Adobe paints the picture-formatted display string.
        value = applyDisplayPicture(el, value);

        // A checkButton field (radio/checkbox) draws only a small fixed-size widget on the value side,
        // so its caption may keep the FULL declared reserve (the widget needs just its own width) — a
        // text/dropdown value instead needs the 0.85 clamp so its box never collapses.
        Element checkButton = checkButtonUi(el);

        Rectangle captionRect = rect;
        Rectangle valueRect = rect;
        if (hasCaption && rect.getWidth() > 1 && rect.getHeight() > 1) {
            String placement = attr(captionEl, "placement", "left");
            double reserve = measure(attr(captionEl, "reserve", "0"));
            if (Double.isNaN(reserve) || reserve <= 0) {
                reserve = captionReserve(captionEl, el, caption, placement, resolver);
            }
            double minValue = 0;
            if (checkButton != null) {
                minValue = checkButtonSize(checkButton, rect) + 2;
            }
            Rectangle[] split = splitForCaption(rect, placement, reserve, minValue);
            captionRect = split[0];
            valueRect = split[1];
        }

        // caption label (XFA <caption><value><text>…) in its reserved strip. A caption authored as
        // rich HTML with a superscript/sized span (e.g. "Právní řád založení <sup>I</sup>") is rendered
        // via the inline-run renderer so the marker stays raised + small; a plain caption uses drawText.
        if (hasCaption) {
            List<Para> capParas = captionRuns(captionEl);
            if (capParas != null) {
                if (drawRichText(captionEl, capParas, captionRect, b, resolver)) {
                    r.captions++;
                    any = true;
                }
            } else if (drawText(el, captionEl, caption, captionRect, b, resolver)) {
                r.captions++;
                any = true;
            }
        }

        // The data-entry box (the <ui> widget's sunken/edged border) around the VALUE area only — so
        // the caption strip stays outside it (caption | value box = distinct columns, matching Adobe).
        if (paintUiBox(el, valueRect, b, r)) {
            any = true;
        }

        // An image draw/field (<value><image contentType="image/*">base64</image>, e.g. a company
        // logo) paints its picture into the value strip — not a text value. Honour <image aspect>.
        byte[] img = imageBytes(el);
        if (img != null) {
            if (paintImage(el, img, valueRect, b, r)) {
                any = true;
            }
            return any;
        }

        // A barcode field (<ui><barcode type="QRCode">) renders its value as a generated 2D symbol in
        // the value box, not as text. Only QR Code is generated; other symbologies fall through.
        Element barcode = barcodeUi(el);
        if (barcode != null) {
            if (paintBarcode(barcode, value, valueRect, b, r)) {
                any = true;
            }
            return any;
        }

        // A checkButton field (radio shape="round" / checkbox shape="square") draws its widget — the
        // circle/box + a filled mark when selected — in the value strip, NOT a text value. The
        // selected state comes from the field's own value, or its enclosing exclGroup's selection.
        if (checkButton != null) {
            if (paintCheckButton(el, ff, byElement, checkButton, valueRect, b, r)) {
                any = true;
            }
            return any;
        }

        // Convert-mode capture: a positioned master-furniture FIELD's value is carried by an editable
        // AcroForm widget (the converter places one at this value rect), so record the rect and skip
        // painting the value here. Only fields are captured — draws keep painting their static text.
        if ("field".equals(local(el))) {
            java.util.function.BiConsumer<Element, Rectangle> sink = FIELD_CAPTURE.get();
            if (sink != null) {
                sink.accept(el, valueRect);
                return any;
            }
        }

        // An exclGroup's bound value is its SELECTION (the chosen option's on-value, e.g. "2"); it is
        // shown by the option radios' filled dots, NOT as text. Painting it here stamped the selection
        // number over the first (unselected) radio circle ("2" in both groups). Containers carry no text.
        if ("exclGroup".equals(local(el)) || "subform".equals(local(el))
                || "subformSet".equals(local(el)) || "area".equals(local(el))) {
            return any;
        }

        // field display value (bound) or a <draw>'s static <value><text> (e.g. a watermark label)
        if (value != null && !value.isEmpty()) {
            if (drawText(el, el, value, valueRect, b, resolver)) {
                r.texts++;
                any = true;
            }
        } else {
            // L5.1: a <draw>/field whose value is rich text (<value><exData contentType="text/html">)
            // — the table column headers, instruction paragraphs and disclosure blocks of 408975 are
            // authored this way and were previously dropped (textOf only reads <value><text>). Paint
            // the HTML paragraphs wrapped to the box, honouring each <p>'s own font-size/weight style.
            List<Para> rich = richParagraphs(el);
            if (!rich.isEmpty() && drawRichText(el, rich, valueRect, b, resolver)) {
                r.texts++;
                any = true;
            }
        }
        return any;
    }

    /// Maps a list/dropdown field's bound **save** value to its **display** item. An XFA
    /// `<field>` can carry two `<items>` lists: one of display labels and one
    /// `save="1"` list of stored codes, paired by index. The bound value is the stored code, so
    /// we look it up in the save list and return the label at the same index. Returns `raw`
    /// unchanged when the field has no paired item lists or the code isn't found (already a label, a
    /// free-text field, etc.).
    private static String displayItem(Element fieldEl, String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        List<Element> itemsList = childrenOf(fieldEl, "items");
        if (itemsList.size() < 2) {
            return raw;
        }
        Element saveItems = null;
        Element dispItems = null;
        for (Element it : itemsList) {
            if ("1".equals(attr(it, "save", "0"))) {
                saveItems = it;
            } else {
                dispItems = it;
            }
        }
        if (saveItems == null || dispItems == null) {
            return raw;
        }
        List<String> saves = itemTexts(saveItems);
        List<String> disps = itemTexts(dispItems);
        int idx = saves.indexOf(raw.trim());
        return (idx >= 0 && idx < disps.size()) ? disps.get(idx) : raw;
    }

    /// Applies a field's display `<format><picture>` to a bound value so it shows in the form's
    /// authored shape — currently the numeric pictures (`num{…}`, the bare `zz9.99` digit
    /// mask, or a `num.…{}` sub-clause) which the Czech insolvency forms use for every amount. The
    /// raw value carried by the data ("0", "0.00") has no formatting; Adobe paints the picture-applied
    /// display string ("0,00" under `num{…zz9.99}` in the `cs_CZ` locale). A non-numeric
    /// value, an absent/unsupported picture, or a non-parseable value all return `raw` unchanged,
    /// so dates / text / dropdown displays are never disturbed.
    private static String applyDisplayPicture(Element fieldEl, String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        String picture = formatPicture(fieldEl);
        if (picture == null) {
            return raw;
        }
        picture = picture.trim();
        // Only numeric pictures are handled here. They appear as num{…}, num.<sub>{…} or a bare digit
        // mask containing only z/9/Z/,/./() and currency/sign glyphs. A date/time picture (date{…},
        // time{…}, datetime{…}) or text picture (text{…}) is left to the existing value path.
        String body;
        if (picture.startsWith("num")) {
            int lb = picture.indexOf('{');
            int rb = picture.lastIndexOf('}');
            body = (lb >= 0 && rb > lb) ? picture.substring(lb + 1, rb) : "";
        } else if (picture.startsWith("date") || picture.startsWith("time") || picture.startsWith("text")
                || picture.indexOf('{') >= 0) {
            // A date/time/text picture, OR a special-value category clause we do not format — most
            // commonly a bare zero/null clause like "zero{}"/"null{}" (14758's line-item GROSS_PRICE/
            // AMOUNT, whose data is already "0.00"/"9.25"/"2775.00"). The leading 'z' of "zero" is NOT a
            // digit-suppress mask: applying the mask path produced "0ero{}". Show the raw value verbatim.
            return raw;
        } else {
            body = picture; // a bare digit mask (e.g. "zz9.99")
        }

        double num;
        try {
            num = Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            return raw; // not a number — leave the authored text as-is
        }

        // Split off any literal prefix/suffix around the digit mask (a leading currency glyph like the
        // "$" in "$zzz,zzz,zz9", or a trailing "%"): everything before the first mask character and
        // after the last is emitted verbatim around the formatted number ("$" + "90,000" → "$90,000").
        int first = 0;
        while (first < body.length() && !isMaskChar(body.charAt(first))) {
            first++;
        }
        int last = body.length();
        while (last > first && !isMaskChar(body.charAt(last - 1))) {
            last--;
        }
        String prefix = body.substring(0, first);
        String suffix = body.substring(last);
        body = body.substring(first, last);

        // Fraction digits = digit placeholders (z/9/Z) following the picture's decimal point.
        int frac = 0;
        int dot = body.indexOf('.');
        if (dot >= 0) {
            for (int i = dot + 1; i < body.length(); i++) {
                char c = body.charAt(i);
                if (c == '9' || c == 'z' || c == 'Z') {
                    frac++;
                } else if (c == '(' || c == ')') {
                    // ignore zero-suppression group markers
                } else if (c != ' ') {
                    break; // hit a literal/suffix — stop counting fraction digits
                }
            }
        }
        boolean grouping = body.indexOf(',') >= 0;

        StringBuilder pat = new StringBuilder(grouping ? "#,##0" : "0");
        if (frac > 0) {
            pat.append('.');
            for (int i = 0; i < frac; i++) {
                pat.append('0');
            }
        }
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(localeOf(fieldEl));
        return prefix + new DecimalFormat(pat.toString(), sym).format(num) + suffix;
    }

    /// Whether `c` is a numeric-picture mask character (digit placeholder, group/decimal, paren).
    private static boolean isMaskChar(char c) {
        return c == '9' || c == 'z' || c == 'Z' || c == ',' || c == '.' || c == '(' || c == ')';
    }

    /// The text of a field's `<format><picture>` clause, or `null` if it declares none.
    private static String formatPicture(Element fieldEl) {
        Element format = firstChildOf(fieldEl, "format");
        Element picture = format != null ? firstChildOf(format, "picture") : null;
        if (picture == null) {
            return null;
        }
        String t = picture.getTextContent();
        return (t == null || t.trim().isEmpty()) ? null : t;
    }

    /// The locale governing `el`'s formatting: the nearest `locale` attribute on `el`
    /// or an ancestor (XFA inherits `locale` down the template tree; the Czech forms set it on the
    /// root `<subform locale="cs_CZ">`). Defaults to [Locale#ROOT] (period decimal) when none
    /// is declared, matching the picture's literal symbols.
    private static Locale localeOf(Element el) {
        for (Node n = el; n instanceof Element; n = n.getParentNode()) {
            String loc = ((Element) n).getAttribute("locale");
            if (loc != null && !loc.isEmpty()) {
                return Locale.forLanguageTag(loc.replace('_', '-'));
            }
        }
        return Locale.ROOT;
    }

    /// The trimmed `<text>` children of an `<items>` element, in order.
    private static List<String> itemTexts(Element items) {
        List<String> out = new ArrayList<>();
        for (Element c = firstEl(items); c != null; c = nextEl(c)) {
            if ("text".equals(local(c))) {
                String t = c.getTextContent();
                out.add(t == null ? "" : t.trim());
            }
        }
        return out;
    }

    /// Splits a field box into `[captionRect, valueRect]` for a caption of the given
    /// `placement` occupying `reserve` points along the relevant edge (left/right take a
    /// vertical strip of that width; top/bottom a horizontal strip of that height). The reserve is
    /// clamped so the value area never collapses below \~15% of the box.
    private static Rectangle[] splitForCaption(Rectangle rect, String placement, double reserve) {
        return splitForCaption(rect, placement, reserve, 0);
    }

    /// As [#splitForCaption(Rectangle, String, double)] but reserving at least `minValue`
    /// points for the value side (e.g. a checkButton's small fixed widget), letting the caption keep
    /// its full declared reserve instead of the 0.85 clamp. `minValue ≤ 0` keeps the 0.85 clamp.
    private static Rectangle[] splitForCaption(Rectangle rect, String placement, double reserve,
                                               double minValue) {
        double llx = rect.getLLX(), lly = rect.getLLY(), urx = rect.getURX(), ury = rect.getURY();
        boolean horizontal = !"top".equals(placement) && !"bottom".equals(placement);
        double extent = horizontal ? rect.getWidth() : rect.getHeight();
        double cap = minValue > 0 ? Math.max(0, extent - minValue) : extent * 0.85;
        double res = Math.max(0, Math.min(reserve, cap));
        switch (placement) {
            case "right":
                return new Rectangle[]{new Rectangle(urx - res, lly, urx, ury),
                        new Rectangle(llx, lly, urx - res, ury)};
            case "top":
                return new Rectangle[]{new Rectangle(llx, ury - res, urx, ury),
                        new Rectangle(llx, lly, urx, ury - res)};
            case "bottom":
                return new Rectangle[]{new Rectangle(llx, lly, urx, lly + res),
                        new Rectangle(llx, lly + res, urx, ury)};
            default: // left (XFA default)
                return new Rectangle[]{new Rectangle(llx, lly, llx + res, ury),
                        new Rectangle(llx + res, lly, urx, ury)};
        }
    }

    /// The reserve to allot a caption that did not declare one (auto-size): the measured text width
    /// (+ small pad) for a left/right caption, or about one line height for a top/bottom caption,
    /// using the caption's resolved font.
    private static double captionReserve(Element captionEl, Element fieldEl, String text,
                                         String placement, XfaFontResolver resolver) {
        Element font = descend(captionEl, "font");
        if (font == null) {
            font = descend(fieldEl, "font");
        }
        double size = font != null ? measure(attr(font, "size", "10pt")) : 10;
        if (Double.isNaN(size) || size <= 0) {
            size = 10;
        }
        if ("top".equals(placement) || "bottom".equals(placement)) {
            return size * 1.25;
        }
        boolean bold = font != null && "bold".equalsIgnoreCase(attr(font, "weight", "normal"));
        boolean italic = font != null && "italic".equalsIgnoreCase(attr(font, "posture", "normal"));
        String family = font != null ? attr(font, "typeface", "Helvetica") : "Helvetica";
        XfaFontResolver.Embedded emb = resolver != null ? resolver.resolve(family, bold, italic) : null;
        double w = emb != null ? embeddedWidth(emb, text, size) : stringWidth(mapFont(font), text, size);
        return w + 4;
    }

    /* ----------------------------- rich text (exData/HTML) ----------------------------- */

    /// One styled inline run within a paragraph (an HTML text node or `<span>`).
    private static final class Run {
        final String text;
        final double size;       // points, NaN ⇒ inherit the paragraph/draw size
        final boolean bold;
        final boolean italic;
        final String family;     // null ⇒ inherit
        final double rise;       // baseline shift in points (vertical-align), 0 = baseline (superscript &gt; 0)
        Run(String text, double size, boolean bold, boolean italic, String family, double rise) {
            this.text = text; this.size = size; this.bold = bold; this.italic = italic;
            this.family = family; this.rise = rise;
        }
    }

    /// One HTML paragraph of a rich-text value, with the style overrides parsed from its `<p>`.
    private static final class Para {
        final String text;
        final double size;       // points, NaN ⇒ inherit the draw's <font>
        final boolean bold;
        final boolean italic;
        final String family;     // null ⇒ inherit
        /// Inline runs when the paragraph mixes sizes / baseline shifts (superscripts); else null = use [#text].
        final List<Run> runs;
        Para(String text, double size, boolean bold, boolean italic, String family) {
            this(text, size, bold, italic, family, null);
        }
        Para(String text, double size, boolean bold, boolean italic, String family, List<Run> runs) {
            this.text = text; this.size = size; this.bold = bold; this.italic = italic;
            this.family = family; this.runs = runs;
        }
    }

    /// Extracts the HTML paragraphs of `el`'s `<value><exData contentType="text/html">`.
    /// Each `<p>` becomes one [Para] carrying its own inline `style` font overrides
    /// (font-size / font-weight / font-style / font-family). A `text/plain` or untyped exData is
    /// returned as a single newline-split paragraph. Empty if the object has no exData value.
    private static List<Para> richParagraphs(Element el) {
        List<Para> out = new ArrayList<>();
        Element value = firstChildOf(el, "value");
        Element exData = value == null ? null : firstChildOf(value, "exData");
        if (exData == null) {
            return out;
        }
        String ct = attr(exData, "contentType", "text/plain");
        if (!ct.toLowerCase().contains("html")) {
            String t = exData.getTextContent();
            if (t != null && !t.trim().isEmpty()) {
                out.add(new Para(t.trim(), Double.NaN, false, false, null));
            }
            return out;
        }
        Element body = descendantByLocal(exData, "body");
        Element scope = body != null ? body : exData;
        collectParagraphs(scope, out);
        if (out.isEmpty()) {
            String t = scope.getTextContent();
            if (t != null && !t.trim().isEmpty()) {
                out.add(new Para(normalizeWs(t), Double.NaN, false, false, null));
            }
        }
        return out;
    }

    /// The caption's rich paragraphs **iff** it is HTML carrying an inline run (a superscript / a
    /// sized `<span>`); otherwise `null` so a plain caption keeps the simple single-run
    /// [#drawText] path (unchanged for the overwhelming majority of captions).
    private static List<Para> captionRuns(Element captionEl) {
        List<Para> paras = richParagraphs(captionEl);
        for (Para p : paras) {
            if (p.runs != null) {
                return paras;
            }
        }
        return null;
    }

    /// Walks `<p>` children of the HTML body, each → a styled paragraph (text content flattened).
    private static void collectParagraphs(Element scope, List<Para> out) {
        for (Element c = firstEl(scope); c != null; c = nextEl(c)) {
            if ("p".equals(local(c))) {
                String text = normalizeWs(c.getTextContent());
                String style = c.getAttribute("style");
                double size = styleSize(style);
                boolean bold = styleContains(style, "font-weight", "bold");
                boolean italic = styleContains(style, "font-style", "italic");
                String family = styleValue(style, "font-family");
                // Build inline runs so a superscript/sized <span> (e.g. the footnote markers
                // "Právní řád založení <span vertical-align:3pt;font-size:6pt>I</span>") renders raised
                // and smaller. The <p>'s own vertical-align is the base rise (the marker "I" is the p's
                // direct text); spans may override it back to baseline. Null when uniform → flat path.
                double pRise = styleRise(style);
                List<Run> runs = new ArrayList<>();
                collectRuns(c, size, bold, italic, family, Double.isNaN(pRise) ? 0 : pRise, runs);
                List<Run> inline = needsRuns(runs, size) ? coalesce(runs) : null;
                out.add(new Para(text, size, bold, italic, family, inline));
            } else {
                collectParagraphs(c, out); // div/span wrappers
            }
        }
    }

    /// Walks a `<p>`'s inline content depth-first, accumulating one [Run] per text node with
    /// the font size / weight / posture / family / baseline-shift inherited and overridden by each
    /// enclosing `<span style>`. `vertical-align:Npt` becomes the run's rise (superscript).
    private static void collectRuns(org.w3c.dom.Node node, double size, boolean bold, boolean italic,
                                    String family, double rise, List<Run> out) {
        for (org.w3c.dom.Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.TEXT_NODE) {
                String t = n.getTextContent();
                if (t != null && !t.isEmpty()) {
                    out.add(new Run(t.replaceAll("\\s+", " "), size, bold, italic, family, rise));
                }
            } else if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                String st = e.getAttribute("style");
                double sz = styleSize(st);
                double r2 = styleRise(st);
                collectRuns(e,
                        Double.isNaN(sz) ? size : sz,
                        bold || styleContains(st, "font-weight", "bold"),
                        italic || styleContains(st, "font-style", "italic"),
                        styleValue(st, "font-family") != null ? styleValue(st, "font-family") : family,
                        Double.isNaN(r2) ? rise : r2,
                        out);
            }
        }
    }

    /// Whether the runs vary in size or carry a baseline shift — i.e. inline rendering is needed.
    private static boolean needsRuns(List<Run> runs, double paraSize) {
        for (Run r : runs) {
            if (Math.abs(r.rise) > 0.01) {
                return true;
            }
            double s = Double.isNaN(r.size) ? paraSize : r.size;
            double p = Double.isNaN(paraSize) ? s : paraSize;
            if (Math.abs(s - p) > 0.01) {
                return true;
            }
        }
        return false;
    }

    /// Drops empty/whitespace-only runs and merges adjacent runs that share size/style/rise.
    private static List<Run> coalesce(List<Run> runs) {
        List<Run> out = new ArrayList<>();
        for (Run r : runs) {
            if (r.text == null || r.text.isEmpty()) {
                continue;
            }
            Run prev = out.isEmpty() ? null : out.get(out.size() - 1);
            if (prev != null && prev.bold == r.bold && prev.italic == r.italic
                    && eq(prev.size, r.size) && Math.abs(prev.rise - r.rise) < 0.01
                    && java.util.Objects.equals(prev.family, r.family)) {
                out.set(out.size() - 1, new Run(prev.text + r.text, prev.size, prev.bold, prev.italic,
                        prev.family, prev.rise));
            } else {
                out.add(r);
            }
        }
        // trim leading/trailing whitespace of the whole paragraph
        if (!out.isEmpty()) {
            Run f = out.get(0);
            out.set(0, new Run(f.text.replaceAll("^\\s+", ""), f.size, f.bold, f.italic, f.family, f.rise));
            Run l = out.get(out.size() - 1);
            out.set(out.size() - 1, new Run(l.text.replaceAll("\\s+$", ""), l.size, l.bold, l.italic, l.family, l.rise));
        }
        return out;
    }

    private static boolean eq(double a, double b) {
        return (Double.isNaN(a) && Double.isNaN(b)) || Math.abs(a - b) < 0.01;
    }

    /// A CSS `vertical-align:Npt` (baseline shift) from an inline style, or [Double#NaN].
    private static double styleRise(String style) {
        String v = styleValue(style, "vertical-align");
        if (v == null || v.toLowerCase().contains("baseline")) {
            return Double.NaN;
        }
        double pts = measure(v.trim());
        return pts == 0 ? Double.NaN : pts;
    }

    /// Paints the rich-text paragraphs of a draw, wrapped to the box width and stacked top-down via the
    /// shared [org.aspose.pdf.engine.layout.TextLayoutHelper] word-wrapper (reused, not forked).
    /// Each paragraph uses its own font size/weight if the `<p>` declared one, else the draw's
    /// `<font>`. vAlign top/middle/bottom positions the whole block; hAlign aligns each line.
    private static boolean drawRichText(Element draw, List<Para> paras, Rectangle rect, ContentStreamBuilder b,
                                        XfaFontResolver resolver) {
        Element font = descend(draw, "font");
        double baseSize = font != null ? measure(attr(font, "size", "10pt")) : 10;
        if (baseSize <= 0) {
            baseSize = 10;
        }
        Element para = descend(draw, "para");
        String hAlign = para != null ? attr(para, "hAlign", "left") : "left";
        String vAlign = para != null ? attr(para, "vAlign", "top") : "top";
        float[] defColor = fontColor(font);
        double boxW = rect.getWidth();
        double wrapW = boxW > 2 ? boxW - 2 : Double.MAX_VALUE; // leave a hair of inset; auto-width ⇒ no wrap

        // Lay the paragraphs into concrete styled lines first (so the total height drives vAlign).
        List<String> lines = new ArrayList<>();
        List<String> lineFonts = new ArrayList<>();
        List<XfaFontResolver.Embedded> lineEmbedded = new ArrayList<>();
        List<Double> lineSizes = new ArrayList<>();
        List<List<Run>> lineRuns = new ArrayList<>(); // non-null ⇒ this line is styled inline runs
        double totalH = 0;
        for (Para p : paras) {
            double size = !Double.isNaN(p.size) && p.size > 0 ? p.size : baseSize;
            String baseFont = mapFontStyle(font, p);
            // FONTEMBED: resolve the real family/style; embed it if available, else keep the substitute.
            boolean bold = p.bold || (font != null && "bold".equalsIgnoreCase(attr(font, "weight", "normal")));
            boolean italic = p.italic || (font != null && "italic".equalsIgnoreCase(attr(font, "posture", "normal")));
            String family = familyOf(p.family, font);
            XfaFontResolver.Embedded emb = resolver != null ? resolver.resolve(family, bold, italic) : null;
            // Inline-run paragraph (mixed sizes / superscripts): keep it on one line of styled runs.
            if (p.runs != null && !p.runs.isEmpty()) {
                double maxSize = size;
                for (Run rn : p.runs) {
                    maxSize = Math.max(maxSize, Double.isNaN(rn.size) ? size : rn.size);
                }
                lines.add("");
                lineFonts.add(baseFont);
                lineEmbedded.add(emb);
                lineSizes.add(maxSize);
                lineRuns.add(p.runs);
                totalH += org.aspose.pdf.engine.layout.TextLayoutHelper.getLineHeight(baseFont, maxSize);
                continue;
            }
            List<String> wrapped = (p.text == null || p.text.isEmpty())
                    ? java.util.Collections.singletonList("")
                    : wrapToWidth(p.text, emb, baseFont, size, wrapW);
            for (String ln : wrapped) {
                lines.add(ln);
                lineFonts.add(baseFont);
                lineEmbedded.add(emb);
                lineSizes.add(size);
                lineRuns.add(null);
                totalH += org.aspose.pdf.engine.layout.TextLayoutHelper.getLineHeight(baseFont, size);
            }
        }
        if (lines.isEmpty()) {
            return false;
        }

        // First-line baseline: top-anchored unless the box is tall enough for middle/bottom block placement.
        double firstAscent = 0.72 * lineSizes.get(0);
        double topY;
        if (rect.getHeight() < 1) {
            topY = rect.getURY() - firstAscent;
        } else if ("middle".equals(vAlign)) {
            topY = rect.getLLY() + (rect.getHeight() + totalH) / 2 - firstAscent;
        } else if ("bottom".equals(vAlign)) {
            topY = rect.getLLY() + totalH - firstAscent;
        } else {
            topY = rect.getURY() - firstAscent - 1;
        }

        double ty = topY;
        for (int i = 0; i < lines.size(); i++) {
            String ln = lines.get(i);
            String baseFont = lineFonts.get(i);
            XfaFontResolver.Embedded emb = lineEmbedded.get(i);
            double size = lineSizes.get(i);
            double lh = org.aspose.pdf.engine.layout.TextLayoutHelper.getLineHeight(baseFont, size);
            List<Run> runs = lineRuns.get(i);
            if (runs != null) {
                drawRunLine(runs, font, baseSize, rect, hAlign, defColor, ty, b, resolver);
                ty -= lh;
                continue;
            }
            if (!ln.isEmpty()) {
                double textW = emb != null ? embeddedWidth(emb, ln, size) : stringWidth(baseFont, ln, size);
                double tx;
                if (rect.getWidth() < 1) {
                    tx = rect.getLLX();
                } else if ("center".equals(hAlign)) {
                    tx = rect.getLLX() + (rect.getWidth() - textW) / 2;
                } else if ("right".equals(hAlign)) {
                    tx = rect.getURX() - textW - 1;
                } else {
                    tx = rect.getLLX() + 1;
                }
                String fontRes = emb != null
                        ? b.registerEmbeddedFont(emb.fontKey, emb.type0Dict, emb.reader)
                        : b.registerFont(baseFont);
                b.saveState();
                b.beginText();
                b.setRGBFillColor(defColor[0], defColor[1], defColor[2]);
                b.setFont(fontRes, size);
                b.setTextMatrix(1, 0, 0, 1, tx, ty);
                b.showText(ln);
                b.endText();
                b.restoreState();
            }
            ty -= lh;
        }
        return true;
    }

    /// Draws one line of styled inline [Run]s left-to-right at baseline `ty`: each run uses
    /// its own size and a baseline `rise` (vertical-align) so a small raised marker renders as a
    /// superscript. hAlign positions the whole line by its total advance.
    private static void drawRunLine(List<Run> runs, Element font, double baseSize, Rectangle rect,
                                    String hAlign, float[] color, double ty, ContentStreamBuilder b,
                                    XfaFontResolver resolver) {
        double total = 0;
        for (Run rn : runs) {
            double sz = Double.isNaN(rn.size) ? baseSize : rn.size;
            String bf = mapRunFont(font, rn);
            XfaFontResolver.Embedded emb = runEmbedded(font, rn, resolver);
            total += emb != null ? embeddedWidth(emb, rn.text, sz) : stringWidth(bf, rn.text, sz);
        }
        // Condense the line horizontally when the substitute font runs wider than its cell (Helvetica
        // vs Myriad Pro) so a caption like "05 Korespondenční adresa IV" stays inside its reserve
        // instead of overflowing onto the neighbouring checkbox — matching drawText's scaleX behaviour.
        double scaleX = 1.0;
        if (rect.getWidth() >= 1) {
            double avail = rect.getWidth() - 2;
            if (avail > 0 && total > avail) {
                scaleX = Math.max(0.4, avail / total);
            }
        }
        double effTotal = total * scaleX;
        double tx;
        if (rect.getWidth() < 1) {
            tx = rect.getLLX();
        } else if ("center".equals(hAlign)) {
            tx = rect.getLLX() + (rect.getWidth() - effTotal) / 2;
        } else if ("right".equals(hAlign)) {
            tx = rect.getURX() - effTotal - 1;
        } else {
            tx = rect.getLLX() + 1;
        }
        for (Run rn : runs) {
            double sz = Double.isNaN(rn.size) ? baseSize : rn.size;
            String bf = mapRunFont(font, rn);
            XfaFontResolver.Embedded emb = runEmbedded(font, rn, resolver);
            double w = emb != null ? embeddedWidth(emb, rn.text, sz) : stringWidth(bf, rn.text, sz);
            if (!rn.text.isEmpty()) {
                String fontRes = emb != null
                        ? b.registerEmbeddedFont(emb.fontKey, emb.type0Dict, emb.reader)
                        : b.registerFont(bf);
                b.saveState();
                b.beginText();
                b.setRGBFillColor(color[0], color[1], color[2]);
                b.setFont(fontRes, sz);
                b.setTextMatrix(scaleX, 0, 0, 1, tx, ty + rn.rise);
                b.showText(rn.text);
                b.endText();
                b.restoreState();
            }
            tx += w * scaleX;
        }
    }

    /// Maps a run's family/style (over the draw's `<font>`) to a base-14 font.
    private static String mapRunFont(Element font, Run rn) {
        boolean bold = rn.bold || (font != null && "bold".equalsIgnoreCase(attr(font, "weight", "normal")));
        boolean italic = rn.italic || (font != null && "italic".equalsIgnoreCase(attr(font, "posture", "normal")));
        String tf = rn.family != null ? rn.family.toLowerCase()
                : (font != null ? attr(font, "typeface", "Helvetica").toLowerCase() : "helvetica");
        if (tf.contains("times")) {
            return bold && italic ? "Times-BoldItalic" : bold ? "Times-Bold" : italic ? "Times-Italic" : "Times-Roman";
        }
        if (tf.contains("courier")) {
            return bold && italic ? "Courier-BoldOblique" : bold ? "Courier-Bold" : italic ? "Courier-Oblique" : "Courier";
        }
        return bold && italic ? "Helvetica-BoldOblique" : bold ? "Helvetica-Bold"
                : italic ? "Helvetica-Oblique" : "Helvetica";
    }

    /// Resolves the embedded font for a run (its family/style over the draw's font), or null.
    private static XfaFontResolver.Embedded runEmbedded(Element font, Run rn, XfaFontResolver resolver) {
        if (resolver == null) {
            return null;
        }
        boolean bold = rn.bold || (font != null && "bold".equalsIgnoreCase(attr(font, "weight", "normal")));
        boolean italic = rn.italic || (font != null && "italic".equalsIgnoreCase(attr(font, "posture", "normal")));
        String family = familyOf(rn.family, font);
        return resolver.resolve(family, bold, italic);
    }

    /// The resolution family for a paragraph: its HTML `font-family` (first token), else the draw's typeface.
    private static String familyOf(String htmlFamily, Element font) {
        String fam = htmlFamily;
        if (fam == null || fam.trim().isEmpty()) {
            fam = font != null ? attr(font, "typeface", "Helvetica") : "Helvetica";
        }
        int comma = fam.indexOf(',');
        if (comma >= 0) {
            fam = fam.substring(0, comma);
        }
        return fam.replace("'", "").replace("\"", "").trim();
    }

    /// String width in points using an embedded font's own advance metrics (so alignment matches the glyphs).
    private static double embeddedWidth(XfaFontResolver.Embedded emb, String text, double size) {
        org.aspose.pdf.engine.font.ttf.TrueTypeReader reader = emb.reader;
        int upm = reader.getUnitsPerEm();
        if (upm <= 0) {
            upm = 1000;
        }
        double total = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            total += reader.getAdvanceWidth(reader.getGlyphId(cp)) * 1000.0 / upm;
            i += Character.charCount(cp);
        }
        return total * size / 1000.0;
    }

    /// A named inset (points) of an element's `<margin>` child, or 0 when absent.
    private static double insetOf(Element el, String which) {
        Element margin = el == null ? null : firstChildOf(el, "margin");
        if (margin == null) {
            return 0;
        }
        double v = measure(attr(margin, which, "0"));
        return Double.isNaN(v) ? 0 : v;
    }

    /// Maps the draw's `<font>` merged with a paragraph's HTML style overrides to a base-14 font.
    private static String mapFontStyle(Element font, Para p) {
        boolean bold = p.bold || (font != null && "bold".equalsIgnoreCase(attr(font, "weight", "normal")));
        boolean italic = p.italic || (font != null && "italic".equalsIgnoreCase(attr(font, "posture", "normal")));
        String tf = p.family != null ? p.family.toLowerCase()
                : (font != null ? attr(font, "typeface", "Helvetica").toLowerCase() : "helvetica");
        if (tf.contains("times")) {
            return bold && italic ? "Times-BoldItalic" : bold ? "Times-Bold" : italic ? "Times-Italic" : "Times-Roman";
        }
        if (tf.contains("courier")) {
            return bold && italic ? "Courier-BoldOblique" : bold ? "Courier-Bold" : italic ? "Courier-Oblique" : "Courier";
        }
        return bold && italic ? "Helvetica-BoldOblique" : bold ? "Helvetica-Bold"
                : italic ? "Helvetica-Oblique" : "Helvetica";
    }

    private static String normalizeWs(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }

    /// A CSS `font-size:Npt` (or px/in/mm) from an inline style, or [Double#NaN].
    private static double styleSize(String style) {
        String v = styleValue(style, "font-size");
        if (v == null) {
            return Double.NaN;
        }
        double pts = measure(v.trim());
        return pts > 0 ? pts : Double.NaN;
    }

    private static boolean styleContains(String style, String prop, String want) {
        String v = styleValue(style, prop);
        return v != null && v.toLowerCase().contains(want);
    }

    /// Reads one `prop:value` from a `;`-separated inline CSS style string.
    private static String styleValue(String style, String prop) {
        if (style == null || style.isEmpty()) {
            return null;
        }
        for (String decl : style.split(";")) {
            int i = decl.indexOf(':');
            if (i > 0 && decl.substring(0, i).trim().equalsIgnoreCase(prop)) {
                return decl.substring(i + 1).trim();
            }
        }
        return null;
    }

    private static Element descendantByLocal(Element el, String name) {
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element ce = (Element) n;
                if (name.equals(local(ce))) {
                    return ce;
                }
                Element found = descendantByLocal(ce, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /// Draws one text run inside `rect` using the object's font + para alignment.
    private static boolean drawText(Element fieldEl, Element styleHost, String text, Rectangle rect,
                                    ContentStreamBuilder b, XfaFontResolver resolver) {
        Element font = descend(styleHost, "font");
        if (font == null) {
            font = descend(fieldEl, "font");
        }
        double size = font != null ? measure(attr(font, "size", "10pt")) : 10;
        if (size <= 0) {
            size = 10;
        }
        String baseFont = mapFont(font);
        // FONTEMBED: resolve the requested family/style to a real embedded font when available.
        boolean bold = font != null && "bold".equalsIgnoreCase(attr(font, "weight", "normal"));
        boolean italic = font != null && "italic".equalsIgnoreCase(attr(font, "posture", "normal"));
        String family = font != null ? attr(font, "typeface", "Helvetica") : "Helvetica";
        XfaFontResolver.Embedded emb = resolver != null ? resolver.resolve(family, bold, italic) : null;
        float[] c = fontColor(font);
        // XFA <font underline="1|2"> draws a rule under the run; "0"/absent ⇒ none ("Věřitel prohlašuje").
        boolean underline = font != null && truthyFlag(attr(font, "underline", "0"));

        Element para = descend(styleHost, "para");
        String hAlign = para != null ? attr(para, "hAlign", "left") : "left";
        String vAlign = para != null ? attr(para, "vAlign", "top") : "top";

        // A multiLine field (<ui><textEdit multiLine="1">) — or any value carrying explicit line breaks —
        // WRAPS to the box width and honours its breaks, instead of being condensed onto one shrunken
        // line (the personnel-narrative paragraphs). Needs a real box width to wrap into.
        if ((isMultiLine(fieldEl) || isMultiLine(styleHost) || hasLineBreak(text))
                && rect.getWidth() >= 4 && rect.getHeight() >= 4) {
            return drawMultiLineText(text, baseFont, emb, size, c, hAlign, vAlign, rect, styleHost, b);
        }

        double pad = 2;
        double textW = emb != null ? embeddedWidth(emb, text, size) : stringWidth(baseFont, text, size);
        // A text-bearing <draw> with no explicit w/h auto-sizes to its content: the box IS the
        // text, anchored at (x,y) top-left. hAlign/vAlign within a content-sized box have no
        // visible effect, so place at the left/top edge — NOT at the hAlign side (right-anchoring
        // a zero-width box places text to the LEFT of x, the mispositioned-labels bug).
        boolean autoW = rect.getWidth() < 1;
        boolean autoH = rect.getHeight() < 1;
        // Condense the run horizontally to fit its cell when the substitute font runs wider than the
        // original (Arial vs Myriad Pro): Adobe shrinks the glyph width (fontHorizontalScale) rather
        // than overflow into the neighbour or clip the tail. Scale via the text matrix's a-component.
        double scaleX = 1.0;
        if (!autoW) {
            double avail = rect.getWidth() - 2 * pad;
            if (avail > 0 && textW > avail) {
                scaleX = Math.max(0.4, avail / textW);
            }
        }
        double effW = textW * scaleX;
        double tx;
        if (autoW) {
            tx = rect.getLLX();
        } else {
            switch (hAlign) {
                case "center": tx = rect.getLLX() + (rect.getWidth() - effW) / 2; break;
                case "right":  tx = rect.getURX() - effW - pad; break;
                default:       tx = rect.getLLX() + pad;
            }
        }
        double ascent = 0.72 * size, descent = 0.21 * size;
        // Honour the object's top/bottom margin insets for vertical placement: a banner draws its title
        // with topInset=3mm (which is what centres it in the green band); reading a fixed pad instead
        // left the title riding too high. Insets default below pad, so ordinary fields are unaffected.
        double topInset = Math.max(pad, insetOf(styleHost, "topInset"));
        double botInset = Math.max(pad, insetOf(styleHost, "bottomInset"));
        double ty;
        if (autoH) {
            ty = rect.getURY() - ascent; // top-anchored auto-height: baseline one ascent below top
        } else {
            switch (vAlign) {
                case "middle": ty = rect.getLLY() + (rect.getHeight() - (ascent + descent)) / 2 + descent; break;
                case "bottom": ty = rect.getLLY() + descent + botInset; break;
                default:       ty = rect.getURY() - ascent - topInset; // top
            }
        }

        String fontRes = emb != null
                ? b.registerEmbeddedFont(emb.fontKey, emb.type0Dict, emb.reader)
                : b.registerFont(baseFont);
        b.saveState();
        // Clip the run to its cell so an over-long label/value can't bleed into the neighbouring
        // column (e.g. the 26mm "Právnická osoba" label overrunning into the "Název/obch.firma" box).
        // Skip for an auto-sized (content-sized) box, which has no fixed width to clip to.
        if (!autoW && !autoH && rect.getWidth() > 0 && rect.getHeight() > 0) {
            b.rectangle(rect.getLLX(), rect.getLLY(), rect.getWidth(), rect.getHeight());
            b.clip();
        }
        b.beginText();
        b.setRGBFillColor(c[0], c[1], c[2]);
        b.setFont(fontRes, size);
        b.setTextMatrix(scaleX, 0, 0, 1, tx, ty);
        b.showText(text);
        b.endText();
        // Underline rule: a thin line one descent-ish below the baseline, spanning the drawn (scaled)
        // run width. Stroked outside the text object (path ops can't sit inside BT/ET).
        if (underline) {
            double uy = ty - 0.13 * size;
            b.setLineWidth(Math.max(0.4, 0.05 * size));
            b.setRGBStrokeColor(c[0], c[1], c[2]);
            b.moveTo(tx, uy);
            b.lineTo(tx + effW, uy);
            b.stroke();
        }
        b.restoreState();
        return true;
    }

    /// Word-wraps `text` to `maxWidth`, measuring with the SAME font used to draw it: an
    /// embedded font's own glyph advances when `emb != null` (so the wrap matches the rendered
    /// glyphs — and Adobe, which uses the real Times New Roman / Arial metrics), else the base-14
    /// approximation via [org.aspose.pdf.engine.layout.TextLayoutHelper]. Honours explicit
    /// newlines. Without this, base-14 "Times-Roman" runs slightly wider than the embedded Times New
    /// Roman it draws, so each line broke \~a word early and the last line overflowed a positioned widget.
    private static List<String> wrapToWidth(String text, XfaFontResolver.Embedded emb, String baseFont,
                                            double size, double maxWidth) {
        if (emb == null || maxWidth <= 0) {
            return org.aspose.pdf.engine.layout.TextLayoutHelper.wrapText(text, baseFont, size, maxWidth);
        }
        List<String> out = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                out.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ", -1)) {
                if (line.length() == 0) {
                    line.append(word);
                } else if (embeddedWidth(emb, line + " " + word, size) <= maxWidth) {
                    line.append(' ').append(word);
                } else {
                    out.add(line.toString());
                    line = new StringBuilder(word);
                }
            }
            out.add(line.toString());
        }
        return out;
    }

    /// Whether a field's `<ui><textEdit multiLine="1">` marks it as a wrapping multi-line box.
    private static boolean isMultiLine(Element el) {
        Element ui = el == null ? null : firstChildOf(el, "ui");
        Element te = ui == null ? null : firstChildOf(ui, "textEdit");
        return te != null && truthyFlag(attr(te, "multiLine", "0"));
    }

    /// Whether `text` contains an explicit line break (newline / line-separator).
    private static boolean hasLineBreak(String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n' || ch == '\r' || ch == ' ' || ch == ' ') {
                return true;
            }
        }
        return false;
    }

    /// Draws `text` wrapped to the box width as stacked lines: explicit line breaks split it into
    /// paragraphs, each wrapped to the available width via [org.aspose.pdf.engine.layout.TextLayoutHelper].
    /// Top/middle/bottom `vAlign` positions the block; each line is h-aligned; the run is clipped
    /// to the box. Used for `multiLine` fields (the personnel/budget narratives).
    private static boolean drawMultiLineText(String text, String baseFont, XfaFontResolver.Embedded emb,
                                             double size, float[] c, String hAlign, String vAlign,
                                             Rectangle rect, Element styleHost, ContentStreamBuilder b) {
        double pad = 2;
        double wrapW = rect.getWidth() - 2 * pad;
        if (wrapW < 2) {
            return false;
        }
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\\R|[  ]", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
            } else {
                lines.addAll(wrapToWidth(paragraph, emb, baseFont, size, wrapW));
            }
        }
        if (lines.isEmpty()) {
            return false;
        }
        double lh = org.aspose.pdf.engine.layout.TextLayoutHelper.getLineHeight(baseFont, size);
        double totalH = lh * lines.size();
        double ascent = 0.72 * size;
        double topInset = Math.max(pad, insetOf(styleHost, "topInset"));
        double ty;
        switch (vAlign) {
            case "middle": ty = rect.getLLY() + (rect.getHeight() + totalH) / 2 - ascent; break;
            case "bottom": ty = rect.getLLY() + totalH - ascent; break;
            default:       ty = rect.getURY() - ascent - topInset;
        }
        String fontRes = emb != null
                ? b.registerEmbeddedFont(emb.fontKey, emb.type0Dict, emb.reader)
                : b.registerFont(baseFont);
        b.saveState();
        b.rectangle(rect.getLLX(), rect.getLLY(), rect.getWidth(), rect.getHeight());
        b.clip();
        for (String ln : lines) {
            if (!ln.isEmpty()) {
                double textW = emb != null ? embeddedWidth(emb, ln, size) : stringWidth(baseFont, ln, size);
                double tx;
                switch (hAlign) {
                    case "center": tx = rect.getLLX() + (rect.getWidth() - textW) / 2; break;
                    case "right":  tx = rect.getURX() - textW - pad; break;
                    default:       tx = rect.getLLX() + pad;
                }
                b.beginText();
                b.setRGBFillColor(c[0], c[1], c[2]);
                b.setFont(fontRes, size);
                b.setTextMatrix(1, 0, 0, 1, tx, ty);
                b.showText(ln);
                b.endText();
            }
            ty -= lh;
        }
        b.restoreState();
        return true;
    }

    /* ------------------------------ helpers -------------------------------- */

    private static final java.util.Set<String> LAYOUT = new java.util.HashSet<>(
            java.util.Arrays.asList("subform", "subformSet", "exclGroup", "area", "field", "draw"));

    private static final java.util.Set<String> FLOWED = new java.util.HashSet<>(
            java.util.Arrays.asList("tb", "lr-tb", "rl-tb", "row", "table"));

    private static boolean isFlowed(Element el) {
        String layout = el.getAttribute("layout");
        return layout != null && FLOWED.contains(layout);
    }

    /// The XFA `relevant` rule evaluated for the PRINT context (the flatten/paint output is a
    /// static, print-like rendering). `relevant` is a whitespace-separated list of
    /// `±token`s: `-print` excludes from print (interactive-only — buttons, on-screen
    /// hints; Adobe omits them when printing); `+print` includes; a list of only `+X`
    /// tokens (none `print`) means relevant ONLY elsewhere, so excluded from print. Absent /
    /// empty → relevant everywhere.
    public static boolean relevantForPrint(Element el) {
        String rel = el.getAttribute("relevant");
        if (rel == null || rel.trim().isEmpty()) {
            return true;
        }
        boolean anyPlus = false;
        for (String tok : rel.trim().split("\\s+")) {
            if ("-print".equals(tok)) {
                return false;
            }
            if ("+print".equals(tok)) {
                return true;
            }
            if (tok.startsWith("+")) {
                anyPlus = true; // included only in some other context
            }
        }
        return !anyPlus;
    }

    private static boolean isVisiblePresence(Element el) {
        String p = el.getAttribute("presence");
        return !("hidden".equals(p) || "invisible".equals(p));
    }

    /// The glyph colour of an XFA `<font>`: its text colour lives in `<font><fill><color>`
    /// (the standard form), falling back to a `<font><color>` child, then black. Reading only the
    /// direct `<color>` child missed the `<fill>`-nested colour — e.g. the white title on the
    /// green banner (`<font><fill><color value="255,255,255"/>`) rendered black.
    private static float[] fontColor(Element font) {
        if (font == null) {
            return new float[]{0, 0, 0};
        }
        Element fill = firstChildOf(font, "fill");
        if (fill != null && isVisiblePresence(fill)) {
            float[] c = color(firstChildOf(fill, "color"), null);
            if (c != null) {
                return c;
            }
        }
        return color(firstChildOf(font, "color"), new float[]{0, 0, 0});
    }

    /// XFA `<color value="r,g,b">` (0-255) → normalized rgb; `def` if absent.
    private static float[] color(Element colorEl, float[] def) {
        if (colorEl == null) {
            return def;
        }
        String v = colorEl.getAttribute("value");
        if (v == null || v.isEmpty()) {
            return def;
        }
        String[] parts = v.split(",");
        if (parts.length < 3) {
            return def;
        }
        try {
            float rr = Integer.parseInt(parts[0].trim()) / 255f;
            float gg = Integer.parseInt(parts[1].trim()) / 255f;
            float bb = Integer.parseInt(parts[2].trim()) / 255f;
            return new float[]{rr, gg, bb};
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /// Maps an XFA `<font>` to a standard-14 base font (typeface + weight + posture).
    private static String mapFont(Element font) {
        boolean bold = false, italic = false;
        if (font != null) {
            bold = "bold".equalsIgnoreCase(attr(font, "weight", "normal"));
            italic = "italic".equalsIgnoreCase(attr(font, "posture", "normal"));
        }
        // Arial/Myriad/Helvetica and unknown → Helvetica family; Times → Times; Courier → Courier.
        String tf = font != null ? attr(font, "typeface", "Helvetica").toLowerCase() : "helvetica";
        if (tf.contains("times")) {
            return bold && italic ? "Times-BoldItalic" : bold ? "Times-Bold" : italic ? "Times-Italic" : "Times-Roman";
        }
        if (tf.contains("courier")) {
            return bold && italic ? "Courier-BoldOblique" : bold ? "Courier-Bold" : italic ? "Courier-Oblique" : "Courier";
        }
        return bold && italic ? "Helvetica-BoldOblique" : bold ? "Helvetica-Bold"
                : italic ? "Helvetica-Oblique" : "Helvetica";
    }

    /// String width in points via standard-font metrics (WinAnsi-coded).
    private static double stringWidth(String baseFont, String text, double size) {
        int[] widths = StandardFonts.getWidths(baseFont);
        if (widths == null) {
            return text.length() * size * 0.5; // fallback estimate
        }
        double total = 0;
        for (int i = 0; i < text.length(); i++) {
            int code = ContentStreamBuilder.unicodeToWinAnsi(text.charAt(i));
            if (code < 0 || code >= widths.length) {
                code = 'n';
            }
            total += widths[code];
        }
        return total * size / 1000.0;
    }

    private static double measure(String raw) {
        XfaMeasurement m = XfaMeasurement.parse(raw);
        return m == null ? 0 : XfaGeometry.toPoints(m);
    }

    /// First contentArea origin (points, page top-left) of the template pageSet, or {0,0}.
    private static double[] contentAreaOrigin(Template tpl) {
        if (tpl == null) {
            return new double[]{0, 0};
        }
        List<ContentArea> areas = new ArrayList<>();
        collectContentAreas(tpl.getElement(), areas);
        if (areas.isEmpty()) {
            return new double[]{0, 0};
        }
        ContentArea ca = areas.get(0);
        return new double[]{XfaGeometry.toPoints(ca.getX()), XfaGeometry.toPoints(ca.getY())};
    }

    private static void collectContentAreas(Element el, List<ContentArea> out) {
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            String ln = local(c);
            if ("contentArea".equals(ln)) {
                out.add((ContentArea) XfaNodeFactory.wrap(c, null));
            } else if ("pageSet".equals(ln) || "pageArea".equals(ln)
                    || "subform".equals(ln) || "subformSet".equals(ln) || "area".equals(ln)) {
                // the pageSet may be nested inside the root subform (XFA permits this),
                // so descend containers too — not only pageSet/pageArea.
                collectContentAreas(c, out);
            }
        }
    }

    private static void mergeFonts(Page page, ContentStreamBuilder b) throws Exception {
        PdfDictionary resDict = page.ensureResources().getPdfDictionary();
        PdfBase fontsBase = resDict.get("Font");
        PdfDictionary fonts;
        if (fontsBase instanceof PdfDictionary) {
            fonts = (PdfDictionary) fontsBase;
        } else {
            fonts = new PdfDictionary();
            resDict.set(PdfName.of("Font"), fonts);
        }
        Map<String, PdfDictionary> embedded = b.getEmbeddedFontDicts();
        for (Map.Entry<String, String> e : b.getFontResources().entrySet()) {
            String resName = e.getValue();
            if (fonts.get(resName) != null) {
                continue;
            }
            PdfDictionary embDict = embedded.get(resName);
            if (embDict != null) {
                // FONTEMBED: a resolved real font — attach its Type0/CIDFontType2/FontFile2 graph.
                fonts.set(PdfName.of(resName), embDict);
            } else {
                PdfDictionary fd = new PdfDictionary();
                fd.set(PdfName.of("Type"), PdfName.of("Font"));
                fd.set(PdfName.of("Subtype"), PdfName.of("Type1"));
                fd.set(PdfName.of("BaseFont"), PdfName.of(e.getKey()));
                fd.set(PdfName.of("Encoding"), PdfName.of("WinAnsiEncoding"));
                fonts.set(PdfName.of(resName), fd);
            }
        }
    }

    /// Attaches the painted Image XObjects to the page's `/Resources/XObject` under the resource
    /// names the content stream's `Do` operators reference (mirrors [#mergeFonts]).
    private static void mergeImages(Page page, ContentStreamBuilder b) throws Exception {
        Map<String, org.aspose.pdf.engine.pdfobjects.PdfStream> images = b.getImageXObjectDicts();
        if (images.isEmpty()) {
            return;
        }
        PdfDictionary resDict = page.ensureResources().getPdfDictionary();
        PdfBase xobjBase = resDict.get("XObject");
        PdfDictionary xobjs;
        if (xobjBase instanceof PdfDictionary) {
            xobjs = (PdfDictionary) xobjBase;
        } else {
            xobjs = new PdfDictionary();
            resDict.set(PdfName.of("XObject"), xobjs);
        }
        for (Map.Entry<String, org.aspose.pdf.engine.pdfobjects.PdfStream> e : images.entrySet()) {
            if (xobjs.get(e.getKey()) == null) {
                xobjs.set(PdfName.of(e.getKey()), e.getValue());
            }
        }
    }

    /* DOM helpers */
    private static String local(Node n) {
        return n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
    }

    private static String attr(Element e, String a, String def) {
        if (e == null) {
            return def;
        }
        String v = e.getAttribute(a);
        return v == null || v.isEmpty() ? def : v;
    }

    /// An XFA on/off attribute (e.g. `underline`) is truthy unless absent / empty / "0".
    private static boolean truthyFlag(String v) {
        return v != null && !v.isEmpty() && !"0".equals(v.trim());
    }

    private static Element firstEl(Element el) {
        Node n = el.getFirstChild();
        while (n != null && n.getNodeType() != Node.ELEMENT_NODE) {
            n = n.getNextSibling();
        }
        return (Element) n;
    }

    private static Element nextEl(Element el) {
        Node n = el.getNextSibling();
        while (n != null && n.getNodeType() != Node.ELEMENT_NODE) {
            n = n.getNextSibling();
        }
        return (Element) n;
    }

    private static Element firstChildOf(Element el, String name) {
        if (el == null) {
            return null;
        }
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            if (name.equals(local(c))) {
                return c;
            }
        }
        return null;
    }

    private static List<Element> childrenOf(Element el, String name) {
        List<Element> out = new ArrayList<>();
        if (el != null) {
            for (Element c = firstEl(el); c != null; c = nextEl(c)) {
                if (name.equals(local(c))) {
                    out.add(c);
                }
            }
        }
        return out;
    }

    private static Element descend(Element el, String name) {
        Element d = firstChildOf(el, name);
        if (d != null) {
            return d;
        }
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            Element g = firstChildOf(c, name);
            if (g != null) {
                return g;
            }
        }
        return null;
    }

    private static String textOf(Element host) {
        if (host == null) {
            return null;
        }
        Element value = firstChildOf(host, "value");
        Element text = value != null ? firstChildOf(value, "text") : null;
        if (text != null) {
            return text.getTextContent();
        }
        return null;
    }

    /// A caption's label text, supporting both plain `<caption><value><text>` and rich
    /// `<caption><value><exData contentType="text/html">…<p>label</p>` forms (the latter is how
    /// this form authors most field captions — e.g. "Název/obch.firma:"). A caption is a single short
    /// label, so the HTML is flattened to its whitespace-normalised text (no per-paragraph wrapping,
    /// unlike a rich-text field value which keeps [#drawRichText]). Returns null/empty when the
    /// caption carries no text.
    private static String captionString(Element captionEl) {
        if (captionEl == null) {
            return null;
        }
        String plain = textOf(captionEl);
        if (plain != null && !plain.trim().isEmpty()) {
            return plain;
        }
        Element value = firstChildOf(captionEl, "value");
        Element exData = value != null ? firstChildOf(value, "exData") : null;
        if (exData != null) {
            String s = exData.getTextContent();
            if (s != null && !s.trim().isEmpty()) {
                return normalizeWs(s);
            }
        }
        return plain;
    }
}
