package org.aspose.pdf.tests;

import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.engine.security.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for PDF security/decryption classes.
public class SecurityTest {

    // ── RC4Cipher ──

    @Test
    public void testRC4RoundTrip() {
        byte[] key = {0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] plaintext = "Hello, World!".getBytes();
        byte[] encrypted = RC4Cipher.process(key, plaintext);
        assertNotNull(encrypted);
        // RC4 is symmetric: encrypting again decrypts
        byte[] decrypted = RC4Cipher.process(key, encrypted);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testRC4EmptyData() {
        byte[] key = {0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] result = RC4Cipher.process(key, new byte[0]);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testRC4NullData() {
        byte[] key = {0x01, 0x02, 0x03, 0x04, 0x05};
        assertNull(RC4Cipher.process(key, null));
    }

    // ── AESCipher ──

    @Test
    public void testAES128RoundTrip() throws Exception {
        byte[] key = new byte[16]; // all zeros
        byte[] iv = new byte[16];
        byte[] plaintext = new byte[32]; // 2 blocks
        for (int i = 0; i < 32; i++) plaintext[i] = (byte) i;

        // Encrypt
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE,
                new javax.crypto.spec.SecretKeySpec(key, "AES"),
                new javax.crypto.spec.IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(plaintext);

        // Prepend IV
        byte[] encData = new byte[16 + cipherText.length];
        System.arraycopy(iv, 0, encData, 0, 16);
        System.arraycopy(cipherText, 0, encData, 16, cipherText.length);

        // Decrypt with our AESCipher
        byte[] decrypted = AESCipher.decrypt(key, encData);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testAESShortData() {
        byte[] key = new byte[16];
        byte[] result = AESCipher.decrypt(key, new byte[8]); // less than IV
        assertEquals(0, result.length);
    }

    @Test
    public void testAESEmptyData() {
        byte[] key = new byte[16];
        byte[] result = AESCipher.decrypt(key, null);
        assertEquals(0, result.length);
    }

    @Test
    public void testAESDecryptWithIV() {
        byte[] key = new byte[16];
        byte[] iv = new byte[16];
        // Encrypt a 32-byte block with NoPadding for testing
        try {
            byte[] plaintext = new byte[32];
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE,
                    new javax.crypto.spec.SecretKeySpec(key, "AES"),
                    new javax.crypto.spec.IvParameterSpec(iv));
            byte[] cipherText = cipher.doFinal(plaintext);

            byte[] decrypted = AESCipher.decryptWithIV(key, iv, cipherText);
            assertArrayEquals(plaintext, decrypted);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // ── PDFKeyDerivation ──

    @Test
    public void testPadPasswordEmpty() {
        byte[] padded = PDFKeyDerivation.padPassword(new byte[0]);
        assertEquals(32, padded.length);
        // Should equal the PADDING constant
        assertArrayEquals(PDFKeyDerivation.PADDING, padded);
    }

    @Test
    public void testPadPasswordNull() {
        byte[] padded = PDFKeyDerivation.padPassword(null);
        assertEquals(32, padded.length);
        assertArrayEquals(PDFKeyDerivation.PADDING, padded);
    }

    @Test
    public void testPadPasswordShort() {
        byte[] pw = "test".getBytes();
        byte[] padded = PDFKeyDerivation.padPassword(pw);
        assertEquals(32, padded.length);
        assertEquals('t', padded[0]);
        assertEquals('e', padded[1]);
        assertEquals('s', padded[2]);
        assertEquals('t', padded[3]);
        // Remaining 28 bytes from PADDING
        assertEquals(PDFKeyDerivation.PADDING[0], padded[4]);
    }

    @Test
    public void testPadPasswordLong() {
        byte[] pw = new byte[64];
        for (int i = 0; i < 64; i++) pw[i] = (byte) i;
        byte[] padded = PDFKeyDerivation.padPassword(pw);
        assertEquals(32, padded.length);
        // First 32 bytes of pw
        for (int i = 0; i < 32; i++) {
            assertEquals((byte) i, padded[i]);
        }
    }

    // ── PDFEncryptionDict ──

    @Test
    public void testEncryptionDictProperties() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Filter"), PdfName.of("Standard"));
        dict.set(PdfName.of("V"), PdfInteger.valueOf(2));
        dict.set(PdfName.of("R"), PdfInteger.valueOf(3));
        dict.set(PdfName.of("Length"), PdfInteger.valueOf(128));
        dict.set(PdfName.of("P"), PdfInteger.valueOf(-3904));

        byte[] oHash = new byte[32];
        byte[] uHash = new byte[32];
        dict.set(PdfName.of("O"), new PdfString(oHash));
        dict.set(PdfName.of("U"), new PdfString(uHash));

        PDFEncryptionDict encDict = new PDFEncryptionDict(dict);
        assertEquals("Standard", encDict.getFilter());
        assertEquals(2, encDict.getV());
        assertEquals(3, encDict.getR());
        assertEquals(128, encDict.getLength());
        assertEquals(16, encDict.getKeyLength());
        assertEquals(-3904, encDict.getP());
        assertTrue(encDict.getEncryptMetadata());
    }

    @Test
    public void testCipherTypeDetection() {
        // V=1 → RC4
        PdfDictionary d1 = new PdfDictionary();
        d1.set(PdfName.of("V"), PdfInteger.valueOf(1));
        assertEquals(PDFEncryptionDict.CipherType.RC4, new PDFEncryptionDict(d1).getCipherType());

        // V=2 → RC4
        PdfDictionary d2 = new PdfDictionary();
        d2.set(PdfName.of("V"), PdfInteger.valueOf(2));
        assertEquals(PDFEncryptionDict.CipherType.RC4, new PDFEncryptionDict(d2).getCipherType());

        // V=5 with /StmF /StdCF → AES_256 (the form every real AES-256 file uses).
        PdfDictionary d5 = new PdfDictionary();
        d5.set(PdfName.of("V"), PdfInteger.valueOf(5));
        d5.set(PdfName.of("StmF"), PdfName.of("StdCF"));
        assertEquals(PDFEncryptionDict.CipherType.AES_256, new PDFEncryptionDict(d5).getCipherType());

        // V=5 WITHOUT /StmF → IDENTITY: per ISO 32000-1 Table 20 the /StmF default
        // is /Identity, i.e. streams are not encrypted (matches Adobe / pdf.js).
        PdfDictionary d5bare = new PdfDictionary();
        d5bare.set(PdfName.of("V"), PdfInteger.valueOf(5));
        assertEquals(PDFEncryptionDict.CipherType.IDENTITY,
                new PDFEncryptionDict(d5bare).getCipherType());

        // V=4 with AESV2 → AES_128
        PdfDictionary d4 = new PdfDictionary();
        d4.set(PdfName.of("V"), PdfInteger.valueOf(4));
        d4.set(PdfName.of("StmF"), PdfName.of("StdCF"));
        PdfDictionary cf = new PdfDictionary();
        PdfDictionary stdCF = new PdfDictionary();
        stdCF.set(PdfName.of("CFM"), PdfName.of("AESV2"));
        cf.set(PdfName.of("StdCF"), stdCF);
        d4.set(PdfName.of("CF"), cf);
        assertEquals(PDFEncryptionDict.CipherType.AES_128, new PDFEncryptionDict(d4).getCipherType());
    }

    @Test
    public void testV1AlwaysLength40() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("V"), PdfInteger.valueOf(1));
        dict.set(PdfName.of("Length"), PdfInteger.valueOf(128)); // should be overridden
        PDFEncryptionDict encDict = new PDFEncryptionDict(dict);
        assertEquals(40, encDict.getLength());
        assertEquals(5, encDict.getKeyLength());
    }

    // ── PDFDecryptor ──

    @Test
    public void testDecryptorActive() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("V"), PdfInteger.valueOf(2));
        dict.set(PdfName.of("R"), PdfInteger.valueOf(3));
        PDFEncryptionDict encDict = new PDFEncryptionDict(dict);

