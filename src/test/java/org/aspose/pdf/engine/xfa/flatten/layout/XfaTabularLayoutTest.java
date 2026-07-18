package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.layout.TextLayoutHelper;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// L2 PART A — tabular / horizontal layout B oracle: column x positions, row heights and
/// lr-tb wrap points asserted against hand-computed values; columns aligned across rows with
/// no cell overlap (C oracle).
public class XfaTabularLayoutTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";
    private static final double EPS = 1e-6;
    private static final double LH = TextLayoutHelper.getLineHeight("Helvetica", 10);

    private static String wrap(String rootChildren, String rootAttrs) {
        return "<template xmlns='" + TPL + "'>"
                + "<subform name='form1' " + rootAttrs + ">" + rootChildren + "</subform>"
                + "<pageSet><pageArea><contentArea w='400pt' h='500pt'/></pageArea></pageSet>"
                + "</template>";
    }

    /* --------------------------------- row --------------------------------- */

    @Test
    void rowPlacesCellsByColumnWidths() throws Exception {
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='r' layout='row' columnWidths='100pt 200pt 150pt'>"
                        + "  <field name='c0' h='20pt'><value><text/></value></field>"
                        + "  <field name='c1' h='20pt'><value><text/></value></field>"
                        + "  <field name='c2' h='20pt'><value><text/></value></field>"
                        + "</subform>",
                "layout='tb'"), null);

        XfaLayoutNode row = r.root.getChildren().get(0);
        List<XfaLayoutNode> cells = row.getChildren();
        assertEquals(3, cells.size());
        assertEquals(0, cells.get(0).getX(), EPS, "cell0 x");
        assertEquals(100, cells.get(1).getX(), EPS, "cell1 x = col0");
        assertEquals(300, cells.get(2).getX(), EPS, "cell2 x = col0+col1");
        assertEquals(100, cells.get(0).getWidth(), EPS);
        assertEquals(200, cells.get(1).getWidth(), EPS);
        assertEquals(150, cells.get(2).getWidth(), EPS);
        assertEquals(20, row.getHeight(), EPS, "row height = cell height");
    }

    @Test
    void rowHeightIsTallestCell() throws Exception {
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='r' layout='row' columnWidths='100pt 100pt 100pt'>"
                        + "  <field name='c0' h='20pt'><value><text/></value></field>"
                        + "  <field name='c1' h='50pt'><value><text/></value></field>"
                        + "  <field name='c2' h='30pt'><value><text/></value></field>"
                        + "</subform>",
                "layout='tb'"), null);
        XfaLayoutNode row = r.root.getChildren().get(0);
        assertEquals(50, row.getHeight(), EPS, "tallest cell wins");
        // all cells share the row's top (no vertical offset within a row)
        for (XfaLayoutNode cell : row.getChildren()) {
            assertEquals(row.getY(), cell.getY(), EPS, "cells top-aligned in the row");
        }
    }

    @Test
    void growableRowHeightFromBoundData() throws Exception {
        // cell 'Memo' bound to 3 lines (in a 150pt column) is taller than 1-line 'Short'.
        String tpl = wrap(
                "<subform name='r' layout='row' columnWidths='150pt 100pt'>"
                        + "  <field name='Memo'><font size='10pt'/><ui><textEdit/></ui><value><text/></value></field>"
                        + "  <field name='Short'><font size='10pt'/><ui><textEdit/></ui><value><text/></value></field>"
                        + "</subform>",
                "layout='tb'");
        String data = "<xfa:data xmlns:xfa='" + DATA + "'><form1><Memo>L1\nL2\nL3</Memo><Short>x</Short></form1></xfa:data>";

        XfaFlowLayout.Result r = layout(tpl, data);
        XfaLayoutNode row = r.root.getChildren().get(0);
        assertEquals(3 * LH, row.getChildren().get(0).getHeight(), EPS, "Memo = 3 lines");
        // A grid cell is stretched to the row height so cell borders form even horizontal rules — the
        // 1-line 'Short' cell occupies the full 3-line row height (its content is placed by vAlign).
        assertEquals(3 * LH, row.getChildren().get(1).getHeight(), EPS, "Short stretched to row height");
        assertEquals(3 * LH, row.getHeight(), EPS, "row height = tallest cell (Memo)");
    }

    /* -------------------------------- table -------------------------------- */

    @Test
    void tableStacksRowsWithAlignedColumns() throws Exception {
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='t' layout='table' columnWidths='100pt 150pt'>"
                        + "  <subform name='row1' layout='row'>"
                        + "    <field name='a1' h='20pt'><value><text/></value></field>"
                        + "    <field name='b1' h='20pt'><value><text/></value></field></subform>"
                        + "  <subform name='row2' layout='row'>"
                        + "    <field name='a2' h='30pt'><value><text/></value></field>"
                        + "    <field name='b2' h='30pt'><value><text/></value></field></subform>"
                        + "  <subform name='row3' layout='row'>"
                        + "    <field name='a3' h='10pt'><value><text/></value></field>"
                        + "    <field name='b3' h='10pt'><value><text/></value></field></subform>"
                        + "</subform>",
                "layout='tb'"), null);

        XfaLayoutNode table = r.root.getChildren().get(0);
        List<XfaLayoutNode> rows = table.getChildren();
        assertEquals(3, rows.size());
        // rows stack at cumulative Y
        assertEquals(0, rows.get(0).getY(), EPS, "row1 Y");
        assertEquals(20, rows.get(1).getY(), EPS, "row2 Y = after row1");
        assertEquals(50, rows.get(2).getY(), EPS, "row3 Y = after row1+row2");
        assertEquals(60, table.getHeight(), EPS, "table height = Σ row heights");
        // columns aligned across rows: each row's two cells at x=0 and x=100, widths 100/150
        for (XfaLayoutNode row : rows) {
            assertEquals(0, row.getChildren().get(0).getX(), EPS, "col0 aligned");
            assertEquals(100, row.getChildren().get(1).getX(), EPS, "col1 aligned (inherited table cols)");
            assertEquals(100, row.getChildren().get(0).getWidth(), EPS);
            assertEquals(150, row.getChildren().get(1).getWidth(), EPS);
        }
    }

    /* -------------------------------- lr-tb -------------------------------- */

    @Test
    void leftRightTopBottomWrapsAtRegionWidth() throws Exception {
        // box width 250; four 100pt cells ⇒ 2 per line, wrap after the second.
        XfaFlowLayout.Result r = layout(wrap(
                "<subform name='box' layout='lr-tb' w='250pt'>"
                        + "  <field name='c0' w='100pt' h='20pt'><value><text/></value></field>"
                        + "  <field name='c1' w='100pt' h='20pt'><value><text/></value></field>"
                        + "  <field name='c2' w='100pt' h='20pt'><value><text/></value></field>"
                        + "  <field name='c3' w='100pt' h='20pt'><value><text/></value></field>"
                        + "</subform>",
                "layout='tb'"), null);

        XfaLayoutNode box = r.root.getChildren().get(0);
        List<XfaLayoutNode> cells = box.getChildren();
        assertEquals(0, cells.get(0).getX(), EPS);
        assertEquals(0, cells.get(0).getY(), EPS);
        assertEquals(100, cells.get(1).getX(), EPS, "second fits on line 1");
        assertEquals(0, cells.get(1).getY(), EPS);
        assertEquals(0, cells.get(2).getX(), EPS, "third wraps to line 2");
        assertEquals(20, cells.get(2).getY(), EPS, "line 2 = line height");
        assertEquals(100, cells.get(3).getX(), EPS);
        assertEquals(20, cells.get(3).getY(), EPS);
        assertEquals(40, box.getHeight(), EPS, "two lines of 20pt");
    }

    /* helpers */

    private static XfaFlowLayout.Result layout(String templateXml, String dataXml) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(templateXml));
        XfaNode data = dataXml == null ? null : XfaNodeFactory.load(parse(dataXml));
        FormDom dom = new BindingEngine().merge(tpl, data);
        return XfaFlowLayout.layout(dom, tpl);
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
