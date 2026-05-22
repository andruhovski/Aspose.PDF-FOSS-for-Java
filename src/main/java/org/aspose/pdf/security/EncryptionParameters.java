package org.aspose.pdf.security;

/**
 * Carries the low-level values used by a custom security handler.
 */
public class EncryptionParameters {

    private final byte[] ownerKey;
    private final byte[] userKey;
    private final int permissions;
    private final int version;
    private final int revision;
    private final int keyLength;

    /**
     * Creates a new parameter snapshot for a custom security handler.
     *
     * @param ownerKey    raw owner key bytes from the encryption dictionary
     * @param userKey     raw user key bytes from the encryption dictionary
     * @param permissions permission flags
     * @param version     encryption dictionary V value
     * @param revision    encryption dictionary R value
     * @param keyLength   logical key length value from the handler/dictionary
     */
    public EncryptionParameters(byte[] ownerKey, byte[] userKey, int permissions,
                                int version, int revision, int keyLength) {
        this.ownerKey = ownerKey != null ? ownerKey.clone() : new byte[0];
        this.userKey = userKey != null ? userKey.clone() : new byte[0];
        this.permissions = permissions;
        this.version = version;
        this.revision = revision;
        this.keyLength = keyLength;
    }

    /**
     * Returns owner key bytes.
     *
     * @return owner key bytes
     */
    public byte[] getOwnerKey() {
        return ownerKey.clone();
    }

    /**
     * Returns user key bytes.
     *
     * @return user key bytes
     */
    public byte[] getUserKey() {
        return userKey.clone();
    }

    /**
     * Returns permission flags.
     *
     * @return permissions
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * Returns encryption version value.
     *
     * @return V value
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns encryption revision value.
     *
     * @return R value
     */
    public int getRevision() {
        return revision;
    }

    /**
     * Returns declared key length value.
     *
     * @return key length
     */
    public int getKeyLength() {
        return keyLength;
    }
}
