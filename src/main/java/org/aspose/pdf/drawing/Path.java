package org.aspose.pdf.drawing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a composite path made up of child {@link Shape} elements.
 * <p>
 * A path can contain any combination of lines, curves, rectangles, and other
 * shapes. Bounds checking delegates to each child shape.
 * </p>
 */
public class Path extends Shape {

    private static final Logger LOG = Logger.getLogger(Path.class.getName());

    private final List<Shape> shapes = new ArrayList<>();

    /**
     * Creates a new empty path.
     */
    public Path() {
        // empty path
    }

    /**
     * Gets the list of child shapes in this path.
     * <p>
     * The returned list is mutable; shapes can be added or removed directly.
     * </p>
     *
     * @return the list of child shapes
     */
    public List<Shape> getShapes() {
        return shapes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks bounds for all child shapes in this path.
     * </p>
     */
    @Override
    public void checkBounds(double width, double height) {
        for (Shape shape : shapes) {
            shape.checkBounds(width, height);
        }
    }
}
