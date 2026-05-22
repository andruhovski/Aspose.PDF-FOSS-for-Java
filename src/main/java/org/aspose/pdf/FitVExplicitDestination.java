package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;

/**
 * FitV explicit destination — fit page height, position at left coordinate.
 */
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
    public COSArray toCOSArray() {
        COSArray arr = new COSArray();
        arr.add(page != null ? page.getCOSDictionary() : org.aspose.pdf.engine.cos.COSNull.INSTANCE);
        arr.add(COSName.of("FitV"));
        arr.add(numOrNull(left));
        return arr;
    }
}
