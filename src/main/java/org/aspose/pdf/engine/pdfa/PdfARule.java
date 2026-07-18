package org.aspose.pdf.engine.pdfa;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;

/// A single PDF/A validation rule.
///
/// Implementations check one specific aspect of the document (e.g. font embedding,
/// color space usage, metadata presence) and report violations to the result collector.
///
public interface PdfARule {

    /// Validates the document against this rule.
    ///
    /// Implementations should inspect the parsed PDF structure via the parser and
    /// add any violations found to the result object. The target format is provided
    /// so that rules can adjust their checks for different PDF/A parts and conformance
    /// levels.
    ///
    /// @param parser the PDF parser providing access to the document structure
    /// @param format the target PDF format being validated against
    /// @param result the result collector to which violations should be added
    void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result);
}
