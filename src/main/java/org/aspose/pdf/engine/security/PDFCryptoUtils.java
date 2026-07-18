package org.aspose.pdf.engine.security;

import java.security.MessageDigest;
import java.util.Arrays;

/// Shared cryptographic utilities for PDF encryption and decryption.
///
/// Contains Algorithm 1 (§7.6.2): per-object key computation, used by
/// both [PDFDecryptor] and `PDFEncryptor`.
///
public final class PDFCryptoUtils {

    private PDFCryptoUtils() {}

    /// Algorithm 1 (ISO 32000-1:2008, §7.6.2): computes the per-object encryption key.
    ///
    /// The file encryption key is extended with the object number and generation number
    /// (low-order bytes first), hashed with MD5, and truncated to the key length + 5
    /// (maximum 16 bytes). For AES-128, the salt bytes `sAlT` are appended before hashing.
    ///
    /// @param encryptionKey the file encryption key
    /// @param objectNumber  the PDF object number
    /// @param generationNumber the PDF generation number
    /// @param cipherType    the cipher type (affects salt bytes for AES-128)
    /// @return the per-object key (5..16 bytes)
    public static byte[] computeObjectKey(byte[] encryptionKey, int objectNumber,
                                           int generationNumber,
                                           PDFEncryptionDict.CipherType cipherType) {
        try {
            int extraLen = (cipherType == PDFEncryptionDict.CipherType.AES_128) ? 4 : 0;
            byte[] extended = new byte[encryptionKey.length + 5 + extraLen];
            System.arraycopy(encryptionKey, 0, extended, 0, encryptionKey.length);
            int offset = encryptionKey.length;
            extended[offset] = (byte) (objectNumber & 0xFF);
            extended[offset + 1] = (byte) ((objectNumber >> 8) & 0xFF);
            extended[offset + 2] = (byte) ((objectNumber >> 16) & 0xFF);
            extended[offset + 3] = (byte) (generationNumber & 0xFF);
            extended[offset + 4] = (byte) ((generationNumber >> 8) & 0xFF);

            if (cipherType == PDFEncryptionDict.CipherType.AES_128) {
                extended[offset + 5] = 0x73; // s
                extended[offset + 6] = 0x41; // A
                extended[offset + 7] = 0x6C; // l
                extended[offset + 8] = 0x54; // T
            }

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hash = md5.digest(extended);
            int keyLen = Math.min(encryptionKey.length + 5, 16);
            return Arrays.copyOf(hash, keyLen);
        } catch (Exception e) {
            throw new RuntimeException("Object key computation failed: " + e.getMessage(), e);
        }
    }
}
