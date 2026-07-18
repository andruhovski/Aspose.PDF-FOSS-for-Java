package org.aspose.pdf.security;

/// Specifies the method used for signature validation.
public enum ValidationMethod {

    /// Automatically determine the validation method based on the signature type.
    Auto,

    /// Validate the digital signature only.
    Signature,

    /// Validate the timestamp only.
    Timestamp,

    /// Validate both signature and timestamp.
    Both
}
