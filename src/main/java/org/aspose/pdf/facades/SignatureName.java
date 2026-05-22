package org.aspose.pdf.facades;

/**
 * Represents a signature field name returned by {@link PdfFileSignature#getSignatureNames(boolean)}.
 * Provides the full name and whether the field contains an actual signature.
 */
public class SignatureName {

    private final String fullName;
    private final boolean hasSignature;

    /**
     * Creates a new signature name descriptor.
     *
     * @param fullName     the fully-qualified field name
     * @param hasSignature whether the field is signed
     */
    public SignatureName(String fullName, boolean hasSignature) {
        this.fullName = fullName;
        this.hasSignature = hasSignature;
    }

    /**
     * Returns the fully-qualified name of the signature field.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns whether this signature field contains an actual signature.
     *
     * @return {@code true} if the field is signed
     */
    public boolean getHasSignature() {
        return hasSignature;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
