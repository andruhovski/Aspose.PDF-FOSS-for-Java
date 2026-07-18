package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/// FitBV explicit destination — fit bounding box height, position at left.
public class FitBVExplicitDestination extends ExplicitDestination {
    private final double left;
    public FitBVExplicitDestination(Page page, double left) { super(page); this.left = left; }
    FitBVExplicitDestination(Page page, int pageNum, double left) { super(page != null ? page : null); this.left = left; }
    public double getLeft() { return left; }

    @Override
    public PdfArray toPdfArray() {
        PdfArray arr = new PdfArray();
        arr.add(page != null ? page.getPdfDictionary() : org.aspose.pdf.engine.pdfobjects.PdfNull.INSTANCE);
        arr.add(PdfName.of("FitBV"));
        arr.add(numOrNull(left));
        return arr;
    }
}
