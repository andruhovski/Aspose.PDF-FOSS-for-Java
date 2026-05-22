package org.aspose.pdf.forms;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the extended RadioButtonField/RadioButtonOptionField API:
 * setValue/getValue/getOptions on the field, and per-option getStyle/setStyle
 * + getCharacteristics on the option.
 */
public class RadioButtonFieldExtendedTests {

    @Test
    public void setValue_selectsMatchingOption() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("radio1");
            radio.addOption("Option1", new Rectangle(50, 50, 70, 70));
            radio.addOption("Option2", new Rectangle(50, 80, 70, 100));
            radio.addOption("Option3", new Rectangle(50, 110, 70, 130));
            doc.getForm().add(radio, 1);

            radio.setValue("Option2");
            assertEquals("Option2", radio.getValue());

            radio.setValue("Option1");
            assertEquals("Option1", radio.getValue());

            radio.setValue("Option3");
            assertEquals("Option3", radio.getValue());
        }
    }

    @Test
    public void setValue_unknownClearsSelection() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("r");
            radio.addOption("Yes", new Rectangle(0, 0, 20, 20));
            radio.addOption("No", new Rectangle(0, 30, 20, 50));
            doc.getForm().add(radio, 1);

            radio.setValue("Yes");
            assertEquals("Yes", radio.getValue());

            radio.setValue("Maybe");                 // not an option
            assertEquals("", radio.getValue());      // selection cleared
        }
    }

    @Test
    public void getValue_unselected_returnsEmpty() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("r");
            radio.addOption("A", new Rectangle(0, 0, 20, 20));
            radio.addOption("B", new Rectangle(0, 30, 20, 50));
            doc.getForm().add(radio, 1);

            // No setValue() yet
            assertEquals("", radio.getValue());
        }
    }

    @Test
    public void getOptions_returnsAllKids() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("r");
            radio.addOption("X", new Rectangle(0, 0, 20, 20));
            radio.addOption("Y", new Rectangle(0, 30, 20, 50));
            radio.addOption("Z", new Rectangle(0, 60, 20, 80));
            doc.getForm().add(radio, 1);

            assertEquals(3, radio.getOptions().size());
            assertEquals("X", radio.getOptions().get(0).getOptionValue());
            assertEquals("Z", radio.getOptions().get(2).getOptionValue());
        }
    }

    @Test
    public void radioOption_style_defaultIsCircle() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("r");
            radio.addOption("A", new Rectangle(0, 0, 20, 20));
            doc.getForm().add(radio, 1);

            RadioButtonOptionField opt = radio.getOptions().get(0);
            assertEquals(BoxStyle.Circle, opt.getStyle());
        }
    }

    @Test
    public void radioOption_setStyle_roundtrips() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("r");
            radio.addOption("A", new Rectangle(0, 0, 20, 20));
            doc.getForm().add(radio, 1);

            RadioButtonOptionField opt = radio.getOptions().get(0);
            opt.setStyle(BoxStyle.Star);
            assertEquals(BoxStyle.Star, opt.getStyle());
            opt.setStyle(BoxStyle.Cross);
            assertEquals(BoxStyle.Cross, opt.getStyle());
        }
    }

    @Test
    public void radioOption_setStyle_rejectsNull() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("r");
            radio.addOption("A", new Rectangle(0, 0, 20, 20));
            doc.getForm().add(radio, 1);
            RadioButtonOptionField opt = radio.getOptions().get(0);
            assertThrows(IllegalArgumentException.class, () -> opt.setStyle(null));
        }
    }

    @Test
    public void radioOption_characteristics_writeBorder() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            RadioButtonField radio = new RadioButtonField(page);
            radio.setPartialName("r");
            radio.addOption("A", new Rectangle(0, 0, 20, 20));
            doc.getForm().add(radio, 1);

            RadioButtonOptionField opt = radio.getOptions().get(0);
            opt.getCharacteristics().setBorder(org.aspose.pdf.Color.fromRgb(0, 0, 1));
            assertNotNull(opt.getCharacteristics().getBorder());
            assertEquals(1.0, opt.getCharacteristics().getBorder().getB(), 0.01);
        }
    }
}
