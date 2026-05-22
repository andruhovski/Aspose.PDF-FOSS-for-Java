package org.aspose.pdf;

import org.aspose.pdf.tagged.AutoTaggingSettings;

import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Options controlling PDF/A (and other standard) validation and conversion.
 * <p>
 * Holds the target format, the error handling strategy, the transparency handling
 * strategy, and the destination for the validation/conversion log.
 * </p>
 */
public class PdfFormatConversionOptions {

    /**
     * Flags controlling how fonts may be removed or subsetted while optimizing
     * conversion output size.
     */
    public enum RemoveFontsStrategy {
        /** Keep all fonts intact. */
        None(0),
        /** Subset embedded fonts where possible. */
        SubsetFonts(1),
        /** Remove duplicated embedded fonts where possible. */
        RemoveDuplicatedFonts(2);

        private final int value;

        RemoveFontsStrategy(int value) {
            this.value = value;
        }

        /**
         * Returns the numeric flag value.
         *
         * @return the flag value
         */
        public int getValue() {
            return value;
        }
    }

    private static final Logger LOG = Logger.getLogger(PdfFormatConversionOptions.class.getName());

    private String logFileName;
    private OutputStream logStream;
    private PdfFormat format;
    private ConvertErrorAction errorAction;
    private ConvertTransparencyAction transparencyAction;

    // Font handling options
    private boolean embedFonts = true;
    private boolean subsetFonts = true;
    private String defaultFontName = "Helvetica";
    private boolean ignoreResourceFontErrors = false;

    // Text alignment during conversion
    private boolean alignText = false;

    // Soft mask handling
    private ConvertSoftMaskAction softMaskAction = ConvertSoftMaskAction.Default;

    // Auto-tagging settings
    private AutoTaggingSettings autoTaggingSettings;
    private boolean optimizeFileSize;
    private boolean lowMemoryMode;
    private boolean transferInfo;
    private String iccProfileFileName;
    private FontEmbeddingOptions fontEmbeddingOptions = new FontEmbeddingOptions();
    private int excludeFontsStrategy;

    /**
     * Creates options for the supplied format using the default delete-on-error behavior.
     *
     * @param format the target PDF format
     */
    public PdfFormatConversionOptions(PdfFormat format) {
        this((String) null, format, ConvertErrorAction.Delete);
    }

    /**
     * Creates options for the supplied format and error action without predefined logging.
     *
     * @param format      the target PDF format
     * @param errorAction the action to take on non-compliant elements
     */
    public PdfFormatConversionOptions(PdfFormat format, ConvertErrorAction errorAction) {
        this((String) null, format, errorAction);
    }

