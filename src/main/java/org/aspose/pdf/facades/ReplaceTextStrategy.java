package org.aspose.pdf.facades;

/// Controls how [PdfContentEditor#replaceText(String, String)] performs replacements.
///
/// This is a small compatibility facade matching the Aspose.PDF API surface used by
/// legacy regression tests. The current implementation supports:
///
///   - plain-text vs regular-expression search
///   - replace first vs replace all scope
public class ReplaceTextStrategy {

    /// Replacement scope.
    public enum Scope {
        /// Replace only the first match.
        REPLACE_FIRST,
        /// Replace all matches.
        REPLACE_ALL
    }

    private boolean regularExpressionUsed;
    private Scope replaceScope = Scope.REPLACE_FIRST;

    /// Returns whether regular-expression matching is enabled.
    ///
    /// @return `true` when the search text should be interpreted as a regex
    public boolean isRegularExpressionUsed() {
        return regularExpressionUsed;
    }

    /// Enables or disables regular-expression matching.
    ///
    /// @param regularExpressionUsed`true` to interpret search text as a regex
    public void setRegularExpressionUsed(boolean regularExpressionUsed) {
        this.regularExpressionUsed = regularExpressionUsed;
    }

    /// Returns the replacement scope.
    ///
    /// @return current scope
    public Scope getReplaceScope() {
        return replaceScope;
    }

    /// Sets the replacement scope.
    ///
    /// @param replaceScope desired scope; ignored when `null`
    public void setReplaceScope(Scope replaceScope) {
        if (replaceScope != null) {
            this.replaceScope = replaceScope;
        }
    }
}
