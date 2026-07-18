package org.aspose.pdf.tests;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [Operator].
public class OperatorTest {

    @Test
    public void constructorWithNameOnly() {
        Operator op = new Operator("BT");
        assertEquals("BT", op.getName());
        assertTrue(op.getOperands().isEmpty());
    }

    @Test
    public void constructorWithOperands() {
        List<PdfBase> operands = Arrays.asList(
                PdfName.of("F1"),
                PdfInteger.valueOf(12)
        );
        Operator op = new Operator("Tf", operands);
        assertEquals("Tf", op.getName());
        assertEquals(2, op.getOperands().size());
        assertEquals(PdfName.of("F1"), op.getOperands().get(0));
        assertEquals(PdfInteger.valueOf(12), op.getOperands().get(1));
    }

    @Test
    public void operandsAreUnmodifiable() {
        List<PdfBase> operands = Arrays.asList(PdfInteger.valueOf(100), PdfInteger.valueOf(700));
        Operator op = new Operator("Td", operands);
        assertThrows(UnsupportedOperationException.class, () -> op.getOperands().add(PdfInteger.ZERO));
    }

    @Test
    public void toStringNoOperands() {
        Operator op = new Operator("ET");
        assertEquals("ET", op.toString());
    }

    @Test
    public void toStringWithOperands() {
        List<PdfBase> operands = Arrays.asList(
                PdfName.of("F1"),
                PdfInteger.valueOf(12)
        );
        Operator op = new Operator("Tf", operands);
        assertEquals("/F1 12 Tf", op.toString());
    }

    @Test
    public void toStringWithNumbers() {
        List<PdfBase> operands = Arrays.asList(
                PdfInteger.valueOf(100),
                PdfInteger.valueOf(700)
        );
        Operator op = new Operator("Td", operands);
        assertEquals("100 700 Td", op.toString());
    }

    @Test
    public void toStringWithString() {
        List<PdfBase> operands = Collections.singletonList(new PdfString("Hello World"));
        Operator op = new Operator("Tj", operands);
        assertEquals("(Hello World) Tj", op.toString());
    }

    @Test
    public void equalsAndHashCode() {
        List<PdfBase> ops1 = Arrays.asList(PdfInteger.valueOf(10), PdfInteger.valueOf(20));
        List<PdfBase> ops2 = Arrays.asList(PdfInteger.valueOf(10), PdfInteger.valueOf(20));
        Operator a = new Operator("Td", ops1);
        Operator b = new Operator("Td", ops2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void notEqualDifferentName() {
        Operator a = new Operator("BT");
        Operator b = new Operator("ET");
        assertNotEquals(a, b);
    }

    @Test
    public void notEqualDifferentOperands() {
        Operator a = new Operator("Td", Arrays.asList(PdfInteger.valueOf(10), PdfInteger.valueOf(20)));
        Operator b = new Operator("Td", Arrays.asList(PdfInteger.valueOf(30), PdfInteger.valueOf(40)));
        assertNotEquals(a, b);
    }

    @Test
    public void nullNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Operator(null));
    }

    @Test
    public void emptyNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Operator(""));
    }

    @Test
    public void matrixOperator() {
        List<PdfBase> operands = Arrays.asList(
                PdfInteger.valueOf(12), PdfInteger.valueOf(0),
                PdfInteger.valueOf(0), PdfInteger.valueOf(12),
                PdfInteger.valueOf(100), PdfInteger.valueOf(700)
        );
        Operator op = new Operator("cm", operands);
        assertEquals("cm", op.getName());
        assertEquals(6, op.getOperands().size());
        assertEquals("12 0 0 12 100 700 cm", op.toString());
    }
}
