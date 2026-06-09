package org.aspose.pdf.tests;

import org.aspose.pdf.DocumentInfo;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DocumentInfo}.
 */
public class DocumentInfoTest {

    @Test
    public void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new DocumentInfo(null));
    }

    @Test
    public void getTitleReturnsValue() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Title", new PdfString("My Document"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("My Document", info.getTitle());
    }

    @Test
    public void getTitleReturnsNullWhenAbsent() {
        DocumentInfo info = new DocumentInfo(new PdfDictionary());
        assertNull(info.getTitle());
    }

    @Test
    public void setTitleUpdatesDict() {
        PdfDictionary dict = new PdfDictionary();
        DocumentInfo info = new DocumentInfo(dict);
        info.setTitle("New Title");
        assertEquals("New Title", info.getTitle());
    }

    @Test
    public void setTitleNullRemovesEntry() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Title", new PdfString("Old Title"));
        DocumentInfo info = new DocumentInfo(dict);
        info.setTitle(null);
        assertNull(info.getTitle());
        assertFalse(dict.containsKey("Title"));
    }

    @Test
    public void getAuthorReturnsValue() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Author", new PdfString("John Doe"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("John Doe", info.getAuthor());
    }

    @Test
    public void setAuthorUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new PdfDictionary());
        info.setAuthor("Jane Doe");
        assertEquals("Jane Doe", info.getAuthor());
    }

    @Test
    public void getSubjectReturnsValue() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Subject", new PdfString("PDF Testing"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("PDF Testing", info.getSubject());
    }

    @Test
    public void setSubjectUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new PdfDictionary());
        info.setSubject("New Subject");
        assertEquals("New Subject", info.getSubject());
    }

    @Test
    public void getKeywordsReturnsValue() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Keywords", new PdfString("pdf, test, java"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("pdf, test, java", info.getKeywords());
    }

    @Test
    public void setKeywordsUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new PdfDictionary());
        info.setKeywords("a, b, c");
        assertEquals("a, b, c", info.getKeywords());
    }

    @Test
    public void getCreatorReturnsValue() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Creator", new PdfString("TestApp"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("TestApp", info.getCreator());
    }

    @Test
    public void setCreatorUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new PdfDictionary());
        info.setCreator("MyApp");
        assertEquals("MyApp", info.getCreator());
    }

    @Test
    public void getProducerReturnsValue() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("Producer", new PdfString("OpenPDF"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("OpenPDF", info.getProducer());
    }

    @Test
    public void setProducerUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new PdfDictionary());
        info.setProducer("OpenPDF 1.0");
        assertEquals("OpenPDF 1.0", info.getProducer());
    }

    @Test
    public void getCreationDateParsesValidDate() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("CreationDate", new PdfString("D:20240315143000"));
        DocumentInfo info = new DocumentInfo(dict);

        Date date = info.getCreationDate();
        assertNotNull(date);

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    @Test
    public void getCreationDateReturnsNullWhenAbsent() {
        DocumentInfo info = new DocumentInfo(new PdfDictionary());
        assertNull(info.getCreationDate());
    }

    @Test
    public void getModDateParsesValidDate() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("ModDate", new PdfString("D:20240601120000"));
        DocumentInfo info = new DocumentInfo(dict);

        Date date = info.getModDate();
        assertNotNull(date);

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void setCreationDateWritesPdfDateString() {
        PdfDictionary dict = new PdfDictionary();
        DocumentInfo info = new DocumentInfo(dict);

        Calendar cal = new GregorianCalendar(2024, Calendar.MARCH, 15, 14, 30, 0);
        info.setCreationDate(cal.getTime());

        Date result = info.getCreationDate();
        assertNotNull(result);

        Calendar resultCal = new GregorianCalendar();
        resultCal.setTime(result);
        assertEquals(2024, resultCal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, resultCal.get(Calendar.MONTH));
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void setCreationDateNullRemovesEntry() {
        PdfDictionary dict = new PdfDictionary();
        dict.set("CreationDate", new PdfString("D:20240101000000"));
        DocumentInfo info = new DocumentInfo(dict);
        info.setCreationDate(null);
        assertNull(info.getCreationDate());
    }

    @Test
    public void getPdfDictionaryReturnsUnderlying() {
        PdfDictionary dict = new PdfDictionary();
        DocumentInfo info = new DocumentInfo(dict);
        assertSame(dict, info.getPdfDictionary());
    }

    @Test
    public void allStringFieldsRoundTrip() {
        DocumentInfo info = new DocumentInfo(new PdfDictionary());
        info.setTitle("T");
        info.setAuthor("A");
        info.setSubject("S");
        info.setKeywords("K");
        info.setCreator("C");
        info.setProducer("P");

        assertEquals("T", info.getTitle());
        assertEquals("A", info.getAuthor());
        assertEquals("S", info.getSubject());
        assertEquals("K", info.getKeywords());
        assertEquals("C", info.getCreator());
        assertEquals("P", info.getProducer());
    }
}
