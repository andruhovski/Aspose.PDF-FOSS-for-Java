# Working with Text

This guide covers extraction, search, and replacement using `TextAbsorber` and `TextFragmentAbsorber`.

## TextAbsorber: pull all text out of a document

`TextAbsorber` walks every page and gives you the entire textual content as a single string:

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.text.TextAbsorber;

try (Document doc = new Document("input.pdf")) {
    TextAbsorber absorber = new TextAbsorber();
    doc.getPages().accept(absorber);
    String text = absorber.getText();
    // do something with text
}
```

### Per-page extraction

If you want text page by page (for indexing, OCR comparison, or layout-aware processing), visit each page directly:

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.text.TextAbsorber;

try (Document doc = new Document("input.pdf")) {
    for (int i = 1; i <= doc.getPages().getCount(); i++) {
        Page page = doc.getPages().get(i);
        TextAbsorber absorber = new TextAbsorber();
        absorber.visit(page);
        System.out.printf("--- Page %d ---%n%s%n", i, absorber.getText());
        absorber.reset();
    }
}
```

Page indices are **1-based**. Calling `absorber.reset()` between pages discards previously absorbed text so each iteration only contains the current page.

### Extraction options

`TextExtractionOptions` controls how text is reconstructed from glyph positions. The default (`Pure`) extracts raw glyph runs; `Raw` and other modes may be added in future releases.

```java
import org.aspose.pdf.text.TextAbsorber;
import org.aspose.pdf.text.TextExtractionOptions;

TextExtractionOptions options = new TextExtractionOptions(
    TextExtractionOptions.TextFormattingMode.Pure);
TextAbsorber absorber = new TextAbsorber(options);
```

## TextFragmentAbsorber: search for specific strings

When you need positional information about text — for redaction, highlighting, or replacement — use `TextFragmentAbsorber`:

```java
import org.aspose.pdf.Document;
import org.aspose.pdf.text.TextFragment;
import org.aspose.pdf.text.TextFragmentAbsorber;
import org.aspose.pdf.text.TextFragmentCollection;

try (Document doc = new Document("input.pdf")) {
    TextFragmentAbsorber absorber = new TextFragmentAbsorber("Invoice");
    doc.getPages().accept(absorber);

    TextFragmentCollection fragments = absorber.getTextFragments();
    System.out.println("Found " + fragments.getCount() + " occurrences");

    for (TextFragment fragment : fragments) {
        System.out.println("  '" + fragment.getText() + "'"
            + " on page " + fragment.getPage().getNumber()
            + " at " + fragment.getPosition());
    }
}
```

### Regular expression search

Pass `TextSearchOptions` configured for regex matching:

```java
import org.aspose.pdf.text.TextFragmentAbsorber;
import org.aspose.pdf.text.TextSearchOptions;

TextSearchOptions options = new TextSearchOptions(true);  // true = regex
TextFragmentAbsorber absorber = new TextFragmentAbsorber(
    "Invoice #\\d{6}", options);
```

The library uses Java's `java.util.regex.Pattern` semantics. Use double-backslashes in Java source for regex metacharacters (`\\d`, `\\s`, etc.).

## Replacing text

Modify a fragment's text and save:

```java
try (Document doc = new Document("input.pdf")) {
    TextFragmentAbsorber absorber = new TextFragmentAbsorber("DRAFT");
    doc.getPages().accept(absorber);

    for (TextFragment fragment : absorber.getTextFragments()) {
        fragment.setText("FINAL");
    }

    doc.save("output.pdf");
}
```

A few caveats specific to PDF text replacement:

- **Font availability matters.** If the new text contains glyphs that are not encoded in the fragment's current font, the replacement may render as missing glyphs or fall back. For ASCII-to-ASCII replacements this is rarely an issue; for replacement involving CJK or non-Latin scripts, ensure the target font supports the needed glyphs.
- **Width may change.** Variable-width fonts mean the new text may be wider or narrower than the original. The library doesn't reflow surrounding content.
- **Tagged PDFs.** If the document has a logical structure tree, replacement updates the visible text but does not touch the structure tree's actual-text entries. You may need to update those separately for accessibility.

## Common patterns

### Count words in a PDF

```java
try (Document doc = new Document("input.pdf")) {
    TextAbsorber absorber = new TextAbsorber();
    doc.getPages().accept(absorber);
    String[] words = absorber.getText().trim().split("\\s+");
    System.out.println("Word count: " + words.length);
}
```

### Find all email addresses

```java
import java.util.regex.Matcher;
import java.util.regex.Pattern;

try (Document doc = new Document("input.pdf")) {
    TextAbsorber absorber = new TextAbsorber();
    doc.getPages().accept(absorber);

    Pattern email = Pattern.compile(
        "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    Matcher m = email.matcher(absorber.getText());
    while (m.find()) {
        System.out.println(m.group());
    }
}
```

### Find page numbers containing a phrase

```java
try (Document doc = new Document("input.pdf")) {
    TextFragmentAbsorber absorber = new TextFragmentAbsorber("Important");
    doc.getPages().accept(absorber);

    Set<Integer> pages = new TreeSet<>();
    for (TextFragment fragment : absorber.getTextFragments()) {
        pages.add(fragment.getPage().getNumber());
    }
    System.out.println("Phrase found on pages: " + pages);
}
```

## Known limitations

- **Reading order.** Text extraction follows the order glyphs were placed in the content stream, which usually matches reading order for well-formed PDFs but may not for documents authored with unusual layout patterns (multi-column without explicit structure, manually positioned glyphs, etc.).
- **Right-to-left scripts.** Basic support exists; complex bidi reordering edge cases may differ from the commercial Aspose.PDF output.
- **Layout-preserving modes** (e.g. `TextFormattingMode.Pure` vs. layout-aware) — the library currently emphasises `Pure`. Other modes are on the roadmap.
- **OCR.** Not provided. Scanned PDFs (images of text) will yield no text from `TextAbsorber`.
