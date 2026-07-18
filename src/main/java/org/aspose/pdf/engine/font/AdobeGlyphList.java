package org.aspose.pdf.engine.font;

import java.util.HashMap;
import java.util.Map;

/// Static mapping from Adobe glyph names to Unicode codepoints.
///
/// Provides a subset of \~600 entries from Adobe's glyphlist.txt, covering
/// Latin-1, Latin Extended, currency, math symbols, ligatures, and common
/// typographic characters. Used by [FontEncoding] to convert glyph
/// names to Unicode characters during text extraction.
///
/// @see <a href="https://github.com/adobe-type-tools/agl-aglfn">Adobe Glyph List</a>
public final class AdobeGlyphList {

    private static final Map<String, Integer> MAP = new HashMap<>(700);

    static {
        // Basic Latin: uppercase
        MAP.put("A", 0x0041); MAP.put("B", 0x0042); MAP.put("C", 0x0043);
        MAP.put("D", 0x0044); MAP.put("E", 0x0045); MAP.put("F", 0x0046);
        MAP.put("G", 0x0047); MAP.put("H", 0x0048); MAP.put("I", 0x0049);
        MAP.put("J", 0x004A); MAP.put("K", 0x004B); MAP.put("L", 0x004C);
        MAP.put("M", 0x004D); MAP.put("N", 0x004E); MAP.put("O", 0x004F);
        MAP.put("P", 0x0050); MAP.put("Q", 0x0051); MAP.put("R", 0x0052);
        MAP.put("S", 0x0053); MAP.put("T", 0x0054); MAP.put("U", 0x0055);
        MAP.put("V", 0x0056); MAP.put("W", 0x0057); MAP.put("X", 0x0058);
        MAP.put("Y", 0x0059); MAP.put("Z", 0x005A);

        // Basic Latin: lowercase
        MAP.put("a", 0x0061); MAP.put("b", 0x0062); MAP.put("c", 0x0063);
        MAP.put("d", 0x0064); MAP.put("e", 0x0065); MAP.put("f", 0x0066);
        MAP.put("g", 0x0067); MAP.put("h", 0x0068); MAP.put("i", 0x0069);
        MAP.put("j", 0x006A); MAP.put("k", 0x006B); MAP.put("l", 0x006C);
        MAP.put("m", 0x006D); MAP.put("n", 0x006E); MAP.put("o", 0x006F);
        MAP.put("p", 0x0070); MAP.put("q", 0x0071); MAP.put("r", 0x0072);
        MAP.put("s", 0x0073); MAP.put("t", 0x0074); MAP.put("u", 0x0075);
        MAP.put("v", 0x0076); MAP.put("w", 0x0077); MAP.put("x", 0x0078);
        MAP.put("y", 0x0079); MAP.put("z", 0x007A);

        // Digits
        MAP.put("zero", 0x0030); MAP.put("one", 0x0031); MAP.put("two", 0x0032);
        MAP.put("three", 0x0033); MAP.put("four", 0x0034); MAP.put("five", 0x0035);
        MAP.put("six", 0x0036); MAP.put("seven", 0x0037); MAP.put("eight", 0x0038);
        MAP.put("nine", 0x0039);

        // Punctuation and symbols (Basic Latin)
        MAP.put("space", 0x0020);
        MAP.put("exclam", 0x0021);
        MAP.put("quotedbl", 0x0022);
        MAP.put("numbersign", 0x0023);
        MAP.put("dollar", 0x0024);
        MAP.put("percent", 0x0025);
        MAP.put("ampersand", 0x0026);
        MAP.put("quotesingle", 0x0027);
        MAP.put("parenleft", 0x0028);
        MAP.put("parenright", 0x0029);
        MAP.put("asterisk", 0x002A);
        MAP.put("plus", 0x002B);
        MAP.put("comma", 0x002C);
        MAP.put("hyphen", 0x002D);
        MAP.put("period", 0x002E);
        MAP.put("slash", 0x002F);
        MAP.put("colon", 0x003A);
        MAP.put("semicolon", 0x003B);
        MAP.put("less", 0x003C);
        MAP.put("equal", 0x003D);
        MAP.put("greater", 0x003E);
        MAP.put("question", 0x003F);
        MAP.put("at", 0x0040);
        MAP.put("bracketleft", 0x005B);
        MAP.put("backslash", 0x005C);
        MAP.put("bracketright", 0x005D);
        MAP.put("asciicircum", 0x005E);
        MAP.put("underscore", 0x005F);
        MAP.put("grave", 0x0060);
        MAP.put("braceleft", 0x007B);
        MAP.put("bar", 0x007C);
        MAP.put("braceright", 0x007D);
        MAP.put("asciitilde", 0x007E);

        // Latin-1 Supplement: accented uppercase
        MAP.put("Agrave", 0x00C0); MAP.put("Aacute", 0x00C1);
        MAP.put("Acircumflex", 0x00C2); MAP.put("Atilde", 0x00C3);
        MAP.put("Adieresis", 0x00C4); MAP.put("Aring", 0x00C5);
        MAP.put("AE", 0x00C6);
        MAP.put("Ccedilla", 0x00C7);
        MAP.put("Egrave", 0x00C8); MAP.put("Eacute", 0x00C9);
        MAP.put("Ecircumflex", 0x00CA); MAP.put("Edieresis", 0x00CB);
        MAP.put("Igrave", 0x00CC); MAP.put("Iacute", 0x00CD);
        MAP.put("Icircumflex", 0x00CE); MAP.put("Idieresis", 0x00CF);
        MAP.put("Eth", 0x00D0);
        MAP.put("Ntilde", 0x00D1);
        MAP.put("Ograve", 0x00D2); MAP.put("Oacute", 0x00D3);
        MAP.put("Ocircumflex", 0x00D4); MAP.put("Otilde", 0x00D5);
        MAP.put("Odieresis", 0x00D6);
        MAP.put("Oslash", 0x00D8);
        MAP.put("Ugrave", 0x00D9); MAP.put("Uacute", 0x00DA);
        MAP.put("Ucircumflex", 0x00DB); MAP.put("Udieresis", 0x00DC);
        MAP.put("Yacute", 0x00DD);
        MAP.put("Thorn", 0x00DE);

        // Latin-1 Supplement: accented lowercase
        MAP.put("germandbls", 0x00DF);
        MAP.put("agrave", 0x00E0); MAP.put("aacute", 0x00E1);
        MAP.put("acircumflex", 0x00E2); MAP.put("atilde", 0x00E3);
        MAP.put("adieresis", 0x00E4); MAP.put("aring", 0x00E5);
        MAP.put("ae", 0x00E6);
        MAP.put("ccedilla", 0x00E7);
        MAP.put("egrave", 0x00E8); MAP.put("eacute", 0x00E9);
        MAP.put("ecircumflex", 0x00EA); MAP.put("edieresis", 0x00EB);
        MAP.put("igrave", 0x00EC); MAP.put("iacute", 0x00ED);
        MAP.put("icircumflex", 0x00EE); MAP.put("idieresis", 0x00EF);
        MAP.put("eth", 0x00F0);
        MAP.put("ntilde", 0x00F1);
        MAP.put("ograve", 0x00F2); MAP.put("oacute", 0x00F3);
        MAP.put("ocircumflex", 0x00F4); MAP.put("otilde", 0x00F5);
        MAP.put("odieresis", 0x00F6);
        MAP.put("oslash", 0x00F8);
        MAP.put("ugrave", 0x00F9); MAP.put("uacute", 0x00FA);
        MAP.put("ucircumflex", 0x00FB); MAP.put("udieresis", 0x00FC);
        MAP.put("yacute", 0x00FD);
        MAP.put("thorn", 0x00FE);
        MAP.put("ydieresis", 0x00FF);

        // Latin Extended-A
        MAP.put("Amacron", 0x0100); MAP.put("amacron", 0x0101);
        MAP.put("Abreve", 0x0102); MAP.put("abreve", 0x0103);
        MAP.put("Aogonek", 0x0104); MAP.put("aogonek", 0x0105);
        MAP.put("Cacute", 0x0106); MAP.put("cacute", 0x0107);
        MAP.put("Ccircumflex", 0x0108); MAP.put("ccircumflex", 0x0109);
        MAP.put("Cdotaccent", 0x010A); MAP.put("cdotaccent", 0x010B);
        MAP.put("Ccaron", 0x010C); MAP.put("ccaron", 0x010D);
        MAP.put("Dcaron", 0x010E); MAP.put("dcaron", 0x010F);
        MAP.put("Dcroat", 0x0110); MAP.put("dcroat", 0x0111);
        MAP.put("Emacron", 0x0112); MAP.put("emacron", 0x0113);
        MAP.put("Ebreve", 0x0114); MAP.put("ebreve", 0x0115);
        MAP.put("Edotaccent", 0x0116); MAP.put("edotaccent", 0x0117);
        MAP.put("Eogonek", 0x0118); MAP.put("eogonek", 0x0119);
        MAP.put("Ecaron", 0x011A); MAP.put("ecaron", 0x011B);
        MAP.put("Gcircumflex", 0x011C); MAP.put("gcircumflex", 0x011D);
        MAP.put("Gbreve", 0x011E); MAP.put("gbreve", 0x011F);
        MAP.put("Gdotaccent", 0x0120); MAP.put("gdotaccent", 0x0121);
        MAP.put("Gcommaaccent", 0x0122); MAP.put("gcommaaccent", 0x0123);
        MAP.put("Hcircumflex", 0x0124); MAP.put("hcircumflex", 0x0125);
        MAP.put("Hbar", 0x0126); MAP.put("hbar", 0x0127);
        MAP.put("Itilde", 0x0128); MAP.put("itilde", 0x0129);
        MAP.put("Imacron", 0x012A); MAP.put("imacron", 0x012B);
        MAP.put("Ibreve", 0x012C); MAP.put("ibreve", 0x012D);
        MAP.put("Iogonek", 0x012E); MAP.put("iogonek", 0x012F);
        MAP.put("Idotaccent", 0x0130); MAP.put("dotlessi", 0x0131);
        MAP.put("IJ", 0x0132); MAP.put("ij", 0x0133);
        MAP.put("Jcircumflex", 0x0134); MAP.put("jcircumflex", 0x0135);
        MAP.put("Kcommaaccent", 0x0136); MAP.put("kcommaaccent", 0x0137);
        MAP.put("kgreenlandic", 0x0138);
        MAP.put("Lacute", 0x0139); MAP.put("lacute", 0x013A);
        MAP.put("Lcommaaccent", 0x013B); MAP.put("lcommaaccent", 0x013C);
        MAP.put("Lcaron", 0x013D); MAP.put("lcaron", 0x013E);
        MAP.put("Ldot", 0x013F); MAP.put("ldot", 0x0140);
        MAP.put("Lslash", 0x0141); MAP.put("lslash", 0x0142);
        MAP.put("Nacute", 0x0143); MAP.put("nacute", 0x0144);
        MAP.put("Ncommaaccent", 0x0145); MAP.put("ncommaaccent", 0x0146);
        MAP.put("Ncaron", 0x0147); MAP.put("ncaron", 0x0148);
        MAP.put("napostrophe", 0x0149);
        MAP.put("Eng", 0x014A); MAP.put("eng", 0x014B);
        MAP.put("Omacron", 0x014C); MAP.put("omacron", 0x014D);
        MAP.put("Obreve", 0x014E); MAP.put("obreve", 0x014F);
        MAP.put("Ohungarumlaut", 0x0150); MAP.put("ohungarumlaut", 0x0151);
        MAP.put("OE", 0x0152); MAP.put("oe", 0x0153);
        MAP.put("Racute", 0x0154); MAP.put("racute", 0x0155);
        MAP.put("Rcommaaccent", 0x0156); MAP.put("rcommaaccent", 0x0157);
        MAP.put("Rcaron", 0x0158); MAP.put("rcaron", 0x0159);
        MAP.put("Sacute", 0x015A); MAP.put("sacute", 0x015B);
        MAP.put("Scircumflex", 0x015C); MAP.put("scircumflex", 0x015D);
        MAP.put("Scedilla", 0x015E); MAP.put("scedilla", 0x015F);
        MAP.put("Scaron", 0x0160); MAP.put("scaron", 0x0161);
        MAP.put("Tcommaaccent", 0x0162); MAP.put("tcommaaccent", 0x0163);
        MAP.put("Tcaron", 0x0164); MAP.put("tcaron", 0x0165);
        MAP.put("Tbar", 0x0166); MAP.put("tbar", 0x0167);
        MAP.put("Utilde", 0x0168); MAP.put("utilde", 0x0169);
        MAP.put("Umacron", 0x016A); MAP.put("umacron", 0x016B);
        MAP.put("Ubreve", 0x016C); MAP.put("ubreve", 0x016D);
        MAP.put("Uring", 0x016E); MAP.put("uring", 0x016F);
        MAP.put("Uhungarumlaut", 0x0170); MAP.put("uhungarumlaut", 0x0171);
        MAP.put("Uogonek", 0x0172); MAP.put("uogonek", 0x0173);
        MAP.put("Wcircumflex", 0x0174); MAP.put("wcircumflex", 0x0175);
        MAP.put("Ycircumflex", 0x0176); MAP.put("ycircumflex", 0x0177);
        MAP.put("Ydieresis", 0x0178);
        MAP.put("Zacute", 0x0179); MAP.put("zacute", 0x017A);
        MAP.put("Zdotaccent", 0x017B); MAP.put("zdotaccent", 0x017C);
        MAP.put("Zcaron", 0x017D); MAP.put("zcaron", 0x017E);

        // Spacing modifier letters
        MAP.put("circumflex", 0x02C6);
        MAP.put("caron", 0x02C7);
        MAP.put("breve", 0x02D8);
        MAP.put("dotaccent", 0x02D9);
        MAP.put("ring", 0x02DA);
        MAP.put("ogonek", 0x02DB);
        MAP.put("tilde", 0x02DC);
        MAP.put("hungarumlaut", 0x02DD);

        // Greek (commonly needed in math/science PDFs)
        MAP.put("Alpha", 0x0391); MAP.put("Beta", 0x0392);
        MAP.put("Gamma", 0x0393); MAP.put("Delta", 0x0394);
        MAP.put("Epsilon", 0x0395); MAP.put("Zeta", 0x0396);
        MAP.put("Eta", 0x0397); MAP.put("Theta", 0x0398);
        MAP.put("Iota", 0x0399); MAP.put("Kappa", 0x039A);
        MAP.put("Lambda", 0x039B); MAP.put("Mu", 0x039C);
        MAP.put("Nu", 0x039D); MAP.put("Xi", 0x039E);
        MAP.put("Omicron", 0x039F); MAP.put("Pi", 0x03A0);
        MAP.put("Rho", 0x03A1); MAP.put("Sigma", 0x03A3);
        MAP.put("Tau", 0x03A4); MAP.put("Upsilon", 0x03A5);
        MAP.put("Phi", 0x03A6); MAP.put("Chi", 0x03A7);
        MAP.put("Psi", 0x03A8); MAP.put("Omega", 0x03A9);
        MAP.put("alpha", 0x03B1); MAP.put("beta", 0x03B2);
        MAP.put("gamma", 0x03B3); MAP.put("delta", 0x03B4);
        MAP.put("epsilon", 0x03B5); MAP.put("zeta", 0x03B6);
        MAP.put("eta", 0x03B7); MAP.put("theta", 0x03B8);
        MAP.put("iota", 0x03B9); MAP.put("kappa", 0x03BA);
        MAP.put("lambda", 0x03BB); MAP.put("mu", 0x03BC);
        MAP.put("nu", 0x03BD); MAP.put("xi", 0x03BE);
        MAP.put("omicron", 0x03BF); MAP.put("pi", 0x03C0);
        MAP.put("rho", 0x03C1); MAP.put("sigma1", 0x03C2);
        MAP.put("sigma", 0x03C3); MAP.put("tau", 0x03C4);
        MAP.put("upsilon", 0x03C5); MAP.put("phi", 0x03C6);
        MAP.put("chi", 0x03C7); MAP.put("psi", 0x03C8);
        MAP.put("omega", 0x03C9);

        // Latin-1 Supplement: symbols and punctuation
        MAP.put("exclamdown", 0x00A1);
        MAP.put("cent", 0x00A2);
        MAP.put("sterling", 0x00A3);
        MAP.put("currency", 0x00A4);
        MAP.put("yen", 0x00A5);
        MAP.put("brokenbar", 0x00A6);
        MAP.put("section", 0x00A7);
        MAP.put("dieresis", 0x00A8);
        MAP.put("copyright", 0x00A9);
        MAP.put("ordfeminine", 0x00AA);
        MAP.put("guillemotleft", 0x00AB);
        MAP.put("logicalnot", 0x00AC);
        MAP.put("registered", 0x00AE);
        MAP.put("macron", 0x00AF);
        MAP.put("degree", 0x00B0);
        MAP.put("plusminus", 0x00B1);
        MAP.put("twosuperior", 0x00B2);
        MAP.put("threesuperior", 0x00B3);
        MAP.put("acute", 0x00B4);
        MAP.put("paragraph", 0x00B6);
        MAP.put("periodcentered", 0x00B7);
        MAP.put("cedilla", 0x00B8);
        MAP.put("onesuperior", 0x00B9);
        MAP.put("ordmasculine", 0x00BA);
        MAP.put("guillemotright", 0x00BB);
        MAP.put("onequarter", 0x00BC);
        MAP.put("onehalf", 0x00BD);
        MAP.put("threequarters", 0x00BE);
        MAP.put("questiondown", 0x00BF);
        MAP.put("multiply", 0x00D7);
        MAP.put("divide", 0x00F7);

        // General punctuation
        MAP.put("endash", 0x2013);
        MAP.put("emdash", 0x2014);
        MAP.put("quoteleft", 0x2018);
        MAP.put("quoteright", 0x2019);
        MAP.put("quotesinglbase", 0x201A);
        MAP.put("quotedblleft", 0x201C);
        MAP.put("quotedblright", 0x201D);
        MAP.put("quotedblbase", 0x201E);
        MAP.put("dagger", 0x2020);
        MAP.put("daggerdbl", 0x2021);
        MAP.put("bullet", 0x2022);
        MAP.put("ellipsis", 0x2026);
        MAP.put("perthousand", 0x2030);
        MAP.put("guilsinglleft", 0x2039);
        MAP.put("guilsinglright", 0x203A);

        // Currency
        MAP.put("Euro", 0x20AC);

        // Letterlike symbols
        MAP.put("trademark", 0x2122);

        // Mathematical operators
        MAP.put("minus", 0x2212);
        MAP.put("fraction", 0x2044);
        MAP.put("radical", 0x221A);
        MAP.put("infinity", 0x221E);
        MAP.put("integral", 0x222B);
        MAP.put("approxequal", 0x2248);
        MAP.put("notequal", 0x2260);
        MAP.put("lessequal", 0x2264);
        MAP.put("greaterequal", 0x2265);
        MAP.put("partialdiff", 0x2202);
        MAP.put("summation", 0x2211);
        MAP.put("product", 0x220F);
        MAP.put("lozenge", 0x25CA);

        // Ligatures
        MAP.put("fi", 0xFB01);
        MAP.put("fl", 0xFB02);

        // Misc
        MAP.put("florin", 0x0192);
        MAP.put("softhyphen", 0x00AD);
        MAP.put("nbspace", 0x00A0);

        // Additional common glyph names
        MAP.put("mu1", 0x00B5);    // micro sign (alias)
        MAP.put("afii10017", 0x0410); // Cyrillic A
        MAP.put("afii10018", 0x0411); // Cyrillic BE
        MAP.put("afii10019", 0x0412); // Cyrillic VE
        MAP.put("afii10020", 0x0413); // Cyrillic GHE
        MAP.put("afii10021", 0x0414); // Cyrillic DE
        MAP.put("afii10022", 0x0415); // Cyrillic IE
        MAP.put("afii10024", 0x0416); // Cyrillic ZHE
        MAP.put("afii10025", 0x0417); // Cyrillic ZE
        MAP.put("afii10026", 0x0418); // Cyrillic I
        MAP.put("afii10027", 0x0419); // Cyrillic SHORT I
        MAP.put("afii10028", 0x041A); // Cyrillic KA
        MAP.put("afii10029", 0x041B); // Cyrillic EL
        MAP.put("afii10030", 0x041C); // Cyrillic EM
        MAP.put("afii10031", 0x041D); // Cyrillic EN
        MAP.put("afii10032", 0x041E); // Cyrillic O
        MAP.put("afii10033", 0x041F); // Cyrillic PE
        MAP.put("afii10034", 0x0420); // Cyrillic ER
        MAP.put("afii10035", 0x0421); // Cyrillic ES
        MAP.put("afii10036", 0x0422); // Cyrillic TE
        MAP.put("afii10037", 0x0423); // Cyrillic U
        MAP.put("afii10038", 0x0424); // Cyrillic EF
        MAP.put("afii10039", 0x0425); // Cyrillic HA
        MAP.put("afii10040", 0x0426); // Cyrillic TSE
        MAP.put("afii10041", 0x0427); // Cyrillic CHE
        MAP.put("afii10042", 0x0428); // Cyrillic SHA
        MAP.put("afii10043", 0x0429); // Cyrillic SHCHA
        MAP.put("afii10044", 0x042A); // Cyrillic HARD SIGN
        MAP.put("afii10045", 0x042B); // Cyrillic YERU
        MAP.put("afii10046", 0x042C); // Cyrillic SOFT SIGN
        MAP.put("afii10047", 0x042D); // Cyrillic E
        MAP.put("afii10048", 0x042E); // Cyrillic YU
        MAP.put("afii10049", 0x042F); // Cyrillic YA
        MAP.put("afii10065", 0x0430); // Cyrillic a
        MAP.put("afii10066", 0x0431); MAP.put("afii10067", 0x0432);
        MAP.put("afii10068", 0x0433); MAP.put("afii10069", 0x0434);
        MAP.put("afii10070", 0x0435); MAP.put("afii10072", 0x0436);
        MAP.put("afii10073", 0x0437); MAP.put("afii10074", 0x0438);
        MAP.put("afii10075", 0x0439); MAP.put("afii10076", 0x043A);
        MAP.put("afii10077", 0x043B); MAP.put("afii10078", 0x043C);
        MAP.put("afii10079", 0x043D); MAP.put("afii10080", 0x043E);
        MAP.put("afii10081", 0x043F); MAP.put("afii10082", 0x0440);
        MAP.put("afii10083", 0x0441); MAP.put("afii10084", 0x0442);
        MAP.put("afii10085", 0x0443); MAP.put("afii10086", 0x0444);
        MAP.put("afii10087", 0x0445); MAP.put("afii10088", 0x0446);
        MAP.put("afii10089", 0x0447); MAP.put("afii10090", 0x0448);
        MAP.put("afii10091", 0x0449); MAP.put("afii10092", 0x044A);
        MAP.put("afii10093", 0x044B); MAP.put("afii10094", 0x044C);
        MAP.put("afii10095", 0x044D); MAP.put("afii10096", 0x044E);
        MAP.put("afii10097", 0x044F);
        MAP.put("afii10023", 0x0401); // Cyrillic IO
        MAP.put("afii10071", 0x0451); // Cyrillic io

        // Arrows
        MAP.put("arrowleft", 0x2190); MAP.put("arrowup", 0x2191);
        MAP.put("arrowright", 0x2192); MAP.put("arrowdown", 0x2193);
        MAP.put("arrowboth", 0x2194);

        // Geometric shapes
        MAP.put("filledbox", 0x25A0);
        MAP.put("H22073", 0x25A1); // white square
        MAP.put("filledrect", 0x25AC);
        MAP.put("triagup", 0x25B2);
        MAP.put("triagdn", 0x25BC);
        MAP.put("circle", 0x25CB);
        MAP.put("H18533", 0x25CF); // black circle

        // Misc symbols
        MAP.put("heart", 0x2665);
        MAP.put("diamond", 0x2666);
        MAP.put("club", 0x2663);
        MAP.put("spade", 0x2660);

        // Additional names used in WinAnsiEncoding
        MAP.put("commaaccent", 0x0326);
        MAP.put("Scommaaccent", 0x0218);
        MAP.put("scommaaccent", 0x0219);
        MAP.put("Tcommaaccent", 0x021A);

        // .notdef is special — indicates undefined glyph
        MAP.put(".notdef", -1);
    }

