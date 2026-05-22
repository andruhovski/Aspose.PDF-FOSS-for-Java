package org.aspose.pdf.tests;

import org.aspose.pdf.DocumentInfo;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSString;
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
        COSDictionary dict = new COSDictionary();
        dict.set("Title", new COSString("My Document"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("My Document", info.getTitle());
    }

    @Test
    public void getTitleReturnsNullWhenAbsent() {
        DocumentInfo info = new DocumentInfo(new COSDictionary());
        assertNull(info.getTitle());
    }

    @Test
    public void setTitleUpdatesDict() {
        COSDictionary dict = new COSDictionary();
        DocumentInfo info = new DocumentInfo(dict);
        info.setTitle("New Title");
        assertEquals("New Title", info.getTitle());
    }

    @Test
    public void setTitleNullRemovesEntry() {
        COSDictionary dict = new COSDictionary();
        dict.set("Title", new COSString("Old Title"));
        DocumentInfo info = new DocumentInfo(dict);
        info.setTitle(null);
        assertNull(info.getTitle());
        assertFalse(dict.containsKey("Title"));
    }

    @Test
    public void getAuthorReturnsValue() {
        COSDictionary dict = new COSDictionary();
        dict.set("Author", new COSString("John Doe"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("John Doe", info.getAuthor());
    }

    @Test
    public void setAuthorUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new COSDictionary());
        info.setAuthor("Jane Doe");
        assertEquals("Jane Doe", info.getAuthor());
    }

    @Test
    public void getSubjectReturnsValue() {
        COSDictionary dict = new COSDictionary();
        dict.set("Subject", new COSString("PDF Testing"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("PDF Testing", info.getSubject());
    }

    @Test
    public void setSubjectUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new COSDictionary());
        info.setSubject("New Subject");
        assertEquals("New Subject", info.getSubject());
    }

    @Test
    public void getKeywordsReturnsValue() {
        COSDictionary dict = new COSDictionary();
        dict.set("Keywords", new COSString("pdf, test, java"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("pdf, test, java", info.getKeywords());
    }

    @Test
    public void setKeywordsUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new COSDictionary());
        info.setKeywords("a, b, c");
        assertEquals("a, b, c", info.getKeywords());
    }

    @Test
    public void getCreatorReturnsValue() {
        COSDictionary dict = new COSDictionary();
        dict.set("Creator", new COSString("TestApp"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("TestApp", info.getCreator());
    }

    @Test
    public void setCreatorUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new COSDictionary());
        info.setCreator("MyApp");
        assertEquals("MyApp", info.getCreator());
    }

    @Test
    public void getProducerReturnsValue() {
        COSDictionary dict = new COSDictionary();
        dict.set("Producer", new COSString("OpenPDF"));
        DocumentInfo info = new DocumentInfo(dict);
        assertEquals("OpenPDF", info.getProducer());
    }

    @Test
    public void setProducerUpdatesDict() {
        DocumentInfo info = new DocumentInfo(new COSDictionary());
        info.setProducer("OpenPDF 1.0");
        assertEquals("OpenPDF 1.0", info.getProducer());
    }

    @Test
    public void getCreationDateParsesValidDate() {
        COSDictionary dict = new COSDictionary();
        dict.set("CreationDate", new COSString("D:20240315143000"));
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
        DocumentInfo info = new DocumentInfo(new COSDictionary());
        assertNull(info.getCreationDate());
    }

    @Test
    public void getModDateParsesValidDate() {
        COSDictionary dict = new COSDictionary();
        dict.set("ModDate", new COSString("D:20240601120000"));
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
        COSDictionary dict = new COSDictionary();
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
        COSDictionary dict = new COSDictionary();
        dict.set("CreationDate", new COSString("D:20240101000000"));
        DocumentInfo info = new DocumentInfo(dict);
        info.setCreationDate(null);
        assertNull(info.getCreationDate());
    }

    @Test
    public void getCOSDictionaryReturnsUnderlying() {
        COSDictionary dict = new COSDictionary();
        DocumentInfo info = new DocumentInfo(dict);
        assertSame(dict, info.getCOSDictionary());
    }

    @Test
    public void allStringFieldsRoundTrip() {
        DocumentInfo info = new DocumentInfo(new COSDictionary());
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
