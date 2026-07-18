package org.aspose.pdf;

import org.aspose.pdf.text.TextState;

import java.util.Objects;
import java.util.logging.Logger;

/// Represents an HTML content fragment that can be added to a PDF page's paragraph collection.
///
/// The HTML text is parsed and rendered into PDF content during document generation.
/// An optional [TextState] can be set to control the default text styling.
///
public class HtmlFragment extends BaseParagraph {

    private static final Logger LOG = Logger.getLogger(HtmlFragment.class.getName());

    private String htmlText;
    private TextState textState;

    /// Creates a new HtmlFragment with the given HTML content.
    ///
    /// @param htmlText the HTML text content; must not be `null`
    /// @throws NullPointerException if `htmlText` is `null`
    public HtmlFragment(String htmlText) {
        Objects.requireNonNull(htmlText, "htmlText must not be null");
        this.htmlText = htmlText;
    }

    /// Returns the HTML text content.
    ///
    /// @return the HTML text
    public String getHtmlText() {
        return htmlText;
    }

    /// Sets the HTML text content.
    ///
    /// @param htmlText the HTML text; must not be `null`
    /// @throws NullPointerException if `htmlText` is `null`
    public void setHtmlText(String htmlText) {
        Objects.requireNonNull(htmlText, "htmlText must not be null");
        this.htmlText = htmlText;
    }

    /// Returns the default text state used for rendering the HTML content.
    ///
    /// @return the text state, or `null` if not set
    public TextState getTextState() {
        return textState;
    }

    /// Sets the default text state used for rendering the HTML content.
    ///
    /// @param textState the text state
    public void setTextState(TextState textState) {
        this.textState = textState;
    }
}
