package org.aspose.pdf.text;

/**
 * Defines font categories for system font substitution.
 * <p>
 * These constants can be combined using bitwise OR to specify
 * which categories of system fonts should be considered for substitution.
 * </p>
 */
public final class SubstitutionFontCategories {

    /** No font categories. */
    public static final int None = 0;

    /** Use the default system font. */
    public static final int TheSameNamedEmbeddedFonts = 1;

    /** Use system fonts with the same family name. */
    public static final int TheSameNamedSystemFonts = 2;

    /** Use all available system fonts for substitution. */
    public static final int All = TheSameNamedEmbeddedFonts | TheSameNamedSystemFonts;

    private SubstitutionFontCategories() {
        // Utility class — no instantiation
    }
}
