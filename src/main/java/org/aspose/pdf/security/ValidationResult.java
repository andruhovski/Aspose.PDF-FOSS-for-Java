package org.aspose.pdf.security;

/// Contains the result of a PDF signature validation operation.
public class ValidationResult {

    private final boolean valid;
    private final String message;

    /// Creates a new validation result.
    ///
    /// @param valid   whether the signature is valid
    /// @param message descriptive message about the validation outcome
    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /// Returns whether the signature is valid.
    ///
    /// @return `true` if the signature passed validation
    public boolean isValid() {
        return valid;
    }

    /// Returns a descriptive message about the validation outcome.
    ///
    /// @return the validation message
    public String getMessage() {
        return message;
    }
}
