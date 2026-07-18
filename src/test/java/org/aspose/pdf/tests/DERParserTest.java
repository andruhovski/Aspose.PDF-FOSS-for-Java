package org.aspose.pdf.tests;

import org.aspose.pdf.engine.security.asn1.DEREncoder;
import org.aspose.pdf.engine.security.asn1.DERNode;
import org.aspose.pdf.engine.security.asn1.OIDs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for ASN.1 DER parser and encoder.
public class DERParserTest {

    @Test
    public void testParseSequenceWithIntegerAndOID() throws IOException {
        // Encode SEQUENCE { INTEGER(42), OID(sha256) }
        byte[] encoded = DEREncoder.encodeSequence(
                DEREncoder.encodeInteger(BigInteger.valueOf(42)),
                DEREncoder.encodeOID(OIDs.SHA256));

        DERNode root = DERNode.parse(encoded);
        assertTrue(root.isSequence());
        assertEquals(2, root.getChildCount());
        assertTrue(root.getChild(0).isInteger());
        assertEquals(BigInteger.valueOf(42), root.getChild(0).getInteger());
        assertTrue(root.getChild(1).isOID());
        assertEquals(OIDs.SHA256, root.getChild(1).getOID());
    }

    @Test
    public void testParseNestedSequence() throws IOException {
        byte[] inner = DEREncoder.encodeSequence(
                DEREncoder.encodeInteger(BigInteger.valueOf(1)));
        byte[] outer = DEREncoder.encodeSequence(inner,
                DEREncoder.encodeOctetString(new byte[]{0x01, 0x02, 0x03}));

        DERNode root = DERNode.parse(outer);
        assertTrue(root.isSequence());
        assertEquals(2, root.getChildCount());

        DERNode innerNode = root.getChild(0);
        assertTrue(innerNode.isSequence());
        assertEquals(1, innerNode.getChildCount());
        assertEquals(BigInteger.valueOf(1), innerNode.getChild(0).getInteger());

        DERNode octet = root.getChild(1);
        assertTrue(octet.isOctetString());
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, octet.getValue());
    }

    @Test
    public void testParseOctetString() throws IOException {
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
        byte[] encoded = DEREncoder.encodeOctetString(data);

        DERNode node = DERNode.parse(encoded);
        assertTrue(node.isOctetString());
        assertArrayEquals(data, node.getValue());
    }

    @Test
    public void testParseBitString() throws IOException {
        byte[] data = {0x03, 0x04, 0x05};
        byte[] encoded = DEREncoder.encodeBitString(data);

        DERNode node = DERNode.parse(encoded);
        assertTrue(node.isBitString());
        // Value includes unused-bits byte (0x00) prefix
        assertEquals(data.length + 1, node.getValue().length);
        assertEquals(0x00, node.getValue()[0]); // unused bits byte
    }

    @Test
    public void testParseNull() throws IOException {
        byte[] encoded = DEREncoder.encodeNull();
        DERNode node = DERNode.parse(encoded);
        assertTrue(node.isNull());
        assertEquals(0, node.getValue().length);
    }

    @Test
    public void testParseMultiByteLength() throws IOException {
        // Create an OCTET STRING with 200 bytes (requires 2-byte length encoding)
        byte[] data = new byte[200];
        for (int i = 0; i < 200; i++) data[i] = (byte) (i & 0xFF);
        byte[] encoded = DEREncoder.encodeOctetString(data);

        DERNode node = DERNode.parse(encoded);
        assertTrue(node.isOctetString());
        assertEquals(200, node.getValue().length);
        assertArrayEquals(data, node.getValue());
    }

    @Test
    public void testParseLargeMultiByteLength() throws IOException {
        // Create an OCTET STRING with 500 bytes (requires 0x82 length encoding)
        byte[] data = new byte[500];
        for (int i = 0; i < 500; i++) data[i] = (byte) (i & 0xFF);
        byte[] encoded = DEREncoder.encodeOctetString(data);

        DERNode node = DERNode.parse(encoded);
        assertTrue(node.isOctetString());
        assertEquals(500, node.getValue().length);
        assertArrayEquals(data, node.getValue());
    }

    @Test
    public void testEncodeDecodeRoundTrip() throws IOException {
        byte[] original = DEREncoder.encodeSequence(
                DEREncoder.encodeInteger(BigInteger.valueOf(12345)),
                DEREncoder.encodeOID(OIDs.SIGNED_DATA),
                DEREncoder.encodeOctetString(new byte[]{1, 2, 3}));

        DERNode parsed = DERNode.parse(original);
        assertTrue(parsed.isSequence());
        assertEquals(3, parsed.getChildCount());
        assertEquals(BigInteger.valueOf(12345), parsed.getChild(0).getInteger());
        assertEquals(OIDs.SIGNED_DATA, parsed.getChild(1).getOID());
        assertArrayEquals(new byte[]{1, 2, 3}, parsed.getChild(2).getValue());
    }

    @Test
    public void testOIDEncodingDecoding() throws IOException {
        String[] oids = {
                OIDs.DATA,            // 1.2.840.113549.1.7.1
                OIDs.SIGNED_DATA,     // 1.2.840.113549.1.7.2
                OIDs.SHA256,          // 2.16.840.1.101.3.4.2.1
                OIDs.SHA512,          // 2.16.840.1.101.3.4.2.3
                OIDs.RSA_ENCRYPTION,  // 1.2.840.113549.1.1.1
                OIDs.SHA256_WITH_RSA  // 1.2.840.113549.1.1.11
        };
        for (String oid : oids) {
            byte[] encoded = DEREncoder.encodeOID(oid);
            DERNode node = DERNode.parse(encoded);
            assertTrue(node.isOID(), "Should be OID tag for " + oid);
            assertEquals(oid, node.getOID(), "OID round-trip failed for " + oid);
        }
    }

    @Test
    public void testContextTag() throws IOException {
        byte[] inner = DEREncoder.encodeInteger(BigInteger.valueOf(99));
        byte[] encoded = DEREncoder.encodeContextTag(0, inner);

        DERNode node = DERNode.parse(encoded);
        assertTrue(node.isContextTag(0));
        assertFalse(node.isContextTag(1));
        assertTrue(node.isConstructed());
        assertEquals(1, node.getChildCount());
        assertEquals(BigInteger.valueOf(99), node.getChild(0).getInteger());
    }

    @Test
    public void testSetEncoding() throws IOException {
        byte[] elem1 = DEREncoder.encodeOID(OIDs.SHA256);
        byte[] elem2 = DEREncoder.encodeOID(OIDs.SHA1);
        byte[] encoded = DEREncoder.encodeSet(elem1, elem2);

        DERNode node = DERNode.parse(encoded);
        assertTrue(node.isSet());
        assertEquals(2, node.getChildCount());
    }

    @Test
    public void testAlgorithmIdentifier() throws IOException {
        byte[] algId = DEREncoder.encodeAlgorithmIdentifier(OIDs.SHA256);
        DERNode node = DERNode.parse(algId);
        assertTrue(node.isSequence());
        assertEquals(2, node.getChildCount());
        assertEquals(OIDs.SHA256, node.getChild(0).getOID());
        assertTrue(node.getChild(1).isNull());
    }

    @Test
    public void testUTCTime() throws IOException {
        Date now = new Date();
        byte[] encoded = DEREncoder.encodeUTCTime(now);
        DERNode node = DERNode.parse(encoded);
        assertEquals(0x17, node.getTag());
        String timeStr = node.getString();
        assertTrue(timeStr.endsWith("Z"), "UTC time should end with Z");
    }

    @Test
    public void testPrintableString() throws IOException {
        byte[] encoded = DEREncoder.encodePrintableString("Hello");
        DERNode node = DERNode.parse(encoded);
        assertEquals(0x13, node.getTag());
        assertEquals("Hello", node.getString());
    }

    @Test
    public void testIntegerEncodingSignHandling() throws IOException {
        // Negative integer
        byte[] encoded = DEREncoder.encodeInteger(BigInteger.valueOf(-1));
        DERNode node = DERNode.parse(encoded);
        assertEquals(BigInteger.valueOf(-1), node.getInteger());

        // Large positive integer
        BigInteger big = new BigInteger("123456789012345678901234567890");
        encoded = DEREncoder.encodeInteger(big);
        node = DERNode.parse(encoded);
        assertEquals(big, node.getInteger());
    }

    @Test
    public void testLengthEncoding() {
        // Short form (< 128)
        byte[] len = DEREncoder.encodeLength(127);
        assertEquals(1, len.length);
        assertEquals(127, len[0] & 0xFF);

        // Two-byte form (128..255)
        len = DEREncoder.encodeLength(200);
        assertEquals(2, len.length);
        assertEquals(0x81, len[0] & 0xFF);
        assertEquals(200, len[1] & 0xFF);

        // Three-byte form (256..65535)
        len = DEREncoder.encodeLength(1000);
        assertEquals(3, len.length);
        assertEquals(0x82, len[0] & 0xFF);
    }

    @Test
    public void testOIDsToJCAMapping() {
        assertEquals("SHA-256", OIDs.toJCADigest(OIDs.SHA256));
        assertEquals("SHA-1", OIDs.toJCADigest(OIDs.SHA1));
        assertEquals("SHA-384", OIDs.toJCADigest(OIDs.SHA384));
        assertEquals("SHA-512", OIDs.toJCADigest(OIDs.SHA512));
        assertEquals("MD5", OIDs.toJCADigest(OIDs.MD5));

        assertEquals("SHA256withRSA", OIDs.toJCASignature(OIDs.SHA256_WITH_RSA));
        assertEquals("SHA1withRSA", OIDs.toJCASignature(OIDs.SHA1_WITH_RSA));

        assertEquals(OIDs.SHA256, OIDs.digestToOID("SHA-256"));
        assertEquals("SHA256withRSA", OIDs.signatureAlgorithmForDigest("SHA-256"));
    }

    @Test
    public void testParseAllMultipleNodes() throws IOException {
        byte[] int1 = DEREncoder.encodeInteger(BigInteger.valueOf(1));
        byte[] int2 = DEREncoder.encodeInteger(BigInteger.valueOf(2));
        byte[] combined = new byte[int1.length + int2.length];
        System.arraycopy(int1, 0, combined, 0, int1.length);
        System.arraycopy(int2, 0, combined, int1.length, int2.length);

        var nodes = DERNode.parseAll(combined, 0, combined.length);
        assertEquals(2, nodes.size());
        assertEquals(BigInteger.valueOf(1), nodes.get(0).getInteger());
        assertEquals(BigInteger.valueOf(2), nodes.get(1).getInteger());
    }

    @Test
    public void testNodeToString() throws IOException {
        byte[] encoded = DEREncoder.encodeSequence(DEREncoder.encodeNull());
        DERNode node = DERNode.parse(encoded);
        String str = node.toString();
        assertTrue(str.contains("0x30"), "Should show SEQUENCE tag");
        assertTrue(str.contains("children=1"), "Should show 1 child");
    }
}
