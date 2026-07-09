package org.aspose.pdf.text;

import org.aspose.pdf.Operator;
import org.aspose.pdf.OperatorCollection;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

/**
 * Extracts table structures from PDF pages by analyzing text positions
 * and ruling lines to identify rows, columns, and cells.
 * <p>
 * The algorithm clusters text fragments by Y-coordinate to detect rows,
 * then by X-coordinate within each row to detect columns/cells.
 * A group of consecutive rows with a consistent column structure is
 * recognized as a table.
 * </p>
 */
public class TableAbsorber {

    private static final Logger LOG = Logger.getLogger(TableAbsorber.class.getName());

    /** Tolerance in points for same-row Y-coordinate detection. */
    private static final double ROW_TOLERANCE = 3.0;

    /** Minimum number of columns required to consider a group of rows as a table. */
    private static final int MIN_COLUMNS = 2;

    /** Minimum number of rows required to consider a group as a table. */
    private static final int MIN_ROWS = 2;

    private final List<AbsorbedTable> tables = new ArrayList<>();

    /**
     * Visits a page and extracts table structures.
     *
     * @param page the page to analyze
     * @throws IOException if text extraction fails
     */
    public void visit(Page page) throws IOException {
        tables.clear();

        // 1. Extract all text fragments using TextFragmentAbsorber
        TextFragmentAbsorber tfa = new TextFragmentAbsorber();
        tfa.visit(page);
        TextFragmentCollection fragments = tfa.getTextFragments();
        if (fragments == null || fragments.size() == 0) {
            return;
        }

        // 2. Collect fragments with valid positions into a sortable list
        List<TextFragment> positioned = new ArrayList<>();
        for (TextFragment f : fragments) {
            if (f.getPosition() != null) {
                positioned.add(f);
            }
        }
        if (positioned.isEmpty()) {
            return;
        }

        // Sort by Y descending (top to bottom), then X ascending (left to right)
        positioned.sort(Comparator
                .comparingDouble((TextFragment f) -> -f.getPosition().getYIndent())
                .thenComparingDouble(f -> f.getPosition().getXIndent()));

        // 3. Primary detection: ruling-line grids (PDFNEWNET-39178). Tables
        // drawn with explicit border/grid strokes are segmented exactly from
        // the geometry — the count and per-cell content then match Aspose.
        List<AbsorbedTable> ruled = detectRuledTables(page, positioned);
        if (!ruled.isEmpty()) {
            tables.addAll(ruled);
            LOG.fine(() -> "TableAbsorber found " + tables.size() + " ruled table(s)");
            return;
        }

        // 4. Fallback: text-position heuristic for tables without rulings.
        List<List<TextFragment>> rows = groupByY(positioned);
        List<List<List<TextFragment>>> tableGroups = detectTableGroups(rows);
        for (List<List<TextFragment>> group : tableGroups) {
            AbsorbedTable table = buildTable(group);
            if (table != null) {
                tables.add(table);
            }
        }

        LOG.fine(() -> "TableAbsorber found " + tables.size() + " table(s)");
    }

    // ================= Ruled-grid detection =================

    /** Segment endpoint / level-merge tolerance in points. */
    private static final double RULE_TOLERANCE = 3.0;

    /** Two grid levels closer than this merge into one rule (points). */
    private static final double LEVEL_MERGE = 2.0;

    /** Axis-alignment tolerance: max cross-axis drift for a rule (points). */
    private static final double AXIS_DRIFT = 0.7;

    /** Safety cap — pages with more rule segments fall back to the heuristic. */
    private static final int MAX_RULE_SEGMENTS = 5000;

    /**
     * Detects tables from ruling lines: collects the stroked/filled
     * axis-aligned path segments of the page (CTM applied), clusters
     * touching segments into connected grid regions, and slices each region
     * into rows/columns at the distinct horizontal/vertical rule levels.
     * Text fragments are assigned to cells by their anchor point.
     *
     * @return the ruled tables in top-to-bottom page order; empty when the
     *         page has no usable rulings
     */
    private List<AbsorbedTable> detectRuledTables(Page page, List<TextFragment> fragments) {
        List<double[]> segments;
        try {
            segments = collectRuleSegments(page);
        } catch (IOException e) {
            LOG.fine(() -> "Rule collection failed, falling back to heuristic: " + e.getMessage());
            return Collections.emptyList();
        }
        if (segments.isEmpty() || segments.size() > MAX_RULE_SEGMENTS) {
            return Collections.emptyList();
        }

        List<List<double[]>> clusters = clusterSegments(segments);
        List<AbsorbedTable> result = new ArrayList<>();
        for (List<double[]> cluster : clusters) {
            AbsorbedTable table = buildRuledTable(cluster, fragments);
            if (table != null) {
                result.add(table);
            }
        }
        // Top-to-bottom page order, matching visual reading order.
        result.sort(Comparator.comparingDouble(
                (AbsorbedTable t) -> -(t.getRectangle() != null ? t.getRectangle().getURY() : 0)));
        return result;
    }

