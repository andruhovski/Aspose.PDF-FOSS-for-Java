package org.aspose.pdf.engine.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * RC4 (ARCFOUR) stream cipher for PDF decryption.
 * <p>
 * RC4 is symmetric — same operation for encrypt and decrypt.
 * Used for V=1,2,3 and V=4 with CFM=V2.
 * </p>
 */
public final class RC4Cipher {

    private RC4Cipher() {}

    /**
     * Processes (encrypts or decrypts) data using RC4.
     *
     * @param key  the RC4 key (5-16 bytes)
     * @param data the data to process
     * @return the processed data
     */
    public static byte[] process(byte[] key, byte[] data) {
        if (data == null || data.length == 0) return data;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "ARCFOUR");
            Cipher cipher = Cipher.getInstance("ARCFOUR");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("RC4 processing failed: " + e.getMessage(), e);
        }
    }
}
