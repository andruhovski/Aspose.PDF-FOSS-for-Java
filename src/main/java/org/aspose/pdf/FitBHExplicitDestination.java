package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;

/** FitBH explicit destination — fit bounding box width, position at top. */
public class FitBHExplicitDestination extends ExplicitDestination {
    private final double top;
    public FitBHExplicitDestination(Page page, double top) { super(page); this.top = top; }
    FitBHExplicitDestination(Page page, int pageNum, double top) { super(page != null ? page : null); this.top = top; }
    public double getTop() { return top; }

    @Override
    public COSArray toCOSArray() {
        COSArray arr = new COSArray();
        arr.add(page != null ? page.getCOSDictionary() : org.aspose.pdf.engine.cos.COSNull.INSTANCE);
        arr.add(COSName.of("FitBH"));
        arr.add(numOrNull(top));
        return arr;
    }
}
