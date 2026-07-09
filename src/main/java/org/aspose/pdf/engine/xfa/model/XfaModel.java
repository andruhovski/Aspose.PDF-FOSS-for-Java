package org.aspose.pdf.engine.xfa.model;

import org.aspose.pdf.engine.xfa.model.config.Config;
import org.aspose.pdf.engine.xfa.model.connectionset.ConnectionSet;
import org.aspose.pdf.engine.xfa.model.datadescription.DataDescription;
import org.aspose.pdf.engine.xfa.model.datasets.Data;
import org.aspose.pdf.engine.xfa.model.datasets.Datasets;
import org.aspose.pdf.engine.xfa.model.localeset.LocaleSet;
import org.aspose.pdf.engine.xfa.model.sourceset.SourceSet;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.aspose.pdf.engine.xfa.packet.XfaPacketSet;
import org.w3c.dom.Document;

/**
 * The consolidated typed view over a whole {@link XfaPacketSet}: typed roots for
 * every XFA grammar a form may carry. This is the single entry point A4 (binding)
 * and A5 (flattening) consume.
 *
 * <p>Each accessor returns the typed root of the corresponding packet, or
 * {@code null} when the packet is absent (or its root does not match the
 * expected grammar). The underlying DOM is shared with the packet set, so edits
 * made through the typed tree are written back by
 * {@link org.aspose.pdf.engine.xfa.packet.XfaPacketWriter}.</p>
 */
public final class XfaModel {

    private final XfaPacketSet packets;

    /**
     * Creates a typed view over a packet set.
     *
     * @param packets the parsed packet set (from A1)
     */
    public XfaModel(XfaPacketSet packets) {
        if (packets == null) {
            throw new IllegalArgumentException("packets must not be null");
        }
        this.packets = packets;
    }

    /**
     * Convenience factory.
     *
     * @param packets the packet set
     * @return a typed view
     */
    public static XfaModel of(XfaPacketSet packets) {
        return new XfaModel(packets);
    }

    /** @return the typed {@code template} root, or {@code null}. */
    public Template template() {
        XfaNode n = XfaNodeFactory.load(packets.template());
        return n instanceof Template ? (Template) n : null;
    }

    /** @return the typed {@code localeSet} root, or {@code null}. */
    public LocaleSet localeSet() {
        XfaNode n = XfaNodeFactory.load(packets.localeSet());
        return n instanceof LocaleSet ? (LocaleSet) n : null;
    }

    /** @return the typed {@code sourceSet} root, or {@code null}. */
    public SourceSet sourceSet() {
        XfaNode n = XfaNodeFactory.load(packets.sourceSet());
        return n instanceof SourceSet ? (SourceSet) n : null;
    }

    /** @return the typed {@code connectionSet} root, or {@code null}. */
    public ConnectionSet connectionSet() {
        XfaNode n = XfaNodeFactory.load(packets.connectionSet());
        return n instanceof ConnectionSet ? (ConnectionSet) n : null;
    }

    /** @return the typed {@code dataDescription} root, or {@code null}. */
    public DataDescription dataDescription() {
        XfaNode n = XfaNodeFactory.load(packets.dataDescription());
        return n instanceof DataDescription ? (DataDescription) n : null;
    }

    /** @return the typed {@code config} root, or {@code null}. */
    public Config config() {
        XfaNode n = XfaNodeFactory.load(packets.config());
        return n instanceof Config ? (Config) n : null;
    }

    /** @return the typed {@code datasets} wrapper root, or {@code null}. */
    public Datasets datasets() {
        XfaNode n = XfaNodeFactory.load(packets.datasets());
        return n instanceof Datasets ? (Datasets) n : null;
    }

    /**
     * The user-data root ({@code <xfa:data>}), from the datasets wrapper if
     * present, else from a standalone {@code data} packet.
     *
     * @return the typed data root, or {@code null}
     */
    public Data data() {
        Datasets ds = datasets();
        if (ds != null && ds.getData() != null) {
            return ds.getData();
        }
        Document doc = packets.getDocument("data");
        XfaNode n = XfaNodeFactory.load(doc);
        return n instanceof Data ? (Data) n : null;
    }

    /** @return the backing packet set. */
    public XfaPacketSet getPackets() {
        return packets;
    }
}
