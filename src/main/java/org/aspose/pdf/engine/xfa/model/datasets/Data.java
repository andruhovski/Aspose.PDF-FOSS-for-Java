package org.aspose.pdf.engine.xfa.model.datasets;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>data</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Data extends XfaNode {

    /** Wraps a backing <code>data</code> element. */
    public Data(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** Allowed values of the <code>xfd:dataNode</code> attribute. */
    public enum Xfd_dataNodeValue {
        DATAVALUE("dataValue"),
        DATAGROUP("dataGroup");
        private final String v;
        Xfd_dataNodeValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static Xfd_dataNodeValue fromValue(String s) {
            for (Xfd_dataNodeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>xfd:dataNode</code> attribute, or null. */
    public Xfd_dataNodeValue getXfd_dataNode() {
        String v = getAttribute("xfd:dataNode");
        return v == null ? null : Xfd_dataNodeValue.fromValue(v);
    }
    /** Sets the <code>xfd:dataNode</code> attribute. */
    public void setXfd_dataNode(Xfd_dataNodeValue value) {
        setAttribute("xfd:dataNode", value == null ? null : value.value());
    }
    /** @return the raw <code>xfd:dataNode</code> string, or null. */
    public String getXfd_dataNodeRaw() { return getAttribute("xfd:dataNode"); }
}
