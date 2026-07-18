package org.aspose.pdf.engine.font;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.io.IOException;
import java.util.logging.Logger;

/// Simple Type 1 font (/Subtype /Type1) — ISO 32000-1:2008, §9.6.
///
/// Handles Standard 14 fonts as a special case: they require no embedding,
/// and their widths and encoding are provided by [StandardFonts].
/// For non-standard Type 1 fonts, reads /Widths, /FirstChar, /Encoding
/// from the font dictionary.
///
public class Type1Font extends PdfFont {

    private static final Logger LOG = Logger.getLogger(Type1Font.class.getName());

    private int[] standardWidths;
    private double[] customWidths;
    private int firstChar;

    /// Creates a Type1Font from a font dictionary.
    ///
    /// @param fontDict the font dictionary (/Type /Font, /Subtype /Type1)
    /// @param parser   the PDF parser (may be null for Standard 14 fonts)
    /// @throws IOException if reading the font data fails
    public Type1Font(PdfDictionary fontDict, PDFParser parser) throws IOException {
        super(fontDict, parser);

        // 1. Check if Standard 14
        if (baseFont != null && StandardFonts.isStandard(baseFont)) {
            this.standardWidths = StandardFonts.getWidths(baseFont);
            if (this.encoding == null) {
                this.encoding = StandardFonts.getEncoding(baseFont);
            }
        }

        // 2. Read /Encoding (may override standard)
        initEncoding();

        // 3. Read /Widths array if present
        initWidths();

        LOG.fine(() -> "Type1Font created: " + baseFont);
    }

    @Override
    public String decode(byte[] charCodes) throws IOException {
        return super.decode(charCodes);
    }

    @Override
    public double getWidth(int charCode) {
        // 1. Custom /Widths array (if present) with /FirstChar offset
        if (customWidths != null && charCode >= firstChar && charCode < firstChar + customWidths.length) {
            double w = customWidths[charCode - firstChar];
            if (w > 0) return w;
        }
        // 2. Standard 14 widths
        if (standardWidths != null && charCode >= 0 && charCode < 256) {
            return standardWidths[charCode];
        }
        // 3. MissingWidth from FontDescriptor
        if (fontDescriptor != null) {
            double mw = fontDescriptor.getMissingWidth();
            if (mw > 0) return mw;
        }
        // 4. Default
        return 1000;
    }

    private void initEncoding() {
        PdfBase encValue = resolve(fontDict.get("Encoding"));
        if (encValue instanceof PdfName) {
            FontEncoding named = FontEncoding.getInstance(((PdfName) encValue).getName());
            if (named != null) {
                this.encoding = named;
            }
        } else if (encValue instanceof PdfDictionary) {
            this.encoding = FontEncoding.fromDictionary((PdfDictionary) encValue);
        }
        // If still null and not Standard 14, use StandardEncoding
        if (this.encoding == null) {
            this.encoding = FontEncoding.STANDARD;
        }
    }

    private void initWidths() {
        PdfBase widthsVal = resolve(fontDict.get("Widths"));
        if (widthsVal instanceof PdfArray) {
            this.firstChar = fontDict.getInt("FirstChar", 0);
            PdfArray arr = (PdfArray) widthsVal;
            this.customWidths = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                customWidths[i] = getNumber(arr.get(i));
            }
        }
    }
}
