package org.aspose.pdf.tests;

import org.aspose.pdf.CryptoAlgorithm;
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.facades.DocumentPrivilege;
import org.aspose.pdf.text.TextAbsorber;
import org.aspose.pdf.text.TextFragment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for Document.encrypt() end-to-end (Step 7 of BUG-011).
public class DocumentEncryptTest {

    @TempDir
    Path tempDir;

    // ── Test 1: Document.encrypt() unit (empty page) ──

    @Test
    public void encrypt_RC4x128_emptyPage() throws Exception {
        String output = tempDir.resolve("enc_empty.pdf").toString();

        Document doc = new Document();
        doc.getPages().add();
        doc.encrypt("user", "owner", DocumentPrivilege.getAllowAll().getValue(),
                CryptoAlgorithm.RC4x128);
        doc.save(output);
        doc.close();

        Document doc2 = new Document(output, "user");
        assertEquals(1, doc2.getPages().size());
        doc2.close();
    }

    // ── Test 2: E2E with text, RC4-128 ──

    @Test
    public void encrypt_RC4x128_withText() throws Exception {
        String output = tempDir.resolve("enc_rc4_text.pdf").toString();

        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment("Hello Encrypted World"));
        doc.encrypt("user", "owner", -3904, CryptoAlgorithm.RC4x128);
        doc.save(output);
        doc.close();

        Document doc2 = new Document(output, "user");
        TextAbsorber absorber = new TextAbsorber();
        doc2.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("Hello Encrypted World"),
                "Text should survive RC4-128 encrypt/decrypt. Got: " + absorber.getText());
        doc2.close();
    }

    // ── Test 3: E2E with text, AES-128 ──

    @Test
    public void encrypt_AESx128_withText() throws Exception {
        String output = tempDir.resolve("enc_aes_text.pdf").toString();

        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment("AES-128 Protected Content"));
        doc.encrypt("secret", "master", -3904, CryptoAlgorithm.AESx128);
        doc.save(output);
        doc.close();

        Document doc2 = new Document(output, "secret");
        TextAbsorber absorber = new TextAbsorber();
        doc2.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("AES-128 Protected Content"),
                "Text should survive AES-128 encrypt/decrypt. Got: " + absorber.getText());
        doc2.close();
    }

    // ── Test 4: Wrong password → IOException ──

    @Test
    public void encrypt_wrongPassword_throws() throws Exception {
        String output = tempDir.resolve("enc_wrong.pdf").toString();

        Document doc = new Document();
        doc.getPages().add();
        doc.encrypt("user", "owner", -3904, CryptoAlgorithm.RC4x128);
        doc.save(output);
        doc.close();

        assertThrows(IOException.class, () -> new Document(output, "wrong"));
    }

    // ── Test 5: Owner password works ──

    @Test
    public void encrypt_ownerPassword_works() throws Exception {
        String output = tempDir.resolve("enc_owner.pdf").toString();

        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment("Owner Access Test"));
        doc.encrypt("user", "owner", -3904, CryptoAlgorithm.RC4x128);
        doc.save(output);
        doc.close();

        Document doc2 = new Document(output, "owner");
        TextAbsorber absorber = new TextAbsorber();
        doc2.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("Owner Access Test"),
                "Owner password should work. Got: " + absorber.getText());
        doc2.close();
    }

    // ── Test: RC4x40 ──

    @Test
    public void encrypt_RC4x40_roundTrip() throws Exception {
        String output = tempDir.resolve("enc_rc4x40.pdf").toString();

        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment("RC4-40 Text"));
        doc.encrypt("u", "o", -3904, CryptoAlgorithm.RC4x40);
        doc.save(output);
        doc.close();

        Document doc2 = new Document(output, "u");
        TextAbsorber absorber = new TextAbsorber();
        doc2.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("RC4-40 Text"));
        doc2.close();
    }

    // ── Test: Encrypt existing PDF (from file, not new) ──

    @Test
    public void encrypt_existingPdf() throws Exception {
        String plainPath = tempDir.resolve("plain.pdf").toString();
        String encPath = tempDir.resolve("enc_existing.pdf").toString();

        // Create unencrypted PDF
        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment("Existing PDF Content"));
        doc.save(plainPath);
        doc.close();

        // Open and encrypt
        Document doc2 = new Document(plainPath);
        doc2.encrypt("pass", "admin", -3904, CryptoAlgorithm.AESx128);
        doc2.save(encPath);
        doc2.close();

        // Verify
        Document doc3 = new Document(encPath, "pass");
        TextAbsorber absorber = new TextAbsorber();
        doc3.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("Existing PDF Content"),
                "Encrypted existing PDF should be readable. Got: " + absorber.getText());
        doc3.close();
    }

    // ── Test: AESx256 (R6) round-trip ──
    //
    // Originally this slot asserted that AESx256 threw UnsupportedOperationException
    // — placeholder from before the R6 / AES-256 path was implemented. The full
    // chain (write-side: Document.encrypt → PDFKeyDerivation.computeHashR6 →
    // /UE+/OE+/Perms generation; read-side: StandardSecurityHandler.authenticate
    // R6 → PDFKeyDerivation.computeEncryptionKeyR6User/Owner) is now in place,
    // so we exercise it instead.

    @Test
    public void encrypt_AESx256_roundTrip_userPassword() throws Exception {
        String output = tempDir.resolve("enc_aes256_user.pdf").toString();

        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment("AES-256 Protected Content"));
        doc.encrypt("user256", "owner256", -3904, CryptoAlgorithm.AESx256);
        doc.save(output);
        doc.close();

        Document doc2 = new Document(output, "user256");
        TextAbsorber absorber = new TextAbsorber();
        doc2.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("AES-256 Protected Content"),
                "Text should survive AES-256 (R6) encrypt/decrypt with user password. Got: "
                        + absorber.getText());
        doc2.close();
    }

    @Test
    public void encrypt_AESx256_roundTrip_ownerPassword() throws Exception {
        String output = tempDir.resolve("enc_aes256_owner.pdf").toString();

        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment("AES-256 Owner Access"));
        doc.encrypt("u", "o", -3904, CryptoAlgorithm.AESx256);
        doc.save(output);
        doc.close();

        Document doc2 = new Document(output, "o");
        TextAbsorber absorber = new TextAbsorber();
        doc2.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("AES-256 Owner Access"),
                "Owner password should authorize AES-256 documents. Got: "
                        + absorber.getText());
        doc2.close();
    }

    @Test
    public void encrypt_AESx256_wrongPassword_throws() throws Exception {
        String output = tempDir.resolve("enc_aes256_wrong.pdf").toString();

        Document doc = new Document();
        doc.getPages().add();
        doc.encrypt("right", "owner", -3904, CryptoAlgorithm.AESx256);
        doc.save(output);
        doc.close();

        assertThrows(IOException.class, () -> new Document(output, "wrong"));
    }
}
