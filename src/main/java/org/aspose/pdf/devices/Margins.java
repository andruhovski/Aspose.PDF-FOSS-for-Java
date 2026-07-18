package org.aspose.pdf.devices;

/// Page margins in printer/device units, used by the C# printing API
/// (`Aspose.Pdf.Devices.Margins`) for `PageSettings.Margins`
/// and `PrinterSettings.DefaultPageSettings.Margins`.
///
/// The constructor parameter order — `(left, right, top, bottom)` —
/// matches `System.Drawing.Printing.Margins` that Aspose's C# build
/// exposes via its `Aspose.Pdf.Devices` alias. Units are integer
/// hundredths of an inch in the .NET tradition; OpenPDF treats them as
/// opaque integers and passes them through to the printing-side renderer if
/// that subsystem is wired up.
///
/// Independent of [org.aspose.pdf.MarginInfo], which is the
/// floating-point-units margin descriptor on the document-side
/// `PageInfo`. The two types serve different layers — one is a
/// printer-side device margin, the other is a layout-side paragraph margin —
/// and are not interchangeable.
public class Margins {

    private int left;
    private int right;
    private int top;
    private int bottom;

    /// Creates margins of zero on every side.
    public Margins() {
    }

    /// Creates margins with the given side widths (in device units).
    ///
    /// @param left   the left margin
    /// @param right  the right margin
    /// @param top    the top margin
    /// @param bottom the bottom margin
    public Margins(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    /// @return the left margin
    public int getLeft() {
        return left;
    }

    /// Sets the left margin.
    ///
    /// @param left the new left margin
    public void setLeft(int left) {
        this.left = left;
    }

    /// @return the right margin
    public int getRight() {
        return right;
    }

    /// Sets the right margin.
    ///
    /// @param right the new right margin
    public void setRight(int right) {
        this.right = right;
    }

    /// @return the top margin
    public int getTop() {
        return top;
    }

    /// Sets the top margin.
    ///
    /// @param top the new top margin
    public void setTop(int top) {
        this.top = top;
    }

    /// @return the bottom margin
    public int getBottom() {
        return bottom;
    }

    /// Sets the bottom margin.
    ///
    /// @param bottom the new bottom margin
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    @Override
    public String toString() {
        return "Margins[L=" + left + ", R=" + right + ", T=" + top + ", B=" + bottom + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Margins)) return false;
        Margins m = (Margins) o;
        return left == m.left && right == m.right && top == m.top && bottom == m.bottom;
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + right;
        result = 31 * result + top;
        result = 31 * result + bottom;
        return result;
    }
}
