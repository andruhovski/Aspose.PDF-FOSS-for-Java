package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// FIX.2 — page size resolved from the XFA <medium> (short/long/orientation/stock).
public class XfaMediumTest {

    private static final String TPL = "http://www.xfa.org/schema/xfa-template/3.0/";
    private static final double EPS = 0.5;

    @Test
    void letterMediumNestedUnderSubform() throws Exception {
        // medium nested pageSet-under-subform (the 408975 shape) is still found.
        double[] m = resolve("<subform name='f'><pageSet><pageArea>"
                + "<medium short='215.9mm' long='279.4mm' stock='letter'/>"
                + "<contentArea w='215.9mm' h='279.4mm'/></pageArea></pageSet></subform>");
        assertEquals(612, m[0], EPS);
        assertEquals(792, m[1], EPS);
    }

    @Test
    void a4PortraitAndLandscape() throws Exception {
        double[] p = resolve("<pageSet><pageArea><medium short='210mm' long='297mm'/></pageArea></pageSet>");
        assertEquals(595.3, p[0], 1.0);
        assertEquals(841.9, p[1], 1.0);
        double[] l = resolve("<pageSet><pageArea><medium short='210mm' long='297mm' orientation='landscape'/></pageArea></pageSet>");
        assertEquals(841.9, l[0], 1.0); // swapped
        assertEquals(595.3, l[1], 1.0);
    }

    @Test
    void stockFallbackWhenNoExplicitDims() throws Exception {
        double[] a4 = resolve("<pageSet><pageArea><medium stock='a4'/></pageArea></pageSet>");
        assertEquals(595.32, a4[0], EPS);
        assertEquals(841.92, a4[1], EPS);
    }

    @Test
    void defaultLetterWhenNoMedium() throws Exception {
        double[] d = resolve("<subform name='f'/>");
        assertEquals(612, d[0], EPS);
        assertEquals(792, d[1], EPS);
    }

    private static double[] resolve(String body) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse("<template xmlns='" + TPL + "'>" + body + "</template>"));
        return XfaMedium.resolve(tpl);
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
