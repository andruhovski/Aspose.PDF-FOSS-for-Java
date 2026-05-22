package org.aspose.pdf.facades;

/**
 * Enumerates the encoding types available for use with {@link FormattedText}.
 * <p>
 * These correspond to the standard PDF font encoding types used in the
 * Aspose.PDF facades API.
 * </p>
 */
public enum EncodingType {

    /** Windows ANSI encoding (WinAnsiEncoding in PDF). */
    Winansi,

    /** Identity-H CMap for CID fonts (horizontal writing). */
    Identity_h,

    /** Windows Central European encoding (Code Page 1250). */
    Cp1250,

    /** Windows Western European encoding (Code Page 1252). */
    Cp1252,

    /** Macintosh Roman encoding. */
    Macroman,

    /** Unicode (UTF-16BE) encoding. */
    Unicode
}
