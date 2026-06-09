package org.aspose.pdf.engine.security;

import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * PDF encryption key derivation algorithms.
 * <p>
 * Implements Algorithm 2 (§7.6.3.3) for R2-R4, and
 * Algorithms 8/11 (§7.6.3.3.3) for R5/R6 (AES-256).
 * </p>
 */
public final class PDFKeyDerivation {

    /** The 32-byte padding string from Algorithm 2 step (a). */
    public static final byte[] PADDING = {
        (byte) 0x28, (byte) 0xBF, (byte) 0x4E, (byte) 0x5E,
        (byte) 0x4E, (byte) 0x75, (byte) 0x8A, (byte) 0x41,
        (byte) 0x64, (byte) 0x00, (byte) 0x4E, (byte) 0x56,
        (byte) 0xFF, (byte) 0xFA, (byte) 0x01, (byte) 0x08,
        (byte) 0x2E, (byte) 0x2E, (byte) 0x00, (byte) 0xB6,
        (byte) 0xD0, (byte) 0x68, (byte) 0x3E, (byte) 0x80,
        (byte) 0x2F, (byte) 0x0C, (byte) 0xA9, (byte) 0xFE,
        (byte) 0x64, (byte) 0x53, (byte) 0x69, (byte) 0x7A
    };

    private PDFKeyDerivation() {}

