package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.layout.ContentStreamBuilder;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.binding.FormField;
import org.aspose.pdf.engine.xfa.flatten.XfaGeometry;
import org.aspose.pdf.engine.xfa.flatten.XfaMedium;
import org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies an L2 {@link XfaPageSplitter.SplitPlan} to produce a <b>paginated Layout DOM</b> and
 * emits the resulting multi-page PDF (Stage C, sprint L3).
 *
 * <p>L3 is where the split is finally <em>acted on</em>: content is distributed across N
 * physical pages of the medium size, each object rebased to its page's content-region top, and
 * each page painted by REUSING the validated C2 {@link XfaPainter} primitives per object (no
 * paint logic is rewritten — final composition polish is L5, leaders/trailers are L4).</p>
 *
 * <p>Two pagination modes are handled:
 * <ul>
 *   <li><b>FLOWED</b> — a flowed root paginates by the L2 {@link XfaPageSplitter.SplitPlan}:
 *       each page range is a page, units rebased to the page top.</li>
 *   <li><b>POSITIONED_PAGES</b> — a positioned root authored as multiple page-sized subforms
 *       (each filling the content region) maps each such subform to its own page. This is the
 *       408975 pattern (3 full-page subforms → 3 pages, matching Adobe).</li>
 *   <li><b>POSITIONED_SINGLE</b> — a genuine single-page positioned form: delegated verbatim to
 *       the validated C2 painter (keeps the proven single-page fidelity).</li>
 * </ul></p>
 */
public final class XfaPaginator {

    private XfaPaginator() {
    }

    /** Pagination mode chosen for a form. */
    public enum Mode {
        /** Flowed root paginated by the split plan. */
        FLOWED,
        /** Positioned root authored as multiple page-sized subforms. */
        POSITIONED_PAGES,
        /** Genuine single-page positioned form (delegated to the C2 painter). */
        POSITIONED_SINGLE
    }

    /** The content region a page draws into: the chosen pageArea's contentArea box. */
    public static final class PageRegion {
        /** Source pageArea element, or {@code null} (medium fallback). */
        public final Element pageArea;
        /** Index of {@code pageArea} in the pageSet declaration order, or {@code -1} (fallback). */
        public final int pageAreaIndex;
        /** contentArea origin X on the medium (points, from page left). */
        public final double contentX;
        /** contentArea origin Y on the medium (points, from page top). */
        public final double contentY;
        /** contentArea width / height in points. */
        public final double contentW;
        public final double contentH;
        /** This pageArea's own medium (page) size in points — its {@code <medium>}, else the global medium. */
        public final double mediumW;
        public final double mediumH;

        PageRegion(Element pageArea, int pageAreaIndex, double contentX, double contentY,
                   double contentW, double contentH, double mediumW, double mediumH) {
            this.pageArea = pageArea;
            this.pageAreaIndex = pageAreaIndex;
            this.contentX = contentX;
            this.contentY = contentY;
            this.contentW = contentW;
            this.contentH = contentH;
            this.mediumW = mediumW;
            this.mediumH = mediumH;
        }
    }

    /** One physical page of placed content (page-local coordinates). */
    public static final class PageLayout {
        /** Top-level placed objects on this page, rebased to the content-region top. */
        public final List<XfaLayoutNode> units;
        /** The page's content region. */
        public final PageRegion region;

        PageLayout(List<XfaLayoutNode> units, PageRegion region) {
            this.units = units;
            this.region = region;
        }
    }

    /** The full paginated layout (pre-emit). */
    public static final class PaginatedLayout {
        /** The physical pages, in order. */
        public final List<PageLayout> pages;
        /** Medium page size in points. */
        public final double mediumW;
        public final double mediumH;
        /** The pagination mode chosen. */
        public final Mode mode;

        PaginatedLayout(List<PageLayout> pages, double mediumW, double mediumH, Mode mode) {
            this.pages = pages;
            this.mediumW = mediumW;
            this.mediumH = mediumH;
            this.mode = mode;
        }

        /** @return the number of physical pages. */
        public int pageCount() {
            return pages.size();
        }
    }

    /** Outcome of a paginate-and-paint pass. */
    public static final class Result {
        /** Physical pages emitted. */
        public int pages;
        /** Pagination mode. */
        public Mode mode;
        /** Aggregate paint counters across all pages. */
        public int painted, fills, borders, texts, captions, presenceHidden, printSkipped, images;
    }

    /* ------------------------------ L3.1 + L3.2 ------------------------------ */

    /**
     * Builds the paginated Layout DOM for a laid-out form: applies the split plan (flowed) or the
     * page-subform pattern (positioned), assigning each page its content region from the
     * {@code <pageSet>}.
     *
     * @param layout the L1/L2 layout result
     * @param plan   the L2 split plan
     * @param tpl    the template (medium + pageSet), or {@code null}
     * @return the paginated layout
     */
    public static PaginatedLayout paginate(XfaFlowLayout.Result layout, XfaPageSplitter.SplitPlan plan,
                                           Template tpl) {
        return paginate(layout, plan, tpl, XfaBookends.NONE, null);
    }

