package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `medium`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Medium extends XfaNode {

    /// Wraps a backing `medium` element.
    public Medium(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `imagingBBox` attribute, or null.
    public String getImagingBBox() { return getString("imagingBBox"); }
    /// Sets the `imagingBBox` attribute.
    public void setImagingBBox(String value) { setAttribute("imagingBBox", value); }

    /// @return the typed `long` attribute, or null.
    public XfaMeasurement getLong() { return getMeasurement("long"); }
    /// Sets the `long` attribute.
    public void setLong(XfaMeasurement value) { setAttribute("long", value == null ? null : value.format()); }

    /// Allowed values of the `orientation` attribute.
    public enum OrientationValue {
        LANDSCAPE("landscape"),
        PORTRAIT("portrait");
        private final String v;
        OrientationValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static OrientationValue fromValue(String s) {
            for (OrientationValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `orientation` attribute, or null.
    public OrientationValue getOrientation() {
        String v = getAttribute("orientation");
        return v == null ? null : OrientationValue.fromValue(v);
    }
    /// Sets the `orientation` attribute.
    public void setOrientation(OrientationValue value) {
        setAttribute("orientation", value == null ? null : value.value());
    }
    /// @return the raw `orientation` string, or null.
    public String getOrientationRaw() { return getAttribute("orientation"); }

    /// @return the typed `short` attribute, or null.
    public XfaMeasurement getShort() { return getMeasurement("short"); }
    /// Sets the `short` attribute.
    public void setShort(XfaMeasurement value) { setAttribute("short", value == null ? null : value.format()); }

    /// @return the typed `stock` attribute, or null.
    public String getStock() { return getString("stock"); }
    /// Sets the `stock` attribute.
    public void setStock(String value) { setAttribute("stock", value); }

    /// Allowed values of the `trayIn` attribute.
    public enum TrayInValue {
        AUTO("auto"),
        DELEGATE("delegate"),
        PAGEFRONT("pageFront");
        private final String v;
        TrayInValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static TrayInValue fromValue(String s) {
            for (TrayInValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `trayIn` attribute, or null.
    public TrayInValue getTrayIn() {
        String v = getAttribute("trayIn");
        return v == null ? null : TrayInValue.fromValue(v);
    }
    /// Sets the `trayIn` attribute.
    public void setTrayIn(TrayInValue value) {
        setAttribute("trayIn", value == null ? null : value.value());
    }
    /// @return the raw `trayIn` string, or null.
    public String getTrayInRaw() { return getAttribute("trayIn"); }

    /// Allowed values of the `trayOut` attribute.
    public enum TrayOutValue {
        AUTO("auto"),
        DELEGATE("delegate");
        private final String v;
        TrayOutValue(String v) { this.v = v; }
        /// @return the XFA attribute string for this value.
        public String value() { return v; }
        /// @param s raw value @return the matching constant, or null.
        public static TrayOutValue fromValue(String s) {
            for (TrayOutValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /// @return the typed `trayOut` attribute, or null.
    public TrayOutValue getTrayOut() {
        String v = getAttribute("trayOut");
        return v == null ? null : TrayOutValue.fromValue(v);
    }
    /// Sets the `trayOut` attribute.
    public void setTrayOut(TrayOutValue value) {
        setAttribute("trayOut", value == null ? null : value.value());
    }
    /// @return the raw `trayOut` string, or null.
    public String getTrayOutRaw() { return getAttribute("trayOut"); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }
}