    /**
     * Walks the page content stream tracking q/Q/cm and collects the
     * axis-aligned segments of every painted path ({@code m/l} polylines and
     * {@code re} rectangle edges). Curves and clipping-only paths are
     * ignored. Each segment is {x1, y1, x2, y2} in device space.
     */
    private List<double[]> collectRuleSegments(Page page) throws IOException {
        OperatorCollection ops = page.getContents();
        List<double[]> segments = new ArrayList<>();
        List<double[]> path = new ArrayList<>();
        double[] ctm = {1, 0, 0, 1, 0, 0};
        Deque<double[]> gsStack = new ArrayDeque<>();
        double curX = 0;
        double curY = 0;
        double startX = 0;
        double startY = 0;

        for (int i = 0; i < ops.size(); i++) {
            Operator op = ops.getAt(i);
            String name = op.getName();
            List<PdfBase> operands = op.getOperands();
            switch (name) {
                case "q":
                    gsStack.push(ctm.clone());
                    break;
                case "Q":
                    if (!gsStack.isEmpty()) {
                        ctm = gsStack.pop();
                    }
                    break;
                case "cm": {
                    if (operands == null || operands.size() < 6) break;
                    double[] m = new double[6];
                    for (int k = 0; k < 6; k++) {
                        m[k] = toDouble(operands.get(k));
                    }
                    ctm = multiply(m, ctm);
                    break;
                }
                case "m":
                    if (operands == null || operands.size() < 2) break;
                    curX = toDouble(operands.get(0));
                    curY = toDouble(operands.get(1));
                    startX = curX;
                    startY = curY;
                    break;
                case "l": {
                    if (operands == null || operands.size() < 2) break;
                    double nx = toDouble(operands.get(0));
                    double ny = toDouble(operands.get(1));
                    addSegment(path, ctm, curX, curY, nx, ny);
                    curX = nx;
                    curY = ny;
                    break;
                }
                case "h":
                    addSegment(path, ctm, curX, curY, startX, startY);
                    curX = startX;
                    curY = startY;
                    break;
                case "re": {
                    if (operands == null || operands.size() < 4) break;
                    double x = toDouble(operands.get(0));
                    double y = toDouble(operands.get(1));
                    double w = toDouble(operands.get(2));
                    double h = toDouble(operands.get(3));
                    addSegment(path, ctm, x, y, x + w, y);
                    addSegment(path, ctm, x, y + h, x + w, y + h);
                    addSegment(path, ctm, x, y, x, y + h);
                    addSegment(path, ctm, x + w, y, x + w, y + h);
                    curX = x;
                    curY = y;
                    startX = x;
                    startY = y;
                    break;
                }
                case "S": case "s": case "f": case "F":
                case "f*": case "B": case "B*": case "b": case "b*":
                    segments.addAll(path);
                    path.clear();
                    break;
                case "n":
                    // Clipping-only path — not painted, not a rule.
                    path.clear();
                    break;
                default:
                    break;
            }
        }
        return segments;
    }

    /** Adds the (transformed) segment to {@code path} if it is axis-aligned. */
    private static void addSegment(List<double[]> path, double[] ctm,
                                   double x1, double y1, double x2, double y2) {
        double dx1 = ctm[0] * x1 + ctm[2] * y1 + ctm[4];
        double dy1 = ctm[1] * x1 + ctm[3] * y1 + ctm[5];
        double dx2 = ctm[0] * x2 + ctm[2] * y2 + ctm[4];
        double dy2 = ctm[1] * x2 + ctm[3] * y2 + ctm[5];
        boolean horizontal = Math.abs(dy1 - dy2) <= AXIS_DRIFT;
        boolean vertical = Math.abs(dx1 - dx2) <= AXIS_DRIFT;
        if (!horizontal && !vertical) {
            return;
        }
        if (horizontal && vertical) {
            return;     // degenerate point
        }
        path.add(new double[]{Math.min(dx1, dx2), Math.min(dy1, dy2),
                Math.max(dx1, dx2), Math.max(dy1, dy2)});
    }