    /**
     * Paginates as {@link #paginate(XfaFlowLayout.Result, XfaPageSplitter.SplitPlan, Template)} but
     * decorates each flowed page with the L4 <b>boundary content</b>: overflow leaders/trailers
     * (bookends) repeated within their occurrence cap, and leaders/trailers at explicit breaks. A
     * positioned form has no flow boundaries and is unaffected (the bookends spec is ignored).
     *
     * @param bookends the resolved overflow boundary content (use {@link XfaBookends#NONE} for none)
     * @param dom      the merged Form DOM (for resolving explicit-break leader/trailer refs), or {@code null}
     */
    public static PaginatedLayout paginate(XfaFlowLayout.Result layout, XfaPageSplitter.SplitPlan plan,
                                           Template tpl, XfaBookends.Spec bookends, FormDom dom) {
        double[] medium = tpl != null ? XfaMedium.resolve(tpl) : XfaMedium.LETTER.clone();
        double mediumW = medium[0];
        double mediumH = medium[1];

        Mode mode;
        List<List<XfaLayoutNode>> pageUnitLists = new ArrayList<>();

        if (layout.root != null && isFlowedRoot(layout.root)) {
            mode = Mode.FLOWED;
            List<XfaLayoutNode> units = XfaPageSplitter.breakableUnits(layout.root);
            List<Element> boundarySources = new ArrayList<>();
            for (int[] range : plan.pageRanges()) {
                List<XfaLayoutNode> page = new ArrayList<>();
                double yOff = range[0] < units.size() ? units.get(range[0]).getY() : 0;
                for (int k = range[0]; k < range[1] && k < units.size(); k++) {
                    page.add(units.get(k).translated(0, -yOff));
                }
                // Styled flowed CONTAINERS (grey section panels / box outlines) are transparent to the
                // splitter, so their <fill>/<border> never became a unit. Repaint them as per-page
                // background boxes behind this page's units (the band the units cover, rebased to top).
                double bandBottom = range[1] > range[0] && range[1] - 1 < units.size()
                        ? units.get(range[1] - 1).getBottom() : yOff;
                addStyledBackgrounds(layout.root, yOff, bandBottom, yOff, page);
                if (!page.isEmpty()) {
                    pageUnitLists.add(page);
                    boundarySources.add(range[0] < units.size() ? units.get(range[0]).getSource() : null);
                }
            }
            decorateFlowed(pageUnitLists, boundarySources, bookends, dom, tpl, layout.regionWidth);
        } else {
            List<XfaLayoutNode> fillers = pageFillers(layout.root, layout.regionWidth, layout.regionHeight);
            if (fillers.size() >= 2) {
                mode = Mode.POSITIONED_PAGES;
                List<XfaLayoutNode> extras = new ArrayList<>();
                for (XfaLayoutNode c : layout.root.getChildren()) {
                    if (!fillers.contains(c)) {
                        extras.add(c);
                    }
                }
                for (int i = 0; i < fillers.size(); i++) {
                    XfaLayoutNode filler = fillers.get(i);
                    double yOff = filler.getY();
                    List<XfaLayoutNode> page = new ArrayList<>();
                    page.add(filler.translated(0, -yOff));
                    if (i == 0) {
                        for (XfaLayoutNode ex : extras) {
                            page.add(ex.translated(0, -yOff));
                        }
                    }
                    pageUnitLists.add(page);
                }
            } else {
                mode = Mode.POSITIONED_SINGLE;
                List<XfaLayoutNode> page = new ArrayList<>();
                if (layout.root != null) {
                    page.addAll(layout.root.getChildren());
                }
                pageUnitLists.add(page);
            }
        }

        if (pageUnitLists.isEmpty()) {
            pageUnitLists.add(new ArrayList<>());
        }

        List<PageRegion> regions = assignPageRegions(tpl, pageUnitLists, mediumW, mediumH);
        List<PageLayout> pages = new ArrayList<>();
        for (int i = 0; i < pageUnitLists.size(); i++) {
            pages.add(new PageLayout(pageUnitLists.get(i), regions.get(i)));
        }
        // L4.3: duplex page qualification — insert blank pages so an oddOrEven-qualified pageArea
        // lands on the correct physical side (no-op unless the pageSet is duplexPaginated).
        pages = XfaDuplex.qualify(pages, tpl, mediumW, mediumH);
        return new PaginatedLayout(pages, mediumW, mediumH, mode);
    }

    /* ------------------------------ L4 decoration ------------------------------ */

