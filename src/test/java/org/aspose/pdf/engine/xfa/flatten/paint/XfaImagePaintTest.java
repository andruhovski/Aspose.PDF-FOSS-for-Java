package org.aspose.pdf.engine.xfa.flatten.paint;

import org.aspose.pdf.engine.layout.ContentStreamBuilder;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.xfa.binding.BindingEngine;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Image-painting coverage: a `<draw>`/field whose value is an embedded
/// `<value><image contentType="image/png">BASE64</image></value>` (a company logo / picture)
/// is decoded into a page Image XObject and painted with a `cm`+`Do`. 14758 (the Home
/// Depot purchase order) authors its header logo this way; the painter previously dropped it entirely.
public class XfaImagePaintTest {

    private static final String TPL = XfaNode.TEMPLATE_NS;

    @Test
    void embeddedPngImageIsPaintedAsXObject() throws Exception {
        String b64 = pngBase64(40, 24, Color.ORANGE);
        String tpl =
                "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
                + "<draw name='Logo' x='10pt' y='20pt' w='80pt' h='48pt'>"
                + "  <value><image aspect='none' contentType='image/png'>" + b64 + "</image></value>"
                + "  <ui><imageEdit/></ui></draw>"
                + "</subform></template>";

        XfaPainter.Result r = new XfaPainter.Result();
        FormDom dom = merge(tpl);
        ContentStreamBuilder b = XfaPainter.buildContent(dom, null, 400, r);
        String content = new String(b.toByteArray(), StandardCharsets.US_ASCII);

        assertEquals(1, r.images, "one image painted");
        Map<String, PdfStream> imgs = b.getImageXObjectDicts();
        assertEquals(1, imgs.size(), "one Image XObject registered");
        String resName = imgs.keySet().iterator().next();
        assertTrue(content.contains("/" + resName + " Do"),
                "the Do operator references the registered image; was:\n" + content);
        // aspect="none" stretches to the 80x48pt box → cm uses the box dimensions.
        assertTrue(content.contains("80.0000 0.0000 0.0000 48.0000"),
                "cm scales the image to the box; was:\n" + content);
        PdfStream xobj = imgs.get(resName);
        assertEquals("Image", xobj.getNameAsString("Subtype"), "XObject is an Image");
        assertEquals(40, xobj.getInt("Width", 0));
        assertEquals(24, xobj.getInt("Height", 0));
    }

    @Test
    void fitAspectPreservesRatioCentredInBox() throws Exception {
        // 40x20 image (2:1) into a 100x100 box with aspect="fit" → scaled to 100x50, centred (y offset 25).
        String b64 = pngBase64(40, 20, Color.BLUE);
        String tpl =
                "<template xmlns='" + TPL + "'><subform name='form1' x='0pt' y='0pt'>"
                + "<draw name='Pic' x='0pt' y='0pt' w='100pt' h='100pt'>"
                + "  <value><image aspect='fit' contentType='image/png'>" + b64 + "</image></value></draw>"
                + "</subform></template>";
        XfaPainter.Result r = new XfaPainter.Result();
        FormDom dom = merge(tpl);
        ContentStreamBuilder b = XfaPainter.buildContent(dom, null, 400, r);
        String content = new String(b.toByteArray(), StandardCharsets.US_ASCII);

        assertEquals(1, r.images, "one image painted");
        assertTrue(content.contains("100.0000 0.0000 0.0000 50.0000"),
                "fit scales 2:1 image to 100x50 preserving aspect; was:\n" + content);
    }

    /* helpers */

    private static String pngBase64(int w, int h, Color c) throws Exception {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(c);
        g.fillRect(0, 0, w, h);
        g.dispose();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    private static FormDom merge(String xml) throws Exception {
        Template tpl = (Template) XfaNodeFactory.load(parse(xml));
        return new BindingEngine().merge(tpl, null);
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
