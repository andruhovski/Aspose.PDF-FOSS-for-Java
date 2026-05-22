package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Layer (Optional Content Group).
 */
public class LayerTest {

    @Test
    public void testCreateLayer() {
        Layer layer = new Layer("layer1", "Background");
        assertEquals("Background", layer.getName());
        assertEquals("layer1", layer.getId());
    }

    @Test
    public void testLayerSetName() {
        Layer layer = new Layer("id1", "Old");
        layer.setName("New");
        assertEquals("New", layer.getName());
    }

    @Test
    public void testLayerDictHasType() {
        Layer layer = new Layer("id1", "Test");
        COSDictionary dict = layer.getCOSDictionary();
        assertEquals("OCG", dict.getNameAsString("Type"));
    }

    @Test
    public void testDocumentGetLayersEmpty() throws Exception {
        Document doc = new Document();
        List<Layer> layers = doc.getLayers();
        assertNotNull(layers);
        assertEquals(0, layers.size());
        doc.close();
    }

    @Test
    public void testPageGetLayersEmpty() {
        COSDictionary pageDict = new COSDictionary();
        pageDict.set(COSName.TYPE, COSName.PAGE);
        Page page = new Page(pageDict, null);
        List<Layer> layers = page.getLayers();
        assertNotNull(layers);
        assertEquals(0, layers.size());
    }

    @Test
    public void testLayerContents() {
        Layer layer = new Layer("id", "Test");
        assertNotNull(layer.getContents());
        assertEquals(0, layer.getContents().size());
    }
}
