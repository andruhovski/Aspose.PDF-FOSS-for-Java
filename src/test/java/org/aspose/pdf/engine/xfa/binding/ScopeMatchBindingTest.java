package org.aspose.pdf.engine.xfa.binding;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A4-FIX (FIX.2): exercises the automatic-binding scope-matching <em>mechanism</em>
 * beyond the two spec repros, so a patch that merely hard-codes Examples 4.51-4.53
 * cannot pass. Each case is a node-for-node assertion of the precedence rules from
 * XFA 3.0 "Basic Data Binding to Produce the XFA Form DOM" (pp.180-183):
 * direct &gt; ancestor &gt; sibling, fewer generations ascended wins, a data value
 * binds to at most one field (consumed / index inferral).
 */
public class ScopeMatchBindingTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";

    private final BindingEngine engine = new BindingEngine();

    /** Ancestor match through TWO unbound subform generations (deeper than the repro). */
    @Test
    void ancestorMatchThroughTwoGenerations() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='doc'>"
                + "<subform name='a'><subform name='b'>"
                + "  <field name='street'><value><text/></value></field>"
                + "</subform></subform></subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<doc><street>99 Candlestick Lane</street></doc></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("99 Candlestick Lane", dom.fieldByPath("doc.a.b.street").getValue(),
                "field bound through two transparent (unbound) subforms to the ancestor data");
    }

    /**
     * Ancestor-vs-sibling precedence: {@code val} is reachable BOTH as a child of the
     * bound ancestor (ancestor match) and as a sibling of the bound container's data
     * (sibling match). The ancestor match must win (spec: ancestor preferable).
     */
    @Test
    void ancestorMatchPreferredOverSiblingMatch() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='doc'>"
                + "<subform name='box'><subform name='wrap'>"
                + "  <field name='val'><value><text/></value></field>"
                + "</subform></subform></subform></template>");
        // box binds <box>; val exists inside box (ancestor, via transparent wrap) AND
        // at doc level (sibling of box).
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<doc><val>SIBLING</val><box><val>ANCESTOR</val></box></doc></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("ANCESTOR", dom.fieldByPath("doc.box.wrap.val").getValue(),
                "ancestor match (inside bound box) beats the sibling match at doc level");
    }

    /**
     * Fewer-generations tie-breaker among sibling matches: {@code y} is reachable by
     * ascending one generation (parent {@code a}) and two ({@code doc}); the nearer
     * (one generation) must win.
     */
    @Test
    void nearerSiblingMatchWinsOverFartherOne() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='doc'>"
                + "<subform name='a'><subform name='b'>"
                + "  <field name='y'><value><text/></value></field>"
                + "</subform></subform></subform></template>");
        // b binds <b> (has z); y not in b -> sibling ascent finds y at a (gen1) and doc (gen2)
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<doc><y>GRANDPARENT</y><a><y>PARENT</y><b><z>filler</z></b></a></doc></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("PARENT", dom.fieldByPath("doc.a.b.y").getValue(),
                "nearer sibling (one generation up) wins over the farther one");
    }

    /**
     * Index inferral / single-binding: two same-named sibling subforms over two data
     * records bind to DISTINCT records in order (no cross-binding, no double-consume).
     */
    @Test
    void repeatedSiblingSubformsBindDistinctRecords() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='doc'>"
                + "<subform name='rec'><field name='v'><value><text/></value></field></subform>"
                + "<subform name='rec'><field name='v'><value><text/></value></field></subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<doc><rec><v>first</v></rec><rec><v>second</v></rec></doc></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        List<String> vs = new ArrayList<>();
        for (FormField f : dom.getFields()) {
            if ("v".equals(f.getName())) {
                vs.add(f.getValue());
            }
        }
        assertEquals(List.of("first", "second"), vs,
                "each repeated subform consumes a distinct data record, in order");
    }

    /**
     * Direct match co-exists with a scope match for the same name (spec's "common"
     * case): the direct field consumes the first value; the scope field, finding it
     * consumed, binds the OTHER same-named value rather than re-binding the first.
     */
    @Test
    void directMatchCoexistsWithScopeMatch() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='doc'>"
                + "<field name='item'><value><text/></value></field>"
                + "<subform name='box'><field name='item'><value><text/></value></field></subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'>"
                + "<doc><item>first</item><item>second</item></doc></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("first", dom.fieldByPath("doc.item").getValue(), "direct field takes the first item");
        assertEquals("second", dom.fieldByPath("doc.box.item").getValue(),
                "scope field binds the other item (direct match consumed the first)");
    }

    /**
     * A4-DIAG repro: deep relative-{@code dataRef} chain through {@code match="none"}
     * layout subforms whose names do NOT mirror the data groups — the FormB101 shape.
     * Each container/field binds via {@code $record.X} (record-relative) then {@code $.Y}
     * (current-context-relative), threading the bound data node down 5 levels. Before
     * the A4-DIAG fix this bound nothing ({@code $record} resolved to the datasets root
     * and {@code match="none"} nulled the inherited context).
     */
    @Test
    void deepRelativeDataRefChainThroughLayoutSubforms() throws Exception {
        Template tpl = tpl("<template xmlns='" + TPL + "'><subform name='form1'>"
                + "<subform name='Page'><bind match='none'/>"
                + "  <subform name='Sect'><bind match='none'/>"
                + "    <subform name='Debtor'><bind match='dataRef' ref='$record.Debtor'/>"
                + "      <subform name='Person'><bind match='dataRef' ref='$.Person'/>"
                + "        <field name='Given'><bind match='dataRef' ref='$.Given'/><value><text/></value></field>"
                + "        <field name='Sur'><bind match='dataRef' ref='$.Sur'/><value><text/></value></field>"
                + "      </subform>"
                + "    </subform>"
                + "  </subform>"
                + "</subform>"
                + "<subform name='Hdr'><bind match='none'/>"
                + "  <field name='Court'><bind match='dataRef' ref='Case.CourtName'/><value><text/></value></field>"
                + "</subform>"
                + "</subform></template>");
        XfaNode data = data("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Case><CourtName>US Bankruptcy Court</CourtName></Case>"
                + "<Debtor><Person><Given>Jack</Given><Sur>Spratt</Sur></Person></Debtor>"
                + "</form1></xfa:data>");
        FormDom dom = engine.merge(tpl, data);
        assertEquals("Jack", dom.fieldByName("Given").getValue(), "deep $.-relative dataRef binds");
        assertEquals("Spratt", dom.fieldByName("Sur").getValue());
        assertEquals(FormField.BindingKind.DATAREF, dom.fieldByName("Given").getKind());
        assertEquals("US Bankruptcy Court", dom.fieldByName("Court").getValue(),
                "bare relative dataRef resolves against the record through match=none subforms");
    }

    /* ----------------------------- helpers -------------------------- */

    private static Template tpl(String xml) throws Exception {
        return (Template) XfaNodeFactory.load(parse(xml));
    }

    private static XfaNode data(String xml) throws Exception {
        return XfaNodeFactory.load(parse(xml));
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
