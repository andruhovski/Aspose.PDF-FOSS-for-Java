# Working with Annotations

Annotations are interactive elements layered on top of PDF page content: comments, highlights, links, stamps, attachments, and so on. This guide covers the main operations.

## Reading annotations from a page

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.annotations.Annotation;
import org.aspose.pdf.annotations.AnnotationCollection;

try (Document doc = new Document("input.pdf")) {
    for (int i = 1; i <= doc.getPages().getCount(); i++) {
        Page page = doc.getPages().get(i);
        AnnotationCollection annots = page.getAnnotations();
        System.out.printf("Page %d has %d annotations%n", i, annots.getCount());
        for (Annotation a : annots) {
            System.out.printf("  %s at %s%n",
                a.getSubtype(),
                a.getRect());
        }
    }
}
```

`AnnotationCollection` implements `Iterable<Annotation>`, so a for-each loop works.

## Annotation types

The library provides typed subclasses for the common annotation types. Cast `Annotation` to the specific type when you need type-specific properties:

| Subclass | PDF subtype | Purpose |
|---|---|---|
| `TextAnnotation` | Text | Sticky note |
| `FreeTextAnnotation` | FreeText | Free-floating text without a popup |
| `LinkAnnotation` | Link | Clickable hyperlink |
| `HighlightAnnotation` | Highlight | Yellow highlight on text |
| `UnderlineAnnotation` | Underline | Underline markup |
| `StrikeOutAnnotation` | StrikeOut | Strikethrough markup |
| `SquigglyAnnotation` | Squiggly | Wavy underline |
| `LineAnnotation` | Line | Line shape |
| `SquareAnnotation` | Square | Rectangle shape |
| `CircleAnnotation` | Circle | Ellipse shape |
| `PolygonAnnotation`, `PolylineAnnotation` | Polygon, PolyLine | Vector shapes |
| `InkAnnotation` | Ink | Freehand drawing |
| `FileAttachmentAnnotation` | FileAttachment | Embedded file |
| `StampAnnotation` | Stamp | Rubber-stamp imagery |
| `CaretAnnotation` | Caret | Insertion point marker |
| `RedactionAnnotation` | Redact | Redaction region |
| `PopupAnnotation` | Popup | Popup window |
| `ScreenAnnotation` | Screen | Embedded media |
| `WidgetAnnotation` | Widget | Form field widget |

`GenericAnnotation` is used for types not yet specialised in the library.

## Adding a highlight

```java
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.annotations.HighlightAnnotation;

try (Document doc = new Document("input.pdf")) {
    Page page = doc.getPages().get(1);

    // Coordinates are in PDF user-space points, origin at lower-left
    Rectangle rect = new Rectangle(100, 700, 400, 720);
    HighlightAnnotation highlight = new HighlightAnnotation(page, rect);

    page.getAnnotations().add(highlight);
    doc.save("highlighted.pdf");
}
```

## Adding a free-text note

```java
import org.aspose.pdf.annotations.FreeTextAnnotation;
import org.aspose.pdf.annotations.DefaultAppearance;

try (Document doc = new Document("input.pdf")) {
    Page page = doc.getPages().get(1);
    Rectangle rect = new Rectangle(100, 600, 300, 650);

    FreeTextAnnotation note = new FreeTextAnnotation(
        page, rect, new DefaultAppearance(/* font, size, color */));
    note.setContents("Please review this section.");

    page.getAnnotations().add(note);
    doc.save("annotated.pdf");
}
```

(`DefaultAppearance` arguments depend on your build — see JavaDoc for current constructor signatures.)

## Adding a link to a URL

```java
import org.aspose.pdf.annotations.LinkAnnotation;

try (Document doc = new Document("input.pdf")) {
    Page page = doc.getPages().get(1);
    Rectangle rect = new Rectangle(100, 500, 400, 520);

    LinkAnnotation link = new LinkAnnotation(page, rect);
    // ... configure action: GoToURIAction or similar
    page.getAnnotations().add(link);
    doc.save("linked.pdf");
}
```

For the exact API to set the link target (URI vs. internal page destination), consult the JavaDoc — it depends on the version.

## Reading annotation content

For markup annotations with text content (highlight, free text, sticky note, etc.):

```java
for (Annotation a : page.getAnnotations()) {
    System.out.println(a.getSubtype() + ": " + a.getContents());
}
```

`getContents()` returns the user-visible text (the popup content for highlights, the typed text for free-text annotations, etc.).

For author information:

```java
import org.aspose.pdf.annotations.MarkupAnnotation;

for (Annotation a : page.getAnnotations()) {
    if (a instanceof MarkupAnnotation) {
        MarkupAnnotation m = (MarkupAnnotation) a;
        // getAuthor(), getSubject(), getCreationDate() (where applicable)
    }
}
```

## Removing annotations

```java
AnnotationCollection annots = page.getAnnotations();

// Remove by index
annots.delete(0);

// Remove by reference
for (Annotation a : annots) {
    if ("...".equals(a.getContents())) {
        annots.delete(a);
        break;
    }
}
```

After modifications, save the document for the changes to persist.

## Common patterns

### Strip all annotations from a document

```java
try (Document doc = new Document("input.pdf")) {
    for (Page page : doc.getPages()) {
        AnnotationCollection annots = page.getAnnotations();
        while (annots.getCount() > 0) {
            annots.delete(0);
        }
    }
    doc.save("no-annotations.pdf");
}
```

### Count annotations by type

```java
import java.util.HashMap;
import java.util.Map;

try (Document doc = new Document("input.pdf")) {
    Map<String, Integer> counts = new HashMap<>();
    for (Page page : doc.getPages()) {
        for (Annotation a : page.getAnnotations()) {
            String key = a.getSubtype();
            counts.merge(key, 1, Integer::sum);
        }
    }
    counts.forEach((type, count) -> System.out.println(type + ": " + count));
}
```

### Extract all attached files

```java
import org.aspose.pdf.annotations.FileAttachmentAnnotation;
import java.io.FileOutputStream;

try (Document doc = new Document("input.pdf")) {
    int idx = 0;
    for (Page page : doc.getPages()) {
        for (Annotation a : page.getAnnotations()) {
            if (a instanceof FileAttachmentAnnotation) {
                FileAttachmentAnnotation att = (FileAttachmentAnnotation) a;
                // Use the FileAttachmentAnnotation API to access embedded data;
                // see JavaDoc for the current accessor name.
                idx++;
            }
        }
    }
}
```

## Annotation coordinates

All annotation rectangles use **PDF user-space units**: points (1/72 inch), with the origin at the **lower-left** of the page. This is opposite to most screen coordinate systems (where y increases downwards). To position something "near the top" of a Letter-sized page (792 pt tall), use a y-coordinate close to 792, not 0.

The rectangle `new Rectangle(llx, lly, urx, ury)` takes lower-left and upper-right corners.

## Limitations

- **Appearance regeneration**: when adding or modifying annotations, the library may regenerate the appearance stream. Visual output matches PDF reader behaviour but may not pixel-perfectly match the original document's pre-saved appearance.
- **Markup interaction**: highlight/strike/underline annotations need text-position information. The library reasonably handles standard cases but may struggle with PDFs that have unusual text layout (e.g. text drawn glyph-by-glyph without spaces).
- **3D, multimedia, and form widget annotations** have varying levels of support. Widgets in particular interact with the AcroForm subsystem; see [Working with forms](forms.md).
