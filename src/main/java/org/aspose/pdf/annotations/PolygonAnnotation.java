package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * Polygon annotation (ISO 32000-1:2008, Section 12.5.6.9, /Subtype /Polygon).
 * <p>
 * A polygon annotation displays a closed polygon on the page. The vertices
 * of the polygon are specified by the /Vertices array.
 * </p>
 */
public class PolygonAnnotation extends MarkupAnnotation {

    private Color interiorColor;

    /**
     * Constructs a polygon annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public PolygonAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new polygon annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public PolygonAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Polygon"));
    }

    /**
     * Constructs a new polygon annotation with the given rectangle and vertices.
     *
     * @param page     the page this annotation belongs to
     * @param rect     the annotation rectangle
     * @param vertices the vertices of the polygon as an array of {@link Point} objects
     */
    public PolygonAnnotation(Page page, Rectangle rect, Point[] vertices) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Polygon"));
        setVertexPoints(vertices);
    }

    /**
     * Returns the vertices of the polygon as an array of coordinate pairs [x1, y1, x2, y2, ...].
     *
     * @return the vertices array, or null if not set
     */
    public double[] getVertices() {
        COSBase v = dict.get("Vertices");
        if (v instanceof COSArray) {
            COSArray arr = (COSArray) v;
            double[] result = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = arr.getFloat(i, 0);
            }
            return result;
        }
        return null;
    }

    /**
     * Returns the vertices of the polygon as an array of {@link Point} objects.
     *
     * @return the vertices as Points, or null if not set
     */
    public Point[] getVertexPoints() {
        double[] coords = getVertices();
        if (coords == null || coords.length < 2) return null;
        int count = coords.length / 2;
        Point[] points = new Point[count];
        for (int i = 0; i < count; i++) {
            points[i] = new Point(coords[i * 2], coords[i * 2 + 1]);
        }
        return points;
    }

    /**
     * Sets the vertices of the polygon from an array of {@link Point} objects.
     *
     * @param vertices the vertices to set
     */
    public void setVertexPoints(Point[] vertices) {
        if (vertices == null) {
            dict.remove(COSName.of("Vertices"));
            return;
        }
        COSArray arr = new COSArray();
        for (Point p : vertices) {
            arr.add(new COSFloat(p.getX()));
            arr.add(new COSFloat(p.getY()));
        }
        dict.set(COSName.of("Vertices"), arr);
    }

    /**
     * Returns the interior color used to fill the polygon (/IC entry).
     *
     * @return the interior color, or null if not set
     */
    public Color getInteriorColor() {
        if (interiorColor != null) return interiorColor;
        COSBase ic = dict.get("IC");
        if (ic instanceof COSArray) {
            COSArray arr = (COSArray) ic;
            if (arr.size() == 3) return Color.fromRgb(arr.getFloat(0, 0), arr.getFloat(1, 0), arr.getFloat(2, 0));
        }
        return null;
    }

    /**
     * Sets the interior color used to fill the polygon (/IC entry).
     *
     * @param color the interior color, or null to remove
     */
    public void setInteriorColor(Color color) {
        this.interiorColor = color;
        if (color == null) {
            dict.remove(COSName.of("IC"));
            return;
        }
        COSArray ic = new COSArray();
        ic.add(new COSFloat(color.getR()));
        ic.add(new COSFloat(color.getG()));
        ic.add(new COSFloat(color.getB()));
        dict.set(COSName.of("IC"), ic);
    }
}
