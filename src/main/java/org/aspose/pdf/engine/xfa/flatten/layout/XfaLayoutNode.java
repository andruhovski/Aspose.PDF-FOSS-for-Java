package org.aspose.pdf.engine.xfa.flatten.layout;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/// A placed object in the XFA **Layout DOM** (Stage C, sprint L1) — the internal
/// tree produced when a flowed-root form is laid into a content region.
///
/// Each node records the source Form DOM element, its resolved position and size,
/// and its placed children. Coordinates are in **content-region top-left space**:
/// `x` increases rightward from the region's left edge, `y` increases
/// _downward_ from the region's top edge (the natural XFA layout space, the same
/// space [org.aspose.pdf.engine.xfa.flatten.XfaGeometry] accumulates in before its
/// single Y-flip to PDF user space). The flip to PDF bottom-left coordinates happens at
/// paint composition (L5) — L1 stays purely in layout space so the placement formula
/// (`child.y = Σ preceding sibling heights`) reads directly off these fields.
///
/// L1 lays content into a SINGLE region with no pagination; a `y + height` that
/// exceeds the region height is the overflow that L2 (split point) and L3 (pagination)
/// will consume — L1 measures it, it does not split.
public final class XfaLayoutNode {

    /// Element kinds treated as growable leaves (no layout children of their own).
    public static final String KIND_FIELD = "field";
    /// A static draw (label / line-art host).
    public static final String KIND_DRAW = "draw";

    private final Element source;
    private final String kind;
    private double x;
    private double y;
    private double width;
    private double height;
    private boolean growable;
    private boolean positioned;
    private final List<XfaLayoutNode> children = new ArrayList<>();

    /// Creates a placed layout node.
    ///
    /// @param source the source Form DOM element this node was placed from
    /// @param kind   the element local name (subform / field / draw / exclGroup / area)
    /// @param x      the top-left X in content-region space (rightward from region left)
    /// @param y      the top-left Y in content-region space (downward from region top)
    /// @param width  the box width in points
    /// @param height the box height in points (resolved, possibly data-driven)
    public XfaLayoutNode(Element source, String kind, double x, double y, double width, double height) {
        this.source = source;
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /// @return the source Form DOM element.
    public Element getSource() {
        return source;
    }

    /// @return the element local name (subform / field / draw / exclGroup / area).
    public String getKind() {
        return kind;
    }

    /// @return the top-left X in content-region space (points).
    public double getX() {
        return x;
    }

    /// @return the top-left Y in content-region space (points, downward from the region top).
    public double getY() {
        return y;
    }

    /// @return the box width in points.
    public double getWidth() {
        return width;
    }

    /// @return the resolved box height in points.
    public double getHeight() {
        return height;
    }

    /// @return the bottom edge of this box (`y + height`) in content-region space.
    public double getBottom() {
        return y + height;
    }

    /// @return `true` if this object's height was data-driven (no fixed `h`;
    ///         it grew to its content / its children)
    public boolean isGrowable() {
        return growable;
    }

    /// @return `true` if this object was placed by its own `x`/`y`
    ///         (positioned subtree inside the flow), `false` if stacked by flow
    public boolean isPositioned() {
        return positioned;
    }

    /// @return the placed children, in document order.
    public List<XfaLayoutNode> getChildren() {
        return children;
    }

    void setHeight(double height) {
        this.height = height;
    }

    void setWidth(double width) {
        this.width = width;
    }

    void setGrowable(boolean growable) {
        this.growable = growable;
    }

    void setPositioned(boolean positioned) {
        this.positioned = positioned;
    }

    void addChild(XfaLayoutNode child) {
        children.add(child);
    }

    /// Returns a deep copy of this subtree shifted by `(dx, dy)` — used by the L3
    /// paginator to rebase a page's content to its on-page origin without mutating the original
    /// (single-region) Layout DOM.
    ///
    /// @param dx horizontal shift in points
    /// @param dy vertical shift in points
    /// @return a translated deep copy
    public XfaLayoutNode translated(double dx, double dy) {
        XfaLayoutNode copy = new XfaLayoutNode(source, kind, x + dx, y + dy, width, height);
        copy.growable = growable;
        copy.positioned = positioned;
        for (XfaLayoutNode child : children) {
            copy.addChild(child.translated(dx, dy));
        }
        return copy;
    }

    @Override
    public String toString() {
        return String.format("%s[x=%.1f y=%.1f w=%.1f h=%.1f%s%s kids=%d]",
                kind, x, y, width, height,
                growable ? " growable" : "", positioned ? " positioned" : "", children.size());
    }
}
