package org.aspose.pdf.text;

/// Options for text formatting within paragraphs.
///
/// Controls indentation, line spacing, and word wrapping behavior
/// when text is laid out on a page.
///
public class TextFormattingOptions {

    /// Line spacing mode for paragraph layout.
    public enum LineSpacingMode {
        /// Line spacing based on font size.
        FontSize,
        /// Full line spacing (includes ascent and descent).
        FullSize,
        /// Proportional line spacing.
        Proportional
    }

    /// Word wrapping mode for paragraph layout.
    public enum WordWrapMode {
        /// No wrapping — text continues on a single line until an explicit break.
        NoWrap,
        /// Wrap at word boundaries.
        ByWords,
        /// Wrap at character boundaries.
        ByCharacter,
        /// Wrap at discretionary hyphenation points (allows splitting words with a
        /// trailing hyphen mark when a soft hyphen or hyphenation rule applies).
        DiscretionaryHyphenation,
        /// No specific wrapping.
        Undefined
    }

    private double subsequentLinesIndent;
    private double firstLineIndent;
    private LineSpacingMode lineSpacing = LineSpacingMode.FontSize;
    private WordWrapMode wrapMode = WordWrapMode.ByWords;

    /// Creates TextFormattingOptions with default settings.
    public TextFormattingOptions() {
    }

    /// Creates TextFormattingOptions with the specified wrap mode.
    ///
    /// @param wrapMode the word wrap mode
    public TextFormattingOptions(WordWrapMode wrapMode) {
        this.wrapMode = wrapMode;
    }

    /// Returns the indent for subsequent lines (all lines except the first).
    ///
    /// @return the subsequent lines indent in points
    public double getSubsequentLinesIndent() {
        return subsequentLinesIndent;
    }

    /// Sets the indent for subsequent lines.
    ///
    /// @param indent the indent in points
    public void setSubsequentLinesIndent(double indent) {
        this.subsequentLinesIndent = indent;
    }

    /// Returns the first line indent.
    ///
    /// @return the first line indent in points
    public double getFirstLineIndent() {
        return firstLineIndent;
    }

    /// Sets the first line indent.
    ///
    /// @param indent the indent in points
    public void setFirstLineIndent(double indent) {
        this.firstLineIndent = indent;
    }

    /// Returns the line spacing mode.
    ///
    /// @return the line spacing mode
    public LineSpacingMode getLineSpacing() {
        return lineSpacing;
    }

    /// Sets the line spacing mode.
    ///
    /// @param mode the line spacing mode
    public void setLineSpacing(LineSpacingMode mode) {
        this.lineSpacing = mode;
    }

    /// Returns the word wrap mode.
    ///
    /// @return the wrap mode
    public WordWrapMode getWrapMode() {
        return wrapMode;
    }

    /// Sets the word wrap mode.
    ///
    /// @param mode the wrap mode
    public void setWrapMode(WordWrapMode mode) {
        this.wrapMode = mode;
    }
}
