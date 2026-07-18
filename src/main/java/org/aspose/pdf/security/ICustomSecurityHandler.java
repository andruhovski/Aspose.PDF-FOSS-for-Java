package org.aspose.pdf.security;

/// Pluggable custom security handler compatible with Aspose-style document
/// encryption extension points.
public interface ICustomSecurityHandler {

    /// Returns the handler filter name written to the encryption dictionary.
    ///
    /// @return filter name
    String getFilter();

    /// Returns the handler sub-filter name, or `null` if none is needed.
    ///
    /// @return sub-filter name or `null`
    String getSubFilter();

    /// Returns the encryption dictionary version value.
    ///
    /// @return V value
    int getVersion();

    /// Returns the handler revision value.
    ///
    /// @return R value
    int getRevision();

    /// Returns the handler-specific key length value.
    ///
    /// @return key length
    int getKeyLength();

    /// Initializes the handler with encryption dictionary parameters.
    ///
    /// @param parameters current encryption parameters
    void initialize(EncryptionParameters parameters);

    /// Calculates the file encryption key for the supplied password.
    ///
    /// @param password user or owner password
    /// @return encryption key bytes
    byte[] calculateEncryptionKey(String password);

    /// Encrypts object data.
    ///
    /// @param data         plaintext bytes
    /// @param objectNumber object number
    /// @param generation   generation number
    /// @param key          file encryption key
    /// @return encrypted bytes
    byte[] encrypt(byte[] data, int objectNumber, int generation, byte[] key);

    /// Decrypts object data.
    ///
    /// @param data         ciphertext bytes
    /// @param objectNumber object number
    /// @param generation   generation number
    /// @param key          file encryption key
    /// @return decrypted bytes
    byte[] decrypt(byte[] data, int objectNumber, int generation, byte[] key);

    /// Returns whether the supplied password acts as owner password.
    ///
    /// @param password password to check
    /// @return true for owner password
    boolean isOwnerPassword(String password);

    /// Returns whether the supplied password acts as user password.
    ///
    /// @param password password to check
    /// @return true for user password
    boolean isUserPassword(String password);

    /// Builds raw owner key bytes for the encryption dictionary.
    ///
    /// @param userPassword  user password
    /// @param ownerPassword owner password
    /// @return owner key bytes
    byte[] getOwnerKey(String userPassword, String ownerPassword);

    /// Builds raw user key bytes for the encryption dictionary.
    ///
    /// @param userPassword user password
    /// @return user key bytes
    byte[] getUserKey(String userPassword);

    /// Optionally encodes permission bytes for the handler.
    ///
    /// @param permissionsInt permission flags
    /// @return encoded permission bytes, or `null` when not used
    default byte[] encryptPermissions(int permissionsInt) {
        return null;
    }
}
