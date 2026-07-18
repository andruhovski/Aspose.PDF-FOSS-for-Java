package org.aspose.pdf.engine.security;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.logging.Logger;

/// Standard security handler — validates passwords and produces encryption keys.
///
/// Implements Algorithms 4-7 (R2-R4) and 8-12 (R5/R6) from
/// ISO 32000-1:2008, §7.6.3.
///
public class StandardSecurityHandler {

    private static final Logger LOG = Logger.getLogger(StandardSecurityHandler.class.getName());

    private final PDFEncryptionDict encDict;
    private final byte[] documentId;
    private byte[] encryptionKey;
    private boolean authenticated;

    /// Creates a security handler for the given encryption dictionary.
    ///
    /// @param encDict    the encryption dictionary
    /// @param documentId the first element of the /ID array (may be null)
    public StandardSecurityHandler(PDFEncryptionDict encDict, byte[] documentId) {
        this.encDict = encDict;
        this.documentId = documentId;
    }

    /// Tries to authenticate with the given password.
    /// Attempts user password first, then owner password.
    ///
    /// @param password the password bytes (empty array for no password)
    /// @return true if authenticated
    public boolean authenticate(byte[] password) {
        if (password == null) password = new byte[0];
        int r = encDict.getR();

        if (r <= 4) {
            if (authenticateUserR2R4(password)) return true;
            if (authenticateOwnerR2R4(password)) return true;
        } else if (r == 5 || r == 6) {
            if (authenticateUserR6(password)) return true;
            if (authenticateOwnerR6(password)) return true;
        }
        return false;
    }

    /// Attempts authentication using only the user-password branch.
    ///
    /// @param password the candidate password bytes
    /// @return `true` if the password is a valid user password
    public boolean authenticateUserPassword(byte[] password) {
        if (password == null) password = new byte[0];
        int r = encDict.getR();
        if (r <= 4) {
            return authenticateUserR2R4(password);
        }
        if (r == 5 || r == 6) {
            return authenticateUserR6(password);
        }
        return false;
    }

    /// Attempts authentication using only the owner-password branch.
    ///
    /// @param password the candidate password bytes
    /// @return `true` if the password is a valid owner password
    public boolean authenticateOwnerPassword(byte[] password) {
        if (password == null) password = new byte[0];
        int r = encDict.getR();
        if (r <= 4) {
            return authenticateOwnerR2R4(password);
        }
        if (r == 5 || r == 6) {
            return authenticateOwnerR6(password);
        }
        return false;
    }

