package org.aspose.pdf.engine.xfa.flatten.layout;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/// Finds the page **split points** of a laid-out flowed form (Stage C, sprint L2 PART B):
/// walks the flow top-to-bottom against the per-page content region height and determines
/// where each page's content ends, honouring XFA **keep** (keep-together) and
/// **break** (forced break) rules.
///
/// **This class FINDS split points; it does NOT split, paginate, or relocate content.**
/// The output is data — the boundary node of each page and the remaining overflow — that L3
/// will act on (re-flow the tail onto subsequent pages). A split point partitions the unit
/// sequence; nothing is dropped or duplicated (content conservation).
///
/// The breakable units are atomic: a table row never splits mid-row, and a keep-together
/// group moves wholly to the next page if it does not fit. A unit taller than the whole region
/// is placed on its own page (it cannot be left off a page); splitting _within_ such an
/// over-tall unit is deferred to L3.
public final class XfaPageSplitter {

    private static final double EPS = 1e-6;

    private XfaPageSplitter() {
    }

    /// A page boundary: the first unit that belongs to the next page.
    public static final class SplitPoint {
        /// The layout node that starts the next page (page N's content begins here).
        public final XfaLayoutNode boundaryNode;
        /// The boundary node's index in the unit sequence.
        public final int boundaryIndex;
        /// Content height (points) remaining from this boundary to the end of the flow.
        public final double overflowRemaining;

        SplitPoint(XfaLayoutNode boundaryNode, int boundaryIndex, double overflowRemaining) {
            this.boundaryNode = boundaryNode;
            this.boundaryIndex = boundaryIndex;
            this.overflowRemaining = overflowRemaining;
        }
    }

    /// The split plan for one laid-out form (no splitting performed).
    public static final class SplitPlan {
        /// Per-page content region height (points) used for the fit test.
        public final double regionHeight;
        /// Total laid-out content height (points).
        public final double totalContentHeight;
        /// The split points, in order (empty ⇒ fits on one page).
        public final List<SplitPoint> splitPoints;
        /// Number of breakable units considered.
        public final int unitCount;

        SplitPlan(double regionHeight, double totalContentHeight, List<SplitPoint> splitPoints, int unitCount) {
            this.regionHeight = regionHeight;
            this.totalContentHeight = totalContentHeight;
            this.splitPoints = splitPoints;
            this.unitCount = unitCount;
        }

        /// @return the number of pages the content would occupy = split points + 1.
        public int pageCount() {
            return splitPoints.size() + 1;
        }

        /// @return `true` if the content needs more than one page (a split point exists).
        public boolean overflows() {
            return !splitPoints.isEmpty();
        }

        /// The unit-index ranges `[start, end)` of each page, derived from the split points.
        /// The ranges partition `[0, unitCount)` with no gap or overlap — the content
        /// conservation invariant.
        ///
        /// @return one `int[]{start,end}` per page, in order
        public List<int[]> pageRanges() {
            List<int[]> ranges = new ArrayList<>();
            int start = 0;
            for (SplitPoint sp : splitPoints) {
                ranges.add(new int[]{start, sp.boundaryIndex});
                start = sp.boundaryIndex;
            }
            ranges.add(new int[]{start, unitCount});
            return ranges;
        }
    }

    /// Computes the split plan for a laid-out form: the breakable units are the page-level
    /// objects of the flow (top-level flowed children, descending one level into a flowed
    /// container so its rows become units), measured against the form's per-page region height.
    ///
    /// @param layout the L1/L2 layout result
    /// @return the split plan (split points found, not acted on)
    public static SplitPlan split(XfaFlowLayout.Result layout) {
        return split(layout, layout.regionHeight);
    }

    /// Computes the split plan against an explicit per-page region height — used by L4 when a
    /// bookend (overflow leader/trailer) reduces the content room available on every page: the
    /// caller passes `regionHeight - leaderHeight - trailerHeight` so the split accounts for
    /// the repeated boilerplate, and the content still fits once the boilerplate is re-inserted.
    ///
    /// @param layout       the L1/L2 layout result
    /// @param regionHeight the effective per-page content region height in points
    /// @return the split plan (split points found, not acted on)
    public static SplitPlan split(XfaFlowLayout.Result layout, double regionHeight) {
        // Only a flowed root paginates. A positioned (or default-layout) root places its children
        // by absolute C1 coordinates — they may overlap and are not a top-to-bottom flow, so the
        // flow split is meaningless: such a form is single-page by construction (C1 unchanged).
        if (layout.root == null || !isFlowedRoot(layout.root)) {
            double total = layout.contentHeight;
            return new SplitPlan(regionHeight, total, new ArrayList<>(), 1);
        }
        List<XfaLayoutNode> units = breakableUnits(layout.root);
        Element rootEl = layout.root == null ? null : layout.root.getSource();
        SplitPlan plan = splitUnits(units, regionHeight, rootEl);
        if (Boolean.getBoolean("xfa.dumpSplit")) {
            dumpSplit(units, plan);
        }
        if (Boolean.getBoolean("xfa.dumpTree") && layout.root != null) {
            dumpTree(layout.root, 0);
        }
        return plan;
    }

