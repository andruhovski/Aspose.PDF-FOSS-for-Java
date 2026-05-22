package org.aspose.pdf.operators;

import org.aspose.pdf.engine.cos.COSBase;

import java.util.List;

/**
 * Move to start of next text line operator (T*).
 * <p>
 * Moves to the start of the next line, equivalent to {@code 0 -Tl Td}, where
 * Tl denotes the current leading parameter in the text state.
 * See ISO 32000-1:2008, §9.4.2, Table 108.
 * </p>
 */
public class MoveToNextLine extends TextPlaceOperator {

    /** Creates a MoveToNextLine operator. */
    public MoveToNextLine() {
        super("T*");
    }

    /** Creates a MoveToNextLine operator from parsed operands. */
    public MoveToNextLine(List<COSBase> operands) {
        super("T*", operands);
    }
}
