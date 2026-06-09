package org.aspose.pdf.engine.colorspace;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Display-oriented DeviceCMYK → sRGB conversion (ISO 32000-1:2008, §8.6.4.4).
 * <p>
 * Real viewers (Adobe Acrobat, PDFBox, Chrome) render DeviceCMYK through a
 * press characterization profile, which differs visibly from the algebraic
 * {@code (1-C)(1-K)} formula: print-neutral grays (C &gt; M ≈ Y) come out
 * neutral instead of greenish, and solid inks are muted (pure cyan ≈
 * {@code rgb(0,174,239)}, pure black ≈ {@code rgb(35,31,32)}).
 * </p>
 * <p>
 * This class provides that conversion <b>without any runtime dependency</b>:
 * a 9×9×9×9 sRGB lookup table (19 683 bytes, classpath resource
 * {@code cmyk-display-9.lut}) sampled from the Apache-2.0-licensed
 * "CGATS001Compat-v2-micro" CMYK characterization profile (the same data
 * Apache PDFBox uses for DeviceCMYK rendering), interpolated quadrilinearly.
 * Interpolation error vs the full profile: mean &lt; 1, p95 ≤ 3.5, max ≈ 32
 * (one dark corner) per 8-bit channel.
 * </p>
 * <p>
 * NOTE: this is the <b>rendering</b> conversion. The public-API conversion
 * {@link DeviceCMYK#toRGBInt(double, double, double, double)} keeps the
 * algebraic formula — Aspose.PDF compatibility tests pin pure C/M/Y/K to
 * exact RGB primaries there.
 * </p>
 */
public final class CmykDisplay {

    private static final Logger LOG = Logger.getLogger(CmykDisplay.class.getName());

    /** Grid points per axis. */
    private static final int N = 9;
    /** LUT laid out C-major … K-minor, 3 bytes (sRGB) per node; null = load failed. */
    private static final byte[] LUT = loadLut();

    private CmykDisplay() {}

    private static byte[] loadLut() {
        try (InputStream in = CmykDisplay.class.getResourceAsStream("cmyk-display-9.lut")) {
            if (in == null) {
                LOG.warning("cmyk-display-9.lut resource missing; falling back to algebraic CMYK");
                return null;
            }
            byte[] data = new byte[N * N * N * N * 3];
            int off = 0;
            while (off < data.length) {
                int r = in.read(data, off, data.length - off);
                if (r < 0) break;
                off += r;
            }
            if (off != data.length) {
                LOG.warning("cmyk-display-9.lut truncated (" + off + " bytes); falling back");
                return null;
            }
            return data;
        } catch (IOException e) {
            LOG.warning("Failed to load cmyk-display-9.lut: " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts CMYK components (each 0..1) to a packed ARGB int using the
     * press-characterized LUT with quadrilinear interpolation.
     *
     * @param c cyan (0..1)
     * @param m magenta (0..1)
     * @param y yellow (0..1)
     * @param k black (0..1)
     * @return packed ARGB int (alpha=0xFF)
     */
    public static int toRGBInt(double c, double m, double y, double k) {
        if (LUT == null) {
            return DeviceCMYK.INSTANCE.toRGBInt(c, m, y, k);
        }
        double[] in = {clamp01(c), clamp01(m), clamp01(y), clamp01(k)};
        int[] i0 = new int[4];
        double[] f = new double[4];
        for (int i = 0; i < 4; i++) {
            double pos = in[i] * (N - 1);
            int idx = (int) pos;
            if (idx > N - 2) idx = N - 2;
            i0[i] = idx;
            f[i] = pos - idx;
        }
        double r = 0, g = 0, b = 0;
        for (int dc = 0; dc <= 1; dc++) {
            double wc = dc == 0 ? 1 - f[0] : f[0];
            if (wc == 0) continue;
            for (int dm = 0; dm <= 1; dm++) {
                double wm = wc * (dm == 0 ? 1 - f[1] : f[1]);
                if (wm == 0) continue;
                for (int dy = 0; dy <= 1; dy++) {
                    double wy = wm * (dy == 0 ? 1 - f[2] : f[2]);
                    if (wy == 0) continue;
                    for (int dk = 0; dk <= 1; dk++) {
                        double w = wy * (dk == 0 ? 1 - f[3] : f[3]);
                        if (w == 0) continue;
                        int base = (((i0[0] + dc) * N + (i0[1] + dm)) * N + (i0[2] + dy)) * N + (i0[3] + dk);
                        base *= 3;
                        r += w * (LUT[base] & 0xFF);
                        g += w * (LUT[base + 1] & 0xFF);
                        b += w * (LUT[base + 2] & 0xFF);
                    }
                }
            }
        }
        int ri = (int) Math.round(r), gi = (int) Math.round(g), bi = (int) Math.round(b);
        return 0xFF000000 | (Math.min(255, ri) << 16) | (Math.min(255, gi) << 8) | Math.min(255, bi);
    }

    /** Array variant for the {@link ColorSpaceBase#toRGBInt(double[])} pipeline. */
    public static int toRGBInt(double[] comps) {
        if (comps == null || comps.length < 4) return 0xFF000000;
        return toRGBInt(comps[0], comps[1], comps[2], comps[3]);
    }

    private static double clamp01(double v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }
}
