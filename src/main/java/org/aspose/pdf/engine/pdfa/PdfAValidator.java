package org.aspose.pdf.engine.pdfa;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfa.rules.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/// PDF/A and PDF/X validation orchestrator.
///
/// Creates the full list of validation rule instances and runs each one
/// against a parsed PDF document. The result object collects all violations
/// found across all rules.
///
/// Usage:
///
/// ```
/// PdfAValidator validator = new PdfAValidator();
/// PdfAValidationResult result = validator.validate(parser, PdfFormat.PDF_A_1B);
/// if (result.isCompliant()) {
///     // Document conforms to PDF/A-1b
/// }
/// ```
public final class PdfAValidator {

    private static final Logger LOG = Logger.getLogger(PdfAValidator.class.getName());

    private final List<PdfARule> rules;

    /// Creates a validator with the default set of all built-in rules.
    public PdfAValidator() {
        List<PdfARule> allRules = new ArrayList<>();
        allRules.add(new FileStructureRules());
        allRules.add(new GraphicsRules());
        allRules.add(new FontRules());
        allRules.add(new TransparencyRules());
        allRules.add(new AnnotationRules());
        allRules.add(new ActionRules());
        allRules.add(new MetadataRules());
        allRules.add(new LogicalStructureRules());
        allRules.add(new InteractiveFormRules());
        allRules.add(new PdfXRules());
        this.rules = Collections.unmodifiableList(allRules);
    }

    /// Creates a validator with a custom set of rules.
    ///
    /// @param rules the rules to use for validation
    /// @throws IllegalArgumentException if rules is `null`
    public PdfAValidator(List<PdfARule> rules) {
        if (rules == null) {
            throw new IllegalArgumentException("rules must not be null");
        }
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
    }

    /// Validates the parsed PDF document against the specified format.
    ///
    /// Runs every registered rule in order and collects violations into a
    /// single [PdfAValidationResult]. If a rule throws an unexpected
    /// exception, it is caught and logged as a warning; validation continues
    /// with the remaining rules.
    ///
    /// @param parser the PDF parser providing access to the document structure
    /// @param format the target PDF format to validate against
    /// @return the validation result containing all violations found
    /// @throws IllegalArgumentException if parser or format is `null`
    public PdfAValidationResult validate(PDFParser parser, PdfFormat format) {
        if (parser == null) {
            throw new IllegalArgumentException("parser must not be null");
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null");
        }

        PdfAValidationResult result = new PdfAValidationResult(format);
        LOG.log(Level.FINE, "Starting PDF/A validation for format: {0}", format);

        for (PdfARule rule : rules) {
            String ruleName = rule.getClass().getSimpleName();
            try {
                LOG.log(Level.FINE, "Running rule: {0}", ruleName);
                rule.validate(parser, format, result);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Rule " + ruleName + " threw an exception", e);
                result.addWarning("INTERNAL",
                        "Rule " + ruleName + " failed with exception: " + e.getMessage(),
                        null, null);
            }
        }

        LOG.log(Level.FINE, "Validation complete. Compliant: {0}, violations: {1}",
                new Object[]{result.isCompliant(), result.getViolations().size()});
        return result;
    }

    /// Returns the list of rules registered in this validator.
    ///
    /// @return unmodifiable list of rules
    public List<PdfARule> getRules() {
        return rules;
    }
}
