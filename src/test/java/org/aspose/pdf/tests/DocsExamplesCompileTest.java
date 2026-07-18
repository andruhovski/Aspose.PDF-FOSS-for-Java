package org.aspose.pdf.tests;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.CryptoAlgorithm;
import org.aspose.pdf.Document;
import org.aspose.pdf.DocumentInfo;
import org.aspose.pdf.Page;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.PdfFormatConversionOptions;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.Resources;
import org.aspose.pdf.XImage;
import org.aspose.pdf.XmpMetadata;
import org.aspose.pdf.XmpValue;
import org.aspose.pdf.annotations.Annotation;
import org.aspose.pdf.annotations.AnnotationCollection;
import org.aspose.pdf.annotations.DefaultAppearance;
import org.aspose.pdf.annotations.FileAttachmentAnnotation;
import org.aspose.pdf.annotations.FreeTextAnnotation;
import org.aspose.pdf.annotations.HighlightAnnotation;
import org.aspose.pdf.annotations.LinkAnnotation;
import org.aspose.pdf.annotations.MarkupAnnotation;
import org.aspose.pdf.devices.JpegDevice;
import org.aspose.pdf.devices.PngDevice;
import org.aspose.pdf.devices.Resolution;
import org.aspose.pdf.devices.TiffDevice;
import org.aspose.pdf.facades.DocumentPrivilege;
import org.aspose.pdf.forms.CheckboxField;
import org.aspose.pdf.forms.ComboBoxField;
import org.aspose.pdf.forms.Field;
import org.aspose.pdf.forms.Form;
import org.aspose.pdf.forms.SignatureField;
import org.aspose.pdf.forms.TextBoxField;
import org.aspose.pdf.forms.xfa.XfaForm;
import org.aspose.pdf.text.TextAbsorber;
import org.aspose.pdf.text.TextExtractionOptions;
import org.aspose.pdf.text.TextFragment;
import org.aspose.pdf.text.TextFragmentAbsorber;
import org.aspose.pdf.text.TextFragmentCollection;
import org.aspose.pdf.text.TextSearchOptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// Compile-time guard for all Java examples embedded in pdf/docs/\*.md and
/// pdf/README.md.
///
/// Every Java snippet from the public documentation is reproduced here
/// as a separate `@Test` method. If the API surface drifts away from
/// what the documentation claims, this class fails to compile and the
/// build breaks — catching the drift before any user encounters it. Having
/// one test method per snippet means surefire reports each example by
/// name, making it easy to spot which snippet has the issue.
///
/// The snippet bodies are never executed at runtime: every test starts
/// with `if (COMPILE_ONLY) return;`. The snippets would otherwise
/// try to open "input.pdf" and similar non-existent files. The Java
/// compiler does NOT mark statements after an `if (constant) return;`
/// as unreachable, so the snippet body is still fully type-checked.
///
/// When updating a snippet in a .md file, update the corresponding
/// method here. When changing an API, fix the snippet AND the
/// corresponding method here.
@SuppressWarnings({"unused", "EmptyTryBlock", "UnnecessaryLocalVariable",
        "TryFinallyCanBeTryWithResources", "CommentedOutCode", "RedundantThrows",
        "ConstantConditions"})
public class DocsExamplesCompileTest {

    /// Set at class load time. Each `@Test` starts with
    /// `if (COMPILE_ONLY) return;` so snippet bodies are
    /// type-checked by the compiler but never executed at runtime.
    private static final boolean COMPILE_ONLY = true;

