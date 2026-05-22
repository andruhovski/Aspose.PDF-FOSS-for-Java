package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.forms.CheckboxField;
import org.aspose.pdf.forms.TextBoxField;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for facade-level form helpers.
 */
public class FormFacadeTest {

    @Test
    public void constructorWithDocument_exposesFieldValuesAndTypes() throws Exception {
        try (Document doc = createSampleFormDocument();
             org.aspose.pdf.facades.Form form = new org.aspose.pdf.facades.Form(doc)) {
            assertEquals("Hello", form.getField("text"));
            assertEquals("Yes", form.getField("check"));
            assertEquals("Text", form.getFieldType("text"));
            assertEquals("CheckBox", form.getFieldType("check"));
            assertNotNull(form.getFieldFacade("text"));
        }
    }

    @Test
    public void constructorWithStream_readsSavedDocument() throws Exception {
        byte[] bytes;
        try (Document doc = createSampleFormDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            doc.save(output);
            bytes = output.toByteArray();
        }

        try (org.aspose.pdf.facades.Form form =
                     new org.aspose.pdf.facades.Form(new ByteArrayInputStream(bytes))) {
            assertEquals(2, form.getFieldNames().length);
            assertEquals("Hello", form.getField("text"));
            assertEquals("Yes", form.getField("check"));
        }
    }

    @Test
    public void fillFieldBoolean_updatesCheckboxState() throws Exception {
        try (Document doc = createSampleFormDocument();
             org.aspose.pdf.facades.Form form = new org.aspose.pdf.facades.Form(doc)) {
            assertTrue(form.fillField("check", false));
            assertEquals("Off", form.getField("check"));
            assertTrue(form.fillField("check", true));
            assertEquals("Yes", form.getField("check"));
        }
    }

    @Test
    public void flattenField_removesSingleFieldFromForm() throws Exception {
        try (Document doc = createSampleFormDocument();
             org.aspose.pdf.facades.Form form = new org.aspose.pdf.facades.Form(doc)) {
            assertTrue(doc.getForm().hasField("text"));
            assertTrue(form.flattenField("text"));
            assertFalse(doc.getForm().hasField("text"));
            assertTrue(doc.getForm().hasField("check"));
        }
    }

    @Test
    public void formEditor_canRemoveFieldAndManageTextLimit() throws Exception {
        try (Document doc = createSampleFormDocument()) {
            org.aspose.pdf.facades.FormEditor editor =
                    new org.aspose.pdf.facades.FormEditor(doc);
            assertTrue(editor.setFieldLimit("text", 12));
            assertEquals(12, editor.getFieldLimit("text"));
            assertEquals("Text", editor.getFieldType("text"));
            assertTrue(editor.removeField("text"));
            assertNull(editor.getField("text"));
            assertFalse(doc.getForm().hasField("text"));
            editor.close();
        }
    }

    private Document createSampleFormDocument() throws Exception {
        Document doc = new Document();
        Page page = doc.getPages().add();

        TextBoxField textField = new TextBoxField(page, new Rectangle(10, 10, 100, 30));
        textField.setPartialName("text");
        textField.setValue("Hello");
        doc.getForm().add(textField);

        CheckboxField checkboxField = new CheckboxField(page, new Rectangle(10, 40, 30, 60));
        checkboxField.setPartialName("check");
        checkboxField.setChecked(true);
        doc.getForm().add(checkboxField);

        return doc;
    }
}
