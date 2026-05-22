package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Metadata-related fixes for PDF/A compliance.
 * <p>
 * These fixes <strong>must run first</strong> during conversion because other fix
 * classes may depend on a well-formed XMP metadata stream being present in the catalog.
 * </p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Create XMP metadata when absent ({@link #ensureXmpMetadata})</li>
 *   <li>Remove compression from the metadata stream ({@link #removeMetadataFilter})</li>
 *   <li>Inject {@code pdfaid:part} / {@code pdfaid:conformance} ({@link #ensurePdfAId})</li>
 *   <li>Synchronise the classic Info dictionary with XMP properties ({@link #syncDocInfoWithXmp})</li>
 * </ul>
 */
public final class MetadataFixes {

    private static final Logger LOG = Logger.getLogger(MetadataFixes.class.getName());

    /**
     * Creates a new MetadataFixes instance.
     */
    public MetadataFixes() {
        // default
    }

    /**
     * Ensures the catalog contains an XMP metadata stream.
     * <p>
     * If the catalog has no {@code /Metadata} entry, a new XMP packet is created
     * from the document's Info dictionary (if present) and attached as an
     * indirect-object stream.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result to collect warnings/errors
     * @throws IOException if an I/O error occurs
     */
    public void ensureXmpMetadata(PDFParser parser, PdfFormat format,
                                  ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase metaRef = catalog.get("Metadata");
        if (metaRef != null) {
            // Already has metadata — resolve to verify it's a stream
            COSBase metaObj = parser.resolveReference(metaRef);
            if (metaObj instanceof COSStream) {
                LOG.fine("Catalog already has /Metadata stream");
                return;
            }
        }

        LOG.info("Creating XMP metadata from DocInfo");

        // Read DocInfo
        String title = "";
        String author = "";
        String subject = "";
        String keywords = "";
        String creator = "";
        String producer = "";
        String createDate = "";
        String modDate = "";

        COSDictionary trailer = parser.getTrailer();
        COSBase infoRef = trailer.get("Info");
        if (infoRef != null) {
            COSBase infoObj = parser.resolveReference(infoRef);
            if (infoObj instanceof COSDictionary) {
                COSDictionary info = (COSDictionary) infoObj;
                title = safeString(info.getString("Title"));
                author = safeString(info.getString("Author"));
                subject = safeString(info.getString("Subject"));
                keywords = safeString(info.getString("Keywords"));
                creator = safeString(info.getString("Creator"));
                producer = safeString(info.getString("Producer"));
                createDate = safeString(info.getString("CreationDate"));
                modDate = safeString(info.getString("ModDate"));
            }
        }

        // If no dates, use current time in PDF date format
        if (createDate.isEmpty()) {
            createDate = currentPdfDate();
        }
        if (modDate.isEmpty()) {
            modDate = currentPdfDate();
        }

        int part = format.getPart();
        String conformance = format.getConformance();

        byte[] xmpBytes = buildXmpPacket(title, author, subject, keywords,
                creator, producer, createDate, modDate, part, conformance);

        // Create a COSStream for the metadata
        COSStream metaStream = new COSStream();
        metaStream.set("Type", COSName.of("Metadata"));
        metaStream.set("Subtype", COSName.of("XML"));
        metaStream.setDecodedData(xmpBytes);
        // Metadata must be uncompressed for PDF/A
        metaStream.set(COSName.FILTER, null);

        // Register as an indirect object
        int maxObj = findMaxObjectNumber(parser);
        COSObjectKey newKey = new COSObjectKey(maxObj + 1, 0);
        COSObjectReference ref = new COSObjectReference(newKey,
                k -> metaStream);

        catalog.set("Metadata", ref);
        result.addWarning("meta.1", "Created XMP metadata stream from DocInfo", "catalog/Metadata", null);
    }

    /**
     * Removes any filter (compression) from the catalog metadata stream.
     * <p>
     * PDF/A requires that the XMP metadata stream be stored uncompressed so that
     * simple byte-level search tools can locate the {@code pdfaid} namespace.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeMetadataFilter(PDFParser parser, PdfFormat format,
                                     ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase metaRef = catalog.get("Metadata");
        if (metaRef == null) {
            return;
        }
        COSBase metaObj = parser.resolveReference(metaRef);
        if (!(metaObj instanceof COSStream)) {
            return;
        }
        COSStream metaStream = (COSStream) metaObj;
        if (!metaStream.getFilters().isEmpty()) {
            LOG.info("Removing filter from metadata stream");
            // Get decoded data first, then store uncompressed
            byte[] decoded = metaStream.getDecodedData();
            metaStream.setDecodedData(decoded);
            metaStream.set(COSName.FILTER, null);
            metaStream.set("DecodeParms", null);
            result.addWarning("meta.2", "Removed compression from metadata stream", "catalog/Metadata", null);
        }
    }

    /**
     * Ensures the XMP metadata contains the correct {@code pdfaid:part} and
     * {@code pdfaid:conformance} values for the target PDF/A profile.
     * <p>
     * If the existing XMP already contains these elements but with wrong values,
     * the entire packet is regenerated.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void ensurePdfAId(PDFParser parser, PdfFormat format,
                             ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        if (!format.isPdfA()) {
            return;
        }

        COSDictionary catalog = parser.getCatalog();
        COSBase metaRef = catalog.get("Metadata");
        if (metaRef == null) {
            return;
        }
        COSBase metaObj = parser.resolveReference(metaRef);
        if (!(metaObj instanceof COSStream)) {
            return;
        }
        COSStream metaStream = (COSStream) metaObj;
        byte[] xmpData = metaStream.getDecodedData();
        String xmp = new String(xmpData, StandardCharsets.UTF_8);

        String partStr = String.valueOf(format.getPart());
        String confStr = format.getConformance();
        if (confStr == null) {
            confStr = "B";
        }

        // Check if pdfaid namespace is present with correct values
        boolean hasPart = xmp.contains("pdfaid:part>") && xmp.contains(">" + partStr + "<");
        boolean hasConf = xmp.contains("pdfaid:conformance>") && xmp.contains(">" + confStr + "<");

        if (!hasPart || !hasConf) {
            LOG.info("Updating pdfaid:part=" + partStr + " pdfaid:conformance=" + confStr);

            // If the XMP has the pdfaid namespace, try to patch it; otherwise rebuild
            if (xmp.contains("pdfaid:part")) {
                // Replace existing part value
                xmp = xmp.replaceAll(
                        "(<pdfaid:part>)[^<]*(</pdfaid:part>)",
                        "$1" + partStr + "$2");
            } else {
                // Insert pdfaid block before closing </rdf:Description> or </rdf:RDF>
                String pdfaidBlock = "\n    <rdf:Description rdf:about=\"\"\n"
                        + "        xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\">\n"
                        + "      <pdfaid:part>" + partStr + "</pdfaid:part>\n"
                        + "      <pdfaid:conformance>" + confStr + "</pdfaid:conformance>\n"
                        + "    </rdf:Description>";
                int insertPos = xmp.lastIndexOf("</rdf:RDF>");
                if (insertPos >= 0) {
                    xmp = xmp.substring(0, insertPos) + pdfaidBlock + "\n  " + xmp.substring(insertPos);
                }
            }

            if (xmp.contains("pdfaid:conformance")) {
                xmp = xmp.replaceAll(
                        "(<pdfaid:conformance>)[^<]*(</pdfaid:conformance>)",
                        "$1" + confStr + "$2");
            }

            metaStream.setDecodedData(xmp.getBytes(StandardCharsets.UTF_8));
            metaStream.set(COSName.FILTER, null);
            result.addWarning("meta.3", "Updated pdfaid:part and pdfaid:conformance in XMP",
                    "catalog/Metadata", null);
        }
    }

    /**
     * Synchronises the classic {@code /Info} dictionary with XMP metadata.
     * <p>
     * If the Info dictionary exists, its values are considered authoritative and
     * are used to update the XMP packet.  This ensures that readers which look at
     * either location see consistent values.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void syncDocInfoWithXmp(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary trailer = parser.getTrailer();
        COSBase infoRef = trailer.get("Info");
        if (infoRef == null) {
            return;
        }
        COSBase infoObj = parser.resolveReference(infoRef);
        if (!(infoObj instanceof COSDictionary)) {
            return;
        }

        COSDictionary catalog = parser.getCatalog();
        COSBase metaRef = catalog.get("Metadata");
        if (metaRef == null) {
            return;
        }
        COSBase metaObj = parser.resolveReference(metaRef);
        if (!(metaObj instanceof COSStream)) {
            return;
        }

        COSDictionary info = (COSDictionary) infoObj;
        COSStream metaStream = (COSStream) metaObj;
        byte[] xmpData = metaStream.getDecodedData();
        String xmp = new String(xmpData, StandardCharsets.UTF_8);

        // Sync Producer
        String producer = info.getString("Producer");
        if (producer != null && !producer.isEmpty()) {
            xmp = replaceOrInsertXmpProperty(xmp, "pdf:Producer", producer);
        }

        // Sync Creator (Author -> dc:creator)
        String author = info.getString("Author");
        if (author != null && !author.isEmpty()) {
            // dc:creator is an rdf:Seq, but for simple sync we check if it's present
            if (!xmp.contains("dc:creator")) {
                String creatorBlock = "<dc:creator><rdf:Seq><rdf:li>" + escapeXml(author)
                        + "</rdf:li></rdf:Seq></dc:creator>";
                xmp = insertPropertyInDescription(xmp, "dc", creatorBlock);
            }
        }

        // Sync Title -> dc:title
        String title = info.getString("Title");
        if (title != null && !title.isEmpty() && !xmp.contains("dc:title")) {
            String titleBlock = "<dc:title><rdf:Alt><rdf:li xml:lang=\"x-default\">"
                    + escapeXml(title) + "</rdf:li></rdf:Alt></dc:title>";
            xmp = insertPropertyInDescription(xmp, "dc", titleBlock);
        }

        metaStream.setDecodedData(xmp.getBytes(StandardCharsets.UTF_8));
        metaStream.set(COSName.FILTER, null);
        LOG.fine("Synchronized DocInfo with XMP metadata");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a complete XMP packet from the supplied document properties.
     *
     * @param title       document title
     * @param creator     author/creator
     * @param desc        description/subject
     * @param keywords    keywords
     * @param creatorTool creator tool
     * @param producer    producer
     * @param createDate  creation date in PDF date format (D:YYYYMMDDHHmmSS...)
     * @param modDate     modification date in PDF date format
     * @param part        pdfaid:part (1, 2, 3)
     * @param conformance pdfaid:conformance (A, B, U)
     * @return the XMP packet as UTF-8 bytes
     */
    static byte[] buildXmpPacket(String title, String creator, String desc, String keywords,
                                 String creatorTool, String producer,
                                 String createDate, String modDate,
                                 int part, String conformance) {
        String xmpCreateDate = pdfDateToXmpDate(createDate);
        String xmpModDate = pdfDateToXmpDate(modDate);

        StringBuilder sb = new StringBuilder(2048);
        sb.append("<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n");
        sb.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
        sb.append("  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");

        // Dublin Core
        sb.append("    <rdf:Description rdf:about=\"\"\n");
        sb.append("        xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n");
        if (!title.isEmpty()) {
            sb.append("      <dc:title><rdf:Alt><rdf:li xml:lang=\"x-default\">");
            sb.append(escapeXml(title));
            sb.append("</rdf:li></rdf:Alt></dc:title>\n");
        }
        if (!creator.isEmpty()) {
            sb.append("      <dc:creator><rdf:Seq><rdf:li>");
            sb.append(escapeXml(creator));
            sb.append("</rdf:li></rdf:Seq></dc:creator>\n");
        }
        if (!desc.isEmpty()) {
            sb.append("      <dc:description><rdf:Alt><rdf:li xml:lang=\"x-default\">");
            sb.append(escapeXml(desc));
            sb.append("</rdf:li></rdf:Alt></dc:description>\n");
        }
        sb.append("    </rdf:Description>\n");

        // XMP Basic
        sb.append("    <rdf:Description rdf:about=\"\"\n");
        sb.append("        xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\">\n");
        if (!creatorTool.isEmpty()) {
            sb.append("      <xmp:CreatorTool>").append(escapeXml(creatorTool)).append("</xmp:CreatorTool>\n");
        }
        if (!xmpCreateDate.isEmpty()) {
            sb.append("      <xmp:CreateDate>").append(xmpCreateDate).append("</xmp:CreateDate>\n");
        }
        if (!xmpModDate.isEmpty()) {
            sb.append("      <xmp:ModifyDate>").append(xmpModDate).append("</xmp:ModifyDate>\n");
        }
        sb.append("    </rdf:Description>\n");

        // PDF properties
        sb.append("    <rdf:Description rdf:about=\"\"\n");
        sb.append("        xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\">\n");
        if (!producer.isEmpty()) {
            sb.append("      <pdf:Producer>").append(escapeXml(producer)).append("</pdf:Producer>\n");
        }
        if (!keywords.isEmpty()) {
            sb.append("      <pdf:Keywords>").append(escapeXml(keywords)).append("</pdf:Keywords>\n");
        }
        sb.append("    </rdf:Description>\n");

        // PDF/A identification
        if (part > 0) {
            sb.append("    <rdf:Description rdf:about=\"\"\n");
            sb.append("        xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\">\n");
            sb.append("      <pdfaid:part>").append(part).append("</pdfaid:part>\n");
            if (conformance != null && !conformance.isEmpty()) {
                sb.append("      <pdfaid:conformance>").append(conformance).append("</pdfaid:conformance>\n");
            }
            sb.append("    </rdf:Description>\n");
        }

        sb.append("  </rdf:RDF>\n");
        sb.append("</x:xmpmeta>\n");

        // Padding (PDF/A recommends ~2KB padding for in-place updates)
        for (int i = 0; i < 20; i++) {
            sb.append("                                                                                \n");
        }
        sb.append("<?xpacket end=\"w\"?>");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converts a PDF date string (D:YYYYMMDDHHmmSS+HH'mm') to ISO 8601 XMP date.
     *
     * @param pdfDate the PDF date string
     * @return the XMP date string, or empty string if the input is empty/null
     */
    static String pdfDateToXmpDate(String pdfDate) {
        if (pdfDate == null || pdfDate.isEmpty()) {
            return "";
        }
        String s = pdfDate;
        if (s.startsWith("D:")) {
            s = s.substring(2);
        }
        if (s.length() < 4) {
            return "";
        }

        // Parse YYYY[MM[DD[HH[mm[SS]]]]][+/-HH'mm']
        String year = s.substring(0, 4);
        String month = s.length() >= 6 ? s.substring(4, 6) : "01";
        String day = s.length() >= 8 ? s.substring(6, 8) : "01";
        String hour = s.length() >= 10 ? s.substring(8, 10) : "00";
        String min = s.length() >= 12 ? s.substring(10, 12) : "00";
        String sec = s.length() >= 14 ? s.substring(12, 14) : "00";

        StringBuilder result = new StringBuilder();
        result.append(year).append('-').append(month).append('-').append(day);
        result.append('T').append(hour).append(':').append(min).append(':').append(sec);

        // Timezone
        if (s.length() > 14) {
            String tz = s.substring(14);
            if (tz.startsWith("Z")) {
                result.append("Z");
            } else if (tz.startsWith("+") || tz.startsWith("-")) {
                result.append(tz.charAt(0));
                String rest = tz.substring(1).replace("'", "");
                if (rest.length() >= 2) {
                    result.append(rest, 0, 2);
                    result.append(':');
                    if (rest.length() >= 4) {
                        result.append(rest, 2, 4);
                    } else {
                        result.append("00");
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * Returns the current time as a PDF date string.
     */
    private static String currentPdfDate() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        return "D:" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * Returns a non-null string (empty string for null input).
     */
    private static String safeString(String s) {
        return s == null ? "" : s;
    }

    /**
     * Finds the maximum object number currently in the parser.
     */
    private static int findMaxObjectNumber(PDFParser parser) {
        int maxObj = 0;
        for (COSObjectKey k : parser.getAllObjectKeys()) {
            maxObj = Math.max(maxObj, k.getObjectNumber());
        }
        return maxObj;
    }

    /**
     * Escapes XML special characters.
     */
    static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Replaces or inserts a simple XMP property value.
     */
    private static String replaceOrInsertXmpProperty(String xmp, String propName, String value) {
        String openTag = "<" + propName + ">";
        String closeTag = "</" + propName + ">";
        int startIdx = xmp.indexOf(openTag);
        if (startIdx >= 0) {
            int endIdx = xmp.indexOf(closeTag, startIdx);
            if (endIdx >= 0) {
                return xmp.substring(0, startIdx + openTag.length())
                        + escapeXml(value)
                        + xmp.substring(endIdx);
            }
        }
        return xmp; // leave unchanged if can't find
    }

    /**
     * Inserts a property block into an rdf:Description that uses the given namespace prefix.
     */
    private static String insertPropertyInDescription(String xmp, String nsPrefix, String propBlock) {
        // Try to insert before the closing tag of an rdf:Description that contains the namespace prefix
        String nsSearch = "xmlns:" + nsPrefix + "=";
        int nsIdx = xmp.indexOf(nsSearch);
        if (nsIdx >= 0) {
            // Find the corresponding </rdf:Description>
            int closeIdx = xmp.indexOf("</rdf:Description>", nsIdx);
            if (closeIdx >= 0) {
                return xmp.substring(0, closeIdx) + "      " + propBlock + "\n    "
                        + xmp.substring(closeIdx);
            }
        }
        // If the namespace block doesn't exist, insert before </rdf:RDF>
        int rdfClose = xmp.lastIndexOf("</rdf:RDF>");
        if (rdfClose >= 0) {
            return xmp.substring(0, rdfClose)
                    + "    <rdf:Description rdf:about=\"\"\n"
                    + "        xmlns:" + nsPrefix + "=\"http://purl.org/dc/elements/1.1/\">\n"
                    + "      " + propBlock + "\n"
                    + "    </rdf:Description>\n  "
                    + xmp.substring(rdfClose);
        }
        return xmp;
    }
}
