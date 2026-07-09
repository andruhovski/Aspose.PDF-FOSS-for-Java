package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.layout.LayoutContext;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.binding.FormField;
import org.aspose.pdf.engine.xfa.flatten.XfaGeometry;
import org.aspose.pdf.engine.xfa.flatten.XfaMedium;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.template.ContentArea;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the XFA <b>Layout DOM</b> for a flowed-root form by flowing its content
 * top-to-bottom into a SINGLE content region (Stage C, sprint L1).
 *
 * <p>This is L1: flow within one region, with each growable object's height resolved
 * from its bound data. It does <b>not</b> paginate, split content, or emit a second
 * page — overflow (content taller than the region) is measured and reported for L2/L3,
 * not acted upon. Paint composition onto the page is L5.</p>
 *
 * <p>Reuses the repo's layout primitives rather than starting a parallel stack:
 * {@link LayoutContext} is the flow cursor (its {@code advanceCursor}/{@code getRemainingHeight}
 * drive both the per-container stacking and the region overflow read);
 * {@link XfaGrowableHeight} (via {@link org.aspose.pdf.engine.layout.TextLayoutHelper})
 * computes data-driven heights; {@link XfaGeometry} supplies anchor resolution for
 * positioned subtrees nested inside the flow. The Form DOM consumed here is already
 * occur-expanded by the binding engine — L1 neither re-expands nor touches binding.</p>
 */
public final class XfaFlowLayout {

    /** A region tall enough that nested containers never run out of vertical space (they grow). */
    private static final double UNBOUNDED = 1.0e7;

    /** Container element kinds that hold layout children. */
    private static final Set<String> CONTAINERS = new HashSet<>(
            Arrays.asList("subform", "subformSet", "exclGroup", "area"));
    /** Layout-object element kinds placed in the flow. */
    private static final Set<String> LAYOUT = new HashSet<>(
            Arrays.asList("subform", "subformSet", "exclGroup", "area", "field", "draw"));
    /** Flowed {@code layout} values (children stack); anything else is positioned. */
    private static final Set<String> FLOWED = new HashSet<>(
            Arrays.asList("tb", "lr-tb", "rl-tb", "row", "table"));

    private XfaFlowLayout() {
    }

    /** The outcome of laying a flowed-root form into one region. */
    public static final class Result {
        /** The placed Layout DOM root (the form's root container). */
        public final XfaLayoutNode root;
        /** Content region width in points. */
        public final double regionWidth;
        /** Content region height in points (the single region; overflow is measured against it). */
        public final double regionHeight;
        /** Total laid-out content height in points (the root's resolved height). */
        public final double contentHeight;
        /** Overflow in points = {@code contentHeight - regionHeight} (&gt; 0 ⇒ needs pagination, L3). */
        public final double overflow;
        /** Placed leaf objects (field/draw). */
        public int placedLeaves;
        /** Placed growable objects (no fixed {@code h}). */
        public int growableObjects;
        /** Placed positioned objects (offset by their own x/y inside the flow). */
        public int positionedObjects;

        Result(XfaLayoutNode root, double regionWidth, double regionHeight,
               double contentHeight, double overflow) {
            this.root = root;
            this.regionWidth = regionWidth;
            this.regionHeight = regionHeight;
            this.contentHeight = contentHeight;
            this.overflow = overflow;
        }

        /** @return {@code true} if the laid-out content exceeds the single region (L3 must paginate). */
        public boolean overflows() {
            return overflow > 0.5; // sub-point slack
        }
    }

    /**
     * Lays the flowed root of {@code dom} into the first content region of {@code tpl}.
     *
     * @param dom the merged (occur-expanded) Form DOM
     * @param tpl the template (for the content region size + medium), or {@code null}
     * @return the Layout DOM + region/overflow measurement
     */
    public static Result layout(FormDom dom, Template tpl) {
        double[] region = contentRegion(tpl);
        double regionW = region[0];
        double regionH = region[1];

        Map<Element, FormField> byElement = new IdentityHashMap<>();
        for (FormField f : dom.getFields()) {
            if (f.getFormNode() != null) {
                byElement.put(f.getFormNode().getElement(), f);
            }
        }

        Result r = new Result(null, regionW, regionH, 0, 0);
        Element rootEl = dom.getRoot() == null ? null : dom.getRoot().getElement();
        XfaLayoutNode root;
        if (rootEl == null) {
            root = new XfaLayoutNode(null, "subform", 0, 0, regionW, 0);
        } else {
            root = layoutBox(rootEl, 0, 0, regionW, null, byElement, r);
        }

        double contentHeight = root.getHeight();
        double overflow = contentHeight - regionH;

        // L1.3: express the single-region fill through the region's LayoutContext so the
        // overflow read comes from the same cursor primitive the flow uses internally.
        LayoutContext regionCtx = new LayoutContext(regionW, regionH, null);
        regionCtx.advanceCursor(contentHeight);
        // remaining < 0 ⇔ the region overflowed by exactly -remaining (= overflow).

        Result out = new Result(root, regionW, regionH, contentHeight, overflow);
        out.placedLeaves = r.placedLeaves;
        out.growableObjects = r.growableObjects;
        out.positionedObjects = r.positionedObjects;
        return out;
    }

