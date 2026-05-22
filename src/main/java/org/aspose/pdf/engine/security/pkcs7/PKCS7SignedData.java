package org.aspose.pdf.engine.security.pkcs7;

import org.aspose.pdf.engine.security.asn1.DEREncoder;
import org.aspose.pdf.engine.security.asn1.DERNode;
import org.aspose.pdf.engine.security.asn1.OIDs;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Logger;

/**
 * Parses and creates PKCS#7 SignedData structures (RFC 2315 §9).
 */
public class PKCS7SignedData {

    private static final Logger LOG = Logger.getLogger(PKCS7SignedData.class.getName());

    private int version = 1;
    private List<String> digestAlgorithms = new ArrayList<>();
    private byte[] contentData;
    private List<X509Certificate> certificates = new ArrayList<>();
    private List<SignerInfo> signerInfos = new ArrayList<>();

    // ── Parse ──

    /** Parses PKCS#7 SignedData from DER-encoded bytes. */
    public static PKCS7SignedData parse(byte[] pkcs7Bytes) throws Exception {
        DERNode root = DERNode.parse(pkcs7Bytes);
        if (!root.isSequence() || root.getChildCount() < 2)
            throw new Exception("Invalid PKCS#7: expected ContentInfo SEQUENCE");

        // ContentInfo: contentType OID + [0] EXPLICIT content
        String contentType = root.getChild(0).getOID();
        if (!OIDs.SIGNED_DATA.equals(contentType))
            throw new Exception("Not a SignedData: " + contentType);

        DERNode signedDataWrapper = root.getChild(1); // [0] EXPLICIT
        DERNode signedData;
        if (signedDataWrapper.isConstructed() && signedDataWrapper.getChildCount() > 0) {
            signedData = signedDataWrapper.getChild(0);
        } else {
            throw new Exception("Missing SignedData content");
        }

        PKCS7SignedData result = new PKCS7SignedData();
        int idx = 0;

        // version
        if (idx < signedData.getChildCount()) {
            result.version = signedData.getChild(idx++).getInteger().intValue();
        }

        // digestAlgorithms SET
        if (idx < signedData.getChildCount()) {
            DERNode algSet = signedData.getChild(idx++);
            for (DERNode alg : algSet.getChildren()) {
                if (alg.getChildCount() > 0 && alg.getChild(0).isOID()) {
                    result.digestAlgorithms.add(alg.getChild(0).getOID());
                }
            }
        }

        // contentInfo (inner)
        if (idx < signedData.getChildCount()) {
            DERNode innerContent = signedData.getChild(idx++);
            // May have [0] EXPLICIT content data
            if (innerContent.getChildCount() >= 2 && innerContent.getChild(1).isContextTag(0)) {
                DERNode dataNode = innerContent.getChild(1);
                if (dataNode.getChildCount() > 0) {
                    result.contentData = dataNode.getChild(0).getValue();
                }
            }
        }

        // [0] IMPLICIT certificates (optional)
        if (idx < signedData.getChildCount() && signedData.getChild(idx).isContextTag(0)) {
            DERNode certsNode = signedData.getChild(idx++);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (DERNode certNode : certsNode.getChildren()) {
                try {
                    // Re-encode the certificate node to get full DER
                    byte[] certDER = DEREncoder.encodeTLV(0x30, certNode.getValue());
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(
                            new ByteArrayInputStream(certDER));
                    result.certificates.add(cert);
                } catch (Exception e) {
                    LOG.fine(() -> "Skipping unparseable certificate: " + e.getMessage());
                }
            }
        }

        // [1] IMPLICIT CRLs (optional) — skip
        if (idx < signedData.getChildCount() && signedData.getChild(idx).isContextTag(1)) {
            idx++;
        }

        // signerInfos SET
        if (idx < signedData.getChildCount()) {
            DERNode siSet = signedData.getChild(idx);
            for (DERNode siNode : siSet.getChildren()) {
                result.signerInfos.add(SignerInfo.parse(siNode));
            }
        }

        return result;
    }

    // ── Create (detached) ──

