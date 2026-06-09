package org.aspose.pdf.facades;

import org.aspose.pdf.CryptoAlgorithm;
import org.aspose.pdf.Document;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.security.PDFEncryptionDict;
import org.aspose.pdf.engine.security.StandardSecurityHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods for managing PDF document security: encryption, decryption,
 * passwords, and access permissions.
 */
public class PdfFileSecurity implements Closeable {

    private static final Logger LOG = Logger.getLogger(PdfFileSecurity.class.getName());

    private Document document;
    private String inputFile;
    private String outputFile;
    private byte[] inputBytes;
    private OutputStream outputStream;
    private boolean allowExceptions = true;

    /**
     * Creates a new {@code PdfFileSecurity} instance.
     */
    public PdfFileSecurity() {
    }

    /**
     * Creates a facade bound to the specified input and output files.
     *
     * @param inputFile input PDF path
     * @param outputFile output PDF path
     */
    public PdfFileSecurity(String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    /**
     * Creates a facade bound to the specified input and output streams.
     *
     * @param inputStream input PDF stream
     * @param outputStream output PDF stream
     * @throws IOException if the input stream cannot be buffered
     */
    public PdfFileSecurity(InputStream inputStream, OutputStream outputStream) throws IOException {
        setInputStream(inputStream);
        this.outputStream = outputStream;
    }

    /**
     * Creates a facade bound to the specified document and output stream.
     *
     * @param document already opened document
     * @param outputStream output PDF stream
     */
    public PdfFileSecurity(Document document, OutputStream outputStream) {
        bindPdf(document);
        this.outputStream = outputStream;
    }

    /**
     * Returns the configured input file path.
     *
     * @return the input file path, or {@code null}
     */
    public String getInputFile() {
        return inputFile;
    }

    /**
     * Sets the configured input file path.
     *
     * @param inputFile the input file path
     */
    public void setInputFile(String inputFile) {
        closeDocumentOnly();
        this.inputFile = inputFile;
        this.inputBytes = null;
    }

    /**
     * Returns the configured output file path.
     *
     * @return the output file path, or {@code null}
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Sets the configured output file path.
     *
     * @param outputFile the output file path
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * Sets the configured input stream. The stream contents are buffered so the
     * caller can continue using the original stream after facade operations.
     *
     * @param inputStream the input PDF stream
     * @throws IOException if buffering fails
     */
    public void setInputStream(InputStream inputStream) throws IOException {
        closeDocumentOnly();
        this.inputBytes = inputStream != null ? readAllBytes(inputStream) : null;
        this.inputFile = null;
    }

    /** Returns the bound Document, or {@code null}. Mirrors C# {@code PdfFileSecurity.Document}. */
    public Document getDocument() {
        return document;
    }

    /**
     * Returns the configured output stream.
     *
     * @return the output stream, or {@code null}
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Sets the configured output stream.
     *
     * @param outputStream the output stream
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Returns whether facade methods should throw immediately when an operation fails.
     *
     * @return {@code true} when exceptions are propagated
     */
    public boolean isAllowExceptions() {
        return allowExceptions;
    }

    /**
     * Sets the exception behavior flag.
     *
     * @param allowExceptions exception behavior flag
     */
    public void setAllowExceptions(boolean allowExceptions) {
        if (!allowExceptions) {
            throw new UnsupportedOperationException("PdfFileSecurity.AllowExceptions=false is not supported");
        }
        this.allowExceptions = true;
    }

    /**
     * Binds a PDF file to this security facade.
     *
     * @param inputFile path to the PDF file
     * @return {@code true} on success
     */
    public boolean bindPdf(String inputFile) {
        closeDocumentOnly();
        this.inputFile = inputFile;
        this.inputBytes = null;
        try {
            this.document = new Document(inputFile);
            return true;
        } catch (Exception e) {
            if (isDeferredEncryptedOpen(e)) {
                LOG.fine("Bound encrypted PDF without opening it eagerly: " + inputFile);
                this.document = null;
                return true;
            }
            LOG.log(Level.WARNING, "Failed to bind PDF from file: " + inputFile, e);
            return false;
        }
    }

    /**
     * Binds a PDF from an input stream.
     *
     * @param inputStream the input stream containing PDF data
     * @return {@code true} on success
     */
    public boolean bindPdf(InputStream inputStream) {
        closeDocumentOnly();
        try {
            this.inputBytes = readAllBytes(inputStream);
            this.inputFile = null;
            this.document = new Document(new ByteArrayInputStream(this.inputBytes));
            return true;
        } catch (Exception e) {
            if (isDeferredEncryptedOpen(e)) {
                LOG.fine("Bound encrypted PDF stream without opening it eagerly");
                this.document = null;
                return true;
            }
            LOG.log(Level.WARNING, "Failed to bind PDF from stream", e);
            return false;
        }
    }

    /**
     * Binds an existing {@link Document} to this security facade.
     *
     * @param document the document to bind
     * @return {@code true} on success
     */
    public boolean bindPdf(Document document) {
        if (document == null) {
            LOG.warning("Cannot bind null document");
            return false;
        }
        closeDocumentOnly();
        this.document = document;
        this.inputFile = null;
        this.inputBytes = null;
        return true;
    }

    /**
     * Saves the bound document to a file.
     *
     * @param outputFile path to the output file
     * @return {@code true} on success
     */
    public boolean save(String outputFile) {
        try {
            if (document == null) {
                LOG.warning("No document bound");
                return false;
            }
            document.requestFullRewrite();
            document.save(outputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PDF to file: " + outputFile, e);
            return false;
        }
    }

    /**
     * Saves the bound document to an output stream.
     *
     * @param outputStream the output stream
     * @return {@code true} on success
     */
    public boolean save(OutputStream outputStream) {
        try {
            if (document == null) {
                LOG.warning("No document bound");
                return false;
            }
            document.requestFullRewrite();
            document.save(outputStream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PDF to stream", e);
            return false;
        }
    }

    /**
     * Saves the bound document to the configured output destination.
     *
     * @return {@code true} on success
     */
    public boolean save() {
        if (outputFile != null) {
            return save(outputFile);
        }
        if (outputStream != null) {
            return save(outputStream);
        }
        LOG.warning("No output destination configured");
        return false;
    }

    /**
     * Decrypts the bound PDF document.
     *
     * @param password the document password
     * @return {@code true} if successful
     */
    public boolean decryptFile(String password) {
        try {
            Document bound = ensureDocument(password);
            if (!bound.isEncrypted()) {
                return true;
            }
            bound.decrypt();
            return true;
        } catch (Exception e) {
            return handleFailure("decryptFile", e);
        }
    }

    /**
     * Returns whether the bound document is encrypted.
     *
     * @return {@code true} if the document is encrypted
     */
    public boolean isEncrypted() {
        if (document == null) {
            return false;
        }
        return document.isEncrypted();
    }

    /**
     * Sets document privileges using explicit password strings and a raw bitmask.
     *
     * @param userPassword the user password
     * @param ownerPassword the owner password
     * @param permissions the permission bitmask
     * @return {@code true} on success
     */
    public boolean setPrivilege(String userPassword, String ownerPassword, int permissions) {
        return setPrivilege(userPassword, ownerPassword, new DocumentPrivilege(permissions));
    }

    /**
     * Sets document privileges using empty passwords.
     *
     * @param privilege privilege flags to apply
     * @return {@code true} on success
     */
    public boolean setPrivilege(DocumentPrivilege privilege) {
        return setPrivilege("", "", privilege);
    }

    /**
     * Sets document privileges and applies standard encryption using the supplied
     * passwords.
     *
     * @param userPassword user password
     * @param ownerPassword owner password
     * @param privilege privilege flags to apply
     * @return {@code true} on success
     */
    public boolean setPrivilege(String userPassword, String ownerPassword, DocumentPrivilege privilege) {
        try {
            Document bound = ensureDocument(null);
            CryptoAlgorithm algorithm = inferExistingAlgorithm(bound, CryptoAlgorithm.AESx128);
            bound.encrypt(userPassword, ownerPassword, privilege, algorithm, false);
            return true;
        } catch (Exception e) {
            return handleFailure("setPrivilege", e);
        }
    }

    /**
     * Encrypts the document using the specified key size.
     *
     * @param userPassword user password
     * @param ownerPassword owner password
     * @param privilege privilege flags
     * @param keySize encryption key size
     * @return {@code true} on success
     */
    public boolean encryptFile(String userPassword, String ownerPassword,
                               DocumentPrivilege privilege, KeySize keySize) {
        return encryptFile(userPassword, ownerPassword, privilege, keySize, null);
    }

    /**
     * Encrypts the document using the specified key size and algorithm family.
     *
     * @param userPassword user password
     * @param ownerPassword owner password
     * @param privilege privilege flags
     * @param keySize encryption key size
     * @param algorithm algorithm family
     * @return {@code true} on success
     */
    public boolean encryptFile(String userPassword, String ownerPassword,
                               DocumentPrivilege privilege, KeySize keySize, Algorithm algorithm) {
        try {
            Document bound = ensureDocument(null);
            bound.encrypt(userPassword, ownerPassword, privilege, mapAlgorithm(keySize, algorithm), false);
            return true;
        } catch (Exception e) {
            return handleFailure("encryptFile", e);
        }
    }

    /**
     * Changes the document passwords while preserving the current permissions and
     * algorithm.
     *
     * @param oldPassword current owner password
     * @param newPassword new user password
     * @param newOwnerPassword new owner password
     * @return {@code true} on success
     */
    public boolean changePassword(String oldPassword, String newPassword, String newOwnerPassword) {
        try {
            Document bound = ensureDocument(oldPassword);
            if (!bound.isEncrypted()) {
                throw new IllegalStateException(
                        "Pdf document is not encrypted, so don't provide password to get access.");
            }
            validateOwnerPassword(bound, oldPassword);
            bound.encrypt(newPassword, newOwnerPassword,
                    inferExistingPermissions(bound),
                    inferExistingAlgorithm(bound, CryptoAlgorithm.RC4x40));
            return true;
        } catch (Exception e) {
            return handleFailure("changePassword", e);
        }
    }

    /**
     * Changes passwords and privilege flags using the specified key size.
     *
     * @param oldPassword old owner password
     * @param newPassword new user password
     * @param newOwnerPassword new owner password
     * @param privilege new privileges
     * @param keySize target key size
     * @return {@code true} on success
     */
    public boolean changePassword(String oldPassword, String newPassword, String newOwnerPassword,
                                  DocumentPrivilege privilege, KeySize keySize) {
        return changePassword(oldPassword, newPassword, newOwnerPassword, privilege, keySize, null);
    }

    /**
     * Changes passwords, privilege flags, and algorithm family.
     *
     * @param oldPassword old owner password
     * @param newPassword new user password
     * @param newOwnerPassword new owner password
     * @param privilege new privileges
     * @param keySize target key size
     * @param algorithm target algorithm family
     * @return {@code true} on success
     */
    public boolean changePassword(String oldPassword, String newPassword, String newOwnerPassword,
                                  DocumentPrivilege privilege, KeySize keySize, Algorithm algorithm) {
        try {
            Document bound = ensureDocument(oldPassword);
            if (!bound.isEncrypted()) {
                throw new IllegalStateException(
                        "Pdf document is not encrypted, so don't provide password to get access.");
            }
            validateOwnerPassword(bound, oldPassword);
            bound.encrypt(newPassword, newOwnerPassword, privilege, mapAlgorithm(keySize, algorithm), false);
            return true;
        } catch (Exception e) {
            return handleFailure("changePassword", e);
        }
    }

    /**
     * Attempts to set privileges and returns {@code false} instead of throwing.
     */
    public boolean trySetPrivilege(String userPassword, String ownerPassword, DocumentPrivilege privilege) {
        try {
            return setPrivilege(userPassword, ownerPassword, privilege);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Attempts to encrypt and returns {@code false} instead of throwing.
     */
    public boolean tryEncryptFile(String userPassword, String ownerPassword,
                                  DocumentPrivilege privilege, KeySize keySize) {
        return tryEncryptFile(userPassword, ownerPassword, privilege, keySize, null);
    }

    /**
     * Attempts to encrypt and returns {@code false} instead of throwing.
     */
    public boolean tryEncryptFile(String userPassword, String ownerPassword,
                                  DocumentPrivilege privilege, KeySize keySize, Algorithm algorithm) {
        try {
            return encryptFile(userPassword, ownerPassword, privilege, keySize, algorithm);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Attempts to decrypt and returns {@code false} instead of throwing.
     */
    public boolean tryDecryptFile(String password) {
        try {
            Document bound = ensureDocument(password);
            if (!bound.isEncrypted()) {
                return false;
            }
            bound.decrypt();
            return true;
        } catch (RuntimeException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Attempts to change password and returns {@code false} instead of throwing.
     */
    public boolean tryChangePassword(String oldPassword, String newPassword, String newOwnerPassword) {
        try {
            return changePassword(oldPassword, newPassword, newOwnerPassword);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Attempts to change password and returns {@code false} instead of throwing.
     */
    public boolean tryChangePassword(String oldPassword, String newPassword, String newOwnerPassword,
                                     DocumentPrivilege privilege, KeySize keySize) {
        try {
            return changePassword(oldPassword, newPassword, newOwnerPassword, privilege, keySize);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Attempts to change password and returns {@code false} instead of throwing.
     */
    public boolean tryChangePassword(String oldPassword, String newPassword, String newOwnerPassword,
                                     DocumentPrivilege privilege, KeySize keySize, Algorithm algorithm) {
        try {
            return changePassword(oldPassword, newPassword, newOwnerPassword, privilege, keySize, algorithm);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Closes the security facade and releases the bound document.
     */
    @Override
    public void close() {
        closeDocumentOnly();
    }

    private void closeDocumentOnly() {
        if (document == null) {
            return;
        }
        try {
            document.close();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Error closing document", e);
        }
        document = null;
    }

    private Document ensureDocument(String password) throws IOException {
        if (document != null) {
            return document;
        }
        if (inputFile != null) {
            document = password == null ? new Document(inputFile) : new Document(inputFile, password);
            return document;
        }
        if (inputBytes != null) {
            document = password == null
                    ? new Document(new ByteArrayInputStream(inputBytes))
                    : new Document(new ByteArrayInputStream(inputBytes), password);
            return document;
        }
        throw new IllegalStateException("No PDF is bound");
    }

    private boolean handleFailure(String operation, Exception e) {
        if (allowExceptions) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException(operation + " failed: " + e.getMessage(), e);
        }
        LOG.log(Level.WARNING, operation + " failed", e);
        return false;
    }

    private static boolean isDeferredEncryptedOpen(Exception e) {
        return e instanceof IOException
                && e.getMessage() != null
                && e.getMessage().contains("Invalid password for encrypted PDF");
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = inputStream.read(chunk)) >= 0) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private static CryptoAlgorithm mapAlgorithm(KeySize keySize, Algorithm algorithm) {
        if (keySize == null) {
            throw new IllegalArgumentException("keySize must not be null");
        }
        switch (keySize) {
            case x40:
                return CryptoAlgorithm.RC4x40;
            case x128:
                if (algorithm == Algorithm.RC4) {
                    return CryptoAlgorithm.RC4x128;
                }
                return CryptoAlgorithm.AESx128;
            case x256:
                if (algorithm == Algorithm.RC4) {
                    throw new IllegalArgumentException("RC4 does not support 256-bit encryption");
                }
                return CryptoAlgorithm.AESx256;
            default:
                throw new IllegalArgumentException("Unsupported key size: " + keySize);
        }
    }

    private static CryptoAlgorithm inferExistingAlgorithm(Document document, CryptoAlgorithm fallback) throws IOException {
        PDFEncryptionDict encDict = getEncryptionDict(document);
        if (encDict == null) {
            return fallback;
        }
        switch (encDict.getCipherType()) {
            case RC4:
                return encDict.getLength() <= 40 ? CryptoAlgorithm.RC4x40 : CryptoAlgorithm.RC4x128;
            case AES_128:
                return CryptoAlgorithm.AESx128;
            case AES_256:
                return CryptoAlgorithm.AESx256;
            default:
                return fallback;
        }
    }

    private static int inferExistingPermissions(Document document) throws IOException {
        PDFEncryptionDict encDict = getEncryptionDict(document);
        return encDict != null ? encDict.getP() : 0;
    }

    private static void validateOwnerPassword(Document document, String password) throws IOException {
        PDFParser parser = document.getParser();
        PDFEncryptionDict encDict = getEncryptionDict(document);
        if (parser == null || encDict == null) {
            return;
        }
        StandardSecurityHandler handler = new StandardSecurityHandler(encDict, getDocumentId(parser));
        byte[] passwordBytes = password == null ? new byte[0] : password.getBytes(StandardCharsets.ISO_8859_1);
        if (!handler.authenticateOwnerPassword(passwordBytes)) {
            throw new IllegalStateException("Only owner password can change credentials.");
        }
    }

    private static PDFEncryptionDict getEncryptionDict(Document document) throws IOException {
        PDFParser parser = document.getParser();
        if (parser == null || !parser.isEncrypted()) {
            return null;
        }
        PdfBase encryptRef = parser.getTrailer().get(PdfName.of("Encrypt"));
        PdfBase encryptObj = parser.resolveReference(encryptRef);
        if (!(encryptObj instanceof PdfDictionary)) {
            return null;
        }
        return new PDFEncryptionDict((PdfDictionary) encryptObj);
    }

    private static byte[] getDocumentId(PDFParser parser) {
        if (parser == null) {
            return null;
        }
        PdfBase idArray = parser.getTrailer().get(PdfName.of("ID"));
        if (!(idArray instanceof PdfArray) || ((PdfArray) idArray).size() == 0) {
            return null;
        }
        PdfBase firstId = ((PdfArray) idArray).get(0);
        return firstId instanceof PdfString ? ((PdfString) firstId).getBytes() : null;
    }
}
