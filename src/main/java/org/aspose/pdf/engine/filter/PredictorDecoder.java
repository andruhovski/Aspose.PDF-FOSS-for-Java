package org.aspose.pdf.engine.filter;

import java.util.logging.Logger;

/**
 * PNG and TIFF predictor support for Flate/LZW filters (§7.4.4.4, ISO 32000-1:2008).
 * <p>
 * Predictors improve compression ratios by encoding pixel differences instead of
 * absolute values. PNG predictors (10–15) prepend a filter-type byte to each row.
 * TIFF predictor (2) uses horizontal differencing without a per-row type byte.
 * </p>
 */
public final class PredictorDecoder {

    private static final Logger LOG = Logger.getLogger(PredictorDecoder.class.getName());

    /** No prediction. */
    public static final int PREDICTOR_NONE = 1;
    /** TIFF Predictor 2: horizontal differencing. */
    public static final int PREDICTOR_TIFF = 2;
    /** PNG None predictor (per-row filter byte present, but filter is 0). */
    public static final int PREDICTOR_PNG_NONE = 10;
    /** PNG Sub predictor. */
    public static final int PREDICTOR_PNG_SUB = 11;
    /** PNG Up predictor. */
    public static final int PREDICTOR_PNG_UP = 12;
    /** PNG Average predictor. */
    public static final int PREDICTOR_PNG_AVERAGE = 13;
    /** PNG Paeth predictor. */
    public static final int PREDICTOR_PNG_PAETH = 14;
    /** PNG Optimum — each row may use a different filter. */
    public static final int PREDICTOR_PNG_OPTIMUM = 15;

    private PredictorDecoder() {
        // Utility class
    }

