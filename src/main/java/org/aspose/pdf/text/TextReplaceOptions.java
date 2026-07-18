package org.aspose.pdf.text;

/// Options for text replacement operations in PDF documents.
///
/// @see TextFragmentAbsorber
public class TextReplaceOptions {

    /// Controls how the page content is adjusted after text replacement.
    public enum ReplaceAdjustment {
        /// No adjustment — replaced text may overlap if longer.
        None,
        /// Adjust word spacing to fit replacement within the same space.
        AdjustSpaceWidth,
        /// Shift the rest of the line to accommodate longer/shorter replacement.
        ShiftRestOfLine,
        /// Hyphenate words and adjust whole paragraphs.
        WholeWordsHyphenation
    }

    /// Controls the scope of text replacement.
    public enum Scope {
        /// Replace first occurrence only.
        REPLACE_FIRST,
        /// Replace all occurrences.
        REPLACE_ALL
    }

    private ReplaceAdjustment replaceAdjustmentAction = ReplaceAdjustment.None;
    private Scope replaceScope = Scope.REPLACE_ALL;
    private boolean ignoreParagraphs;

    /// Creates TextReplaceOptions with default settings.
    public TextReplaceOptions() {
    }

    /// Creates TextReplaceOptions with the specified adjustment action.
    ///
    /// @param adjustment the replace adjustment action
    public TextReplaceOptions(ReplaceAdjustment adjustment) {
        this.replaceAdjustmentAction = adjustment;
    }

    /// Creates TextReplaceOptions with the specified scope.
    ///
    /// @param scope the replace scope
    public TextReplaceOptions(Scope scope) {
        this.replaceScope = scope;
    }

    /// Returns the replace adjustment action.
    ///
    /// @return the adjustment action
    public ReplaceAdjustment getReplaceAdjustmentAction() {
        return replaceAdjustmentAction;
    }

    /// Sets the replace adjustment action.
    ///
    /// @param action the adjustment action
    public void setReplaceAdjustmentAction(ReplaceAdjustment action) {
        this.replaceAdjustmentAction = action;
    }

    /// Returns the replace scope.
    ///
    /// @return the scope
    public Scope getReplaceScope() {
        return replaceScope;
    }

    /// Sets the replace scope.
    ///
    /// @param scope the scope
    public void setReplaceScope(Scope scope) {
        this.replaceScope = scope;
    }

    /// Returns whether paragraph boundaries should be ignored during replacement.
    ///
    /// @return `true` if paragraph boundaries should be ignored
    public boolean isIgnoreParagraphs() {
        return ignoreParagraphs;
    }

    /// Sets whether paragraph boundaries should be ignored during replacement.
    ///
    /// @param ignoreParagraphs`true` to ignore paragraph boundaries
    public void setIgnoreParagraphs(boolean ignoreParagraphs) {
        this.ignoreParagraphs = ignoreParagraphs;
    }
}
