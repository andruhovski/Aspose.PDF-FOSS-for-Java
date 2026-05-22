package org.aspose.pdf.drawing;

import org.aspose.pdf.BaseParagraph;
import org.aspose.pdf.BorderInfo;

import java.util.logging.Logger;

/**
 * Represents a drawing canvas that can be added to a PDF page's paragraph collection.
 * <p>
 * A {@code Graph} extends {@link BaseParagraph} so it participates in the page layout
 * system. It contains a {@link ShapeCollection} of drawing shapes (rectangles, circles,
 * lines, curves, etc.) and provides dimensions, positioning, and styling properties.
 * </p>
 *
 * <pre>{@code
 * Graph graph = new Graph(200, 100);
 * graph.getShapes().add(new Rectangle(10, 10, 80, 40));
 * page.getParagraphs().add(graph);
 * }</pre>
 */
public class Graph extends BaseParagraph {

    private static final Logger LOG = Logger.getLogger(Graph.class.getName());

    private double width;
    private double height;
    private double left;
    private double top;
    private boolean isChangePosition = true;
    private BorderInfo border;
    private GraphInfo graphInfo = new GraphInfo();
    private ShapeCollection shapes;
    private String title;

    /**
     * Creates a new graph canvas with the specified dimensions.
     *
     * @param width  the width of the canvas in user-space units
     * @param height the height of the canvas in user-space units
     */
    public Graph(double width, double height) {
        this.width = width;
        this.height = height;
        this.shapes = new ShapeCollection(width, height);
    }

    /**
     * Gets the width of this graph canvas.
     *
     * @return the width in user-space units
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the width of this graph canvas.
     *
     * @param width the width in user-space units
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Gets the height of this graph canvas.
     *
     * @return the height in user-space units
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the height of this graph canvas.
     *
     * @param height the height in user-space units
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Gets the left position offset.
     *
     * @return the left offset in user-space units
     */
    public double getLeft() {
        return left;
    }

    /**
     * Sets the left position offset.
     *
     * @param left the left offset in user-space units
     */
    public void setLeft(double left) {
        this.left = left;
    }

    /**
     * Gets the top position offset.
     *
     * @return the top offset in user-space units
     */
    public double getTop() {
        return top;
    }

    /**
     * Sets the top position offset.
     *
     * @param top the top offset in user-space units
     */
    public void setTop(double top) {
        this.top = top;
    }

    /**
     * Returns whether position changes are tracked for layout.
     *
     * @return {@code true} if position changes are applied; defaults to {@code true}
     */
    public boolean isChangePosition() {
        return isChangePosition;
    }

    /**
     * Sets whether position changes are tracked for layout.
     *
     * @param changePosition {@code true} to enable position change tracking
     */
    public void setChangePosition(boolean changePosition) {
        this.isChangePosition = changePosition;
    }

    /**
     * Gets the border styling for this graph.
     *
     * @return the border info, or {@code null} if not set
     */
    public BorderInfo getBorder() {
        return border;
    }

    /**
     * Sets the border styling for this graph.
     *
     * @param border the border info to apply
     */
    public void setBorder(BorderInfo border) {
        this.border = border;
    }

    /**
     * Gets the graphic styling properties for this graph.
     *
     * @return the graph info; never {@code null}
     */
    public GraphInfo getGraphInfo() {
        return graphInfo;
    }

    /**
     * Sets the graphic styling properties for this graph.
     *
     * @param graphInfo the graph info to apply
     */
    public void setGraphInfo(GraphInfo graphInfo) {
        this.graphInfo = graphInfo;
    }

    /**
     * Gets the collection of shapes contained in this graph.
     *
     * @return the shape collection; never {@code null}
     */
    public ShapeCollection getShapes() {
        return shapes;
    }

    /**
     * Gets the title of this graph.
     *
     * @return the title, or {@code null} if not set
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this graph.
     *
     * @param title the title text
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