    /** 3x2 PDF matrix multiplication: result = m × ctm. */
    private static double[] multiply(double[] m, double[] ctm) {
        return new double[]{
                m[0] * ctm[0] + m[1] * ctm[2],
                m[0] * ctm[1] + m[1] * ctm[3],
                m[2] * ctm[0] + m[3] * ctm[2],
                m[2] * ctm[1] + m[3] * ctm[3],
                m[4] * ctm[0] + m[5] * ctm[2] + ctm[4],
                m[4] * ctm[1] + m[5] * ctm[3] + ctm[5]
        };
    }

    private static double toDouble(PdfBase value) {
        if (value instanceof PdfInteger) {
            return ((PdfInteger) value).intValue();
        }
        if (value instanceof PdfFloat) {
            return ((PdfFloat) value).doubleValue();
        }
        return 0;
    }

    /** Groups segments whose bounding boxes touch (within tolerance) into clusters. */
    private static List<List<double[]>> clusterSegments(List<double[]> segments) {
        int n = segments.size();
        int[] component = new int[n];
        java.util.Arrays.fill(component, -1);
        int clusterCount = 0;
        for (int i = 0; i < n; i++) {
            if (component[i] >= 0) {
                continue;
            }
            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(i);
            component[i] = clusterCount;
            while (!queue.isEmpty()) {
                int u = queue.poll();
                double[] a = segments.get(u);
                for (int v = 0; v < n; v++) {
                    if (component[v] >= 0) {
                        continue;
                    }
                    double[] b = segments.get(v);
                    if (a[0] - RULE_TOLERANCE <= b[2] && a[2] + RULE_TOLERANCE >= b[0]
                            && a[1] - RULE_TOLERANCE <= b[3] && a[3] + RULE_TOLERANCE >= b[1]) {
                        component[v] = clusterCount;
                        queue.add(v);
                    }
                }
            }
            clusterCount++;
        }
        List<List<double[]>> clusters = new ArrayList<>(clusterCount);
        for (int c = 0; c < clusterCount; c++) {
            clusters.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            clusters.get(component[i]).add(segments.get(i));
        }
        return clusters;
    }

    /**
     * Slices one rule cluster into a row/column grid and fills the cells
     * with the text fragments whose anchor falls inside. Returns {@code null}
     * when the cluster has fewer than two horizontal or vertical rule levels
     * (a lone underline is not a table).
     */
    private AbsorbedTable buildRuledTable(List<double[]> cluster, List<TextFragment> fragments) {
        List<Double> hLevels = new ArrayList<>();
        List<Double> vLevels = new ArrayList<>();
        for (double[] s : cluster) {
            if (Math.abs(s[1] - s[3]) <= AXIS_DRIFT) {
                addLevel(hLevels, (s[1] + s[3]) / 2);
            } else if (Math.abs(s[0] - s[2]) <= AXIS_DRIFT) {
                addLevel(vLevels, (s[0] + s[2]) / 2);
            }
        }
        if (hLevels.size() < 2 || vLevels.size() < 2) {
            return null;
        }
        hLevels.sort(Comparator.reverseOrder());   // top → bottom
        vLevels.sort(Comparator.naturalOrder());   // left → right

        AbsorbedTable table = new AbsorbedTable();
        for (int r = 0; r + 1 < hLevels.size(); r++) {
            double top = hLevels.get(r);
            double bottom = hLevels.get(r + 1);
            AbsorbedRow row = new AbsorbedRow();
            for (int c = 0; c + 1 < vLevels.size(); c++) {
                double left = vLevels.get(c);
                double right = vLevels.get(c + 1);
                AbsorbedCell cell = new AbsorbedCell();
                cell.setRectangle(new Rectangle(left, bottom, right, top));
                for (TextFragment f : fragments) {
                    double[] anchor = anchorOf(f);
                    if (anchor != null
                            && anchor[0] >= left - 1 && anchor[0] <= right + 1
                            && anchor[1] >= bottom - 1 && anchor[1] <= top + 1) {
                        cell.addTextFragment(f);
                    }
                }
                row.addCell(cell);
            }
            table.addRow(row);
        }
        table.setRectangle(new Rectangle(
                vLevels.get(0), hLevels.get(hLevels.size() - 1),
                vLevels.get(vLevels.size() - 1), hLevels.get(0)));
        return table;
    }

