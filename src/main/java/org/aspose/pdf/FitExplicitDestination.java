package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/**
 * Fit explicit destination — display page scaled to fit entirely within window.
 */
public class FitExplicitDestination extends ExplicitDestination {

    public FitExplicitDestination(Page page) { super(page); }

    FitExplicitDestination(Page page, int pageNum) {
        super(page != null ? page : null);
    }

    @Override
    public PdfArray toPdfArray() {
        PdfArray arr = new PdfArray();
        arr.add(page != null ? page.getPdfDictionary() : org.aspose.pdf.engine.pdfobjects.PdfNull.INSTANCE);
        arr.add(PdfName.of("Fit"));
        return arr;
    }
}
