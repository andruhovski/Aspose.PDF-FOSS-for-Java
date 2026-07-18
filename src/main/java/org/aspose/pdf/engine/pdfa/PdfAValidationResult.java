package org.aspose.pdf.engine.pdfa;

import org.aspose.pdf.PdfFormat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/// Collects and reports PDF/A validation violations.
///
/// After validation rules have been applied, this result object holds all violations
/// found. It can determine overall compliance (no ERROR-level violations) and write
/// an XML log in a standard format.
///
public final class PdfAValidationResult {

    private static final Logger LOG = Logger.getLogger(PdfAValidationResult.class.getName());

    private final List<PdfAViolation> violations = new ArrayList<>();
    private final PdfFormat targetFormat;

    /// Creates a new validation result for the given target format.
    ///
    /// @param targetFormat the PDF format that was validated against
    /// @throws IllegalArgumentException if targetFormat is `null`
    public PdfAValidationResult(PdfFormat targetFormat) {
        if (targetFormat == null) {
            throw new IllegalArgumentException("targetFormat must not be null");
        }
        this.targetFormat = targetFormat;
    }

    /// Adds a pre-constructed violation to this result.
    ///
    /// @param violation the violation to add
    /// @throws IllegalArgumentException if violation is `null`
    public void addViolation(PdfAViolation violation) {
        if (violation == null) {
            throw new IllegalArgumentException("violation must not be null");
        }
        violations.add(violation);
    }

    /// Convenience method to add an ERROR-level violation.
    ///
    /// @param ruleId     the rule identifier
    /// @param message    the violation message
    /// @param objectPath the PDF object path (may be `null`)
    /// @param clause     the ISO clause reference (may be `null`)
    public void addError(String ruleId, String message, String objectPath, String clause) {
        violations.add(new PdfAViolation(ruleId, PdfAViolation.Severity.ERROR, message, objectPath, clause));
    }

    /// Convenience method to add a WARNING-level violation.
    ///
    /// @param ruleId     the rule identifier
    /// @param message    the violation message
    /// @param objectPath the PDF object path (may be `null`)
    /// @param clause     the ISO clause reference (may be `null`)
    public void addWarning(String ruleId, String message, String objectPath, String clause) {
        violations.add(new PdfAViolation(ruleId, PdfAViolation.Severity.WARNING, message, objectPath, clause));
    }

    /// Returns `true` if the document is compliant (no ERROR-level violations).
    /// Warnings alone do not break compliance.
    ///
    /// @return true if compliant
    public boolean isCompliant() {
        for (PdfAViolation v : violations) {
            if (v.getSeverity() == PdfAViolation.Severity.ERROR) {
                return false;
            }
        }
        return true;
    }

    /// Returns an unmodifiable list of all violations.
    ///
    /// @return the violations
    public List<PdfAViolation> getViolations() {
        return Collections.unmodifiableList(violations);
    }

    /// Returns the target format this result was validated against.
    ///
    /// @return the target format
    public PdfFormat getTargetFormat() {
        return targetFormat;
    }

    /// Writes the validation result as an XML log to the specified file path.
    ///
    /// @param filePath the path to write the XML log to
    /// @throws IOException if an I/O error occurs
    public void writeXmlLog(String filePath) throws IOException {
        if (filePath == null) {
            // A null log path means "no log file requested" — same semantics as
            // convert()/validate() with a null logFileName. Skip silently rather
            // than NPE in new FileOutputStream(null).
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            writeXmlLog(fos);
        }
    }

    /// Writes the validation result as an XML log to the specified output stream.
    ///
    /// The XML format is:
    ///
    /// ```
    /// <?xml version="1.0" encoding="UTF-8"?>
    /// <PdfAValidationLog>
    ///   <TargetFormat>PDF_A_1B</TargetFormat>
    ///   <Timestamp>2024-01-15T10:30:00</Timestamp>
    ///   <Violations>
    ///     <Violation ruleId="6.1.3" severity="ERROR">
    ///       <Message>...</Message>
    ///       <ObjectPath>...</ObjectPath>
    ///       <Clause>...</Clause>
    ///     </Violation>
    ///   </Violations>
    /// </PdfAValidationLog>
    /// ```
    ///
    /// @param os the output stream
    /// @throws IOException if an I/O error occurs
    public void writeXmlLog(OutputStream os) throws IOException {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<PdfAValidationLog>\n");
        xml.append("  <TargetFormat>").append(escapeXml(targetFormat.name())).append("</TargetFormat>\n");
        xml.append("  <Timestamp>")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append("</Timestamp>\n");
        xml.append("  <Violations>\n");
        for (PdfAViolation v : violations) {
            xml.append("    <Violation ruleId=\"").append(escapeXmlAttr(v.getRuleId()))
                    .append("\" severity=\"").append(v.getSeverity().name()).append("\">\n");
            xml.append("      <Message>").append(escapeXml(v.getMessage())).append("</Message>\n");
            if (v.getObjectPath() != null) {
                xml.append("      <ObjectPath>").append(escapeXml(v.getObjectPath())).append("</ObjectPath>\n");
            }
            if (v.getClause() != null) {
                xml.append("      <Clause>").append(escapeXml(v.getClause())).append("</Clause>\n");
            }
            xml.append("    </Violation>\n");
        }
        xml.append("  </Violations>\n");
        xml.append("</PdfAValidationLog>\n");
        os.write(xml.toString().getBytes(StandardCharsets.UTF_8));
    }

    /// Escapes XML special characters in text content.
    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /// Escapes XML special characters in attribute values.
    private static String escapeXmlAttr(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
