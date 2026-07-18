package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;
import org.aspose.pdf.engine.pdfobjects.PdfBase;

import java.util.Arrays;
import java.util.List;

/// Set char width and bounding box operator for Type 3 fonts (d1).
///
/// Sets the glyph width and bounding box in a Type 3 font glyph description.
/// The operands are wx, wy (displacement vector) and llx, lly, urx, ury (bounding box).
/// See ISO 32000-1:2008, §9.6.5, Table 113.
///
public class SetCharWidthBoundingBox extends Operator {

    private final double wx;
    private final double wy;
    private final double llx;
    private final double lly;
    private final double urx;
    private final double ury;

    /// Creates a SetCharWidthBoundingBox (d1) operator with the specified values.
    ///
    /// @param wx  the horizontal displacement
    /// @param wy  the vertical displacement
    /// @param llx the lower-left x of the bounding box
    /// @param lly the lower-left y of the bounding box
    /// @param urx the upper-right x of the bounding box
    /// @param ury the upper-right y of the bounding box
    public SetCharWidthBoundingBox(double wx, double wy, double llx, double lly, double urx, double ury) {
        super("d1", Arrays.asList(num(wx), num(wy), num(llx), num(lly), num(urx), num(ury)));
        this.wx = wx;
        this.wy = wy;
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }

    /// Creates a SetCharWidthBoundingBox (d1) operator from parsed operands.
    ///
    /// Expects six numeric operands: wx, wy, llx, lly, urx, ury.
    ///
    /// @param operands the operands from the content stream parser
    public SetCharWidthBoundingBox(List<PdfBase> operands) {
        super("d1", operands);
        this.wx  = (operands != null && operands.size() > 0) ? getNumber(operands.get(0)) : 0;
        this.wy  = (operands != null && operands.size() > 1) ? getNumber(operands.get(1)) : 0;
        this.llx = (operands != null && operands.size() > 2) ? getNumber(operands.get(2)) : 0;
        this.lly = (operands != null && operands.size() > 3) ? getNumber(operands.get(3)) : 0;
        this.urx = (operands != null && operands.size() > 4) ? getNumber(operands.get(4)) : 0;
        this.ury = (operands != null && operands.size() > 5) ? getNumber(operands.get(5)) : 0;
    }

    /// Returns the horizontal displacement component.
    ///
    /// @return wx
    public double getWx() { return wx; }

    /// Returns the vertical displacement component.
    ///
    /// @return wy
    public double getWy() { return wy; }

    /// Returns the lower-left x of the bounding box.
    ///
    /// @return llx
    public double getLlx() { return llx; }

    /// Returns the lower-left y of the bounding box.
    ///
    /// @return lly
    public double getLly() { return lly; }

    /// Returns the upper-right x of the bounding box.
    ///
    /// @return urx
    public double getUrx() { return urx; }

    /// Returns the upper-right y of the bounding box.
    ///
    /// @return ury
    public double getUry() { return ury; }
}
