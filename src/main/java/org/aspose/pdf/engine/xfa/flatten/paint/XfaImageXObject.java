package org.aspose.pdf.engine.xfa.flatten.paint;

import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.zip.Deflater;

/// Decodes an XFA `<image>` (the base64 payload of a `<draw>`/`<field>` logo or
/// picture) into a PDF Image XObject stream ready to register with
/// [org.aspose.pdf.engine.layout.ContentStreamBuilder#registerImage(String, PdfStream)] and
/// paint via `cm`+`Do`.
///
/// Mirrors the proven decode used by the document layout engine: a JPEG is forwarded verbatim
/// through `/DCTDecode` (lossless reuse); everything else (PNG/GIF/BMP/TIFF) is decoded through
/// [ImageIO], composited against white where it carries alpha, and re-encoded as an 8-bit
/// DeviceRGB `/FlateDecode` stream. Zero third-party dependencies (only `javax.imageio`).
public final class XfaImageXObject {

    private static final Logger LOG = Logger.getLogger(XfaImageXObject.class.getName());

    private XfaImageXObject() {
    }

    /// Decodes raw image bytes into a PDF Image XObject stream.
    ///
    /// @param raw the decoded image bytes (PNG/JPEG/GIF/BMP/TIFF)
    /// @return the Image XObject stream, or `null` if the bytes could not be decoded
    public static PdfStream decode(byte[] raw) {
        if (raw == null || raw.length < 4) {
            return null;
        }
        // JPEG fast path: forward source bytes through /DCTDecode (PDF viewers decode them natively).
        if ((raw[0] & 0xFF) == 0xFF && (raw[1] & 0xFF) == 0xD8) {
            int[] dims = jpegDimensions(raw);
            if (dims != null) {
                String cs = dims[2] == 1 ? "DeviceGray" : dims[2] == 4 ? "DeviceCMYK" : "DeviceRGB";
                return build(dims[0], dims[1], "DCTDecode", cs, raw);
            }
        }
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(raw));
            if (bi == null) {
                return null;
            }
            int w = bi.getWidth();
            int h = bi.getHeight();
            byte[] rgb = new byte[w * h * 3];
            int idx = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = bi.getRGB(x, y);
                    int a = (argb >>> 24) & 0xFF;
                    int r = (argb >>> 16) & 0xFF;
                    int g = (argb >>> 8) & 0xFF;
                    int b = argb & 0xFF;
                    if (a < 255) { // composite transparent areas against white page paper
                        r = (r * a + 255 * (255 - a)) / 255;
                        g = (g * a + 255 * (255 - a)) / 255;
                        b = (b * a + 255 * (255 - a)) / 255;
                    }
                    rgb[idx++] = (byte) r;
                    rgb[idx++] = (byte) g;
                    rgb[idx++] = (byte) b;
                }
            }
            return build(w, h, "FlateDecode", "DeviceRGB", flate(rgb));
        } catch (Exception e) {
            LOG.warning(() -> "XFA image decode failed: " + e.getMessage());
            return null;
        }
    }

    /// Rasterizes a QR Code module matrix into a 1-bit DeviceGray Image XObject (dark module = black),
    /// surrounded by a `quiet`-module white margin (the QR spec mandates ≥4). One image sample
    /// per module keeps the stream tiny; the `cm`+`Do` scale to the field box does the rest.
    ///
    /// @param matrix`matrix[y][x]` = `true` for a dark module
    /// @param quiet  the quiet-zone width in modules (4 per ISO/IEC 18004)
    /// @return the Image XObject stream
    public static PdfStream qrImage(boolean[][] matrix, int quiet) {
        int modules = matrix.length;
        // Emit several samples per module so the symbol stays crisp after the viewer scales the image to
        // the field box: a 1-sample-per-module source blurs under bilinear scaling and a dense (high
        // version) symbol then drops below the per-module pixel count a scanner needs (measured: v16
        // failed to decode at 1x, decodes cleanly at this density).
        int scale = 8;
        int gridModules = modules + 2 * quiet;
        int dim = gridModules * scale;
        int rowBytes = (dim + 7) / 8;
        byte[] data = new byte[rowBytes * dim];
        java.util.Arrays.fill(data, (byte) 0xFF); // default white (sample value 1 = white in DeviceGray)
        for (int y = 0; y < modules; y++) {
            for (int x = 0; x < modules; x++) {
                if (matrix[y][x]) {
                    for (int sy = 0; sy < scale; sy++) {
                        int py = (y + quiet) * scale + sy;
                        int rowBase = py * rowBytes;
                        for (int sx = 0; sx < scale; sx++) {
                            int px = (x + quiet) * scale + sx;
                            data[rowBase + (px >>> 3)] &= ~(0x80 >>> (px & 7)); // clear bit = black
                        }
                    }
                }
            }
        }
        PdfStream s = new PdfStream();
        s.set(PdfName.of("Type"), PdfName.of("XObject"));
        s.set(PdfName.of("Subtype"), PdfName.of("Image"));
        s.set(PdfName.of("Width"), PdfInteger.valueOf(dim));
        s.set(PdfName.of("Height"), PdfInteger.valueOf(dim));
        s.set(PdfName.of("BitsPerComponent"), PdfInteger.valueOf(1));
        s.set(PdfName.of("ColorSpace"), PdfName.of("DeviceGray"));
        s.set(PdfName.of("Filter"), PdfName.of("FlateDecode"));
        s.setEncodedData(flate(data));
        return s;
    }

    private static PdfStream build(int w, int h, String filter, String colorSpace, byte[] bytes) {
        PdfStream s = new PdfStream();
        s.set(PdfName.of("Type"), PdfName.of("XObject"));
        s.set(PdfName.of("Subtype"), PdfName.of("Image"));
        s.set(PdfName.of("Width"), PdfInteger.valueOf(w));
        s.set(PdfName.of("Height"), PdfInteger.valueOf(h));
        s.set(PdfName.of("BitsPerComponent"), PdfInteger.valueOf(8));
        s.set(PdfName.of("ColorSpace"), PdfName.of(colorSpace));
        s.set(PdfName.of("Filter"), PdfName.of(filter));
        s.setEncodedData(bytes);
        return s;
    }

    private static byte[] flate(byte[] raw) {
        Deflater def = new Deflater();
        def.setInput(raw);
        def.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length / 2);
        byte[] buf = new byte[8192];
        while (!def.finished()) {
            int n = def.deflate(buf);
            out.write(buf, 0, n);
        }
        def.end();
        return out.toByteArray();
    }

    /// Parses the first JPEG SOFn marker for {width, height, components}, or null.
    private static int[] jpegDimensions(byte[] d) {
        int p = 2;
        while (p + 9 < d.length) {
            if ((d[p] & 0xFF) != 0xFF) {
                p++;
                continue;
            }
            int marker = d[p + 1] & 0xFF;
            // SOF0..SOF15 except DHT(C4)/JPG(C8)/DAC(CC) carry frame dimensions.
            if (marker >= 0xC0 && marker <= 0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC) {
                int height = ((d[p + 5] & 0xFF) << 8) | (d[p + 6] & 0xFF);
                int width = ((d[p + 7] & 0xFF) << 8) | (d[p + 8] & 0xFF);
                int comps = d[p + 9] & 0xFF;
                return new int[]{width, height, comps};
            }
            int len = ((d[p + 2] & 0xFF) << 8) | (d[p + 3] & 0xFF);
            if (len < 2) {
                return null;
            }
            p += 2 + len;
        }
        return null;
    }
}
