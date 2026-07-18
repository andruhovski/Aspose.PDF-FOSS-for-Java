package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/// FitB explicit destination — fit page bounding box within window.
public class FitBExplicitDestination extends ExplicitDestination {
    public FitBExplicitDestination(Page page) { super(page); }
    FitBExplicitDestination(Page page, int pageNum) { super(page != null ? page : null); }

    @Override
    public PdfArray toPdfArray() {
        PdfArray arr = new PdfArray();
        arr.add(page != null ? page.getPdfDictionary() : org.aspose.pdf.engine.pdfobjects.PdfNull.INSTANCE);
        arr.add(PdfName.of("FitB"));
        return arr;
    }
}
