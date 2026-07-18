package org.aspose.pdf.drawing;

/// Specifies how bounds checking is performed when shapes are added to a collection.
public enum BoundsCheckMode {

    /// No bounds checking is performed. Shapes may extend beyond the container.
    Default,

    /// A [BoundsOutOfRangeException] is thrown if a shape does not fit
    /// within the container dimensions.
    ThrowExceptionIfDoesNotFit
}
