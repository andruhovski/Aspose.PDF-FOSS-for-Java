package org.aspose.pdf;

/**
 * Abstract base for hyperlinks attached to layout paragraphs such as
 * {@link TextFragment}, {@link Image} and {@code Heading}.
 *
 * <p>Concrete subclasses describe where a click on the host paragraph
 * navigates to: {@link WebHyperlink} opens an external URL,
 * {@link LocalHyperlink} jumps to an in-document anchor, and
 * {@link FileHyperlink} launches an external file. Mirrors the Aspose.PDF
 * {@code Aspose.Pdf.Hyperlink} class hierarchy used by the Generator API.</p>
 */
public abstract class Hyperlink {
    // Marker base — kind-specific data lives on each subclass.
}
