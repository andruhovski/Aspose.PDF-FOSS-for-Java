package org.aspose.pdf.tagged;

/// Exception thrown when a header element's text conflicts with the TOC page title.
///
/// This occurs when attempting to save a document where a header element
/// linked to a TOC page has text that was set independently of the TOC page title,
/// creating an ambiguous or conflicting state.
public class HeaderElementTextConflictException extends TaggedException {

    /// Creates a HeaderElementTextConflictException with the specified detail message.
    ///
    /// @param message the detail message
    public HeaderElementTextConflictException(String message) {
        super(message);
    }

    /// Creates a HeaderElementTextConflictException with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param cause   the cause
    public HeaderElementTextConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
