package org.aspose.pdf.engine.function;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSStream;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Type 0 (Sampled) function (ISO 32000-1:2008, §7.10.2).
 * Uses a table of sample values with linear interpolation.
 *
 * <p>The function is defined by a multi-dimensional array of sample values.
 * Input values are mapped through Encode/Decode arrays and interpolated
 * between adjacent samples.</p>
 */
public final class SampledFunction extends PdfFunction {

    private static final Logger LOG = Logger.getLogger(SampledFunction.class.getName());

    private final int[] size;
    private final int bitsPerSample;
    private final double[] encode;
    private final double[] decode;
    private final int numOutputs;
    private final int[] samples; // flat array of all sample values (unsigned ints)

    /**
     * Creates a sampled function from a COS stream dictionary.
     *
     * @param dict   the function stream dictionary
     * @param domain the input domain
     * @param range  the output range
     * @throws IOException if the sample data cannot be read
     */
    public SampledFunction(COSDictionary dict, double[] domain, double[] range)
            throws IOException {
        super(domain, range);

        double[] sizeArr = getNumberArray(dict, "Size");
        this.size = sizeArr != null ? toIntArray(sizeArr) : new int[]{2};
        this.bitsPerSample = dict.getInt("BitsPerSample", 8);
        this.numOutputs = range != null ? range.length / 2 : 1;

        double[] enc = getNumberArray(dict, "Encode");
        if (enc != null) {
            this.encode = enc;
        } else {
            // Default encode: [0 Size[i]-1] for each input
            this.encode = new double[size.length * 2];
            for (int i = 0; i < size.length; i++) {
                this.encode[2 * i] = 0;
                this.encode[2 * i + 1] = size[i] - 1;
            }
        }

        double[] dec = getNumberArray(dict, "Decode");
        if (dec != null) {
            this.decode = dec;
        } else {
            // Default decode: same as range
            this.decode = range != null ? range.clone() : new double[]{0, 1};
        }

        // Read sample data from stream
        if (dict instanceof COSStream) {
            byte[] data = ((COSStream) dict).getDecodedData();
            this.samples = decodeSamples(data);
        } else {
            this.samples = new int[0];
        }
    }

    @Override
    public double[] evaluate(double[] input) {
        if (samples.length == 0 || size.length == 0) {
            return new double[numOutputs];
        }

        // For 1-D input (most common case): linear interpolation
        int m = getInputDimension();
        if (m == 1) {
            return evaluate1D(input[0]);
        }

        // Multi-dimensional: evaluate with nearest-neighbor for simplicity
        return evaluateND(input);
    }

    private double[] evaluate1D(double x) {
        x = clamp(x, domain[0], domain[1]);
        // Encode: map domain to [0, Size-1]
        double encoded;
        if (domain[1] == domain[0]) {
            encoded = encode[0];
        } else {
            encoded = encode[0] + (x - domain[0]) * (encode[1] - encode[0])
                    / (domain[1] - domain[0]);
        }
        encoded = clamp(encoded, 0, size[0] - 1);

        // Interpolation indices
        int i0 = (int) Math.floor(encoded);
        int i1 = Math.min(i0 + 1, size[0] - 1);
        double frac = encoded - i0;

        double maxSample = (1 << bitsPerSample) - 1;
        double[] result = new double[numOutputs];
        for (int j = 0; j < numOutputs; j++) {
            int sampleIdx0 = i0 * numOutputs + j;
            int sampleIdx1 = i1 * numOutputs + j;
            double s0 = (sampleIdx0 < samples.length) ? samples[sampleIdx0] : 0;
            double s1 = (sampleIdx1 < samples.length) ? samples[sampleIdx1] : 0;

            // Interpolate
            double interpolated = s0 + frac * (s1 - s0);

            // Decode: map from [0, maxSample] to decode range
            int decIdx = j * 2;
            double decMin = (decIdx < decode.length) ? decode[decIdx] : 0;
            double decMax = (decIdx + 1 < decode.length) ? decode[decIdx + 1] : 1;
            result[j] = decMin + (interpolated / maxSample) * (decMax - decMin);

            // Clamp to range
            if (range != null && j * 2 + 1 < range.length) {
                result[j] = clamp(result[j], range[j * 2], range[j * 2 + 1]);
            }
        }
        return result;
    }

    private double[] evaluateND(double[] input) {
        // Nearest-neighbor for multi-dimensional (simplified)
        int m = getInputDimension();
        int flatIndex = 0;
        int stride = numOutputs;
        for (int dim = m - 1; dim >= 0; dim--) {
            double x = clamp(input[dim], domain[dim * 2], domain[dim * 2 + 1]);
            double encoded;
            if (domain[dim * 2 + 1] == domain[dim * 2]) {
                encoded = encode[dim * 2];
            } else {
                encoded = encode[dim * 2] + (x - domain[dim * 2])
                        * (encode[dim * 2 + 1] - encode[dim * 2])
                        / (domain[dim * 2 + 1] - domain[dim * 2]);
            }
            int idx = (int) Math.round(clamp(encoded, 0, size[dim] - 1));
            flatIndex += idx * stride;
            stride *= size[dim];
        }

        double maxSample = (1 << bitsPerSample) - 1;
        double[] result = new double[numOutputs];
        for (int j = 0; j < numOutputs; j++) {
            int si = flatIndex + j;
            double s = (si < samples.length) ? samples[si] : 0;
            int decIdx = j * 2;
            double decMin = (decIdx < decode.length) ? decode[decIdx] : 0;
            double decMax = (decIdx + 1 < decode.length) ? decode[decIdx + 1] : 1;
            result[j] = decMin + (s / maxSample) * (decMax - decMin);
            if (range != null && j * 2 + 1 < range.length) {
                result[j] = clamp(result[j], range[j * 2], range[j * 2 + 1]);
            }
        }
        return result;
    }

    private int[] decodeSamples(byte[] data) {
        int totalSamples = numOutputs;
        for (int s : size) totalSamples *= s;

        int[] result = new int[totalSamples];
        if (bitsPerSample == 8) {
            for (int i = 0; i < Math.min(totalSamples, data.length); i++) {
                result[i] = data[i] & 0xFF;
            }
        } else if (bitsPerSample == 16) {
            for (int i = 0; i < totalSamples && i * 2 + 1 < data.length; i++) {
                result[i] = ((data[i * 2] & 0xFF) << 8) | (data[i * 2 + 1] & 0xFF);
            }
        } else if (bitsPerSample == 32) {
            for (int i = 0; i < totalSamples && i * 4 + 3 < data.length; i++) {
                result[i] = ((data[i * 4] & 0xFF) << 24) | ((data[i * 4 + 1] & 0xFF) << 16)
                        | ((data[i * 4 + 2] & 0xFF) << 8) | (data[i * 4 + 3] & 0xFF);
            }
        } else {
            // Bit-packed samples
            int bitPos = 0;
            for (int i = 0; i < totalSamples; i++) {
                int value = 0;
                for (int b = bitsPerSample - 1; b >= 0; b--) {
                    int byteIdx = bitPos / 8;
                    int bitIdx = 7 - (bitPos % 8);
                    if (byteIdx < data.length) {
                        value |= ((data[byteIdx] >> bitIdx) & 1) << b;
                    }
                    bitPos++;
                }
                result[i] = value;
            }
        }
        return result;
    }

    private static int[] toIntArray(double[] arr) {
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) result[i] = (int) arr[i];
        return result;
    }
}