    /** Creates a detached PKCS#7 signature. */
    public static PKCS7SignedData createDetached(PrivateKey privateKey, X509Certificate certificate,
                                                  X509Certificate[] chain, String digestAlgorithm,
                                                  byte[] dataToSign) throws Exception {
        PKCS7SignedData result = new PKCS7SignedData();
        String digestOID = OIDs.digestToOID(digestAlgorithm);
        result.digestAlgorithms.add(digestOID);

        // Add certificates
        result.certificates.add(certificate);
        if (chain != null) {
            for (X509Certificate c : chain) {
                if (!c.equals(certificate)) result.certificates.add(c);
            }
        }

        // Compute message digest
        MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
        byte[] digest = md.digest(dataToSign);

        // Build authenticated attributes
        byte[] contentTypeAttr = DEREncoder.encodeSequence(
                DEREncoder.encodeOID(OIDs.CONTENT_TYPE),
                DEREncoder.encodeSet(DEREncoder.encodeOID(OIDs.DATA)));
        byte[] messageDigestAttr = DEREncoder.encodeSequence(
                DEREncoder.encodeOID(OIDs.MESSAGE_DIGEST),
                DEREncoder.encodeSet(DEREncoder.encodeOctetString(digest)));
        Date signingTime = new Date();
        byte[] signingTimeAttr = DEREncoder.encodeSequence(
                DEREncoder.encodeOID(OIDs.SIGNING_TIME),
                DEREncoder.encodeSet(DEREncoder.encodeUTCTime(signingTime)));

        // DER-encode attrs as SET for signing (use 0x31 tag)
        byte[] attrsContent = concat(contentTypeAttr, messageDigestAttr, signingTimeAttr);
        byte[] attrsForSigning = DEREncoder.encodeSet(contentTypeAttr, messageDigestAttr, signingTimeAttr);

        // Sign attributes
        String sigAlg = OIDs.signatureAlgorithmForDigest(digestAlgorithm);
        Signature sig = Signature.getInstance(sigAlg);
        sig.initSign(privateKey);
        sig.update(attrsForSigning);
        byte[] signatureBytes = sig.sign();

        // Build SignerInfo
        SignerInfo si = new SignerInfo();
        si.setIssuerDER(certificate.getIssuerX500Principal().getEncoded());
        si.setSerialNumber(certificate.getSerialNumber());
        si.setDigestAlgorithmOID(digestOID);
        si.setSignatureAlgorithmOID(OIDs.signatureToOID(sigAlg));
        si.setEncryptedDigest(signatureBytes);
        si.setAuthenticatedAttributesRaw(attrsContent);
        si.putAuthenticatedAttribute(OIDs.CONTENT_TYPE, DEREncoder.encodeOID(OIDs.DATA));
        si.putAuthenticatedAttribute(OIDs.MESSAGE_DIGEST, digest);
        // Store signing time for retrieval
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyMMddHHmmss'Z'");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        si.putAuthenticatedAttribute(OIDs.SIGNING_TIME,
                sdf.format(signingTime).getBytes(java.nio.charset.StandardCharsets.US_ASCII));

