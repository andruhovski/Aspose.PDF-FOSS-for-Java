package org.aspose.pdf.engine.render;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.logging.Logger;

/// Separable blend-mode composite for PDF /BM (ISO 32000-1:2008, §11.3.5).
///
/// Java2D ships only Porter-Duff composites; PDF content (notably Highlight
/// annotation appearance streams, corpus 30894) relies on the Multiply blend
/// mode so the markup darkens the page instead of covering it. Only Multiply
/// is implemented — other separable modes fall back to normal SRC\_OVER at the
/// [#fillComposite] level.
///
public final class BlendComposite implements Composite {

    private static final Logger LOG = Logger.getLogger(BlendComposite.class.getName());

    /// Constant alpha (the PDF /ca value) applied on top of the blend.
    private final float alpha;

    private BlendComposite(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
    }

    /// Returns the composite for a non-stroking paint op under the given
    /// graphics state: a Multiply blender when /BM is Multiply (or the
    /// equivalent Darken on white-ish backdrops would differ — only Multiply
    /// is special-cased), otherwise plain SRC\_OVER with the /ca alpha.
    ///
    /// @param state the current graphics state
    /// @return the composite to install on the Graphics2D
    public static Composite fillComposite(GraphicsState state) {
        float ca = state.getNonStrokingAlpha();
        if ("Multiply".equals(state.getBlendMode())) {
            return new BlendComposite(ca);
        }
        return ca < 1.0f
                ? AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ca)
                : AlphaComposite.SrcOver;
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel,
                                          RenderingHints hints) {
        return new MultiplyContext(alpha);
    }

    /// Multiply: C = Cs × Cb (per channel), then standard source-over with
    /// the source alpha × constant alpha (§11.3.5.2, B(Cb, Cs) = Cb × Cs).
    private static final class MultiplyContext implements CompositeContext {
        private final float alpha;

        MultiplyContext(float alpha) {
            this.alpha = alpha;
        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int w = Math.min(src.getWidth(), dstIn.getWidth());
            int h = Math.min(src.getHeight(), dstIn.getHeight());
            int sBands = src.getNumBands();
            int dBands = dstIn.getNumBands();
            int[] sp = new int[w * sBands];
            int[] dp = new int[w * dBands];

            for (int y = 0; y < h; y++) {
                src.getPixels(src.getMinX(), src.getMinY() + y, w, 1, sp);
                dstIn.getPixels(dstIn.getMinX(), dstIn.getMinY() + y, w, 1, dp);

                for (int x = 0; x < w; x++) {
                    int si = x * sBands;
                    int di = x * dBands;
                    int srcAlpha = sBands > 3 ? sp[si + 3] : 255;
                    float as = (srcAlpha / 255f) * alpha;
                    if (as <= 0f) continue;

                    for (int c = 0; c < 3 && c < sBands && c < dBands; c++) {
                        int cs = sp[si + c];
                        int cb = dp[di + c];
                        int blended = (cs * cb) / 255;
                        dp[di + c] = clamp((int) (cb + (blended - cb) * as));
                    }
                    if (dBands > 3) {
                        int da = dp[di + 3];
                        dp[di + 3] = clamp((int) (da + (255 - da) * as));
                    }
                }
                dstOut.setPixels(dstOut.getMinX(), dstOut.getMinY() + y, w, 1, dp);
            }
        }

        private static int clamp(int v) {
            return v < 0 ? 0 : Math.min(v, 255);
        }

        @Override
        public void dispose() {
            // no resources held
        }
    }
}
