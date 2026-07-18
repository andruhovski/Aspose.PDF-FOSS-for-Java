package org.aspose.pdf.tests;

import org.aspose.pdf.engine.security.asn1.DERNode;
import org.aspose.pdf.engine.security.asn1.OIDs;
import org.aspose.pdf.engine.security.pkcs7.PKCS7SignedData;
import org.aspose.pdf.engine.security.pkcs7.SignerInfo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for PKCS#7 SignedData creation, encoding, parsing, and verification.
public class PKCS7Test {

    private static PrivateKey privateKey;
    private static X509Certificate certificate;

    @BeforeAll
    public static void generateSelfSignedCert() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        privateKey = kp.getPrivate();

        // Create self-signed certificate using JDK internal API workaround:
        // Build a minimal X.509 v3 certificate using sun.security or manual DER
        certificate = createSelfSignedCertificate(kp, "CN=Test Signer, O=OpenPDF Test");
    }

    @Test
    public void testCreateAndVerifyDetachedSignature() throws Exception {
        byte[] data = "Hello, World! This is test data for signing.".getBytes();

        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", data);

        assertNotNull(pkcs7);
        assertEquals(1, pkcs7.getVersion());
        assertFalse(pkcs7.getSignerInfos().isEmpty());
        assertFalse(pkcs7.getCertificates().isEmpty());

        // Verify
        assertTrue(pkcs7.verify(data), "Signature should verify against original data");
    }

    @Test
    public void testEncodeAndParseRoundTrip() throws Exception {
        byte[] data = "Round-trip test data".getBytes();

        PKCS7SignedData original = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", data);

        // Encode to DER
        byte[] encoded = original.encode();
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);

        // Parse back
        PKCS7SignedData parsed = PKCS7SignedData.parse(encoded);
        assertNotNull(parsed);
        assertEquals(1, parsed.getVersion());
        assertEquals(1, parsed.getSignerInfos().size());
        assertTrue(parsed.getCertificates().size() >= 1);

        // Verify parsed signature
        assertTrue(parsed.verify(data), "Parsed signature should verify");
    }

    @Test
    public void testExtractSignerInfo() throws Exception {
        byte[] data = "Signer info test".getBytes();

        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", data);

        byte[] encoded = pkcs7.encode();
        PKCS7SignedData parsed = PKCS7SignedData.parse(encoded);

        List<SignerInfo> signerInfos = parsed.getSignerInfos();
        assertEquals(1, signerInfos.size());

        SignerInfo si = signerInfos.get(0);
        assertNotNull(si.getSerialNumber());
        assertNotNull(si.getDigestAlgorithmOID());
        assertNotNull(si.getEncryptedDigest());
        assertTrue(si.getEncryptedDigest().length > 0);
    }

    @Test
    public void testExtractCertificates() throws Exception {
        byte[] data = "Certificate extraction test".getBytes();

        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", data);

        byte[] encoded = pkcs7.encode();
        PKCS7SignedData parsed = PKCS7SignedData.parse(encoded);

        List<X509Certificate> certs = parsed.getCertificates();
        assertFalse(certs.isEmpty());
        X509Certificate parsedCert = certs.get(0);
        assertEquals(certificate.getSerialNumber(), parsedCert.getSerialNumber());
        assertEquals(certificate.getSubjectX500Principal(), parsedCert.getSubjectX500Principal());
    }

    @Test
    public void testVerificationFailsWithTamperedData() throws Exception {
        byte[] originalData = "Original data".getBytes();
        byte[] tamperedData = "Tampered data".getBytes();

        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", originalData);

        assertFalse(pkcs7.verify(tamperedData),
                "Verification should fail with tampered data");
    }

    @Test
    public void testSigningTime() throws Exception {
        byte[] data = "Signing time test".getBytes();
        Date before = new Date(System.currentTimeMillis() - 2000);

        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", data);

        Date after = new Date(System.currentTimeMillis() + 2000);

        SignerInfo si = pkcs7.getSignerInfos().get(0);
        Date signingTime = si.getSigningTime();
        assertNotNull(signingTime);
        assertTrue(signingTime.after(before) || signingTime.equals(before));
        assertTrue(signingTime.before(after) || signingTime.equals(after));
    }

    @Test
    public void testMessageDigestAttribute() throws Exception {
        byte[] data = "Digest attribute test".getBytes();

        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", data);

        SignerInfo si = pkcs7.getSignerInfos().get(0);
        byte[] messageDigest = si.getMessageDigest();
        assertNotNull(messageDigest);
        assertTrue(messageDigest.length > 0);

        // Verify digest matches SHA-256 of data
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] expectedDigest = md.digest(data);
        assertArrayEquals(expectedDigest, messageDigest);
    }

    @Test
    public void testDERStructureIsValidPKCS7() throws Exception {
        byte[] data = "DER structure test".getBytes();

        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", data);
        byte[] encoded = pkcs7.encode();

        // Parse as raw DER and verify structure
        DERNode root = DERNode.parse(encoded);
        assertTrue(root.isSequence(), "Root should be SEQUENCE (ContentInfo)");
        assertEquals(2, root.getChildCount(), "ContentInfo should have 2 children");

        // First child: contentType OID
        assertTrue(root.getChild(0).isOID());
        assertEquals(OIDs.SIGNED_DATA, root.getChild(0).getOID());

        // Second child: [0] EXPLICIT SignedData
        assertTrue(root.getChild(1).isContextTag(0));
    }

    @Test
    public void testWithCertificateChain() throws Exception {
        // Create a second key pair (simulating chain)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp2 = kpg.generateKeyPair();
        X509Certificate cert2 = createSelfSignedCertificate(kp2, "CN=Intermediate CA, O=Test");

        byte[] data = "Chain test".getBytes();
        X509Certificate[] chain = {certificate, cert2};

        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, chain, "SHA-256", data);

        byte[] encoded = pkcs7.encode();
        PKCS7SignedData parsed = PKCS7SignedData.parse(encoded);

        // Should have 2 certificates
        assertTrue(parsed.getCertificates().size() >= 2,
                "Should contain certificate chain");
    }

    @Test
    public void testDigestAlgorithmExtractedFromParsedSignerInfo() throws Exception {
        byte[] data = "Digest algorithm test".getBytes();
        PKCS7SignedData pkcs7 = PKCS7SignedData.createDetached(
                privateKey, certificate, null, "SHA-256", data);
        byte[] encoded = pkcs7.encode();
        PKCS7SignedData parsed = PKCS7SignedData.parse(encoded);

        SignerInfo si = parsed.getSignerInfos().get(0);
        assertEquals("SHA-256", si.getDigestAlgorithm());
    }

    // ── Helper: self-signed X.509 certificate ──

    /// Creates a minimal self-signed X.509 v3 certificate by manually
    /// constructing the DER encoding. No sun.security internals needed.
    static X509Certificate createSelfSignedCertificate(KeyPair keyPair, String dn) throws Exception {
        // Build TBSCertificate DER
        byte[] serialBytes = org.aspose.pdf.engine.security.asn1.DEREncoder.encodeInteger(
                BigInteger.valueOf(System.currentTimeMillis()));

        // SHA256withRSA AlgorithmIdentifier
        byte[] sigAlgId = org.aspose.pdf.engine.security.asn1.DEREncoder.encodeAlgorithmIdentifier(
                "1.2.840.113549.1.1.11");

        // Issuer/Subject: minimal RDN from dn string
        byte[] nameBytes = encodeDN(dn);

        // Validity: not before yesterday, not after +1 year
        Date notBefore = new Date(System.currentTimeMillis() - 86400000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 86400000L);
        byte[] validity = org.aspose.pdf.engine.security.asn1.DEREncoder.encodeSequence(
                org.aspose.pdf.engine.security.asn1.DEREncoder.encodeUTCTime(notBefore),
                org.aspose.pdf.engine.security.asn1.DEREncoder.encodeUTCTime(notAfter));

        // SubjectPublicKeyInfo: use the JDK-encoded form directly
        byte[] spki = keyPair.getPublic().getEncoded();

        // Version: [0] EXPLICIT INTEGER 2 (v3)
        byte[] version = org.aspose.pdf.engine.security.asn1.DEREncoder.encodeContextTag(0,
                org.aspose.pdf.engine.security.asn1.DEREncoder.encodeInteger(BigInteger.valueOf(2)));

        // TBSCertificate SEQUENCE
        byte[] tbsCert = org.aspose.pdf.engine.security.asn1.DEREncoder.encodeSequence(
                version, serialBytes, sigAlgId, nameBytes, validity, nameBytes, spki);

        // Sign TBSCertificate
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(keyPair.getPrivate());
        sig.update(tbsCert);
        byte[] signatureValue = sig.sign();

        // Certificate SEQUENCE { tbsCert, signatureAlgorithm, signatureValue }
        byte[] certDER = org.aspose.pdf.engine.security.asn1.DEREncoder.encodeSequence(
                tbsCert, sigAlgId,
                org.aspose.pdf.engine.security.asn1.DEREncoder.encodeBitString(signatureValue));

        // Parse via JDK CertificateFactory
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certDER));
    }

    /// Encodes a simple DN string like "CN=Test, O=Org" into DER.
    private static byte[] encodeDN(String dn) {
        // Parse key=value pairs
        String[] parts = dn.split(",\\s*");
        java.io.ByteArrayOutputStream rdns = new java.io.ByteArrayOutputStream();
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;
            String oid = dnAttributeOID(kv[0].trim());
            byte[] attrValue = org.aspose.pdf.engine.security.asn1.DEREncoder.encodePrintableString(kv[1].trim());
            byte[] attrTypeAndValue = org.aspose.pdf.engine.security.asn1.DEREncoder.encodeSequence(
                    org.aspose.pdf.engine.security.asn1.DEREncoder.encodeOID(oid), attrValue);
            byte[] rdn = org.aspose.pdf.engine.security.asn1.DEREncoder.encodeSet(attrTypeAndValue);
            rdns.write(rdn, 0, rdn.length);
        }
        return org.aspose.pdf.engine.security.asn1.DEREncoder.encodeTLV(0x30, rdns.toByteArray());
    }

    private static String dnAttributeOID(String attr) {
        switch (attr.toUpperCase()) {
            case "CN": return "2.5.4.3";
            case "O":  return "2.5.4.10";
            case "OU": return "2.5.4.11";
            case "C":  return "2.5.4.6";
            case "ST": return "2.5.4.8";
            case "L":  return "2.5.4.7";
            default:   return "2.5.4.3";
        }
    }

}
