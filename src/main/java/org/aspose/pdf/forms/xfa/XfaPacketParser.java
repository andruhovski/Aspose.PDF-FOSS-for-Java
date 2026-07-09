package org.aspose.pdf.forms.xfa;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.xfa.packet.XfaPacket;
import org.aspose.pdf.engine.xfa.packet.XfaPacketReader;
import org.aspose.pdf.engine.xfa.packet.XfaPacketSet;
import org.aspose.pdf.engine.xfa.packet.XfaPacketWriter;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin backwards-compatible facade over the typed packet model
 * ({@link XfaPacketReader} / {@link XfaPacketSet} / {@link XfaPacketWriter}).
 *
 * <p>The packet-splitting and write-back logic now lives in
 * {@code engine.xfa.packet}; this class is retained so existing callers keep
 * compiling and behaving identically.</p>
 *
 * @deprecated use {@link XfaPacketReader}/{@link XfaPacketSet}/{@link XfaPacketWriter} directly.
 */
@Deprecated
public class XfaPacketParser {

    private final XfaPacketSet set;

    /**
     * Parses the {@code /XFA} entry.
     *
     * @param xfaEntry the resolved {@code /XFA} value (PdfArray or PdfStream)
     * @throws IOException if reading or parsing fails
     */
    public XfaPacketParser(PdfBase xfaEntry) throws IOException {
        this.set = XfaPacketReader.read(xfaEntry);
    }

    /**
     * Returns a packet DOM by name.
     *
     * @param name packet name
     * @return the parsed DOM, or {@code null}
     */
    public Document getPacket(String name) {
        return set.getDocument(name);
    }

    /** @return the assembled XDP document, or {@code null}. */
    public Document getXDP() {
        return set.getXdp();
    }

    /** @return all packets as an ordered name-to-DOM map. */
    public Map<String, Document> getAllPackets() {
        Map<String, Document> out = new LinkedHashMap<>();
        for (XfaPacket p : set.all()) {
            out.put(p.getName(), p.getDocument());
        }
        return out;
    }

    /**
     * Writes modified packet DOMs back to the PDF structures.
     *
     * @param xfaEntry the original {@code /XFA} value
     * @throws IOException if serialization or writing fails
     */
    public void writeBack(PdfBase xfaEntry) throws IOException {
        XfaPacketWriter.writeBack(xfaEntry, set);
    }

    /** @return the underlying typed packet set. */
    public XfaPacketSet getPacketSet() {
        return set;
    }
}
