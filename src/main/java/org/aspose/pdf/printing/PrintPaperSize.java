package org.aspose.pdf.printing;

/**
 * Specifies the size of a piece of paper, with width and height in hundredths of an inch.
 */
public class PrintPaperSize {

    private String name;
    private int width;
    private int height;
    private PrinterPaperKind kind = PrinterPaperKind.Custom;

    /**
     * Creates a default paper size.
     */
    public PrintPaperSize() {
    }

    /**
     * Creates a paper size with the given name, width, and height.
     *
     * @param name   the paper name (e.g. "A4")
     * @param width  the width in 1/100 inch
     * @param height the height in 1/100 inch
     */
    public PrintPaperSize(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    /** Returns the paper name. */
    public String getName() { return name; }
    /** Sets the paper name. */
    public void setName(String name) { this.name = name; }

    /** Returns the width in 1/100 inch. */
    public int getWidth() { return width; }
    /** Sets the width in 1/100 inch. */
    public void setWidth(int width) { this.width = width; }

    /** Returns the height in 1/100 inch. */
    public int getHeight() { return height; }
    /** Sets the height in 1/100 inch. */
    public void setHeight(int height) { this.height = height; }

    /** Returns the paper kind. */
    public PrinterPaperKind getKind() { return kind; }
    /** Sets the paper kind. */
    public void setKind(PrinterPaperKind kind) { this.kind = kind; }
}
