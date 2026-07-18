package org.aspose.pdf.text;

/// Options for text extraction from PDF pages.
///
/// Controls formatting mode:
///
///   - [TextFormattingMode#Raw] — extracts text in the order it appears in the content stream
///   - [TextFormattingMode#Pure] — attempts to reconstruct visual layout with spacing
public class TextExtractionOptions {

    /// Text formatting modes for extraction.
    public enum TextFormattingMode {
        /// Raw order from content stream.
        Raw,
        /// Reconstructed visual layout.
        Pure,
        /// Memory-saving mode for large documents.
        MemorySaving
    }

    private TextFormattingMode formattingMode;

    /// Creates TextExtractionOptions with the given formatting mode.
    ///
    /// @param mode the formatting mode
    public TextExtractionOptions(TextFormattingMode mode) {
        this.formattingMode = mode;
    }

    /// Returns the current formatting mode.
    ///
    /// @return the formatting mode
    public TextFormattingMode getFormattingMode() {
        return formattingMode;
    }

    /// Sets the formatting mode.
    ///
    /// @param mode the formatting mode
    public void setFormattingMode(TextFormattingMode mode) {
        this.formattingMode = mode;
    }
}
