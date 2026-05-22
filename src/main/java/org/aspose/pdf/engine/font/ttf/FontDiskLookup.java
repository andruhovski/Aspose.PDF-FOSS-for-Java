package org.aspose.pdf.engine.font.ttf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Resolves a logical font name (e.g. {@code "SimSun"}) to its raw TrueType
 * bytes on disk, returning a freshly assembled TTF buffer suitable for
 * embedding into a PDF {@code /FontFile2} stream.
 *
 * <p>The lookup walks the OS-specific font directories (Windows
 * {@code C:\Windows\Fonts}, macOS {@code /Library/Fonts} +
 * {@code /System/Library/Fonts}, Linux {@code /usr/share/fonts}) and tries
 * a small set of filename variants for each: lowercase, no-spaces, and
 * common suffixes ({@code .ttf}, {@code .ttc}, {@code .otf}).</p>
 *
 * <p>When the match is a TrueType Collection ({@code .ttc} starting with
 * the {@code ttcf} magic), the first face is extracted into a brand-new
 * standalone TTF buffer — its sfnt header is rebuilt, table-directory
 * offsets are rewritten relative to the buffer start, and the shared
 * tables are copied from the source. PDF readers do not understand TTC
 * directly, so this re-packaging is what lets {@code SimSun} embed at all.</p>
 */
public final class FontDiskLookup {

    private static final Logger LOG = Logger.getLogger(FontDiskLookup.class.getName());

    private FontDiskLookup() {}