    private AdobeGlyphList() {
        // Utility class — no instantiation
    }

    /// Returns the Unicode codepoint for the given Adobe glyph name.
    ///
    /// @param glyphName the glyph name (e.g., "A", "space", "fi")
    /// @return the Unicode codepoint, or -1 if the glyph name is not found
    public static int getUnicode(String glyphName) {
        if (glyphName == null) {
            return -1;
        }
        // Handle subset/custom glyph names like G69, G120, G147 often found in
        // embedded Type1 subsets without ToUnicode. For low bytes this usually
        // corresponds to WinAnsi/byte values; fall back to the numeric codepoint.
        if (glyphName.length() > 1 && (glyphName.charAt(0) == 'G' || glyphName.charAt(0) == 'g')) {
            boolean digitsOnly = true;
            for (int i = 1; i < glyphName.length(); i++) {
                if (!Character.isDigit(glyphName.charAt(i))) {
                    digitsOnly = false;
                    break;
                }
            }
            if (digitsOnly) {
                try {
                    int code = Integer.parseInt(glyphName.substring(1));
                    if (code >= 0 && code <= 255) {
                        int unicode = FontEncoding.WIN_ANSI.getUnicode(code);
                        if (unicode >= 0 && unicode != code) {
                            return unicode;
                        }
                    }
                    if (code >= 0) {
                        return code;
                    }
                } catch (NumberFormatException e) {
                    // fall through to normal map lookup
                }
            }
        }
        // Handle "uniXXXX" pattern (§ Annex E of Adobe Glyph List spec)
        if (glyphName.startsWith("uni") && glyphName.length() == 7) {
            try {
                return Integer.parseInt(glyphName.substring(3), 16);
            } catch (NumberFormatException e) {
                // fall through to map lookup
            }
        }
        Integer result = MAP.get(glyphName);
        return result != null ? result : -1;
    }

    /// Returns whether the given glyph name is present in the Adobe Glyph List.
    ///
    /// @param glyphName the glyph name to check
    /// @return true if the mapping exists
    public static boolean contains(String glyphName) {
        return glyphName != null && MAP.containsKey(glyphName);
    }
}
