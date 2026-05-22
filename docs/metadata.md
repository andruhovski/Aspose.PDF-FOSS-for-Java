# Document Metadata

PDFs carry two kinds of metadata: the legacy **Info dictionary** (PDF 1.0+) and the modern **XMP** stream (PDF 1.4+). Most modern documents include both; the library exposes both.

## DocumentInfo: the legacy Info dictionary

`DocumentInfo` is the simpler, key-value API for the standard fields:

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.DocumentInfo;

try (Document doc = new Document("input.pdf")) {
    DocumentInfo info = doc.getInfo();
    System.out.println("Title:    " + info.getTitle());
    System.out.println("Author:   " + info.getAuthor());
    System.out.println("Subject:  " + info.getSubject());
    System.out.println("Keywords: " + info.getKeywords());
    System.out.println("Creator:  " + info.getCreator());
    System.out.println("Producer: " + info.getProducer());
    System.out.println("Created:  " + info.getCreationDate());
}
```

### Setting Info fields

```java
try (Document doc = new Document("input.pdf")) {
    DocumentInfo info = doc.getInfo();
    info.setTitle("Quarterly Report Q1 2026");
    info.setAuthor("Jane Doe");
    info.setSubject("Financial summary");
    info.setKeywords("finance, quarterly, 2026");

    doc.save("output.pdf");
}
```

The standard Info fields are:

| Field | Setter | Notes |
|---|---|---|
| Title | `setTitle(String)` | Document title |
| Author | `setAuthor(String)` | Author name |
| Subject | `setSubject(String)` | Document subject |
| Keywords | `setKeywords(String)` | Comma-separated list, typically |
| Creator | `setCreator(String)` | Application that originally created the source |
| Producer | `setProducer(String)` | Application that produced the PDF |
| CreationDate | `setCreationDate(Date)` | When the document was created |
| ModificationDate | `setModDate(Date)` | Last modified |

You can also store custom Info fields beyond the standard ones — consult the JavaDoc for `DocumentInfo` for the current API.

## XMP metadata

XMP (Extensible Metadata Platform) is the richer, XML-based metadata format. PDFs use XMP per ISO 16684-1. The library exposes XMP via `Document.getMetadata()`:

```java
import org.aspose.pdf.XmpMetadata;
import org.aspose.pdf.XmpValue;

try (Document doc = new Document("input.pdf")) {
    XmpMetadata xmp = doc.getMetadata();
    for (Map.Entry<String, XmpValue> entry : xmp) {
        System.out.println(entry.getKey() + " = " + entry.getValue());
    }
}
```

XMP properties are namespaced; common namespaces include:

- `dc:` — Dublin Core (title, creator, description, ...)
- `pdf:` — PDF-specific (producer, keywords, ...)
- `xmp:` — XMP core (create date, modify date, ...)
- `pdfaid:` — PDF/A identification (part, conformance)
- `xmpMM:` — Media management

## Reading raw XMP XML

To work with the XMP XML directly (e.g. to use a third-party XMP library on the data):

```java
import java.io.ByteArrayOutputStream;

try (Document doc = new Document("input.pdf")) {
    ByteArrayOutputStream xml = new ByteArrayOutputStream();
    doc.getXmpMetadata(xml);
    String xmpXml = xml.toString("UTF-8");
    // ... parse with your XML tool of choice ...
}
```

## Writing raw XMP XML

```java
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

String xmpXml =
    "<?xpacket begin=\"...\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n"
  + "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n"
  + "  ...\n"
  + "</x:xmpmeta>\n"
  + "<?xpacket end=\"w\"?>\n";

try (Document doc = new Document("input.pdf")) {
    doc.setXmpMetadata(new ByteArrayInputStream(
        xmpXml.getBytes(StandardCharsets.UTF_8)));
    doc.save("output.pdf");
}
```

The XML must be a valid XMP packet (with `<?xpacket ...?>` PI's). Malformed XMP may produce an exception or be partially accepted.

## Synchronisation between Info and XMP

PDF spec recommends that fields appearing in both Info and XMP should match. Most viewers display the XMP values when both are present; some legacy tools rely on Info. Best practice for new documents:

1. Set Info via `DocumentInfo` (covers basic compatibility).
2. Set or update XMP via `setXmpMetadata` or property-level XMP accessors if your library version exposes them.

For documents converted to PDF/A, the conversion step also normalises metadata: XMP becomes required, and missing required properties are added.

## Common patterns

### Strip all metadata

```java
try (Document doc = new Document("input.pdf")) {
    DocumentInfo info = doc.getInfo();
    info.setTitle("");
    info.setAuthor("");
    info.setSubject("");
    info.setKeywords("");
    info.setCreator("");
    // Producer is set by the library on save; can be overwritten

    // For XMP — write an empty/minimal packet
    doc.save("stripped.pdf");
}
```

For true privacy-sensitive stripping (production / forensic use), also examine and clear: page-level metadata streams, embedded file metadata, XFA data, JavaScript with names/emails, signature blocks, and document-level navigation aids.

### Set a canonical creator tag

For batch-processed documents you may want a consistent producer/creator string identifying your service:

```java
try (Document doc = new Document(inputPath)) {
    DocumentInfo info = doc.getInfo();
    info.setCreator("MyApp v1.2.3");
    doc.save(outputPath);
}
```

`Producer` is set by the library itself on save, identifying Aspose.PDF FOSS. You can overwrite it if you have a specific labelling requirement.

## Limitations

- **XMP schemas**: well-known namespaces (Dublin Core, PDF, XMP core, PDF/A ID) are recognised. Custom schemas can be read and written as raw XML; structured access for arbitrary schemas may not be available via the typed `XmpMetadata` API.
- **Date precision**: PDF dates store seconds-level precision with a timezone offset; some Aspose.PDF features may round to seconds.
