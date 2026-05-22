# Limitations and Out-of-Scope Features

Aspose.PDF FOSS for Java is an early-alpha open-source implementation focused on the core PDF specification (ISO 32000-1:2008). This page lists known limitations and features that are explicitly out of scope.

This is not a complete API gap report — that level of detail belongs in JavaDoc and individual feature pages. This is a high-level orientation for users evaluating whether the library covers their use case.

## Out of scope (will not be implemented)

These features exist in the commercial Aspose.PDF for Java but are out of scope for the FOSS edition:

- **OCR** — recognition of text in scanned images. Use a dedicated OCR library (Tesseract, etc.) for this.
- **Conversion to non-PDF formats** — DOCX, DOC, XLSX, PPTX, EPUB, MOBI, HTML, Markdown, LaTeX, ZUGFeRD, and similar. The commercial product handles these; the FOSS edition is PDF in / PDF out.
- **Conversion from non-PDF formats** — DOC, DOCX, XLSX, PPTX, HTML, XPS, PCL, PostScript, EPS, SVG as input. (HTML→PDF has a constructor stub but is not feature-complete.)
- **XFA forms** — full Adobe XFA form rendering and editing. The library exposes XFA data as XML but does not render or edit the dynamic XFA layer.
- **3D annotations and PRC/U3D streams** — out of scope.
- **PDF/X** (print production family) — out of scope; PDF/A is supported.

If your use case requires any of these, [Aspose.PDF for Java](https://products.aspose.com/pdf/java/) is the commercial product to look at.

## In scope but partially implemented

These features are present and working for common cases, but rough edges exist:

- **Text replacement** with non-Latin scripts (CJK, Arabic, Hebrew) — basic cases work; complex bidi reordering and shaping edge cases may differ from the reference renderer.
- **Optimisation / size reduction** — basic stream re-compression works; advanced strategies (image downsampling profiles, font merging across resources) are limited.
- **Tagged PDF / logical structure** — readable; programmatic construction of well-formed structure trees has partial coverage.
- **PDF/UA accessibility** — baseline support; full tag tree validation in progress.
- **Long-term validation (LTV) for signatures** — partial DSS dictionary support, timestamps in progress.
- **High-fidelity rasterization options** — many of the commercial product's `RenderingOptions` knobs (font hinting modes, OCG-aware rendering, custom scale modes) are not yet implemented.
- **Some Aspose-style facades** (`PdfFileEditor`, `PdfFileStamp`, `PdfConverter`, `PdfAnnotationEditor`) — covered for common operations; some less-common overloads not present.

## Performance considerations

- **Very large PDFs** (thousands of pages, or single pages with very dense content) may consume more memory than the commercial product. The library prefers correctness over micro-optimisation at this stage.
- **Concurrent use of a single `Document` instance** is not safe. Each `Document` should be used by one thread at a time. For parallel processing, open separate `Document` instances (one per thread).
- **Reading is generally faster than writing.** Save operations rebuild the cross-reference table and stream data; for high-throughput pipelines, batch operations where possible rather than save-after-every-change.

## Compatibility expectations

- **PDFs produced by this library are readable** by Adobe Acrobat, Foxit, Sumatra, Apple Preview, Chrome's PDF viewer, and other major readers. Roundtrip testing is part of the test suite.
- **PDFs produced by other tools** should generally be readable. The library is forgiving toward minor spec violations (incremental update edge cases, slight cross-reference irregularities) but is strict about structural integrity (it will reject files that cannot be safely processed).
- **Encrypted PDFs** are supported up to and including AES-256. Reading a file with public-key encryption requires the `ICustomSecurityHandler` interface; an out-of-the-box helper for the common cases is not provided yet.

## Reporting gaps and bugs

If you find a feature you need that is "in scope but partially implemented" with a missing case, please [open a GitHub Issue](https://github.com/aspose-pdf-foss/Aspose.PDF-FOSS-for-Java/issues) with:

1. A short description of the use case
2. A minimal reproducible example (Java + sample PDF if applicable)
3. The expected output (or a reference to how the commercial Aspose.PDF for Java handles it)

For features that are "out of scope" in this list, please don't open feature requests — those will be closed and a pointer to the commercial product provided.
