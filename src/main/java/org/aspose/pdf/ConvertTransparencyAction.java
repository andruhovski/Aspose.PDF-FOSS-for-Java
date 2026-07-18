package org.aspose.pdf;

/// Specifies the action to take when transparency is encountered during PDF/A conversion.
///
/// PDF/A-1 forbids transparency; PDF/A-2 and later allow it.
///
public enum ConvertTransparencyAction {

    /// Use the default behavior for the target format: flatten transparency
    /// where the target standard forbids it.
    Default,

    /// Replace transparent objects with an opaque mask approximation.
    Mask
}