    /**
     * Lays an arbitrary subform/area fragment into a standalone Layout node at the content-region
     * origin ({@code x=0,y=0}), reusing the same box machinery as the main flow. Used by L4 to
     * size and place <b>boundary content</b> (a bookend / break leader or trailer subform) before
     * it is inserted into a page.
     *
     * @param fragment   the subform / area element to lay out (a leader/trailer reference target)
     * @param availWidth the available width in points (normally the content-region width)
     * @param dom        the merged Form DOM, for any bound field values inside the fragment, or {@code null}
     * @return the placed layout node (origin-based, height resolved), or {@code null} if {@code fragment} is null
     */
    public static XfaLayoutNode layoutFragment(Element fragment, double availWidth, FormDom dom) {
        return layoutFragment(fragment, availWidth, dom == null ? null : dom.getFields());
    }

    /**
     * Lays a fragment as {@link #layoutFragment(Element, double, FormDom)} but resolves bound field
     * values from an explicit field list rather than the FormDom flow fields. Used by the paginator
     * to lay out {@code <pageSet>} master furniture against the FormDom master-field channel.
     *
     * @param fields the fields whose form nodes supply values for the fragment, or {@code null}
     */
    public static XfaLayoutNode layoutFragment(Element fragment, double availWidth, List<FormField> fields) {
        if (fragment == null) {
            return null;
        }
        Map<Element, FormField> byElement = new IdentityHashMap<>();
        if (fields != null) {
            for (FormField f : fields) {
                if (f.getFormNode() != null) {
                    byElement.put(f.getFormNode().getElement(), f);
                }
            }
        }
        Result r = new Result(null, availWidth, UNBOUNDED, 0, 0);
        return layoutBox(fragment, 0, 0, availWidth, null, byElement, r);
    }

    /* ------------------------------- traversal -------------------------------- */

