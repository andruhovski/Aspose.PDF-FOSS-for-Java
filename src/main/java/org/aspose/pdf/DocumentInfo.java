package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfString;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/// Wraps the PDF document information dictionary (ISO 32000-1:2008, §14.3.3).
///
/// Provides access to metadata fields such as Title, Author, Subject, Keywords,
/// Creator, Producer, CreationDate, and ModDate. Date fields are stored as
/// PDF date strings and converted to [Date] via [PdfString#getAsDate()].
///
public class DocumentInfo {

    private static final Logger LOG = Logger.getLogger(DocumentInfo.class.getName());

    private final PdfDictionary dict;

    /// Creates a DocumentInfo wrapper around the given /Info dictionary.
    ///
    /// @param infoDict the /Info dictionary from the trailer
    /// @throws IllegalArgumentException if infoDict is null
    public DocumentInfo(PdfDictionary infoDict) {
        if (infoDict == null) {
            throw new IllegalArgumentException("Info dictionary must not be null");
        }
        this.dict = infoDict;
        LOG.fine(() -> "DocumentInfo created with " + dict.size() + " entries");
    }

    /// Returns the document title, or null if not set.
    ///
    /// @return the title string, or null
    public String getTitle() {
        return dict.getString("Title");
    }

    /// Sets the document title.
    ///
    /// @param title the title string
    public void setTitle(String title) {
        setStringValue("Title", title);
    }

    /// Returns the document author, or null if not set.
    ///
    /// @return the author string, or null
    public String getAuthor() {
        return dict.getString("Author");
    }

    /// Sets the document author.
    ///
    /// @param author the author string
    public void setAuthor(String author) {
        setStringValue("Author", author);
    }

    /// Returns the document subject, or null if not set.
    ///
    /// @return the subject string, or null
    public String getSubject() {
        return dict.getString("Subject");
    }

    /// Sets the document subject.
    ///
    /// @param subject the subject string
    public void setSubject(String subject) {
        setStringValue("Subject", subject);
    }

    /// Returns the document keywords, or null if not set.
    ///
    /// @return the keywords string, or null
    public String getKeywords() {
        return dict.getString("Keywords");
    }

    /// Sets the document keywords.
    ///
    /// @param keywords the keywords string
    public void setKeywords(String keywords) {
        setStringValue("Keywords", keywords);
    }

    /// Returns the creator application name, or null if not set.
    ///
    /// @return the creator string, or null
    public String getCreator() {
        return dict.getString("Creator");
    }

    /// Sets the creator application name.
    ///
    /// @param creator the creator string
    public void setCreator(String creator) {
        setStringValue("Creator", creator);
    }

    /// Returns the producer application name, or null if not set.
    ///
    /// @return the producer string, or null
    public String getProducer() {
        return dict.getString("Producer");
    }

    /// Sets the producer application name.
    ///
    /// @param producer the producer string
    public void setProducer(String producer) {
        setStringValue("Producer", producer);
    }

    /// Returns the document creation date, or null if not set or not parseable.
    ///
    /// @return the creation date, or null
    public Date getCreationDate() {
        return getDateValue("CreationDate");
    }

    /// Sets the document creation date.
    ///
    /// @param date the creation date
    public void setCreationDate(Date date) {
        setDateValue("CreationDate", date);
    }

    /// Returns the document modification date, or null if not set or not parseable.
    ///
    /// @return the modification date, or null
    public Date getModDate() {
        return getDateValue("ModDate");
    }

    /// Sets the document modification date.
    ///
    /// @param date the modification date
    public void setModDate(Date date) {
        setDateValue("ModDate", date);
    }

