package org.aspose.pdf.security;

/// Options for controlling PDF signature validation behavior.
public class ValidationOptions {

    private ValidationMode validationMode = ValidationMode.OnlineAll;
    private ValidationMethod validationMethod = ValidationMethod.Auto;

    /// Creates a new `ValidationOptions` with default settings.
    public ValidationOptions() {
    }

    /// Returns the validation mode.
    ///
    /// @return the validation mode
    public ValidationMode getValidationMode() {
        return validationMode;
    }

    /// Sets the validation mode.
    ///
    /// @param validationMode the validation mode
    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    /// Returns the validation method.
    ///
    /// @return the validation method
    public ValidationMethod getValidationMethod() {
        return validationMethod;
    }

    /// Sets the validation method.
    ///
    /// @param validationMethod the validation method
    public void setValidationMethod(ValidationMethod validationMethod) {
        this.validationMethod = validationMethod;
    }
}
