package org.aspose.pdf.text;

/**
 * Represents text edit options that describe how text-editing operations
 * (font replacement, character substitution, language transformation and
 * underline detection) are performed by {@link TextFragmentAbsorber},
 * {@link TextSegment} and the content-editing facades.
 *
 * <p>API-compatible with {@code Aspose.Pdf.Text.TextEditOptions}. See
 * <a href="https://reference.aspose.com/pdf/net/aspose.pdf.text/texteditoptions/">the
 * Aspose reference</a>.</p>
 */
public class TextEditOptions {

    /**
     * Defines font replacement behavior performed during text-editing operations.
     */
    public enum FontReplace {
        /** No additional changes performed during font replacement. */
        Default,
        /** Fonts that become unused during font replacement are removed from the resulting document. */
        RemoveUnusedFonts
    }

    /**
     * Defines the language transformation mode applied while showing/editing text.
     */
    public enum LanguageTransformation {
        /** Default language transformation is performed. */
        Default,
        /**
         * Language transformation is performed the same way as in a text editor; text
         * appears in the document exactly as written in the source.
         */
        ExactlyAsISee,
        /** Language transformation is not performed. */
        None
    }

    /**
     * Defines the action taken when the current font lacks a character required by an edit.
     */
    public enum NoCharacterAction {
        /** Raises an exception when a font lacks the required character. */
        ThrowException,
        /** Replaces the font with a standard font that contains the required character. */
        UseStandardFont,
        /** Proceeds with text replacement without attempting font substitution. */
        ReplaceAnyway,
        /** Substitutes fonts through a multi-step search of user/system fonts. */
        ReplaceFonts,
        /** Replaces the font with the user-defined {@link #getReplacementFont() replacement font}. */
        UseCustomReplacementFont
    }

    /**
     * Defines how clipping paths are processed for edited text.
     */
    public enum ClippingPathsProcessingMode {
        /** Clipping paths are preserved as-is. */
        Default,
        /** Clipping paths are removed when text is edited. */
        Remove
    }

    private boolean allowLanguageTransformation = true;
    private FontReplace fontReplaceBehavior = FontReplace.Default;
    private LanguageTransformation languageTransformationBehavior = LanguageTransformation.Default;
    private NoCharacterAction noCharacterBehavior = NoCharacterAction.ThrowException;
    private ClippingPathsProcessingMode clippingPathsProcessing = ClippingPathsProcessingMode.Default;
    private Font replacementFont;
    private boolean toAttemptGetUnderlineFromSource;

    /**
     * Creates options with default settings (language transformation allowed,
     * {@link FontReplace#Default}, {@link NoCharacterAction#ThrowException}).
     */
    public TextEditOptions() {
    }

    /**
     * Initializes new options for the specified language transformation permission.
     *
     * @param allowLanguageTransformation {@code true} to allow language transformation
     */
    public TextEditOptions(boolean allowLanguageTransformation) {
        this.allowLanguageTransformation = allowLanguageTransformation;
    }

    /**
     * Initializes new options for the specified font replacement behavior.
     *
     * @param fontReplaceBehavior the font replacement behavior
     */
    public TextEditOptions(FontReplace fontReplaceBehavior) {
        this.fontReplaceBehavior = fontReplaceBehavior != null ? fontReplaceBehavior : FontReplace.Default;
    }

    /**
     * Initializes new options for the specified language transformation behavior.
     *
     * @param languageTransformationBehavior the language transformation behavior
     */
    public TextEditOptions(LanguageTransformation languageTransformationBehavior) {
        this.languageTransformationBehavior =
                languageTransformationBehavior != null ? languageTransformationBehavior : LanguageTransformation.Default;
    }

    /**
     * Initializes new options for the specified no-character behavior.
     *
     * @param noCharacterBehavior the action taken when the font lacks a character
     */
    public TextEditOptions(NoCharacterAction noCharacterBehavior) {
        this.noCharacterBehavior = noCharacterBehavior != null ? noCharacterBehavior : NoCharacterAction.ThrowException;
    }

    /**
     * @return {@code true} if language transformation is allowed (default {@code true})
     */
    public boolean isAllowLanguageTransformation() {
        return allowLanguageTransformation;
    }

    /**
     * @param allowLanguageTransformation {@code true} to allow language transformation
     */
    public void setAllowLanguageTransformation(boolean allowLanguageTransformation) {
        this.allowLanguageTransformation = allowLanguageTransformation;
    }

    /**
     * @return the font replacement behavior
     */
    public FontReplace getFontReplaceBehavior() {
        return fontReplaceBehavior;
    }

    /**
     * @param fontReplaceBehavior the font replacement behavior
     */
    public void setFontReplaceBehavior(FontReplace fontReplaceBehavior) {
        this.fontReplaceBehavior = fontReplaceBehavior != null ? fontReplaceBehavior : FontReplace.Default;
    }

    /**
     * @return the language transformation behavior
     */
    public LanguageTransformation getLanguageTransformationBehavior() {
        return languageTransformationBehavior;
    }

    /**
     * @param languageTransformationBehavior the language transformation behavior
     */
    public void setLanguageTransformationBehavior(LanguageTransformation languageTransformationBehavior) {
        this.languageTransformationBehavior =
                languageTransformationBehavior != null ? languageTransformationBehavior : LanguageTransformation.Default;
    }

    /**
     * @return the action taken when the font lacks a required character
     */
    public NoCharacterAction getNoCharacterBehavior() {
        return noCharacterBehavior;
    }

    /**
     * @param noCharacterBehavior the action taken when the font lacks a required character
     */
    public void setNoCharacterBehavior(NoCharacterAction noCharacterBehavior) {
        this.noCharacterBehavior = noCharacterBehavior != null ? noCharacterBehavior : NoCharacterAction.ThrowException;
    }

    /**
     * @return how clipping paths are processed for edited text
     */
    public ClippingPathsProcessingMode getClippingPathsProcessing() {
        return clippingPathsProcessing;
    }

    /**
     * @param clippingPathsProcessing how clipping paths are processed for edited text
     */
    public void setClippingPathsProcessing(ClippingPathsProcessingMode clippingPathsProcessing) {
        this.clippingPathsProcessing =
                clippingPathsProcessing != null ? clippingPathsProcessing : ClippingPathsProcessingMode.Default;
    }

    /**
     * @return the fallback font used when {@link NoCharacterAction#UseCustomReplacementFont} is active
     */
    public Font getReplacementFont() {
        return replacementFont;
    }

    /**
     * @param replacementFont the fallback font for missing-character substitution
     */
    public void setReplacementFont(Font replacementFont) {
        this.replacementFont = replacementFont;
    }

    /**
     * Controls whether underline information is detected from the source document content.
     *
     * @return {@code true} to attempt reading underline state from source content
     */
    public boolean isToAttemptGetUnderlineFromSource() {
        return toAttemptGetUnderlineFromSource;
    }

    /**
     * @param toAttemptGetUnderlineFromSource {@code true} to attempt reading underline state from source content
     */
    public void setToAttemptGetUnderlineFromSource(boolean toAttemptGetUnderlineFromSource) {
        this.toAttemptGetUnderlineFromSource = toAttemptGetUnderlineFromSource;
    }
}
