package org.aspose.pdf.engine.xfa.packet;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import static org.aspose.pdf.engine.xfa.packet.XfaPacketReader.resolveRef;

/**
 * Writes a modified {@link XfaPacketSet} back to the PDF {@code /XFA} entry,
 * replacing the former {@code XfaPacketParser} write path.
 *
 * <p>For a PdfArray entry each packet's stream is updated with the serialized
 * DOM of the matching packet; for a single PdfStream entry the assembled XDP
 * document is serialized back. Behaviour matches the previous implementation.</p>
 */
public final class XfaPacketWriter {

    private static final Logger LOG = Logger.getLogger(XfaPacketWriter.class.getName());

    private XfaPacketWriter() { }

    /**
     * Writes packet DOMs back to the {@code /XFA} structures.
     *
     * @param xfaEntry the original {@code /XFA} value (PdfArray or PdfStream)
     * @param set      the (possibly modified) packet set
     * @throws IOException if serialization/writing fails
     */
    public static void writeBack(PdfBase xfaEntry, XfaPacketSet set) throws IOException {
        if (xfaEntry == null) {
            throw new IllegalArgumentException("XFA entry must not be null");
        }
        PdfBase resolved = resolveRef(xfaEntry);
        if (resolved instanceof PdfStream) {
            Document xdp = set.getXdp();
            if (xdp == null) {
                LOG.warning("No XDP document to write back");
                return;
            }
            ((PdfStream) resolved).setDecodedData(serialize(xdp));
        } else if (resolved instanceof PdfArray) {
            writeBackToArray((PdfArray) resolved, set);
        } else {
            throw new IllegalArgumentException(
                    "XFA entry must be a PdfStream or PdfArray, got: " + resolved.getClass().getSimpleName());
        }
    }

    private static void writeBackToArray(PdfArray array, XfaPacketSet set) throws IOException {
        for (int i = 0; i + 1 < array.size(); i += 2) {
            PdfBase nameObj = resolveRef(array.get(i));
            PdfBase streamObj = resolveRef(array.get(i + 1));
            if (!(streamObj instanceof PdfStream)) {
                continue;
            }
            String packetName;
            if (nameObj instanceof PdfString) {
                packetName = ((PdfString) nameObj).getString();
            } else if (nameObj instanceof PdfName) {
                packetName = ((PdfName) nameObj).getName();
            } else {
                continue;
            }
            XfaPacket packet = set.get(packetName);
            if (packet == null || packet.getDocument() == null) {
                continue;
            }
            // Only rewrite packets whose DOM was actually mutated. Re-serializing an UNCHANGED
            // packet (template/config/…) is not byte-neutral — DOM round-tripping normalizes
            // whitespace/namespaces/attribute order, which Acrobat's strict XFA engine can reject
            // (e.g. xml:space="preserve" rich-text in <exData>), leaving the file un-openable even
            // though every packet is still well-formed XML. The public API (set/setFieldImage)
            // mutates only the datasets packet, so leave the rest byte-identical to the source.
            if (!packet.isDirty()) {
                continue;
            }
            ((PdfStream) streamObj).setDecodedData(serialize(packet.getDocument()));
        }
    }

    /**
     * Serializes a DOM document to UTF-8 bytes (no XML declaration), matching
     * the prior write path.
     *
     * @param doc the document
     * @return serialized bytes
     * @throws IOException on transform failure
     */
    public static byte[] serialize(Document doc) throws IOException {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            t.transform(new DOMSource(doc), new StreamResult(baos));
            return baos.toByteArray();
        } catch (TransformerException e) {
            throw new IOException("Failed to serialize XML document", e);
        }
    }
}
