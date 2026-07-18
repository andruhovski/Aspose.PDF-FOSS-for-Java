package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfString;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/// Set spacing, move to next line, and show text operator (").
///
/// Sets the word spacing and character spacing, moves to the next line, and shows a
/// text string. This operator is equivalent to:
///
/// <pre>
///   aw Tw
///   ac Tc
///   string '
/// </pre>
///
/// See ISO 32000-1:2008, §9.4.3, Table 109.
///
public class SetSpacingMoveToNextLineShowText extends TextShowOperator {

    private final double wordSpacing;
    private final double charSpacing;
    private final String text;

    /// Creates a SetSpacingMoveToNextLineShowText (") operator with the specified parameters.
    ///
    /// @param aw   the word spacing value
    /// @param ac   the character spacing value
    /// @param text the text string to show
    /// @throws IllegalArgumentException if text is null
    public SetSpacingMoveToNextLineShowText(double aw, double ac, String text) {
        super("\"", Arrays.asList(num(aw), num(ac),
                new PdfString(text.getBytes(StandardCharsets.ISO_8859_1))));
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        this.wordSpacing = aw;
        this.charSpacing = ac;
        this.text = text;
    }

    /// Creates a SetSpacingMoveToNextLineShowText (") operator from parsed operands.
    ///
    /// Expects three operands: wordSpacing (number), charSpacing (number), and a PdfString.
    ///
    /// @param operands the operands from the content stream parser
    public SetSpacingMoveToNextLineShowText(List<PdfBase> operands) {
        super("\"", operands);
        this.wordSpacing = (operands != null && operands.size() > 0)
                ? getNumber(operands.get(0))
                : 0;
        this.charSpacing = (operands != null && operands.size() > 1)
                ? getNumber(operands.get(1))
                : 0;
        if (operands != null && operands.size() > 2 && operands.get(2) instanceof PdfString) {
            this.text = ((PdfString) operands.get(2)).getString();
        } else {
            this.text = "";
        }
    }

    /// Returns the word spacing value.
    ///
    /// @return the word spacing
    public double getWordSpacing() {
        return wordSpacing;
    }

    /// Returns the character spacing value.
    ///
    /// @return the character spacing
    public double getCharSpacing() {
        return charSpacing;
    }

    /// Returns the text string.
    ///
    /// @return the text
    public String getText() {
        return text;
    }
}
