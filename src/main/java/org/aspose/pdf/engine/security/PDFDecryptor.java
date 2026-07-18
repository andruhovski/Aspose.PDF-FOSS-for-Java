package org.aspose.pdf.engine.security;

import org.aspose.pdf.security.ICustomSecurityHandler;

import java.util.logging.Logger;

/// Decrypts individual PDF objects (strings and streams).
///
/// Uses [PDFCryptoUtils#computeObjectKey] for Algorithm 1 (§7.6.2)
/// per-object key computation.
///
public class PDFDecryptor {

    private static final Logger LOG = Logger.getLogger(PDFDecryptor.class.getName());

    private final byte[] encryptionKey;
    private final PDFEncryptionDict.CipherType cipherType;
    private final int revision;
    private final ICustomSecurityHandler customHandler;

    /// Creates a decryptor with the given key and encryption parameters.
    ///
    /// @param encryptionKey the file encryption key
    /// @param encDict       the encryption dictionary
    public PDFDecryptor(byte[] encryptionKey, PDFEncryptionDict encDict) {
        this.encryptionKey = encryptionKey;
        this.cipherType = encDict.getCipherType();
        this.revision = encDict.getR();
        this.customHandler = null;
    }

    /// Creates a decryptor backed by a custom security handler.
    ///
    /// @param encryptionKey file encryption key
    /// @param encDict       encryption dictionary
    /// @param customHandler custom handler implementation
    public PDFDecryptor(byte[] encryptionKey, PDFEncryptionDict encDict, ICustomSecurityHandler customHandler) {
        this.encryptionKey = encryptionKey;
        this.cipherType = encDict.getCipherType();
        this.revision = encDict.getR();
        this.customHandler = customHandler;
    }

    /// Decrypts data belonging to a specific PDF object.
    ///
    /// @param data             the encrypted bytes
    /// @param objectNumber     the object number
    /// @param generationNumber the generation number
    /// @return the decrypted bytes
    public byte[] decrypt(byte[] data, int objectNumber, int generationNumber) {
        if (data == null || data.length == 0) return data;
        try {
            if (customHandler != null) {
                return customHandler.decrypt(data, objectNumber, generationNumber, encryptionKey);
            }
            // /StmF (or /StrF) = /Identity: this object's data is not encrypted.
            // Must be checked before the R>=5 branch, since an R5/R6 file may still
            // declare /Identity crypt filters.
            if (cipherType == PDFEncryptionDict.CipherType.IDENTITY) {
                return data;
            }
            if (revision >= 5) {
                // R=5/R=6: AES-256 with file key directly
                return AESCipher.decrypt(encryptionKey, data);
            }
            byte[] objectKey = PDFCryptoUtils.computeObjectKey(
                    encryptionKey, objectNumber, generationNumber, cipherType);
            switch (cipherType) {
                case RC4:
                    return RC4Cipher.process(objectKey, data);
                case AES_128:
                    return AESCipher.decrypt(objectKey, data);
                default:
                    return data;
            }
        } catch (Exception e) {
            LOG.fine(() -> "Decryption failed for obj " + objectNumber + ": " + e.getMessage());
            return data; // Return original data on failure
        }
    }

    /// Returns true if the decryptor has a valid key.
    public boolean isActive() { return encryptionKey != null; }

    /// Returns a copy of the file encryption key currently used for decryption.
    ///
    /// @return the file encryption key, or `null` if decryption is inactive
    public byte[] getEncryptionKey() {
        return encryptionKey != null ? encryptionKey.clone() : null;
    }

    /// Returns the custom handler used by this decryptor, if any.
    ///
    /// @return custom handler or `null`
    public ICustomSecurityHandler getCustomHandler() {
        return customHandler;
    }
}
