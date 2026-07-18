package org.aspose.pdf.engine.xfa.flatten.paint;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// C2.3 — presence gating: `presence=hidden/invisible` objects produce NO marks
/// (this suppresses the draft-watermark exposure defect, ticket class 46656);
/// `visible` objects paint. Exercised with `<draw>` text labels (a watermark
/// is a `<draw>`).
public class XfaPresencePaintTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;

    private static final String TEMPLATE =
            "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
            + "<draw name='Watermark' x='10pt' y='10pt' w='200pt' h='50pt' presence='hidden'>"
            + "  <font typeface='Arial' size='40pt'/><value><text>DRAFTWATERMARK</text></value></draw>"
            + "<draw name='Invis' x='10pt' y='70pt' w='200pt' h='20pt' presence='invisible'>"
            + "  <font typeface='Arial' size='12pt'/><value><text>INVISIBLEXYZ</text></value></draw>"
            + "<draw name='Title' x='10pt' y='100pt' w='200pt' h='20pt'>"
            + "  <font typeface='Arial' size='12pt'/><value><text>VisibleTitle</text></value></draw>"
            + "</subform></template>";

    @Test
    void hiddenAndInvisibleDrawsAreSuppressedVisibleIsPainted() throws Exception {
        FormDom dom = merge();
        XfaPainter.Result r = new XfaPainter.Result();
        String content = new String(XfaPainter.buildContent(dom, null, 200, r).toByteArray(),
                StandardCharsets.US_ASCII);

        assertFalse(content.contains("DRAFTWATERMARK"), "presence=hidden watermark must NOT be painted (46656)");
        assertFalse(content.contains("INVISIBLEXYZ"), "presence=invisible must NOT be painted");
        assertTrue(content.contains("(VisibleTitle) Tj"), "visible draw IS painted");
        assertEquals(2, r.presenceHidden, "two suppressed objects (hidden + invisible)");
    }

    private static FormDom merge() throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(TEMPLATE));
        return new BindingEngine().merge(tpl, null);
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
