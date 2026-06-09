package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/**
 * FitH explicit destination — fit page width, position at top coordinate.
 */
public class FitHExplicitDestination extends ExplicitDestination {

    private final double top;

    public FitHExplicitDestination(Page page, double top) {
        super(page);
        this.top = top;
    }

    FitHExplicitDestination(Page page, int pageNum, double top) {
        super(page != null ? page : null);
        this.top = top;
    }

    public double getTop() { return top; }

    @Override
    public PdfArray toPdfArray() {
        PdfArray arr = new PdfArray();
        arr.add(page != null ? page.getPdfDictionary() : org.aspose.pdf.engine.pdfobjects.PdfNull.INSTANCE);
        arr.add(PdfName.of("FitH"));
        arr.add(numOrNull(top));
        return arr;
    }
}