    /**
     * Creates options that write the conversion log to a file.
     *
     * @param logFileName  the path to the log file (may be {@code null} to suppress file logging)
     * @param format       the target PDF format
     * @param errorAction  the action to take on non-compliant elements
     * @throws IllegalArgumentException if format or errorAction is {@code null}
     */
    public PdfFormatConversionOptions(String logFileName, PdfFormat format, ConvertErrorAction errorAction) {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null");
        }
        if (errorAction == null) {
            throw new IllegalArgumentException("errorAction must not be null");
        }
        this.logFileName = logFileName;
        this.format = format;
        this.errorAction = errorAction;
        this.transparencyAction = ConvertTransparencyAction.Default;
    }

    /**
     * Creates options that write the conversion log to an output stream.
     *
     * @param logStream    the stream to receive the log (may be {@code null})
     * @param format       the target PDF format
     * @param errorAction  the action to take on non-compliant elements
     * @throws IllegalArgumentException if format or errorAction is {@code null}
     */
    public PdfFormatConversionOptions(OutputStream logStream, PdfFormat format, ConvertErrorAction errorAction) {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null");
        }
        if (errorAction == null) {
            throw new IllegalArgumentException("errorAction must not be null");
        }
        this.logStream = logStream;
        this.format = format;
        this.errorAction = errorAction;
        this.transparencyAction = ConvertTransparencyAction.Default;
    }

    /**
     * Returns a conservative default option set used by simple validation calls.
     *
     * @return default conversion options targeting PDF/A-1b
     */
    public static PdfFormatConversionOptions getDefault() {
        return new PdfFormatConversionOptions(PdfFormat.PDF_A_1B);
    }

    /**
     * Returns the log file name, or {@code null} if not set.
     *
     * @return the log file path
     */
    public String getLogFileName() {
        return logFileName;
    }

    /**
     * Sets the log file name.
     *
     * @param logFileName the log file path, or {@code null} to disable file logging
     */
    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    /**
     * Returns the log output stream, or {@code null} if not set.
     *
     * @return the log stream
     */
    public OutputStream getLogStream() {
        return logStream;
    }

    /**
     * Sets the log output stream.
     *
     * @param logStream the stream for log output, or {@code null}
     */
    public void setLogStream(OutputStream logStream) {
        this.logStream = logStream;
    }

    /**
     * Returns the target PDF format.
     *
     * @return the format
     */
    public PdfFormat getFormat() {
        return format;
    }

    /**
     * Sets the target PDF format.
     *
     * @param format the format (must not be {@code null})
     * @throws IllegalArgumentException if format is {@code null}
     */
    public void setFormat(PdfFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null");
        }
        this.format = format;
    }

    /**
     * Returns the error action strategy.
     *
     * @return the error action
     */
    public ConvertErrorAction getErrorAction() {
        return errorAction;
    }

    /**
     * Sets the error action strategy.
     *
     * @param errorAction the action (must not be {@code null})
     * @throws IllegalArgumentException if errorAction is {@code null}
     */
    public void setErrorAction(ConvertErrorAction errorAction) {
        if (errorAction == null) {
            throw new IllegalArgumentException("errorAction must not be null");
        }
        this.errorAction = errorAction;
    }

    /**
     * Returns the transparency handling action.
     *
     * @return the transparency action
     */
    public ConvertTransparencyAction getTransparencyAction() {
        return transparencyAction;
    }

    /**
     * Sets the transparency handling action.
     *
     * @param transparencyAction the action (must not be {@code null})
     * @throws IllegalArgumentException if transparencyAction is {@code null}
     */
    public void setTransparencyAction(ConvertTransparencyAction transparencyAction) {
        if (transparencyAction == null) {
            throw new IllegalArgumentException("transparencyAction must not be null");
        }
        this.transparencyAction = transparencyAction;
    }

    /**
     * Returns whether fonts should be embedded during conversion.
     *
     * @return {@code true} if fonts will be embedded (default)
     */
    public boolean isEmbedFonts() {
        return embedFonts;
    }

    /**
     * Sets whether fonts should be embedded during conversion.
     *
     * @param embedFonts {@code true} to embed fonts
     */
    public void setEmbedFonts(boolean embedFonts) {
        this.embedFonts = embedFonts;
    }

    /**
     * Returns whether embedded fonts should be subsetted.
     *
     * @return {@code true} if fonts will be subsetted (default)
     */
    public boolean isSubsetFonts() {
        return subsetFonts;
    }

    /**
     * Sets whether embedded fonts should be subsetted.
     *
     * @param subsetFonts {@code true} to subset fonts
     */
    public void setSubsetFonts(boolean subsetFonts) {
        this.subsetFonts = subsetFonts;
    }

    /**
     * Returns the default font name used when a font cannot be found.
     *
     * @return the default font name
     */
    public String getDefaultFontName() {
        return defaultFontName;
    }

    /**
     * Sets the default font name used when a font cannot be found.
     *
     * @param defaultFontName the font name (must not be {@code null})
     * @throws IllegalArgumentException if defaultFontName is {@code null}
     */
    public void setDefaultFontName(String defaultFontName) {
        if (defaultFontName == null) {
            throw new IllegalArgumentException("defaultFontName must not be null");
        }
        this.defaultFontName = defaultFontName;
    }

    /**
     * Returns whether resource font errors should be ignored during conversion.
     *
     * @return {@code true} if font errors are ignored
     */
    public boolean isIgnoreResourceFontErrors() {
        return ignoreResourceFontErrors;
    }

    /**
     * Sets whether resource font errors should be ignored during conversion.
     *
     * @param ignoreResourceFontErrors {@code true} to ignore font errors
     */
    public void setIgnoreResourceFontErrors(boolean ignoreResourceFontErrors) {
        this.ignoreResourceFontErrors = ignoreResourceFontErrors;
    }

    /**
     * Returns whether text alignment is enabled during conversion.
     *
     * @return {@code true} if text alignment is enabled
     */
    public boolean isAlignText() {
        return alignText;
    }

    /**
     * Sets whether text alignment should be applied during conversion.
     *
     * @param alignText {@code true} to enable text alignment
     */
    public void setAlignText(boolean alignText) {
        this.alignText = alignText;
    }

    /**
     * Returns the soft mask handling action.
     *
     * @return the soft mask action
     */
    public ConvertSoftMaskAction getSoftMaskAction() {
        return softMaskAction;
    }

    /**
     * Sets the soft mask handling action.
     *
     * @param softMaskAction the action (must not be {@code null})
     * @throws IllegalArgumentException if softMaskAction is {@code null}
     */
    public void setSoftMaskAction(ConvertSoftMaskAction softMaskAction) {
        if (softMaskAction == null) {
            throw new IllegalArgumentException("softMaskAction must not be null");
        }
        this.softMaskAction = softMaskAction;
    }

    /**
     * Returns the auto-tagging settings, or {@code null} if not set.
     *
     * @return the auto-tagging settings
     */
    public AutoTaggingSettings getAutoTaggingSettings() {
        return autoTaggingSettings;
    }

    /**
     * Sets the auto-tagging settings for automatic structure tagging during conversion.
     *
     * @param autoTaggingSettings the auto-tagging settings, or {@code null} to disable
     */
    public void setAutoTaggingSettings(AutoTaggingSettings autoTaggingSettings) {
        this.autoTaggingSettings = autoTaggingSettings;
    }

    /**
     * Returns whether conversion should attempt to minimize the output file size.
     *
     * @return true if file-size optimization is enabled
     */
    public boolean isOptimizeFileSize() {
        return optimizeFileSize;
    }

    /**
     * Sets whether conversion should attempt to minimize the output file size.
     *
     * @param optimizeFileSize true to enable file-size optimization
     */
    public void setOptimizeFileSize(boolean optimizeFileSize) {
        this.optimizeFileSize = optimizeFileSize;
    }

    /**
     * Returns whether low-memory conversion mode is enabled.
     *
     * @return true if low-memory mode is enabled
     */
    public boolean isLowMemoryMode() {
        return lowMemoryMode;
    }

    /**
     * Sets whether low-memory conversion mode is enabled.
     *
     * @param lowMemoryMode true to enable low-memory mode
     */
    public void setIsLowMemoryMode(boolean lowMemoryMode) {
        this.lowMemoryMode = lowMemoryMode;
    }

    /**
     * Returns whether document information should be transferred into generated metadata.
     *
     * @return true if document information transfer is enabled
     */
    public boolean isTransferInfo() {
        return transferInfo;
    }

    /**
     * Sets whether document information should be transferred into generated metadata.
     *
     * @param transferInfo true to enable transfer
     */
    public void setTransferInfo(boolean transferInfo) {
        this.transferInfo = transferInfo;
    }

    /**
     * Returns the ICC profile file name used for output intent generation.
     *
     * @return the ICC profile path, or null if not specified
     */
    public String getIccProfileFileName() {
        return iccProfileFileName;
    }

    /**
     * Sets the ICC profile file name used for output intent generation.
     *
     * @param iccProfileFileName the ICC profile path, or null
     */
    public void setIccProfileFileName(String iccProfileFileName) {
        this.iccProfileFileName = iccProfileFileName;
    }

    /**
     * Returns font embedding options used by conversion.
     *
     * @return the font embedding options
     */
    public FontEmbeddingOptions getFontEmbeddingOptions() {
        return fontEmbeddingOptions;
    }

    /**
     * Sets font embedding options used by conversion.
     *
     * @param fontEmbeddingOptions the font embedding options, or null to reset to defaults
     */
    public void setFontEmbeddingOptions(FontEmbeddingOptions fontEmbeddingOptions) {
        this.fontEmbeddingOptions = fontEmbeddingOptions != null
                ? fontEmbeddingOptions
                : new FontEmbeddingOptions();
    }

    /**
     * Returns the bitwise font-exclusion strategy flags.
     *
     * @return the font strategy flags
     */
    public int getExcludeFontsStrategy() {
        return excludeFontsStrategy;
    }

    /**
     * Sets the bitwise font-exclusion strategy flags.
     *
     * @param excludeFontsStrategy the font strategy flags
     */
    public void setExcludeFontsStrategy(int excludeFontsStrategy) {
        this.excludeFontsStrategy = excludeFontsStrategy;
    }
}
