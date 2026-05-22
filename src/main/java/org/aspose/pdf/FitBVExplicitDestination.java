package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;

/** FitBV explicit destination — fit bounding box height, position at left. */
public class FitBVExplicitDestination extends ExplicitDestination {
    private final double left;
    public FitBVExplicitDestination(Page page, double left) { super(page); this.left = left; }
    FitBVExplicitDestination(Page page, int pageNum, double left) { super(page != null ? page : null); this.left = left; }
    public double getLeft() { return left; }

    @Override
    public COSArray toCOSArray() {
        COSArray arr = new COSArray();
        arr.add(page != null ? page.getCOSDictionary() : org.aspose.pdf.engine.cos.COSNull.INSTANCE);
        arr.add(COSName.of("FitBV"));
        arr.add(numOrNull(left));
        return arr;
    }
}
