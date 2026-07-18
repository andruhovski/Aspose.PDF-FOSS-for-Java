package org.aspose.pdf.engine.xfa.packet;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// The full set of XFA packets parsed from a PDF's `/XFA` entry, with
/// typed accessors per packet plus the assembled `xdp` document.
///
/// This is the typed model the public `forms.xfa.XfaForm` adapter
/// delegates to. Packet order is preserved (it mirrors the source array / XDP
/// child order), which matters for byte-stable write-back.
public final class XfaPacketSet {

    private final Map<String, XfaPacket> packets = new LinkedHashMap<>();
    private Document xdp;

    /// Adds or replaces a packet.
    ///
    /// @param packet the packet (keyed by [XfaPacket#getName()])
    public void put(XfaPacket packet) {
        packets.put(packet.getName(), packet);
    }

    /// Returns a packet by name.
    ///
    /// @param name packet name
    /// @return the packet, or `null` if absent
    public XfaPacket get(String name) {
        return name == null ? null : packets.get(name);
    }

    /// Returns a packet's DOM by name.
    ///
    /// @param name packet name
    /// @return the DOM, or `null` if the packet is absent
    public Document getDocument(String name) {
        XfaPacket p = get(name);
        return p == null ? null : p.getDocument();
    }

    /// @return `true` if a packet with the given name is present.
    public boolean has(String name) {
        return packets.containsKey(name);
    }

    /// @return packet names in source order.
    public List<String> names() {
        return new ArrayList<>(packets.keySet());
    }

    /// @return all packets in source order.
    public List<XfaPacket> all() {
        return new ArrayList<>(packets.values());
    }

    /// @return the assembled XDP document (or the parsed XDP if the source was a single stream).
    public Document getXdp() {
        return xdp;
    }

    /// Sets the assembled XDP document.
    ///
    /// @param xdp the XDP document
    public void setXdp(Document xdp) {
        this.xdp = xdp;
    }

    // ── typed accessors ──

    /// @return the `template` packet DOM, or `null`.
    public Document template() { return getDocument("template"); }

    /// @return the `datasets` packet DOM, or `null`.
    public Document datasets() { return getDocument("datasets"); }

    /// @return the `config` packet DOM, or `null`.
    public Document config() { return getDocument("config"); }

    /// @return the `localeSet` packet DOM, or `null`.
    public Document localeSet() { return getDocument("localeSet"); }

    /// @return the `connectionSet` packet DOM, or `null`.
    public Document connectionSet() { return getDocument("connectionSet"); }

    /// @return the `sourceSet` packet DOM, or `null`.
    public Document sourceSet() { return getDocument("sourceSet"); }

    /// @return the `dataDescription` packet DOM, or `null`.
    public Document dataDescription() { return getDocument("dataDescription"); }

    /// @return the `form` packet DOM, or `null`.
    public Document form() { return getDocument("form"); }

    /// @return the `xdp` document, or `null`.
    public Document xdp() { return xdp; }
}