    /// Diagnostic: prints the laid-out node tree with each node's geometry and declared minH/maxH.
    private static void dumpTree(XfaLayoutNode node, int depth) {
        Element el = node.getSource();
        String name = el == null ? "?" : (el.getLocalName() + ":" + el.getAttribute("name"));
        double minH = el == null ? 0 : XfaGrowableHeight.minH(el);
        double contentBottom = 0;
        for (XfaLayoutNode c : node.getChildren()) {
            contentBottom = Math.max(contentBottom, (c.getY() - node.getY()) + c.getHeight());
        }
        StringBuilder pad = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            pad.append("  ");
        }
        System.err.printf("%sy=%-7s h=%-7s minH=%-6s content=%-7s %s%n",
                pad, fmt(node.getY()), fmt(node.getHeight()), fmt(minH),
                node.getChildren().isEmpty() ? "-" : fmt(contentBottom), name);
        for (XfaLayoutNode c : node.getChildren()) {
            dumpTree(c, depth + 1);
        }
    }

    /// Diagnostic: prints each breakable unit with its geometry and keep/break flags + page ranges.
    private static void dumpSplit(List<XfaLayoutNode> units, SplitPlan plan) {
        System.err.println("=== xfa.dumpSplit: regionH=" + fmt(plan.regionHeight)
                + " totalH=" + fmt(plan.totalContentHeight) + " units=" + units.size()
                + " pages=" + plan.pageCount() + " ===");
        java.util.Set<Integer> bounds = new java.util.HashSet<>();
        for (SplitPoint sp : plan.splitPoints) {
            bounds.add(sp.boundaryIndex);
        }
        for (int k = 0; k < units.size(); k++) {
            XfaLayoutNode u = units.get(k);
            Element el = u.getSource();
            String name = el == null ? "?" : (el.getLocalName() + ":" + el.getAttribute("name"));
            StringBuilder kf = new StringBuilder();
            if (el != null) {
                if (keepWithNext(el)) kf.append(" keepNext");
                if (keepWithPrev(el)) kf.append(" keepPrev");
                if (truthyKeepIntact(el)) kf.append(" keepIntact");
                if (hasBreakBefore(el)) kf.append(" brkBefore");
                if (hasBreakAfter(el)) kf.append(" brkAfter");
            }
            System.err.printf("%s[%3d] y=%-8s h=%-8s %-34s%s%n",
                    bounds.contains(k) ? ">>" : "  ", k, fmt(u.getY()),
                    fmt(u.getBottom() - u.getY()), name, kf);
        }
        System.err.println("    pageRanges=" + java.util.Arrays.deepToString(plan.pageRanges().toArray()));
    }

    private static String fmt(double v) {
        return String.format("%.1f", v);
    }

    private static boolean isFlowedRoot(XfaLayoutNode root) {
        Element el = root.getSource();
        if (el == null) {
            return false;
        }
        String layout = el.getAttribute("layout");
        return "tb".equals(layout) || "lr-tb".equals(layout) || "rl-tb".equals(layout)
                || "row".equals(layout) || "table".equals(layout);
    }

    /// Core split algorithm over a unit sequence (each unit's `y`/`height` are in
    /// content-region coordinates). Walks the units against `regionHeight`, grouping
    /// keep-together neighbours and honouring forced breaks, and records a split point before
    /// each unit that starts a new page. No unit is moved or dropped.
    ///
    /// @param units        the breakable units, in flow order
    /// @param regionHeight the per-page content region height in points
    /// @return the split plan
    public static SplitPlan splitUnits(List<XfaLayoutNode> units, double regionHeight) {
        return splitUnits(units, regionHeight, null);
    }

    /// As [#splitUnits(List, double)] but with the layout `rootEl` so the heading-orphan
    /// keep-region logic can bound a keep chain to one top-level section (it stops a chain at the
    /// form root). Pass `null` to disable that extension (adjacent keeps only).
    public static SplitPlan splitUnits(List<XfaLayoutNode> units, double regionHeight, Element rootEl) {
        int n = units.size();
        List<SplitPoint> points = new ArrayList<>();
        if (n == 0) {
            return new SplitPlan(regionHeight, 0, points, 0);
        }
        double total = units.get(n - 1).getBottom() - units.get(0).getY();

        // "glue[k]" = no page break may fall between unit k and unit k+1. Two kinds of glue:
        //  (1) ADJACENT keep — A <keep next> B, or B <keep previous> A (the literal XFA rule).
        //  (2) KEEP-REGION — a section heading (<keep next>) and its body (<keep previous>) are
        //      separated by intervening rows that declare NO keep (e.g. on 11367 the "Pohledávka č."
        //      heading, the claim-type field, and the "pohledavka_zahlavi" body). The literal rule
        //      permits a break between those rows, stranding the heading at the page foot while the
        //      body overflows. Adobe keeps the whole block together; we bridge the <keep next> to its
        //      matching <keep previous> within their common ancestor subform, then absorb the section's
        //      light leading furniture (title + zero-height spacers) so the heading travels with it.
        boolean[] glue = new boolean[n];
        for (int k = 0; k + 1 < n; k++) {
            if (keepWithNext(units.get(k).getSource()) || keepWithPrev(units.get(k + 1).getSource())) {
                glue[k] = true;
            }
        }
        for (int p = 0; p + 1 < n; p++) {
            Element e = units.get(p).getSource();
            if (e == null || !keepWithNext(e)) {
                continue;
            }
            int close = keepRegionClose(units, p, rootEl);
            if (close <= p) {
                continue;
            }
            int start = furnitureStart(units, p, rootEl);
            for (int k = start; k < close; k++) {
                glue[k] = true;
            }
        }

        // (3) SECTION KEEP — a whole top-level section (a direct child of the form root) that fits
        //     within ONE region is kept together rather than split. Adobe starts such a section on a
        //     fresh content area instead of stranding its head on the tail of the previous page: on
        //     11367 the entire claim section moves to p2 (leaving p1 ending after "Věřitel") even
        //     though its first rows would fit p1's tail. Bounded by regionHeight — an over-tall section
        //     still splits (and the keep-region above keeps at least its heading with its body).
        // The sections are the direct children of the deepest container that holds ALL units (the flow
        // parent) — NOT of rootEl, which may sit above a single-child wrapper subform.
        Element flowParent = n > 1
                ? commonAncestor(units.get(0).getSource(), units.get(n - 1).getSource()) : null;
        if (flowParent != null) {
            int a = 0;
            while (a < n) {
                Element secA = sectionOf(units.get(a).getSource(), flowParent);
                int b = a + 1;
                while (b < n && sectionOf(units.get(b).getSource(), flowParent) == secA) {
                    b++;
                }
                if (secA != null && b - a > 1
                        && units.get(b - 1).getBottom() - units.get(a).getY() <= regionHeight + EPS) {
                    for (int k = a; k < b - 1; k++) {
                        glue[k] = true;
                    }
                }
                a = b;
            }
        }

        // Maximal runs of glued units become atomic groups for the fit test.
        List<int[]> groups = new ArrayList<>();
        int i = 0;
        while (i < n) {
            int j = i;
            while (j + 1 < n && glue[j]) {
                j++;
            }
            groups.add(new int[]{i, j + 1});
            i = j + 1;
        }

        double pageTop = units.get(0).getY();
        int g = 0;
        int gCount = groups.size();
        while (g < gCount) {
            int pageStart = g;
            while (g < gCount) {
                int[] grp = groups.get(g);
                XfaLayoutNode first = units.get(grp[0]);
                XfaLayoutNode last = units.get(grp[1] - 1);
                if (g > pageStart && hasBreakBefore(first.getSource())) {
                    break; // forced break before this group sets the boundary
                }
                double used = last.getBottom() - pageTop;
                if (g == pageStart) {
                    g++; // the first group always occupies the page (even if taller than the region)
                } else if (used <= regionHeight + EPS) {
                    g++; // fits
                } else {
                    break; // group overflows → it (wholly) moves to the next page
                }
                if (hasBreakAfter(units.get(groups.get(g - 1)[1] - 1).getSource())) {
                    break; // forced break after the group just placed
                }
            }
            if (g < gCount) {
                int boundaryIdx = groups.get(g)[0];
                XfaLayoutNode boundary = units.get(boundaryIdx);
                double remaining = units.get(n - 1).getBottom() - boundary.getY();
                points.add(new SplitPoint(boundary, boundaryIdx, remaining));
                pageTop = boundary.getY();
            }
        }
        return new SplitPlan(regionHeight, total, points, n);
    }

    /* --------------------------- breakable units --------------------------- */

    /// Derives the page-breakable unit sequence from the layout root: descend through flowed
    /// containers (tb / lr-tb / table subforms) down to **atomic** units — a table row
    /// (never split mid-row), a leaf field/draw, a positioned block, a keep-intact subform, or
    /// a non-flowed/leaf container. A flowed container that merely groups other flow content is
    /// transparent to the page split; its atomic descendants are the things a page boundary can
    /// fall between.
    public static List<XfaLayoutNode> breakableUnits(XfaLayoutNode root) {
        List<XfaLayoutNode> units = new ArrayList<>();
        if (root == null) {
            return units;
        }
        collectUnits(root, units);
        if (units.isEmpty()) {
            units.addAll(root.getChildren());
        }
        return units;
    }

    private static void collectUnits(XfaLayoutNode container, List<XfaLayoutNode> out) {
        for (XfaLayoutNode child : container.getChildren()) {
            if (isAtomic(child)) {
                out.add(child);
            } else {
                collectUnits(child, out); // transparent flowed group — descend to its units
            }
        }
    }

    /// Whether the splitter treats `node` as a **transparent** flowed group — one it descends
    /// into so that its leaf children become the breakable units, rather than an atomic block. Such a
    /// container is never emitted as a unit, so its own `<fill>`/`<border>` (a grey section
    /// panel) must be repainted separately by the paginator as a per-page background.
    public static boolean isTransparentContainer(XfaLayoutNode node) {
        return !isAtomic(node);
    }

    /// Whether a node is an indivisible page-break unit (a boundary can't fall inside it).
    private static boolean isAtomic(XfaLayoutNode node) {
        Element el = node.getSource();
        if (el == null || node.getChildren().isEmpty() || node.isPositioned()) {
            return true; // leaf / empty / positioned block
        }
        String kind = node.getKind();
        if (!("subform".equals(kind) || "subformSet".equals(kind) || "area".equals(kind))) {
            return true; // not a container (field/draw/exclGroup) → atomic
        }
        String layout = el.getAttribute("layout");
        if ("row".equals(layout)) {
            return true; // a table row never splits mid-row
        }
        if (keepWithNext(el) || keepWithPrev(el) || truthyKeepIntact(el)) {
            return true; // keep-together subform stays whole
        }
        boolean flowed = "tb".equals(layout) || "lr-tb".equals(layout)
                || "rl-tb".equals(layout) || "table".equals(layout);
        return !flowed; // a positioned/default container is one block; a flowed one is transparent
    }

    /// The closing unit of a keep-region opened by a `<keep next>` at index `p`: the
    /// nearest later unit carrying `<keep previous>` that shares a common ancestor subform with
    /// `p` (so the pair is one logical block — a heading and its body). Returns `p` when no
    /// such close exists before the chain leaves the section (its ancestor reaches `rootEl`).
    private static int keepRegionClose(List<XfaLayoutNode> units, int p, Element rootEl) {
        Element pe = units.get(p).getSource();
        if (pe == null) {
            return p;
        }
        int n = units.size();
        for (int m = p + 1; m < n; m++) {
            Element me = units.get(m).getSource();
            if (me == null) {
                return p;
            }
            Element anc = commonAncestor(pe, me);
            if (anc == null || anc == rootEl) {
                return p; // left the section without a matching keep-previous
            }
            if (keepWithPrev(me)) {
                return m;
            }
        }
        return p;
    }

    /// Walks back from a keep-region's heading (index `p`) over the section's **light leading
    /// furniture** — zero-height spacers and small keep-flagged titles in the same top-level section —
    /// so the section title travels onto the next page with the heading instead of stranding. Stops at
    /// the first substantial unit or the section boundary (common ancestor with `p` is the root).
    private static int furnitureStart(List<XfaLayoutNode> units, int p, Element rootEl) {
        Element pe = units.get(p).getSource();
        if (pe == null) {
            return p;
        }
        int start = p;
        for (int t = p - 1; t >= 0; t--) {
            XfaLayoutNode u = units.get(t);
            Element te = u.getSource();
            if (te == null) {
                break;
            }
            Element anc = commonAncestor(pe, te);
            if (anc == null || anc == rootEl) {
                break; // a different top-level section — do not absorb it
            }
            double h = u.getBottom() - u.getY();
            boolean light = h < 1.0
                    || ((keepWithPrev(te) || keepWithNext(te) || truthyKeepIntact(te)) && h < 40.0);
            if (!light) {
                break; // substantial content — a legitimate page-break point precedes it
            }
            start = t;
        }
        return start;
    }

    /// The top-level section `el` belongs to: its ancestor that is a direct child of
    /// `rootEl` (or `el` itself if it is one). Null if `el` is not under `rootEl`.
    private static Element sectionOf(Element el, Element rootEl) {
        if (el == null) {
            return null;
        }
        Element cur = el;
        for (Node p = el.getParentNode(); p instanceof Element; p = p.getParentNode()) {
            if (p == rootEl) {
                return cur;
            }
            cur = (Element) p;
        }
        return null;
    }

    /// The nearest ancestor element of `a` that also contains `b` (their join), or null.
    private static Element commonAncestor(Element a, Element b) {
        for (Node pn = a.getParentNode(); pn instanceof Element; pn = pn.getParentNode()) {
            if (isWithin(b, (Element) pn)) {
                return (Element) pn;
            }
        }
        return null;
    }

    /// Whether `node` is `ancestor` or nested within it.
    private static boolean isWithin(Element node, Element ancestor) {
        for (Node pn = node; pn != null; pn = pn.getParentNode()) {
            if (pn == ancestor) {
                return true;
            }
        }
        return false;
    }

    private static boolean truthyKeepIntact(Element el) {
        Element keep = firstChild(el, "keep");
        return keep != null && keep.getAttribute("intact") != null
                && !keep.getAttribute("intact").isEmpty() && !"none".equals(keep.getAttribute("intact"));
    }

    /* ----------------------------- keep / break ---------------------------- */

    /// A unit keeps with its next sibling: `<keep next="contentArea|pageArea|…">`. NOTE:
    /// `keep.intact` is deliberately NOT consulted here — `intact` means "do not split
    /// THIS object across content areas" (it makes the object atomic, handled by [#isAtomic]),
    /// not "stay on the same page as my neighbour". Conflating the two chains every
    /// `intact="contentArea"` sibling into one giant unbreakable block, stranding earlier content on a
    /// near-empty page.
    static boolean keepWithNext(Element el) {
        Element keep = firstChild(el, "keep");
        return keep != null && truthyKeep(keep.getAttribute("next"));
    }

    /// A unit keeps with its previous sibling: `<keep previous="contentArea|…">` (not `intact`; see [#keepWithNext]).
    static boolean keepWithPrev(Element el) {
        Element keep = firstChild(el, "keep");
        return keep != null && truthyKeep(keep.getAttribute("previous"));
    }

    /// A keep target other than absent/empty/`none` means "stay together".
    private static boolean truthyKeep(String v) {
        return v != null && !v.isEmpty() && !"none".equals(v);
    }

    /// A forced break before the unit: a `<break before="contentArea|pageArea|…">`, a
    /// legacy `<breakBefore>` child, or a `breakBefore` attribute.
    static boolean hasBreakBefore(Element el) {
        if (el == null) {
            return false;
        }
        if (truthyBreak(el.getAttribute("breakBefore"))) {
            return true;
        }
        if (firstChild(el, "breakBefore") != null) {
            return true;
        }
        Element br = firstChild(el, "break");
        return br != null && truthyBreak(br.getAttribute("before"));
    }

    /// A forced break after the unit (symmetric to [#hasBreakBefore]).
    static boolean hasBreakAfter(Element el) {
        if (el == null) {
            return false;
        }
        if (truthyBreak(el.getAttribute("breakAfter"))) {
            return true;
        }
        if (firstChild(el, "breakAfter") != null) {
            return true;
        }
        Element br = firstChild(el, "break");
        return br != null && truthyBreak(br.getAttribute("after"));
    }

    /// A break target other than absent/empty/`auto` forces the break.
    private static boolean truthyBreak(String v) {
        return v != null && !v.isEmpty() && !"auto".equals(v);
    }

    private static Element firstChild(Element el, String localName) {
        if (el == null) {
            return null;
        }
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String ln = n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
                if (localName.equals(ln)) {
                    return (Element) n;
                }
            }
        }
        return null;
    }
}
