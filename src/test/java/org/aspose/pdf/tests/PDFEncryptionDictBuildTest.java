package org.aspose.pdf.tests;

import org.aspose.pdf.CryptoAlgorithm;
import org.aspose.pdf.engine.security.PDFEncryptionDict;
import org.aspose.pdf.engine.security.PDFKeyDerivation;
import org.aspose.pdf.engine.security.StandardSecurityHandler;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PDFEncryptionDict#build] static factory.
/// Validates all four CryptoAlgorithm variants and integration
/// with StandardSecurityHandler.authenticate().
public class PDFEncryptionDictBuildTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    // ── RC4x40 (V=1, R=2, Length=40) ──

    @Test
    public void build_RC4x40_correctParameters() {
        byte[] O = randomBytes(32);
        byte[] U = randomBytes(32);
        int P = -3904;

        PDFEncryptionDict dict = PDFEncryptionDict.build(
                CryptoAlgorithm.RC4x40, P, O, U, null, null, null);

        assertEquals(1, dict.getV());
        assertEquals(2, dict.getR());
        assertEquals(40, dict.getLength());
        assertEquals(5, dict.getKeyLength());
        assertEquals(P, dict.getP());
        assertArrayEquals(O, dict.getO());
        assertArrayEquals(U, dict.getU());
        assertEquals("Standard", dict.getFilter());
        assertEquals(PDFEncryptionDict.CipherType.RC4, dict.getCipherType());
        assertNotNull(dict.getPdfDictionary());
    }

    // ── RC4x128 (V=2, R=3, Length=128) ──

    @Test
    public void build_RC4x128_correctParameters() {
        byte[] O = randomBytes(32);
        byte[] U = randomBytes(32);
        int P = -1028;

        PDFEncryptionDict dict = PDFEncryptionDict.build(
                CryptoAlgorithm.RC4x128, P, O, U, null, null, null);

        assertEquals(2, dict.getV());
        assertEquals(3, dict.getR());
        assertEquals(128, dict.getLength());
        assertEquals(16, dict.getKeyLength());
        assertEquals(P, dict.getP());
        assertArrayEquals(O, dict.getO());
        assertArrayEquals(U, dict.getU());
        assertEquals("Standard", dict.getFilter());
        assertEquals(PDFEncryptionDict.CipherType.RC4, dict.getCipherType());
        assertNotNull(dict.getPdfDictionary());
    }

    // ── AESx128 (V=4, R=4, Length=128) ──

    @Test
    public void build_AESx128_correctParameters() {
        byte[] O = randomBytes(32);
        byte[] U = randomBytes(32);
        int P = -3904;

        PDFEncryptionDict dict = PDFEncryptionDict.build(
                CryptoAlgorithm.AESx128, P, O, U, null, null, null);

        assertEquals(4, dict.getV());
        assertEquals(4, dict.getR());
        assertEquals(128, dict.getLength());
        assertEquals(16, dict.getKeyLength());
        assertEquals(P, dict.getP());
        assertArrayEquals(O, dict.getO());
        assertArrayEquals(U, dict.getU());
        assertEquals("Standard", dict.getFilter());
        assertEquals(PDFEncryptionDict.CipherType.AES_128, dict.getCipherType());
        assertEquals("StdCF", dict.getStmF());
        assertEquals("StdCF", dict.getStrF());
        assertNotNull(dict.getCF());
        assertNotNull(dict.getPdfDictionary());
    }

    // ── AESx256 (V=5, R=6, Length=256) ──

    @Test
    public void build_AESx256_correctParameters() {
        byte[] O = randomBytes(48);
        byte[] U = randomBytes(48);
        byte[] OE = randomBytes(32);
        byte[] UE = randomBytes(32);
        byte[] Perms = randomBytes(16);
        int P = -1028;

        PDFEncryptionDict dict = PDFEncryptionDict.build(
                CryptoAlgorithm.AESx256, P, O, U, OE, UE, Perms);

        assertEquals(5, dict.getV());
        assertEquals(6, dict.getR());
        assertEquals(256, dict.getLength());
        assertEquals(32, dict.getKeyLength());
        assertEquals(P, dict.getP());
        assertArrayEquals(O, dict.getO());
        assertArrayEquals(U, dict.getU());
        assertArrayEquals(OE, dict.getOE());
        assertArrayEquals(UE, dict.getUE());
        assertArrayEquals(Perms, dict.getPerms());
        assertEquals("Standard", dict.getFilter());
        assertEquals(PDFEncryptionDict.CipherType.AES_256, dict.getCipherType());
        assertEquals("StdCF", dict.getStmF());
        assertEquals("StdCF", dict.getStrF());
        assertNotNull(dict.getCF());
        assertNotNull(dict.getPdfDictionary());
    }

    // ── AESx256 with null OE/UE/Perms (graceful) ──

    @Test
    public void build_AESx256_nullR6Fields() {
        byte[] O = randomBytes(48);
        byte[] U = randomBytes(48);

        PDFEncryptionDict dict = PDFEncryptionDict.build(
                CryptoAlgorithm.AESx256, -3904, O, U, null, null, null);

        assertEquals(6, dict.getR());
        assertNull(dict.getOE());
        assertNull(dict.getUE());
        assertNull(dict.getPerms());
    }

    // ── EncryptMetadata defaults to true ──

    @Test
    public void build_encryptMetadata_defaultTrue() {
        PDFEncryptionDict dict = PDFEncryptionDict.build(
                CryptoAlgorithm.RC4x128, -3904,
                randomBytes(32), randomBytes(32), null, null, null);

        assertTrue(dict.getEncryptMetadata());
    }

    // ── GOLD INTEGRATION TEST: build + authenticate (RC4x128) ──

    @Test
    public void goldTest_RC4x128_buildThenAuthenticate() {
        byte[] userPw = "user123".getBytes();
        byte[] ownerPw = "owner456".getBytes();
        byte[] documentId = randomBytes(16);
        int P = -3904;
        int keyLenBytes = 16;

        // Step 1: generate O
        byte[] O = PDFKeyDerivation.generateO_R2R4(ownerPw, userPw, keyLenBytes, 3);

        // Step 2: compute encryption key (need temp dict with O to derive key)
        PDFEncryptionDict tempDict = PDFEncryptionDict.build(
                CryptoAlgorithm.RC4x128, P, O, new byte[32], null, null, null);
        byte[] encKey = PDFKeyDerivation.computeEncryptionKeyR2R4(userPw, tempDict, documentId);

        // Step 3: generate U
        byte[] U = PDFKeyDerivation.generateU_R3R4(encKey, documentId);

        // Step 4: build final dict via build()
        PDFEncryptionDict finalDict = PDFEncryptionDict.build(
                CryptoAlgorithm.RC4x128, P, O, U, null, null, null);

        // Step 5: authenticate — both passwords should succeed
        StandardSecurityHandler userHandler = new StandardSecurityHandler(finalDict, documentId);
        assertTrue(userHandler.authenticate(userPw), "User password should authenticate");
        assertNotNull(userHandler.getEncryptionKey());
        assertArrayEquals(encKey, userHandler.getEncryptionKey());

        StandardSecurityHandler ownerHandler = new StandardSecurityHandler(finalDict, documentId);
        assertTrue(ownerHandler.authenticate(ownerPw), "Owner password should authenticate");

        // Wrong password should fail
        StandardSecurityHandler wrongHandler = new StandardSecurityHandler(finalDict, documentId);
        assertFalse(wrongHandler.authenticate("WRONG".getBytes()), "Wrong password should fail");
    }

    // ── GOLD INTEGRATION TEST: build + authenticate (RC4x40) ──

    @Test
    public void goldTest_RC4x40_buildThenAuthenticate() {
        byte[] userPw = "u".getBytes();
        byte[] ownerPw = "o".getBytes();
        byte[] documentId = randomBytes(16);
        int P = -3904;
        int keyLenBytes = 5;

        byte[] O = PDFKeyDerivation.generateO_R2R4(ownerPw, userPw, keyLenBytes, 2);
        PDFEncryptionDict tempDict = PDFEncryptionDict.build(
                CryptoAlgorithm.RC4x40, P, O, new byte[32], null, null, null);
        byte[] encKey = PDFKeyDerivation.computeEncryptionKeyR2R4(userPw, tempDict, documentId);
        byte[] U = PDFKeyDerivation.generateU_R2(encKey);

        PDFEncryptionDict finalDict = PDFEncryptionDict.build(
                CryptoAlgorithm.RC4x40, P, O, U, null, null, null);

        StandardSecurityHandler handler = new StandardSecurityHandler(finalDict, documentId);
        assertTrue(handler.authenticate(userPw), "User password should authenticate (R=2)");

        StandardSecurityHandler ownerHandler = new StandardSecurityHandler(finalDict, documentId);
        assertTrue(ownerHandler.authenticate(ownerPw), "Owner password should authenticate (R=2)");
    }

    // ── GOLD INTEGRATION TEST: build + authenticate (AESx128) ──

    @Test
    public void goldTest_AESx128_buildThenAuthenticate() {
        byte[] userPw = "aesuser".getBytes();
        byte[] ownerPw = "aesowner".getBytes();
        byte[] documentId = randomBytes(16);
        int P = -1028;
        int keyLenBytes = 16;

        byte[] O = PDFKeyDerivation.generateO_R2R4(ownerPw, userPw, keyLenBytes, 4);
        PDFEncryptionDict tempDict = PDFEncryptionDict.build(
                CryptoAlgorithm.AESx128, P, O, new byte[32], null, null, null);
        byte[] encKey = PDFKeyDerivation.computeEncryptionKeyR2R4(userPw, tempDict, documentId);
        byte[] U = PDFKeyDerivation.generateU_R3R4(encKey, documentId);

        PDFEncryptionDict finalDict = PDFEncryptionDict.build(
                CryptoAlgorithm.AESx128, P, O, U, null, null, null);

        StandardSecurityHandler userHandler = new StandardSecurityHandler(finalDict, documentId);
        assertTrue(userHandler.authenticate(userPw), "User password should authenticate (AES-128)");

        StandardSecurityHandler ownerHandler = new StandardSecurityHandler(finalDict, documentId);
        assertTrue(ownerHandler.authenticate(ownerPw), "Owner password should authenticate (AES-128)");

        StandardSecurityHandler wrongHandler = new StandardSecurityHandler(finalDict, documentId);
        assertFalse(wrongHandler.authenticate("WRONG".getBytes()));
    }

    // ── Helpers ──

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
