package org.aspose.pdf.engine.xfa.model;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static java.time.Duration.ofSeconds;

/// A2.4: use/usehref/proto prototype resolution, inheritance, cycle detection.
public class XfaProtoResolverTest {

    private static final String NS = XfaNode.TEMPLATE_NS;

    @Test
    void useInheritsAttributesAndChildren() throws Exception {
        XfaNode root = wrap("<template xmlns='" + NS + "'>"
                + "<subform name='proto' id='p1' layout='tb'><field name='inherited'/></subform>"
                + "<subform name='real' use='#p1'/>"
                + "</template>");
        XfaProtoResolver.Report rep = new XfaProtoResolver(root).resolve();
        assertEquals(1, rep.getResolvedCount());

        XfaNode real = root.getChildren("subform").get(1);
        assertEquals("tb", real.getAttribute("layout"), "inherited attribute");
        assertNotNull(real.getChild("field"), "inherited child");
        assertEquals("inherited", real.getChild("field").getName());
    }

    @Test
    void localPropertiesOverridePrototype() throws Exception {
        XfaNode root = wrap("<template xmlns='" + NS + "'>"
                + "<subform name='proto' id='p1' layout='tb'/>"
                + "<subform name='real' use='#p1' layout='lr'/>"
                + "</template>");
        new XfaProtoResolver(root).resolve();
        XfaNode real = root.getChildren("subform").get(1);
        assertEquals("lr", real.getAttribute("layout"), "local value wins over inherited");
    }

    @Test
    void usehrefSameDocumentResolves() throws Exception {
        XfaNode root = wrap("<template xmlns='" + NS + "'>"
                + "<subform name='proto' id='p2'><field name='f'/></subform>"
                + "<subform name='real' usehref='#p2'/>"
                + "</template>");
        XfaProtoResolver.Report rep = new XfaProtoResolver(root).resolve();
        assertEquals(1, rep.getResolvedCount());
        assertNotNull(root.getChildren("subform").get(1).getChild("field"));
    }

    @Test
    void usehrefExternalProtoSourceResolves() throws Exception {
        XfaNode proto = wrap("<template xmlns='" + NS + "'>"
                + "<subform name='base' id='b1'><field name='ext'/></subform></template>");
        XfaNode root = wrap("<template xmlns='" + NS + "'>"
                + "<subform name='real' usehref='lib.xdp#b1'/></template>");
        XfaProtoResolver r = new XfaProtoResolver(root);
        r.addProtoSource("lib.xdp", proto);
        XfaProtoResolver.Report rep = r.resolve();
        assertEquals(1, rep.getResolvedCount());
        assertNotNull(root.getChild("subform").getChild("field"));
    }

    @Test
    void cycleIsDetectedNotInfiniteLooped() throws Exception {
        XfaNode root = wrap("<template xmlns='" + NS + "'>"
                + "<subform name='a' id='A' use='#B'/>"
                + "<subform name='b' id='B' use='#A'/>"
                + "</template>");
        assertTimeoutPreemptively(ofSeconds(5), () -> {
            XfaProtoResolver.Report rep = new XfaProtoResolver(root).resolve();
            assertFalse(rep.getCycles().isEmpty(), "cycle must be reported");
        });
    }

    @Test
    void unresolvedReferenceDegradesWithoutCrash() throws Exception {
        XfaNode root = wrap("<template xmlns='" + NS + "'>"
                + "<subform name='real' use='#doesNotExist'/></template>");
        XfaProtoResolver.Report rep = new XfaProtoResolver(root).resolve();
        assertEquals(0, rep.getResolvedCount());
        assertEquals(1, rep.getUnresolved().size());
        assertTrue(rep.getUnresolved().get(0).contains("doesNotExist"));
    }

    private static XfaNode wrap(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        Document doc = f.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return XfaNodeFactory.wrap(doc.getDocumentElement(), null);
    }
}
