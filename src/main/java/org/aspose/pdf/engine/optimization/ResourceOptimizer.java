package org.aspose.pdf.engine.optimization;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.optimization.OptimizationOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

/// Size-reduction passes over a parsed document's object graph, driven by
/// [OptimizationOptions]. Called by
/// `Document.optimizeResources(...)` BEFORE the compact full rewrite,
/// so objects orphaned by a pass (e.g. duplicate streams whose references
/// were re-pointed) are dropped by the rewrite's reachability pass.
///
/// Every pass is safe-by-default: any stream that cannot be read, or whose
/// replacement would not be smaller, is left untouched. Passes only mutate
/// the live object graph (dictionaries, arrays, stream data) — never the
/// file structure, which the writer rebuilds anyway.
///
public final class ResourceOptimizer {

    private static final Logger LOG = Logger.getLogger(ResourceOptimizer.class.getName());

    private ResourceOptimizer() {
    }

    /// Result counters for logging/tests.
    public static final class Stats {
        /// Number of duplicate streams re-pointed to a canonical copy.
        public int duplicatesLinked;
        /// Encoded bytes of the duplicate streams that became unreachable.
        public long duplicateBytes;
        /// Number of streams re-encoded with a tighter filter chain.
        public int streamsRecompressed;
        /// Encoded bytes saved by recompression.
        public long recompressionSaved;
        /// Number of images converted to JPEG and/or downsampled.
        public int imagesRecompressed;
        /// Encoded bytes saved by image recompression.
        public long imageBytesSaved;
        /// Private-info dictionary entries removed (Metadata/PieceInfo/…).
        public int privateEntriesRemoved;
        /// Embedded font programs stripped to their used glyphs.
        public int fontsSubset;
        /// Bytes saved by glyph stripping (pre-Flate).
        public long fontBytesSaved;

        @Override
        public String toString() {
            return "duplicatesLinked=" + duplicatesLinked
                    + " duplicateBytes=" + duplicateBytes
                    + " streamsRecompressed=" + streamsRecompressed
                    + " recompressionSaved=" + recompressionSaved
                    + " imagesRecompressed=" + imagesRecompressed
                    + " imageBytesSaved=" + imageBytesSaved;
        }
    }

    /// Runs the enabled optimization passes over the parser's object graph.
    ///
    /// @param parser  the parsed document (must not be null)
    /// @param options the enabled passes (null → nothing to do)
    /// @return counters describing what was changed
    public static Stats optimize(PDFParser parser, OptimizationOptions options) {
        return optimize(parser, options, null);
    }

    /// Runs the enabled optimization passes over the parser's object graph.
    ///
    /// @param parser        the parsed document (must not be null)
    /// @param options       the enabled passes (null → nothing to do)
    /// @param imageDisplay  maximum display size in points per image stream
    ///                      (`{widthPt, heightPt}`), used to compute the
    ///                      effective resolution for downsampling; may be null
    /// @return counters describing what was changed
    public static Stats optimize(PDFParser parser, OptimizationOptions options,
                                 Map<PdfStream, double[]> imageDisplay) {
        Stats stats = new Stats();
        if (parser == null || options == null) {
            return stats;
        }
        boolean encrypted = parser.isEncrypted();
        // Lossy image passes run FIRST so the lossless recompressor sees the
        // final pixel data; then recompression, then linking, so streams that
        // converge to identical bytes collapse to one object.
        if ((options.isCompressImages()
                || (options.isResizeImages() && options.getMaxResolution() > 0))
                && !encrypted) {
            recompressImages(parser, options, imageDisplay, stats);
        }
        // Subsetting precedes lossless recompression so the shrunken font
        // programs get the max-effort Flate pass too.
        if (options.isSubsetFonts() && !encrypted) {
            subsetFonts(parser, stats);
        }
        if (!"false".equals(System.getProperty("pdf.optimize.recompress")) && !encrypted) {
            recompressStreams(parser, stats);
        }
        if (options.isLinkDuplicateStreams()) {
            linkDuplicateStreams(parser, stats);
        }
        if (options.isRemovePrivateInfo()) {
            removePrivateInfo(parser, stats);
        }
        LOG.fine(() -> "ResourceOptimizer: " + stats);
        return stats;
    }

    // ================= Pass: remove private info =================

