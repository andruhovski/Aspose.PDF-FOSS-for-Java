package org.aspose.pdf.annotations;

/**
 * Intent of a {@link FreeTextAnnotation} (ISO 32000-1:2008, §12.5.6.6).
 *
 * <p>The {@code /IT} entry on a free-text annotation describes how a reader
 * should interpret and render the annotation:</p>
 * <ul>
 *   <li>{@link #FreeText} — plain free text, no extra geometry (default).</li>
 *   <li>{@link #FreeTextCallout} — text with a callout line connecting it to
 *       a target point on the page (see {@link FreeTextAnnotation#getCallout()}).</li>
 *   <li>{@link #FreeTextTypeWriter} — typewriter-style text. No callout.</li>
 * </ul>
 *
 * <p>{@link #Undefined} is returned when {@code /IT} is absent or holds an
 * unrecognised value.</p>
 */
public enum FreeTextIntent {

    Undefined,
    FreeText,
    FreeTextCallout,
    FreeTextTypeWriter;

    /**
     * Returns the PDF name token (without leading slash) for this intent,
     * or {@code null} for {@link #Undefined}.
     *
     * @return the PDF name, or null
     */
    public String toPdfName() {
        switch (this) {
            case FreeText: return "FreeText";
            case FreeTextCallout: return "FreeTextCallout";
            case FreeTextTypeWriter: return "FreeTextTypeWriter";
            default: return null;
        }
    }

    /**
     * Parses a PDF name token into an enum value. Unknown names map to
     * {@link #Undefined}.
     *
     * @param name the PDF name (without leading slash)
     * @return the matching enum, or {@link #Undefined}
     */
    public static FreeTextIntent fromPdfName(String name) {
        if (name == null) return Undefined;
        switch (name) {
            case "FreeText": return FreeText;
            case "FreeTextCallout": return FreeTextCallout;
            case "FreeTextTypeWriter": return FreeTextTypeWriter;
            default: return Undefined;
        }
    }
}
