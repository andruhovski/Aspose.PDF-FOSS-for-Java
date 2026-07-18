package org.aspose.pdf.engine.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Sanity tests for the 1D inverse DWT lifting routines used by
/// [JPXDecodeFilter]. The tests construct sub-band coefficient inputs
/// with known properties (e.g., LL only, constant) and assert that the
/// inverse lifting produces the expected reconstruction.
public class JPXDecodeFilterDWTTest {

    private static final double EPS_97 = 1e-3;
    private static final double K_97   = 1.230174104914001;

    /// 5/3 inverse DWT on a buffer whose low half is constant K and high half
    /// is zero must reconstruct the constant K everywhere. (5/3 has no extra
    /// scaling — lifting alone is exact for constant input.)
    @Test
    public void dwt53_constantLow_zeroHigh_reconstructsConstant() {
        int len = 16;
        int halfLen = (len + 1) / 2;
        int[] buf = new int[len];
        for (int i = 0; i < halfLen; i++) buf[i] = 1000;       // low (LL)
        // high (HL) already zero

        JPXDecodeFilter.inverseDWT53_1D(buf, 0, len);

        for (int i = 0; i < len; i++) {
            assertEquals(1000, buf[i], "5/3 IDWT of constant-low should reproduce constant; idx " + i);
        }
    }

    /// 9/7 inverse DWT: when low half = K (the spec's gain) and high half = 0,
    /// we expect the reconstruction to be 1.0 everywhere (since forward 9/7
    /// of constant-1 input divides the low coefficients by K).
    ///
    /// If the inverse incorrectly multiplied low by 1/K instead of K, the
    /// reconstruction would be 1/K^2 ≈ 0.66 instead of 1.0.
    @Test
    public void dwt97_constantLow_zeroHigh_reconstructsConstant() {
        int len = 16;
        int halfLen = (len + 1) / 2;
        double[] buf = new double[len];
        // If forward 9/7 of constant-1 input produces LL = 1/K (low samples
        // divided by K), then inverse should be fed 1/K to recover 1.
        // We feed K^-1 directly and expect 1.0 output.
        for (int i = 0; i < halfLen; i++) buf[i] = 1.0 / K_97; // low

        JPXDecodeFilter.inverseDWT97_1D(buf, 0, len);

        for (int i = 0; i < len; i++) {
            assertEquals(1.0, buf[i], EPS_97,
                    "9/7 IDWT of (1/K, ..., 1/K | 0,...,0) should reconstruct 1; idx " + i);
        }
    }

    /// Pins the no-input → no-output behaviour. Zero sub-bands must reconstruct
    /// zero exactly (no scaling can manufacture a signal from a zero input).
    @Test
    public void dwt97_zeroLow_zeroHigh_isZero() {
        double[] buf = new double[16];
        JPXDecodeFilter.inverseDWT97_1D(buf, 0, 16);
        for (int i = 0; i < 16; i++) {
            assertEquals(0.0, buf[i], EPS_97, "zero in → zero out, idx " + i);
        }
    }

    /// Alternative 9/7 convention: forward MULTIPLIES low by K instead of
    /// dividing. If THIS is the convention used by Kakadu / JPEG 2000 spec,
    /// feeding low = K to inverse should yield 1.0.
    @Test
    public void dwt97_constantLow_K_check_alternate() {
        int len = 16;
        int halfLen = (len + 1) / 2;
        double[] buf = new double[len];
        for (int i = 0; i < halfLen; i++) buf[i] = K_97;       // low = K

        JPXDecodeFilter.inverseDWT97_1D(buf, 0, len);

        System.out.println("[dwt97] feed K=" + K_97 + " expect ?");
        for (int i = 0; i < len; i++) {
            System.out.printf("  out[%d] = %.6f%n", i, buf[i]);
        }
    }

