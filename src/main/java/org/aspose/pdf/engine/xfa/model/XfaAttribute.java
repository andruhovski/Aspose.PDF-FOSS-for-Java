package org.aspose.pdf.engine.xfa.model;

/// A typed accessor for one attribute of an [XfaNode], backed by a
/// [Codec] that coerces between the attribute's string form and a Java
/// type ([String], [Integer], [Boolean], [XfaMeasurement]
/// or a generated enum). Reading an absent attribute yields `null`.
///
/// @param <T> the decoded Java type
public final class XfaAttribute<T> {

    /// Bidirectional string codec for an attribute value.
    ///
    /// @param <T> decoded type
    public interface Codec<T> {
        /// Decodes the raw string (`null`-safe at the call site).
        T decode(String raw);
        /// Encodes a value back to its attribute string.
        String encode(T value);
    }

    /// Identity codec for string attributes.
    public static final Codec<String> STRING = new Codec<String>() {
        public String decode(String raw) { return raw; }
        public String encode(String value) { return value; }
    };

    /// Codec for XFA integer attributes.
    public static final Codec<Integer> INTEGER = new Codec<Integer>() {
        public Integer decode(String raw) {
            try {
                return Integer.valueOf(raw.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        public String encode(Integer value) { return value == null ? null : value.toString(); }
    };

    /// Codec for XFA boolean attributes (`0/1` or `true/false`).
    public static final Codec<Boolean> BOOLEAN = new Codec<Boolean>() {
        public Boolean decode(String raw) {
            String v = raw.trim();
            if (v.equals("1") || v.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            }
            if (v.equals("0") || v.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            }
            return null;
        }
        public String encode(Boolean value) { return value == null ? null : (value ? "1" : "0"); }
    };

    /// Codec for XFA measurement attributes.
    public static final Codec<XfaMeasurement> MEASUREMENT = new Codec<XfaMeasurement>() {
        public XfaMeasurement decode(String raw) { return XfaMeasurement.parse(raw); }
        public String encode(XfaMeasurement value) { return value == null ? null : value.format(); }
    };

    private final XfaNode node;
    private final String name;
    private final Codec<T> codec;

    /// Binds a typed accessor to a node attribute.
    ///
    /// @param node  owning node
    /// @param name  attribute name
    /// @param codec value codec
    public XfaAttribute(XfaNode node, String name, Codec<T> codec) {
        this.node = node;
        this.name = name;
        this.codec = codec;
    }

    /// @return the decoded value, or `null` if the attribute is absent.
    public T get() {
        String raw = node.getAttribute(name);
        return raw == null ? null : codec.decode(raw);
    }

    /// Sets the value (removes the attribute when `value` is null).
    ///
    /// @param value the value
    public void set(T value) {
        node.setAttribute(name, value == null ? null : codec.encode(value));
    }

    /// @return the raw attribute string, or `null`.
    public String getRaw() {
        return node.getAttribute(name);
    }

    /// @return `true` if the attribute is present.
    public boolean isPresent() {
        return node.getAttribute(name) != null;
    }
}
