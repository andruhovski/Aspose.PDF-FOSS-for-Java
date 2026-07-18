package org.aspose.pdf.operators;

import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.List;

/// Abstract base class for text state operators (ISO 32000-1:2008, §9.3).
///
/// Text state operators modify the text state parameters that control how text is
/// rendered. Concrete subclasses include operators for setting the font (Tf), character
/// spacing (Tc), word spacing (Tw), horizontal scaling (Tz), leading (TL), rendering
/// mode (Tr), and text rise (Ts).
///
public abstract class TextStateOperator extends TextOperator {

    /// Creates a text state operator with the given name and no operands.
    ///
    /// @param name the operator keyword (e.g., "Tf", "Tc", "Tw", "Tz", "TL", "Tr", "Ts")
    protected TextStateOperator(String name) {
        super(name);
    }

    /// Creates a text state operator with the given name and operands.
    ///
    /// @param name     the operator keyword
    /// @param operands the operands preceding this operator in the content stream
    protected TextStateOperator(String name, List<PdfBase> operands) {
        super(name, operands);
    }
}
