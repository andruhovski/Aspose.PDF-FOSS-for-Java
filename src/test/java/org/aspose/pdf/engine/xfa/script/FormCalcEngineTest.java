package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B2 — FormCalc engine (lexer/parser/evaluator + routing). Asserts the spec worked-example results
 * (objective oracle: the FormCalc spec gives exact outputs), the SOM-bridged aggregates over real
 * data, and the contentType routing (FormCalc→FormCalc engine, JS→JS-0; the FormCalc-as-JS
 * SyntaxError cleared).
 */
public class FormCalcEngineTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";

    /* ------------------------------ B2.2 — spec worked examples (exact) ------------------------------ */

    @Test
    void arithmeticAndPrecedence() {
        assertEquals(7.0, fc("3 + 4"));
        assertEquals(14.0, fc("2 + 3 * 4"), "multiplication binds tighter");
        assertEquals(20.0, fc("(2 + 3) * 4"), "parentheses override");
        assertEquals(2.5, fc("5 / 2"));
        assertEquals(0.0, fc("5 / 0"), "FormCalc guards divide-by-zero to a value");
        assertEquals(-3.0, fc("-3"));
    }

    @Test
    void comparisonAndLogical() {
        assertEquals(1.0, fc("5 eq 5"));
        assertEquals(0.0, fc("5 ne 5"));
        assertEquals(1.0, fc("3 lt 4"));
        assertEquals(1.0, fc("5 == 5"));
        assertEquals(1.0, fc("4 <> 5"));
        assertEquals(1.0, fc("(1 eq 1) and (2 eq 2)"));
        assertEquals(0.0, fc("(1 eq 1) and (2 eq 3)"));
        assertEquals(1.0, fc("(1 eq 2) or (3 eq 3)"));
        assertEquals(1.0, fc("not (1 eq 2)"));
    }

    @Test
    void stringConcatAndBuiltins() {
        assertEquals("Tony Blue", fc("\"Tony\" & \" \" & \"Blue\""));
        assertEquals("abc", fc("Concat(\"a\", \"b\", \"c\")"));
        assertEquals(5.0, fc("Len(\"hello\")"));
        assertEquals(3.0, fc("Abs(-3)"));
        assertEquals(3.14, fc("Round(3.14159, 2)"));
    }

    @Test
    void aggregatesOverScalars() {
        assertEquals(6.0, fc("Sum(1, 2, 3)"), "spec: Sum(1,2,3) = 6");
        assertEquals(4.0, fc("Avg(2, 4, 6)"), "spec: Avg = 4");
        assertEquals(1.0, fc("Min(3, 1, 2)"));
        assertEquals(3.0, fc("Max(3, 1, 2)"));
        assertEquals(3.0, fc("Count(1, 2, 3)"));
    }

    @Test
    void ifThenElseAndElseif() {
        assertEquals("Y", fc("if (1 eq 1) then \"Y\" else \"N\" endif"));
        assertEquals("N", fc("if (1 eq 2) then \"Y\" else \"N\" endif"));
        assertEquals("mid", fc("if (5 gt 9) then \"hi\" elseif (5 gt 3) then \"mid\" else \"lo\" endif"));
        assertEquals("lo", fc("if (1 gt 9) then \"hi\" elseif (1 gt 3) then \"mid\" else \"lo\" endif"));
    }

    @Test
    void num2dateDate2numRoundTrip() {
        // Date2Num then Num2Date returns the same calendar date (epoch 1900-01-01).
        assertEquals("2020-01-15",
                fc("Num2Date(Date2Num(\"2020-01-15\", \"YYYY-MM-DD\"), \"YYYY-MM-DD\")"));
        assertEquals("15.01.2020",
                fc("Num2Date(Date2Num(\"2020-01-15\", \"YYYY-MM-DD\"), \"DD.MM.YYYY\")"));
    }

    @Test
    void expressionListReturnsLastValue() {
        assertEquals(9.0, fc("3 + 1; 7 - 2; 4 + 5"), "value of a list is its last expression");
    }

    /* ------------------------------ B2.2 — SOM-bridged over real data ------------------------------ */

    @Test
    void productOfSiblingFields() throws Exception {
        FormDom dom = merge(form(numField("numQty", null), numField("numUnitPrice", null),
                numField("numAmount", null)), data("numQty", "6", "numUnitPrice", "7"));
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode total = host.wrap(dom.fieldByName("numAmount").getFormNode());
        assertEquals(42.0, host.runFormCalc("numQty * numUnitPrice", total), "6 * 7 via scope refs");
    }

    @Test
    void sumOverRepeatedRowsSetAxis() throws Exception {
        String tpl = "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + "<subform name='Detail'><occur min='0' max='-1'/>" + numField("amount", null) + "</subform>"
                + numField("Total", null) + "</subform></template>";
        String dataXml = "<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Detail><amount>10</amount></Detail><Detail><amount>20</amount></Detail>"
                + "<Detail><amount>30</amount></Detail><Total></Total></form1></xfa:data>";
        FormDom dom = merge(tpl, dataXml);
        XfaScriptHost host = new XfaScriptHost(dom);
        XfaScriptNode total = host.wrap(dom.fieldByName("Total").getFormNode());
        assertEquals(60.0, host.runFormCalc("Sum(Detail[*].amount)", total), "10+20+30");
        assertEquals(3.0, host.runFormCalc("Count(Detail[*].amount)", total));
        assertEquals(20.0, host.runFormCalc("Avg(Detail[*].amount)", total));
        assertEquals(1.0, host.runFormCalc("Exists(Detail)", total));
        assertEquals(0.0, host.runFormCalc("Exists(NoSuchNode)", total));
    }

    /* ------------------------------ B2.3 — routing ------------------------------ */

    @Test
    void untypedFormCalcCalculateComputesAndFlows() throws Exception {
        // No contentType → XFA spec default FormCalc. Previously mis-run as JS (SyntaxError on `endif`).
        FormDom dom = merge(form(numField("numQty", null), numField("numUnitPrice", null),
                calcFcNumField("numAmount", "numQty * numUnitPrice")),
                data("numQty", "6", "numUnitPrice", "7"));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.formCalcFailed, "FormCalc ran clean: " + r.errors);
        assertTrue(r.formCalc >= 1, "routed to FormCalc");
        assertEquals("42", dom.fieldByName("numAmount").getValue(), "computed value written");
    }

    @Test
    void formCalcIfThenNoLongerSyntaxErrors() throws Exception {
        // The B3-DIAG "FormCalc-as-JS SyntaxError" shape: if…then…else…endif now parses as FormCalc.
        FormDom dom = merge(form(numField("score", null),
                calcFcTxtField("grade", "if (score ge 50) then \"PASS\" else \"FAIL\" endif")),
                data("score", "72"));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.formCalcFailed, "no SyntaxError: " + r.errors);
        assertEquals("PASS", dom.fieldByName("grade").getValue());
    }

    @Test
    void javascriptCalculateStillRoutesToJs() throws Exception {
        // An explicit-JS calculate must keep running on JS-0 (no mis-route to FormCalc).
        FormDom dom = merge(form(numField("score", null),
                calcTxtField("grade", "score.rawValue >= 50 ? \"PASS\" : \"FAIL\"")),
                data("score", "72"));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.formCalc, "JS not counted as FormCalc");
        assertEquals(0, r.calculatesFailed, "JS ran: " + r.errors);
        assertEquals("PASS", dom.fieldByName("grade").getValue());
    }

    @Test
    void dollarAssignmentWritesCurrentField() throws Exception {
        FormDom dom = merge(form(numField("a", null), numField("b", null),
                calcFcNumField("c", "$ = a + b")), data("a", "5", "b", "8"));
        XfaScripting.Result r = XfaScripting.execute(dom);
        assertEquals(0, r.formCalcFailed, r.errors.toString());
        assertEquals("13", dom.fieldByName("c").getValue(), "$ = a + b writes the field");
    }

    /* ------------------------------ helpers ------------------------------ */

    /** Evaluates a pure FormCalc expression against a throwaway one-field form. */
    private static Object fc(String expr) {
        try {
            FormDom dom = merge(form(numField("dummy", null)), null);
            XfaScriptHost host = new XfaScriptHost(dom);
            return host.runFormCalc(expr, host.formRoot());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String numField(String name, String calc) {
        return "<field name='" + name + "'><ui><numericEdit/></ui><value><decimal/></value></field>";
    }

    private static String calcFcNumField(String name, String formcalc) {
        return "<field name='" + name + "'><ui><numericEdit/></ui>"
                + "<calculate><script>" + esc(formcalc) + "</script></calculate></field>";
    }

    private static String calcFcTxtField(String name, String formcalc) {
        return "<field name='" + name + "'><ui><textEdit/></ui><value><text/></value>"
                + "<calculate><script>" + esc(formcalc) + "</script></calculate></field>";
    }

    private static String calcTxtField(String name, String js) {
        return "<field name='" + name + "'><ui><textEdit/></ui><value><text/></value>"
                + "<calculate><script contentType='application/x-javascript'>" + esc(js)
                + "</script></calculate></field>";
    }

    private static String form(String... fields) {
        return "<template xmlns='" + TPL + "'><subform name='form1' layout='tb'>"
                + String.join("", fields) + "</subform></template>";
    }

    private static String data(String... kv) {
        StringBuilder sb = new StringBuilder("<xfa:data xmlns:xfa='" + DATA + "'><form1>");
        for (int i = 0; i + 1 < kv.length; i += 2) {
            sb.append('<').append(kv[i]).append('>').append(kv[i + 1]).append("</").append(kv[i]).append('>');
        }
        return sb.append("</form1></xfa:data>").toString();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static FormDom merge(String tplXml, String dataXml) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(tplXml));
        XfaNode data = dataXml == null ? null : XfaNodeFactory.load(parse(dataXml));
        return new BindingEngine().merge(tpl, data);
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
