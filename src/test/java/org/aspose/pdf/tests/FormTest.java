package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.forms.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for form (AcroForm) classes.
 */
public class FormTest {

    // ── Helper: build a minimal AcroForm dict ──

    private PdfDictionary buildAcroForm(PdfDictionary... fieldDicts) {
        PdfArray fields = new PdfArray();
        for (PdfDictionary fd : fieldDicts) fields.add(fd);
        PdfDictionary acro = new PdfDictionary();
        acro.set(PdfName.of("Fields"), fields);
        return acro;
    }

    private PdfDictionary buildTextField(String name, String value) {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("FT"), PdfName.of("Tx"));
        d.set(PdfName.of("T"), new PdfString(name.getBytes()));
        if (value != null) d.set(PdfName.of("V"), new PdfString(value.getBytes()));
        d.set(PdfName.of("Subtype"), PdfName.of("Widget"));
        return d;
    }

    private PdfDictionary buildCheckbox(String name, boolean checked) {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("FT"), PdfName.of("Btn"));
        d.set(PdfName.of("T"), new PdfString(name.getBytes()));
        d.set(PdfName.of("V"), PdfName.of(checked ? "Yes" : "Off"));
        d.set(PdfName.of("Subtype"), PdfName.of("Widget"));
        // AP/N for on value
        PdfDictionary ap = new PdfDictionary();
        PdfDictionary n = new PdfDictionary();
        n.set(PdfName.of("Yes"), new PdfDictionary());
        n.set(PdfName.of("Off"), new PdfDictionary());
        ap.set(PdfName.of("N"), n);
        d.set(PdfName.of("AP"), ap);
        return d;
    }

    private PdfDictionary buildComboBox(String name, String value, String... options) {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("FT"), PdfName.of("Ch"));
        d.set(PdfName.of("Ff"), PdfInteger.valueOf(1 << 17)); // combo flag
        d.set(PdfName.of("T"), new PdfString(name.getBytes()));
        if (value != null) d.set(PdfName.of("V"), new PdfString(value.getBytes()));
        PdfArray opt = new PdfArray();
        for (String o : options) opt.add(new PdfString(o.getBytes()));
        d.set(PdfName.of("Opt"), opt);
        d.set(PdfName.of("Subtype"), PdfName.of("Widget"));
        return d;
    }

    // ── Form basics ──

    @Test
    public void testEmptyForm() {
        Form form = new Form(new PdfDictionary(), null, null);
        assertEquals(0, form.getCount());
    }

    @Test
    public void testFormFieldCount() {
        PdfDictionary acro = buildAcroForm(
                buildTextField("field1", "hello"),
                buildTextField("field2", "world"),
                buildTextField("field3", null)
        );
        Form form = new Form(acro, null, null);
        assertEquals(3, form.getCount());
    }

    @Test
    public void testFormGetByName() {
        PdfDictionary acro = buildAcroForm(buildTextField("myField", "value"));
        Form form = new Form(acro, null, null);
        Field f = form.get("myField");
        assertNotNull(f);
        assertEquals("value", f.getValue());
    }

    @Test
    public void testFormGetByIndex() {
        PdfDictionary acro = buildAcroForm(
                buildTextField("a", "1"),
                buildTextField("b", "2")
        );
        Form form = new Form(acro, null, null);
        assertEquals("a", form.get(1).getPartialName());
        assertEquals("b", form.get(2).getPartialName());
    }

    @Test
    public void testFormOneBasedIndex() {
        PdfDictionary acro = buildAcroForm(buildTextField("f", "v"));
        Form form = new Form(acro, null, null);
        assertThrows(IndexOutOfBoundsException.class, () -> form.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> form.get(2));
    }

    @Test
    public void testFormIteration() {
        PdfDictionary acro = buildAcroForm(
                buildTextField("a", "1"),
                buildTextField("b", "2"),
                buildTextField("c", "3")
        );
        Form form = new Form(acro, null, null);
        int count = 0;
        for (Field f : form) {
            assertNotNull(f);
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testFormType() {
        PdfDictionary acro = buildAcroForm();
        Form form = new Form(acro, null, null);
        assertEquals(Form.FormType.Standard, form.getType());
    }

    // ── Field factory dispatch ──

    @Test
    public void testTextBoxFieldFromFactory() {
        PdfDictionary acro = buildAcroForm(buildTextField("text1", "hello"));
        Form form = new Form(acro, null, null);
        Field f = form.get(1);
        assertTrue(f instanceof TextBoxField, "Expected TextBoxField, got " + f.getClass().getSimpleName());
    }

    @Test
    public void testCheckboxFieldFromFactory() {
        PdfDictionary acro = buildAcroForm(buildCheckbox("check1", true));
        Form form = new Form(acro, null, null);
        Field f = form.get(1);
        assertTrue(f instanceof CheckboxField, "Expected CheckboxField, got " + f.getClass().getSimpleName());
    }

    @Test
    public void testComboBoxFieldFromFactory() {
        PdfDictionary acro = buildAcroForm(buildComboBox("combo1", "B", "A", "B", "C"));
        Form form = new Form(acro, null, null);
        Field f = form.get(1);
        assertTrue(f instanceof ComboBoxField, "Expected ComboBoxField, got " + f.getClass().getSimpleName());
    }

    @Test
    public void testRadioButtonFromFactory() {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("FT"), PdfName.of("Btn"));
        d.set(PdfName.of("Ff"), PdfInteger.valueOf(1 << 15)); // radio flag
        d.set(PdfName.of("T"), new PdfString("radio1".getBytes()));
        d.set(PdfName.of("Subtype"), PdfName.of("Widget"));
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1) instanceof RadioButtonField);
    }

    @Test
    public void testButtonFieldFromFactory() {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("FT"), PdfName.of("Btn"));
        d.set(PdfName.of("Ff"), PdfInteger.valueOf(1 << 16)); // push button flag
        d.set(PdfName.of("T"), new PdfString("button1".getBytes()));
        d.set(PdfName.of("Subtype"), PdfName.of("Widget"));
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1) instanceof ButtonField);
    }

    @Test
    public void testListBoxFieldFromFactory() {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("FT"), PdfName.of("Ch"));
        // No combo flag → ListBox
        d.set(PdfName.of("T"), new PdfString("list1".getBytes()));
        d.set(PdfName.of("Subtype"), PdfName.of("Widget"));
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1) instanceof ListBoxField);
    }

    @Test
    public void testSignatureFieldFromFactory() {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("FT"), PdfName.of("Sig"));
        d.set(PdfName.of("T"), new PdfString("sig1".getBytes()));
        d.set(PdfName.of("Subtype"), PdfName.of("Widget"));
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1) instanceof SignatureField);
    }

    // ── Field properties ──

    @Test
    public void testFieldGetSetValue() {
        PdfDictionary acro = buildAcroForm(buildTextField("f", "old"));
        Form form = new Form(acro, null, null);
        Field f = form.get(1);
        assertEquals("old", f.getValue());
        f.setValue("new");
        assertEquals("new", f.getValue());
    }

    @Test
    public void testFieldFullName() {
        PdfDictionary acro = buildAcroForm(buildTextField("myName", "v"));
        Form form = new Form(acro, null, null);
        assertEquals("myName", form.get(1).getFullName());
    }

    @Test
    public void testFieldPartialName() {
        PdfDictionary acro = buildAcroForm(buildTextField("partial", "v"));
        Form form = new Form(acro, null, null);
        assertEquals("partial", form.get(1).getPartialName());
    }

    @Test
    public void testFieldFlags() {
        PdfDictionary d = buildTextField("f", "v");
        d.set(PdfName.of("Ff"), PdfInteger.valueOf(1)); // ReadOnly
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1).isReadOnly());
        assertFalse(form.get(1).isRequired());
    }

    @Test
    public void testFieldRequired() {
        PdfDictionary d = buildTextField("f", "v");
        d.set(PdfName.of("Ff"), PdfInteger.valueOf(2)); // Required
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1).isRequired());
    }

    // ── TextBoxField ──

    @Test
    public void testTextBoxMultiline() {
        PdfDictionary d = buildTextField("f", "v");
        d.set(PdfName.of("Ff"), PdfInteger.valueOf(1 << 12)); // multiline
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        TextBoxField tb = (TextBoxField) form.get(1);
        assertTrue(tb.isMultiline());
    }

    @Test
    public void testTextBoxMaxLen() {
        PdfDictionary d = buildTextField("f", "v");
        d.set(PdfName.of("MaxLen"), PdfInteger.valueOf(100));
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        TextBoxField tb = (TextBoxField) form.get(1);
        assertEquals(100, tb.getMaxLen());
    }

    // ── CheckboxField ──

    @Test
    public void testCheckboxIsChecked() {
        PdfDictionary acro = buildAcroForm(buildCheckbox("cb", true));
        Form form = new Form(acro, null, null);
        CheckboxField cb = (CheckboxField) form.get(1);
        assertTrue(cb.isChecked());
    }

    @Test
    public void testCheckboxSetChecked() {
        PdfDictionary acro = buildAcroForm(buildCheckbox("cb", false));
        Form form = new Form(acro, null, null);
        CheckboxField cb = (CheckboxField) form.get(1);
        assertFalse(cb.isChecked());
        cb.setChecked(true);
        assertTrue(cb.isChecked());
        cb.setChecked(false);
        assertFalse(cb.isChecked());
    }

    @Test
    public void testCheckboxExportValue() {
        PdfDictionary acro = buildAcroForm(buildCheckbox("cb", true));
        Form form = new Form(acro, null, null);
        CheckboxField cb = (CheckboxField) form.get(1);
        assertEquals("Yes", cb.getExportValue());
    }

    // ── ComboBoxField ──

    @Test
    public void testComboBoxOptions() {
        PdfDictionary acro = buildAcroForm(buildComboBox("combo", "B", "A", "B", "C"));
        Form form = new Form(acro, null, null);
        ComboBoxField combo = (ComboBoxField) form.get(1);
        OptionCollection opts = combo.getOptions();
        assertEquals(3, opts.getCount());
        assertEquals("A", opts.get(0).getValue());
        assertEquals("B", opts.get(1).getValue());
        assertEquals("C", opts.get(2).getValue());
    }

    @Test
    public void testComboBoxSelected() {
        PdfDictionary acro = buildAcroForm(buildComboBox("combo", "B", "A", "B", "C"));
        Form form = new Form(acro, null, null);
        ComboBoxField combo = (ComboBoxField) form.get(1);
        assertEquals("B", combo.getSelected());
    }

    @Test
    public void testComboBoxSetSelected() {
        PdfDictionary acro = buildAcroForm(buildComboBox("combo", "B", "A", "B", "C"));
        Form form = new Form(acro, null, null);
        ComboBoxField combo = (ComboBoxField) form.get(1);
        combo.setSelected("C");
        assertEquals("C", combo.getSelected());
    }

    // ── OptionCollection iteration ──

    @Test
    public void testOptionIteration() {
        PdfDictionary acro = buildAcroForm(buildComboBox("combo", null, "X", "Y"));
        Form form = new Form(acro, null, null);
        ComboBoxField combo = (ComboBoxField) form.get(1);
        int count = 0;
        for (Option opt : combo.getOptions()) {
            assertNotNull(opt.getValue());
            count++;
        }
        assertEquals(2, count);
    }

    // ── SignatureField ──

    @Test
    public void testSignatureFieldNotSigned() {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("FT"), PdfName.of("Sig"));
        d.set(PdfName.of("T"), new PdfString("sig".getBytes()));
        PdfDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        SignatureField sig = (SignatureField) form.get(1);
        assertFalse(sig.isSigned());
    }

    // ── Nested fields ──

    @Test
    public void testNestedFieldNames() {
        // Parent "person" with kids "name" and "age"
        PdfDictionary parent = new PdfDictionary();
        parent.set(PdfName.of("T"), new PdfString("person".getBytes()));

        PdfDictionary child1 = new PdfDictionary();
        child1.set(PdfName.of("FT"), PdfName.of("Tx"));
        child1.set(PdfName.of("T"), new PdfString("name".getBytes()));
        child1.set(PdfName.of("V"), new PdfString("Alice".getBytes()));

        PdfDictionary child2 = new PdfDictionary();
        child2.set(PdfName.of("FT"), PdfName.of("Tx"));
        child2.set(PdfName.of("T"), new PdfString("age".getBytes()));
        child2.set(PdfName.of("V"), new PdfString("30".getBytes()));

        PdfArray kids = new PdfArray();
        kids.add(child1);
        kids.add(child2);
        parent.set(PdfName.of("Kids"), kids);

        PdfDictionary acro = buildAcroForm(parent);
        Form form = new Form(acro, null, null);
        assertEquals(2, form.getCount());

        Field f1 = form.get("person.name");
        assertNotNull(f1, "Should find person.name");
        assertEquals("Alice", f1.getValue());

        Field f2 = form.get("person.age");
        assertNotNull(f2, "Should find person.age");
        assertEquals("30", f2.getValue());
    }

    // ── Form add/delete ──

    @Test
    public void testFormAdd() {
        Form form = new Form(new PdfDictionary(), null, null);
        assertEquals(0, form.getCount());

        PdfDictionary fd = buildTextField("newField", "hello");
        TextBoxField field = new TextBoxField(fd, null, "newField");
        form.add(field);
        assertEquals(1, form.getCount());
        assertEquals("hello", form.get("newField").getValue());
    }

    @Test
    public void testFormDelete() {
        PdfDictionary acro = buildAcroForm(
                buildTextField("a", "1"),
                buildTextField("b", "2")
        );
        Form form = new Form(acro, null, null);
        assertEquals(2, form.getCount());
        form.delete("a");
        assertEquals(1, form.getCount());
        assertNull(form.get("a"));
        assertNotNull(form.get("b"));
    }

    @Test
    public void testFormFlatten() throws IOException {
        PdfDictionary acro = buildAcroForm(buildTextField("f", "v"));
        Form form = new Form(acro, null, null);
        assertEquals(1, form.getCount());
        form.flatten();
        assertEquals(0, form.getCount());
    }

    // ── Document.getForm() ──

    @Test
    public void testDocumentGetFormEmpty() throws Exception {
        Document doc = new Document();
        Form form = doc.getForm();
        assertNotNull(form);
        assertEquals(0, form.getCount());
        doc.close();
    }
}