    /**
     * Lays one box at content-region top-left {@code (absX, absY)} with the given available
     * width, returning its placed node (position + resolved height + children).
     *
     * @param inheritedCols column widths handed down from an enclosing {@code layout="table"}
     *                      (consumed by a {@code layout="row"} child); {@code null} otherwise
     */
    private static XfaLayoutNode layoutBox(Element el, double absX, double absY, double availWidth,
                                           double[] inheritedCols, Map<Element, FormField> byElement, Result r) {
        String kind = local(el);
        double declaredW = measure(el, "w");
        double boxW = Double.isNaN(declaredW) ? availWidth : declaredW;

        // Leaf (field/draw): no layout children — height is data-driven (or fixed h).
        if (!CONTAINERS.contains(kind)) {
            FormField ff = byElement.get(el);
            String value = ff != null ? ff.getValue() : null;
            double h = XfaGrowableHeight.height(el, value, boxW);
            XfaLayoutNode leaf = new XfaLayoutNode(el, kind, absX, absY, boxW, h);
            leaf.setGrowable(XfaGrowableHeight.isGrowable(el));
            r.placedLeaves++;
            if (leaf.isGrowable()) {
                r.growableObjects++;
            }
            return leaf;
        }

        // Container: place children, then size to contain them (unless h is fixed).
        double[] ins = XfaGrowableHeight.insets(el); // left, top, right, bottom
        // A layout="table" with no explicit w sizes to the SUM of its own columnWidths (+ insets): the
        // grid IS the table, so its <border> must meet the cells' outer edge. A wider inherited available
        // width otherwise leaves a sliver between the last column and the table border — the stray thin
        // "cell" on 11902's TOTAL EQUIPMENT row (its table is narrower than the parent subform).
        if (Double.isNaN(declaredW) && "table".equals(el.getAttribute("layout"))) {
            double[] tcols = columnWidthsOf(el, null);
            if (tcols != null && tcols.length > 0) {
                double sum = ins[0] + ins[2];
                for (double cw : tcols) {
                    sum += cw;
                }
                if (sum > 0) {
                    boxW = sum;
                }
            }
        }
        double contentAbsX = absX + ins[0];
        double contentAbsY = absY + ins[1];
        double innerW = boxW - ins[0] - ins[2];
        if (innerW <= 0) {
            innerW = boxW > 0 ? boxW : availWidth;
        }

        XfaLayoutNode node = new XfaLayoutNode(el, kind, absX, absY, boxW, 0);

        // Dispatch by the container's layout strategy (XFA 3.0 ch.8). Each placer fills
        // node's children and returns the content extent = deepest child bottom relative to
        // the content origin. tb/rl-tb/position keep L1's vertical/positioned placement; the
        // L2 additions are lr-tb (horizontal wrap), row and table (columnWidths geometry).
        String layout = el.getAttribute("layout");
        double contentExtent;
        if ("table".equals(layout)) {
            contentExtent = placeTable(el, node, contentAbsX, contentAbsY, innerW, byElement, r);
        } else if ("row".equals(layout)) {
            double[] cols = columnWidthsOf(el, inheritedCols);
            contentExtent = placeRow(el, node, contentAbsX, contentAbsY, innerW, cols, byElement, r);
        } else if ("lr-tb".equals(layout)) {
            contentExtent = placeHorizontalWrap(el, node, contentAbsX, contentAbsY, innerW, byElement, r);
        } else {
            contentExtent = placeVertical(el, node, contentAbsX, contentAbsY, innerW, isFlowed(el), byElement, r);
        }

        double fixedH = measure(el, "h");
        if (!Double.isNaN(fixedH)) {
            node.setHeight(fixedH);
        } else {
            double h = ins[1] + contentExtent + ins[3];
            // A container's minH is a height floor — EXCEPT when a LAYOUT-GROUP subform under-fills it.
            // Such a subform reserved canvas space for an optional <occur min=0> child (a description
            // shown only when its row is selected) that is absent at render; in Adobe Designer it was
            // sized to that MAX state, and Adobe collapses it to real content when the child is absent.
            // Honouring minH there leaves ~13-22mm of phantom space per row, inflating the page so a
            // claim section Adobe fits on one page overflows onto the next (11367: the 5 "Vlastnosti"
            // rows + the "Příslušenství" block, ~316pt). Two complementary markers identify such a group:
            // a design-canvas y offset (vestigial under a flowing parent), or a still-present nested
            // subform beside the dropped one. A leaf-content box (the green title banner: no y, no nested
            // subform, only a styled draw) matches neither and keeps its minH as a genuine floor.
            double minH = XfaGrowableHeight.minH(el);
            if (minH > h && (el.hasAttribute("y") || hasChildContainer(el))) {
                minH = 0;
            }
            node.setHeight(clamp(h, minH, XfaGrowableHeight.maxH(el)));
            node.setGrowable(true);
            r.growableObjects++;
        }
        return node;
    }

    /* ----------------------------- layout strategies -------------------------- */