    /// Round-trip check: a manual 9/7 forward DWT followed by our inverse DWT
    /// should recover the original signal. We use a small test signal and
    /// implement the forward lifting inline.
    @Test
    public void dwt97_roundTrip_smallSignal() {
        double[] orig = {10, 20, 30, 40, 50, 60, 70, 80};
        double[] x = orig.clone();
        int len = x.length;

        // Forward 9/7 lifting (canonical ISO/IEC 15444-1 Annex F.4.6 order):
        // 1) d_n -= α (s_n + s_{n+1})  on odd indices
        // 2) s_n -= β (d_{n-1} + d_n)  on even
        // 3) d_n -= γ (s_n + s_{n+1})  on odd
        // 4) s_n -= δ (d_{n-1} + d_n)  on even
        // 5) scale: even *= 1/K, odd *= K
        final double A = -1.586134342, B = -0.052980118;
        final double G =  0.882911075, D =  0.443506852;

        // mirror boundary helpers
        java.util.function.IntUnaryOperator mirror = i -> i < 0 ? -i : (i >= len ? 2*(len-1)-i : i);

        for (int i = 1; i < len; i += 2) {
            int li = mirror.applyAsInt(i - 1);
            int ri = mirror.applyAsInt(i + 1);
            x[i] += A * (x[li] + x[ri]);
        }
        for (int i = 0; i < len; i += 2) {
            int li = mirror.applyAsInt(i - 1);
            int ri = mirror.applyAsInt(i + 1);
            x[i] += B * (x[li] + x[ri]);
        }
        for (int i = 1; i < len; i += 2) {
            int li = mirror.applyAsInt(i - 1);
            int ri = mirror.applyAsInt(i + 1);
            x[i] += G * (x[li] + x[ri]);
        }
        for (int i = 0; i < len; i += 2) {
            int li = mirror.applyAsInt(i - 1);
            int ri = mirror.applyAsInt(i + 1);
            x[i] += D * (x[li] + x[ri]);
        }
        // Scaling: convention 1 — even (low) /= K², odd (high) *= K². The inverse
        // multiplies low by K² so that the lifting's residual 1/K loss-side
        // gain produces the correct DC reconstruction (see DeviceCMYK comments
        // and dwt97_constantLow_zeroHigh_reconstructsConstant).
        double K2 = K_97 * K_97;
        double[] sc1 = x.clone();
        for (int i = 0; i < len; i += 2) sc1[i] /= K2;
        for (int i = 1; i < len; i += 2) sc1[i] *= K2;
        // Convention 2 — even *= K, odd /= K (legacy, won't round-trip under
        // the K² inverse; left as a probe).
        double[] sc2 = x.clone();
        for (int i = 0; i < len; i += 2) sc2[i] *= K_97;
        for (int i = 1; i < len; i += 2) sc2[i] /= K_97;

        // De-interleave each into [low ... | high ...] layout that our IDWT expects.
        int halfLen = (len + 1) / 2;
        double[] sub1 = new double[len], sub2 = new double[len];
        for (int n = 0; n < halfLen; n++) sub1[n] = sc1[2 * n];
        for (int n = 0; n < len - halfLen; n++) sub1[halfLen + n] = sc1[2 * n + 1];
        for (int n = 0; n < halfLen; n++) sub2[n] = sc2[2 * n];
        for (int n = 0; n < len - halfLen; n++) sub2[halfLen + n] = sc2[2 * n + 1];

        // Apply our inverse
        double[] rec1 = sub1.clone();
        double[] rec2 = sub2.clone();
        JPXDecodeFilter.inverseDWT97_1D(rec1, 0, len);
        JPXDecodeFilter.inverseDWT97_1D(rec2, 0, len);

        System.out.println("[roundtrip] orig=" + java.util.Arrays.toString(orig));
        System.out.println("[roundtrip] rec  (forward /K, *K — convention 1): "
                + java.util.Arrays.toString(rec1));
        System.out.println("[roundtrip] rec  (forward *K, /K — convention 2): "
                + java.util.Arrays.toString(rec2));

        // One of these must match (within EPS).
        boolean conv1ok = true, conv2ok = true;
        for (int i = 0; i < len; i++) {
            if (Math.abs(rec1[i] - orig[i]) > 1e-3) conv1ok = false;
            if (Math.abs(rec2[i] - orig[i]) > 1e-3) conv2ok = false;
        }
        System.out.println("[roundtrip] convention1 OK=" + conv1ok + "   convention2 OK=" + conv2ok);
        assertTrue(conv1ok || conv2ok, "Neither scaling convention round-trips");
    }
}
