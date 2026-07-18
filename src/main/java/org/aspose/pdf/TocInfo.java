package org.aspose.pdf;

import org.aspose.pdf.text.TextFragment;
import org.aspose.pdf.text.TextState;

import java.util.logging.Logger;

/// Represents Table of Contents information for a PDF document.
///
/// Configures the title, formatting, and level-specific styles for
/// a generated table of contents.
///
public class TocInfo {

    private static final Logger LOG = Logger.getLogger(TocInfo.class.getName());

    private TextFragment title;
    private int formatArrayLength;
    private LevelFormat[] formatArray;

    /// Creates a TocInfo with default settings.
    public TocInfo() {
        // defaults
    }

    /// Returns the title text fragment for the table of contents.
    ///
    /// @return the title, or `null` if not set
    public TextFragment getTitle() {
        return title;
    }

    /// Sets the title text fragment for the table of contents.
    ///
    /// @param title the title text fragment
    public void setTitle(TextFragment title) {
        this.title = title;
    }

    /// Returns the number of levels in the format array.
    ///
    /// @return the format array length
    public int getFormatArrayLength() {
        return formatArrayLength;
    }

    /// Sets the number of levels in the format array.
    /// This also initializes the format array with the given length.
    ///
    /// @param formatArrayLength the number of levels
    public void setFormatArrayLength(int formatArrayLength) {
        this.formatArrayLength = formatArrayLength;
        this.formatArray = new LevelFormat[formatArrayLength];
        for (int i = 0; i < formatArrayLength; i++) {
            this.formatArray[i] = new LevelFormat();
        }
    }

    /// Returns the array of level-specific format settings.
    ///
    /// @return the format array, or `null` if not initialized
    public LevelFormat[] getFormatArray() {
        return formatArray;
    }

    /// Sets the format array directly.
    ///
    /// @param formatArray the level format array
    public void setFormatArray(LevelFormat[] formatArray) {
        this.formatArray = formatArray;
        this.formatArrayLength = formatArray != null ? formatArray.length : 0;
    }

    /// Represents formatting settings for a single TOC level.
    public static class LevelFormat {

        private MarginInfo margin;
        private TextState textState;
        private int lineDash;

        /// Creates a LevelFormat with default settings.
        public LevelFormat() {
            // defaults
        }

        /// Returns the margin for this TOC level.
        ///
        /// @return the margin info, or `null` if not set
        public MarginInfo getMargin() {
            return margin;
        }

        /// Sets the margin for this TOC level.
        ///
        /// @param margin the margin info
        public void setMargin(MarginInfo margin) {
            this.margin = margin;
        }

        /// Returns the text state (font, size, color) for this TOC level.
        ///
        /// @return the text state, or `null` if not set
        public TextState getTextState() {
            return textState;
        }

        /// Sets the text state (font, size, color) for this TOC level.
        ///
        /// @param textState the text state
        public void setTextState(TextState textState) {
            this.textState = textState;
        }

        /// Returns the line dash style for the leader dots.
        ///
        /// @return the line dash value
        public int getLineDash() {
            return lineDash;
        }

        /// Sets the line dash style for the leader dots.
        ///
        /// @param lineDash the line dash value
        public void setLineDash(int lineDash) {
            this.lineDash = lineDash;
        }
    }
}
