package org.aspose.pdf.engine.security.pkcs7;

import org.aspose.pdf.engine.security.asn1.DERNode;
import org.aspose.pdf.engine.security.asn1.OIDs;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Per-signer information within a PKCS#7 SignedData structure (RFC 2315 §9.2).
 */
public class SignerInfo {

    private byte[] issuerDER;
    private BigInteger serialNumber;
    private String digestAlgorithmOID;
    private String signatureAlgorithmOID;
    private byte[] encryptedDigest;
    private Map<String, byte[]> authenticatedAttributes;
    private byte[] authenticatedAttributesRaw; // DER bytes with SET tag for verification

    SignerInfo() {
        this.authenticatedAttributes = new HashMap<>();
    }

    /** Parses a SignerInfo from a DER SEQUENCE node. */
    static SignerInfo parse(DERNode seq) throws Exception {
        SignerInfo si = new SignerInfo();
        int idx = 0;

        // version INTEGER
        idx++; // skip version

        // issuerAndSerialNumber SEQUENCE { issuer, serial }
        if (idx < seq.getChildCount()) {
            DERNode issuerAndSerial = seq.getChild(idx++);
            if (issuerAndSerial.getChildCount() >= 2) {
                si.issuerDER = issuerAndSerial.getChild(0).getValue();
                si.serialNumber = issuerAndSerial.getChild(1).getInteger();
            }
        }

        // digestAlgorithm AlgorithmIdentifier
        if (idx < seq.getChildCount()) {
            DERNode algId = seq.getChild(idx++);
            if (algId.getChildCount() > 0 && algId.getChild(0).isOID()) {
                si.digestAlgorithmOID = algId.getChild(0).getOID();
            }
        }

        // [0] IMPLICIT authenticatedAttributes (optional)
        if (idx < seq.getChildCount() && seq.getChild(idx).isContextTag(0)) {
            DERNode attrs = seq.getChild(idx++);
            si.authenticatedAttributesRaw = attrs.getValue();
            // Parse attributes
            for (DERNode attr : attrs.getChildren()) {
                if (attr.getChildCount() >= 2 && attr.getChild(0).isOID()) {
                    String oid = attr.getChild(0).getOID();
                    DERNode valueSet = attr.getChild(1);
                    if (valueSet.getChildCount() > 0) {
                        si.authenticatedAttributes.put(oid, valueSet.getChild(0).getValue());
                    }
                }
            }
        }

        // digestEncryptionAlgorithm AlgorithmIdentifier
        if (idx < seq.getChildCount()) {
            DERNode algId = seq.getChild(idx++);
            if (algId.getChildCount() > 0 && algId.getChild(0).isOID()) {
                si.signatureAlgorithmOID = algId.getChild(0).getOID();
            }
        }

        // encryptedDigest OCTET STRING
        if (idx < seq.getChildCount()) {
            si.encryptedDigest = seq.getChild(idx++).getValue();
        }

        return si;
    }

    // ── Accessors ──

    public BigInteger getSerialNumber() { return serialNumber; }
    public String getDigestAlgorithmOID() { return digestAlgorithmOID; }
    public String getSignatureAlgorithmOID() { return signatureAlgorithmOID; }
    public byte[] getEncryptedDigest() { return encryptedDigest; }
    public byte[] getIssuerDER() { return issuerDER; }
    public Map<String, byte[]> getAuthenticatedAttributes() { return authenticatedAttributes; }
    public byte[] getAuthenticatedAttributesRaw() { return authenticatedAttributesRaw; }

    /** Returns the digest algorithm as JCA name. */
    public String getDigestAlgorithm() {
        return OIDs.toJCADigest(digestAlgorithmOID);
    }

    /** Returns the signing time from authenticated attributes. */
    public Date getSigningTime() {
        byte[] timeBytes = authenticatedAttributes.get(OIDs.SIGNING_TIME);
        if (timeBytes == null) return null;
        try {
            String timeStr = new String(timeBytes, java.nio.charset.StandardCharsets.US_ASCII);
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }

    /** Returns the message digest from authenticated attributes. */
    public byte[] getMessageDigest() {
        return authenticatedAttributes.get(OIDs.MESSAGE_DIGEST);
    }

    // ── Setters (for creation) ──
    void setIssuerDER(byte[] issuerDER) { this.issuerDER = issuerDER; }
    void setSerialNumber(BigInteger sn) { this.serialNumber = sn; }
    void setDigestAlgorithmOID(String oid) { this.digestAlgorithmOID = oid; }
    void setSignatureAlgorithmOID(String oid) { this.signatureAlgorithmOID = oid; }
    void setEncryptedDigest(byte[] sig) { this.encryptedDigest = sig; }
    void setAuthenticatedAttributesRaw(byte[] raw) { this.authenticatedAttributesRaw = raw; }
    void putAuthenticatedAttribute(String oid, byte[] value) { authenticatedAttributes.put(oid, value); }
}
