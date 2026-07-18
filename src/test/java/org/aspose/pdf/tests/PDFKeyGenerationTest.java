package org.aspose.pdf.tests;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.security.PDFEncryptionDict;
import org.aspose.pdf.engine.security.PDFKeyDerivation;
import org.aspose.pdf.engine.security.StandardSecurityHandler;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for write-side O/U hash generation (Algorithms 3, 4, 5).
/// The gold test: generate O and U, then verify with the existing read-side
/// StandardSecurityHandler.authenticate(). If it passes, the hashes are correct.
public class PDFKeyGenerationTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    // ── GOLD TEST: R=3, RC4-128 ──

    @Test
    public void goldTest_R3_RC4_128() {
        byte[] userPw = "user".getBytes();
        byte[] ownerPw = "owner".getBytes();
        int R = 3;
        int V = 2;
        int keyLenBytes = 16;
        int P = -3904;
        byte[] documentId = randomBytes(16);

        verifyGenerateAndAuthenticate(userPw, ownerPw, V, R, keyLenBytes, P, documentId);
    }

    // ── GOLD TEST: R=2, RC4-40 ──

    @Test
    public void goldTest_R2_RC4_40() {
        byte[] userPw = "hello".getBytes();
        byte[] ownerPw = "world".getBytes();
        int R = 2;
        int V = 1;
        int keyLenBytes = 5;
        int P = -3904;
        byte[] documentId = randomBytes(16);

        verifyGenerateAndAuthenticate(userPw, ownerPw, V, R, keyLenBytes, P, documentId);
    }

    // ── GOLD TEST: R=4, AES-128 ──

    @Test
    public void goldTest_R4_AES_128() {
        byte[] userPw = "secret".getBytes();
        byte[] ownerPw = "master".getBytes();
        int R = 4;
        int V = 4;
        int keyLenBytes = 16;
        int P = -1028;
        byte[] documentId = randomBytes(16);

        verifyGenerateAndAuthenticate(userPw, ownerPw, V, R, keyLenBytes, P, documentId);
    }

    // ── GOLD TEST: empty user password ──

    @Test
    public void goldTest_emptyUserPassword() {
        byte[] userPw = new byte[0];
        byte[] ownerPw = "admin".getBytes();
        int R = 3;
        int V = 2;
        int keyLenBytes = 16;
        int P = -3904;
        byte[] documentId = randomBytes(16);

        verifyGenerateAndAuthenticate(userPw, ownerPw, V, R, keyLenBytes, P, documentId);
    }

    // ── GOLD TEST: empty owner password (falls back to user per spec) ──

    @Test
    public void goldTest_emptyOwnerPassword() {
        byte[] userPw = "onlyuser".getBytes();
        byte[] ownerPw = new byte[0]; // generateO uses userPw as fallback per Algorithm 3
        int R = 3;
        int V = 2;
        int keyLenBytes = 16;
        int P = -3904;
        byte[] documentId = randomBytes(16);

        // Generate O with empty owner → uses userPw as effective owner password
        byte[] O = PDFKeyDerivation.generateO_R2R4(ownerPw, userPw, keyLenBytes, R);
        assertEquals(32, O.length);

        PDFEncryptionDict encDict = buildEncDict(V, R, keyLenBytes * 8, P, O, new byte[32]);
        byte[] encKey = PDFKeyDerivation.computeEncryptionKeyR2R4(userPw, encDict, documentId);
        byte[] U = PDFKeyDerivation.generateU_R3R4(encKey, documentId);

        encDict = buildEncDict(V, R, keyLenBytes * 8, P, O, U);

        // User password should work (via user path)
        StandardSecurityHandler handler = new StandardSecurityHandler(encDict, documentId);
        assertTrue(handler.authenticate(userPw), "User password should authenticate");

        // The effective owner password is userPw (Algorithm 3 fallback).
        // So authenticating with userPw also succeeds via the owner path.
        // Authenticating with empty password should also work because
        // authenticate() tries user first — and empty user pw won't match,
        // but the owner path decrypts O with empty pw → won't recover userPw.
        // So this correctly fails for empty password when O was made from userPw.
        StandardSecurityHandler handler2 = new StandardSecurityHandler(encDict, documentId);
        assertFalse(handler2.authenticate(ownerPw),
                "Empty password should NOT authenticate (O was generated with userPw as effective owner)");
    }

    // ── Deterministic: same inputs → same output ──

    @Test
    public void generateO_isDeterministic() {
        byte[] ownerPw = "owner".getBytes();
        byte[] userPw = "user".getBytes();

        byte[] O1 = PDFKeyDerivation.generateO_R2R4(ownerPw, userPw, 16, 3);
        byte[] O2 = PDFKeyDerivation.generateO_R2R4(ownerPw, userPw, 16, 3);
        assertArrayEquals(O1, O2, "generateO should be deterministic");
    }

    @Test
    public void generateU_R2_isDeterministic() {
        byte[] key = new byte[5];
        Arrays.fill(key, (byte) 0x42);

        byte[] U1 = PDFKeyDerivation.generateU_R2(key);
        byte[] U2 = PDFKeyDerivation.generateU_R2(key);
        assertArrayEquals(U1, U2, "generateU_R2 should be deterministic");
    }

    @Test
    public void generateU_R3R4_isDeterministic() {
        byte[] key = new byte[16];
        Arrays.fill(key, (byte) 0xAB);
        byte[] docId = randomBytes(16);

        byte[] U1 = PDFKeyDerivation.generateU_R3R4(key, docId);
        byte[] U2 = PDFKeyDerivation.generateU_R3R4(key, docId);
        assertArrayEquals(U1, U2, "generateU_R3R4 should be deterministic");
    }

    // ── R=2 vs R=3 produce different U values ──

    @Test
    public void generateU_R2_vs_R3_differentAlgorithms() {
        // Use same key (padded to 16 for R3, but only first 5 used for R2)
        byte[] key5 = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] key16 = new byte[16];
        System.arraycopy(key5, 0, key16, 0, 5);
        byte[] docId = randomBytes(16);

        byte[] U_R2 = PDFKeyDerivation.generateU_R2(key5);
        byte[] U_R3 = PDFKeyDerivation.generateU_R3R4(key16, docId);

        assertEquals(32, U_R2.length);
        assertEquals(32, U_R3.length);
        // The two algorithms should produce different results
        assertFalse(Arrays.equals(U_R2, U_R3),
                "R=2 and R=3 should produce different U values");
    }

    // ── Output sizes ──

    @Test
    public void generateO_returns32bytes() {
        byte[] O = PDFKeyDerivation.generateO_R2R4("ow".getBytes(), "us".getBytes(), 5, 2);
        assertEquals(32, O.length);
    }

    @Test
    public void generateU_R2_returns32bytes() {
        byte[] U = PDFKeyDerivation.generateU_R2(new byte[5]);
        assertEquals(32, U.length);
    }

    @Test
    public void generateU_R3R4_returns32bytes() {
        byte[] U = PDFKeyDerivation.generateU_R3R4(new byte[16], new byte[16]);
        assertEquals(32, U.length);
    }

    // ── Multiple random runs to catch non-determinism bugs ─���

    @Test
    public void goldTest_multipleRandomRuns() {
        for (int run = 0; run < 5; run++) {
            byte[] userPw = ("user_" + run).getBytes();
            byte[] ownerPw = ("owner_" + run).getBytes();
            byte[] documentId = randomBytes(16);
            int P = -(run * 100 + 3904);

            verifyGenerateAndAuthenticate(userPw, ownerPw, 2, 3, 16, P, documentId);
        }
    }

    // ── Helpers ──

    /// The core gold test pattern: generate O and U, build an encryption dict,
    /// and verify that StandardSecurityHandler.authenticate() accepts both
    /// user and owner passwords, and rejects a wrong password.
    private void verifyGenerateAndAuthenticate(byte[] userPw, byte[] ownerPw,
                                                int V, int R, int keyLenBytes,
                                                int P, byte[] documentId) {
        // 1. Generate O
        byte[] O = PDFKeyDerivation.generateO_R2R4(ownerPw, userPw, keyLenBytes, R);
        assertEquals(32, O.length, "O should be 32 bytes");

        // 2. Build temporary encDict with O (U placeholder) to compute encryption key
        PDFEncryptionDict tempDict = buildEncDict(V, R, keyLenBytes * 8, P, O, new byte[32]);

        // 3. Derive encryption key from user password
        byte[] encKey = PDFKeyDerivation.computeEncryptionKeyR2R4(userPw, tempDict, documentId);
        assertNotNull(encKey);
        assertEquals(keyLenBytes, encKey.length, "Encryption key length");

        // 4. Generate U
        byte[] U;
        if (R == 2) {
            U = PDFKeyDerivation.generateU_R2(encKey);
        } else {
            U = PDFKeyDerivation.generateU_R3R4(encKey, documentId);
        }
        assertEquals(32, U.length, "U should be 32 bytes");

        // 5. Build final encDict with both O and U
        PDFEncryptionDict encDict = buildEncDict(V, R, keyLenBytes * 8, P, O, U);

        // 6. Authenticate with user password → TRUE
        StandardSecurityHandler userHandler = new StandardSecurityHandler(encDict, documentId);
        assertTrue(userHandler.authenticate(userPw),
                "User password should authenticate (R=" + R + ")");
        assertNotNull(userHandler.getEncryptionKey());
        assertArrayEquals(encKey, userHandler.getEncryptionKey(),
                "Authenticated key should match generated key");

        // 7. Authenticate with owner password → TRUE
        StandardSecurityHandler ownerHandler = new StandardSecurityHandler(encDict, documentId);
        assertTrue(ownerHandler.authenticate(ownerPw),
                "Owner password should authenticate (R=" + R + ")");

        // 8. Authenticate with wrong password → FALSE
        StandardSecurityHandler wrongHandler = new StandardSecurityHandler(encDict, documentId);
        assertFalse(wrongHandler.authenticate("WRONG_PASSWORD_12345".getBytes()),
                "Wrong password should NOT authenticate");
    }

    /// Builds a PDFEncryptionDict from raw parameters using a manual PdfDictionary.
    private PDFEncryptionDict buildEncDict(int V, int R, int lengthBits,
                                            int P, byte[] O, byte[] U) {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Filter"), PdfName.of("Standard"));
        dict.set(PdfName.of("V"), PdfInteger.valueOf(V));
        dict.set(PdfName.of("R"), PdfInteger.valueOf(R));
        dict.set(PdfName.of("Length"), PdfInteger.valueOf(lengthBits));
        dict.set(PdfName.of("P"), PdfInteger.valueOf(P));
        dict.set(PdfName.of("O"), new PdfString(O));
        dict.set(PdfName.of("U"), new PdfString(U));

        // For V=4 (R=4), add CF/StmF/StrF for AES-128
        if (V == 4) {
            dict.set(PdfName.of("StmF"), PdfName.of("StdCF"));
            dict.set(PdfName.of("StrF"), PdfName.of("StdCF"));
            PdfDictionary stdCF = new PdfDictionary();
            stdCF.set(PdfName.of("CFM"), PdfName.of("AESV2"));
            stdCF.set(PdfName.of("AuthEvent"), PdfName.of("DocOpen"));
            stdCF.set(PdfName.of("Length"), PdfInteger.valueOf(16));
            PdfDictionary cf = new PdfDictionary();
            cf.set(PdfName.of("StdCF"), stdCF);
            dict.set(PdfName.of("CF"), cf);
        }

        return new PDFEncryptionDict(dict);
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
