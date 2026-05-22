package org.aspose.pdf.tagged;

/**
 * Runtime exception thrown when tagged PDF validation rules are violated.
 *
 * <p>This exception is used for structural validation errors in the
 * logical structure tree, such as invalid parent-child relationships
 * between structure elements (ISO 32000-1:2008, §14.8).</p>
 */
public class TaggedException extends RuntimeException {

    /**
     * Creates a TaggedException with the specified detail message.
     *
     * @param message the detail message
     */
    public TaggedException(String message) {
        super(message);
    }

    /**
     * Creates a TaggedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public TaggedException(String message, Throwable cause) {
        super(message, cause);
    }
}