    /**
     * {@code layout="tb"} / {@code rl-tb} / positioned (L1): flow children stack top-to-bottom;
     * a positioned child (own x/y) offsets from the content origin and does not advance the
     * cursor; in a non-flowed container an unpositioned child sits at the content origin.
     *
     * @return the content extent (deepest child bottom relative to {@code contentAbsY})
     */
    private static double placeVertical(Element el, XfaLayoutNode node, double contentAbsX,
                                        double contentAbsY, double innerW, boolean flowed,
                                        Map<Element, FormField> byElement, Result r) {
        // The flow cursor for this container's children — a LayoutContext whose content top
        // is its (effectively unbounded) inner region top; advanceCursor stacks each child.
        LayoutContext flow = new LayoutContext(innerW, UNBOUNDED, null);
        double contentExtent = 0;
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            if (!LAYOUT.contains(local(c))) {
                continue; // skip properties (ui/value/caption/border/...)
            }
            if (!occupiesLayout(c)) {
                continue; // hidden/inactive/not-print-relevant — reserves no space
            }
            XfaLayoutNode child;
            // A child is positioned when it carries its own x/y, OR when its container uses
            // position layout (non-flowed): there EVERY child sits at its (x, y), each axis defaulting
            // to 0 when unspecified. Requiring BOTH x and y dropped partial-coordinate fields (e.g. an
            // "IČ" at y=6.5mm with no x) onto y=0, overlapping the field above it (the missing
            // "Název/obch.firma" row of a Právnická-osoba block).
            if (isPositioned(c) || !flowed) {
                double cx = orZero(measure(c, "x"));
                double cy = orZero(measure(c, "y"));
                double cw = orZero(measure(c, "w"));
                double ch = orZero(measure(c, "h"));
                // A width-less positioned LEAF falls back to its minW (the billing-address fields, e.g.
                // COMPANY_NM_CA minW=38.1mm at anchorType="middleCenter"): the anchor needs the real width
                // to offset the box left by half it (x − w/2). With cw=0 the box stayed at x, indenting the
                // address; with cw=minW it lands at the box's left edge and the text aligns like Adobe.
                if (cw <= 0 && !CONTAINERS.contains(local(c))) {
                    double mw = measure(c, "minW");
                    if (!Double.isNaN(mw) && mw > 0) {
                        cw = mw;
                    }
                }
                double[] tl = XfaGeometry.anchorTopLeft(cx, cy, cw, ch, c.getAttribute("anchorType"));
                child = layoutBox(c, contentAbsX + tl[0], contentAbsY + tl[1],
                        cw > 0 ? cw : innerW, null, byElement, r);
                child.setPositioned(true);
                r.positionedObjects++;
            } else {
                double childAbsY = contentAbsY + (flow.getContentTop() - flow.getCursorY());
                child = layoutBox(c, contentAbsX, childAbsY, innerW, null, byElement, r);
                // A declared-width leaf with hAlign="right" pins its box to the inner-width right edge
                // (the rule line above 14758's "Total Net": Line3, hAlign="right", in a tb container) so
                // it sits over the right-hand total column. Restricted to a declared-width leaf so it
                // can't move flowing text/fields.
                if ("right".equals(c.getAttribute("hAlign")) && !CONTAINERS.contains(local(c))
                        && !Double.isNaN(measure(c, "w")) && child.getWidth() < innerW) {
                    child = child.translated(innerW - child.getWidth(), 0);
                }
                flow.advanceCursor(child.getHeight());
            }
            node.addChild(child);
            contentExtent = Math.max(contentExtent, (child.getY() + child.getHeight()) - contentAbsY);
        }
        return contentExtent;
    }

    /**
     * {@code layout="lr-tb"}: children flow left-to-right at the current line, wrapping to a
     * new line when the next child would exceed the inner width. Line height = the tallest
     * child on that line; the cursor drops by the completed line's height on each wrap.
     *
     * @return the content extent (bottom of the last line relative to {@code contentAbsY})
     */
    private static double placeHorizontalWrap(Element el, XfaLayoutNode node, double contentAbsX,
                                              double contentAbsY, double innerW,
                                              Map<Element, FormField> byElement, Result r) {
        double cursorX = 0;   // x within the line, relative to contentAbsX
        double lineY = 0;     // top of the current line, relative to contentAbsY
        double lineHeight = 0;
        double contentExtent = 0;
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            if (!LAYOUT.contains(local(c))) {
                continue;
            }
            if (!occupiesLayout(c)) {
                continue; // hidden/inactive/not-print-relevant — reserves no space
            }
            // Measure the child first (so its width drives the wrap test). lr-tb children size to
            // their declared width; a child with NO declared width shrinks-to-fit its content rather
            // than claiming the whole inner width — else a narrow width-less container (e.g. the
            // radio exclGroup on 11367) would force every following sibling onto a new line instead
            // of sitting beside it ("Právní řád založení" should sit to the RIGHT of the radios).
            double childW = measure(c, "w");
            boolean declared = !Double.isNaN(childW);
            // A width-less LEAF (field/draw) with a minW has a natural width = minW: treat it as declared
            // so it sits at that width and WRAPS when it no longer fits the line. The address block's
            // CITY/REGION/POSTAL_CODE (minW 13.97/6.35/28.575mm) then pack onto one line and LANDX
            // ("CANADA", minW 60.96mm) wraps below them, matching Adobe — instead of each width-less
            // field claiming the whole inner width and stacking vertically (which over-tall the block so
            // "CANADA" collided with the "Billing Address" header).
            if (!declared && !CONTAINERS.contains(local(c))) {
                double leafMinW = measure(c, "minW");
                if (!Double.isNaN(leafMinW) && leafMinW > 0) {
                    childW = leafMinW;
                    declared = true;
                }
            }
            // Shrink-to-fit only a width-less LAYOUT-ONLY container (no own fill/border) — a styled box
            // (e.g. the green title banner, a filled section) keeps the full inner width as before, so
            // shrinking can't collapse it. A leaf has no children → contentWidthOf is 0 → never shrinks.
            boolean shrink = !declared && !hasBoxStyling(c);
            double avail = declared ? childW : innerW;
            if (shrink) {
                avail = Math.max(1, innerW - cursorX);
            }
            if (!shrink && cursorX > 0 && cursorX + avail > innerW + 1e-6) {
                lineY += lineHeight; // wrap: close the current line, drop to the next
                cursorX = 0;
                lineHeight = 0;
            }
            XfaLayoutNode child = layoutBox(c, contentAbsX + cursorX, contentAbsY + lineY,
                    avail, null, byElement, r);
            if (shrink) {
                double cw = contentWidthOf(child);
                if (cw > 1e-6 && cw < child.getWidth()) {
                    child.setWidth(cw); // shrink-to-fit so following siblings can share the line
                }
                if (cursorX > 0 && cursorX + child.getWidth() > innerW + 1e-6) {
                    lineY += lineHeight; // still overflows → wrap and re-place at the new line
                    cursorX = 0;
                    lineHeight = 0;
                    child = layoutBox(c, contentAbsX, contentAbsY + lineY, Math.max(1, innerW),
                            null, byElement, r);
                    double cw2 = contentWidthOf(child);
                    if (cw2 > 1e-6 && cw2 < child.getWidth()) {
                        child.setWidth(cw2);
                    }
                }
            }
            // A declared-width leaf with an explicit hAlign="right" pins its box to the inner-width edge
            // instead of flowing at the left cursor — the rule line above 14758's "Total Net" (Line3,
            // hAlign="right") then sits over the right-hand total column as Adobe draws it. Restricted to
            // a declared-width leaf so it cannot disturb wrapping text/fields.
            if (declared && !CONTAINERS.contains(local(c)) && child.getWidth() < innerW
                    && "right".equals(c.getAttribute("hAlign"))) {
                double shift = (contentAbsX + innerW - child.getWidth()) - child.getX();
                if (shift > 0) {
                    child = child.translated(shift, 0);
                }
                node.addChild(child);
                cursorX = innerW; // consumes the rest of the line so a later sibling wraps below
                lineHeight = Math.max(lineHeight, child.getHeight());
                contentExtent = Math.max(contentExtent, lineY + lineHeight);
                continue;
            }
            node.addChild(child);
            cursorX += child.getWidth();
            lineHeight = Math.max(lineHeight, child.getHeight());
            contentExtent = Math.max(contentExtent, lineY + lineHeight);
        }
        return contentExtent;
    }

    /**
     * The shrink-to-fit content width of a laid-out container node: the rightmost edge of its
     * placed children (relative to the node's own left edge) plus the container's right inset. Used to
     * size a width-less child in a horizontal flow so it occupies only as much width as its content.
     */
    /** Whether an element carries its own box styling (a {@code <fill>} or {@code <border>} child) — such a
     * width-less container should keep the full inner width (a banner/section box), not shrink-to-fit. */
    private static boolean hasBoxStyling(Element el) {
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            String ln = local(c);
            if ("fill".equals(ln) || "border".equals(ln)) {
                return true;
            }
        }
        return false;
    }

    private static double contentWidthOf(XfaLayoutNode node) {
        // A leaf (no layout children — e.g. a width-less text <draw> like the "Zde uveďte veškeré
        // přílohy…" appendix heading) has no child geometry to fit; its content is its own text, which
        // this measures nothing of. Returning the bare right inset would collapse it to ~1pt and hide
        // the text, so report 0 — the shrink-to-fit guard then keeps the child's full available width.
        if (node.getChildren().isEmpty()) {
            return 0;
        }
        double maxRight = 0;
        for (XfaLayoutNode c : node.getChildren()) {
            maxRight = Math.max(maxRight, (c.getX() - node.getX()) + c.getWidth());
        }
        double[] ins = XfaGrowableHeight.insets(node.getSource());
        return maxRight + ins[2];
    }

    /**
     * {@code layout="table"}: a subform whose flowed children (rows) stack top-to-bottom, each
     * row laid out against this table's {@code columnWidths} so columns align across rows.
     *
     * @return the content extent (bottom of the last row relative to {@code contentAbsY})
     */
    private static double placeTable(Element el, XfaLayoutNode node, double contentAbsX,
                                     double contentAbsY, double innerW,
                                     Map<Element, FormField> byElement, Result r) {
        double[] cols = columnWidthsOf(el, null);
        LayoutContext flow = new LayoutContext(innerW, UNBOUNDED, null);
        double[] extent = {0};
        placeTableRows(el, node, cols, contentAbsX, contentAbsY, innerW, flow, byElement, r, extent);
        return extent[0];
    }

    /**
     * Places a table's row children against {@code cols}, descending TRANSPARENTLY through any
     * {@code <subformSet>} grouping (the header rows of an XFA table are wrapped in a subformSet that is
     * a binding/occur construct, not a layout box — its rows must still receive the table's columns and
     * sit at the table's x-origin, else the header shears away from the body grid).
     */
    private static void placeTableRows(Element container, XfaLayoutNode tableNode, double[] cols,
                                       double contentAbsX, double contentAbsY, double innerW,
                                       LayoutContext flow, Map<Element, FormField> byElement, Result r,
                                       double[] extent) {
        for (Element c = firstEl(container); c != null; c = nextEl(c)) {
            if (!LAYOUT.contains(local(c))) {
                continue;
            }
            if (!occupiesLayout(c)) {
                continue; // hidden/inactive/not-print-relevant — reserves no space
            }
            if ("subformSet".equals(local(c))) {
                placeTableRows(c, tableNode, cols, contentAbsX, contentAbsY, innerW, flow, byElement, r, extent);
                continue;
            }
            double rowAbsY = contentAbsY + (flow.getContentTop() - flow.getCursorY());
            // Hand the table columns down to the row child (it consumes them in placeRow).
            XfaLayoutNode row = layoutBox(c, contentAbsX, rowAbsY, innerW, cols, byElement, r);
            tableNode.addChild(row);
            flow.advanceCursor(row.getHeight());
            extent[0] = Math.max(extent[0], (row.getY() + row.getHeight()) - contentAbsY);
        }
    }

    /**
     * {@code layout="row"}: cells are placed left-to-right at the row's content origin, each
     * cell's x = the sum of preceding column widths and width = its column width. The row
     * height is the tallest (growable) cell — a row is laid out as one line.
     *
     * @param cols the effective column widths (the row's own, else the table's inherited)
     * @return the content extent (the tallest cell height relative to {@code contentAbsY})
     */
    private static double placeRow(Element el, XfaLayoutNode node, double contentAbsX,
                                   double contentAbsY, double innerW, double[] cols,
                                   Map<Element, FormField> byElement, Result r) {
        double cursorX = 0;
        double rowHeight = 0;
        int col = 0;
        java.util.List<XfaLayoutNode> placed = new java.util.ArrayList<>();
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            if (!LAYOUT.contains(local(c))) {
                continue;
            }
            // A cell may span several grid columns (XFA <... colSpan="N">) — its width is the sum of the
            // covered column widths, and the column cursor advances by the span so every following cell
            // lands in its true column (the multi-level "Computation"/"PERSONNEL (FEDERAL)" headers and
            // the nested computation table rely on this — without it the whole grid shears apart).
            int span = colSpan(c);
            // colSpan="-1" (XFA 3.0) means "span the remainder of the row" — used by the multi-level
            // "Computation" header (one cell over the Quantity+Cost sub-columns). Resolve it against the
            // columns still available from the current cursor position.
            if (span < 0) {
                span = cols != null && col < cols.length ? cols.length - col : 1;
            }
            double colW;
            if (cols != null && col < cols.length) {
                colW = 0;
                for (int k = 0; k < span && col + k < cols.length; k++) {
                    colW += cols[col + k];
                }
            } else {
                double declared = measure(c, "w");
                colW = Double.isNaN(declared) ? Math.max(0, innerW - cursorX) : declared;
            }
            // A non-occupying cell (a print-hidden button column) still owns its grid slot: reserve the
            // column width so the rest of the row stays aligned, but place no visible node.
            if (occupiesLayout(c)) {
                XfaLayoutNode cell = layoutBox(c, contentAbsX + cursorX, contentAbsY, colW, null, byElement, r);
                // The column width is AUTHORITATIVE for a table cell: a cell's own declared w (whether
                // smaller — a 30mm cell in a 35mm column leaving a gap — or larger — a 30mm "empty1"
                // header cell in a 5mm spacer column shoving the whole header row right) must be replaced
                // by the column width so cell borders meet into a continuous ruled grid AND the header
                // columns line up with the body columns. Leaf cells only; a container cell was already
                // laid out against this same colW as its available width.
                if (cols != null && cell.getChildren().isEmpty() && Math.abs(cell.getWidth() - colW) > 1e-6) {
                    cell.setWidth(colW);
                }
                node.addChild(cell);
                placed.add(cell);
                rowHeight = Math.max(rowHeight, cell.getHeight());
            }
            cursorX += colW;
            col += span;
        }
        // In a real grid (defined columnWidths), every cell shares the row height so their borders form
        // even horizontal grid lines: a short cell (the 7.62mm "cost" against a taller name/position
        // cell) otherwise leaves its box floating with a gap below it. Stretch leaf cells to the row
        // height; a nested container keeps its own extent.
        if (cols != null) {
            for (XfaLayoutNode cell : placed) {
                if (cell.getChildren().isEmpty() && cell.getHeight() < rowHeight) {
                    cell.setHeight(rowHeight);
                }
            }
        }
        return rowHeight;
    }

    /**
     * A table cell's {@code colSpan} (number of grid columns it covers). Returns {@code -1} verbatim
     * for the XFA "span the remainder of the row" sentinel (resolved by the caller against the columns
     * left); any other value is clamped to at least 1.
     */
    private static int colSpan(Element cell) {
        String s = cell.getAttribute("colSpan");
        if (s == null || s.trim().isEmpty()) {
            return 1;
        }
        try {
            int v = Integer.parseInt(s.trim());
            return v < 0 ? -1 : Math.max(1, v);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    /**
     * The effective column widths for a {@code row}/{@code table}: parse this element's own
     * {@code columnWidths} attribute (space-separated measurements) if present, otherwise the
     * widths inherited from an enclosing table.
     */
    private static double[] columnWidthsOf(Element el, double[] inherited) {
        String raw = el.getAttribute("columnWidths");
        if (raw == null || raw.trim().isEmpty()) {
            return inherited;
        }
        String[] toks = raw.trim().split("\\s+");
        double[] cols = new double[toks.length];
        for (int i = 0; i < toks.length; i++) {
            XfaMeasurement m = XfaMeasurement.parse(toks[i]);
            cols[i] = m == null ? 0 : XfaGeometry.toPoints(m);
        }
        return cols;
    }

    /* -------------------------------- region ---------------------------------- */

    /**
     * The single content region {@code {width,height}} in points: the first
     * {@code <contentArea>} of the template, else the medium page size as a fallback.
     */
    static double[] contentRegion(Template tpl) {
        if (tpl != null) {
            // Prefer the contentArea of the pageArea the flowed BODY targets: a form may declare a tall
            // first-page contentArea (a positioned header page) AND a shorter body-page contentArea for
            // the flowing table. The line items must split by the BODY region's height, not the first
            // page's, or they overrun the body page's footer (14758: Page1 266.7mm header region vs Page2
            // 177.8mm line-item region — splitting by 266.7 overflowed the table into the page footer and
            // dropped rows). Falls back to the first contentArea when there is no such targeted body.
            ContentArea ca = bodyContentArea(tpl.getElement());
            if (ca == null) {
                ca = firstContentArea(tpl.getElement());
            }
            if (ca != null) {
                double w = XfaGeometry.toPoints(ca.getW());
                double h = XfaGeometry.toPoints(ca.getH());
                if (w > 0 && h > 0) {
                    return new double[]{w, h};
                }
            }
            return XfaMedium.resolve(tpl);
        }
        return XfaMedium.LETTER.clone();
    }

    /**
     * The contentArea of the pageArea targeted by a flowed body subform — one that carries a
     * {@code breakBefore}/{@code break} {@code targetType="pageArea"} AND contains a repeating
     * ({@code <occur max="-1"|&gt;1>}) table/row (the flowing line-item body). Returns {@code null} when
     * the template has no such targeted body (the common single-region case), so the caller keeps the
     * first contentArea.
     */
    private static ContentArea bodyContentArea(Element root) {
        String targetId = findBodyBreakTarget(root);
        if (targetId == null) {
            return null;
        }
        if (targetId.startsWith("#")) {
            targetId = targetId.substring(1);
        }
        Element pageArea = findPageAreaByIdOrName(root, targetId);
        if (pageArea == null) {
            return null;
        }
        for (Element c = firstEl(pageArea); c != null; c = nextEl(c)) {
            if ("contentArea".equals(local(c))) {
                return (ContentArea) XfaNodeFactory.wrap(c, null);
            }
        }
        return null;
    }

    /** Finds the pageArea {@code target} of the first flowed subform that both breaks to a pageArea and repeats rows. */
    private static String findBodyBreakTarget(Element el) {
        if ("subform".equals(local(el)) && isFlowed(el) && hasRepeatingTable(el)) {
            String t = breakTargetPageArea(el);
            if (t != null) {
                return t;
            }
        }
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            String t = findBodyBreakTarget(c);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    /** The {@code target} of a child {@code <breakBefore>}/{@code <break before>} with {@code targetType="pageArea"}, or null. */
    private static String breakTargetPageArea(Element subform) {
        for (Element c = firstEl(subform); c != null; c = nextEl(c)) {
            String ln = local(c);
            if (("breakBefore".equals(ln) || "break".equals(ln))
                    && "pageArea".equals(c.getAttribute("targetType"))) {
                String t = c.getAttribute("target");
                if (t != null && !t.isEmpty()) {
                    return t;
                }
            }
        }
        return null;
    }

    /** Whether {@code el} contains a descendant container with {@code <occur max="-1">} or {@code max&gt;1} (a repeating body). */
    private static boolean hasRepeatingTable(Element el) {
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            if ("occur".equals(local(c))) {
                String max = c.getAttribute("max");
                if ("-1".equals(max)) {
                    return true;
                }
                try {
                    if (max != null && !max.isEmpty() && Integer.parseInt(max) > 1) {
                        return true;
                    }
                } catch (NumberFormatException ignore) {
                    // not a count
                }
            }
            if (hasRepeatingTable(c)) {
                return true;
            }
        }
        return false;
    }

    /** Depth-first search for a {@code <pageArea>} whose {@code id} or {@code name} equals {@code key}. */
    private static Element findPageAreaByIdOrName(Element el, String key) {
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            if ("pageArea".equals(local(c))
                    && (key.equals(c.getAttribute("id")) || key.equals(c.getAttribute("name")))) {
                return c;
            }
            Element found = findPageAreaByIdOrName(c, key);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static ContentArea firstContentArea(Element el) {
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            String ln = local(c);
            if ("contentArea".equals(ln)) {
                return (ContentArea) XfaNodeFactory.wrap(c, null);
            }
            if ("pageSet".equals(ln) || "pageArea".equals(ln)
                    || "subform".equals(ln) || "subformSet".equals(ln) || "area".equals(ln)) {
                ContentArea found = firstContentArea(c);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /* -------------------------------- helpers --------------------------------- */

    private static boolean isFlowed(Element el) {
        String layout = el.getAttribute("layout");
        return layout != null && FLOWED.contains(layout);
    }

    /**
     * Whether a child occupies space in the print/render layout. Per XFA {@code presence} semantics:
     * {@code hidden} and {@code inactive} are removed from layout entirely (reserve no space);
     * {@code invisible} is kept (reserves space, just isn't painted); {@code visible}/absent flows
     * normally. Content not {@code relevant} to the print context (e.g. {@code relevant="-print"}
     * interactive-only objects) is likewise excluded — Adobe omits it when printing — so the laid-out
     * height matches the printed form instead of inflating with hidden/interactive boxes.
     */
    private static boolean occupiesLayout(Element el) {
        String presence = el.getAttribute("presence");
        if ("hidden".equals(presence) || "inactive".equals(presence)) {
            return false;
        }
        if (org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.relevantForPrint(el)) {
            return true;
        }
        // Convert mode keeps a visible interactive button (the +/- row controls, relevant="-print") in
        // the layout so the AcroForm converter can position it as a clickable widget. Print/flatten/
        // render still omit it (default flag off).
        return INCLUDE_INTERACTIVE.get()
                && org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.isInteractiveButton(el);
    }

    /** Convert-only flag: include {@code relevant="-print"} interactive buttons in the layout. */
    private static final ThreadLocal<Boolean> INCLUDE_INTERACTIVE = ThreadLocal.withInitial(() -> false);

    /**
     * Enables/disables inclusion of {@code relevant="-print"} interactive buttons in the layout for the
     * current thread — set by {@link org.aspose.pdf.engine.xfa.flatten.XfaAcroFormConverter} around its
     * layout pass so the converted AcroForm keeps the +/- controls; off for print/flatten/render.
     *
     * @param on whether to include interactive buttons
     */
    public static void setIncludeInteractive(boolean on) {
        INCLUDE_INTERACTIVE.set(on);
    }

    /** A positioned object carries its own {@code x} AND {@code y}. */
    private static boolean isPositioned(Element el) {
        return !Double.isNaN(measure(el, "x")) && !Double.isNaN(measure(el, "y"));
    }

    private static double measure(Element el, String name) {
        if (el == null || !el.hasAttribute(name)) {
            return Double.NaN;
        }
        XfaMeasurement m = XfaMeasurement.parse(el.getAttribute(name));
        if (m == null) {
            return Double.NaN;
        }
        double pts = XfaGeometry.toPoints(m);
        return pts == 0.0 && m.getValue() != 0.0 ? Double.NaN : pts;
    }

    private static double orZero(double v) {
        return Double.isNaN(v) ? 0.0 : v;
    }

    private static double clamp(double v, double lo, double hi) {
        if (v < lo) {
            return lo;
        }
        return v > hi ? hi : v;
    }

    private static String local(Node n) {
        return n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
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

    /** Whether {@code el} directly nests another grouping container (subform / subformSet / area) —
     * the marker of a layout group whose minH reserved space for an optional, now-absent occur child. */
    private static boolean hasChildContainer(Element el) {
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            String ln = local(c);
            if ("subform".equals(ln) || "subformSet".equals(ln) || "area".equals(ln)) {
                return true;
            }
        }
        return false;
    }
}
