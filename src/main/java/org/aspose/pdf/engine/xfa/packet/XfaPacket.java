package org.aspose.pdf.engine.xfa.packet;

import org.w3c.dom.Document;

/**
 * One XFA packet: its name, its parsed DOM and (when available) the exact
 * source bytes it was read from.
 *
 * <p>XFA is delivered as a set of XML packets inside the AcroForm {@code /XFA}
 * entry (ISO 32000-1 sec 12.7.8): {@code template}, {@code datasets},
 * {@code config}, {@code localeSet}, {@code connectionSet}, {@code sourceSet},
 * {@code dataDescription}, {@code form}, wrapped by an optional {@code xdp}
 * root. Source bytes are retained for byte-stable round-trip where the source
 * was a discrete packet stream.</p>
 */
public final class XfaPacket {

    private final String name;
    private final Document document;
    private final byte[] sourceBytes;
    /** Set once this packet's DOM has been mutated and must be re-serialized on write-back. */
    private boolean dirty;

    /**
     * Creates a packet.
     *
     * @param name        packet name (e.g. {@code "template"})
     * @param document    parsed DOM (may be {@code null} if parsing failed)
     * @param sourceBytes original bytes, or {@code null} if not preserved
     */
    public XfaPacket(String name, Document document, byte[] sourceBytes) {
        this.name = name;
        this.document = document;
        this.sourceBytes = sourceBytes;
    }

    /** @return the packet name. */
    public String getName() {
        return name;
    }

    /** @return the parsed DOM, or {@code null}. */
    public Document getDocument() {
        return document;
    }

    /** @return a copy of the original source bytes, or {@code null} if not preserved. */
    public byte[] getSourceBytes() {
        return sourceBytes == null ? null : sourceBytes.clone();
    }

    /** @return {@code true} if original source bytes are available. */
    public boolean hasSourceBytes() {
        return sourceBytes != null;
    }

    /** @return {@code true} once {@link #markDirty()} has been called (DOM was mutated). */
    public boolean isDirty() {
        return dirty;
    }

    /** Marks this packet as modified so write-back re-serializes it. */
    public void markDirty() {
        this.dirty = true;
    }
}
