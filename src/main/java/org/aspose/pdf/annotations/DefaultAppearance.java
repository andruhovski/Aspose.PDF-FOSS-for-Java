package org.aspose.pdf.annotations;

import org.aspose.pdf.Color;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Represents the default appearance string (/DA) for form fields and free text annotations
 * (ISO 32000-1:2008, Section 12.7.3.3).
 * <p>
 * The default appearance string contains operators that set the text color and font,
 * e.g. {@code "0 0 0 rg /Helv 12 Tf"}.
 * </p>
 */
public class DefaultAppearance {

    private static final Logger LOG = Logger.getLogger(DefaultAppearance.class.getName());

    private String fontName;
    private double fontSize;
    private Color textColor;

    /**
     * Creates a DefaultAppearance with default values (Helvetica, 12pt, black).
     */
    public DefaultAppearance() {
        this.fontName = "Helvetica";
        this.fontSize = 12;
        this.textColor = Color.BLACK;
    }

    /**
     * Creates a DefaultAppearance with the specified font, size, and color.
     *
     * @param fontName the font name (e.g. "Helvetica", "Courier")
     * @param fontSize the font size in points
     * @param color    the text color
     */
    public DefaultAppearance(String fontName, double fontSize, Color color) {
        this.fontName = fontName != null ? fontName : "Helvetica";
        this.fontSize = fontSize;
        this.textColor = color != null ? color : Color.BLACK;
    }

    /**
     * Returns the font name.
     *
     * @return the font name
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Sets the font name.
     *
     * @param fontName the font name
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    /**
     * Returns the font size in points.
     *
     * @return the font size
     */
    public double getFontSize() {
        return fontSize;
    }

    /**
     * Sets the font size in points.
     *
     * @param fontSize the font size
     */
    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Returns the text color.
     *
     * @return the text color
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color.
     *
     * @param textColor the text color
     */
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    /**
     * Returns the default appearance string in PDF operator format.
     * <p>
     * The format is: {@code "r g b rg /FontName fontSize Tf"} for RGB colors,
     * or {@code "gray g /FontName fontSize Tf"} for grayscale colors.
     * </p>
     *
     * @return the DA string
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        if (textColor != null) {
            double[] components = textColor.getComponents();
            if (textColor.getColorSpace() == Color.ColorSpace.GRAY) {
                sb.append(formatNumber(components[0])).append(" g ");
            } else if (textColor.getColorSpace() == Color.ColorSpace.CMYK) {
                sb.append(formatNumber(components[0])).append(' ')
                  .append(formatNumber(components[1])).append(' ')
                  .append(formatNumber(components[2])).append(' ')
                  .append(formatNumber(components[3])).append(" k ");
            } else {
                // RGB
                sb.append(formatNumber(textColor.getR())).append(' ')
                  .append(formatNumber(textColor.getG())).append(' ')
                  .append(formatNumber(textColor.getB())).append(" rg ");
            }
        }
        // Font resource name: use a short name derived from font name
        String resourceName = toResourceName(fontName);
        sb.append('/').append(resourceName).append(' ');
        sb.append(formatNumber(fontSize)).append(" Tf");
        return sb.toString();
    }

    /**
     * Generates the DA string in PDF operator format.
     * <p>
     * Equivalent to {@link #getText()}.
     * </p>
     *
     * @return the DA string
     */
    public String toAppearanceString() {
        return getText();
    }

    /**
     * Parses a default appearance string (/DA) into a {@link DefaultAppearance} object.
     * <p>
     * Recognizes patterns like {@code "r g b rg /FontName fontSize Tf"}
     * and {@code "gray g /FontName fontSize Tf"}.
     * </p>
     *
     * @param da the DA string to parse
     * @return a new DefaultAppearance parsed from the string, or a default instance if parsing fails
     */
    public static DefaultAppearance fromString(String da) {
        if (da == null || da.trim().isEmpty()) {
            return new DefaultAppearance();
        }
        String fontName = "Helvetica";
        double fontSize = 12;
        Color textColor = Color.BLACK;

        try {
            String[] tokens = da.trim().split("\\s+");
            // Parse font: look for /Name followed by number and Tf
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].startsWith("/") && i + 2 < tokens.length && "Tf".equals(tokens[i + 2])) {
                    String resourceName = tokens[i].substring(1);
                    fontName = fromResourceName(resourceName);
                    fontSize = Double.parseDouble(tokens[i + 1]);
                    break;
                }
            }
            // Parse color: look for "rg" (RGB), "g" (gray), or "k" (CMYK) operators
            for (int i = 0; i < tokens.length; i++) {
                if ("rg".equals(tokens[i]) && i >= 3) {
                    double r = Double.parseDouble(tokens[i - 3]);
                    double g = Double.parseDouble(tokens[i - 2]);
                    double b = Double.parseDouble(tokens[i - 1]);
                    textColor = Color.fromRgb(r, g, b);
                    break;
                } else if ("g".equals(tokens[i]) && i >= 1 && !tokens[i - 1].startsWith("/")) {
                    try {
                        double gray = Double.parseDouble(tokens[i - 1]);
                        textColor = Color.fromGray(gray);
                        break;
                    } catch (NumberFormatException ignored) {
                        // not a gray value, skip
                    }
                } else if ("k".equals(tokens[i]) && i >= 4) {
                    double c = Double.parseDouble(tokens[i - 4]);
                    double m = Double.parseDouble(tokens[i - 3]);
                    double y = Double.parseDouble(tokens[i - 2]);
                    double k = Double.parseDouble(tokens[i - 1]);
                    textColor = Color.fromCmyk(c, m, y, k);
                    break;
                }
            }
        } catch (Exception e) {
            LOG.fine("Failed to parse DA string '" + da + "': " + e.getMessage());
        }

        return new DefaultAppearance(fontName, fontSize, textColor);
    }

    /**
     * Converts a PDF resource name back to a standard font name.
     */
    private static String fromResourceName(String resourceName) {
        if (resourceName == null) return "Helvetica";
        switch (resourceName) {
            case "Helv": return "Helvetica";
            case "HeBo": return "Helvetica-Bold";
            case "Cour": return "Courier";
            case "TiRo": return "Times-Roman";
            case "TiBo": return "Times-Bold";
            case "Symb": return "Symbol";
            case "ZaDb": return "ZapfDingbats";
            default: return resourceName;
        }
    }

    /**
     * Returns the DA string (same as {@link #getText()}).
     *
     * @return the DA string
     */
    @Override
    public String toString() {
        return getText();
    }

    /**
     * Converts a font name to a typical PDF resource name.
     * Common fonts get short names; others use the name as-is.
     */
    private static String toResourceName(String font) {
        if (font == null) return "Helv";
        switch (font) {
            case "Helvetica": return "Helv";
            case "Helvetica-Bold": return "HeBo";
            case "Courier": return "Cour";
            case "Times-Roman": return "TiRo";
            case "Times-Bold": return "TiBo";
            case "Symbol": return "Symb";
            case "ZapfDingbats": return "ZaDb";
            default: return font;
        }
    }

    private static String formatNumber(double v) {
        if (v == (long) v) {
            return String.valueOf((long) v);
        }
        return String.format(Locale.US, "%.4g", v);
    }
}
