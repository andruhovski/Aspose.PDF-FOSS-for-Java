package org.aspose.pdf.engine.font.cff;

/**
 * CFF predefined "Standard Strings" (Adobe Technical Note #5176, Appendix A).
 *
 * <p>SIDs 0..390 are mapped to fixed glyph names by this table; SIDs 391+
 * are custom strings looked up in the font's String INDEX.</p>
 */
final class CFFStandardStrings {

    static final String[] NAMES = {
        ".notdef", "space", "exclam", "quotedbl", "numbersign", "dollar", "percent",
        "ampersand", "quoteright", "parenleft", "parenright", "asterisk", "plus",
        "comma", "hyphen", "period", "slash", "zero", "one", "two", "three", "four",
        "five", "six", "seven", "eight", "nine", "colon", "semicolon", "less",
        "equal", "greater", "question", "at", "A", "B", "C", "D", "E", "F", "G",
        "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "bracketleft", "backslash", "bracketright",
        "asciicircum", "underscore", "quoteleft", "a", "b", "c", "d", "e", "f",
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
        "v", "w", "x", "y", "z", "braceleft", "bar", "braceright", "asciitilde",
        "exclamdown", "cent", "sterling", "fraction", "yen", "florin", "section",
        "currency", "quotesingle", "quotedblleft", "guillemotleft",
        "guilsinglleft", "guilsinglright", "fi", "fl", "endash", "dagger",
        "daggerdbl", "periodcentered", "paragraph", "bullet", "quotesinglbase",
        "quotedblbase", "quotedblright", "guillemotright", "ellipsis", "perthousand",
        "questiondown", "grave", "acute", "circumflex", "tilde", "macron", "breve",
        "dotaccent", "dieresis", "ring", "cedilla", "hungarumlaut", "ogonek",
        "caron", "emdash", "AE", "ordfeminine", "Lslash", "Oslash", "OE",
        "ordmasculine", "ae", "dotlessi", "lslash", "oslash", "oe", "germandbls",
        "onesuperior", "twosuperior", "threesuperior", "minus", "multiply",
        "onesuperior", "twosuperior", "threesuperior", "Amacron", "amacron",
        "Aogonek", "aogonek", "Cacute", "cacute", "Ccaron", "ccaron", "Dcaron",
        "dcaron", "Dcroat", "dcroat", "Delta", "Ecaron", "ecaron", "Eogonek",
        "eogonek", "Emacron", "emacron", "Edotaccent", "edotaccent",
        "Gbreve", "gbreve", "Gcommaaccent", "gcommaaccent", "Hbar", "hbar",
        "Iogonek", "iogonek", "Idotaccent", "Imacron", "imacron", "Kcommaaccent",
        "kcommaaccent", "kgreenlandic", "Lacute", "lacute", "Lcommaaccent",
        "lcommaaccent", "Ldot", "ldot", "Nacute", "nacute", "napostrophe",
        "Ncommaaccent", "ncommaaccent", "Eng", "eng", "Ohungarumlaut",
        "ohungarumlaut", "Omacron", "omacron", "Racute", "racute", "Rcommaaccent",
        "rcommaaccent", "Sacute", "sacute", "Scedilla", "scedilla", "Scommaaccent",
        "scommaaccent", "Tcaron", "tcaron", "Tcommaaccent", "tcommaaccent",
        "Umacron", "umacron", "Uogonek", "uogonek", "Uring", "uring",
        "Uhungarumlaut", "uhungarumlaut", "Ydieresis", "ydieresis", "Zacute",
        "zacute", "Zdotaccent", "zdotaccent", "IJ", "ij", "Eth", "eth", "Thorn",
        "thorn", "lozenge", "Aacute", "aacute", "Acircumflex", "acircumflex",
        "Adieresis", "adieresis", "Agrave", "agrave", "Aring", "aring", "Atilde",
        "atilde", "Ccedilla", "ccedilla", "Eacute", "eacute", "Ecircumflex",
        "ecircumflex", "Edieresis", "edieresis", "Egrave", "egrave", "Iacute",
        "iacute", "Icircumflex", "icircumflex", "Idieresis", "idieresis", "Igrave",
        "igrave", "Ntilde", "ntilde", "Oacute", "oacute", "Ocircumflex",
        "ocircumflex", "Odieresis", "odieresis", "Ograve", "ograve", "Otilde",
        "otilde", "Scaron", "scaron", "Uacute", "uacute", "Ucircumflex",
        "ucircumflex", "Udieresis", "udieresis", "Ugrave", "ugrave", "Yacute",
        "yacute", "Zcaron", "zcaron", "Threesuperior", "Foursuperior",
        "Fivesuperior", "Sixsuperior", "Sevensuperior", "Eightsuperior",
        "Ninesuperior", "zerosuperior", "foursuperior", "fivesuperior",
        "sixsuperior", "sevensuperior", "eightsuperior", "ninesuperior",
        "zeroinferior", "oneinferior", "twoinferior", "threeinferior",
        "fourinferior", "fiveinferior", "sixinferior", "seveninferior",
        "eightinferior", "nineinferior", "centinferior", "dollarinferior",
        "periodinferior", "commainferior",
        "Agravesmall", "Aacutesmall", "Acircumflexsmall", "Atildesmall",
        "Adieresissmall", "Aringsmall", "AEsmall", "Ccedillasmall", "Egravesmall",
        "Eacutesmall", "Ecircumflexsmall", "Edieresissmall", "Igravesmall",
        "Iacutesmall", "Icircumflexsmall", "Idieresissmall", "Ethsmall",
        "Ntildesmall", "Ogravesmall", "Oacutesmall", "Ocircumflexsmall",
        "Otildesmall", "Odieresissmall", "OEsmall", "Oslashsmall", "Ugravesmall",
        "Uacutesmall", "Ucircumflexsmall", "Udieresissmall", "Yacutesmall",
        "Thornsmall", "Ydieresissmall", "001.000", "001.001", "001.002", "001.003",
        "Black", "Bold", "Book", "Light", "Medium", "Regular", "Roman", "Semibold"
    };

    /** Number of glyphs in the predefined ISOAdobe charset (228 glyphs incl. .notdef). */
    static final int ISO_ADOBE_LEN = 229;

    private CFFStandardStrings() {}

    static String lookup(int sid) {
        if (sid >= 0 && sid < NAMES.length) return NAMES[sid];
        return ".notdef";
    }
}