        result.signerInfos.add(si);
        return result;
    }

    // ── Verify ──

    /** Verifies the signature against original data. */
    public boolean verify(byte[] originalData) throws Exception {
        if (signerInfos.isEmpty()) return false;
        SignerInfo si = signerInfos.get(0);

        // Find matching certificate
        X509Certificate cert = findCertificate(si);
        if (cert == null) return false;

        String digestAlg = si.getDigestAlgorithm();

        // Verify message digest attribute
        byte[] storedDigest = si.getMessageDigest();
        if (storedDigest != null && originalData != null) {
            MessageDigest md = MessageDigest.getInstance(digestAlg);
            byte[] computed = md.digest(originalData);
            if (!MessageDigest.isEqual(computed, storedDigest)) return false;
        }

        // Verify signature over authenticated attributes
        byte[] attrsRaw = si.getAuthenticatedAttributesRaw();
        byte[] encDigest = si.getEncryptedDigest();
        if (encDigest == null) return false;

        String sigAlg = OIDs.toJCASignature(si.getSignatureAlgorithmOID());
        if ("RSA".equals(sigAlg)) sigAlg = OIDs.signatureAlgorithmForDigest(digestAlg);
        Signature verifier = Signature.getInstance(sigAlg);
        verifier.initVerify(cert.getPublicKey());

        if (attrsRaw != null) {
            // Re-encode with SET tag (0x31) for verification (RFC 2315 §9.3:
            // when authenticated attributes are present, the signature is over
            // the DER encoding of the SET OF Attribute structure).
            byte[] attrsForVerify = DEREncoder.encodeTLV(0x31, attrsRaw);
            verifier.update(attrsForVerify);
            return verifier.verify(encDigest);
        }

        // No authenticated attributes — signature is directly over the content.
        // RFC 2315 §9.3: the content being signed is the encapsulated content if
        // present (SignedData.encapContentInfo.eContent), otherwise the externally
        // supplied detached content (originalData here). The JCA verifier hashes
        // the data internally per digestAlg before checking the signature.
        byte[] toSign = (contentData != null) ? contentData : originalData;
        if (toSign == null) return false;
        verifier.update(toSign);
        return verifier.verify(encDigest);
    }

    /**
     * Returns the encapsulated content data, if any. Used by callers that need
     * to verify the binding between detached external content and the inner
     * digest (e.g. {@code adbe.pkcs7.sha1}, where {@code eContent} = {@code SHA-1(byteRange)}).
     *
     * @return the encapsulated content bytes, or {@code null}
     */
    public byte[] getEncapsulatedContent() {
        return contentData;
    }

    // ── Encode ──

    /** Encodes this SignedData to DER bytes. */
    public byte[] encode() throws Exception {
        // Build digest algorithms SET
        byte[][] algIds = new byte[digestAlgorithms.size()][];
        for (int i = 0; i < digestAlgorithms.size(); i++) {
            algIds[i] = DEREncoder.encodeAlgorithmIdentifier(digestAlgorithms.get(i));
        }
        byte[] digestAlgSet = DEREncoder.encodeSet(algIds);

        // Build content info (detached: just data OID, no content)
        byte[] innerContent = DEREncoder.encodeSequence(DEREncoder.encodeOID(OIDs.DATA));

        // Build certificates [0] IMPLICIT
        byte[][] certDERs = new byte[certificates.size()][];
        for (int i = 0; i < certificates.size(); i++) {
            certDERs[i] = certificates.get(i).getEncoded();
            // Strip outer SEQUENCE tag to get raw content for IMPLICIT
            DERNode certNode = DERNode.parse(certDERs[i]);
            certDERs[i] = DEREncoder.encodeTLV(0x30, certNode.getValue());
        }
        byte[] certsImplicit = certDERs.length > 0
                ? DEREncoder.encodeContextTagImplicit(0, concat(certDERs))
                : new byte[0];

        // Build signer infos SET
        byte[][] siDERs = new byte[signerInfos.size()][];
        for (int i = 0; i < signerInfos.size(); i++) {
            siDERs[i] = encodeSignerInfo(signerInfos.get(i));
        }
        byte[] siSet = DEREncoder.encodeSet(siDERs);

        // Build SignedData SEQUENCE
        byte[] signedDataSeq = certsImplicit.length > 0
                ? DEREncoder.encodeSequence(
                    DEREncoder.encodeInteger(version), digestAlgSet, innerContent,
                    certsImplicit, siSet)
                : DEREncoder.encodeSequence(
                    DEREncoder.encodeInteger(version), digestAlgSet, innerContent, siSet);

        // Wrap in ContentInfo
        return DEREncoder.encodeSequence(
                DEREncoder.encodeOID(OIDs.SIGNED_DATA),
                DEREncoder.encodeContextTag(0, signedDataSeq));
    }

    private byte[] encodeSignerInfo(SignerInfo si) {
        byte[] version = DEREncoder.encodeInteger(1);
        byte[] issuerAndSerial = DEREncoder.encodeSequence(
                DEREncoder.encodeTLV(0x30, si.getIssuerDER()),
                DEREncoder.encodeInteger(si.getSerialNumber()));
        byte[] digestAlg = DEREncoder.encodeAlgorithmIdentifier(si.getDigestAlgorithmOID());

        // Authenticated attributes [0] IMPLICIT
        byte[] authAttrs = new byte[0];
        if (si.getAuthenticatedAttributesRaw() != null) {
            authAttrs = DEREncoder.encodeTLV(0xA0, si.getAuthenticatedAttributesRaw());
        }

        byte[] sigAlg = DEREncoder.encodeAlgorithmIdentifier(
                si.getSignatureAlgorithmOID() != null ? si.getSignatureAlgorithmOID() : OIDs.RSA_ENCRYPTION);
        byte[] encDigest = DEREncoder.encodeOctetString(si.getEncryptedDigest());

        return authAttrs.length > 0
                ? DEREncoder.encodeSequence(version, issuerAndSerial, digestAlg, authAttrs, sigAlg, encDigest)
                : DEREncoder.encodeSequence(version, issuerAndSerial, digestAlg, sigAlg, encDigest);
    }

    private X509Certificate findCertificate(SignerInfo si) {
        for (X509Certificate cert : certificates) {
            if (cert.getSerialNumber().equals(si.getSerialNumber())) return cert;
        }
        return certificates.isEmpty() ? null : certificates.get(0);
    }

    // ── Accessors ──

    public List<SignerInfo> getSignerInfos() { return signerInfos; }
    public List<X509Certificate> getCertificates() { return certificates; }
    public int getVersion() { return version; }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) total += a.length;
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) { System.arraycopy(a, 0, result, pos, a.length); pos += a.length; }
        return result;
    }
}
