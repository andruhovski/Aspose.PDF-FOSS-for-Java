package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Optional Content Group (layer) in a PDF document
 * (ISO 32000-1:2008, §8.11).
 */
public class Layer {

    private final COSDictionary ocgDict;
    private String id;
    private final List<Operator> contents;

    /** Creates a new layer. */
    public Layer(String id, String name) {
        this.id = id;
        this.ocgDict = new COSDictionary();
        ocgDict.set(COSName.of("Type"), COSName.of("OCG"));
        ocgDict.set(COSName.of("Name"), new COSString(name.getBytes(StandardCharsets.UTF_8)));
        this.contents = new ArrayList<>();
    }

    /** Wraps an existing OCG dictionary. */
    public Layer(COSDictionary ocgDict) {
        this.ocgDict = ocgDict != null ? ocgDict : new COSDictionary();
        this.contents = new ArrayList<>();
    }

    /** Returns the layer ID. */
    public String getId() { return id; }

    /** Returns the layer name (/Name). */
    public String getName() {
        COSBase n = ocgDict.get("Name");
        return (n instanceof COSString) ? ((COSString) n).getString() : "";
    }

    /** Sets the layer name. */
    public void setName(String name) {
        ocgDict.set(COSName.of("Name"), new COSString(name.getBytes(StandardCharsets.UTF_8)));
    }

    /** Returns content operators for this layer. */
    public List<Operator> getContents() { return contents; }

    /** Returns the underlying OCG dictionary. */
    public COSDictionary getCOSDictionary() { return ocgDict; }
}
