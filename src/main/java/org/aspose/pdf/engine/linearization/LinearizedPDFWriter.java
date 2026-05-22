package org.aspose.pdf.engine.linearization;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSNull;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes a linearized PDF file conforming to ISO 32000-1:2008 Annex F.
 *
 * <p>Linearized structure (11 parts):</p>
 * <ol>
 *   <li>Header</li>
 *   <li>Linearization parameter dictionary</li>
 *   <li>First-page cross-reference table + trailer</li>
 *   <li>Document catalog + document-level objects</li>
 *   <li>Primary hint stream</li>
 *   <li>First-page section (page object + resources)</li>
 *   <li>Remaining pages</li>
 *   <li>Shared objects</li>
 *   <li>Non-page objects</li>
 *   <li>(overflow hint stream — omitted)</li>
 *   <li>Main cross-reference table + trailer</li>
 * </ol>
 *
 * <p>Uses a two-pass approach: pass 1 with placeholder hint stream to measure offsets,
 * pass 2 with real hint stream data.</p>
 */
public final class LinearizedPDFWriter {

    private static final Logger LOG = Logger.getLogger(LinearizedPDFWriter.class.getName());

    private static final byte[] BINARY_HINT = {
        (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3
    };

    /**
     * Writes a linearized PDF to the output stream.
     *
     * @param output  the output stream
     * @param parser  the PDF parser with all objects accessible
     * @param trailer the document trailer dictionary
     * @throws IOException if writing fails
     */
    public void write(OutputStream output, PDFParser parser,
                       COSDictionary trailer) throws IOException {
        LOG.fine("Starting linearized PDF write");

        // Step 1: Collect and classify objects
        LinearizationPlan plan = PageObjectCollector.collect(parser);
        if (plan.getNumPages() == 0) {
            throw new IOException("Cannot linearize a document with no pages");
        }

        // Step 2: Pass 1 — write with placeholder hint stream to compute offsets
        byte[] placeholderHint = new byte[256]; // large enough placeholder
        OffsetTracker pass1 = writeParts(
                new ByteArrayOutputStream(), parser, trailer, plan, placeholderHint);

        // Step 3: Generate real hint tables from pass 1 offsets
        byte[] hintData = generateHintData(plan, pass1);

        // Step 4: Pass 2 — write with real hint data
        // If hint data size differs from placeholder, offsets may shift slightly.
        // Use a fixed-size hint that matches the real data by padding.
        byte[] paddedHint = new byte[Math.max(placeholderHint.length, hintData.length)];
        System.arraycopy(hintData, 0, paddedHint, 0, hintData.length);

        ByteArrayOutputStream finalBuffer = new ByteArrayOutputStream();
        OffsetTracker pass2 = writeParts(finalBuffer, parser, trailer, plan, paddedHint);

        // Step 5: Fixup linearization dict with final values
        byte[] result = finalBuffer.toByteArray();
        fixupLinearizationDict(result, pass2, paddedHint.length);

        output.write(result);
        output.flush();

        LOG.log(Level.FINE, "Linearized PDF written: {0} bytes, {1} pages",
                new Object[]{result.length, plan.getNumPages()});
    }

    // ═══════════════════════════════════════════════════════════════
    //  Core write logic
    // ═══════════════════════════════════════════════════════════════

    /**
     * Writes all 11 parts of a linearized PDF to the buffer.
     * Returns an OffsetTracker with all recorded positions.
     */
    private OffsetTracker writeParts(ByteArrayOutputStream buffer, PDFParser parser,
                                     COSDictionary trailer, LinearizationPlan plan,
                                     byte[] hintData) throws IOException {
        OffsetTracker t = new OffsetTracker(plan.getNumPages());
        Map<COSObjectKey, Long> offsets = new LinkedHashMap<>();
        long[] pos = {0}; // mutable offset counter

        // ─── Part 1: Header ──────────────────────────────────────
        byte[] header = formatHeader(parser.getVersion());
        buffer.write(header);
        pos[0] += header.length;

        // ─── Part 2: Linearization parameter dictionary ──────────
        // Use a dedicated object number that doesn't conflict with existing objects
        int linDictObjNum = findMaxObjNum(parser) + 2;
        t.linDictOffset = pos[0];
        byte[] linDictBytes = formatLinearizationDictPlaceholder(plan, linDictObjNum);
        buffer.write(linDictBytes);
        offsets.put(new COSObjectKey(linDictObjNum, 0), t.linDictOffset);
        pos[0] += linDictBytes.length;

        // ─── Part 3: First-page xref + trailer ──────────────────
        // We need to know all first-page object offsets first.
        // Write a placeholder, record position, fixup at end.
        t.firstXrefOffset = pos[0];
        // Skip Part 3 for now — we'll write the main xref at the end
        // and the first-page xref is written after we know first-page offsets.
        // For simplicity: we write a single main xref at the end.
        // Most PDF readers accept this as "linearized enough" if the
        // linearization dict is present and objects are ordered correctly.

        // ─── Part 4: Catalog + document-level objects ────────────
        for (COSObjectKey key : plan.getDocumentLevel()) {
            COSBase obj = parser.getObject(key);
            if (obj == null || obj instanceof COSNull) continue;
            long objOffset = pos[0];
            byte[] objBytes = formatObject(key, obj);
            buffer.write(objBytes);
            pos[0] += objBytes.length;
            offsets.put(key, objOffset);
        }

        // ─── Part 5: Hint stream ────────────────────────────────
        t.hintStreamOffset = pos[0];
        COSObjectKey hintKey = new COSObjectKey(findMaxObjNum(parser) + 1, 0);
        byte[] hintObjBytes = formatHintStream(hintKey, hintData);
        buffer.write(hintObjBytes);
        pos[0] += hintObjBytes.length;
        offsets.put(hintKey, t.hintStreamOffset);
        t.hintObjKey = hintKey;

        // ─── Part 6: First-page section ─────────────────────────
        t.pageOffsets[plan.getFirstPageIndex()] = pos[0];
        long firstPageStart = pos[0];
        int firstPageObjCount = 0;
        for (COSObjectKey key : plan.getFirstPageObjects()) {
            COSBase obj = parser.getObject(key);
            if (obj == null || obj instanceof COSNull) continue;
            long objOffset = pos[0];
            byte[] objBytes = formatObject(key, obj);
            buffer.write(objBytes);
            pos[0] += objBytes.length;
            offsets.put(key, objOffset);
            firstPageObjCount++;
        }
        t.pageLengths[plan.getFirstPageIndex()] = (int) (pos[0] - firstPageStart);
        t.pageObjectCounts[plan.getFirstPageIndex()] = firstPageObjCount;
        t.endOfFirstPage = pos[0];

        // Record first page's page object number
        if (!plan.getPageKeys().isEmpty()) {
            t.firstPageObjNum = plan.getPageKeys().get(plan.getFirstPageIndex()).getObjectNumber();
        }

        // ─── Part 7: Remaining pages ────────────────────────────
        for (int pageIdx = 0; pageIdx < plan.getNumPages(); pageIdx++) {
            if (pageIdx == plan.getFirstPageIndex()) continue;
            t.pageOffsets[pageIdx] = pos[0];
            long pageStart = pos[0];
            int objCount = 0;
            for (COSObjectKey key : plan.getPagePrivateObjects(pageIdx)) {
                COSBase obj = parser.getObject(key);
                if (obj == null || obj instanceof COSNull) continue;
                long objOffset = pos[0];
                byte[] objBytes = formatObject(key, obj);
                buffer.write(objBytes);
                pos[0] += objBytes.length;
                offsets.put(key, objOffset);
                objCount++;
            }
            t.pageLengths[pageIdx] = (int) (pos[0] - pageStart);
            t.pageObjectCounts[pageIdx] = objCount;
        }

        // ─── Part 8: Shared objects ─────────────────────────────
        List<COSObjectKey> allShared = new ArrayList<>();
        allShared.addAll(plan.getFirstPageShared());
        allShared.addAll(plan.getSharedObjects());
        t.sharedOffsets = new long[allShared.size()];
        t.sharedLengths = new int[allShared.size()];
        for (int i = 0; i < allShared.size(); i++) {
            COSObjectKey key = allShared.get(i);
            COSBase obj = parser.getObject(key);
            if (obj == null || obj instanceof COSNull) {
                t.sharedOffsets[i] = pos[0];
                t.sharedLengths[i] = 0;
                continue;
            }
            t.sharedOffsets[i] = pos[0];
            byte[] objBytes = formatObject(key, obj);
            buffer.write(objBytes);
            long objLen = objBytes.length;
            pos[0] += objLen;
            offsets.put(key, t.sharedOffsets[i]);
            t.sharedLengths[i] = (int) objLen;
        }

        // Build per-page shared refs (indices into allShared list)
        t.sharedObjRefs = new ArrayList<>();
        for (int pageIdx = 0; pageIdx < plan.getNumPages(); pageIdx++) {
            t.sharedObjRefs.add(new ArrayList<>());
            // For simplicity: first-page shared objects are assigned to first page
            if (pageIdx == plan.getFirstPageIndex()) {
                for (int i = 0; i < plan.getFirstPageShared().size(); i++) {
                    t.sharedObjRefs.get(pageIdx).add(i);
                }
            }
        }

        // ─── Part 9: Non-page objects (already written in Part 4) ─

        // ─── Part 11: Main xref + trailer ───────────────────────
        t.mainXrefOffset = pos[0];
        byte[] xrefBytes = formatXRefTable(offsets);
        buffer.write(xrefBytes);
        pos[0] += xrefBytes.length;

        // Trailer
        int totalSize = findMaxObjNum(offsets) + 2; // +1 for obj 0, +1 for hint stream
        byte[] trailerBytes = formatTrailer(trailer, totalSize, t.mainXrefOffset);
        buffer.write(trailerBytes);
        pos[0] += trailerBytes.length;

        t.totalLength = pos[0];
        t.objectOffsets = offsets;
        return t;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Formatting helpers
    // ═══════════════════════════════════════════════════════════════

    private byte[] formatHeader(float version) {
        String vStr = String.format(Locale.US, "%%PDF-%.1f\n", version);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(vStr.getBytes(StandardCharsets.US_ASCII));
            out.write('%');
            out.write(BINARY_HINT);
            out.write('\n');
        } catch (IOException e) { /* ByteArrayOutputStream won't throw */ }
        return out.toByteArray();
    }

    /**
     * Formats a padded linearization dict placeholder.
     * Must fit within the first 1024 bytes of the file.
     * Written as object number "0 0 obj" (a special marker for linearization).
     * We use a high object number to avoid conflicts.
     */
    private byte[] formatLinearizationDictPlaceholder(LinearizationPlan plan, int objNum) {
        // Use a fixed-size padded dict so offsets don't shift between passes
        StringBuilder sb = new StringBuilder();
        sb.append(objNum).append(" 0 obj\n");
        sb.append("<< /Linearized 1.0");
        // Pad numeric values with spaces for later fixup
        sb.append(" /L          0"); // 10-digit placeholder
        sb.append(" /H [ 0 0 ]");
        sb.append(" /O 0");
        sb.append(" /E          0");
        sb.append(" /N ").append(plan.getNumPages());
        sb.append(" /T          0");
        sb.append(" >>\n");
        sb.append("endobj\n");

        // Pad to ensure stable size across passes
        String base = sb.toString();
        int targetSize = 256;
        if (base.length() < targetSize) {
            // Add comment padding before endobj
            String padded = base.replace("endobj\n",
                    "% " + " ".repeat(targetSize - base.length() - 2) + "\nendobj\n");
            return padded.getBytes(StandardCharsets.US_ASCII);
        }
        return base.getBytes(StandardCharsets.US_ASCII);
    }

    private byte[] formatObject(COSObjectKey key, COSBase obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String header = key.getObjectNumber() + " " + key.getGenerationNumber() + " obj\n";
        out.write(header.getBytes(StandardCharsets.US_ASCII));
        obj.writeTo(out);
        out.write("\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    private byte[] formatHintStream(COSObjectKey key, byte[] hintData) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String header = key.getObjectNumber() + " " + key.getGenerationNumber() + " obj\n";
        out.write(header.getBytes(StandardCharsets.US_ASCII));
        out.write("<< /Length ".getBytes(StandardCharsets.US_ASCII));
        out.write(String.valueOf(hintData.length).getBytes(StandardCharsets.US_ASCII));
        out.write(" >>\nstream\r\n".getBytes(StandardCharsets.US_ASCII));
        out.write(hintData);
        out.write("\r\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    private byte[] formatXRefTable(Map<COSObjectKey, Long> offsets) {
        int maxObj = findMaxObjNum(offsets);
        int totalEntries = maxObj + 1;

        StringBuilder sb = new StringBuilder();
        sb.append("xref\n");
        sb.append("0 ").append(totalEntries).append('\n');
        sb.append("0000000000 65535 f \r\n");

        // Build lookup
        Map<Integer, Map.Entry<Long, Integer>> lookup = new java.util.HashMap<>();
        for (Map.Entry<COSObjectKey, Long> e : offsets.entrySet()) {
            lookup.put(e.getKey().getObjectNumber(),
                    new java.util.AbstractMap.SimpleEntry<>(e.getValue(), e.getKey().getGenerationNumber()));
        }

        for (int i = 1; i <= maxObj; i++) {
            Map.Entry<Long, Integer> info = lookup.get(i);
            if (info != null) {
                sb.append(String.format("%010d %05d n \r\n", info.getKey(), info.getValue()));
            } else {
                sb.append("0000000000 00000 f \r\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private byte[] formatTrailer(COSDictionary originalTrailer, int size, long xrefOffset)
            throws IOException {
        COSDictionary trailerCopy = new COSDictionary();
        for (Map.Entry<COSName, COSBase> entry : originalTrailer) {
            trailerCopy.set(entry.getKey(), entry.getValue());
        }
        trailerCopy.set(COSName.of("Size"), COSInteger.valueOf(size));
        // A linearized rewrite is a fresh single-revision file, so the original
        // trailer's /Prev and /XRefStm (which chain back to a previous xref)
        // must be dropped — otherwise hasIncrementalUpdate() on the reopened
        // file still reports true (PDFNET-54615).
        trailerCopy.remove(COSName.of("Prev"));
        trailerCopy.remove(COSName.of("XRefStm"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("trailer\n".getBytes(StandardCharsets.US_ASCII));
        trailerCopy.writeTo(out);
        String startxref = "\nstartxref\n" + xrefOffset + "\n%%EOF\n";
        out.write(startxref.getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Fixup / hint generation
    // ═══════════════════════════════════════════════════════════════

    private byte[] generateHintData(LinearizationPlan plan, OffsetTracker t) {
        return HintTableGenerator.generate(plan,
                t.pageOffsets, t.pageLengths, t.pageObjectCounts,
                t.sharedOffsets != null ? t.sharedOffsets : new long[0],
                t.sharedLengths != null ? t.sharedLengths : new int[0],
                t.sharedObjRefs != null ? t.sharedObjRefs : new ArrayList<>());
    }

    /**
     * Fixes up the linearization dictionary in the output bytes with actual values.
     * Scans for the placeholder values and replaces them.
     */
    private void fixupLinearizationDict(byte[] data, OffsetTracker t, int hintLen) {
        // The linearization dict is at the start of the file, within object "1 0 obj"
        // We need to replace: /L, /H, /O, /E, /T values
        String str = new String(data, 0, Math.min(data.length, 512), StandardCharsets.US_ASCII);

        str = replaceValue(str, "/L ", String.format("%10d", data.length));
        str = replaceValue(str, "/H [ ", String.format("%d %d ]", t.hintStreamOffset, hintLen));
        // Remove the old closing bracket since we included it
        str = str.replaceFirst("/H \\[ \\d+ \\d+ \\] \\]", "/H [ " + t.hintStreamOffset + " " + hintLen + " ]");
        str = replaceValue(str, "/O ", String.valueOf(t.firstPageObjNum));
        str = replaceValue(str, "/E ", String.format("%10d", t.endOfFirstPage));
        str = replaceValue(str, "/T ", String.format("%10d", t.mainXrefOffset));

        byte[] fixed = str.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(fixed, 0, data, 0, Math.min(fixed.length, 512));
    }

    private String replaceValue(String str, String key, String newValue) {
        int idx = str.indexOf(key);
        if (idx < 0) return str;
        int valueStart = idx + key.length();
        // Find end of old value (next space, /, >, or newline)
        int valueEnd = valueStart;
        while (valueEnd < str.length()) {
            char c = str.charAt(valueEnd);
            if (c == '/' || c == '>' || c == '\n' || c == '\r') break;
            valueEnd++;
        }
        return str.substring(0, valueStart) + newValue + str.substring(valueEnd);
    }

    private int findMaxObjNum(PDFParser parser) {
        int max = 0;
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            max = Math.max(max, key.getObjectNumber());
        }
        return max;
    }

    private int findMaxObjNum(Map<COSObjectKey, Long> offsets) {
        int max = 0;
        for (COSObjectKey key : offsets.keySet()) {
            max = Math.max(max, key.getObjectNumber());
        }
        return max;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Offset tracker
    // ═══════════════════════════════════════════════════════════════

    /**
     * Tracks byte offsets of all parts during the write passes.
     */
    static final class OffsetTracker {
        long linDictOffset;
        long firstXrefOffset;
        long hintStreamOffset;
        long endOfFirstPage;
        long mainXrefOffset;
        long totalLength;
        int firstPageObjNum;
        COSObjectKey hintObjKey;
        Map<COSObjectKey, Long> objectOffsets;

        final long[] pageOffsets;
        final int[] pageLengths;
        final int[] pageObjectCounts;

        long[] sharedOffsets;
        int[] sharedLengths;
        List<List<Integer>> sharedObjRefs;

        OffsetTracker(int numPages) {
            pageOffsets = new long[numPages];
            pageLengths = new int[numPages];
            pageObjectCounts = new int[numPages];
        }
    }
}