    /// Strips application-private and derived payloads that carry no document
    /// content (`removePrivateInfo`): XMP `/Metadata` streams,
    /// `/PieceInfo` (Photoshop/Illustrator round-trip data, often the
    /// largest hidden payload in authored files), page thumbnails
    /// (`/Thumb`) and image `/Alternates`. The detached streams
    /// become unreachable and are dropped by the compact rewrite.
    private static void removePrivateInfo(PDFParser parser, Stats stats) {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof PdfDictionary)) {
                continue;
            }
            PdfDictionary dict = (PdfDictionary) obj;
            for (String privateKey : new String[]{"Metadata", "PieceInfo", "Thumb", "Alternates"}) {
                if (dict.get(privateKey) != null) {
                    // /Thumb and /Alternates only exist on pages/images; the
                    // blanket removal is safe because neither key is defined
                    // with another meaning anywhere in ISO 32000-1.
                    dict.set(PdfName.of(privateKey), null);
                    stats.privateEntriesRemoved++;
                }
            }
        }
        if (parser.getTrailer() != null) {
            // /Info document properties stay — Aspose keeps them; only the
            // duplicated XMP form is dropped above.
            LOG.fine(() -> "removePrivateInfo: " + stats.privateEntriesRemoved + " entries");
        }
    }

    // ================= Pass: recompress / downsample images =================

    /// Converts photographic raster images to JPEG at the requested quality
    /// (`compressImages`/`imageQuality`) and downsamples images
    /// displayed below their stored resolution (`resizeImages`/
    /// `maxResolution`). Only unambiguously safe candidates are
    /// touched: 8-bpc DeviceRGB/DeviceGray, no masks, no /Decode remap; a
    /// replacement must be strictly smaller than the current payload.
    private static void recompressImages(PDFParser parser, OptimizationOptions options,
                                         Map<PdfStream, double[]> imageDisplay, Stats stats) {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof PdfStream)) {
                continue;
            }
            try {
                recompressImage((PdfStream) obj, options, imageDisplay, stats);
            } catch (IOException | RuntimeException e) {
                LOG.fine(() -> "Image recompression skipped for " + key + ": " + e.getMessage());
            }
        }
    }

    private static void recompressImage(PdfStream stream, OptimizationOptions options,
                                        Map<PdfStream, double[]> imageDisplay,
                                        Stats stats) throws IOException {
        if (!"Image".equals(stream.getNameAsString("Subtype"))
                || stream.getInt("BitsPerComponent", 0) != 8
                || stream.get("ImageMask") != null
                || stream.get("SMask") != null
                || stream.get("Mask") != null
                || stream.get("Decode") != null
                || stream.get("F") != null) {
            return;
        }
        int width = stream.getInt("Width", 0);
        int height = stream.getInt("Height", 0);
        if (width <= 0 || height <= 0) {
            return;
        }
        PdfBase cs = stream.get("ColorSpace");
        if (cs instanceof PdfObjectReference) {
            try {
                cs = ((PdfObjectReference) cs).dereference();
            } catch (IllegalStateException e) {
                return;
            }
        }
        if (!(cs instanceof PdfName)) {
            return;
        }
        String csName = ((PdfName) cs).getName();
        boolean gray = "DeviceGray".equals(csName);
        if (!gray && !"DeviceRGB".equals(csName)) {
            return;     // CMYK/Indexed/ICC candidates are out of scope (v1)
        }

        // Source pixels.
        String filterName = singleFilterName(stream);
        java.awt.image.BufferedImage img;
        if ("DCTDecode".equals(filterName) || "DCT".equals(filterName)) {
            if (!options.isCompressImages() || options.getImageQuality() >= 100) {
                // Never re-encode an existing JPEG without an explicit
                // quality reduction — repeated lossy passes only hurt.
                if (imageDisplay == null || !options.isResizeImages()) {
                    return;
                }
            }
            byte[] encoded = stream.getEncodedData();
            if (encoded == null) {
                return;
            }
            img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(encoded));
            if (img == null) {
                return;
            }
        } else if (filterName == null || isLosslessByteFilter(filterName)) {
            byte[] raw = stream.getDecodedData();
            int comps = gray ? 1 : 3;
            if (raw == null || (long) width * height * comps != raw.length) {
                return;
            }
            img = rasterFromBytes(raw, width, height, gray);
        } else {
            return;     // CCITT/JBIG2/JPX stay as they are
        }

        // Downsample when displayed well below the stored resolution.
        boolean resized = false;
        if (options.isResizeImages() && options.getMaxResolution() > 0 && imageDisplay != null) {
            double[] display = imageDisplay.get(stream);
            if (display != null && display[0] > 0) {
                int targetW = (int) Math.ceil(options.getMaxResolution() * display[0] / 72.0);
                int targetH = (int) Math.ceil(options.getMaxResolution() * Math.max(display[1], 1) / 72.0);
                if (img.getWidth() > targetW * 1.1 && targetW >= 8) {
                    int newH = Math.max(1, Math.min(targetH,
                            (int) Math.round((double) img.getHeight() * targetW / img.getWidth())));
                    java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(
                            targetW, newH, gray
                                    ? java.awt.image.BufferedImage.TYPE_BYTE_GRAY
                                    : java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
                    java.awt.Graphics2D g2 = scaled.createGraphics();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.drawImage(img, 0, 0, targetW, newH, null);
                    g2.dispose();
                    img = scaled;
                    resized = true;
                }
            }
        }

        byte[] currentEncoded = stream.getEncodedData();
        long currentSize = currentEncoded != null ? currentEncoded.length
                : (long) width * height * (gray ? 1 : 3);

        byte[] replacement = null;
        boolean asJpeg = false;
        if (options.isCompressImages()) {
            float quality = Math.max(5, Math.min(100,
                    options.getImageQuality() > 0 ? options.getImageQuality() : 75)) / 100f;
            byte[] jpeg = encodeJpeg(img, gray, quality);
            if (jpeg != null && jpeg.length < currentSize) {
                replacement = jpeg;
                asJpeg = true;
            }
        }
        if (replacement == null && resized) {
            // Resize without JPEG (or JPEG lost): store the scaled raster
            // as predictor+Flate so the downsample still lands.
            byte[] raw = bytesFromRaster(img, gray);
            byte[] flat = deflateBest(pngPredictorEncode(raw, img.getWidth(), gray ? 1 : 3));
            if (flat.length < currentSize) {
                replacement = flat;
            }
        }
        if (replacement == null) {
            return;
        }

        stream.set(PdfName.of("Width"),
                org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(img.getWidth()));
        stream.set(PdfName.of("Height"),
                org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(img.getHeight()));
        if (asJpeg) {
            stream.set(PdfName.of("Filter"), PdfName.of("DCTDecode"));
            stream.set(PdfName.of("DecodeParms"), null);
        } else {
            stream.set(PdfName.of("Filter"), PdfName.of("FlateDecode"));
            PdfDictionary parms = new PdfDictionary();
            parms.set(PdfName.of("Predictor"), org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(15));
            parms.set(PdfName.of("Colors"), org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(gray ? 1 : 3));
            parms.set(PdfName.of("BitsPerComponent"), org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(8));
            parms.set(PdfName.of("Columns"), org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(img.getWidth()));
            stream.set(PdfName.of("DecodeParms"), parms);
        }
        stream.set(PdfName.of("DP"), null);
        stream.setEncodedData(replacement);
        stats.imagesRecompressed++;
        stats.imageBytesSaved += Math.max(0, currentSize - replacement.length);
    }

    /// The single filter name of the stream, or null when unfiltered; "" for chains.
    private static String singleFilterName(PdfStream stream) {
        PdfBase filter = stream.get("Filter");
        if (filter instanceof PdfObjectReference) {
            try {
                filter = ((PdfObjectReference) filter).dereference();
            } catch (IOException | IllegalStateException e) {
                return "";
            }
        }
        if (filter == null) {
            return null;
        }
        if (filter instanceof PdfName) {
            return ((PdfName) filter).getName();
        }
        if (filter instanceof PdfArray && ((PdfArray) filter).size() == 1
                && ((PdfArray) filter).get(0) instanceof PdfName) {
            return ((PdfName) ((PdfArray) filter).get(0)).getName();
        }
        return "";
    }

    /// Builds an AWT image over raw 8-bpc gray or RGB component bytes.
    private static java.awt.image.BufferedImage rasterFromBytes(byte[] raw, int width,
                                                                int height, boolean gray) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                width, height,
                gray ? java.awt.image.BufferedImage.TYPE_BYTE_GRAY
                        : java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
        java.awt.image.WritableRaster raster = img.getRaster();
        int comps = gray ? 1 : 3;
        int[] pixel = new int[comps];
        int idx = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < comps; c++) {
                    pixel[c] = raw[idx++] & 0xFF;
                }
                raster.setPixel(x, y, pixel);
            }
        }
        return img;
    }

    /// Extracts raw 8-bpc gray or RGB component bytes from an AWT image.
    private static byte[] bytesFromRaster(java.awt.image.BufferedImage img, boolean gray) {
        int width = img.getWidth();
        int height = img.getHeight();
        int comps = gray ? 1 : 3;
        byte[] out = new byte[width * height * comps];
        int idx = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = img.getRGB(x, y);
                if (gray) {
                    out[idx++] = (byte) (argb & 0xFF);
                } else {
                    out[idx++] = (byte) ((argb >>> 16) & 0xFF);
                    out[idx++] = (byte) ((argb >>> 8) & 0xFF);
                    out[idx++] = (byte) (argb & 0xFF);
                }
            }
        }
        return out;
    }

    /// JPEG-encodes the image at the given quality; null when encoding fails.
    private static byte[] encodeJpeg(java.awt.image.BufferedImage img, boolean gray, float quality) {
        try {
            // JPEG has no alpha and the writer needs an opaque raster type.
            java.awt.image.BufferedImage opaque = img;
            int wanted = gray ? java.awt.image.BufferedImage.TYPE_BYTE_GRAY
                    : java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
            if (img.getType() != wanted) {
                opaque = new java.awt.image.BufferedImage(img.getWidth(), img.getHeight(), wanted);
                java.awt.Graphics2D g2 = opaque.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
            }
            java.util.Iterator<javax.imageio.ImageWriter> writers =
                    javax.imageio.ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                return null;
            }
            javax.imageio.ImageWriter writer = writers.next();
            try {
                javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (javax.imageio.stream.ImageOutputStream ios =
                             javax.imageio.ImageIO.createImageOutputStream(out)) {
                    writer.setOutput(ios);
                    writer.write(null, new javax.imageio.IIOImage(opaque, null, null), param);
                }
                return out.toByteArray();
            } finally {
                writer.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    // ================= Pass: subset (strip) embedded fonts =================

    /// Per-font usage record for the subsetting pass.
    private static final class SubsetCandidate {
        final PdfDictionary type0;
        final PdfDictionary descendant;
        final PdfDictionary descriptor;
        final PdfStream fontFile2;
        final java.util.Set<Integer> usedCids = new TreeSet<>();

        SubsetCandidate(PdfDictionary type0, PdfDictionary descendant,
                        PdfDictionary descriptor, PdfStream fontFile2) {
            this.type0 = type0;
            this.descendant = descendant;
            this.descriptor = descriptor;
            this.fontFile2 = fontFile2;
        }
    }

    /// Strips unused glyph outlines from embedded TrueType programs of
    /// Identity-encoded CID fonts (Type0 → CIDFontType2, Identity-H/V,
    /// Identity CIDToGIDMap — the shape where CID == GID, so used glyphs can
    /// be read directly off the show strings).
    ///
    /// Conservative by design: the pass aborts wholesale when the document
    /// has AcroForm fields (variable text may use any glyph), or when any
    /// content stream fails to parse, or when text is shown through a font
    /// the scanner cannot resolve — under-collecting usage must never
    /// happen. Fonts already carrying a subset prefix are still processed
    /// (a merged document often re-embeds full programs under old names).
    ///
    private static void subsetFonts(PDFParser parser, Stats stats) {
        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            return;
        }
        if (catalog == null) {
            return;
        }
        PdfDictionary acroForm = resolveDict(catalog.get("AcroForm"));
        if (acroForm != null) {
            PdfBase fields = resolveValue(acroForm.get("Fields"));
            boolean hasFields = fields instanceof PdfArray && ((PdfArray) fields).size() > 0;
            if (hasFields || acroForm.get("XFA") != null) {
                LOG.fine("subsetFonts skipped: document has form fields");
                return;
            }
        }

        // 1. Candidates: Identity-encoded CIDFontType2 with an embedded TTF.
        Map<PdfDictionary, SubsetCandidate> candidates = new java.util.IdentityHashMap<>();
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof PdfDictionary) || obj instanceof PdfStream) {
                continue;
            }
            PdfDictionary font = (PdfDictionary) obj;
            SubsetCandidate candidate = asSubsetCandidate(font);
            if (candidate != null) {
                candidates.put(font, candidate);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        // 2. Usage scan over every page's content, form XObjects and
        //    annotation appearances. Any uncertainty aborts the pass.
        try {
            for (PdfObjectKey key : parser.getAllObjectKeys()) {
                PdfBase obj = parser.getObject(key);
                if (!(obj instanceof PdfDictionary) || obj instanceof PdfStream) {
                    continue;
                }
                PdfDictionary dict = (PdfDictionary) obj;
                if (!"Page".equals(dict.getNameAsString("Type"))) {
                    continue;
                }
                scanPageUsage(dict, candidates);
            }
        } catch (IOException | RuntimeException e) {
            LOG.fine(() -> "subsetFonts aborted (usage scan): " + e.getMessage());
            return;
        }

        // 3. Strip.
        for (SubsetCandidate candidate : candidates.values()) {
            try {
                applySubset(candidate, stats);
            } catch (IOException | RuntimeException e) {
                LOG.fine(() -> "subsetFonts: font skipped: " + e.getMessage());
            }
        }
    }

    /// Recognises the safe Identity-encoded CIDFontType2 shape, else null.
    private static SubsetCandidate asSubsetCandidate(PdfDictionary font) {
        if (!"Font".equals(font.getNameAsString("Type"))
                || !"Type0".equals(font.getNameAsString("Subtype"))) {
            return null;
        }
        String encoding = font.getNameAsString("Encoding");
        if (!"Identity-H".equals(encoding) && !"Identity-V".equals(encoding)) {
            return null;
        }
        PdfBase descendants = resolveValue(font.get("DescendantFonts"));
        if (!(descendants instanceof PdfArray) || ((PdfArray) descendants).size() != 1) {
            return null;
        }
        PdfDictionary descendant = resolveDict(((PdfArray) descendants).get(0));
        if (descendant == null
                || !"CIDFontType2".equals(descendant.getNameAsString("Subtype"))) {
            return null;
        }
        PdfBase cidToGid = resolveValue(descendant.get("CIDToGIDMap"));
        if (cidToGid != null
                && !(cidToGid instanceof PdfName && "Identity".equals(((PdfName) cidToGid).getName()))) {
            return null;    // a mapping stream would break the CID==GID shortcut
        }
        PdfDictionary descriptor = resolveDict(descendant.get("FontDescriptor"));
        if (descriptor == null) {
            return null;
        }
        PdfBase fontFile = resolveValue(descriptor.get("FontFile2"));
        if (!(fontFile instanceof PdfStream)) {
            return null;
        }
        return new SubsetCandidate(font, descendant, descriptor, (PdfStream) fontFile);
    }

    /// Collects CID usage from one page: contents, form XObjects, appearances.
    private static void scanPageUsage(PdfDictionary page,
                                      Map<PdfDictionary, SubsetCandidate> candidates) throws IOException {
        PdfDictionary resources = inheritedResources(page);
        byte[] content = pageContentBytes(page);
        java.util.Set<PdfDictionary> visitedForms =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        if (content != null) {
            scanContentUsage(content, resources, candidates, visitedForms, 0);
        }
        PdfBase annots = resolveValue(page.get("Annots"));
        if (annots instanceof PdfArray) {
            PdfArray array = (PdfArray) annots;
            for (int i = 0; i < array.size(); i++) {
                PdfDictionary annot = resolveDict(array.get(i));
                if (annot == null) {
                    continue;
                }
                PdfBase ap = resolveValue(annot.get("AP"));
                if (ap instanceof PdfDictionary) {
                    for (Map.Entry<PdfName, PdfBase> state : (PdfDictionary) ap) {
                        PdfBase appearance = resolveValue(state.getValue());
                        if (appearance instanceof PdfStream) {
                            scanFormUsage((PdfStream) appearance, resources, candidates, visitedForms, 0);
                        } else if (appearance instanceof PdfDictionary) {
                            for (Map.Entry<PdfName, PdfBase> sub : (PdfDictionary) appearance) {
                                PdfBase stream = resolveValue(sub.getValue());
                                if (stream instanceof PdfStream) {
                                    scanFormUsage((PdfStream) stream, resources, candidates,
                                            visitedForms, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /// Resources with page-tree inheritance (§7.7.3.4).
    private static PdfDictionary inheritedResources(PdfDictionary page) {
        PdfDictionary node = page;
        for (int depth = 0; node != null && depth < 64; depth++) {
            PdfDictionary resources = resolveDict(node.get("Resources"));
            if (resources != null) {
                return resources;
            }
            node = resolveDict(node.get("Parent"));
        }
        return null;
    }

    private static byte[] pageContentBytes(PdfDictionary page) throws IOException {
        PdfBase contents = resolveValue(page.get("Contents"));
        if (contents instanceof PdfStream) {
            return ((PdfStream) contents).getDecodedData();
        }
        if (contents instanceof PdfArray) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfArray array = (PdfArray) contents;
            for (int i = 0; i < array.size(); i++) {
                PdfBase item = resolveValue(array.get(i));
                if (item instanceof PdfStream) {
                    out.write(((PdfStream) item).getDecodedData());
                    out.write('\n');
                }
            }
            return out.toByteArray();
        }
        return null;
    }

    private static void scanFormUsage(PdfStream form, PdfDictionary fallbackResources,
                                      Map<PdfDictionary, SubsetCandidate> candidates,
                                      java.util.Set<PdfDictionary> visitedForms,
                                      int depth) throws IOException {
        if (depth > 8 || !visitedForms.add(form)) {
            return;
        }
        PdfDictionary resources = resolveDict(form.get("Resources"));
        scanContentUsage(form.getDecodedData(),
                resources != null ? resources : fallbackResources,
                candidates, visitedForms, depth);
    }

    /// Walks one content stream attributing show-operator bytes to fonts.
    /// Throws when text is shown through a font that cannot be resolved —
    /// the caller treats that as "usage unknown" and aborts the pass.
    private static void scanContentUsage(byte[] content, PdfDictionary resources,
                                         Map<PdfDictionary, SubsetCandidate> candidates,
                                         java.util.Set<PdfDictionary> visitedForms,
                                         int depth) throws IOException {
        java.util.List<org.aspose.pdf.Operator> ops =
                org.aspose.pdf.engine.parser.ContentStreamParser.parse(content);
        PdfDictionary fontResources = resources != null ? resolveDict(resources.get("Font")) : null;
        PdfDictionary xObjects = resources != null ? resolveDict(resources.get("XObject")) : null;

        PdfDictionary currentFont = null;
        boolean currentResolved = true;
        java.util.Deque<PdfDictionary> fontStack = new java.util.ArrayDeque<>();
        java.util.Deque<Boolean> resolvedStack = new java.util.ArrayDeque<>();

        for (org.aspose.pdf.Operator op : ops) {
            String name = op.getName();
            switch (name) {
                case "q":
                    fontStack.push(currentFont);
                    resolvedStack.push(currentResolved);
                    break;
                case "Q":
                    if (!fontStack.isEmpty()) {
                        currentFont = fontStack.pop();
                        currentResolved = resolvedStack.pop();
                    }
                    break;
                case "Tf": {
                    currentFont = null;
                    currentResolved = false;
                    java.util.List<PdfBase> operands = op.getOperands();
                    if (operands != null && !operands.isEmpty()
                            && operands.get(0) instanceof PdfName && fontResources != null) {
                        currentFont = resolveDict(fontResources.get(
                                ((PdfName) operands.get(0)).getName()));
                    }
                    currentResolved = currentFont != null;
                    break;
                }
                case "Tj": case "'": case "\"": case "TJ": {
                    if (!currentResolved) {
                        throw new IllegalStateException("text shown through unresolved font");
                    }
                    SubsetCandidate candidate = candidates.get(currentFont);
                    if (candidate != null) {
                        collectShowBytes(op, candidate.usedCids);
                    }
                    break;
                }
                case "Do": {
                    java.util.List<PdfBase> operands = op.getOperands();
                    if (operands != null && !operands.isEmpty()
                            && operands.get(0) instanceof PdfName && xObjects != null) {
                        PdfBase target = resolveValue(xObjects.get(
                                ((PdfName) operands.get(0)).getName()));
                        if (target instanceof PdfStream
                                && "Form".equals(((PdfStream) target).getNameAsString("Subtype"))) {
                            scanFormUsage((PdfStream) target, resources, candidates,
                                    visitedForms, depth + 1);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    /// Adds the 2-byte CIDs of a show operator's strings to `cids`.
    private static void collectShowBytes(org.aspose.pdf.Operator op, java.util.Set<Integer> cids) {
        java.util.List<PdfBase> operands = op.getOperands();
        if (operands == null) {
            return;
        }
        for (PdfBase operand : operands) {
            if (operand instanceof org.aspose.pdf.engine.pdfobjects.PdfString) {
                addCids(((org.aspose.pdf.engine.pdfobjects.PdfString) operand).getBytes(), cids);
            } else if (operand instanceof PdfArray) {
                PdfArray array = (PdfArray) operand;
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof org.aspose.pdf.engine.pdfobjects.PdfString) {
                        addCids(((org.aspose.pdf.engine.pdfobjects.PdfString) array.get(i)).getBytes(),
                                cids);
                    }
                }
            }
        }
    }

    private static void addCids(byte[] bytes, java.util.Set<Integer> cids) {
        for (int i = 0; i + 1 < bytes.length; i += 2) {
            cids.add(((bytes[i] & 0xFF) << 8) | (bytes[i + 1] & 0xFF));
        }
    }

    /// Strips the candidate's TTF and updates dict metadata when it shrinks.
    private static void applySubset(SubsetCandidate candidate, Stats stats) throws IOException {
        byte[] original = candidate.fontFile2.getDecodedData();
        if (original == null || original.length < 12) {
            return;
        }
        byte[] stripped = TtfGlyphStripper.strip(original, candidate.usedCids);
        if (stripped == null || stripped.length >= original.length) {
            return;
        }
        // Verify the result still parses as a TrueType program.
        new org.aspose.pdf.engine.font.ttf.TrueTypeReader(stripped);

        candidate.fontFile2.setDecodedData(stripped);
        candidate.fontFile2.set(PdfName.of("Length1"),
                org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(stripped.length));
        applySubsetPrefix(candidate);
        stats.fontsSubset++;
        stats.fontBytesSaved += original.length - stripped.length;
    }

    /// Prepends the conventional 6-letter subset tag when absent.
    private static void applySubsetPrefix(SubsetCandidate candidate) {
        String base = candidate.type0.getNameAsString("BaseFont");
        if (base == null || (base.length() > 7 && base.charAt(6) == '+')) {
            return;
        }
        // Deterministic tag derived from the font name — stable across runs.
        StringBuilder tag = new StringBuilder(7);
        int seed = base.hashCode();
        for (int i = 0; i < 6; i++) {
            tag.append((char) ('A' + Math.floorMod(seed >> (i * 5), 26)));
        }
        tag.append('+');
        String prefixed = tag + base;
        candidate.type0.set(PdfName.of("BaseFont"), PdfName.of(prefixed));
        if (candidate.descendant.getNameAsString("BaseFont") != null) {
            candidate.descendant.set(PdfName.of("BaseFont"), PdfName.of(prefixed));
        }
        if (candidate.descriptor.getNameAsString("FontName") != null) {
            candidate.descriptor.set(PdfName.of("FontName"), PdfName.of(prefixed));
        }
    }

    /// Resolves a possibly-indirect value; null on failure.
    private static PdfBase resolveValue(PdfBase value) {
        if (value instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) value).dereference();
            } catch (IOException | IllegalStateException e) {
                return null;
            }
        }
        return value;
    }

    /// Resolves a possibly-indirect dictionary; null when not a dict.
    private static PdfDictionary resolveDict(PdfBase value) {
        PdfBase resolved = resolveValue(value);
        return resolved instanceof PdfDictionary ? (PdfDictionary) resolved : null;
    }

    // ================= Pass: recompress streams =================

    /// Filters whose output is a plain byte-for-byte lossless recoding.
    private static boolean isLosslessByteFilter(String name) {
        return "FlateDecode".equals(name) || "Fl".equals(name)
                || "LZWDecode".equals(name) || "LZW".equals(name)
                || "ASCIIHexDecode".equals(name) || "AHx".equals(name)
                || "ASCII85Decode".equals(name) || "A85".equals(name)
                || "RunLengthDecode".equals(name) || "RL".equals(name);
    }

    /// Re-encodes every stream whose filter chain is purely lossless
    /// (uncompressed, ASCIIHex/85, LZW, RunLength or loosely-deflated Flate)
    /// as a single maximum-effort `/FlateDecode`, adding a PNG
    /// predictor for 8-bpc raster images when that wins. A replacement is
    /// applied only when it is strictly smaller than the current encoded
    /// form, and predictor candidates are verified by decoding them back.
    private static void recompressStreams(PDFParser parser, Stats stats) {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof PdfStream)) {
                continue;
            }
            PdfStream stream = (PdfStream) obj;
            try {
                recompressStream(stream, stats);
            } catch (IOException | RuntimeException e) {
                LOG.fine(() -> "Recompression skipped for " + key + ": " + e.getMessage());
            }
        }
    }

    private static void recompressStream(PdfStream stream, Stats stats) throws IOException {
        String type = stream.getNameAsString("Type");
        if ("XRef".equals(type) || "ObjStm".equals(type)) {
            return;     // file structure — rebuilt by the writer
        }
        if ("Metadata".equals(type)) {
            // XMP conventionally stays uncompressed so non-PDF tools can
            // read it, and PDF/A-1 §6.7.11 outright requires it unfiltered.
            return;
        }
        if (stream.get("F") != null) {
            return;     // external file stream: no embedded payload
        }
        // The whole chain must be lossless byte filters (or absent).
        PdfBase filter = stream.get("Filter");
        if (filter instanceof PdfObjectReference) {
            try {
                filter = ((PdfObjectReference) filter).dereference();
            } catch (IllegalStateException e) {
                return;
            }
        }
        if (filter instanceof PdfName) {
            if (!isLosslessByteFilter(((PdfName) filter).getName())) {
                return;
            }
        } else if (filter instanceof PdfArray) {
            PdfArray chain = (PdfArray) filter;
            for (int i = 0; i < chain.size(); i++) {
                PdfBase item = chain.get(i);
                if (!(item instanceof PdfName)
                        || !isLosslessByteFilter(((PdfName) item).getName())) {
                    return;
                }
            }
        } else if (filter != null) {
            return;
        }

        byte[] decoded = stream.getDecodedData();
        if (decoded == null || decoded.length == 0) {
            return;
        }
        byte[] currentEncoded = stream.getEncodedData();
        long currentSize = currentEncoded != null ? currentEncoded.length : decoded.length;

        byte[] best = deflateBest(decoded);
        PdfDictionary bestParms = null;

        // PNG-predictor candidate for 8-bpc raster images.
        int[] geometry = predictorGeometry(stream, decoded.length);
        if (geometry != null) {
            int columns = geometry[0];
            int colors = geometry[1];
            byte[] filtered = pngPredictorEncode(decoded, columns, colors);
            byte[] candidate = deflateBest(filtered);
            if (candidate.length < best.length) {
                PdfDictionary parms = new PdfDictionary();
                parms.set(PdfName.of("Predictor"), org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(15));
                parms.set(PdfName.of("Colors"), org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(colors));
                parms.set(PdfName.of("BitsPerComponent"), org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(8));
                parms.set(PdfName.of("Columns"), org.aspose.pdf.engine.pdfobjects.PdfInteger.valueOf(columns));
                // Self-check: the decoder must reproduce the original bytes.
                org.aspose.pdf.engine.filter.FlateFilter flate =
                        new org.aspose.pdf.engine.filter.FlateFilter();
                byte[] roundTrip = flate.decode(candidate, parms);
                if (java.util.Arrays.equals(roundTrip, decoded)) {
                    best = candidate;
                    bestParms = parms;
                }
            }
        }

        if (best.length >= currentSize) {
            return;     // no win — leave the stream untouched
        }
        stream.set(PdfName.of("Filter"), PdfName.of("FlateDecode"));
        stream.set(PdfName.of("DecodeParms"), bestParms);   // null clears the key
        stream.set(PdfName.of("DP"), null);
        stream.setEncodedData(best);
        stats.streamsRecompressed++;
        stats.recompressionSaved += currentSize - best.length;
    }

    /// Maximum-effort raw deflate of the given bytes.
    private static byte[] deflateBest(byte[] data) {
        java.util.zip.Deflater deflater =
                new java.util.zip.Deflater(java.util.zip.Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(64, data.length / 3));
        byte[] buf = new byte[8192];
        while (!deflater.finished()) {
            out.write(buf, 0, deflater.deflate(buf));
        }
        deflater.end();
        return out.toByteArray();
    }

    /// Returns `{columns, colors}` when the stream is an 8-bpc raster
    /// image whose decoded size matches width × colors × height exactly —
    /// the only shape our PNG predictor encoder handles — else `null`.
    private static int[] predictorGeometry(PdfStream stream, int decodedLength) {
        if (!"Image".equals(stream.getNameAsString("Subtype"))) {
            return null;
        }
        if (stream.getInt("BitsPerComponent", 0) != 8 || stream.get("ImageMask") != null) {
            return null;
        }
        int width = stream.getInt("Width", 0);
        int height = stream.getInt("Height", 0);
        if (width <= 0 || height <= 0) {
            return null;
        }
        PdfBase cs = stream.get("ColorSpace");
        if (cs instanceof PdfObjectReference) {
            try {
                cs = ((PdfObjectReference) cs).dereference();
            } catch (IOException | IllegalStateException e) {
                return null;
            }
        }
        int colors;
        if (cs instanceof PdfName) {
            switch (((PdfName) cs).getName()) {
                case "DeviceGray": colors = 1; break;
                case "DeviceRGB": colors = 3; break;
                case "DeviceCMYK": colors = 4; break;
                default: return null;
            }
        } else if (cs instanceof PdfArray && ((PdfArray) cs).size() > 0
                && ((PdfArray) cs).get(0) instanceof PdfName
                && "Indexed".equals(((PdfName) ((PdfArray) cs).get(0)).getName())) {
            colors = 1;
        } else {
            return null;
        }
        long expected = (long) width * colors * height;
        if (expected != decodedLength) {
            return null;
        }
        return new int[]{width, colors};
    }

    /// PNG predictor pre-filtering (per-row best of None/Sub/Up/Average/Paeth
    /// by the standard minimum-sum-of-absolute-differences heuristic). Output
    /// rows carry the PNG filter-type byte prefix expected by
    /// `/Predictor 15` decoding.
    private static byte[] pngPredictorEncode(byte[] data, int columns, int colors) {
        int rowLen = columns * colors;
        int rows = data.length / rowLen;
        byte[] out = new byte[rows * (rowLen + 1)];
        byte[] prior = new byte[rowLen];
        byte[][] scratch = new byte[5][rowLen];
        for (int r = 0; r < rows; r++) {
            int rowOff = r * rowLen;
            int bestType = 0;
            long bestScore = Long.MAX_VALUE;
            for (int t = 0; t < 5; t++) {
                long score = 0;
                for (int i = 0; i < rowLen; i++) {
                    int raw = data[rowOff + i] & 0xFF;
                    int left = i >= colors ? data[rowOff + i - colors] & 0xFF : 0;
                    int up = prior[i] & 0xFF;
                    int upLeft = i >= colors ? prior[i - colors] & 0xFF : 0;
                    int value;
                    switch (t) {
                        case 1: value = raw - left; break;
                        case 2: value = raw - up; break;
                        case 3: value = raw - (left + up) / 2; break;
                        case 4: value = raw - paeth(left, up, upLeft); break;
                        default: value = raw; break;
                    }
                    byte b = (byte) value;
                    scratch[t][i] = b;
                    // Standard PNG heuristic: sum of signed-byte magnitudes.
                    score += Math.abs((int) b);
                    if (score >= bestScore) {
                        // early exit per row candidate
                        score = Long.MAX_VALUE;
                        break;
                    }
                }
                if (score < bestScore) {
                    bestScore = score;
                    bestType = t;
                }
            }
            int outOff = r * (rowLen + 1);
            out[outOff] = (byte) bestType;
            System.arraycopy(scratch[bestType], 0, out, outOff + 1, rowLen);
            System.arraycopy(data, rowOff, prior, 0, rowLen);
        }
        return out;
    }

    /// PNG Paeth predictor (RFC 2083 §6.6).
    private static int paeth(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        if (pa <= pb && pa <= pc) return a;
        return pb <= pc ? b : c;
    }

    // ================= Pass: link duplicate streams =================

    /// Finds streams that are byte-identical (same dictionary content, same
    /// encoded payload) and re-points every reference to the copy with the
    /// lowest object number. The orphaned duplicates are dropped by the
    /// compact rewrite that follows optimization.
    ///
    /// Runs up to three rounds so nested duplicates converge: two images
    /// that differ only in referencing distinct-but-identical soft masks
    /// become identical themselves once the masks are linked in round one.
    ///
    private static void linkDuplicateStreams(PDFParser parser, Stats stats) {
        for (int round = 0; round < 3; round++) {
            Map<PdfObjectKey, PdfObjectKey> remap = buildDuplicateMap(parser, stats);
            if (remap.isEmpty()) {
                return;
            }
            remapReferences(parser, remap);
        }
    }

    /// Maps each duplicate stream's key to the canonical (lowest-numbered) key.
    private static Map<PdfObjectKey, PdfObjectKey> buildDuplicateMap(PDFParser parser, Stats stats) {
        Map<String, PdfObjectKey> canonical = new HashMap<>();
        Map<PdfObjectKey, PdfObjectKey> remap = new LinkedHashMap<>();
        List<PdfObjectKey> keys = new ArrayList<>(parser.getAllObjectKeys());
        // Deterministic: lowest object number becomes the canonical copy.
        keys.sort((a, b) -> {
            int cmp = Integer.compare(a.getObjectNumber(), b.getObjectNumber());
            return cmp != 0 ? cmp : Integer.compare(a.getGenerationNumber(), b.getGenerationNumber());
        });
        for (PdfObjectKey key : keys) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof PdfStream)) {
                continue;
            }
            PdfStream stream = (PdfStream) obj;
            String type = stream.getNameAsString("Type");
            // File-structure streams are rebuilt by the writer; linking them
            // would be meaningless (and they are dropped on rewrite anyway).
            if ("XRef".equals(type) || "ObjStm".equals(type)) {
                continue;
            }
            String signature = signatureOf(stream);
            if (signature == null) {
                continue;
            }
            PdfObjectKey first = canonical.putIfAbsent(signature, key);
            if (first != null && !first.equals(key)) {
                remap.put(key, first);
                stats.duplicatesLinked++;
                byte[] encoded = stream.getEncodedData();
                stats.duplicateBytes += encoded != null ? encoded.length : 0;
            }
        }
        return remap;
    }

    /// Canonical content signature of a stream: dictionary entries sorted by
    /// key name (skipping `/Length`, which the writer recomputes)
    /// followed by the payload bytes. References serialise as
    /// `"N G R"`, so streams pointing at different targets stay
    /// distinct until those targets are themselves linked.
    ///
    /// @return the signature, or `null` when the stream data cannot be
    ///         read (such streams are never linked)
    private static String signatureOf(PdfStream stream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            TreeMap<String, PdfBase> sorted = new TreeMap<>();
            for (Map.Entry<PdfName, PdfBase> entry : stream) {
                String name = entry.getKey().getName();
                if ("Length".equals(name)) {
                    continue;
                }
                sorted.put(name, entry.getValue());
            }
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            for (Map.Entry<String, PdfBase> entry : sorted.entrySet()) {
                buf.write(entry.getKey().getBytes(StandardCharsets.UTF_8));
                buf.write(0);
                entry.getValue().writeTo(buf);
                buf.write(0);
            }
            digest.update(buf.toByteArray());
            digest.update((byte) 0xFF);
            byte[] payload = stream.getEncodedData();
            if (payload == null) {
                // Stream created in memory: hash the decoded form instead.
                digest.update((byte) 0x01);
                payload = stream.getDecodedData();
            }
            if (payload == null) {
                return null;
            }
            digest.update(payload);
            StringBuilder hex = new StringBuilder();
            for (byte b : digest.digest()) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (IOException | NoSuchAlgorithmException | RuntimeException e) {
            return null;
        }
    }

    /// Rewrites every reference in the object graph (all indirect objects
    /// plus the trailer) that points at a re-mapped key so it targets the
    /// canonical copy instead.
    private static void remapReferences(PDFParser parser, Map<PdfObjectKey, PdfObjectKey> remap) {
        PdfObjectReference.ObjectResolver resolver = parser::getObject;
        // Malformed files can contain DIRECT dictionary/array cycles (a dict
        // reachable from itself without an indirect reference in between), so
        // the walk carries an identity visited-set to terminate.
        java.util.Set<PdfBase> visited =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            if (remap.containsKey(key)) {
                continue;   // the duplicate itself is about to become garbage
            }
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            remapIn(obj, remap, resolver, visited);
        }
        if (parser.getTrailer() != null) {
            remapIn(parser.getTrailer(), remap, resolver, visited);
        }
    }

    /// Recursively replaces re-mapped references inside dicts and arrays.
    private static void remapIn(PdfBase node, Map<PdfObjectKey, PdfObjectKey> remap,
                                PdfObjectReference.ObjectResolver resolver,
                                java.util.Set<PdfBase> visited) {
        if (node instanceof PdfDictionary) {
            PdfDictionary dict = (PdfDictionary) node;
            if (!visited.add(dict)) {
                return;
            }
            // Snapshot the keys — set() during iteration would be undefined.
            for (PdfName name : new ArrayList<>(dict.keySet())) {
                PdfBase value = dict.get(name);
                PdfBase replaced = replacement(value, remap, resolver);
                if (replaced != null) {
                    dict.set(name, replaced);
                } else {
                    remapIn(value, remap, resolver, visited);
                }
            }
        } else if (node instanceof PdfArray) {
            PdfArray array = (PdfArray) node;
            if (!visited.add(array)) {
                return;
            }
            for (int i = 0; i < array.size(); i++) {
                PdfBase value = array.get(i);
                PdfBase replaced = replacement(value, remap, resolver);
                if (replaced != null) {
                    array.set(i, replaced);
                } else {
                    remapIn(value, remap, resolver, visited);
                }
            }
        }
    }

    /// A new reference to the canonical key, or `null` if no remap applies.
    private static PdfBase replacement(PdfBase value, Map<PdfObjectKey, PdfObjectKey> remap,
                                       PdfObjectReference.ObjectResolver resolver) {
        if (!(value instanceof PdfObjectReference)) {
            return null;
        }
        PdfObjectKey target = remap.get(((PdfObjectReference) value).getKey());
        return target != null ? new PdfObjectReference(target, resolver) : null;
    }
}
