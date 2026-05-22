package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;

/** FitB explicit destination — fit page bounding box within window. */
public class FitBExplicitDestination extends ExplicitDestination {
    public FitBExplicitDestination(Page page) { super(page); }
    FitBExplicitDestination(Page page, int pageNum) { super(page != null ? page : null); }

    @Override
    public COSArray toCOSArray() {
        COSArray arr = new COSArray();
        arr.add(page != null ? page.getCOSDictionary() : org.aspose.pdf.engine.cos.COSNull.INSTANCE);
        arr.add(COSName.of("FitB"));
        return arr;
    }
}
