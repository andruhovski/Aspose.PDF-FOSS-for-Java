package org.aspose.pdf;

/// Abstract base for hyperlinks attached to layout paragraphs such as
/// [org.aspose.pdf.text.TextFragment], [Image] and [Heading].
///
/// Concrete subclasses describe where a click on the host paragraph
/// navigates to: [WebHyperlink] opens an external URL,
/// [LocalHyperlink] jumps to an in-document anchor, and
/// [FileHyperlink] launches an external file. Mirrors the Aspose.PDF
/// `Aspose.Pdf.Hyperlink` class hierarchy used by the Generator API.
public abstract class Hyperlink {
    // Marker base — kind-specific data lives on each subclass.
}