    /**
     * Searches the OS font directories for {@code logicalName} and returns
     * the bytes of a standalone TTF for the first matching face. Returns
     * {@code null} if no candidate file is found or none decodes cleanly.
     *
     * @param logicalName the font name as a caller would type it
     *                    ({@code "SimSun"}, {@code "Arial"}, etc.)
     * @return TTF bytes (TTC's first face extracted to a standalone TTF), or
     *         {@code null} if not resolvable
     */
    public static byte[] loadByName(String logicalName) {
        if (logicalName == null || logicalName.isEmpty()) return null;
        String[] dirs = osFontDirs();
        String[] basenames = candidateBasenames(logicalName);
        for (String dir : dirs) {
            Path dp = Paths.get(dir);
            if (!Files.isDirectory(dp)) continue;
            for (String base : basenames) {
                for (String ext : new String[]{".ttf", ".otf", ".ttc"}) {
                    Path p = dp.resolve(base + ext);
                    if (Files.isRegularFile(p)) {
                        byte[] bytes = readAll(p);
                        if (bytes == null) continue;
                        byte[] ttf = ext.equals(".ttc")
                                ? extractFirstFaceFromTTC(bytes)
                                : bytes;
                        if (ttf != null) {
                            LOG.fine("Resolved font " + logicalName + " → " + p);
                            return ttf;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Loads a font file by an exact basename (no candidate-list expansion).
     * Returns the file's bytes unchanged when it's a standalone TTF/OTF, or
     * the first face unpacked into a standalone TTF when it's a TTC.
     *
     * <p>Used by callers that have already narrowed the choice down to one
     * physical face — e.g. picking {@code simsunb} over {@code simsun} when
     * the text being rendered carries CJK Extension B characters.</p>
     *
     * @param basename file basename without extension (e.g. {@code "simsunb"})
     * @return font bytes, or {@code null} when the file is missing
     */
    public static byte[] loadByExactBasename(String basename) {
        if (basename == null || basename.isEmpty()) return null;
        for (String dir : osFontDirs()) {
            Path dp = Paths.get(dir);
            if (!Files.isDirectory(dp)) continue;
            for (String ext : new String[]{".ttf", ".otf", ".ttc"}) {
                Path p = dp.resolve(basename + ext);
                if (!Files.isRegularFile(p)) continue;
                byte[] bytes = readAll(p);
                if (bytes == null) continue;
                byte[] ttf = ext.equals(".ttc") ? extractFirstFaceFromTTC(bytes) : bytes;
                if (ttf != null) return ttf;
            }
        }
        return null;
    }

    /** OS-specific font search roots, ordered most-likely first. */
    private static String[] osFontDirs() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String winDir = System.getenv("WINDIR");
            if (winDir == null || winDir.isEmpty()) winDir = "C:\\Windows";
            return new String[]{winDir + "\\Fonts"};
        }
        if (os.contains("mac")) {
            return new String[]{"/Library/Fonts", "/System/Library/Fonts",
                    System.getProperty("user.home") + "/Library/Fonts"};
        }
        return new String[]{"/usr/share/fonts", "/usr/local/share/fonts",
                System.getProperty("user.home") + "/.fonts"};
    }

    /**
     * Generates filename candidates from a logical font name. SimSun lives at
     * {@code simsun.ttc}; {@code Arial Unicode MS} at {@code arialuni.ttf} —
     * exact mapping is intractable without a font catalog, so we try a few
     * sensible derivations (lowercase, strip spaces, well-known shortcuts)
     * and rely on the caller catching a {@code null} return.
     */
    private static String[] candidateBasenames(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        String compact = lower.replace(" ", "").replace("-", "");
        String[] base;
        if ("arial unicode ms".equals(lower)) {
            base = new String[]{"arialuni", "arial"};
        } else if ("simsun".equals(lower)) {
            base = new String[]{"simsun", "simsunb"};
        } else if ("simhei".equals(lower)) {
            base = new String[]{"simhei"};
        } else {
            base = new String[]{compact, lower};
        }
        return base;
    }

    private static byte[] readAll(Path p) {
        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            LOG.fine("Failed to read " + p + ": " + e.getMessage());
            return null;
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  TTC → standalone TTF
    // ────────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} when {@code bytes} starts with the {@code ttcf}
     * magic of a TrueType Collection.
     */
    public static boolean isTTC(byte[] bytes) {
        return bytes != null && bytes.length >= 4
                && bytes[0] == 't' && bytes[1] == 't'
                && bytes[2] == 'c' && bytes[3] == 'f';
    }

    /**
     * Rewrites the first face of a TrueType Collection into a standalone
     * TTF buffer. Returns {@code null} if the input is malformed or does
     * not advertise at least one face.
     */
    public static byte[] extractFirstFaceFromTTC(byte[] ttc) {
        if (!isTTC(ttc) || ttc.length < 16) return null;
        int numFonts = readInt32(ttc, 8);
        if (numFonts <= 0 || 12 + numFonts * 4 > ttc.length) return null;
        int faceOffset = readInt32(ttc, 12);   // first face's sfnt header
        return extractFaceAt(ttc, faceOffset);
    }

    /**
     * Extracts the face whose sfnt header begins at {@code faceOffset} into
     * a fresh standalone TTF. Public for callers that pick non-default
     * faces from a TTC.
     */
    public static byte[] extractFaceAt(byte[] ttc, int faceOffset) {
        if (ttc == null || faceOffset < 0 || faceOffset + 12 > ttc.length) return null;
        int sfntVersion = readInt32(ttc, faceOffset);
        int numTables = readUInt16(ttc, faceOffset + 4);
        int searchRange = readUInt16(ttc, faceOffset + 6);
        int entrySelector = readUInt16(ttc, faceOffset + 8);
        int rangeShift = readUInt16(ttc, faceOffset + 10);

        int[] tableTags = new int[numTables];
        int[] checksums = new int[numTables];
        int[] offsets = new int[numTables];
        int[] lengths = new int[numTables];

        int totalLen = 12 + 16 * numTables;
        for (int i = 0; i < numTables; i++) {
            int entryOff = faceOffset + 12 + i * 16;
            if (entryOff + 16 > ttc.length) return null;
            tableTags[i] = readInt32(ttc, entryOff);
            checksums[i] = readInt32(ttc, entryOff + 4);
            offsets[i] = readInt32(ttc, entryOff + 8);
            lengths[i] = readInt32(ttc, entryOff + 12);
            totalLen += alignTo4(lengths[i]);
        }

        byte[] out = new byte[totalLen];
        writeInt32(out, 0, sfntVersion);
        writeUInt16(out, 4, numTables);
        writeUInt16(out, 6, searchRange);
        writeUInt16(out, 8, entrySelector);
        writeUInt16(out, 10, rangeShift);

        int writeCursor = 12 + 16 * numTables;
        for (int i = 0; i < numTables; i++) {
            int entryOff = 12 + i * 16;
            writeInt32(out, entryOff, tableTags[i]);
            writeInt32(out, entryOff + 4, checksums[i]);
            writeInt32(out, entryOff + 8, writeCursor);
            writeInt32(out, entryOff + 12, lengths[i]);
            if (offsets[i] + lengths[i] > ttc.length) return null;
            System.arraycopy(ttc, offsets[i], out, writeCursor, lengths[i]);
            writeCursor += alignTo4(lengths[i]);
        }
        return out;
    }

    private static int alignTo4(int n) {
        return (n + 3) & ~3;
    }

    private static int readInt32(byte[] b, int o) {
        return ((b[o] & 0xFF) << 24) | ((b[o + 1] & 0xFF) << 16)
                | ((b[o + 2] & 0xFF) << 8) | (b[o + 3] & 0xFF);
    }

    private static int readUInt16(byte[] b, int o) {
        return ((b[o] & 0xFF) << 8) | (b[o + 1] & 0xFF);
    }

    private static void writeInt32(byte[] b, int o, int v) {
        b[o]     = (byte) (v >>> 24);
        b[o + 1] = (byte) (v >>> 16);
        b[o + 2] = (byte) (v >>> 8);
        b[o + 3] = (byte) v;
    }

    private static void writeUInt16(byte[] b, int o, int v) {
        b[o]     = (byte) (v >>> 8);
        b[o + 1] = (byte) v;
    }
}
