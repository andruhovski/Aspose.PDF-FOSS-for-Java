package org.aspose.pdf.drawing;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * A collection of {@link Shape} objects with optional bounds checking.
 * <p>
 * When the {@link BoundsCheckMode} is set to
 * {@link BoundsCheckMode#ThrowExceptionIfDoesNotFit}, each shape added to
 * the collection is validated against the container dimensions. If the shape
 * does not fit, a {@link BoundsOutOfRangeException} is thrown.
 * </p>
 */
public class ShapeCollection extends ArrayList<Shape> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ShapeCollection.class.getName());

    private BoundsCheckMode boundsCheckMode = BoundsCheckMode.Default;
    private double containerWidth;
    private double containerHeight;

    /**
     * Creates a new shape collection with the specified container dimensions.
     *
     * @param width  the container width in user-space units
     * @param height the container height in user-space units
     */
    public ShapeCollection(double width, double height) {
        this.containerWidth = width;
        this.containerHeight = height;
    }

    /**
     * Gets the current bounds check mode.
     *
     * @return the bounds check mode
     */
    public BoundsCheckMode getBoundsCheckMode() {
        return boundsCheckMode;
    }

    /**
     * Updates the bounds check mode for this collection.
     *
     * @param mode the new bounds check mode
     */
    public void updateBoundsCheckMode(BoundsCheckMode mode) {
        this.boundsCheckMode = mode;
    }

    /**
     * Gets the container width used for bounds checking.
     *
     * @return the container width
     */
    public double getContainerWidth() {
        return containerWidth;
    }

    /**
     * Gets the container height used for bounds checking.
     *
     * @return the container height
     */
    public double getContainerHeight() {
        return containerHeight;
    }

    /**
     * Adds a shape to this collection. If bounds checking is enabled, the shape
     * is validated against the container dimensions before being added.
     *
     * @param shape the shape to add
     * @return {@code true} (as specified by {@link java.util.Collection#add})
     * @throws BoundsOutOfRangeException if bounds checking is enabled and the
     *                                   shape does not fit within the container
     */
    @Override
    public boolean add(Shape shape) {
        if (boundsCheckMode == BoundsCheckMode.ThrowExceptionIfDoesNotFit) {
            shape.checkBounds(containerWidth, containerHeight);
        }
        return super.add(shape);
    }
}
