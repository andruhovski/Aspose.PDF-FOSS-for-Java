package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.annotations.RedactionAnnotation;
import org.aspose.pdf.text.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/// Redaction must REMOVE the underlying text under the redacted region, not merely
/// paint a box over it (ISO 32000-1 §12.5.6.23) — otherwise the "redacted" text stays
/// extractable and searchable. Guards the G3 fix (PDFNET\_40853).
public class RedactionRemovesTextTest {

    private static Document docWithTwoLines() throws Exception {
        Document doc = new Document();
        Page page = doc.getPages().add();
        TextBuilder tb = new TextBuilder(page);

        TextFragment secret = new TextFragment("SECRET-2222");
        secret.getTextState().setFontName("Helvetica");
        secret.getTextState().setFontSize(12);
        secret.setPosition(new Position(100, 700));
        tb.appendText(secret);

        TextFragment keep = new TextFragment("PUBLIC-9999");
        keep.getTextState().setFontName("Helvetica");
        keep.getTextState().setFontSize(12);
        keep.setPosition(new Position(100, 600));
        tb.appendText(keep);
        return doc;
    }

    @Test
    public void redactRemovesUnderlyingText() throws Exception {
        Document doc = docWithTwoLines();

        // find the secret fragment's rectangle and redact it
        TextFragmentAbsorber find = new TextFragmentAbsorber("SECRET-2222");
        doc.getPages().get(1).accept(find);
        assertEquals(1, find.getTextFragments().getCount(), "precondition: secret text present");
        Rectangle secretRect = find.getTextFragments().get(1).getRectangle();

        Page page = doc.getPages().get(1);
        RedactionAnnotation ra = new RedactionAnnotation(page, secretRect);
        ra.setFillColor(Color.getBlack());
        page.getAnnotations().add(ra);
        ra.redact();

        // round-trip through save so we assert on persisted content, not in-memory state
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        doc.close();

        try (Document reopened = new Document(new ByteArrayInputStream(baos.toByteArray()))) {
            TextFragmentAbsorber gone = new TextFragmentAbsorber("2222");
            gone.setTextSearchOptions(new TextSearchOptions(true));
            reopened.getPages().get(1).accept(gone);
            assertEquals(0, gone.getTextFragments().getCount(),
                    "redacted text must not be extractable/searchable after redact()");

            // the non-redacted text is untouched
            TextFragmentAbsorber kept = new TextFragmentAbsorber("PUBLIC-9999");
            reopened.getPages().get(1).accept(kept);
            assertEquals(1, kept.getTextFragments().getCount(),
                    "text outside the redaction region must survive");
        }
    }
}
