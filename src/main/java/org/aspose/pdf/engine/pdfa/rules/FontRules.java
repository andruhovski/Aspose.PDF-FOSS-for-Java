package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Validates font requirements for PDF/A compliance (most critical rule set).
 *
 * <p>Checks the following ISO 19005 clauses:</p>
 * <ul>
 *   <li>6.3.4 &mdash; All fonts must be embedded (FontDescriptor has FontFile/FontFile2/FontFile3)</li>
 *   <li>6.3.5 &mdash; Subset Type1 must have /CharSet; Subset CIDFont must have /CIDSet</li>
 *   <li>6.3.7 &mdash; Non-symbolic TrueType: Encoding must be MacRomanEncoding or WinAnsiEncoding</li>
 *   <li>6.3.8 &mdash; Level A/U: font must have /ToUnicode CMap</li>
 *   <li>6.3.3.2 &mdash; CIDFontType2: must have /CIDToGIDMap</li>
 * </ul>
 */
public final class FontRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(FontRules.class.getName());

    /** Pattern to detect subset font names: 6 uppercase letters followed by '+'. */
    private static final Pattern SUBSET_PREFIX = Pattern.compile("^[A-Z]{6}\\+.+");

    /**
     * Creates a new font rules checker.
     */
    public FontRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfA()) {
            return;
        }
        checkPages(parser, format, result);
    }

    /**
     * Iterates all pages and checks each page's font resources.
     */
    private void checkPages(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        COSDictionary pages = resolveDict(catalog.get("Pages"));
        if (pages == null) {
            return;
        }
        COSArray kids = pages.getArray("Kids");
        if (kids == null) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            COSDictionary page = resolveDict(kids.get(i));
            if (page == null) {
                continue;
            }
            String pagePath = "page[" + i + "]";
            COSDictionary resources = resolveDict(page.get("Resources"));
            if (resources == null) {
                continue;
            }
            checkFonts(resources, pagePath, format, result);
        }
    }

    /**
     * Checks all fonts in a resources dictionary, including fonts in Form XObjects.
     */
    private void checkFonts(COSDictionary resources, String pagePath,
                             PdfFormat format, PdfAValidationResult result) {
        COSDictionary fonts = resolveDict(resources.get("Font"));
        if (fonts != null) {
            for (COSName key : fonts.keySet()) {
                COSDictionary font = resolveDict(fonts.get(key.getName()));
                if (font == null) {
                    continue;
                }
                String fontPath = pagePath + "/Resources/Font/" + key.getName();
                checkSingleFont(font, fontPath, format, result);
            }
        }

        // Also check fonts in Form XObjects (they have their own Resources)
        COSDictionary xobjects = resolveDict(resources.get("XObject"));
        if (xobjects != null) {
            for (COSName key : xobjects.keySet()) {
                COSDictionary xobj = resolveDict(xobjects.get(key.getName()));
                if (xobj == null) {
                    continue;
                }
                String subtype = xobj.getNameAsString("Subtype");
                if ("Form".equals(subtype)) {
                    COSDictionary xobjResources = resolveDict(xobj.get("Resources"));
                    if (xobjResources != null) {
                        String xobjPath = pagePath + "/Resources/XObject/" + key.getName();
                        checkFonts(xobjResources, xobjPath, format, result);
                    }
                }
            }
        }
    }

    /**
     * Validates a single font dictionary.
     */
    private void checkSingleFont(COSDictionary font, String fontPath,
                                  PdfFormat format, PdfAValidationResult result) {
        String subtype = font.getNameAsString("Subtype");
        String baseFont = font.getNameAsString("BaseFont");
        boolean isSubset = baseFont != null && SUBSET_PREFIX.matcher(baseFont).matches();

        // Handle Type0 (composite) fonts
        if ("Type0".equals(subtype)) {
            checkCompositeFont(font, fontPath, format, result);
            return;
        }

        COSDictionary fontDescriptor = resolveDict(font.get("FontDescriptor"));

        // 6.3.4: Font must be embedded (FontDescriptor must exist and have FontFile/2/3)
        if (fontDescriptor == null) {
            // Type3 fonts don't have FontDescriptor — skip embedding check
            if (!"Type3".equals(subtype)) {
                result.addError("6.3.4",
                        "Font is missing FontDescriptor (font not embedded): " + baseFont,
                        fontPath, "6.3.4");
            }
            return;
        }

        boolean hasFile = fontDescriptor.get("FontFile") != null
                || fontDescriptor.get("FontFile2") != null
                || fontDescriptor.get("FontFile3") != null;
        if (!hasFile) {
            result.addError("6.3.4",
                    "Font must be embedded (no FontFile/FontFile2/FontFile3 in FontDescriptor): "
                            + baseFont,
                    fontPath, "6.3.4");
        }

        // 6.3.5: Subset Type1 must have /CharSet in FontDescriptor
        if (isSubset && ("Type1".equals(subtype) || "MMType1".equals(subtype))) {
            if (fontDescriptor.get("CharSet") == null) {
                result.addError("6.3.5",
                        "Subset Type1 font must have /CharSet in FontDescriptor: " + baseFont,
                        fontPath, "6.3.5");
            }
        }

        // 6.3.7: Non-symbolic TrueType encoding
        if ("TrueType".equals(subtype)) {
            checkTrueTypeEncoding(font, fontDescriptor, fontPath, baseFont, result);
        }

        // 6.3.8: Level A or U requires /ToUnicode
        if (format.isLevelA() || format.isLevelU()) {
            checkToUnicode(font, fontPath, baseFont, result);
        }
    }

    /**
     * Checks composite (Type0) fonts and their descendant CIDFonts.
     */
    private void checkCompositeFont(COSDictionary font, String fontPath,
                                     PdfFormat format, PdfAValidationResult result) {
        String baseFont = font.getNameAsString("BaseFont");
        boolean isSubset = baseFont != null && SUBSET_PREFIX.matcher(baseFont).matches();

        COSArray descendants = resolveArray(font.get("DescendantFonts"));
        if (descendants == null || descendants.size() == 0) {
            result.addError("6.3.4",
                    "Type0 font has no DescendantFonts: " + baseFont,
                    fontPath, "6.3.4");
            return;
        }

        COSDictionary cidFont = resolveDict(descendants.get(0));
        if (cidFont == null) {
            return;
        }

        String cidSubtype = cidFont.getNameAsString("Subtype");
        COSDictionary fontDescriptor = resolveDict(cidFont.get("FontDescriptor"));

        // 6.3.4: CIDFont must be embedded
        if (fontDescriptor != null) {
            boolean hasFile = fontDescriptor.get("FontFile") != null
                    || fontDescriptor.get("FontFile2") != null
                    || fontDescriptor.get("FontFile3") != null;
            if (!hasFile) {
                result.addError("6.3.4",
                        "CIDFont must be embedded (no FontFile/FontFile2/FontFile3): " + baseFont,
                        fontPath, "6.3.4");
            }

            // 6.3.5: Subset CIDFont must have /CIDSet
            if (isSubset && fontDescriptor.get("CIDSet") == null) {
                result.addError("6.3.5",
                        "Subset CIDFont must have /CIDSet in FontDescriptor: " + baseFont,
                        fontPath, "6.3.5");
            }
        } else {
            result.addError("6.3.4",
                    "CIDFont is missing FontDescriptor: " + baseFont,
                    fontPath, "6.3.4");
        }

        // 6.3.3.2: CIDFontType2 must have /CIDToGIDMap
        if ("CIDFontType2".equals(cidSubtype)) {
            COSBase cidToGid = cidFont.get("CIDToGIDMap");
            if (cidToGid == null) {
                result.addError("6.3.3.2",
                        "CIDFontType2 must have /CIDToGIDMap: " + baseFont,
                        fontPath, "6.3.3.2");
            }
        }

        // 6.3.8: Level A or U requires /ToUnicode on parent Type0 font
        if (format.isLevelA() || format.isLevelU()) {
            checkToUnicode(font, fontPath, baseFont, result);
        }
    }

    /**
     * 6.3.7: Non-symbolic TrueType font must use MacRomanEncoding or WinAnsiEncoding.
     */
    private void checkTrueTypeEncoding(COSDictionary font, COSDictionary fontDescriptor,
                                        String fontPath, String baseFont,
                                        PdfAValidationResult result) {
        // Check if font is symbolic (bit 3 of /Flags = 0x04)
        int flags = fontDescriptor.getInt("Flags", 0);
        boolean isSymbolic = (flags & 0x04) != 0;
        if (isSymbolic) {
            return; // symbolic fonts can use any encoding
        }

        String encoding = font.getNameAsString("Encoding");
        if (encoding == null) {
            // Check if /Encoding is a dictionary (difference encoding)
            COSBase encRef = font.get("Encoding");
            if (encRef != null) {
                COSDictionary encDict = resolveDict(encRef);
                if (encDict != null) {
                    encoding = encDict.getNameAsString("BaseEncoding");
                }
            }
        }

        if (encoding == null || (!"MacRomanEncoding".equals(encoding)
                && !"WinAnsiEncoding".equals(encoding))) {
            result.addError("6.3.7",
                    "Non-symbolic TrueType font must use MacRomanEncoding or WinAnsiEncoding: "
                            + baseFont + " (found: " + encoding + ")",
                    fontPath, "6.3.7");
        }
    }

    /**
     * 6.3.8: Level A/U: font must have /ToUnicode CMap (exceptions for standard encodings).
     */
    private void checkToUnicode(COSDictionary font, String fontPath,
                                 String baseFont, PdfAValidationResult result) {
        // Exception: if encoding is a standard one, /ToUnicode is not required
        String encoding = font.getNameAsString("Encoding");
        if ("MacRomanEncoding".equals(encoding) || "WinAnsiEncoding".equals(encoding)
                || "MacExpertEncoding".equals(encoding)) {
            return;
        }

        // Type3 fonts can provide their own encoding and don't require /ToUnicode
        String subtype = font.getNameAsString("Subtype");
        if ("Type3".equals(subtype)) {
            return;
        }

        COSBase toUnicode = font.get("ToUnicode");
        if (toUnicode == null) {
            result.addError("6.3.8",
                    "Font must have /ToUnicode CMap for Level A/U compliance: " + baseFont,
                    fontPath, "6.3.8");
        }
    }

    /**
     * Resolves a COSBase to a COSDictionary, dereferencing indirect references.
     */
    private static COSDictionary resolveDict(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof COSDictionary) ? (COSDictionary) val : null;
    }

    /**
     * Resolves a COSBase to a COSArray, dereferencing indirect references.
     */
    private static COSArray resolveArray(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof COSArray) ? (COSArray) val : null;
    }
}
