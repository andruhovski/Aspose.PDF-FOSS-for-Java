package org.aspose.pdf.facades;

import org.aspose.pdf.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents formatted text with font, color, and encoding properties,
 * used primarily for stamps in the facades API.
 * <p>
 * This class stores one or more lines of text along with formatting metadata
 * such as foreground/background color, font name, font size, encoding type,
 * and whether the font should be embedded. It is typically passed to
 * {@link org.aspose.pdf.TextStamp} or {@link Stamp} to create
 * visually styled text stamps on PDF pages.
 * </p>
 */
public class FormattedText {

    private static final Logger LOG = Logger.getLogger(FormattedText.class.getName());

    private final List<String> lines = new ArrayList<>();
    private Color foregroundColor;
    private Color backgroundColor;
    private String fontName;
    private float fontSize;
    private boolean embedded;
    private FontStyle fontStyle;
    private EncodingType encoding;
    private float lineSpacing;

    /**
     * Creates an empty {@code FormattedText} instance.
     */
    public FormattedText() {
    }

    /**
     * Creates a {@code FormattedText} with the given text.
     *
     * @param text the text content
     */
    public FormattedText(String text) {
        if (text != null) {
            lines.add(text);
        }
    }

    /**
     * Creates a {@code FormattedText} with foreground and background colors.
     *
     * @param text            the text content
     * @param foregroundColor the foreground (text) color
     * @param backgroundColor the background color
     */
    public FormattedText(String text, Color foregroundColor, Color backgroundColor) {
        if (text != null) {
            lines.add(text);
        }
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    /**
     * Creates a {@code FormattedText} with color, font style, encoding, and size.
     *
     * @param text      the text content
     * @param color     the foreground color
     * @param fontStyle the predefined font style
     * @param encoding  the encoding type
     * @param embedded  whether the font should be embedded
     * @param fontSize  the font size in points
     */
    public FormattedText(String text, Color color, FontStyle fontStyle,
                         EncodingType encoding, boolean embedded, float fontSize) {
        if (text != null) {
            lines.add(text);
        }
        this.foregroundColor = color;
        this.fontStyle = fontStyle;
        this.encoding = encoding;
        this.embedded = embedded;
        this.fontSize = fontSize;
        this.fontName = fontStyleToName(fontStyle);
    }

    /**
     * Creates a {@code FormattedText} with color, font name, encoding, and size.
     *
     * @param text     the text content
     * @param color    the foreground color
     * @param fontName the font name (e.g., "Arial", "Helvetica")
     * @param encoding the encoding type
     * @param embedded whether the font should be embedded
     * @param fontSize the font size in points
     */
    public FormattedText(String text, Color color, String fontName,
                         EncodingType encoding, boolean embedded, float fontSize) {
        if (text != null) {
            lines.add(text);
        }
        this.foregroundColor = color;
        this.fontName = fontName;
        this.encoding = encoding;
        this.embedded = embedded;
        this.fontSize = fontSize;
    }

    /**
     * Creates a {@code FormattedText} with foreground/background colors, font style, encoding, and size.
     *
     * @param text            the text content
     * @param foregroundColor the foreground color
     * @param backgroundColor the background color
     * @param fontStyle       the predefined font style
     * @param encoding        the encoding type
     * @param embedded        whether the font should be embedded
     * @param fontSize        the font size in points
     */
    public FormattedText(String text, Color foregroundColor, Color backgroundColor,
                         FontStyle fontStyle, EncodingType encoding,
                         boolean embedded, float fontSize) {
        if (text != null) {
            lines.add(text);
        }
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.fontStyle = fontStyle;
        this.encoding = encoding;
        this.embedded = embedded;
        this.fontSize = fontSize;
        this.fontName = fontStyleToName(fontStyle);
    }

    /**
     * Creates a {@code FormattedText} with foreground/background colors, font name, encoding, and size.
     *
     * @param text            the text content
     * @param foregroundColor the foreground color
     * @param backgroundColor the background color
     * @param fontName        the font name (e.g., "Arial", "Helvetica")
     * @param encoding        the encoding type
     * @param embedded        whether the font should be embedded
     * @param fontSize        the font size in points
     */
    public FormattedText(String text, Color foregroundColor, Color backgroundColor,
                         String fontName, EncodingType encoding,
                         boolean embedded, float fontSize) {
        if (text != null) {
            lines.add(text);
        }
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.fontName = fontName;
        this.encoding = encoding;
        this.embedded = embedded;
        this.fontSize = fontSize;
    }

    /**
     * Creates a {@code FormattedText} with color, font style, encoding, size, and line spacing.
     *
     * @param text        the text content
     * @param color       the foreground color
     * @param fontStyle   the predefined font style
     * @param encoding    the encoding type
     * @param embedded    whether the font should be embedded
     * @param fontSize    the font size in points
     * @param lineSpacing the line spacing in points
     */
    public FormattedText(String text, Color color, FontStyle fontStyle,
                         EncodingType encoding, boolean embedded,
                         float fontSize, float lineSpacing) {
        this(text, color, fontStyle, encoding, embedded, fontSize);
        this.lineSpacing = lineSpacing;
    }

    /**
     * Creates a {@code FormattedText} with foreground/background colors, font style, encoding, size, and line spacing.
     *
     * @param text            the text content
     * @param foregroundColor the foreground color
     * @param backgroundColor the background color
     * @param fontStyle       the predefined font style
     * @param encoding        the encoding type
     * @param embedded        whether the font should be embedded
     * @param fontSize        the font size in points
     * @param lineSpacing     the line spacing in points
     */
    public FormattedText(String text, Color foregroundColor, Color backgroundColor,
                         FontStyle fontStyle, EncodingType encoding,
                         boolean embedded, float fontSize, float lineSpacing) {
        this(text, foregroundColor, backgroundColor, fontStyle, encoding, embedded, fontSize);
        this.lineSpacing = lineSpacing;
    }

    /**
     * Adds a new line of text.
     *
     * @param text the text to add as a new line
     */
    public void addNewLineText(String text) {
        if (text != null) {
            lines.add(text);
        }
    }

    /**
     * Adds a new line of text with custom line spacing.
     *
     * @param text        the text to add as a new line
     * @param lineSpacing the line spacing for this line in points
     */
    public void addNewLineText(String text, float lineSpacing) {
        if (text != null) {
            lines.add(text);
        }
        this.lineSpacing = lineSpacing;
    }

    /**
     * Returns the first line of text, or an empty string if no text has been set.
     *
     * @return the first line of text
     */
    public String getFirstLine() {
        return lines.isEmpty() ? "" : lines.get(0);
    }

    /**
     * Returns all lines of text joined by newline characters.
     *
     * @return the complete text content
     */
    public String getText() {
        return String.join("\n", lines);
    }

    /**
     * Returns the foreground (text) color.
     *
     * @return the text color, or {@code null} if not set
     */
    public Color getTextColor() {
        return foregroundColor;
    }

    /**
     * Returns the background color.
     *
     * @return the background color, or {@code null} if not set
     */
    public Color getBackColor() {
        return backgroundColor;
    }

    /**
     * Returns the font name.
     *
     * @return the font name, or {@code null} if not set
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Returns the font size in points.
     *
     * @return the font size
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * Returns whether the font should be embedded in the PDF.
     *
     * @return {@code true} if the font should be embedded
     */
    public boolean isEmbedded() {
        return embedded;
    }

    /**
     * Returns the predefined font style.
     *
     * @return the font style, or {@code null} if a custom font name was used
     */
    public FontStyle getFontStyle() {
        return fontStyle;
    }

    /**
     * Returns the encoding type.
     *
     * @return the encoding type, or {@code null} if not set
     */
    public EncodingType getEncoding() {
        return encoding;
    }

    /**
     * Returns the line spacing in points.
     *
     * @return the line spacing
     */
    public float getLineSpacing() {
        return lineSpacing;
    }

    /**
     * Converts a {@link FontStyle} enum value to the corresponding PDF font name.
     *
     * @param style the font style
     * @return the PDF font name, or "Helvetica" if style is {@code null}
     */
    private static String fontStyleToName(FontStyle style) {
        if (style == null) return "Helvetica";
        switch (style) {
            case Helvetica: return "Helvetica";
            case Courier: return "Courier";
            case CourierBold: return "Courier-Bold";
            case CourierOblique: return "Courier-Oblique";
            case CourierBoldOblique: return "Courier-BoldOblique";
            case TimesRoman: return "Times-Roman";
            case TimesBold: return "Times-Bold";
            case TimesItalic: return "Times-Italic";
            case TimesBoldItalic: return "Times-BoldItalic";
            case Symbol: return "Symbol";
            case ZapfDingbats: return "ZapfDingbats";
            case CjkFont: return "HeiseiMin-W3";
            default: return "Helvetica";
        }
    }

    @Override
    public String toString() {
        return "FormattedText{text='" + getText() + "', font='" + fontName
                + "', size=" + fontSize + "}";
    }
}
