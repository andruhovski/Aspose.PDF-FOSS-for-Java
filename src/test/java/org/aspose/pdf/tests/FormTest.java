package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
import org.aspose.pdf.forms.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for form (AcroForm) classes.
 */
public class FormTest {

    // ── Helper: build a minimal AcroForm dict ──

    private COSDictionary buildAcroForm(COSDictionary... fieldDicts) {
        COSArray fields = new COSArray();
        for (COSDictionary fd : fieldDicts) fields.add(fd);
        COSDictionary acro = new COSDictionary();
        acro.set(COSName.of("Fields"), fields);
        return acro;
    }

    private COSDictionary buildTextField(String name, String value) {
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("FT"), COSName.of("Tx"));
        d.set(COSName.of("T"), new COSString(name.getBytes()));
        if (value != null) d.set(COSName.of("V"), new COSString(value.getBytes()));
        d.set(COSName.of("Subtype"), COSName.of("Widget"));
        return d;
    }

    private COSDictionary buildCheckbox(String name, boolean checked) {
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("FT"), COSName.of("Btn"));
        d.set(COSName.of("T"), new COSString(name.getBytes()));
        d.set(COSName.of("V"), COSName.of(checked ? "Yes" : "Off"));
        d.set(COSName.of("Subtype"), COSName.of("Widget"));
        // AP/N for on value
        COSDictionary ap = new COSDictionary();
        COSDictionary n = new COSDictionary();
        n.set(COSName.of("Yes"), new COSDictionary());
        n.set(COSName.of("Off"), new COSDictionary());
        ap.set(COSName.of("N"), n);
        d.set(COSName.of("AP"), ap);
        return d;
    }

    private COSDictionary buildComboBox(String name, String value, String... options) {
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("FT"), COSName.of("Ch"));
        d.set(COSName.of("Ff"), COSInteger.valueOf(1 << 17)); // combo flag
        d.set(COSName.of("T"), new COSString(name.getBytes()));
        if (value != null) d.set(COSName.of("V"), new COSString(value.getBytes()));
        COSArray opt = new COSArray();
        for (String o : options) opt.add(new COSString(o.getBytes()));
        d.set(COSName.of("Opt"), opt);
        d.set(COSName.of("Subtype"), COSName.of("Widget"));
        return d;
    }

    // ── Form basics ──

    @Test
    public void testEmptyForm() {
        Form form = new Form(new COSDictionary(), null, null);
        assertEquals(0, form.getCount());
    }

    @Test
    public void testFormFieldCount() {
        COSDictionary acro = buildAcroForm(
                buildTextField("field1", "hello"),
                buildTextField("field2", "world"),
                buildTextField("field3", null)
        );
        Form form = new Form(acro, null, null);
        assertEquals(3, form.getCount());
    }

    @Test
    public void testFormGetByName() {
        COSDictionary acro = buildAcroForm(buildTextField("myField", "value"));
        Form form = new Form(acro, null, null);
        Field f = form.get("myField");
        assertNotNull(f);
        assertEquals("value", f.getValue());
    }

    @Test
    public void testFormGetByIndex() {
        COSDictionary acro = buildAcroForm(
                buildTextField("a", "1"),
                buildTextField("b", "2")
        );
        Form form = new Form(acro, null, null);
        assertEquals("a", form.get(1).getPartialName());
        assertEquals("b", form.get(2).getPartialName());
    }

    @Test
    public void testFormOneBasedIndex() {
        COSDictionary acro = buildAcroForm(buildTextField("f", "v"));
        Form form = new Form(acro, null, null);
        assertThrows(IndexOutOfBoundsException.class, () -> form.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> form.get(2));
    }

    @Test
    public void testFormIteration() {
        COSDictionary acro = buildAcroForm(
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
        COSDictionary acro = buildAcroForm();
        Form form = new Form(acro, null, null);
        assertEquals(Form.FormType.Standard, form.getType());
    }

    // ── Field factory dispatch ──

    @Test
    public void testTextBoxFieldFromFactory() {
        COSDictionary acro = buildAcroForm(buildTextField("text1", "hello"));
        Form form = new Form(acro, null, null);
        Field f = form.get(1);
        assertTrue(f instanceof TextBoxField, "Expected TextBoxField, got " + f.getClass().getSimpleName());
    }

    @Test
    public void testCheckboxFieldFromFactory() {
        COSDictionary acro = buildAcroForm(buildCheckbox("check1", true));
        Form form = new Form(acro, null, null);
        Field f = form.get(1);
        assertTrue(f instanceof CheckboxField, "Expected CheckboxField, got " + f.getClass().getSimpleName());
    }

    @Test
    public void testComboBoxFieldFromFactory() {
        COSDictionary acro = buildAcroForm(buildComboBox("combo1", "B", "A", "B", "C"));
        Form form = new Form(acro, null, null);
        Field f = form.get(1);
        assertTrue(f instanceof ComboBoxField, "Expected ComboBoxField, got " + f.getClass().getSimpleName());
    }

    @Test
    public void testRadioButtonFromFactory() {
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("FT"), COSName.of("Btn"));
        d.set(COSName.of("Ff"), COSInteger.valueOf(1 << 15)); // radio flag
        d.set(COSName.of("T"), new COSString("radio1".getBytes()));
        d.set(COSName.of("Subtype"), COSName.of("Widget"));
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1) instanceof RadioButtonField);
    }

    @Test
    public void testButtonFieldFromFactory() {
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("FT"), COSName.of("Btn"));
        d.set(COSName.of("Ff"), COSInteger.valueOf(1 << 16)); // push button flag
        d.set(COSName.of("T"), new COSString("button1".getBytes()));
        d.set(COSName.of("Subtype"), COSName.of("Widget"));
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1) instanceof ButtonField);
    }

    @Test
    public void testListBoxFieldFromFactory() {
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("FT"), COSName.of("Ch"));
        // No combo flag → ListBox
        d.set(COSName.of("T"), new COSString("list1".getBytes()));
        d.set(COSName.of("Subtype"), COSName.of("Widget"));
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1) instanceof ListBoxField);
    }

    @Test
    public void testSignatureFieldFromFactory() {
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("FT"), COSName.of("Sig"));
        d.set(COSName.of("T"), new COSString("sig1".getBytes()));
        d.set(COSName.of("Subtype"), COSName.of("Widget"));
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1) instanceof SignatureField);
    }

    // ── Field properties ──

    @Test
    public void testFieldGetSetValue() {
        COSDictionary acro = buildAcroForm(buildTextField("f", "old"));
        Form form = new Form(acro, null, null);
        Field f = form.get(1);
        assertEquals("old", f.getValue());
        f.setValue("new");
        assertEquals("new", f.getValue());
    }

    @Test
    public void testFieldFullName() {
        COSDictionary acro = buildAcroForm(buildTextField("myName", "v"));
        Form form = new Form(acro, null, null);
        assertEquals("myName", form.get(1).getFullName());
    }

    @Test
    public void testFieldPartialName() {
        COSDictionary acro = buildAcroForm(buildTextField("partial", "v"));
        Form form = new Form(acro, null, null);
        assertEquals("partial", form.get(1).getPartialName());
    }

    @Test
    public void testFieldFlags() {
        COSDictionary d = buildTextField("f", "v");
        d.set(COSName.of("Ff"), COSInteger.valueOf(1)); // ReadOnly
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1).isReadOnly());
        assertFalse(form.get(1).isRequired());
    }

    @Test
    public void testFieldRequired() {
        COSDictionary d = buildTextField("f", "v");
        d.set(COSName.of("Ff"), COSInteger.valueOf(2)); // Required
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        assertTrue(form.get(1).isRequired());
    }

    // ── TextBoxField ──

    @Test
    public void testTextBoxMultiline() {
        COSDictionary d = buildTextField("f", "v");
        d.set(COSName.of("Ff"), COSInteger.valueOf(1 << 12)); // multiline
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        TextBoxField tb = (TextBoxField) form.get(1);
        assertTrue(tb.isMultiline());
    }

    @Test
    public void testTextBoxMaxLen() {
        COSDictionary d = buildTextField("f", "v");
        d.set(COSName.of("MaxLen"), COSInteger.valueOf(100));
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        TextBoxField tb = (TextBoxField) form.get(1);
        assertEquals(100, tb.getMaxLen());
    }

    // ── CheckboxField ──

    @Test
    public void testCheckboxIsChecked() {
        COSDictionary acro = buildAcroForm(buildCheckbox("cb", true));
        Form form = new Form(acro, null, null);
        CheckboxField cb = (CheckboxField) form.get(1);
        assertTrue(cb.isChecked());
    }

    @Test
    public void testCheckboxSetChecked() {
        COSDictionary acro = buildAcroForm(buildCheckbox("cb", false));
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
        COSDictionary acro = buildAcroForm(buildCheckbox("cb", true));
        Form form = new Form(acro, null, null);
        CheckboxField cb = (CheckboxField) form.get(1);
        assertEquals("Yes", cb.getExportValue());
    }

    // ── ComboBoxField ──

    @Test
    public void testComboBoxOptions() {
        COSDictionary acro = buildAcroForm(buildComboBox("combo", "B", "A", "B", "C"));
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
        COSDictionary acro = buildAcroForm(buildComboBox("combo", "B", "A", "B", "C"));
        Form form = new Form(acro, null, null);
        ComboBoxField combo = (ComboBoxField) form.get(1);
        assertEquals("B", combo.getSelected());
    }

    @Test
    public void testComboBoxSetSelected() {
        COSDictionary acro = buildAcroForm(buildComboBox("combo", "B", "A", "B", "C"));
        Form form = new Form(acro, null, null);
        ComboBoxField combo = (ComboBoxField) form.get(1);
        combo.setSelected("C");
        assertEquals("C", combo.getSelected());
    }

    // ── OptionCollection iteration ──

    @Test
    public void testOptionIteration() {
        COSDictionary acro = buildAcroForm(buildComboBox("combo", null, "X", "Y"));
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
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("FT"), COSName.of("Sig"));
        d.set(COSName.of("T"), new COSString("sig".getBytes()));
        COSDictionary acro = buildAcroForm(d);
        Form form = new Form(acro, null, null);
        SignatureField sig = (SignatureField) form.get(1);
        assertFalse(sig.isSigned());
    }

    // ── Nested fields ──

    @Test
    public void testNestedFieldNames() {
        // Parent "person" with kids "name" and "age"
        COSDictionary parent = new COSDictionary();
        parent.set(COSName.of("T"), new COSString("person".getBytes()));

        COSDictionary child1 = new COSDictionary();
        child1.set(COSName.of("FT"), COSName.of("Tx"));
        child1.set(COSName.of("T"), new COSString("name".getBytes()));
        child1.set(COSName.of("V"), new COSString("Alice".getBytes()));

        COSDictionary child2 = new COSDictionary();
        child2.set(COSName.of("FT"), COSName.of("Tx"));
        child2.set(COSName.of("T"), new COSString("age".getBytes()));
        child2.set(COSName.of("V"), new COSString("30".getBytes()));

        COSArray kids = new COSArray();
        kids.add(child1);
        kids.add(child2);
        parent.set(COSName.of("Kids"), kids);

        COSDictionary acro = buildAcroForm(parent);
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
        Form form = new Form(new COSDictionary(), null, null);
        assertEquals(0, form.getCount());

        COSDictionary fd = buildTextField("newField", "hello");
        TextBoxField field = new TextBoxField(fd, null, "newField");
        form.add(field);
        assertEquals(1, form.getCount());
        assertEquals("hello", form.get("newField").getValue());
    }

    @Test
    public void testFormDelete() {
        COSDictionary acro = buildAcroForm(
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
        COSDictionary acro = buildAcroForm(buildTextField("f", "v"));
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
