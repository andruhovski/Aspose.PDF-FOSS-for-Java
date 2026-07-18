package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/// FitBH explicit destination — fit bounding box width, position at top.
public class FitBHExplicitDestination extends ExplicitDestination {
    private final double top;
    public FitBHExplicitDestination(Page page, double top) { super(page); this.top = top; }
    FitBHExplicitDestination(Page page, int pageNum, double top) { super(page != null ? page : null); this.top = top; }
    public double getTop() { return top; }

    @Override
    public PdfArray toPdfArray() {
        PdfArray arr = new PdfArray();
        arr.add(page != null ? page.getPdfDictionary() : org.aspose.pdf.engine.pdfobjects.PdfNull.INSTANCE);
        arr.add(PdfName.of("FitBH"));
        arr.add(numOrNull(top));
        return arr;
    }
}
