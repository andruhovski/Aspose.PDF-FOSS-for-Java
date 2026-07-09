package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.Document;
import org.aspose.pdf.forms.Field;
import org.aspose.pdf.forms.Form;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A5.2/A5.3: Form DOM node -> AcroForm field mapping, assembly, and round-trip. */
public class XfaFlattenerTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;
    private static final String DATA = "http://www.xfa.org/schema/xfa-data/1.0/";

    private static final String TEMPLATE = "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
            + "<field name='Name' x='10pt' y='10pt' w='100pt' h='20pt'><ui><textEdit/></ui><value><text/></value></field>"
            + "<field name='Agree' x='10pt' y='40pt' w='20pt' h='20pt'><ui><checkButton/></ui><value><text/></value></field>"
            + "<field name='Color' x='10pt' y='70pt' w='100pt' h='20pt'><ui><choiceList/></ui>"
            + "  <items><text>Red</text><text>Green</text><text>Blue</text></items><value><text/></value></field>"
            + "<field name='Sig' x='10pt' y='110pt' w='100pt' h='40pt'><ui><signature/></ui></field>"
            + "<exclGroup name='Gender' x='10pt' y='160pt'>"
            + "  <field name='M' x='0pt' y='0pt' w='20pt' h='20pt'><ui><checkButton/></ui></field>"
            + "  <field name='F' x='30pt' y='0pt' w='20pt' h='20pt'><ui><checkButton/></ui></field>"
            + "</exclGroup>"
            + "</subform></template>";

    private static final String DATASETS = "<xfa:data xmlns:xfa='" + DATA + "'><form1>"
            + "<Name>Alice</Name><Agree>1</Agree><Color>Green</Color></form1></xfa:data>";

    @Test
    void mapsEachUiTypeToCorrectFieldClassWithValues() throws Exception {
        FormDom dom = merge();
        Document doc = new Document();
        XfaFlattener.Result r = XfaFlattener.flatten(doc, dom, XfaFlattener.XfaPolicy.DROP, null);

        // one AcroForm field per leaf + one radio group for the exclGroup (children folded in)
        assertEquals(Integer.valueOf(1), r.byType.get("TextBoxField"));
        assertEquals(Integer.valueOf(1), r.byType.get("CheckboxField"));
        assertEquals(Integer.valueOf(1), r.byType.get("ComboBoxField"));
        assertEquals(Integer.valueOf(1), r.byType.get("SignatureField"));
        assertEquals(Integer.valueOf(1), r.byType.get("RadioButtonField"), "exclGroup -> one radio group");
        assertEquals(5, r.fieldsAdded);
        assertTrue(r.boundValuesCarried >= 3, "Name/Agree/Color values carried; got " + r.boundValuesCarried);
        assertEquals(0, r.geometryFallback, "all fields had static x/y -> resolved geometry");

        Form form = doc.getForm();
        assertEquals("Alice", form.get("form1.Name").getValue());
        assertEquals("Green", form.get("form1.Color").getValue());
        assertNotNull(form.get("form1.Gender"), "radio group present");
    }

    @Test
    void droppedXfaStillOpensWithFieldsAndValuesAfterReload() throws Exception {
        FormDom dom = merge();
        Document doc = new Document();
        XfaFlattener.flatten(doc, dom, XfaFlattener.XfaPolicy.DROP, null);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.save(bos);

        try (Document reopened = new Document(new ByteArrayInputStream(bos.toByteArray()))) {
            Form form = reopened.getForm();
            assertTrue(form.getCount() >= 5, "fields survive save/reload; got " + form.getCount());
            Field name = form.get("form1.Name");
            assertNotNull(name, "Name field present after reload");
            assertEquals("Alice", name.getValue(), "value stable across save/reload");
            assertEquals("Green", form.get("form1.Color").getValue());
        }
    }

    /** A5.2: exclGroup -> one radio group with N options and the CHOSEN one marked on,
     *  plus XFA caption -> AcroForm tooltip (/TU). */
    @Test
    void exclGroupRadioMarksChosenOptionAndCaptionTooltip() throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse("<template xmlns='" + TPL + "'>"
                + "<subform name='form1' x='0pt' y='0pt'>"
                + "<exclGroup name='Gender' x='10pt' y='10pt'>"
                + "  <caption><value><text>Gender</text></value></caption>"
                + "  <field name='M' x='0pt' y='0pt' w='12pt' h='12pt'><value><text>male</text></value></field>"
                + "  <field name='F' x='20pt' y='0pt' w='12pt' h='12pt'><value><text>female</text></value></field>"
                + "</exclGroup>"
                + "<field name='Name' x='10pt' y='40pt' w='100pt' h='20pt'><ui><textEdit/></ui>"
                + "  <caption><value><text>Full Name</text></value></caption><value><text/></value></field>"
                + "</subform></template>"));
        XfaNode data = XfaNodeFactory.load(parse("<xfa:data xmlns:xfa='" + DATA + "'><form1>"
                + "<Gender>female</Gender><Name>Alice</Name></form1></xfa:data>"));
        Document doc = new Document();
        XfaFlattener.Result r = XfaFlattener.flatten(doc, new BindingEngine().merge(tpl, data),
                XfaFlattener.XfaPolicy.DROP, null);
        assertEquals(Integer.valueOf(1), r.byType.get("RadioButtonField"), "one radio group");

        org.aspose.pdf.forms.RadioButtonField radio =
                (org.aspose.pdf.forms.RadioButtonField) doc.getForm().get("form1.Gender");
        assertNotNull(radio, "radio group present");
        assertEquals(2, radio.getOptions().size(), "two options");
        assertEquals(1, radio.getSelected(), "option 2 (female) is on");
        assertEquals("Gender", radio.getAlternateName(), "exclGroup caption -> tooltip");

        Field name = doc.getForm().get("form1.Name");
        assertEquals("Alice", name.getValue());
        assertEquals("Full Name", name.getAlternateName(), "field caption -> tooltip");
    }

    /* helpers */

    private static FormDom merge() throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(TEMPLATE));
        XfaNode data = XfaNodeFactory.load(parse(DATASETS));
        return new BindingEngine().merge(tpl, data);
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