    /**
     * Algorithm 2: Compute encryption key for R2-R4.
     *
     * @param password   user password bytes (may be empty)
     * @param encDict    the encryption dictionary
     * @param documentId first element of /ID array from trailer
     * @return the encryption key
     */
    public static byte[] computeEncryptionKeyR2R4(byte[] password, PDFEncryptionDict encDict,
                                                    byte[] documentId) {
        try {
            int keyLen = encDict.getKeyLength();
            byte[] paddedPw = padPassword(password);
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            md5.update(paddedPw);
            md5.update(encDict.getO());

            int p = encDict.getP();
            md5.update((byte) (p & 0xFF));
            md5.update((byte) ((p >> 8) & 0xFF));
            md5.update((byte) ((p >> 16) & 0xFF));
            md5.update((byte) ((p >> 24) & 0xFF));

            if (documentId != null) {
                md5.update(documentId);
            }

            if (encDict.getR() >= 4 && !encDict.getEncryptMetadata()) {
                md5.update(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
            }

            byte[] hash = md5.digest();

            if (encDict.getR() >= 3) {
                for (int i = 0; i < 50; i++) {
                    md5.reset();
                    md5.update(hash, 0, keyLen);
                    hash = md5.digest();
                }
            }

            int n = (encDict.getR() == 2) ? 5 : keyLen;
            return Arrays.copyOf(hash, n);
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Compute encryption key for R=5/R=6 (AES-256).
     * Decrypts /UE using a key derived from password + key salt from /U.
     */
    public static byte[] computeEncryptionKeyR6User(byte[] password, PDFEncryptionDict encDict) {
        try {
            byte[] u = encDict.getU();
            byte[] ue = encDict.getUE();
            if (u == null || u.length < 48 || ue == null || ue.length < 32) {
                throw new RuntimeException("Missing U/UE for R6 key derivation");
            }

            byte[] keySalt = Arrays.copyOfRange(u, 40, 48);
            byte[] key = computeHashR6(password, keySalt, null);

            return AESCipher.decryptWithIV(key, new byte[16], ue);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("R6 user key derivation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Compute encryption key for R=6 via owner password.
     */
    public static byte[] computeEncryptionKeyR6Owner(byte[] password, PDFEncryptionDict encDict) {
        try {
            byte[] o = encDict.getO();
            byte[] u = encDict.getU();
            byte[] oe = encDict.getOE();
            if (o == null || o.length < 48 || oe == null || oe.length < 32 || u == null) {
                throw new RuntimeException("Missing O/OE/U for R6 owner key derivation");
            }

            byte[] keySalt = Arrays.copyOfRange(o, 40, 48);
            byte[] key = computeHashR6(password, keySalt, Arrays.copyOf(u, Math.min(48, u.length)));

            return AESCipher.decryptWithIV(key, new byte[16], oe);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("R6 owner key derivation failed: " + e.getMessage(), e);
        }
    }

    /**
     * R=5 (Adobe Extension Level 3) hash: a single SHA-256 of
     * {@code password + salt [+ userKey]}, with NO Algorithm 2.B iteration.
     * <p>
     * This is the crucial difference from {@link #computeHashR6}: R=5 predates
     * ISO 32000-2 and uses only the initial hash. Using the iterated R=6 hash on
     * an R=5 file yields the wrong key and AES decryption produces garbage.
     * </p>
     */
    public static byte[] computeHashR5(byte[] password, byte[] salt, byte[] userKey) {
        try {
            byte[] truncPw = truncatePassword(password);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(truncPw);
            if (salt != null) {
                sha256.update(salt);
            }
            if (userKey != null) {
                sha256.update(userKey);
            }
            return sha256.digest();
        } catch (Exception e) {
            throw new RuntimeException("R5 hash derivation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Compute the file encryption key for R=5 via the user password
     * (Adobe Extension Level 3 — single SHA-256, no iteration).
     */
    public static byte[] computeEncryptionKeyR5User(byte[] password, PDFEncryptionDict encDict) {
        try {
            byte[] u = encDict.getU();
            byte[] ue = encDict.getUE();
            if (u == null || u.length < 48 || ue == null || ue.length < 32) {
                throw new RuntimeException("Missing U/UE for R5 key derivation");
            }
            byte[] keySalt = Arrays.copyOfRange(u, 40, 48);
            byte[] key = computeHashR5(password, keySalt, null);
            return AESCipher.decryptWithIV(key, new byte[16], ue);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("R5 user key derivation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Compute the file encryption key for R=5 via the owner password
     * (Adobe Extension Level 3 — single SHA-256, no iteration).
     */
    public static byte[] computeEncryptionKeyR5Owner(byte[] password, PDFEncryptionDict encDict) {
        try {
            byte[] o = encDict.getO();
            byte[] u = encDict.getU();
            byte[] oe = encDict.getOE();
            if (o == null || o.length < 48 || oe == null || oe.length < 32 || u == null) {
                throw new RuntimeException("Missing O/OE/U for R5 owner key derivation");
            }
            byte[] keySalt = Arrays.copyOfRange(o, 40, 48);
            byte[] key = computeHashR5(password, keySalt, Arrays.copyOf(u, Math.min(48, u.length)));
            return AESCipher.decryptWithIV(key, new byte[16], oe);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("R5 owner key derivation failed: " + e.getMessage(), e);
        }
    }

    // ── Write-side: O/U hash generation ──────────────────────────────

    /**
     * Algorithm 3 (ISO 32000-1:2008, §7.6.3.4): computes the /O (owner) hash.
     * <p>
     * The owner password is used to derive a key that encrypts the padded user
     * password. The result is stored as the /O entry in the encryption dictionary.
     * </p>
     *
     * @param ownerPassword owner password bytes (if null/empty, userPassword is used)
     * @param userPassword  user password bytes
     * @param keyLenBytes   key length in bytes (5 for R=2, 16 for R=3/4)
     * @param R             security handler revision (2, 3, or 4)
     * @return 32-byte /O hash
     */
    public static byte[] generateO_R2R4(byte[] ownerPassword, byte[] userPassword,
                                         int keyLenBytes, int R) {
        try {
            // Step a-d: pad the owner password (or user password if owner is empty)
            byte[] effectiveOwnerPw = (ownerPassword == null || ownerPassword.length == 0)
                    ? userPassword : ownerPassword;
            byte[] paddedOwnerPw = padPassword(effectiveOwnerPw);

            // Step e: MD5 hash of padded owner password
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hash = md5.digest(paddedOwnerPw);

            // Step f: for R >= 3, iterate MD5 50 times
            if (R >= 3) {
                for (int i = 0; i < 50; i++) {
                    md5.reset();
                    md5.update(hash, 0, keyLenBytes);
                    hash = md5.digest();
                }
            }

            // Step g: take first keyLenBytes as RC4 key
            byte[] ownerKey = Arrays.copyOf(hash, keyLenBytes);

            // Step h: pad the user password
            byte[] paddedUserPw = padPassword(userPassword);

            // Step i: RC4-encrypt padded user password
            byte[] result = RC4Cipher.process(ownerKey, paddedUserPw);

            // Step j: for R >= 3, iterate RC4 19 times with XOR-modified keys
            if (R >= 3) {
                for (int i = 1; i <= 19; i++) {
                    byte[] iterKey = new byte[ownerKey.length];
                    for (int j = 0; j < ownerKey.length; j++) {
                        iterKey[j] = (byte) (ownerKey[j] ^ i);
                    }
                    result = RC4Cipher.process(iterKey, result);
                }
            }

            return result; // 32 bytes
        } catch (Exception e) {
            throw new RuntimeException("O hash generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Algorithm 4 (ISO 32000-1:2008, §7.6.3.4): computes the /U hash for R=2.
     * <p>
     * RC4-encrypts the 32-byte PADDING constant with the file encryption key.
     * </p>
     *
     * @param encryptionKey the file encryption key (5 bytes for R=2)
     * @return 32-byte /U hash
     */
    public static byte[] generateU_R2(byte[] encryptionKey) {
        return RC4Cipher.process(encryptionKey, PADDING.clone());
    }

    /**
     * Algorithm 5 (ISO 32000-1:2008, §7.6.3.4): computes the /U hash for R=3/4.
     * <p>
     * MD5 hashes the PADDING + document ID, then RC4-encrypts with the encryption
     * key, iterating 19 additional times with XOR-modified keys. The result is
     * 16 meaningful bytes padded to 32 bytes.
     * </p>
     *
     * @param encryptionKey the file encryption key (16 bytes for R=3/4)
     * @param documentId    the first element of the /ID array from trailer
     * @return 32-byte /U hash (first 16 bytes significant, last 16 arbitrary)
     */
    public static byte[] generateU_R3R4(byte[] encryptionKey, byte[] documentId) {
        try {
            // Step a: MD5(PADDING + documentId)
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(PADDING);
            if (documentId != null) {
                md5.update(documentId);
            }
            byte[] hash = md5.digest(); // 16 bytes

            // Step b: RC4-encrypt with encryption key
            byte[] result = RC4Cipher.process(encryptionKey, hash);

            // Step c: iterate RC4 19 times with XOR-modified keys
            for (int i = 1; i <= 19; i++) {
                byte[] iterKey = new byte[encryptionKey.length];
                for (int j = 0; j < encryptionKey.length; j++) {
                    iterKey[j] = (byte) (encryptionKey[j] ^ i);
                }
                result = RC4Cipher.process(iterKey, result);
            }

            // Step d: pad to 32 bytes (last 16 bytes are arbitrary)
            byte[] u = new byte[32];
            System.arraycopy(result, 0, u, 0, Math.min(result.length, 16));
            // Remaining 16 bytes stay as zeros (arbitrary per spec)
            return u;
        } catch (Exception e) {
            throw new RuntimeException("U hash generation (R3/R4) failed: " + e.getMessage(), e);
        }
    }

    // ── Shared utilities ─────────────────────────────────────────────

    /**
     * Pads or truncates a password to exactly 32 bytes.
     */
    public static byte[] padPassword(byte[] password) {
        byte[] result = new byte[32];
        if (password == null || password.length == 0) {
            System.arraycopy(PADDING, 0, result, 0, 32);
        } else {
            int len = Math.min(password.length, 32);
            System.arraycopy(password, 0, result, 0, len);
            if (len < 32) {
                System.arraycopy(PADDING, 0, result, len, 32 - len);
            }
        }
        return result;
    }

    private static byte[] truncatePassword(byte[] password) {
        if (password == null) return new byte[0];
        if (password.length <= 127) return password;
        return Arrays.copyOf(password, 127);
    }

    /**
     * Computes the R=6 validation or key-derivation hash (ISO 32000-2, Algorithm 2.B).
     * Returns the 32-byte file key material used for /UE and /OE decryption.
     *
     * @param password the password bytes
     * @param salt     the validation or key salt
     * @param userKey  the first 48 bytes of /U for owner-password flows, otherwise null
     * @return the 32-byte R=6 hash result
     */
    public static byte[] computeHashR6(byte[] password, byte[] salt, byte[] userKey) {
        try {
            byte[] truncPw = truncatePassword(password);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(truncPw);
            if (salt != null) {
                sha256.update(salt);
            }
            if (userKey != null) {
                sha256.update(userKey);
            }
            byte[] k = sha256.digest();

            int round = 0;
            byte[] e = new byte[] {0};
            while (round < 64 || ((e[e.length - 1] & 0xFF) > (round - 32))) {
                byte[] repeated = repeatInput64(truncPw, k, userKey);
                e = aesCbcNoPaddingEncrypt(Arrays.copyOfRange(k, 0, 16),
                        Arrays.copyOfRange(k, 16, 32), repeated);

                int mod = first16AsUnsignedMod3(e);
                MessageDigest md = MessageDigest.getInstance(mod == 0 ? "SHA-256"
                        : mod == 1 ? "SHA-384" : "SHA-512");
                k = md.digest(e);
                round++;
            }
            return Arrays.copyOf(k, 32);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("R6 hash derivation failed: " + e.getMessage(), e);
        }
    }

    private static byte[] repeatInput64(byte[] password, byte[] k, byte[] userKey) {
        int partLen = password.length + k.length + (userKey != null ? userKey.length : 0);
        byte[] block = new byte[partLen];
        int offset = 0;
        System.arraycopy(password, 0, block, offset, password.length);
        offset += password.length;
        System.arraycopy(k, 0, block, offset, k.length);
        offset += k.length;
        if (userKey != null) {
            System.arraycopy(userKey, 0, block, offset, userKey.length);
        }

        byte[] repeated = new byte[block.length * 64];
        for (int i = 0; i < 64; i++) {
            System.arraycopy(block, 0, repeated, i * block.length, block.length);
        }
        return repeated;
    }

    private static byte[] aesCbcNoPaddingEncrypt(byte[] key, byte[] iv, byte[] plaintext) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("R6 AES round failed: " + e.getMessage(), e);
        }
    }

    private static int first16AsUnsignedMod3(byte[] bytes) {
        int mod = 0;
        for (int i = 0; i < 16 && i < bytes.length; i++) {
            mod = ((mod << 8) + (bytes[i] & 0xFF)) % 3;
        }
        return mod;
    }
}