    /// Returns the time zone parsed from the /CreationDate PDF date string.
    ///
    /// PDF date format: `D:YYYYMMDDHHmmSSOHH'mm'` where O is + or - or Z.
    /// If no timezone offset is present, returns the system default timezone.
    ///
    /// @return the timezone from the creation date, or the system default if not parseable
    public TimeZone getCreationTimeZone() {
        PdfBase obj = dict.get("CreationDate");
        if (!(obj instanceof PdfString)) {
            return TimeZone.getDefault();
        }
        String dateStr = ((PdfString) obj).getString();
        return parseTimeZone(dateStr);
    }

    /// Returns the underlying PDF dictionary.
    ///
    /// @return the raw /Info dictionary
    public PdfDictionary getPdfDictionary() {
        return dict;
    }

    /// Clears all standard document information entries from the underlying
    /// dictionary.
    ///
    /// This is a pragmatic Aspose-compatible helper used by facade workflows
    /// that need to remove document metadata without replacing the /Info
    /// dictionary object itself.
    ///
    public void clear() {
        setTitle(null);
        setAuthor(null);
        setSubject(null);
        setKeywords(null);
        setCreator(null);
        setProducer(null);
        setCreationDate(null);
        setModDate(null);
    }

    /// Retrieves a date value from the dictionary.
    /// The value is expected to be a PdfString containing a PDF date string.
    ///
    /// @param key the dictionary key
    /// @return the parsed date, or null
    private Date getDateValue(String key) {
        PdfBase obj = dict.get(key);
        if (!(obj instanceof PdfString)) {
            return null;
        }
        LocalDateTime ldt = ((PdfString) obj).getAsDate();
        if (ldt == null) {
            return null;
        }
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /// Sets a string value in the dictionary. If value is null, the key is removed.
    ///
    /// @param key   the dictionary key
    /// @param value the string value, or null to remove
    private void setStringValue(String key, String value) {
        if (value == null) {
            dict.set(key, null);
        } else {
            dict.setString(key, value);
        }
    }


    /// Parses the timezone offset from a PDF date string.
    /// PDF date format: D:YYYYMMDDHHmmSSOHH'mm' where O is +, -, or Z.
    ///
    /// @param dateStr the PDF date string
    /// @return the parsed timezone, or system default if not parseable
    private TimeZone parseTimeZone(String dateStr) {
        if (dateStr == null) return TimeZone.getDefault();
        // Strip "D:" prefix
        String s = dateStr.startsWith("D:") ? dateStr.substring(2) : dateStr;
        // Timezone offset starts after the basic date/time (14 chars: YYYYMMDDHHmmSS)
        if (s.length() <= 14) return TimeZone.getDefault();
        String tzPart = s.substring(14);
        if (tzPart.startsWith("Z")) {
            return TimeZone.getTimeZone("UTC");
        }
        if (tzPart.startsWith("+") || tzPart.startsWith("-")) {
            // Parse +HH'mm' or -HH'mm'
            String cleaned = tzPart.replace("'", "");
            try {
                int hours = Integer.parseInt(cleaned.substring(1, 3));
                int minutes = cleaned.length() >= 5 ? Integer.parseInt(cleaned.substring(3, 5)) : 0;
                int totalMs = (hours * 60 + minutes) * 60 * 1000;
                if (cleaned.startsWith("-")) totalMs = -totalMs;
                return TimeZone.getTimeZone(ZoneOffset.ofTotalSeconds(totalMs / 1000));
            } catch (Exception e) {
                LOG.fine(() -> "Failed to parse timezone from date string: " + dateStr);
            }
        }
        return TimeZone.getDefault();
    }

    /// Sets a date value in the dictionary as a PDF date string.
    /// Format: D:YYYYMMDDHHmmSS
    ///
    /// @param key  the dictionary key
    /// @param date the date, or null to remove
    private void setDateValue(String key, Date date) {
        if (date == null) {
            dict.set(key, null);
            return;
        }
        LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String pdfDate = String.format("D:%04d%02d%02d%02d%02d%02d",
                ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(),
                ldt.getHour(), ldt.getMinute(), ldt.getSecond());
        dict.setString(key, pdfDate);
    }
}
