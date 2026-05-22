package org.aspose.pdf.tests;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Operator}.
 */
public class OperatorTest {

    @Test
    public void constructorWithNameOnly() {
        Operator op = new Operator("BT");
        assertEquals("BT", op.getName());
        assertTrue(op.getOperands().isEmpty());
    }

    @Test
    public void constructorWithOperands() {
        List<COSBase> operands = Arrays.asList(
                COSName.of("F1"),
                COSInteger.valueOf(12)
        );
        Operator op = new Operator("Tf", operands);
        assertEquals("Tf", op.getName());
        assertEquals(2, op.getOperands().size());
        assertEquals(COSName.of("F1"), op.getOperands().get(0));
        assertEquals(COSInteger.valueOf(12), op.getOperands().get(1));
    }

    @Test
    public void operandsAreUnmodifiable() {
        List<COSBase> operands = Arrays.asList(COSInteger.valueOf(100), COSInteger.valueOf(700));
        Operator op = new Operator("Td", operands);
        assertThrows(UnsupportedOperationException.class, () -> op.getOperands().add(COSInteger.ZERO));
    }

    @Test
    public void toStringNoOperands() {
        Operator op = new Operator("ET");
        assertEquals("ET", op.toString());
    }

    @Test
    public void toStringWithOperands() {
        List<COSBase> operands = Arrays.asList(
                COSName.of("F1"),
                COSInteger.valueOf(12)
        );
        Operator op = new Operator("Tf", operands);
        assertEquals("/F1 12 Tf", op.toString());
    }

    @Test
    public void toStringWithNumbers() {
        List<COSBase> operands = Arrays.asList(
                COSInteger.valueOf(100),
                COSInteger.valueOf(700)
        );
        Operator op = new Operator("Td", operands);
        assertEquals("100 700 Td", op.toString());
    }

    @Test
    public void toStringWithString() {
        List<COSBase> operands = Collections.singletonList(new COSString("Hello World"));
        Operator op = new Operator("Tj", operands);
        assertEquals("(Hello World) Tj", op.toString());
    }

    @Test
    public void equalsAndHashCode() {
        List<COSBase> ops1 = Arrays.asList(COSInteger.valueOf(10), COSInteger.valueOf(20));
        List<COSBase> ops2 = Arrays.asList(COSInteger.valueOf(10), COSInteger.valueOf(20));
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
        Operator a = new Operator("Td", Arrays.asList(COSInteger.valueOf(10), COSInteger.valueOf(20)));
        Operator b = new Operator("Td", Arrays.asList(COSInteger.valueOf(30), COSInteger.valueOf(40)));
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
        List<COSBase> operands = Arrays.asList(
                COSInteger.valueOf(12), COSInteger.valueOf(0),
                COSInteger.valueOf(0), COSInteger.valueOf(12),
                COSInteger.valueOf(100), COSInteger.valueOf(700)
        );
        Operator op = new Operator("cm", operands);
        assertEquals("cm", op.getName());
        assertEquals(6, op.getOperands().size());
        assertEquals("12 0 0 12 100 700 cm", op.toString());
    }
}
