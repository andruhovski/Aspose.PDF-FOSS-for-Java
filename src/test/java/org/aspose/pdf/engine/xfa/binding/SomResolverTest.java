package org.aspose.pdf.engine.xfa.binding;

import org.aspose.pdf.engine.xfa.binding.som.SomExpr;
import org.aspose.pdf.engine.xfa.binding.som.SomParser;
import org.aspose.pdf.engine.xfa.binding.som.SomResolver;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// A4.1: SOM resolver — spec-example notations over the typed model.
public class SomResolverTest {

    private static final String TPL_NS = XfaNode.TEMPLATE_NS;
    private static final String DATA_NS = "http://www.xfa.org/schema/xfa-data/1.0/";

    private final SomResolver som = new SomResolver();

    // A data tree mirroring the spec "Receipt" example.
    private XfaNode data() throws Exception {
        return wrap("<xfa:data xmlns:xfa='" + DATA_NS + "'>"
                + "<Receipt>"
                + "<Detail><Total_Price>10</Total_Price></Detail>"
                + "<Detail><Total_Price>20</Total_Price></Detail>"
                + "<Detail><Total_Price>30</Total_Price></Detail>"
                + "<Total_Price>60</Total_Price>"
                + "</Receipt></xfa:data>");
    }

    private XfaNode template() throws Exception {
        return wrap("<template xmlns='" + TPL_NS + "'>"
                + "<subform name='Receipt'>"
                + "<subform name='Detail'><field name='Total_Price'/></subform>"
                + "<field name='Total_Price'/>"
                + "</subform></template>");
    }

    @Test
    void dottedPathSingleNode() throws Exception {
        SomResolver.Context ctx = new SomResolver.Context();
        ctx.data = data();
        XfaNode n = som.resolveNode("$data.Receipt.Total_Price", ctx);
        assertNotNull(n);
        assertEquals("60", n.getTextContent());  // the Receipt-level Total_Price (first match)
    }

    @Test
    void allSiblingsWithStar() throws Exception {
        SomResolver.Context ctx = new SomResolver.Context();
        ctx.data = data();
        // Detail[*].Total_Price -> the three detail totals (the list)
        List<XfaNode> totals = som.resolveNodes("$data.Receipt.Detail[*].Total_Price", ctx);
        assertEquals(3, totals.size());
        assertEquals("10", totals.get(0).getTextContent());
        assertEquals("30", totals.get(2).getTextContent());
    }

    @Test
    void numericIndexSelectsOccurrence() throws Exception {
        SomResolver.Context ctx = new SomResolver.Context();
        ctx.data = data();
        XfaNode d = som.resolveNode("$data.Receipt.Detail[1].Total_Price", ctx);
        assertEquals("20", d.getTextContent());
        assertNull(som.resolveNode("$data.Receipt.Detail[9].Total_Price", ctx), "out-of-range -> null");
    }

    @Test
    void propertySyntaxName() throws Exception {
        SomResolver.Context ctx = new SomResolver.Context();
        ctx.template = template();
        assertEquals("Detail", som.resolveValue("$template.Receipt.Detail.#name", ctx));
    }

    @Test
    void classSyntaxSubform() throws Exception {
        SomResolver.Context ctx = new SomResolver.Context();
        ctx.template = template();
        // #subform[0] under Receipt -> the Detail subform
        XfaNode n = som.resolveNode("$template.Receipt.#subform[0]", ctx);
        assertNotNull(n);
        assertEquals("Detail", n.getName());
    }

    @Test
    void classSyntaxDataValueVsDataGroup() throws Exception {
        SomResolver.Context ctx = new SomResolver.Context();
        ctx.data = data();
        // Receipt has Detail (group) x3 + Total_Price (value)
        List<XfaNode> groups = som.resolveNodes("$data.Receipt.#dataGroup[*]", ctx);
        assertEquals(3, groups.size());
        List<XfaNode> values = som.resolveNodes("$data.Receipt.#dataValue[*]", ctx);
        assertEquals(1, values.size());
        assertEquals("Total_Price", values.get(0).getElementName());
    }

    @Test
    void relativeAndCurrentScope() throws Exception {
        XfaNode data = data();
        XfaNode receipt = data.getChild("Receipt");
        SomResolver.Context ctx = SomResolver.Context.of(receipt);
        ctx.data = data;
        // relative (no root): Detail[2].Total_Price from Receipt
        assertEquals("30", som.resolveValue("Detail[2].Total_Price", ctx));
        // $ current
        assertEquals("Receipt", som.resolveNode("$", ctx).getElementName());
    }

    @Test
    void parentNavigation() throws Exception {
        XfaNode data = data();
        XfaNode firstDetail = data.getChild("Receipt").getChildren("Detail").get(0);
        SomResolver.Context ctx = SomResolver.Context.of(firstDetail);
        // ..  -> Receipt
        assertEquals("Receipt", som.resolveNode("..", ctx).getElementName());
    }

    @Test
    void structuralPredicateFilters() throws Exception {
        SomResolver.Context ctx = new SomResolver.Context();
        ctx.data = data();
        // Detail whose Total_Price == 20
        List<XfaNode> r = som.resolveNodes("$data.Receipt.Detail[Total_Price == 20]", ctx);
        assertEquals(1, r.size());
        assertEquals("20", r.get(0).getChild("Total_Price").getTextContent());
        // comparison
        List<XfaNode> gt = som.resolveNodes("$data.Receipt.Detail[Total_Price > 15]", ctx);
        assertEquals(2, gt.size());
    }

    @Test
    void scriptPredicateFlaggedNotEvaluated() {
        SomExpr e = som.parse("Detail[Sum(Total_Price) > 0]");
        assertTrue(e.hasScriptPredicate(), "function-call predicate flagged as script (deferred)");
    }

    @Test
    void unresolvedPathDegradesToEmpty() throws Exception {
        SomResolver.Context ctx = new SomResolver.Context();
        ctx.data = data();
        assertTrue(som.resolveNodes("$data.Nope.Missing", ctx).isEmpty());
        assertNull(som.resolveNode("$data.Nope", ctx));
    }

    @Test
    void parserRoundTripsRootAndSteps() {
        SomParser p = new SomParser(new HashSet<>(java.util.Arrays.asList("subform", "field")));
        SomExpr e = p.parse("$data.Receipt.Detail[*]");
        assertEquals(SomExpr.Root.DATA, e.getRoot());
        assertEquals(2, e.getSteps().size()); // Receipt, Detail[*] ($data is the root, not a step)
    }

    private static XfaNode wrap(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        Document doc = f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return XfaNodeFactory.load(doc);
    }
}
