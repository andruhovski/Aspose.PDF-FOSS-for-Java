# PDF/A — Validation and Conversion

PDF/A is an ISO 19005 family of standards for long-term archival PDFs. The library supports validation and conversion across the major parts:

- **PDF/A-1** (ISO 19005-1, 2005) — based on PDF 1.4
- **PDF/A-2** (ISO 19005-2, 2011) — based on PDF 1.7, adds JPEG2000, transparency, layers
- **PDF/A-3** (ISO 19005-3, 2012) — extends PDF/A-2 to allow arbitrary embedded files
- **PDF/A-4** (ISO 19005-4, 2020) — based on PDF 2.0

Each part has conformance levels (A = accessibility, B = basic, U = Unicode) — supported as enum values in `PdfFormat`:

```
PdfFormat.PDF_A_1A    PdfFormat.PDF_A_2A    PdfFormat.PDF_A_3A    PdfFormat.PDF_A_4
PdfFormat.PDF_A_1B    PdfFormat.PDF_A_2B    PdfFormat.PDF_A_3B    PdfFormat.PDF_A_4E
                      PdfFormat.PDF_A_2U    PdfFormat.PDF_A_3U    PdfFormat.PDF_A_4F
```

## Validate a document against a PDF/A standard

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.PdfFormat;

try (Document doc = new Document("input.pdf")) {
    boolean compliant = doc.validate("validation-log.xml", PdfFormat.PDF_A_2B);
    if (compliant) {
        System.out.println("Document is PDF/A-2B compliant.");
    } else {
        System.out.println("Not compliant. See validation-log.xml for details.");
    }
}
```

The log file is an XML report listing every violation encountered (rule, page, location, severity).

You can also validate to a stream instead of a file:

```java
import java.io.ByteArrayOutputStream;

try (Document doc = new Document("input.pdf")) {
    ByteArrayOutputStream log = new ByteArrayOutputStream();
    boolean ok = doc.validate(log, PdfFormat.PDF_A_2B);
    String xmlReport = log.toString("UTF-8");
}
```

## Convert a document to PDF/A

Conversion attempts to transform the document into a compliant form: embedding fonts, removing prohibited content (transparency in PDF/A-1, encryption in any PDF/A), adding required metadata, and so on.

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.ConvertErrorAction;

try (Document doc = new Document("input.pdf")) {
    boolean ok = doc.convert(
        "conversion-log.xml",
        PdfFormat.PDF_A_2B,
        ConvertErrorAction.Delete);

    doc.save("output-pdfa.pdf");
}
```

`ConvertErrorAction` controls what happens when content cannot be made compliant:

| Action | Meaning |
|---|---|
| `Delete` | Remove the offending content; conversion succeeds |
| `None` | Leave content as-is; conversion may fail validation |

For most use cases, `Delete` is the right choice: it produces a compliant file by stripping anything that can't be made compliant. Inspect the log to see what was removed.

## Using PdfFormatConversionOptions

For more control:

```java
import org.aspose.pdf.PdfFormatConversionOptions;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.ConvertErrorAction;

PdfFormatConversionOptions options = new PdfFormatConversionOptions(
    "log.xml",
    PdfFormat.PDF_A_2B,
    ConvertErrorAction.Delete);

try (Document doc = new Document("input.pdf")) {
    boolean ok = doc.convert(options);
    doc.save("output.pdf");
}
```

## Convenience method for PDF/A-2B

A shortcut exists for the most common conversion target:

```java
try (Document doc = new Document("input.pdf")) {
    doc.convertToPdfA2B("log.xml");
    doc.save("output.pdf");
}
```

## Which conformance level should you pick?

- **PDF/A-1B** — most conservative, widest reader compatibility, no transparency, no JPEG2000, no layers
- **PDF/A-2B** — modern baseline, supports transparency, JPEG2000, OpenType fonts
- **PDF/A-3B** — same as 2B but allows arbitrary file attachments (e.g. ZUGFeRD invoices)
- **PDF/A-4** — newest, based on PDF 2.0; pick this if you control both sides

For accessibility-required documents (government, healthcare, regulated industries), pick the **A** conformance level (`PDF_A_1A`, `PDF_A_2A`, `PDF_A_3A`) which additionally requires tagged PDF structure.

For most archival purposes, **PDF/A-2B** is a good default.

## What conversion changes

Typical transformations performed during conversion:

- **Fonts** — non-embedded fonts are embedded or substituted; subsetting is applied where possible
- **Colour** — output intent (ICC profile) added if missing; device-dependent colours are converted to device-independent
- **Transparency** — in PDF/A-1, transparent objects are flattened or removed (depends on `ConvertErrorAction`)
- **Encryption** — removed (PDF/A files must not be encrypted)
- **External links and actions** — script-execution actions removed; URI links are preserved
- **JavaScript** — removed (not allowed in PDF/A)
- **Audio/video annotations** — removed (not allowed in PDF/A)
- **Metadata** — XMP metadata is required; missing metadata is added with sensible defaults

The conversion log details every change.

## Verifying after conversion

Always validate the output to make sure conversion produced a compliant file:

```java
try (Document doc = new Document("output.pdf")) {
    boolean ok = doc.validate("validation-log.xml", PdfFormat.PDF_A_2B);
    if (!ok) {
        // conversion left some violations — inspect the log
    }
}
```

## Known limitations

- **Some advanced PDF features** are out of scope and will be flagged during conversion (3D annotations, multimedia, certain transition effects).
- **Validation rule coverage** is being expanded across releases. Some less-common veraPDF rules may not yet be implemented; if you find a missed violation, please report it.
- **PDF/X (print production)** is out of scope for the FOSS edition — the commercial Aspose.PDF supports it.
- **PDF/UA (accessibility)** baseline support exists; complete tag tree validation is in progress.
