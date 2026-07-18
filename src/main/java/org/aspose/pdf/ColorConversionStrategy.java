package org.aspose.pdf;

/// Specifies the color conversion strategy to apply when processing PDF content.
///
/// Used by [ColorConverter] to transform color operators in page content streams
/// from one color space to another.
///
public enum ColorConversionStrategy {

    /// No color conversion is performed.
    None,

    /// Convert all colors to DeviceCMYK.
    ConvertToCmyk,

    /// Convert all colors to DeviceRGB.
    ConvertToRgb,

    /// Convert all colors to DeviceGray.
    ConvertToGrayscale
}