        byte[] key = new byte[16];
        PDFDecryptor decryptor = new PDFDecryptor(key, encDict);
        assertTrue(decryptor.isActive());
    }

    @Test
    public void testDecryptorNullData() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("V"), PdfInteger.valueOf(2));
        dict.set(PdfName.of("R"), PdfInteger.valueOf(3));
        PDFEncryptionDict encDict = new PDFEncryptionDict(dict);

        PDFDecryptor decryptor = new PDFDecryptor(new byte[16], encDict);
        assertNull(decryptor.decrypt(null, 1, 0));
        assertEquals(0, decryptor.decrypt(new byte[0], 1, 0).length);
    }

    // ── StandardSecurityHandler ──

    @Test
    public void testHandlerWithEmptyPassword() {
        // Build a minimal R=2 V=1 encryption dict with empty password
        // This is a known test case: empty password with specific O/U values
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Filter"), PdfName.of("Standard"));
        dict.set(PdfName.of("V"), PdfInteger.valueOf(1));
        dict.set(PdfName.of("R"), PdfInteger.valueOf(2));
        dict.set(PdfName.of("P"), PdfInteger.valueOf(-3904));

        // For R=2 with empty password: O and U are computed from the PADDING constant
        // We can't easily create known-good test values without a full reference impl,
        // but we can verify the handler doesn't crash
        byte[] oHash = new byte[32];
        byte[] uHash = new byte[32];
        dict.set(PdfName.of("O"), new PdfString(oHash));
        dict.set(PdfName.of("U"), new PdfString(uHash));

        PDFEncryptionDict encDict = new PDFEncryptionDict(dict);
        StandardSecurityHandler handler = new StandardSecurityHandler(encDict, new byte[16]);

        // Authenticate returns boolean — shouldn't throw
        handler.authenticate(new byte[0]);
    }

    @Test
    public void testHandlerR6WithInvalidData() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("V"), PdfInteger.valueOf(5));
        dict.set(PdfName.of("R"), PdfInteger.valueOf(6));
        dict.set(PdfName.of("O"), new PdfString(new byte[48]));
        dict.set(PdfName.of("U"), new PdfString(new byte[48]));
        dict.set(PdfName.of("OE"), new PdfString(new byte[32]));
        dict.set(PdfName.of("UE"), new PdfString(new byte[32]));

        PDFEncryptionDict encDict = new PDFEncryptionDict(dict);
        StandardSecurityHandler handler = new StandardSecurityHandler(encDict, null);

        // With zero-filled hashes, authentication should fail gracefully
        boolean result = handler.authenticate(new byte[0]);
        // It may or may not authenticate depending on the zeros — either way, no crash
        assertFalse(handler.isAuthenticated() && handler.getEncryptionKey() == null);
    }

    // ── Document integration ──

    @Test
    public void testUnencryptedDocumentStillWorks() throws Exception {
        // Verify that the new security init doesn't break unencrypted docs
        // This uses the empty Document() constructor — no encryption
        org.aspose.pdf.Document doc = new org.aspose.pdf.Document();
        assertFalse(doc.isEncrypted());
        doc.close();
    }
}
