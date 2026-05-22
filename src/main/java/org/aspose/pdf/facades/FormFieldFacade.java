package org.aspose.pdf.facades;

import org.aspose.pdf.Color;

/**
 * Visual-style facade applied to fields created via
 * {@link FormEditor#addField(FieldType, String, String, int, double, double, double, double)}.
 * Mirrors {@code Aspose.Pdf.Facades.FormFieldFacade}: collects font, colour and
 * border attributes that the editor consults when constructing a new widget
 * annotation, so callers can prepare a "field skin" once and reuse it across
 * multiple {@code addField} calls.
 */
public class FormFieldFacade {

    /** Solid border style — single line of constant width. */
    public static final int BorderStyleSolid = 0;
    /** Dashed border. */
    public static final int BorderStyleDashed = 1;
    /** Beveled border (3D raised look). */
    public static final int BorderStyleBeveled = 2;
    /** Inset border (3D pressed look). */
    public static final int BorderStyleInset = 3;
    /** Underline-only border. */
    public static final int BorderStyleUnderline = 4;

    private Color backgroundColor;
    private Color borderColor;
    private int borderStyle = BorderStyleSolid;
    private double borderWidth;
    private String fontName;
    private double fontSize;
    private Color textColor;
    private String alignment;

    /** Creates an empty facade. */
    public FormFieldFacade() {
    }

    /** Returns the field background colour, or {@code null} when unset. */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /** Sets the field background colour. */
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    /**
     * Mirrors the C# property name {@code BackgroudColor} (note the typo in the
     * original .NET API — Aspose preserves it for backward compatibility).
     * Delegates to {@link #setBackgroundColor(Color)}.
     */
    public void setBackgroudColor(Color color) {
        setBackgroundColor(color);
    }

    /** Returns the field border colour, or {@code null} when unset. */
    public Color getBorderColor() {
        return borderColor;
    }

    /** Sets the field border colour. */
    public void setBorderColor(Color color) {
        this.borderColor = color;
    }

    /** Returns the border style — one of {@code BorderStyle*} constants. */
    public int getBorderStyle() {
        return borderStyle;
    }

    /** Sets the border style — one of {@code BorderStyle*} constants. */
    public void setBorderStyle(int style) {
        this.borderStyle = style;
    }

    /** Returns the border width. */
    public double getBorderWidth() {
        return borderWidth;
    }

    /** Sets the border width. */
    public void setBorderWidth(double width) {
        this.borderWidth = width;
    }

    /** Returns the font name (e.g. "Helv"). */
    public String getFontName() {
        return fontName;
    }

    /** Sets the font name. */
    public void setFontName(String name) {
        this.fontName = name;
    }

    /** Returns the font size. */
    public double getFontSize() {
        return fontSize;
    }

    /** Sets the font size. */
    public void setFontSize(double size) {
        this.fontSize = size;
    }

    /** Returns the text colour. */
    public Color getTextColor() {
        return textColor;
    }

    /** Sets the text colour. */
    public void setTextColor(Color color) {
        this.textColor = color;
    }

    /** Returns the text alignment hint (e.g. "Left", "Center", "Right"). */
    public String getAlignment() {
        return alignment;
    }

    /** Sets the text alignment hint. */
    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }
}