    /** Merges {@code value} into the level list within {@link #LEVEL_MERGE}. */
    private static void addLevel(List<Double> levels, double value) {
        for (int i = 0; i < levels.size(); i++) {
            if (Math.abs(levels.get(i) - value) <= LEVEL_MERGE) {
                return;
            }
        }
        levels.add(value);
    }

    /** The point used to place a fragment into a cell: rect centre, else position. */
    private static double[] anchorOf(TextFragment f) {
        Rectangle r = f.getRectangle();
        if (r != null && r.getWidth() > 0) {
            return new double[]{(r.getLLX() + r.getURX()) / 2, (r.getLLY() + r.getURY()) / 2};
        }
        Position p = f.getPosition();
        return p != null ? new double[]{p.getXIndent(), p.getYIndent()} : null;
    }

    /**
     * Returns the list of tables detected on the last visited page.
     *
     * @return unmodifiable list of absorbed tables
     */
    public List<AbsorbedTable> getTableList() {
        return Collections.unmodifiableList(tables);
    }

    /**
     * Groups text fragments into rows based on Y-coordinate proximity.
     */
    private List<List<TextFragment>> groupByY(List<TextFragment> fragments) {
        List<List<TextFragment>> rows = new ArrayList<>();
        List<TextFragment> currentRow = new ArrayList<>();
        double currentY = Double.NaN;

        for (TextFragment f : fragments) {
            double y = f.getPosition().getYIndent();
            if (Double.isNaN(currentY) || Math.abs(y - currentY) <= ROW_TOLERANCE) {
                currentRow.add(f);
                if (Double.isNaN(currentY)) {
                    currentY = y;
                }
            } else {
                if (!currentRow.isEmpty()) {
                    // Sort row by X coordinate
                    currentRow.sort(Comparator.comparingDouble(
                            fr -> fr.getPosition().getXIndent()));
                    rows.add(currentRow);
                }
                currentRow = new ArrayList<>();
                currentRow.add(f);
                currentY = y;
            }
        }
        if (!currentRow.isEmpty()) {
            currentRow.sort(Comparator.comparingDouble(
                    fr -> fr.getPosition().getXIndent()));
            rows.add(currentRow);
        }
        return rows;
    }

    /**
     * Detects groups of consecutive rows that form tables.
     * A table is a sequence of rows where each row has at least MIN_COLUMNS fragments.
     */
    private List<List<List<TextFragment>>> detectTableGroups(List<List<TextFragment>> rows) {
        List<List<List<TextFragment>>> groups = new ArrayList<>();
        List<List<TextFragment>> currentGroup = new ArrayList<>();

        for (List<TextFragment> row : rows) {
            if (row.size() >= MIN_COLUMNS) {
                currentGroup.add(row);
            } else {
                if (currentGroup.size() >= MIN_ROWS) {
                    groups.add(currentGroup);
                }
                currentGroup = new ArrayList<>();
            }
        }
        if (currentGroup.size() >= MIN_ROWS) {
            groups.add(currentGroup);
        }
        return groups;
    }

    /**
     * Builds an AbsorbedTable from a group of rows.
     */
    private AbsorbedTable buildTable(List<List<TextFragment>> rowFragments) {
        AbsorbedTable table = new AbsorbedTable();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (List<TextFragment> rowFrags : rowFragments) {
            AbsorbedRow row = new AbsorbedRow();
            for (TextFragment f : rowFrags) {
                AbsorbedCell cell = new AbsorbedCell();
                cell.addTextFragment(f);
                if (f.getRectangle() != null) {
                    cell.setRectangle(f.getRectangle());
                    Rectangle r = f.getRectangle();
                    minX = Math.min(minX, r.getLLX());
                    minY = Math.min(minY, r.getLLY());
                    maxX = Math.max(maxX, r.getURX());
                    maxY = Math.max(maxY, r.getURY());
                }
                row.addCell(cell);
            }
            table.addRow(row);
        }

        if (minX != Double.MAX_VALUE) {
            table.setRectangle(new Rectangle(minX, minY, maxX, maxY));
        }
        return table;
    }
}
