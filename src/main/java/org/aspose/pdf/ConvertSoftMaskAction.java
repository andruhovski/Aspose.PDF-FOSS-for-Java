package org.aspose.pdf;

/// Specifies the action to take when a PDF/A conversion encounters soft masks.
public enum ConvertSoftMaskAction {

    /// Default handling — preserve soft masks where allowed by the target format.
    Default,

    /// Convert soft masks to stencil masks for PDF/A compliance.
    ConvertToStencilMask
}