    /// Algorithm 6: Authenticate user password (R2-R4).
    private boolean authenticateUserR2R4(byte[] password) {
        try {
            byte[] key = PDFKeyDerivation.computeEncryptionKeyR2R4(password, encDict, documentId);
            byte[] computed;

            if (encDict.getR() == 2) {
                // Algorithm 4
                computed = RC4Cipher.process(key, PDFKeyDerivation.PADDING.clone());
            } else {
                // Algorithm 5
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(PDFKeyDerivation.PADDING);
                if (documentId != null) md5.update(documentId);
                byte[] hash = md5.digest();

                byte[] result = RC4Cipher.process(key, hash);
                for (int i = 1; i <= 19; i++) {
                    byte[] iterKey = new byte[key.length];
                    for (int j = 0; j < key.length; j++) {
                        iterKey[j] = (byte) (key[j] ^ i);
                    }
                    result = RC4Cipher.process(iterKey, result);
                }
                computed = new byte[32];
                System.arraycopy(result, 0, computed, 0, Math.min(result.length, 16));
            }

            byte[] stored = encDict.getU();
            int compareLen = (encDict.getR() == 2) ? 32 : 16;
            if (stored.length >= compareLen && computed.length >= compareLen
                    && Arrays.equals(Arrays.copyOf(stored, compareLen),
                                     Arrays.copyOf(computed, compareLen))) {
                this.encryptionKey = key;
                this.authenticated = true;
                LOG.fine("User password authenticated (R=" + encDict.getR() + ")");
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.fine(() -> "User auth R2-R4 failed: " + e.getMessage());
            return false;
        }
    }

    /// Algorithm 7: Authenticate owner password (R2-R4).
    private boolean authenticateOwnerR2R4(byte[] ownerPassword) {
        try {
            byte[] paddedPw = PDFKeyDerivation.padPassword(ownerPassword);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hash = md5.digest(paddedPw);
            int keyLen = encDict.getKeyLength();

            if (encDict.getR() >= 3) {
                for (int i = 0; i < 50; i++) {
                    md5.reset();
                    md5.update(hash, 0, keyLen);
                    hash = md5.digest();
                }
            }

            byte[] ownerKey = Arrays.copyOf(hash, (encDict.getR() == 2) ? 5 : keyLen);
            byte[] userPw;

            if (encDict.getR() == 2) {
                userPw = RC4Cipher.process(ownerKey, encDict.getO());
            } else {
                byte[] result = encDict.getO().clone();
                for (int i = 19; i >= 0; i--) {
                    byte[] iterKey = new byte[ownerKey.length];
                    for (int j = 0; j < ownerKey.length; j++) {
                        iterKey[j] = (byte) (ownerKey[j] ^ i);
                    }
                    result = RC4Cipher.process(iterKey, result);
                }
                userPw = result;
            }
            return authenticateUserR2R4(userPw);
        } catch (Exception e) {
            LOG.fine(() -> "Owner auth R2-R4 failed: " + e.getMessage());
            return false;
        }
    }

    /// Algorithm 11: Authenticate user password (R=5/R=6).
    private boolean authenticateUserR6(byte[] password) {
        try {
            byte[] u = encDict.getU();
            if (u == null || u.length < 48) return false;

            byte[] validationSalt = Arrays.copyOfRange(u, 32, 40);
            byte[] hash = encDict.getR() == 6
                    ? PDFKeyDerivation.computeHashR6(password, validationSalt, null)
                    : hashR5(password, validationSalt, null);

            if (Arrays.equals(hash, Arrays.copyOf(u, 32))) {
                this.encryptionKey = encDict.getR() == 6
                        ? PDFKeyDerivation.computeEncryptionKeyR6User(password, encDict)
                        : PDFKeyDerivation.computeEncryptionKeyR5User(password, encDict);
                this.authenticated = true;
                LOG.fine(() -> "User password authenticated (R=" + encDict.getR() + ")");
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.fine(() -> "User auth R6 failed: " + e.getMessage());
            return false;
        }
    }

    /// Algorithm 12: Authenticate owner password (R=5/R=6).
    private boolean authenticateOwnerR6(byte[] password) {
        try {
            byte[] o = encDict.getO();
            byte[] u = encDict.getU();
            if (o == null || o.length < 48 || u == null) return false;

            byte[] validationSalt = Arrays.copyOfRange(o, 32, 40);
            byte[] userKey = Arrays.copyOf(u, Math.min(48, u.length));
            byte[] hash = encDict.getR() == 6
                    ? PDFKeyDerivation.computeHashR6(password, validationSalt, userKey)
                    : hashR5(password, validationSalt, userKey);

            if (Arrays.equals(hash, Arrays.copyOf(o, 32))) {
                this.encryptionKey = encDict.getR() == 6
                        ? PDFKeyDerivation.computeEncryptionKeyR6Owner(password, encDict)
                        : PDFKeyDerivation.computeEncryptionKeyR5Owner(password, encDict);
                this.authenticated = true;
                LOG.fine(() -> "Owner password authenticated (R=" + encDict.getR() + ")");
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.fine(() -> "Owner auth R6 failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isAuthenticated() { return authenticated; }
    public byte[] getEncryptionKey() { return encryptionKey; }
    public PDFEncryptionDict getEncryptionDict() { return encDict; }

    private byte[] hashR5(byte[] password, byte[] salt, byte[] userKey) throws Exception {
        byte[] truncPw = password.length > 127 ? Arrays.copyOf(password, 127) : password;
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(truncPw);
        sha256.update(salt);
        if (userKey != null) {
            sha256.update(userKey);
        }
        return sha256.digest();
    }
}
