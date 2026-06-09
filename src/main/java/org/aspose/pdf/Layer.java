package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Optional Content Group (layer) in a PDF document
 * (ISO 32000-1:2008, §8.11).
 */
public class Layer {

    private final PdfDictionary ocgDict;
    private String id;
    private final List<Operator> contents;

    /** Creates a new layer. */
    public Layer(String id, String name) {
        this.id = id;
        this.ocgDict = new PdfDictionary();
        ocgDict.set(PdfName.of("Type"), PdfName.of("OCG"));
        ocgDict.set(PdfName.of("Name"), new PdfString(name.getBytes(StandardCharsets.UTF_8)));
        this.contents = new ArrayList<>();
    }

    /** Wraps an existing OCG dictionary. */
    public Layer(PdfDictionary ocgDict) {
        this.ocgDict = ocgDict != null ? ocgDict : new PdfDictionary();
        this.contents = new ArrayList<>();
    }

    /** Returns the layer ID. */
    public String getId() { return id; }

    /** Returns the layer name (/Name). */
    public String getName() {
        PdfBase n = ocgDict.get("Name");
        return (n instanceof PdfString) ? ((PdfString) n).getString() : "";
    }

    /** Sets the layer name. */
    public void setName(String name) {
        ocgDict.set(PdfName.of("Name"), new PdfString(name.getBytes(StandardCharsets.UTF_8)));
    }

    /** Returns content operators for this layer. */
    public List<Operator> getContents() { return contents; }

    /** Returns the underlying OCG dictionary. */
    public PdfDictionary getPdfDictionary() { return ocgDict; }
}
