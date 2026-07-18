package org.aspose.pdf;

/// Enumerates the cryptographic algorithms available for PDF encryption
/// (ISO 32000-1:2008, Section 7.6).
public enum CryptoAlgorithm {

    /// RC4 encryption with a 40-bit key (V=1, R=2).
    RC4x40,

    /// RC4 encryption with a 128-bit key (V=2, R=3).
    RC4x128,

    /// AES encryption with a 128-bit key (V=4, R=4).
    AESx128,

    /// AES encryption with a 256-bit key (V=5, R=6).
    AESx256
}
