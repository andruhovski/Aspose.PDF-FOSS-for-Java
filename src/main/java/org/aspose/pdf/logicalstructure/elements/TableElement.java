package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;

import java.util.logging.Logger;

/**
 * Represents a table (Table) structure element in the logical structure tree
 * (ISO 32000-1:2008, §14.8.4.4, Table 337).
 */
public class TableElement extends Element {

    private static final Logger LOG = Logger.getLogger(TableElement.class.getName());

    private int repeatingRowsCount;

    /**
     * Creates a Table element wrapping the given structure element.
     *
     * @param se the structure element with type Table
     */
    public TableElement(StructureElement se) {
        super(se);
    }

    /**
     * Returns the number of repeating header rows.
     *
     * @return the repeating rows count
     */
    public int getRepeatingRowsCount() {
        return repeatingRowsCount;
    }

    /**
     * Sets the number of repeating header rows.
     *
     * @param count the number of rows to repeat
     */
    public void setRepeatingRowsCount(int count) {
        this.repeatingRowsCount = count;
    }
}
