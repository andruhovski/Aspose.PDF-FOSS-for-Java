package org.aspose.pdf.text;

/// Represents a font substitution strategy that uses system fonts.
///
/// This strategy searches for suitable replacement fonts among the fonts
/// installed on the system, filtered by the specified font categories.
///
public class SystemFontsSubstitution extends FontSubstitution {

    private int fontCategories;
    private Font defaultFont;

    /// Creates a new system font substitution strategy with the specified categories.
    ///
    /// @param fontCategories bitwise combination of [SubstitutionFontCategories] constants
    public SystemFontsSubstitution(int fontCategories) {
        this.fontCategories = fontCategories;
    }

    /// Gets the font categories used for substitution.
    ///
    /// @return the font categories
    public int getFontCategories() {
        return fontCategories;
    }

    /// Sets the font categories used for substitution.
    ///
    /// @param fontCategories bitwise combination of [SubstitutionFontCategories] constants
    public void setFontCategories(int fontCategories) {
        this.fontCategories = fontCategories;
    }

    /// Gets the default font used when no suitable substitution is found.
    ///
    /// @return the default font, or `null` if not set
    public Font getDefaultFont() {
        return defaultFont;
    }

    /// Sets the default font used when no suitable substitution is found.
    ///
    /// @param defaultFont the default font
    public void setDefaultFont(Font defaultFont) {
        this.defaultFont = defaultFont;
    }
}
