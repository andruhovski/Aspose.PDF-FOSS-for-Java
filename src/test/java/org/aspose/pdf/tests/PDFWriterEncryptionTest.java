package org.aspose.pdf.tests;

import org.aspose.pdf.CryptoAlgorithm;
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSNull;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.security.PDFEncryptionDict;
import org.aspose.pdf.engine.security.PDFEncryptor;
import org.aspose.pdf.engine.security.PDFKeyDerivation;
import org.aspose.pdf.engine.writer.PDFWriter;
import org.aspose.pdf.text.TextAbsorber;
import org.aspose.pdf.text.TextFragment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PDFWriter encryption integration (Step 6 of BUG-011).
 * Verifies encrypt → save → reopen round-trip for RC4-128 and AES-128,
 * non-encrypted regression, and correct exclusion of the /Encrypt dict.
 */
public class PDFWriterEncryptionTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    @TempDir
    Path tempDir;

    // ── Test 1: RC4-128 round-trip ──

    @Test
    public void roundTrip_RC4_128() throws Exception {
        String plainPath = tempDir.resolve("plain.pdf").toString();
        String encPath = tempDir.resolve("encrypted_rc4.pdf").toString();

        // Create unencrypted PDF with text
        createSimplePdf(plainPath, "Hello Encrypted World");

        // Encrypt it
        encryptFile(plainPath, encPath, "user", "owner", CryptoAlgorithm.RC4x128, -3904);

        // Reopen with password and verify text
        Document doc = new Document(encPath, "user");
        TextAbsorber absorber = new TextAbsorber();
        doc.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("Hello Encrypted World"),
                "Text should be readable after decryption. Got: " + absorber.getText());
        doc.close();
    }

    // ── Test 2: AES-128 round-trip ──

    @Test
    public void roundTrip_AES_128() throws Exception {
        String plainPath = tempDir.resolve("plain.pdf").toString();
        String encPath = tempDir.resolve("encrypted_aes.pdf").toString();

        createSimplePdf(plainPath, "AES Encrypted Content");

        encryptFile(plainPath, encPath, "aesuser", "aesowner", CryptoAlgorithm.AESx128, -3904);

        Document doc = new Document(encPath, "aesuser");
        TextAbsorber absorber = new TextAbsorber();
        doc.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("AES Encrypted Content"),
                "AES-encrypted text should be readable. Got: " + absorber.getText());
        doc.close();
    }

    // ── Test 3: Non-encrypted save regression ──

    @Test
    public void nonEncryptedSave_notBroken() throws Exception {
        String path = tempDir.resolve("noenc.pdf").toString();

        createSimplePdf(path, "No Encryption Here");

        Document doc = new Document(path);
        TextAbsorber absorber = new TextAbsorber();
        doc.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("No Encryption Here"),
                "Non-encrypted PDF should be readable. Got: " + absorber.getText());
        doc.close();
    }

    // ── Test 4: /Encrypt dict is not encrypted (readable in raw bytes) ──

    @Test
    public void encryptDict_notEncrypted() throws Exception {
        String plainPath = tempDir.resolve("plain.pdf").toString();
        String encPath = tempDir.resolve("encrypted_check.pdf").toString();

        createSimplePdf(plainPath, "Check Encrypt Dict");

        encryptFile(plainPath, encPath, "u", "o", CryptoAlgorithm.RC4x128, -3904);

        // Read raw bytes of encrypted file
        byte[] rawBytes = Files.readAllBytes(Path.of(encPath));
        String rawStr = new String(rawBytes, StandardCharsets.ISO_8859_1);

        // /Filter /Standard should be visible in plain text (part of /Encrypt dict)
        assertTrue(rawStr.contains("/Filter"), "/Filter should be visible in raw bytes");
        assertTrue(rawStr.contains("/Standard"), "/Standard should be visible in raw bytes");
        // /V and /R should be visible (they're integers, not encrypted)
        assertTrue(rawStr.contains("/V "), "/V should be visible in raw bytes");
        assertTrue(rawStr.contains("/R "), "/R should be visible in raw bytes");
    }

    // ── Test 5: Strings in metadata are encrypted ──

    @Test
    public void metadataStrings_encrypted() throws Exception {
        String plainPath = tempDir.resolve("plain_meta.pdf").toString();
        String encPath = tempDir.resolve("encrypted_meta.pdf").toString();

        // Create a PDF with a title in the /Info dict
        createPdfWithTitle(plainPath, "Secret Title XYZ", "Some text on page");

        encryptFile(plainPath, encPath, "user", "owner", CryptoAlgorithm.RC4x128, -3904);

        // Raw bytes should NOT contain "Secret Title XYZ" (it's encrypted)
        byte[] rawBytes = Files.readAllBytes(Path.of(encPath));
        String rawStr = new String(rawBytes, StandardCharsets.ISO_8859_1);
        assertFalse(rawStr.contains("Secret Title XYZ"),
                "Title should be encrypted — not visible in raw bytes");

        // But reopening with password should recover it
        Document doc = new Document(encPath, "user");
        if (doc.getInfo() != null) {
            assertEquals("Secret Title XYZ", doc.getInfo().getTitle(),
                    "Title should be recoverable after decryption");
        }
        doc.close();
    }

    // ── Test: Owner password also works ──

    @Test
    public void ownerPassword_works() throws Exception {
        String plainPath = tempDir.resolve("plain.pdf").toString();
        String encPath = tempDir.resolve("encrypted_owner.pdf").toString();

        createSimplePdf(plainPath, "Owner Test");

        encryptFile(plainPath, encPath, "user", "owner", CryptoAlgorithm.RC4x128, -3904);

        // Open with owner password
        Document doc = new Document(encPath, "owner");
        TextAbsorber absorber = new TextAbsorber();
        doc.getPages().get(1).accept(absorber);
        assertTrue(absorber.getText().contains("Owner Test"),
                "Owner password should also work. Got: " + absorber.getText());
        doc.close();
    }

    // ── Helpers ──

    /**
     * Creates a simple one-page PDF with the given text.
     */
    private void createSimplePdf(String path, String text) throws IOException {
        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment(text));
        doc.save(path);
        doc.close();
    }

    /**
     * Creates a PDF with a title in /Info and text on a page.
     */
    private void createPdfWithTitle(String path, String title, String text) throws IOException {
        // Create the base PDF first, then modify it to add /Info with title
        Document doc = new Document();
        Page page = doc.getPages().add();
        page.getParagraphs().add(new TextFragment(text));
        doc.save(path);
        doc.close();

        // Reopen, add title to /Info dict, and re-save as full rewrite
        addInfoTitle(path, title);
    }

    /**
     * Opens a PDF, adds /Title to the /Info dict, and rewrites the file.
     */
    private void addInfoTitle(String path, String title) throws IOException {
        RandomAccessReader reader = RandomAccessReader.fromFile(new File(path));
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        Map<COSObjectKey, COSBase> objects = loadAllObjects(parser);
        int maxObjNum = maxObjectNumber(objects);

        // Create /Info dictionary with /Title
        COSDictionary infoDict = new COSDictionary();
        infoDict.set(COSName.of("Title"), new COSString(title));
        COSObjectKey infoKey = new COSObjectKey(++maxObjNum, 0);
        objects.put(infoKey, infoDict);

        // Update trailer with /Info reference
        COSDictionary trailer = new COSDictionary(parser.getTrailer());
        trailer.remove(COSName.of("Prev"));
        trailer.remove(COSName.of("XRefStm"));
        trailer.set(COSName.INFO, new COSObjectReference(infoKey, k -> objects.get(k)));

        try (FileOutputStream fos = new FileOutputStream(path)) {
            PDFWriter writer = new PDFWriter(fos, parser.getVersion());
            writer.write(trailer, objects);
        }
        reader.close();
    }

    /**
     * Opens an unencrypted PDF, encrypts it, and saves to a new file.
     */
    private void encryptFile(String input, String output,
                              String userPw, String ownerPw,
                              CryptoAlgorithm algorithm, int permissions) throws Exception {
        RandomAccessReader reader = RandomAccessReader.fromFile(new File(input));
        PDFParser parser = new PDFParser(reader);
        parser.parse();

        Map<COSObjectKey, COSBase> objects = loadAllObjects(parser);
        int maxObjNum = maxObjectNumber(objects);

        byte[] userPwBytes = userPw.getBytes(StandardCharsets.UTF_8);
        byte[] ownerPwBytes = ownerPw.getBytes(StandardCharsets.UTF_8);
        byte[] documentId = randomBytes(16);

        // Determine key length and revision from algorithm
        int keyLenBytes, R;
        switch (algorithm) {
            case RC4x40:  keyLenBytes = 5;  R = 2; break;
            case RC4x128: keyLenBytes = 16; R = 3; break;
            case AESx128: keyLenBytes = 16; R = 4; break;
            default:
                throw new IllegalArgumentException("Unsupported algorithm for this test: " + algorithm);
        }

        // Generate O hash
        byte[] O = PDFKeyDerivation.generateO_R2R4(ownerPwBytes, userPwBytes, keyLenBytes, R);

        // Compute encryption key (needs temp dict with O)
        PDFEncryptionDict tempDict = PDFEncryptionDict.build(
                algorithm, permissions, O, new byte[32], null, null, null);
        byte[] encKey = PDFKeyDerivation.computeEncryptionKeyR2R4(userPwBytes, tempDict, documentId);

        // Generate U hash
        byte[] U;
        if (R == 2) {
            U = PDFKeyDerivation.generateU_R2(encKey);
        } else {
            U = PDFKeyDerivation.generateU_R3R4(encKey, documentId);
        }

        // Build final encryption dict
        PDFEncryptionDict encDict = PDFEncryptionDict.build(
                algorithm, permissions, O, U, null, null, null);
        PDFEncryptor encryptor = new PDFEncryptor(encKey, encDict);

        // Register /Encrypt dict as indirect object
        COSObjectKey encDictKey = new COSObjectKey(++maxObjNum, 0);
        objects.put(encDictKey, encDict.getCOSDictionary());

        // Build trailer with /Encrypt and /ID
        COSDictionary trailer = new COSDictionary(parser.getTrailer());
        trailer.remove(COSName.of("Prev"));
        trailer.remove(COSName.of("XRefStm"));
        trailer.set(COSName.of("Encrypt"), new COSObjectReference(encDictKey, k -> objects.get(k)));

        COSArray idArray = new COSArray();
        idArray.add(new COSString(documentId));
        idArray.add(new COSString(documentId));
        trailer.set(COSName.of("ID"), idArray);

        // Write encrypted PDF
        try (FileOutputStream fos = new FileOutputStream(output)) {
            PDFWriter writer = new PDFWriter(fos, parser.getVersion());
            writer.setEncryptor(encryptor, encDictKey);
            writer.write(trailer, objects);
        }

        reader.close();
    }

    private Map<COSObjectKey, COSBase> loadAllObjects(PDFParser parser) throws IOException {
        Map<COSObjectKey, COSBase> objects = new LinkedHashMap<>();
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj = parser.getObject(key);
            if (obj != null && !(obj instanceof COSNull)) {
                objects.put(key, obj);
            }
        }
        return objects;
    }

    private int maxObjectNumber(Map<COSObjectKey, COSBase> objects) {
        return objects.keySet().stream()
                .mapToInt(COSObjectKey::getObjectNumber)
                .max().orElse(0);
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
