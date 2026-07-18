package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/// FitV explicit destination — fit page height, position at left coordinate.
public class FitVExplicitDestination extends ExplicitDestination {

    private final double left;

    public FitVExplicitDestination(Page page, double left) {
        super(page);
        this.left = left;
    }

    FitVExplicitDestination(Page page, int pageNum, double left) {
        super(page != null ? page : null);
        this.left = left;
    }

    public double getLeft() { return left; }

    @Override
    public PdfArray toPdfArray() {
        PdfArray arr = new PdfArray();
        arr.add(page != null ? page.getPdfDictionary() : org.aspose.pdf.engine.pdfobjects.PdfNull.INSTANCE);
        arr.add(PdfName.of("FitV"));
        arr.add(numOrNull(left));
        return arr;
    }
}
