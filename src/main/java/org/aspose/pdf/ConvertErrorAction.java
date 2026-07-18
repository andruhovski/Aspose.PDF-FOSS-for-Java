package org.aspose.pdf;

/// Specifies the action to take when a PDF/A conversion encounters non-compliant elements.
public enum ConvertErrorAction {

    /// Delete the non-compliant element from the document.
    Delete,

    /// Delete the non-compliant element from the document.
    ///
    /// Misspelled alias kept for API compatibility: the Aspose.PDF for .NET
    /// enum ships this literal (`ConvertErrorAction.Delcete`) and
    /// customer code references it. Treated identically to [#Delete].
    ///
    Delcete,

    /// Leave the non-compliant element unchanged (log only).
    None;

    /// Returns whether this action requests deletion of non-compliant
    /// elements (covers both [#Delete] and its compatibility alias
    /// [#Delcete]).
    ///
    /// @return `true` for the delete actions
    public boolean isDelete() {
        return this == Delete || this == Delcete;
    }
}
