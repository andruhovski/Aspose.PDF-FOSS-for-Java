package org.aspose.pdf.engine.pdfa;

import java.util.logging.Logger;

/**
 * Represents a single violation found during PDF/A validation.
 * <p>
 * Each violation carries a rule identifier, severity, human-readable message,
 * the PDF object path where the violation was detected, and a reference to the
 * relevant ISO clause.
 * </p>
 */
public final class PdfAViolation {

    private static final Logger LOG = Logger.getLogger(PdfAViolation.class.getName());

    /**
     * Severity levels for PDF/A violations.
     */
    public enum Severity {
        /** The violation makes the document non-compliant. */
        ERROR,
        /** The violation is informational and does not break compliance. */
        WARNING
    }

    private final String ruleId;
    private final Severity severity;
    private final String message;
    private final String objectPath;
    private final String clause;

    /**
     * Creates a new violation record.
     *
     * @param ruleId     the rule identifier (e.g. "6.1.3")
     * @param severity   the severity level
     * @param message    a human-readable description of the violation
     * @param objectPath the PDF object path where the violation was found (e.g. "page[0]/resources/font[0]")
     * @param clause     the ISO clause reference (e.g. "ISO 19005-1:2005, 6.1.3")
     * @throws IllegalArgumentException if ruleId, severity, or message is {@code null}
     */
    public PdfAViolation(String ruleId, Severity severity, String message, String objectPath, String clause) {
        if (ruleId == null) {
            throw new IllegalArgumentException("ruleId must not be null");
        }
        if (severity == null) {
            throw new IllegalArgumentException("severity must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        this.ruleId = ruleId;
        this.severity = severity;
        this.message = message;
        this.objectPath = objectPath;
        this.clause = clause;
    }

    /**
     * Returns the rule identifier.
     *
     * @return the rule id
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * Returns the severity of this violation.
     *
     * @return the severity
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Returns a human-readable message describing the violation.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the PDF object path where the violation was detected, or {@code null}.
     *
     * @return the object path
     */
    public String getObjectPath() {
        return objectPath;
    }

    /**
     * Returns the ISO clause reference, or {@code null}.
     *
     * @return the clause
     */
    public String getClause() {
        return clause;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(severity).append("] ").append(ruleId);
        if (clause != null) {
            sb.append(" (").append(clause).append(')');
        }
        sb.append(": ").append(message);
        if (objectPath != null) {
            sb.append(" @ ").append(objectPath);
        }
        return sb.toString();
    }
}
