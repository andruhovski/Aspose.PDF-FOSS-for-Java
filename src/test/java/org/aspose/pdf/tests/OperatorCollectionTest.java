package org.aspose.pdf.tests;

import org.aspose.pdf.Operator;
import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link OperatorCollection}.
 */
public class OperatorCollectionTest {

    @Test
    public void emptyCollection() {
        OperatorCollection coll = new OperatorCollection();
        assertEquals(0, coll.size());
        assertFalse(coll.iterator().hasNext());
        assertTrue(coll.getAll().isEmpty());
    }

    @Test
    public void constructFromList() {
        List<Operator> ops = Arrays.asList(
                new Operator("BT"),
                new Operator("Tf", Arrays.asList(COSName.of("F1"), COSInteger.valueOf(12))),
                new Operator("ET")
        );
        OperatorCollection coll = new OperatorCollection(ops);
        assertEquals(3, coll.size());
        // 1-based indexing (Aspose parity): get(1) is the first element.
        assertEquals("BT", coll.get(1).getName());
        assertEquals("Tf", coll.get(2).getName());
        assertEquals("ET", coll.get(3).getName());
        // 0-based engine accessor mirrors the same data.
        assertEquals("BT", coll.getAt(0).getName());
        assertEquals("ET", coll.getAt(2).getName());
    }

    @Test
    public void addOperator() {
        OperatorCollection coll = new OperatorCollection();
        coll.add(new Operator("q"));
        coll.add(new Operator("Q"));
        assertEquals(2, coll.size());
        assertEquals("q", coll.get(1).getName());
        assertEquals("Q", coll.get(2).getName());
    }

    @Test
    public void addNullThrows() {
        OperatorCollection coll = new OperatorCollection();
        assertThrows(IllegalArgumentException.class, () -> coll.add((Operator) null));
    }

    @Test
    public void iteratorWorks() {
        OperatorCollection coll = new OperatorCollection();
        coll.add(new Operator("BT"));
        coll.add(new Operator("ET"));

        List<String> names = new ArrayList<>();
        for (Operator op : coll) {
            names.add(op.getName());
        }
        assertEquals(Arrays.asList("BT", "ET"), names);
    }

    @Test
    public void getAllIsUnmodifiable() {
        OperatorCollection coll = new OperatorCollection();
        coll.add(new Operator("BT"));
        assertThrows(UnsupportedOperationException.class, () -> coll.getAll().add(new Operator("ET")));
    }

    @Test
    public void getOutOfBoundsThrows() {
        OperatorCollection coll = new OperatorCollection();
        assertThrows(IndexOutOfBoundsException.class, () -> coll.get(0));
    }

    @Test
    public void nullListThrows() {
        assertThrows(IllegalArgumentException.class, () -> new OperatorCollection(null));
    }

    @Test
    public void toStringFormat() {
        OperatorCollection coll = new OperatorCollection();
        coll.add(new Operator("BT"));
        coll.add(new Operator("ET"));
        String s = coll.toString();
        assertTrue(s.contains("size=2"));
        assertTrue(s.contains("BT"));
        assertTrue(s.contains("ET"));
    }
}
