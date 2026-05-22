package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;

/**
 * FitR explicit destination — fit specified rectangle within window.
 */
public class FitRExplicitDestination extends ExplicitDestination {

    private final double left, bottom, right, top;

    public FitRExplicitDestination(Page page, double left, double bottom, double right, double top) {
        super(page);
        this.left = left; this.bottom = bottom; this.right = right; this.top = top;
    }

    FitRExplicitDestination(Page page, int pageNum, double left, double bottom, double right, double top) {
        super(page != null ? page : null);
        this.left = left; this.bottom = bottom; this.right = right; this.top = top;
    }

    public double getLeft() { return left; }
    public double getBottom() { return bottom; }
    public double getRight() { return right; }
    public double getTop() { return top; }

    @Override
    public COSArray toCOSArray() {
        COSArray arr = new COSArray();
        arr.add(page != null ? page.getCOSDictionary() : org.aspose.pdf.engine.cos.COSNull.INSTANCE);
        arr.add(COSName.of("FitR"));
        arr.add(numOrNull(left)); arr.add(numOrNull(bottom));
        arr.add(numOrNull(right)); arr.add(numOrNull(top));
        return arr;
    }
}
