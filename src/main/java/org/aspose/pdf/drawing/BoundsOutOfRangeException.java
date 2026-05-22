package org.aspose.pdf.drawing;

/**
 * Thrown when a drawing shape does not fit within the bounds of its container.
 * <p>
 * This exception is raised during bounds checking when
 * {@link BoundsCheckMode#ThrowExceptionIfDoesNotFit} is active and a shape's
 * geometry exceeds the container dimensions.
 * </p>
 */
public class BoundsOutOfRangeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code BoundsOutOfRangeException} with the specified detail message.
     *
     * @param message the detail message describing which bounds were exceeded
     */
    public BoundsOutOfRangeException(String message) {
        super(message);
    }
}
