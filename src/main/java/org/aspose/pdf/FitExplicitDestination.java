package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;

/**
 * Fit explicit destination — display page scaled to fit entirely within window.
 */
public class FitExplicitDestination extends ExplicitDestination {

    public FitExplicitDestination(Page page) { super(page); }

    FitExplicitDestination(Page page, int pageNum) {
        super(page != null ? page : null);
    }

    @Override
    public COSArray toCOSArray() {
        COSArray arr = new COSArray();
        arr.add(page != null ? page.getCOSDictionary() : org.aspose.pdf.engine.cos.COSNull.INSTANCE);
        arr.add(COSName.of("Fit"));
        return arr;
    }
}
