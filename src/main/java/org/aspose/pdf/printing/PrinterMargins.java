package org.aspose.pdf.printing;

/// Specifies the margins of a printed page, in hundredths of an inch.
public class PrinterMargins {

    private int left;
    private int right;
    private int top;
    private int bottom;

    /// Creates margins with the specified values (in 1/100 inch).
    ///
    /// @param left   left margin
    /// @param right  right margin
    /// @param top    top margin
    /// @param bottom bottom margin
    public PrinterMargins(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    /// Creates margins with all values set to zero.
    public PrinterMargins() {
    }

    /// Returns the left margin in 1/100 inch.
    public int getLeft() { return left; }
    /// Sets the left margin in 1/100 inch.
    public void setLeft(int left) { this.left = left; }

    /// Returns the right margin in 1/100 inch.
    public int getRight() { return right; }
    /// Sets the right margin in 1/100 inch.
    public void setRight(int right) { this.right = right; }

    /// Returns the top margin in 1/100 inch.
    public int getTop() { return top; }
    /// Sets the top margin in 1/100 inch.
    public void setTop(int top) { this.top = top; }

    /// Returns the bottom margin in 1/100 inch.
    public int getBottom() { return bottom; }
    /// Sets the bottom margin in 1/100 inch.
    public void setBottom(int bottom) { this.bottom = bottom; }
}