    // ──────────────────────────────────────────────────────────────────
    // pdf/README.md  — 4 snippets
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void readme_1_extractText() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            TextAbsorber absorber = new TextAbsorber();
            doc.getPages().accept(absorber);
            String text = absorber.getText();
            System.out.println(text);
        }
    }

    @Test
    public void readme_2_extractImages() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            int pageIndex = 1;
            int imageIndex = 1;
            for (XImage image : doc.getPages().get(pageIndex).getResources().getImages()) {
                try (FileOutputStream out = new FileOutputStream("image-" + imageIndex + ".png")) {
                    image.save(out);
                }
                imageIndex++;
            }
        }
    }

    @Test
    public void readme_3_createPdf() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            TextFragment fragment = new TextFragment("Hello, PDF world!");
            page.getParagraphs().add(fragment);
            doc.save("output.pdf");
        }
    }

    @Test
    public void readme_4_workWithForms() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("form.pdf")) {
            Form form = doc.getForm();
            TextBoxField nameField = (TextBoxField) form.get("name");
            nameField.setValue("Jane Doe");

            // Iterate all fields
            for (org.aspose.pdf.forms.Field field : form.getFields()) {
                System.out.println(field.getPartialName() + " = " + field.getValue());
            }

            doc.save("form-filled.pdf");
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // pdf/docs/getting-started.md  — 4 snippets
    // ──────────────────────────────────────────────────────────────────

    /// Snippet 1 was originally written as a public class with main().
    public static class HelloPdf {
        public static void main(String[] args) throws Exception {
            try (Document doc = new Document()) {
                Page page = doc.getPages().add();
                page.getParagraphs().add(new TextFragment("Hello, PDF world!"));
                doc.save("hello.pdf");
            }
            System.out.println("Wrote hello.pdf");
        }
    }

    @Test
    public void gettingStarted_1_helloPdf() throws Exception {
        if (COMPILE_ONLY) return;
        HelloPdf.main(new String[]{});
    }

    /// Snippet 2 was originally written as a public class with main().
    public static class ReadPdf {
        public static void main(String[] args) throws Exception {
            try (Document doc = new Document("hello.pdf")) {
                System.out.println("Page count: " + doc.getPages().getCount());

                TextAbsorber absorber = new TextAbsorber();
                doc.getPages().accept(absorber);
                System.out.println("Text:\n" + absorber.getText());
            }
        }
    }

    @Test
    public void gettingStarted_2_readPdf() throws Exception {
        if (COMPILE_ONLY) return;
        ReadPdf.main(new String[]{});
    }

    @Test
    public void gettingStarted_3_inputStream() throws Exception {
        if (COMPILE_ONLY) return;
        try (InputStream in = DocsExamplesCompileTest.class.getResourceAsStream("/templates/invoice.pdf");
             Document doc = new Document(in)) {
            // ... process ...
        }
    }

    @Test
    public void gettingStarted_4_saving() throws Exception {
        if (COMPILE_ONLY) return;
        Document doc = new Document();
        doc.save("output.pdf");                        // by path
        doc.save(new FileOutputStream("output.pdf"));  // by stream
        doc.save();                                    // overwrite the file the doc was loaded from
    }

    // ──────────────────────────────────────────────────────────────────
    // pdf/docs/text-extraction.md  — 9 snippets
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void textExtraction_1_wholeDoc() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            TextAbsorber absorber = new TextAbsorber();
            doc.getPages().accept(absorber);
            String text = absorber.getText();
            // do something with text
        }
    }

    @Test
    public void textExtraction_2_perPage() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            for (int i = 1; i <= doc.getPages().getCount(); i++) {
                Page page = doc.getPages().get(i);
                TextAbsorber absorber = new TextAbsorber();
                absorber.visit(page);
                System.out.printf("--- Page %d ---%n%s%n", i, absorber.getText());
                absorber.reset();
            }
        }
    }

    @Test
    public void textExtraction_3_options() {
        if (COMPILE_ONLY) return;
        TextExtractionOptions options = new TextExtractionOptions(
            TextExtractionOptions.TextFormattingMode.Pure);
        TextAbsorber absorber = new TextAbsorber(options);
    }

    @Test
    public void textExtraction_4_search() throws Exception {
        if (COMPILE_ONLY) return;
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
    }

    @Test
    public void textExtraction_5_regex() {
        if (COMPILE_ONLY) return;
        TextSearchOptions options = new TextSearchOptions(true);  // true = regex
        TextFragmentAbsorber absorber = new TextFragmentAbsorber(
            "Invoice #\\d{6}", options);
    }

    @Test
    public void textExtraction_6_replace() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            TextFragmentAbsorber absorber = new TextFragmentAbsorber("DRAFT");
            doc.getPages().accept(absorber);

            for (TextFragment fragment : absorber.getTextFragments()) {
                fragment.setText("FINAL");
            }

            doc.save("output.pdf");
        }
    }

    @Test
    public void textExtraction_7_countWords() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            TextAbsorber absorber = new TextAbsorber();
            doc.getPages().accept(absorber);
            String[] words = absorber.getText().trim().split("\\s+");
            System.out.println("Word count: " + words.length);
        }
    }

    @Test
    public void textExtraction_8_findEmails() throws Exception {
        if (COMPILE_ONLY) return;
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
    }

    @Test
    public void textExtraction_9_pageNumbers() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            TextFragmentAbsorber absorber = new TextFragmentAbsorber("Important");
            doc.getPages().accept(absorber);

            Set<Integer> pages = new TreeSet<>();
            for (TextFragment fragment : absorber.getTextFragments()) {
                pages.add(fragment.getPage().getNumber());
            }
            System.out.println("Phrase found on pages: " + pages);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // pdf/docs/forms.md  — 11 snippets
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void forms_1_read() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("form.pdf")) {
            Form form = doc.getForm();
            System.out.println("Field count: " + form.getCount());

            for (Field field : form) {
                System.out.printf("  %s = %s%n",
                    field.getPartialName(),
                    field.getValue());
            }
        }
    }

    @Test
    public void forms_2_lookup() throws Exception {
        if (COMPILE_ONLY) return;
        Form form = stubForm();
        Field nameField = form.get("billing.name");
        if (nameField != null) {
            nameField.setValue("Jane Doe");
        }
    }

    @Test
    public void forms_3_caseInsensitive() throws Exception {
        if (COMPILE_ONLY) return;
        Form form = stubForm();
        if (form.hasField("Billing.Name", true)) {  // true = ignoreCase
            // ...
        }
    }

    @Test
    public void forms_4_fill() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("form.pdf")) {
            Form form = doc.getForm();

            ((TextBoxField) form.get("name")).setValue("Jane Doe");
            ((TextBoxField) form.get("email")).setValue("jane@example.com");
            ((CheckboxField) form.get("subscribe")).setChecked(true);
            ((ComboBoxField) form.get("country")).setValue("Netherlands");

            doc.save("form-filled.pdf");
        }
    }

    @Test
    public void forms_5_multiline() throws Exception {
        if (COMPILE_ONLY) return;
        Form form = stubForm();
        TextBoxField comments = (TextBoxField) form.get("comments");
        comments.setMultiline(true);
        comments.setValue("Line 1\nLine 2\nLine 3");
    }

    @Test
    public void forms_6_createField() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();

            // Field at position (100, 700) with size 200 x 24 points
            Rectangle rect = new Rectangle(100, 700, 300, 724);
            TextBoxField field = new TextBoxField(page, rect);
            field.setPartialName("name");
            field.setValue("Default value");

            doc.getForm().add(field);  // depending on API revision, this may be implicit
            doc.save("new-form.pdf");
        }
    }

    @Test
    public void forms_7_flatten() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("form-filled.pdf")) {
            Form form = doc.getForm();
            // Flatten depending on availability — check the Form API
            // for the current method signature in your version.
            // ... flatten ...
            doc.save("form-flat.pdf");
        }
    }

    @Test
    public void forms_8_xfa() throws Exception {
        if (COMPILE_ONLY) return;
        Form form = stubForm();
        XfaForm xfa = form.getXFA();
        if (xfa != null) {
            // ... operate on the XFA representation ...
        }
    }

    @Test
    public void forms_9_csvReport() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("form.pdf")) {
            System.out.println("name,value");
            for (Field field : doc.getForm()) {
                System.out.printf("\"%s\",\"%s\"%n",
                    field.getPartialName(),
                    field.getValue() != null ? field.getValue() : "");
            }
        }
    }

    /// Helper used by snippet 10 below — declared as in the .md sample.
    void fillFrom(Document doc, Map<String, String> values) throws IOException {
        Form form = doc.getForm();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Field field = form.get(entry.getKey());
            if (field != null) {
                field.setValue(entry.getValue());
            }
        }
    }

    @Test
    public void forms_10_bulkFillFromMap() throws Exception {
        if (COMPILE_ONLY) return;
        fillFrom(new Document(), new HashMap<>());
    }

    @Test
    public void forms_11_hasForm() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("file.pdf")) {
            Form form = doc.getForm();
            if (form != null && form.getCount() > 0) {
                System.out.println("Has " + form.getCount() + " form fields");
            } else {
                System.out.println("No form (or empty form)");
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // pdf/docs/annotations.md  — 10 snippets
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void annotations_1_read() throws Exception {
        if (COMPILE_ONLY) return;
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
    }

    @Test
    public void annotations_2_highlight() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            Page page = doc.getPages().get(1);

            // Coordinates are in PDF user-space points, origin at lower-left
            Rectangle rect = new Rectangle(100, 700, 400, 720);
            HighlightAnnotation highlight = new HighlightAnnotation(page, rect);

            page.getAnnotations().add(highlight);
            doc.save("highlighted.pdf");
        }
    }

    @Test
    public void annotations_3_freeText() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            Page page = doc.getPages().get(1);
            Rectangle rect = new Rectangle(100, 600, 300, 650);

            FreeTextAnnotation note = new FreeTextAnnotation(
                page, rect, new DefaultAppearance(/* font, size, color */));
            note.setContents("Please review this section.");

            page.getAnnotations().add(note);
            doc.save("annotated.pdf");
        }
    }

    @Test
    public void annotations_4_link() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            Page page = doc.getPages().get(1);
            Rectangle rect = new Rectangle(100, 500, 400, 520);

            LinkAnnotation link = new LinkAnnotation(page, rect);
            // ... configure action: GoToURIAction or similar
            page.getAnnotations().add(link);
            doc.save("linked.pdf");
        }
    }

    @Test
    public void annotations_5_content() throws Exception {
        if (COMPILE_ONLY) return;
        Page page = stubPage();
        for (Annotation a : page.getAnnotations()) {
            System.out.println(a.getSubtype() + ": " + a.getContents());
        }
    }

    @Test
    public void annotations_6_author() throws Exception {
        if (COMPILE_ONLY) return;
        Page page = stubPage();
        for (Annotation a : page.getAnnotations()) {
            if (a instanceof MarkupAnnotation) {
                MarkupAnnotation m = (MarkupAnnotation) a;
                // getAuthor(), getSubject(), getCreationDate() (where applicable)
            }
        }
    }

    @Test
    public void annotations_7_remove() throws Exception {
        if (COMPILE_ONLY) return;
        Page page = stubPage();
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
    }

    @Test
    public void annotations_8_stripAll() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            for (Page page : doc.getPages()) {
                AnnotationCollection annots = page.getAnnotations();
                while (annots.getCount() > 0) {
                    annots.delete(0);
                }
            }
            doc.save("no-annotations.pdf");
        }
    }

    @Test
    public void annotations_9_countByType() throws Exception {
        if (COMPILE_ONLY) return;
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
    }

    @Test
    public void annotations_10_extractAttachments() throws Exception {
        if (COMPILE_ONLY) return;
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
    }

    // ──────────────────────────────────────────────────────────────────
    // pdf/docs/rasterization.md  — 10 snippets
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void rasterization_1_quickPng() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            Page page = doc.getPages().get(1);

            PngDevice device = new PngDevice(new Resolution(150));
            try (FileOutputStream out = new FileOutputStream("page-1.png")) {
                device.process(page, out);
            }
        }
    }

    @Test
    public void rasterization_2_resolutionVariants() {
        if (COMPILE_ONLY) return;
        new Resolution(72);     // screen resolution (smallest)
        new Resolution(150);    // typical for on-screen viewing
        new Resolution(300);    // print quality
        new Resolution(600);    // high-quality print, archival
    }

    @Test
    public void rasterization_3_asymmetricResolution() {
        if (COMPILE_ONLY) return;
        new Resolution(300, 150);  // 300 DPI horizontal, 150 vertical
    }

    @Test
    public void rasterization_4_fixedSize() {
        if (COMPILE_ONLY) return;
        // Render to exactly 800 x 1200 pixels at 96 DPI
        PngDevice device = new PngDevice(800, 1200, new Resolution(96));
    }

    @Test
    public void rasterization_5_everyPage() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            PngDevice device = new PngDevice(new Resolution(150));
            for (int i = 1; i <= doc.getPages().getCount(); i++) {
                try (FileOutputStream out = new FileOutputStream("page-" + i + ".png")) {
                    device.process(doc.getPages().get(i), out);
                }
            }
        }
    }

    @Test
    public void rasterization_6_inMemory() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            PngDevice device = new PngDevice(new Resolution(96));
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            device.process(doc.getPages().get(1), buf);
            byte[] pngBytes = buf.toByteArray();
            // ... pass pngBytes to a web framework, encode as base64, etc.
        }
    }

    @Test
    public void rasterization_7_multiPageTiff() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            TiffDevice device = new TiffDevice(new Resolution(150));
            try (FileOutputStream out = new FileOutputStream("output.tiff")) {
                // Render every page
                for (int i = 1; i <= doc.getPages().getCount(); i++) {
                    device.process(doc.getPages().get(i), out);
                }
            }
        }
    }

    @Test
    public void rasterization_8_jpegQuality() {
        if (COMPILE_ONLY) return;
        JpegDevice device = new JpegDevice(new Resolution(150), 85);  // quality = 85
    }

    @Test
    public void rasterization_9_thumbnails() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            // Small thumbnail, 200px wide, ~aspect-ratio-preserving height for a Letter page
            JpegDevice device = new JpegDevice(200, 260, new Resolution(72), 75);
            try (FileOutputStream out = new FileOutputStream("thumb.jpg")) {
                device.process(doc.getPages().get(1), out);
            }
        }
    }

    /// Helper used by snippet 10 below — declared as in the .md sample.
    void renderAll(Path inputPdf, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        try (Document doc = new Document(inputPdf.toString())) {
            PngDevice device = new PngDevice(new Resolution(150));
            int width = String.valueOf(doc.getPages().getCount()).length();

            for (int i = 1; i <= doc.getPages().getCount(); i++) {
                Path out = outputDir.resolve(String.format("page-%0" + width + "d.png", i));
                try (var os = Files.newOutputStream(out)) {
                    device.process(doc.getPages().get(i), os);
                }
            }
        }
    }

    @Test
    public void rasterization_10_renderAllToFolder() throws Exception {
        if (COMPILE_ONLY) return;
        renderAll(Paths.get("input.pdf"), Paths.get("out"));
    }

    // ──────────────────────────────────────────────────────────────────
    // pdf/docs/security.md  — 7 snippets
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void security_1_encrypt() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("plain.pdf")) {
            int permissions = DocumentPrivilege.ALLOW_PRINT
                            | DocumentPrivilege.ALLOW_COPY;

            doc.encrypt(
                "userPwd",        // user password (required to open)
                "ownerPwd",       // owner password (required to change permissions)
                permissions,
                CryptoAlgorithm.AESx256);

            doc.save("encrypted.pdf");
        }
    }

    @Test
    public void security_2_permissionFlags() {
        if (COMPILE_ONLY) return;
        int permissions = DocumentPrivilege.ALLOW_PRINT
                        | DocumentPrivilege.ALLOW_COPY
                        | DocumentPrivilege.ALLOW_SCREEN_READERS;
    }

    @Test
    public void security_3_allowAllForbidAll() {
        if (COMPILE_ONLY) return;
        DocumentPrivilege all = DocumentPrivilege.getAllowAll();
        DocumentPrivilege none = DocumentPrivilege.getForbidAll();
        int allValue = all.getValue();
    }

    @Test
    public void security_4_readEncrypted() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("encrypted.pdf", "userPwd")) {
            // ... normal operations
        }
    }

    @Test
    public void security_5_removeEncryption() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("encrypted.pdf", "userPwd")) {
            doc.save("decrypted.pdf");  // saved without encryption by default
        }
    }

    @Test
    public void security_6_digitalSignatures() {
        if (COMPILE_ONLY) return;
        // Pseudocode — exact API varies by version; see SignatureField and the
        // signature subsystem in src/main/java/org/aspose/pdf/forms/ and
        // engine/signature/ for the methods available in your build.
    }

    @Test
    public void security_7_verifySignature() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("signed.pdf")) {
            for (Field field : doc.getForm()) {
                if (field instanceof SignatureField) {
                    SignatureField sig = (SignatureField) field;
                    // ... query sig for validity, signer name, time, etc.
                }
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // pdf/docs/pdfa.md  — 6 snippets
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void pdfa_1_validateFile() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            boolean compliant = doc.validate("validation-log.xml", PdfFormat.PDF_A_2B);
            if (compliant) {
                System.out.println("Document is PDF/A-2B compliant.");
            } else {
                System.out.println("Not compliant. See validation-log.xml for details.");
            }
        }
    }

    @Test
    public void pdfa_2_validateStream() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            ByteArrayOutputStream log = new ByteArrayOutputStream();
            boolean ok = doc.validate(log, PdfFormat.PDF_A_2B);
            String xmlReport = log.toString("UTF-8");
        }
    }

    @Test
    public void pdfa_3_convert() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            boolean ok = doc.convert(
                "conversion-log.xml",
                PdfFormat.PDF_A_2B,
                ConvertErrorAction.Delete);

            doc.save("output-pdfa.pdf");
        }
    }

    @Test
    public void pdfa_4_conversionOptions() throws Exception {
        if (COMPILE_ONLY) return;
        PdfFormatConversionOptions options = new PdfFormatConversionOptions(
            "log.xml",
            PdfFormat.PDF_A_2B,
            ConvertErrorAction.Delete);

        try (Document doc = new Document("input.pdf")) {
            boolean ok = doc.convert(options);
            doc.save("output.pdf");
        }
    }

    @Test
    public void pdfa_5_convertToPdfA2B() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            doc.convertToPdfA2B("log.xml");
            doc.save("output.pdf");
        }
    }

    @Test
    public void pdfa_6_verifyAfterConversion() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("output.pdf")) {
            boolean ok = doc.validate("validation-log.xml", PdfFormat.PDF_A_2B);
            if (!ok) {
                // conversion left some violations — inspect the log
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // pdf/docs/metadata.md  — 7 snippets
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void metadata_1_readInfo() throws Exception {
        if (COMPILE_ONLY) return;
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
    }

    @Test
    public void metadata_2_setInfo() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            DocumentInfo info = doc.getInfo();
            info.setTitle("Quarterly Report Q1 2026");
            info.setAuthor("Jane Doe");
            info.setSubject("Financial summary");
            info.setKeywords("finance, quarterly, 2026");

            doc.save("output.pdf");
        }
    }

    @Test
    public void metadata_3_xmpIterate() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            XmpMetadata xmp = doc.getMetadata();
            for (Map.Entry<String, XmpValue> entry : xmp) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    @Test
    public void metadata_4_readRawXmp() throws Exception {
        if (COMPILE_ONLY) return;
        try (Document doc = new Document("input.pdf")) {
            ByteArrayOutputStream xml = new ByteArrayOutputStream();
            doc.getXmpMetadata(xml);
            String xmpXml = xml.toString("UTF-8");
            // ... parse with your XML tool of choice ...
        }
    }

    @Test
    public void metadata_5_writeRawXmp() throws Exception {
        if (COMPILE_ONLY) return;
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
    }

    @Test
    public void metadata_6_stripAll() throws Exception {
        if (COMPILE_ONLY) return;
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
    }

    @Test
    public void metadata_7_canonicalCreator() throws Exception {
        if (COMPILE_ONLY) return;
        String inputPath = "input.pdf";
        String outputPath = "output.pdf";
        try (Document doc = new Document(inputPath)) {
            DocumentInfo info = doc.getInfo();
            info.setCreator("MyApp v1.2.3");
            doc.save(outputPath);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Helpers — stub objects so snippets referencing a "previously
    // declared" variable (form, page, ...) still compile.
    // ──────────────────────────────────────────────────────────────────

    private static Form stubForm() {
        try {
            return new Document().getForm();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Page stubPage() {
        try {
            return new Document().getPages().add();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
