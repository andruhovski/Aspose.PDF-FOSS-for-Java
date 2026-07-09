package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>barcode</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Barcode extends XfaNode {

    /** Wraps a backing <code>barcode</code> element. */
    public Barcode(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>charEncoding</code> attribute, or null. */
    public String getCharEncoding() { return getString("charEncoding"); }
    /** Sets the <code>charEncoding</code> attribute. */
    public void setCharEncoding(String value) { setAttribute("charEncoding", value); }

    /** Allowed values of the <code>checksum</code> attribute. */
    public enum ChecksumValue {
        V_1MOD10("1mod10"),
        V_1MOD10_1MOD11("1mod10_1mod11"),
        V_2MOD10("2mod10"),
        AUTO("auto"),
        NONE("none");
        private final String v;
        ChecksumValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static ChecksumValue fromValue(String s) {
            for (ChecksumValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>checksum</code> attribute, or null. */
    public ChecksumValue getChecksum() {
        String v = getAttribute("checksum");
        return v == null ? null : ChecksumValue.fromValue(v);
    }
    /** Sets the <code>checksum</code> attribute. */
    public void setChecksum(ChecksumValue value) {
        setAttribute("checksum", value == null ? null : value.value());
    }
    /** @return the raw <code>checksum</code> string, or null. */
    public String getChecksumRaw() { return getAttribute("checksum"); }

    /** @return the typed <code>dataColumnCount</code> attribute, or null. */
    public String getDataColumnCount() { return getString("dataColumnCount"); }
    /** Sets the <code>dataColumnCount</code> attribute. */
    public void setDataColumnCount(String value) { setAttribute("dataColumnCount", value); }

    /** @return the typed <code>dataLength</code> attribute, or null. */
    public String getDataLength() { return getString("dataLength"); }
    /** Sets the <code>dataLength</code> attribute. */
    public void setDataLength(String value) { setAttribute("dataLength", value); }

    /** Allowed values of the <code>dataPrep</code> attribute. */
    public enum DataPrepValue {
        FLATECOMPRESS("flateCompress"),
        NONE("none");
        private final String v;
        DataPrepValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static DataPrepValue fromValue(String s) {
            for (DataPrepValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>dataPrep</code> attribute, or null. */
    public DataPrepValue getDataPrep() {
        String v = getAttribute("dataPrep");
        return v == null ? null : DataPrepValue.fromValue(v);
    }
    /** Sets the <code>dataPrep</code> attribute. */
    public void setDataPrep(DataPrepValue value) {
        setAttribute("dataPrep", value == null ? null : value.value());
    }
    /** @return the raw <code>dataPrep</code> string, or null. */
    public String getDataPrepRaw() { return getAttribute("dataPrep"); }

    /** @return the typed <code>dataRowCount</code> attribute, or null. */
    public String getDataRowCount() { return getString("dataRowCount"); }
    /** Sets the <code>dataRowCount</code> attribute. */
    public void setDataRowCount(String value) { setAttribute("dataRowCount", value); }

    /** @return the typed <code>endChar</code> attribute, or null. */
    public String getEndChar() { return getString("endChar"); }
    /** Sets the <code>endChar</code> attribute. */
    public void setEndChar(String value) { setAttribute("endChar", value); }

    /** @return the typed <code>errorCorrectionLevel</code> attribute, or null. */
    public String getErrorCorrectionLevel() { return getString("errorCorrectionLevel"); }
    /** Sets the <code>errorCorrectionLevel</code> attribute. */
    public void setErrorCorrectionLevel(String value) { setAttribute("errorCorrectionLevel", value); }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>moduleHeight</code> attribute, or null. */
    public XfaMeasurement getModuleHeight() { return getMeasurement("moduleHeight"); }
    /** Sets the <code>moduleHeight</code> attribute. */
    public void setModuleHeight(XfaMeasurement value) { setAttribute("moduleHeight", value == null ? null : value.format()); }

    /** @return the typed <code>moduleWidth</code> attribute, or null. */
    public XfaMeasurement getModuleWidth() { return getMeasurement("moduleWidth"); }
    /** Sets the <code>moduleWidth</code> attribute. */
    public void setModuleWidth(XfaMeasurement value) { setAttribute("moduleWidth", value == null ? null : value.format()); }

    /** Allowed values of the <code>printCheckDigit</code> attribute. */
    public enum PrintCheckDigitValue {
        V_0("0"),
        V_1("1");
        private final String v;
        PrintCheckDigitValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static PrintCheckDigitValue fromValue(String s) {
            for (PrintCheckDigitValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>printCheckDigit</code> attribute, or null. */
    public PrintCheckDigitValue getPrintCheckDigit() {
        String v = getAttribute("printCheckDigit");
        return v == null ? null : PrintCheckDigitValue.fromValue(v);
    }
    /** Sets the <code>printCheckDigit</code> attribute. */
    public void setPrintCheckDigit(PrintCheckDigitValue value) {
        setAttribute("printCheckDigit", value == null ? null : value.value());
    }
    /** @return the raw <code>printCheckDigit</code> string, or null. */
    public String getPrintCheckDigitRaw() { return getAttribute("printCheckDigit"); }

    /** @return the typed <code>rowColumnRatio</code> attribute, or null. */
    public String getRowColumnRatio() { return getString("rowColumnRatio"); }
    /** Sets the <code>rowColumnRatio</code> attribute. */
    public void setRowColumnRatio(String value) { setAttribute("rowColumnRatio", value); }

    /** @return the typed <code>startChar</code> attribute, or null. */
    public String getStartChar() { return getString("startChar"); }
    /** Sets the <code>startChar</code> attribute. */
    public void setStartChar(String value) { setAttribute("startChar", value); }

    /** Allowed values of the <code>textLocation</code> attribute. */
    public enum TextLocationValue {
        ABOVE("above"),
        ABOVEEMBEDDED("aboveEmbedded"),
        BELOW("below"),
        BELOWEMBEDDED("belowEmbedded"),
        NONE("none");
        private final String v;
        TextLocationValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static TextLocationValue fromValue(String s) {
            for (TextLocationValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>textLocation</code> attribute, or null. */
    public TextLocationValue getTextLocation() {
        String v = getAttribute("textLocation");
        return v == null ? null : TextLocationValue.fromValue(v);
    }
    /** Sets the <code>textLocation</code> attribute. */
    public void setTextLocation(TextLocationValue value) {
        setAttribute("textLocation", value == null ? null : value.value());
    }
    /** @return the raw <code>textLocation</code> string, or null. */
    public String getTextLocationRaw() { return getAttribute("textLocation"); }

    /** Allowed values of the <code>truncate</code> attribute. */
    public enum TruncateValue {
        V_0("0"),
        V_1("1");
        private final String v;
        TruncateValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static TruncateValue fromValue(String s) {
            for (TruncateValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>truncate</code> attribute, or null. */
    public TruncateValue getTruncate() {
        String v = getAttribute("truncate");
        return v == null ? null : TruncateValue.fromValue(v);
    }
    /** Sets the <code>truncate</code> attribute. */
    public void setTruncate(TruncateValue value) {
        setAttribute("truncate", value == null ? null : value.value());
    }
    /** @return the raw <code>truncate</code> string, or null. */
    public String getTruncateRaw() { return getAttribute("truncate"); }

    /** @return the typed <code>type</code> attribute, or null. */
    public String getType() { return getString("type"); }
    /** Sets the <code>type</code> attribute. */
    public void setType(String value) { setAttribute("type", value); }

    /** Allowed values of the <code>upsMode</code> attribute. */
    public enum UpsModeValue {
        INTERNATIONALCARRIER("internationalCarrier"),
        SECURESYMBOL("secureSymbol"),
        STANDARDSYMBOL("standardSymbol"),
        USCARRIER("usCarrier");
        private final String v;
        UpsModeValue(String v) { this.v = v; }
        /** @return the XFA attribute string for this value. */
        public String value() { return v; }
        /** @param s raw value @return the matching constant, or null. */
        public static UpsModeValue fromValue(String s) {
            for (UpsModeValue e : values()) { if (e.v.equals(s)) return e; }
            return null;
        }
    }
    /** @return the typed <code>upsMode</code> attribute, or null. */
    public UpsModeValue getUpsMode() {
        String v = getAttribute("upsMode");
        return v == null ? null : UpsModeValue.fromValue(v);
    }
    /** Sets the <code>upsMode</code> attribute. */
    public void setUpsMode(UpsModeValue value) {
        setAttribute("upsMode", value == null ? null : value.value());
    }
    /** @return the raw <code>upsMode</code> string, or null. */
    public String getUpsModeRaw() { return getAttribute("upsMode"); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the typed <code>wideNarrowRatio</code> attribute, or null. */
    public String getWideNarrowRatio() { return getString("wideNarrowRatio"); }
    /** Sets the <code>wideNarrowRatio</code> attribute. */
    public void setWideNarrowRatio(String value) { setAttribute("wideNarrowRatio", value); }

    /** @return the <code>encrypt</code> child (typed), or null. */
    public Encrypt getEncrypt() { return (Encrypt) getChild("encrypt"); }
    /** Ensures and returns the <code>encrypt</code> child. */
    public Encrypt ensureEncrypt() { return (Encrypt) ensureChild("encrypt"); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }
}