    /**
     * Decodes predictor-filtered data.
     *
     * @param data             the raw data after inflate/LZW decode
     * @param predictor        the predictor value (1, 2, or 10–15)
     * @param columns          number of sample values per row
     * @param colors           number of color components per sample
     * @param bitsPerComponent bits per component (typically 8)
     * @return decoded data with predictors reversed
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static byte[] decode(byte[] data, int predictor, int columns, int colors, int bitsPerComponent) {
        if (data == null || data.length == 0) {
            return data == null ? new byte[0] : data;
        }
        if (predictor == PREDICTOR_NONE) {
            return data;
        }
        if (predictor == PREDICTOR_TIFF) {
            return decodeTIFF(data, columns, colors, bitsPerComponent);
        }
        if (predictor >= 10 && predictor <= 15) {
            return decodePNG(data, columns, colors, bitsPerComponent);
        }
        LOG.warning("Unknown predictor value: " + predictor + ", returning data unchanged");
        return data;
    }

    /**
     * Encodes data with the specified predictor.
     *
     * @param data             the raw data to encode
     * @param predictor        the predictor value (1, 2, or 10–15)
     * @param columns          number of sample values per row
     * @param colors           number of color components per sample
     * @param bitsPerComponent bits per component (typically 8)
     * @return predictor-encoded data
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static byte[] encode(byte[] data, int predictor, int columns, int colors, int bitsPerComponent) {
        if (data == null || data.length == 0) {
            return data == null ? new byte[0] : data;
        }
        if (predictor == PREDICTOR_NONE) {
            return data;
        }
        if (predictor == PREDICTOR_TIFF) {
            return encodeTIFF(data, columns, colors, bitsPerComponent);
        }
        if (predictor >= 10 && predictor <= 15) {
            return encodePNG(data, predictor, columns, colors, bitsPerComponent);
        }
        LOG.warning("Unknown predictor value: " + predictor + ", returning data unchanged");
        return data;
    }

    // ---- PNG predictor decode ----

    private static byte[] decodePNG(byte[] data, int columns, int colors, int bitsPerComponent) {
        int bytesPerPixel = Math.max(1, (colors * bitsPerComponent + 7) / 8);
        int bytesPerRow = columns * colors * bitsPerComponent / 8;
        // Each row in the input has 1 filter-type byte + bytesPerRow data bytes
        int inputRowSize = bytesPerRow + 1;

        if (data.length % inputRowSize != 0) {
            LOG.warning("PNG predictor data length " + data.length +
                    " not divisible by row size " + inputRowSize + "; adjusting");
        }

        int numRows = data.length / inputRowSize;
        byte[] result = new byte[numRows * bytesPerRow];
        byte[] prevRow = new byte[bytesPerRow];
        byte[] currentRow = new byte[bytesPerRow];

        for (int row = 0; row < numRows; row++) {
            int srcOffset = row * inputRowSize;
            int filterType = data[srcOffset] & 0xFF;

            for (int i = 0; i < bytesPerRow; i++) {
                int raw = data[srcOffset + 1 + i] & 0xFF;
                int a = (i >= bytesPerPixel) ? (currentRow[i - bytesPerPixel] & 0xFF) : 0;
                int b = prevRow[i] & 0xFF;
                int c = (i >= bytesPerPixel) ? (prevRow[i - bytesPerPixel] & 0xFF) : 0;

                int decoded;
                switch (filterType) {
                    case 0: // None
                        decoded = raw;
                        break;
                    case 1: // Sub
                        decoded = (raw + a) & 0xFF;
                        break;
                    case 2: // Up
                        decoded = (raw + b) & 0xFF;
                        break;
                    case 3: // Average
                        decoded = (raw + ((a + b) / 2)) & 0xFF;
                        break;
                    case 4: // Paeth
                        decoded = (raw + paethPredictor(a, b, c)) & 0xFF;
                        break;
                    default:
                        LOG.warning("Unknown PNG filter type: " + filterType + " in row " + row);
                        decoded = raw;
                        break;
                }
                currentRow[i] = (byte) decoded;
            }

            System.arraycopy(currentRow, 0, result, row * bytesPerRow, bytesPerRow);
            // Swap prev and current
            byte[] tmp = prevRow;
            prevRow = currentRow;
            currentRow = tmp;
        }

        return result;
    }

    // ---- PNG predictor encode ----

    private static byte[] encodePNG(byte[] data, int predictor, int columns, int colors, int bitsPerComponent) {
        int bytesPerPixel = Math.max(1, (colors * bitsPerComponent + 7) / 8);
        int bytesPerRow = columns * colors * bitsPerComponent / 8;

        if (data.length % bytesPerRow != 0) {
            LOG.warning("PNG predictor encode: data length " + data.length +
                    " not divisible by row size " + bytesPerRow);
        }

        int numRows = data.length / bytesPerRow;
        int outputRowSize = bytesPerRow + 1;
        byte[] result = new byte[numRows * outputRowSize];
        byte[] prevRow = new byte[bytesPerRow];

        // Map predictor constant to PNG filter type
        int filterType;
        switch (predictor) {
            case PREDICTOR_PNG_NONE:
                filterType = 0;
                break;
            case PREDICTOR_PNG_SUB:
                filterType = 1;
                break;
            case PREDICTOR_PNG_UP:
                filterType = 2;
                break;
            case PREDICTOR_PNG_AVERAGE:
                filterType = 3;
                break;
            case PREDICTOR_PNG_PAETH:
            case PREDICTOR_PNG_OPTIMUM:
                filterType = 4;
                break;
            default:
                filterType = 0;
                break;
        }

        for (int row = 0; row < numRows; row++) {
            int srcOffset = row * bytesPerRow;
            int dstOffset = row * outputRowSize;
            result[dstOffset] = (byte) filterType;

            for (int i = 0; i < bytesPerRow; i++) {
                int x = data[srcOffset + i] & 0xFF;
                int a = (i >= bytesPerPixel) ? (data[srcOffset + i - bytesPerPixel] & 0xFF) : 0;
                int b = prevRow[i] & 0xFF;
                int c = (i >= bytesPerPixel) ? (prevRow[i - bytesPerPixel] & 0xFF) : 0;

                int encoded;
                switch (filterType) {
                    case 0:
                        encoded = x;
                        break;
                    case 1: // Sub
                        encoded = (x - a) & 0xFF;
                        break;
                    case 2: // Up
                        encoded = (x - b) & 0xFF;
                        break;
                    case 3: // Average
                        encoded = (x - ((a + b) / 2)) & 0xFF;
                        break;
                    case 4: // Paeth
                        encoded = (x - paethPredictor(a, b, c)) & 0xFF;
                        break;
                    default:
                        encoded = x;
                        break;
                }
                result[dstOffset + 1 + i] = (byte) encoded;
            }

            System.arraycopy(data, srcOffset, prevRow, 0, bytesPerRow);
        }

        return result;
    }

    // ---- TIFF predictor ----

    private static byte[] decodeTIFF(byte[] data, int columns, int colors, int bitsPerComponent) {
        int bytesPerPixel = Math.max(1, (colors * bitsPerComponent + 7) / 8);
        int bytesPerRow = columns * colors * bitsPerComponent / 8;

        if (data.length % bytesPerRow != 0) {
            LOG.warning("TIFF predictor: data length " + data.length +
                    " not divisible by row size " + bytesPerRow);
        }

        int numRows = data.length / bytesPerRow;
        byte[] result = new byte[data.length];

        for (int row = 0; row < numRows; row++) {
            int offset = row * bytesPerRow;
            // Copy first pixel bytes as-is
            for (int i = 0; i < bytesPerPixel && i < bytesPerRow; i++) {
                result[offset + i] = data[offset + i];
            }
            // Reverse horizontal differencing for remaining bytes
            for (int i = bytesPerPixel; i < bytesPerRow; i++) {
                result[offset + i] = (byte) ((data[offset + i] & 0xFF) + (result[offset + i - bytesPerPixel] & 0xFF));
            }
        }

        return result;
    }

    private static byte[] encodeTIFF(byte[] data, int columns, int colors, int bitsPerComponent) {
        int bytesPerPixel = Math.max(1, (colors * bitsPerComponent + 7) / 8);
        int bytesPerRow = columns * colors * bitsPerComponent / 8;
        int numRows = data.length / bytesPerRow;
        byte[] result = new byte[data.length];

        for (int row = 0; row < numRows; row++) {
            int offset = row * bytesPerRow;
            for (int i = 0; i < bytesPerPixel && i < bytesPerRow; i++) {
                result[offset + i] = data[offset + i];
            }
            for (int i = bytesPerPixel; i < bytesPerRow; i++) {
                result[offset + i] = (byte) ((data[offset + i] & 0xFF) - (data[offset + i - bytesPerPixel] & 0xFF));
            }
        }

        return result;
    }

    /**
     * Paeth predictor function (PNG specification, RFC 2083).
     *
     * @param a the byte to the left
     * @param b the byte above
     * @param c the byte to the upper-left
     * @return the predicted value
     */
    public static int paethPredictor(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        if (pa <= pb && pa <= pc) {
            return a;
        } else if (pb <= pc) {
            return b;
        } else {
            return c;
        }
    }
}