    /**
     * Inserts the L4 boundary content into each flowed page: overflow leaders/trailers (bookends)
     * on every page within their occurrence cap, and explicit-break leaders/trailers at forced
     * breaks. Leaders go at the page top (content shifted down to make room); trailers at the page
     * bottom (after the content). The split that produced {@code pages} already reserved the
     * overflow leader/trailer height, so the content + boilerplate fits the region.
     */
    private static void decorateFlowed(List<List<XfaLayoutNode>> pages, List<Element> boundarySources,
                                       XfaBookends.Spec bk, FormDom dom, Template tpl, double regionWidth) {
        if (pages.isEmpty()) {
            return;
        }
        boolean anyBreak = false;
        for (Element s : boundarySources) {
            if (s != null) {
                anyBreak = true;
                break;
            }
        }
        if ((bk == null || !bk.hasOverflow()) && !anyBreak) {
            return; // no boundary content — leave the L3 pagination untouched (e.g. 408975)
        }

        // An overflow leader/trailer repeats only on the pages its OWNING container actually spans — a
        // header row belongs above the continuation of its own table, not on every page of a multi-
        // section form (11902: the first table's "PERSONNEL" header was leaking onto all 24 pages).
        // With no resolvable owner we keep the legacy every-page behaviour (the dominant single-table
        // corpus case). The owner is matched by name (template element vs. merged form-DOM units).
        String owner = bk == null ? null : bk.ownerName;
        boolean inBody = bk != null && bk.leaderInBody;

        // Pass A — leaders at the top of each page (overflow on continuation pages; break leader at its boundary).
        for (int p = 0; p < pages.size(); p++) {
            XfaLayoutNode breakLeader = p > 0
                    ? XfaBookends.breakLeader(boundarySources.get(p), dom, tpl, regionWidth) : null;
            boolean overflowLeader = bk != null && bk.overflowLeader != null && p < bk.overflowLeaderMax
                    && leaderAppliesToPage(pages, p, owner, inBody);
            if (breakLeader != null) {
                prependLeader(pages.get(p), breakLeader);
            }
            if (overflowLeader) {
                prependLeader(pages.get(p), bk.overflowLeader);
            }
        }

        // Pass B — trailers at the bottom (overflow where the owner spans onto the next page; break trailer on the page being left).
        for (int p = 0; p < pages.size(); p++) {
            boolean overflowTrailer = bk != null && bk.overflowTrailer != null && p < bk.overflowTrailerMax
                    && trailerAppliesToPage(pages, p, owner, inBody);
            if (overflowTrailer) {
                appendTrailer(pages.get(p), bk.overflowTrailer);
            }
            if (p > 0) {
                XfaLayoutNode breakTrailer = XfaBookends.breakTrailer(boundarySources.get(p), dom, tpl, regionWidth);
                if (breakTrailer != null) {
                    appendTrailer(pages.get(p - 1), breakTrailer);
                }
            }
        }
    }

    /**
     * Whether the overflow leader applies to page {@code p}: the owning container's content must
     * continue onto this page from the previous one (it spans the page boundary). With no owner the
     * leader is global (legacy single-table behaviour); page 0 never gets a leader (the table's own
     * header is part of its first-page content).
     */
    private static boolean leaderAppliesToPage(List<List<XfaLayoutNode>> pages, int p, String owner,
                                               boolean inBody) {
        if (owner == null || owner.isEmpty()) {
            return true; // no owner resolved — legacy every-page leader
        }
        if (!pageHasOwner(pages.get(p), owner)) {
            return false; // the owner's content is not on this page at all
        }
        // Header is part of the body (rendered once on the owner's first page) → repeat only where the
        // owner CONTINUES from the previous page. A separate leader subform repeats on every owner page.
        return !inBody || (p > 0 && pageHasOwner(pages.get(p - 1), owner));
    }

    /**
     * Whether the overflow trailer applies to page {@code p}: the owner spans this page, and — when the
     * trailer is part of the body — the owner also continues onto the NEXT page (the trailer marks a
     * mid-table page break, not the table's natural end).
     */
    private static boolean trailerAppliesToPage(List<List<XfaLayoutNode>> pages, int p, String owner,
                                                boolean inBody) {
        if (owner == null || owner.isEmpty()) {
            return true;
        }
        if (!pageHasOwner(pages.get(p), owner)) {
            return false;
        }
        return !inBody || (p + 1 < pages.size() && pageHasOwner(pages.get(p + 1), owner));
    }

