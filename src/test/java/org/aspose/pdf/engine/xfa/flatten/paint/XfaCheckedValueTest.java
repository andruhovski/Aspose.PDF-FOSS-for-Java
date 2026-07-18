package org.aspose.pdf.engine.xfa.flatten.paint;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/// Tests [XfaPainter#isCheckedValue]: a checkButton's CHECKED state is defined by its
/// authored on-value (first `<items>` entry, typed `<text>` or `<integer>`),
/// not by generic truthiness — a form may author on-values like "2", "03" or even "0".
public class XfaCheckedValueTest {

    private static Element field(String itemsXml) throws Exception {
        String xml = "<field xmlns=\"http://www.xfa.org/schema/xfa-template/3.0/\" name=\"cb\">"
                + "<ui><checkButton/></ui>" + itemsXml + "</field>";
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
                .getDocumentElement();
    }

    @Test
    public void authoredOnValue_nonNumericOne() throws Exception {
        Element el = field("<items><text>2</text><text>0</text></items>");
        assertTrue(XfaPainter.isCheckedValue(el, "2"));
        assertFalse(XfaPainter.isCheckedValue(el, "0"));
        assertFalse(XfaPainter.isCheckedValue(el, "1")); // "1" is NOT this field's on-value
    }

    @Test
    public void authoredOnValue_zeroPadded() throws Exception {
        Element el = field("<items><text>03</text><text/></items>");
        assertTrue(XfaPainter.isCheckedValue(el, "03"));
        assertFalse(XfaPainter.isCheckedValue(el, "3"));
    }

    @Test
    public void integerTypedItems() throws Exception {
        Element el = field("<items><integer>1</integer><integer>0</integer></items>");
        assertTrue(XfaPainter.isCheckedValue(el, "1"));
        assertFalse(XfaPainter.isCheckedValue(el, "0"));
    }

    @Test
    public void noItems_defaultOnValueAndBooleanSpellings() throws Exception {
        Element el = field("");
        assertTrue(XfaPainter.isCheckedValue(el, "1"));
        assertTrue(XfaPainter.isCheckedValue(el, "true"));
        assertTrue(XfaPainter.isCheckedValue(el, "Yes"));
        assertFalse(XfaPainter.isCheckedValue(el, "0"));
        assertFalse(XfaPainter.isCheckedValue(el, ""));
        assertFalse(XfaPainter.isCheckedValue(el, null));
    }
}
