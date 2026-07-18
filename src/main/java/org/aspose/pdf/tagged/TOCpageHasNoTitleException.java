package org.aspose.pdf.tagged;

/// Exception thrown when attempting to link a TOC page title to a header element,
/// but the TOC page does not have a title set via `TocInfo`.
///
/// Before calling `TOCElement.linkTocPageTitleToHeaderElement()`,
/// the page must have a `TocInfo` with a non-null title.
public class TOCpageHasNoTitleException extends TaggedException {

    /// Creates a TOCpageHasNoTitleException with the specified detail message.
    ///
    /// @param message the detail message
    public TOCpageHasNoTitleException(String message) {
        super(message);
    }

    /// Creates a TOCpageHasNoTitleException with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param cause   the cause
    public TOCpageHasNoTitleException(String message, Throwable cause) {
        super(message, cause);
    }
}
