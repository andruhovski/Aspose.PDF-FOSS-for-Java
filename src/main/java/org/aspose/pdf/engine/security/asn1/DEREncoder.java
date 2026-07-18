package org.aspose.pdf.engine.security.asn1;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/// Encodes ASN.1 DER (Distinguished Encoding Rules) structures.
public final class DEREncoder {

    private DEREncoder() {}

    /// Encodes a SEQUENCE (tag 0x30) containing the given contents.
    public static byte[] encodeSequence(byte[]... contents) {
        return encodeTLV(0x30, concat(contents));
    }

    /// Encodes a SET (tag 0x31) containing the given contents.
    public static byte[] encodeSet(byte[]... contents) {
        return encodeTLV(0x31, concat(contents));
    }

    /// Encodes an OID from dotted string.
    public static byte[] encodeOID(String oid) {
        String[] parts = oid.split("\\.");
        if (parts.length < 2) return encodeTLV(0x06, new byte[0]);
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(first * 40 + second);
        for (int i = 2; i < parts.length; i++) {
            long val = Long.parseLong(parts[i]);
            encodeOIDComponent(baos, val);
        }
        return encodeTLV(0x06, baos.toByteArray());
    }

    /// Encodes an INTEGER.
    public static byte[] encodeInteger(BigInteger value) {
        return encodeTLV(0x02, value.toByteArray());
    }

    /// Encodes a small integer.
    public static byte[] encodeInteger(int value) {
        return encodeInteger(BigInteger.valueOf(value));
    }

    /// Encodes an OCTET STRING.
    public static byte[] encodeOctetString(byte[] data) {
        return encodeTLV(0x04, data);
    }

    /// Encodes a BIT STRING (with unused-bits prefix byte 0x00).
    public static byte[] encodeBitString(byte[] data) {
        byte[] val = new byte[data.length + 1];
        val[0] = 0; // unused bits
        System.arraycopy(data, 0, val, 1, data.length);
        return encodeTLV(0x03, val);
    }

    /// Encodes NULL.
    public static byte[] encodeNull() {
        return new byte[]{0x05, 0x00};
    }

    /// Encodes a context-specific constructed tag `[n]` EXPLICIT.
    public static byte[] encodeContextTag(int tagNumber, byte[] content) {
        return encodeTLV(0xA0 | tagNumber, content);
    }

    /// Encodes a context-specific implicit tag `[n]` IMPLICIT (preserves inner content).
    public static byte[] encodeContextTagImplicit(int tagNumber, byte[] content) {
        return encodeTLV(0xA0 | tagNumber, content);
    }

    /// Encodes a UTCTime value.
    public static byte[] encodeUTCTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return encodeTLV(0x17, sdf.format(date).getBytes(StandardCharsets.US_ASCII));
    }

    /// Encodes a PrintableString.
    public static byte[] encodePrintableString(String s) {
        return encodeTLV(0x13, s.getBytes(StandardCharsets.US_ASCII));
    }

    /// Encodes a UTF8String.
    public static byte[] encodeUTF8String(String s) {
        return encodeTLV(0x0C, s.getBytes(StandardCharsets.UTF_8));
    }

    /// Encodes an AlgorithmIdentifier SEQUENCE { OID, NULL }.
    public static byte[] encodeAlgorithmIdentifier(String oid) {
        return encodeSequence(encodeOID(oid), encodeNull());
    }

    // ── Core TLV encoding ──

    /// Encodes a Tag-Length-Value triple.
    public static byte[] encodeTLV(int tag, byte[] value) {
        byte[] lenBytes = encodeLength(value.length);
        byte[] result = new byte[1 + lenBytes.length + value.length];
        result[0] = (byte) tag;
        System.arraycopy(lenBytes, 0, result, 1, lenBytes.length);
        System.arraycopy(value, 0, result, 1 + lenBytes.length, value.length);
        return result;
    }

    /// Encodes a DER length value.
    public static byte[] encodeLength(int length) {
        if (length < 0x80) return new byte[]{(byte) length};
        if (length < 0x100) return new byte[]{(byte) 0x81, (byte) length};
        if (length < 0x10000) return new byte[]{(byte) 0x82, (byte) (length >> 8), (byte) length};
        return new byte[]{(byte) 0x83, (byte) (length >> 16), (byte) (length >> 8), (byte) length};
    }

    /// Returns the number of bytes needed to encode the given length.
    static int encodedLengthSize(int length) {
        if (length < 0x80) return 1;
        if (length < 0x100) return 2;
        if (length < 0x10000) return 3;
        return 4;
    }

    private static void encodeOIDComponent(ByteArrayOutputStream baos, long val) {
        if (val < 128) {
            baos.write((int) val);
            return;
        }
        // Multi-byte: high bit set on all but last byte
        int numBytes = 0;
        long temp = val;
        while (temp > 0) { numBytes++; temp >>= 7; }
        byte[] bytes = new byte[numBytes];
        for (int i = numBytes - 1; i >= 0; i--) {
            bytes[i] = (byte) (val & 0x7F);
            if (i < numBytes - 1) bytes[i] |= 0x80;
            val >>= 7;
        }
        baos.write(bytes, 0, bytes.length);
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) total += a.length;
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }
}
