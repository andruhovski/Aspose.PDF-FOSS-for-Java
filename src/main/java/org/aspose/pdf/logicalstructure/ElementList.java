package org.aspose.pdf.logicalstructure;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/// Ordered list of child [StructureElement]s within a structure tree node.
public class ElementList implements Iterable<StructureElement> {

    private final List<StructureElement> elements;

    /// Creates an element list.
    ///
    /// @param elements the elements (may be null)
    public ElementList(List<StructureElement> elements) {
        this.elements = elements != null ? elements : Collections.emptyList();
    }

    /// Returns the number of elements.
    ///
    /// @return the element count
    public int getCount() { return elements.size(); }

    /// Returns the element at the given index.
    ///
    /// @param index the 0-based index
    /// @return the structure element
    public StructureElement get(int index) { return elements.get(index); }

    @Override
    public Iterator<StructureElement> iterator() { return elements.iterator(); }
}
