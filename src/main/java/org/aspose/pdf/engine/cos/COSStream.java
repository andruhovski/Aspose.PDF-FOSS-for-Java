package org.aspose.pdf.engine.cos;

import org.aspose.pdf.engine.filter.FilterFactory;
import org.aspose.pdf.engine.security.PDFDecryptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * PDF stream object (§7.3.8, ISO 32000-1:2008).
 * <p>
 * A dictionary plus a sequence of bytes. Streams are always indirect objects.
 * The bytes may be encoded (compressed) via one or more filters specified in /Filter.
 * Provides lazy decoding with SoftReference caching.
 * </p>
 */
public class COSStream extends COSDictionary {

    private static final Logger LOG = Logger.getLogger(COSStream.class.getName());

    private static final byte[] STREAM_KEYWORD = "stream".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ENDSTREAM_KEYWORD = "endstream".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] CRLF = {'\r', '\n'};
    private static final byte[] LF = {'\n'};

    /** Encoded data (as stored in the PDF file). */
    private byte[] encodedData;

    /** Decoded data (lazy, cached via SoftReference). */
    private SoftReference<byte[]> decodedData = new SoftReference<>(null);

    /** Pending decoded data that needs encoding on write. */
    private byte[] pendingDecodedData;

    /** Decryptor for encrypted PDFs (set by parser). */
    private PDFDecryptor decryptor;
    /** Object number for decryption context. */
    private int decryptObjNum;
    /** Generation number for decryption context. */
    private int decryptGenNum;

    /**
     * Creates an empty stream.
     */
    public COSStream() {
        this.encodedData = new byte[0];
    }

    /**
     * Creates a stream with the given encoded data.
     *
     * @param encodedData the encoded bytes
     */
    public COSStream(byte[] encodedData) {
        this.encodedData = encodedData != null ? encodedData.clone() : new byte[0];
    }

    /**
     * Creates a stream from an existing dictionary and encoded data.
     * The dictionary entries are copied into this stream's dictionary.
     *
     * @param dict        the dictionary with stream metadata
     * @param encodedData the encoded bytes
     */
    public COSStream(COSDictionary dict, byte[] encodedData) {
        if (dict != null) {
            this.map.putAll(dict.map);
        }
        this.encodedData = encodedData != null ? encodedData.clone() : new byte[0];
    }

    /**
     * Returns this stream as a dictionary. Since COSStream extends COSDictionary,
     * this method returns {@code this}.
     *
     * @return this stream (which is also a dictionary)
     */
    public COSDictionary getDictionary() {
        return this;
    }

    /**
     * Returns the encoded data (as stored in the PDF file).
     *
     * @return the encoded bytes
     */
    public byte[] getEncodedData() {
        return encodedData != null ? encodedData.clone() : new byte[0];
    }

    /**
     * Returns the encoded data as an InputStream.
     *
     * @return input stream over encoded data
     */
    public InputStream getEncodedStream() {
        return new ByteArrayInputStream(encodedData != null ? encodedData : new byte[0]);
    }

    /**
     * Returns the decoded data (after applying filters in reverse order).
     * Results are cached via SoftReference.
     *
     * @return the decoded bytes
     * @throws IOException if decoding fails
     */
    public byte[] getDecodedData() throws IOException {
        // If we have pending decoded data, return it directly
        if (pendingDecodedData != null) {
            return pendingDecodedData.clone();
        }

        // Check cache
        byte[] cached = decodedData.get();
        if (cached != null) {
            return cached.clone();
        }

        // Decrypt if needed (before filter decompression)
        byte[] raw = encodedData != null ? encodedData : new byte[0];
        if (decryptor != null && decryptor.isActive()) {
            raw = decryptor.decrypt(raw, decryptObjNum, decryptGenNum);
        }

        // No filters → raw == decoded
        List<COSName> filters = getFilters();
        if (filters.isEmpty()) {
            decodedData = new SoftReference<>(raw);
            return raw.clone();
        }

        // Decode through filter chain
        byte[] decoded = decodeWithFilters(raw, filters);
        decodedData = new SoftReference<>(decoded);
        return decoded.clone();
    }

    /**
     * Returns the decoded data as an InputStream.
     *
     * @return input stream over decoded data
     * @throws IOException if decoding fails
     */
    public InputStream getDecodedStream() throws IOException {
        return new ByteArrayInputStream(getDecodedData());
    }

    /**
     * Sets the decoded data. The data will be encoded when written.
     *
     * @param data the decoded bytes
     */
    public void setDecodedData(byte[] data) {
        this.pendingDecodedData = data != null ? data.clone() : new byte[0];
        this.decodedData = new SoftReference<>(this.pendingDecodedData);
        // Invalidate encoded data — will be recomputed on write
        this.encodedData = null;
        markDirty();
    }

    /**
     * Sets the encoded data directly.
     *
     * @param data the encoded bytes
     */
    public void setEncodedData(byte[] data) {
        this.encodedData = data != null ? data.clone() : new byte[0];
        this.pendingDecodedData = null;
        this.decodedData = new SoftReference<>(null);
        markDirty();
    }

    /**
     * Returns true if this stream has an active decryptor attached (i.e. its
     * encoded bytes are ciphertext in the source document).
     */
    public boolean hasActiveDecryptor() {
        return decryptor != null && decryptor.isActive();
    }

    /**
     * Decrypts {@code encodedData} in place and detaches the decryptor, leaving
     * the stream looking exactly like an equivalent unencrypted stream loaded
     * from disk.
     * <p>
     * This is the right primitive for {@link org.aspose.pdf.Document#decrypt()}:
     * it avoids touching the filter chain entirely, so streams whose filters we
     * can decode but not re-encode (JBIG2, CCITTFax, DCT, JPX) survive a
     * {@code decrypt → save} round-trip. Decoding+re-encoding via
     * {@link #setDecodedData(byte[])} would force a re-encode through every
     * filter, which throws {@code "JBIG2Decode encoding not implemented"} on
     * any image stream that uses JBIG2.
     * </p>
     *
     * @return true if anything was decrypted (i.e. a decryptor was attached and
     *         had encoded bytes to operate on); false if there was no active
     *         decryptor and the call was a no-op
     */
    public boolean materializeDecryption() {
        if (decryptor == null || !decryptor.isActive()) {
            return false;
        }
        if (encodedData != null && encodedData.length > 0) {
            encodedData = decryptor.decrypt(encodedData, decryptObjNum, decryptGenNum);
        }
        decryptor = null;
        decryptObjNum = 0;
        decryptGenNum = 0;
        // The cached decoded bytes (if any) were derived from a now-stale
        // encrypted-encoded source; drop them so getDecodedData re-runs
        // the (now decryptor-free) filter chain on the next call.
        decodedData = new SoftReference<>(null);
        markDirty();
        return true;
    }

    /**
     * Returns whether the stream has pending decoded data set via
     * {@link #setDecodedData(byte[])} that has not yet been re-encoded by
     * {@link #prepareEncodedData()}.
     * <p>
     * Used by the writer to distinguish freshly-modified content (that needs
     * re-encryption) from content that is still the original bytes loaded
     * from disk (which is already ciphertext under {@link #hasActiveDecryptor()}).
     * </p>
     *
     * @return true if {@link #setDecodedData(byte[])} was called and the new
     *         decoded bytes have not yet been re-encoded
     */
    public boolean hasPendingDecodedData() {
        return pendingDecodedData != null;
    }

    /**
     * Sets the decryptor for this stream (called by PDFParser for encrypted PDFs).
     *
     * @param decryptor the decryptor
     * @param objNum    the object number
     * @param genNum    the generation number
     */
    public void setDecryptor(PDFDecryptor decryptor, int objNum, int genNum) {
        this.decryptor = decryptor;
        this.decryptObjNum = objNum;
        this.decryptGenNum = genNum;
        // Invalidate decoded cache since we now need to decrypt
        this.decodedData = new SoftReference<>(null);
    }

    /**
     * Returns the length of the encoded data.
     *
     * @return the encoded data length
     */
    public long getLength() {
        if (encodedData != null) {
            return encodedData.length;
        }
        return 0;
    }

    /**
     * Returns the list of filter names from /Filter.
     *
     * @return the filter names (may be empty)
     */
    public List<COSName> getFilters() {
        COSBase filterObj = get(COSName.FILTER);
        if (filterObj == null) {
            return Collections.emptyList();
        }
        if (filterObj instanceof COSName) {
            return Collections.singletonList((COSName) filterObj);
        }
        if (filterObj instanceof COSArray) {
            COSArray arr = (COSArray) filterObj;
            List<COSName> result = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                COSBase item = arr.get(i);
                if (item instanceof COSName) {
                    result.add((COSName) item);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Sets a single filter.
     *
     * @param filter the filter name
     */
    public void setFilter(COSName filter) {
        if (filter == null) {
            set(COSName.FILTER, null);
        } else {
            set(COSName.FILTER, filter);
        }
    }

    /**
     * Sets multiple filters.
     *
     * @param filters the filter names
     */
    public void setFilters(List<COSName> filters) {
        if (filters == null || filters.isEmpty()) {
            set(COSName.FILTER, null);
        } else if (filters.size() == 1) {
            set(COSName.FILTER, filters.get(0));
        } else {
            COSArray arr = new COSArray(filters.size());
            for (COSName f : filters) {
                arr.add(f);
            }
            set(COSName.FILTER, arr);
        }
    }

    /**
     * Ensures pending decoded data is encoded through filters and returns the
     * encoded bytes. Used by PDFWriter for write-side encryption: the writer
     * needs the compressed bytes before encrypting them.
     *
     * @return the encoded data (compressed but not encrypted)
     * @throws IOException if filter encoding fails
     */
    public byte[] prepareEncodedData() throws IOException {
        if (pendingDecodedData != null && encodedData == null) {
            List<COSName> filters = getFilters();
            if (filters.isEmpty()) {
                encodedData = pendingDecodedData;
            } else {
                encodedData = encodeWithFilters(pendingDecodedData, filters);
            }
        }
        return encodedData != null ? encodedData.clone() : new byte[0];
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        // Prepare encoded data if we have pending decoded data
        prepareEncodedData();

        if (encodedData == null) {
            encodedData = new byte[0];
        }

        // Update /Length
        set(COSName.LENGTH, COSInteger.valueOf(encodedData.length));

        // Write dictionary
        super.writeTo(os);

        // Write stream data (§7.3.8.1: after "stream" must be CR+LF or LF only, not CR alone)
        os.write(LF);
        os.write(STREAM_KEYWORD);
        os.write(CRLF);
        os.write(encodedData);
        os.write(CRLF);
        os.write(ENDSTREAM_KEYWORD);
    }

    @Override
    public <T> T accept(ICOSVisitor<T> visitor) {
        return visitor.visitStream(this);
    }

    @Override
    public String toString() {
        return "COSStream{dictSize=" + map.size() + ", length=" + getLength() + "}";
    }

    /**
     * Decodes data through the filter chain using FilterFactory.
     * Filters are applied left-to-right per §7.4.1.
     */
    private byte[] decodeWithFilters(byte[] data, List<COSName> filters) throws IOException {
        LOG.fine(() -> "Decoding stream with " + filters.size() + " filter(s)");
        return FilterFactory.decodeChain(data, filters, getEffectiveDecodeParams(filters));
    }

    /**
     * Returns per-filter parameter dictionaries for the decode chain, augmenting
     * the raw {@code /DecodeParms} with image-dictionary fallbacks where the
     * filter expects them.
     *
     * <p>Specifically, {@link org.aspose.pdf.engine.filter.CCITTFaxDecodeFilter}
     * relies on {@code /Rows} to know when to stop decoding; PDF writers
     * routinely omit {@code /Rows} from {@code /DecodeParms} on the assumption
     * that consumers will fall back to the Image XObject's {@code /Height}.
     * We materialize that fallback here so the filter receives a self-contained
     * parameter dictionary.</p>
     */
    private List<COSDictionary> getEffectiveDecodeParams(List<COSName> filters) {
        List<COSDictionary> base = getDecodeParams();
        if (filters == null || filters.isEmpty()) return base;
        List<COSDictionary> result = new ArrayList<>(filters.size());
        for (int i = 0; i < filters.size(); i++) {
            COSName filterName = filters.get(i);
            COSDictionary parms = (base != null && i < base.size()) ? base.get(i) : null;
            if (filterName != null && "CCITTFaxDecode".equals(filterName.getName())) {
                COSDictionary augmented = new COSDictionary();
                if (parms != null) {
                    for (java.util.Map.Entry<COSName, COSBase> e : parms) {
                        augmented.set(e.getKey(), e.getValue());
                    }
                }
                // /Rows fallback ← /Height (image dict, §7.4.6 vs §8.9.5.1)
                if (augmented.get("Rows") == null) {
                    COSBase h = get("Height");
                    if (h != null) augmented.set("Rows", h);
                }
                // /Columns fallback ← /Width (some PDFs omit /Columns too)
                if (augmented.get("Columns") == null) {
                    COSBase w = get("Width");
                    if (w != null) augmented.set("Columns", w);
                }
                result.add(augmented);
            } else {
                result.add(parms);
            }
        }
        return result;
    }

    /**
     * Encodes data through the filter chain using FilterFactory.
     * Filters are applied right-to-left per §7.4.1.
     */
    private byte[] encodeWithFilters(byte[] data, List<COSName> filters) throws IOException {
        LOG.fine(() -> "Encoding stream with " + filters.size() + " filter(s)");
        return FilterFactory.encodeChain(data, filters, getEncodeParams(filters));
    }

    /**
     * Reads the /DecodeParms entry and returns it as a list of COSDictionary.
     * If absent, returns null. If a single dictionary, returns a singleton list.
     * If an array, iterates and casts each element.
     */
    private List<COSDictionary> getDecodeParams() {
        COSBase dp = get(COSName.DECODE_PARMS);
        if (dp == null) {
            return null;
        }
        if (dp instanceof COSDictionary) {
            return Collections.singletonList((COSDictionary) dp);
        }
        if (dp instanceof COSArray) {
            COSArray arr = (COSArray) dp;
            List<COSDictionary> result = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                COSBase item = arr.get(i);
                if (item instanceof COSDictionary) {
                    result.add((COSDictionary) item);
                } else {
                    result.add(null);
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Returns parameter dictionaries for write-side filter encoding.
     *
     * <p>For image filters, encode needs not only /DecodeParms but also stream
     * dictionary entries such as /Width, /Height, /BitsPerComponent and
     * /ColorSpace. We therefore merge each filter's decode-parameter dictionary
     * over a shallow copy of the stream dictionary.</p>
     */
    private List<COSDictionary> getEncodeParams(List<COSName> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }
        List<COSDictionary> decodeParams = getDecodeParams();
        List<COSDictionary> result = new ArrayList<>(filters.size());
        for (int i = 0; i < filters.size(); i++) {
            COSDictionary merged = new COSDictionary(this);
            if (decodeParams != null && i < decodeParams.size() && decodeParams.get(i) != null) {
                COSDictionary perFilter = decodeParams.get(i);
                for (java.util.Map.Entry<COSName, COSBase> entry : perFilter) {
                    merged.set(entry.getKey(), entry.getValue());
                }
            }
            result.add(merged);
        }
        return result;
    }
}
