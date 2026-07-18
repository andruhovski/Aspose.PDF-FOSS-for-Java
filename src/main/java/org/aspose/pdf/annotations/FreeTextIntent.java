package org.aspose.pdf.annotations;

/// Intent of a [FreeTextAnnotation] (ISO 32000-1:2008, §12.5.6.6).
///
/// The `/IT` entry on a free-text annotation describes how a reader
/// should interpret and render the annotation:
///
///   - [#FreeText] — plain free text, no extra geometry (default).
///   - [#FreeTextCallout] — text with a callout line connecting it to
///     a target point on the page (see [FreeTextAnnotation#getCallout()]).
///   - [#FreeTextTypeWriter] — typewriter-style text. No callout.
///
/// [#Undefined] is returned when `/IT` is absent or holds an
/// unrecognised value.
public enum FreeTextIntent {

    Undefined,
    FreeText,
    FreeTextCallout,
    FreeTextTypeWriter;

    /// Returns the PDF name token (without leading slash) for this intent,
    /// or `null` for [#Undefined].
    ///
    /// @return the PDF name, or null
    public String toPdfName() {
        switch (this) {
            case FreeText: return "FreeText";
            case FreeTextCallout: return "FreeTextCallout";
            case FreeTextTypeWriter: return "FreeTextTypeWriter";
            default: return null;
        }
    }

    /// Parses a PDF name token into an enum value. Unknown names map to
    /// [#Undefined].
    ///
    /// @param name the PDF name (without leading slash)
    /// @return the matching enum, or [#Undefined]
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
