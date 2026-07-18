package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.Color;
import org.aspose.pdf.MarginInfo;
import org.aspose.pdf.text.Font;
import org.aspose.pdf.text.FontStyles;

import java.util.logging.Logger;

/// Represents text state settings for a structure element in a tagged PDF
/// (ISO 32000-1:2008, §14.8.2.4).
///
/// These properties control the visual presentation of text associated
/// with a structure element. When the document is rendered, the text state
/// is applied to all marked content sequences belonging to the element.
public class StructureTextState {

    private static final Logger LOG = Logger.getLogger(StructureTextState.class.getName());

    private Font font;
    private Float fontSize;
    private int fontStyle = FontStyles.Regular;
    private Color foregroundColor;
    private Color backgroundColor;
    private Float characterSpacing;
    private Float wordSpacing;
    private Float lineSpacing;
    private Float horizontalScaling;
    private boolean strikeOut;
    private boolean underline;
    private boolean subscript;
    private boolean superscript;
    private MarginInfo marginInfo;

    /// Creates a new StructureTextState with default values.
    public StructureTextState() {
    }

    /// Returns the font.
    ///
    /// @return the font, or `null` if not set
    public Font getFont() {
        return font;
    }

    /// Sets the font.
    ///
    /// @param font the font to use
    public void setFont(Font font) {
        this.font = font;
    }

    /// Returns the font size.
    ///
    /// @return the font size, or `null` if not set
    public Float getFontSize() {
        return fontSize;
    }

    /// Sets the font size.
    ///
    /// @param fontSize the font size in points
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    /// Returns the font style flags.
    ///
    /// @return the font style (combination of [FontStyles] flags)
    public int getFontStyle() {
        return fontStyle;
    }

    /// Sets the font style flags.
    ///
    /// @param fontStyle combination of [FontStyles] flags
    public void setFontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
    }

    /// Returns the foreground (text) color.
    ///
    /// @return the foreground color, or `null` if not set
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /// Sets the foreground (text) color.
    ///
    /// @param foregroundColor the color
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    /// Returns the background color.
    ///
    /// @return the background color, or `null` if not set
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /// Sets the background color.
    ///
    /// @param backgroundColor the color
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /// Returns the character spacing.
    ///
    /// @return the character spacing, or `null` if not set
    public Float getCharacterSpacing() {
        return characterSpacing;
    }

    /// Sets the character spacing.
    ///
    /// @param characterSpacing spacing in text space units
    public void setCharacterSpacing(float characterSpacing) {
        this.characterSpacing = characterSpacing;
    }

    /// Returns the word spacing.
    ///
    /// @return the word spacing, or `null` if not set
    public Float getWordSpacing() {
        return wordSpacing;
    }

    /// Sets the word spacing.
    ///
    /// @param wordSpacing spacing in text space units
    public void setWordSpacing(float wordSpacing) {
        this.wordSpacing = wordSpacing;
    }

    /// Returns the line spacing.
    ///
    /// @return the line spacing, or `null` if not set
    public Float getLineSpacing() {
        return lineSpacing;
    }

    /// Sets the line spacing.
    ///
    /// @param lineSpacing the line spacing value
    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    /// Returns the horizontal scaling factor.
    ///
    /// @return the horizontal scaling (100 = normal), or `null` if not set
    public Float getHorizontalScaling() {
        return horizontalScaling;
    }

    /// Sets the horizontal scaling factor.
    ///
    /// @param horizontalScaling the scaling factor (100 = normal)
    public void setHorizontalScaling(float horizontalScaling) {
        this.horizontalScaling = horizontalScaling;
    }

    /// Returns whether strikeout is enabled.
    ///
    /// @return `true` if strikeout is enabled
    public boolean isStrikeOut() {
        return strikeOut;
    }

    /// Sets the strikeout flag.
    ///
    /// @param strikeOut`true` to enable strikeout
    public void setStrikeOut(boolean strikeOut) {
        this.strikeOut = strikeOut;
    }

    /// Returns whether underline is enabled.
    ///
    /// @return `true` if underline is enabled
    public boolean isUnderline() {
        return underline;
    }

    /// Sets the underline flag.
    ///
    /// @param underline`true` to enable underline
    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    /// Returns whether subscript is enabled.
    ///
    /// @return `true` if subscript is enabled
    public boolean isSubscript() {
        return subscript;
    }

    /// Sets the subscript flag.
    ///
    /// @param subscript`true` to enable subscript
    public void setSubscript(boolean subscript) {
        this.subscript = subscript;
    }

    /// Returns whether superscript is enabled.
    ///
    /// @return `true` if superscript is enabled
    public boolean isSuperscript() {
        return superscript;
    }

    /// Sets the superscript flag.
    ///
    /// @param superscript`true` to enable superscript
    public void setSuperscript(boolean superscript) {
        this.superscript = superscript;
    }

    /// Returns the margin info for positioning.
    ///
    /// @return the margin info, or `null` if not set
    public MarginInfo getMarginInfo() {
        return marginInfo;
    }

    /// Sets the margin info for positioning.
    ///
    /// @param marginInfo the margin info
    public void setMarginInfo(MarginInfo marginInfo) {
        this.marginInfo = marginInfo;
    }
}