    /** Whether any unit on {@code page} descends from a form-DOM container named {@code owner}. */
    private static boolean pageHasOwner(List<XfaLayoutNode> page, String owner) {
        for (XfaLayoutNode u : page) {
            for (Node n = u.getSource(); n instanceof Element; n = n.getParentNode()) {
                if (owner.equals(((Element) n).getAttribute("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Shifts a page's content down by the leader height and inserts the leader at the page top. */
    private static void prependLeader(List<XfaLayoutNode> page, XfaLayoutNode leaderProto) {
        double h = leaderProto.getHeight();
        for (int i = 0; i < page.size(); i++) {
            page.set(i, page.get(i).translated(0, h));
        }
        page.add(0, leaderProto.translated(0, 0));
    }

    /** Appends the trailer at the page's current content bottom (its last/lowest object). */
    private static void appendTrailer(List<XfaLayoutNode> page, XfaLayoutNode trailerProto) {
        double bottom = 0;
        for (XfaLayoutNode u : page) {
            bottom = Math.max(bottom, u.getBottom());
        }
        page.add(trailerProto.translated(0, bottom));
    }

    /* -------------------------------- L3.3 --------------------------------- */

    /**
     * Paginates {@code dom} and emits the multi-page result onto {@code doc}, painting each page
     * by reusing the C2 painter per object. A genuine single-page positioned form is delegated
     * verbatim to {@link XfaPainter#paint} (preserving the validated single-page fidelity).
     *
     * @param doc the target document (pages are added as needed)
     * @param dom the merged Form DOM
     * @param tpl the template, or {@code null}
     * @return the paginate-and-paint result
     * @throws Exception on document access failure
     */
    public static Result paint(Document doc, FormDom dom, Template tpl) throws Exception {
        // Resolve fonts from the source document's embedded programs first (extracted before the
        // placeholder pages are deleted), then host/fallback faces.
        return paint(doc, dom, tpl, org.aspose.pdf.engine.xfa.flatten.paint.XfaFontResolver.create(doc));
    }

    /**
     * Paginates and paints {@code dom}, embedding real fonts via {@code resolver} (XFA-FONTEMBED) where
     * resolvable, else the standard-14 substitute.
     *
     * @param resolver the font resolver (embedded&gt;host&gt;substitution); never {@code null}
     */
    public static Result paint(Document doc, FormDom dom, Template tpl,
                               org.aspose.pdf.engine.xfa.flatten.paint.XfaFontResolver resolver) throws Exception {
        XfaFlowLayout.Result layout = XfaFlowLayout.layout(dom, tpl);
        // L4: resolve the overflow boundary content (bookends) and reserve its height in the split so
        // the repeated leader/trailer + the flowed content together fit each page's region.
        XfaBookends.Spec bookends = XfaBookends.overflow(dom, tpl, layout.regionWidth);
        double splitRegionH = layout.regionHeight - bookends.leaderHeight() - bookends.trailerHeight();
        if (splitRegionH < 1) {
            splitRegionH = layout.regionHeight; // degenerate boilerplate taller than the region — ignore
        }
        XfaPageSplitter.SplitPlan plan = XfaPageSplitter.split(layout, splitRegionH);
        PaginatedLayout pag = paginate(layout, plan, tpl, bookends, dom);

        // Replace the source's XFA placeholder pages ("Please wait…" / "To view the full contents
        // of this document, you need a later version…") with the freshly painted form — Adobe
        // renders the live form in place of the placeholder, it is never part of the output.
        while (doc.getPages().getCount() > 0) {
            doc.getPages().delete(1);
        }

        Result out = new Result();
        out.mode = pag.mode;

        if (pag.mode == Mode.POSITIONED_SINGLE) {
            XfaPainter.Result pr = XfaPainter.paint(doc, dom, tpl, resolver);
            out.pages = 1;
            out.painted = pr.painted;
            out.fills = pr.fills;
            out.borders = pr.borders;
            out.texts = pr.texts;
            out.captions = pr.captions;
            out.presenceHidden = pr.presenceHidden;
            out.printSkipped = pr.printSkipped;
            out.images = pr.images;
            return out;
        }

        Map<Element, FormField> byElement = new IdentityHashMap<>();
        for (FormField f : dom.getFields()) {
            if (f.getFormNode() != null) {
                byElement.put(f.getFormNode().getElement(), f);
            }
        }
        // Master-page furniture channel: the bound <pageArea> elements + a value lookup over the
        // FormDom master fields (headers/footers/address blocks painted on each page as furniture).
        Map<Element, FormField> masterByElement = new IdentityHashMap<>();
        for (FormField f : dom.getMasterFields()) {
            if (f.getFormNode() != null) {
                masterByElement.put(f.getFormNode().getElement(), f);
            }
        }

        for (int p = 0; p < pag.pageCount(); p++) {
            PageLayout pl = pag.pages.get(p);
            // Each page is sized by its OWN assigned pageArea's medium (a pageSet may mix landscape and
            // portrait masters); a region with no own medium carries the global one as its fallback.
            double pageW = pl.region.mediumW > 0 ? pl.region.mediumW : pag.mediumW;
            double pageH = pl.region.mediumH > 0 ? pl.region.mediumH : pag.mediumH;
            Page page = p < doc.getPages().getCount() ? doc.getPages().get(p + 1) : doc.getPages().add();
            page.setPageSize(pageW, pageH);
            ContentStreamBuilder b = new ContentStreamBuilder();
            XfaPainter.Result pr = new XfaPainter.Result();
            // Page furniture FIRST (it is the master-page background): the bound pageArea assigned
            // to this physical page, painted at medium-relative (positioned) coordinates.
            paintPageFurniture(dom, masterByElement, pl, p + 1, pag.pageCount(), pageW, pageH, b, pr, resolver);
            for (XfaLayoutNode unit : pl.units) {
                paintNode(unit, byElement, pageH, pl.region.contentX, pl.region.contentY, b, pr, resolver);
            }
            XfaPainter.attach(page, b);
            out.painted += pr.painted;
            out.fills += pr.fills;
            out.borders += pr.borders;
            out.texts += pr.texts;
            out.captions += pr.captions;
            out.presenceHidden += pr.presenceHidden;
            out.printSkipped += pr.printSkipped;
            out.images += pr.images;
        }
        out.pages = pag.pageCount();
        return out;
    }

    /**
     * Prepends background-only box nodes for the styled flowed container subforms overlapping a page's
     * Y band to {@code page} (so they paint behind the page's units). A flowed container is transparent
     * to the splitter — its leaf children become the units while the container's own {@code <fill>}/
     * {@code <border>} (the grey section panel + box outline of the Czech insolvency forms) is dropped.
     * Each match is re-added as a <i>childless</i> box node (its children are already unit-painted),
     * rebased by {@code yOff} to the page top. Only transparent containers are walked, so a subtree
     * already painted whole as one atomic unit is never double-drawn.
     *
     * @param root       the flowed layout root
     * @param bandTop    the page band's top in absolute content-region Y (= {@code yOff})
     * @param bandBottom the page band's bottom in absolute content-region Y
     * @param yOff       the offset that rebases this page's content to the region top
     * @param page       the page's unit list; backgrounds are inserted at its front
     */
    private static void addStyledBackgrounds(XfaLayoutNode root, double bandTop, double bandBottom,
                                             double yOff, List<XfaLayoutNode> page) {
        if (root == null) {
            return;
        }
        List<XfaLayoutNode> bg = new ArrayList<>();
        collectStyledBackgrounds(root, bandTop, bandBottom, yOff, bg);
        page.addAll(0, bg);
    }

    private static void collectStyledBackgrounds(XfaLayoutNode node, double bandTop, double bandBottom,
                                                 double yOff, List<XfaLayoutNode> out) {
        for (XfaLayoutNode child : node.getChildren()) {
            if (!XfaPageSplitter.isTransparentContainer(child)) {
                continue; // atomic block — already emitted (and painted) whole as a unit
            }
            if (hasBoxStyling(child.getSource())
                    && child.getY() < bandBottom && child.getBottom() > bandTop) {
                out.add(new XfaLayoutNode(child.getSource(), child.getKind(),
                        child.getX(), child.getY() - yOff, child.getWidth(), child.getHeight()));
            }
            collectStyledBackgrounds(child, bandTop, bandBottom, yOff, out); // nested grey panels
        }
    }

    /** Whether {@code el} carries its own box styling (a {@code <fill>} or {@code <border>} child). */
    private static boolean hasBoxStyling(Element el) {
        if (el == null) {
            return false;
        }
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                String ln = n.getLocalName();
                if ("fill".equals(ln) || "border".equals(ln)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Walks a placed Layout-DOM node, painting each object at its page-local PDF rectangle via
     * the reused C2 primitives. Presence/relevant gating mirrors the C2 walk (a suppressed
     * subtree is not painted).
     */
    private static void paintNode(XfaLayoutNode node, Map<Element, FormField> byElement, double mediumH,
                                  double contentX, double contentY, ContentStreamBuilder b, XfaPainter.Result r,
                                  org.aspose.pdf.engine.xfa.flatten.paint.XfaFontResolver resolver) {
        Element el = node.getSource();
        if (el != null) {
            if (!XfaPainter.isPaintableForPrint(el)) {
                r.presenceHidden++;
                return; // suppressed subtree (presence hidden/invisible or relevant=-print)
            }
            // page-local content coords → PDF user space (flip Y against the medium height).
            double llx = contentX + node.getX();
            double topY = contentY + node.getY();
            double ury = mediumH - topY;
            double lly = ury - node.getHeight();
            double urx = llx + node.getWidth();
            Rectangle rect = new Rectangle(llx, lly, urx, ury);
            FormField ff = byElement.get(el);
            XfaPainter.paintPlaced(el, ff, byElement, rect, b, r, resolver);
        }
        for (XfaLayoutNode child : node.getChildren()) {
            paintNode(child, byElement, mediumH, contentX, contentY, b, r, resolver);
        }
    }

    /**
     * Paints a physical page's master-page furniture: the bound {@code <pageArea>} assigned to it
     * (header / footer / address blocks), with this page's "Page N of M" numbers resolved and any
     * last-/first-page-only presence applied. Shared by the render track ({@link #paint}) and the
     * AcroForm converter so both pages look the same. No-op when the page has no assigned pageArea.
     *
     * @param dom             the merged Form DOM (its master channel supplies the furniture)
     * @param masterByElement element→field map over {@link FormDom#getMasterFields()}
     * @param pl              the page layout (its region carries the pageArea index)
     * @param pageNum         this physical page's 1-based number
     * @param pageCount       the total physical page count
     */
    public static void paintPageFurniture(FormDom dom, Map<Element, FormField> masterByElement,
                                          PageLayout pl, int pageNum, int pageCount,
                                          double pageW, double pageH, ContentStreamBuilder b,
                                          XfaPainter.Result r,
                                          org.aspose.pdf.engine.xfa.flatten.paint.XfaFontResolver resolver) {
        List<Element> masterAreas = dom.getMasterPageAreas();
        int ai = pl.region.pageAreaIndex;
        if (ai < 0 || ai >= masterAreas.size()) {
            return;
        }
        Element area = masterAreas.get(ai);
        resolvePageNumberEmbeds(area, pageNum, pageCount);
        applyPagePresence(area, pageNum, pageCount);
        paintMasterFurniture(area, masterByElement, dom.getMasterFields(), pageW, pageH, b, r, resolver);
    }

    /**
     * Paints the master-page <b>furniture</b> of a bound {@code <pageArea>} onto the current page:
     * its positioned child subforms / draws / fields (the page header, footer, address blocks),
     * laid out at their medium-relative coordinates and painted via the reused C2 primitives. The
     * pageArea's structural children ({@code medium}, {@code contentArea}, {@code occur}) carry no
     * ink and are skipped. Resolves bound values from the FormDom master-field channel. Defensive:
     * a furniture failure never aborts the page.
     */
    private static void paintMasterFurniture(Element pageArea, Map<Element, FormField> byElement,
                                             List<FormField> masterFields, double mediumW, double mediumH,
                                             ContentStreamBuilder b, XfaPainter.Result r,
                                             org.aspose.pdf.engine.xfa.flatten.paint.XfaFontResolver resolver) {
        try {
            for (Element c = firstEl(pageArea); c != null; c = nextEl(c)) {
                String ln = local(c);
                if (!"subform".equals(ln) && !"draw".equals(ln) && !"field".equals(ln)
                        && !"area".equals(ln) && !"exclGroup".equals(ln)) {
                    continue; // medium / contentArea / occur — no furniture
                }
                if (!XfaPainter.isPaintableForPrint(c)) {
                    continue;
                }
                double cx = orZero(pt(c.getAttribute("x")));
                double cy = orZero(pt(c.getAttribute("y")));
                double cw = orZero(pt(c.getAttribute("w")));
                double ch = orZero(pt(c.getAttribute("h")));
                double[] tl = XfaGeometry.anchorTopLeft(cx, cy, cw, ch, c.getAttribute("anchorType"));
                XfaLayoutNode node = XfaFlowLayout.layoutFragment(c, cw > 0 ? cw : mediumW, masterFields);
                if (node == null) {
                    continue;
                }
                paintNode(node.translated(tl[0], tl[1]), byElement, mediumH, 0, 0, b, r, resolver);
            }
        } catch (RuntimeException ignore) {
            // furniture is best-effort page decoration; never abort the page over it
        }
    }

    /**
     * Resolves a master page's "Page N of M" numbering for the given physical page. XFA computes the
     * current page / page count in hidden fields via {@code xfa.layout.page(this)} /
     * {@code xfa.layout.pageCount()} (run at a {@code layout:ready} event we do not execute headlessly)
     * and embeds them into a rich-text {@code <draw>} through {@code <span xfa:embed="#fieldId">}. This
     * (1) finds those fields by their script and maps each {@code id} to this page's number / the total,
     * then (2) writes that number as the text content of every matching {@code xfa:embed} span, so the
     * rich-text painter renders "Page 1 of 3". Re-run per page (CurrentPage differs); idempotent.
     */
    private static void resolvePageNumberEmbeds(Element pageArea, int pageNum, int pageCount) {
        java.util.Map<String, String> byId = new java.util.HashMap<>();
        collectPageNumberFields(pageArea, pageNum, pageCount, byId);
        if (!byId.isEmpty()) {
            applyEmbedText(pageArea, byId);
        }
    }

    /** Maps the {@code id} of each page-number field (its script calls xfa.layout.page/pageCount) to its value. */
    private static void collectPageNumberFields(Element el, int pageNum, int pageCount,
                                                java.util.Map<String, String> byId) {
        if ("field".equals(local(el))) {
            String id = el.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                String script = descendantScriptText(el);
                if (script != null) {
                    if (script.contains("xfa.layout.pageCount")) {
                        byId.put(id, Integer.toString(pageCount));
                    } else if (script.contains("xfa.layout.page")) {
                        byId.put(id, Integer.toString(pageNum));
                    }
                }
            }
            return;
        }
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            collectPageNumberFields(c, pageNum, pageCount, byId);
        }
    }

    /** The concatenated text of all {@code <script>} descendants of {@code el}, or null if none. */
    private static String descendantScriptText(Element el) {
        StringBuilder sb = new StringBuilder();
        collectScriptText(el, sb);
        return sb.length() == 0 ? null : sb.toString();
    }

    private static void collectScriptText(Element el, StringBuilder sb) {
        if ("script".equals(local(el))) {
            String t = el.getTextContent();
            if (t != null) {
                sb.append(t);
            }
            return;
        }
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            collectScriptText(c, sb);
        }
    }

    /** Writes the mapped value as text content of every element carrying an {@code xfa:embed="#id"} attribute. */
    private static void applyEmbedText(Element el, java.util.Map<String, String> byId) {
        String embed = embedRef(el);
        if (embed != null) {
            String id = embed.startsWith("#") ? embed.substring(1) : embed;
            String v = byId.get(id);
            if (v != null) {
                el.setTextContent(v);
            }
        }
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            applyEmbedText(c, byId);
        }
    }

    /**
     * Applies a master-furniture object's page-conditional presence for the given physical page. Some
     * page furniture is shown only on the LAST page (XFA idiom: an {@code initialize} script
     * {@code if (curpage ne totpages) then $.presence = "hidden"}) or only on the FIRST page. We do not
     * run those scripts headlessly, so detect the idiom from the object's own event script and set its
     * {@code presence} per page: 14758's "END OF PURCHASE ORDER" ({@code END_PO}) then appears only on
     * the final page instead of every page. Re-applied per page (sets visible/hidden explicitly).
     */
    private static void applyPagePresence(Element el, int pageNum, int pageCount) {
        String ln = local(el);
        if ("subform".equals(ln) || "draw".equals(ln) || "field".equals(ln)) {
            String s = ownEventScript(el);
            if (s != null && s.contains("presence") && s.contains("hidden")) {
                Boolean show = null;
                if (s.contains("pageCount")) {
                    show = pageNum == pageCount; // "hidden unless last page"
                } else if (s.contains(".page(") && (s.contains("1") )
                        && (s.contains("eq 1") || s.contains("== 1") || s.contains("ne 1") || s.contains("!= 1"))) {
                    show = pageNum == 1;         // "hidden unless first page"
                }
                if (show != null) {
                    el.setAttribute("presence", show ? "visible" : "hidden");
                }
            }
        }
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            applyPagePresence(c, pageNum, pageCount);
        }
    }

    /** The concatenated text of {@code el}'s OWN direct {@code <event><script>} children (not descendants'). */
    private static String ownEventScript(Element el) {
        StringBuilder sb = new StringBuilder();
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            if ("event".equals(local(c))) {
                for (Element g = firstEl(c); g != null; g = nextEl(g)) {
                    if ("script".equals(local(g))) {
                        String t = g.getTextContent();
                        if (t != null) {
                            sb.append(t).append('\n');
                        }
                    }
                }
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** The {@code xfa:embed} attribute value of {@code el} (matched by local name, namespace-agnostic), or null. */
    private static String embedRef(Element el) {
        if (!el.hasAttributes()) {
            return null;
        }
        org.w3c.dom.NamedNodeMap attrs = el.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node a = attrs.item(i);
            String ln = a.getLocalName() != null ? a.getLocalName() : a.getNodeName();
            if ("embed".equals(ln) || "xfa:embed".equals(a.getNodeName())) {
                String v = a.getNodeValue();
                return v == null || v.isEmpty() ? null : v;
            }
        }
        return null;
    }

    /* ------------------------------- helpers ------------------------------- */

    private static boolean isFlowedRoot(XfaLayoutNode root) {
        Element el = root.getSource();
        if (el == null) {
            return false;
        }
        String layout = el.getAttribute("layout");
        return "tb".equals(layout) || "lr-tb".equals(layout) || "rl-tb".equals(layout)
                || "row".equals(layout) || "table".equals(layout);
    }

    /**
     * A positioned root's page-sized child subforms (each filling the content region) — the
     * authored "page subform" pattern. A subform whose box covers most of the region is a page.
     */
    private static List<XfaLayoutNode> pageFillers(XfaLayoutNode root, double regionW, double regionH) {
        List<XfaLayoutNode> fillers = new ArrayList<>();
        if (root == null || regionW <= 0 || regionH <= 0) {
            return fillers;
        }
        for (XfaLayoutNode c : root.getChildren()) {
            if ("subform".equals(c.getKind())
                    && c.getHeight() >= regionH * 0.8 && c.getWidth() >= regionW * 0.8) {
                fillers.add(c);
            }
        }
        return fillers;
    }

    /* ------------------------- L3.2 pageArea selection ------------------------- */

    /**
     * Assigns each page its content region from the template {@code <pageSet>}: ordered
     * {@code <pageArea>}s with {@code <occur max>} limits (a pageArea repeats up to its max, then
     * the next is used; the last repeats while content remains). The medium-sized contentArea is
     * the fallback when the template declares none.
     */
    private static List<PageRegion> assignPageRegions(Template tpl, List<List<XfaLayoutNode>> pageUnitLists,
                                                      double mediumW, double mediumH) {
        int pageCount = pageUnitLists.size();
        List<PageRegion> areas = collectPageAreas(tpl, mediumW, mediumH);
        PageRegion fallback = new PageRegion(null, -1, 0, 0, mediumW, mediumH, mediumW, mediumH);
        List<PageRegion> out = new ArrayList<>();
        if (areas.isEmpty()) {
            for (int i = 0; i < pageCount; i++) {
                out.add(fallback);
            }
            return out;
        }
        // When the pageSet declares pageAreas of DIFFERENT widths (e.g. 11902's landscape Page1 +
        // portrait Page2), blind ordered-occurrence assignment can hand a wide (landscape) page subform
        // a narrow (portrait) master — its furniture frame then overshoots the page and the content
        // overflows. Assign each page the pageArea whose contentArea width best fits the page's own
        // content width instead. Only engaged for varying-width pageSets; uniform-width pageSets (the
        // corpus norm, including single-master and cover+body forms) keep the spec ordered-occurrence.
        double minW = Double.MAX_VALUE, maxW = 0;
        for (PageRegion a : areas) {
            if (a.contentW > 0) {
                minW = Math.min(minW, a.contentW);
                maxW = Math.max(maxW, a.contentW);
            }
        }
        boolean varyingWidth = maxW > 0 && (maxW - minW) > 1.0;
        if (varyingWidth) {
            for (int p = 0; p < pageCount; p++) {
                out.add(bestFitArea(areas, pageContentWidth(pageUnitLists.get(p))));
            }
            return out;
        }
        int ai = 0;
        int used = 0;
        for (int p = 0; p < pageCount; p++) {
            PageRegion area = areas.get(ai);
            out.add(area);
            used++;
            int max = occurMax(area.pageArea);
            if (max > 0 && used >= max && ai < areas.size() - 1) {
                ai++;
                used = 0;
            }
        }
        return out;
    }

    /** The page's content width = the rightmost extent of its top-level units (0 if empty). */
    private static double pageContentWidth(List<XfaLayoutNode> units) {
        double w = 0;
        for (XfaLayoutNode u : units) {
            w = Math.max(w, u.getX() + u.getWidth());
        }
        return w;
    }

    /**
     * Picks the pageArea best fitting a page {@code width}: the narrowest contentArea that still holds
     * the content ({@code contentW >= width}); if none is wide enough, the widest available. Ties and
     * zero-width content keep the first (declaration order).
     */
    private static PageRegion bestFitArea(List<PageRegion> areas, double width) {
        PageRegion fit = null;
        PageRegion widest = areas.get(0);
        for (PageRegion a : areas) {
            if (a.contentW > widest.contentW) {
                widest = a;
            }
            if (a.contentW + 1.0 >= width && (fit == null || a.contentW < fit.contentW)) {
                fit = a;
            }
        }
        return fit != null ? fit : widest;
    }

    /** Collects the ordered pageAreas (with their contentArea boxes and own medium size) of the first pageSet. */
    private static List<PageRegion> collectPageAreas(Template tpl, double mediumW, double mediumH) {
        List<PageRegion> out = new ArrayList<>();
        if (tpl == null) {
            return out;
        }
        Element pageSet = findFirst(tpl.getElement(), "pageSet");
        if (pageSet == null) {
            return out;
        }
        int idx = 0;
        for (Element pa = firstEl(pageSet); pa != null; pa = nextEl(pa)) {
            if (!"pageArea".equals(local(pa))) {
                continue;
            }
            Element ca = findFirst(pa, "contentArea");
            double cx = ca != null ? pt(ca.getAttribute("x")) : 0;
            double cy = ca != null ? pt(ca.getAttribute("y")) : 0;
            double cw = ca != null ? pt(ca.getAttribute("w")) : 0;
            double ch = ca != null ? pt(ca.getAttribute("h")) : 0;
            // Each pageArea may carry its own <medium> (orientation/size); fall back to the global one.
            double[] m = XfaMedium.resolvePageArea(pa, new double[]{mediumW, mediumH});
            out.add(new PageRegion(pa, idx++, orZero(cx), orZero(cy), orZero(cw), orZero(ch), m[0], m[1]));
        }
        return out;
    }

    /** A pageArea's {@code <occur max>} (default 1; {@code -1} = unbounded). */
    private static int occurMax(Element pageArea) {
        if (pageArea == null) {
            return 1;
        }
        Element occur = firstChild(pageArea, "occur");
        if (occur == null) {
            return 1;
        }
        String max = occur.getAttribute("max");
        if (max == null || max.isEmpty()) {
            return 1;
        }
        try {
            return Integer.parseInt(max.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static double pt(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Double.NaN;
        }
        XfaMeasurement m = XfaMeasurement.parse(raw);
        return m == null ? Double.NaN : XfaGeometry.toPoints(m);
    }

    private static double orZero(double v) {
        return Double.isNaN(v) ? 0.0 : v;
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

    private static Element firstChild(Element el, String localName) {
        for (Element c = firstEl(el); c != null; c = nextEl(c)) {
            if (localName.equals(local(c))) {
                return c;
            }
        }
        return null;
    }

    private static Element findFirst(Element el, String localName) {
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element ce = (Element) n;
                if (localName.equals(local(ce))) {
                    return ce;
                }
                Element found = findFirst(ce, localName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
