package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSName;

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
    public COSArray toCOSArray() {
        COSArray arr = new COSArray();
        arr.add(page != null ? page.getCOSDictionary() : org.aspose.pdf.engine.cos.COSNull.INSTANCE);
        arr.add(COSName.of("FitH"));
        arr.add(numOrNull(top));
        return arr;
    }
}
