package org.aspose.pdf;

import org.aspose.pdf.text.TextFragment;

/// Represents a footnote or endnote attached to a [TextFragment]
/// (Aspose.PDF API compatibility).
///
/// A note has two parts:
///
///   - **Marker** — the small symbol that appears as superscript at the
///     attachment point in the body text (e.g. `"1"`, `"2"`,
///     `"*"`). When the [marker text][#getText()] is empty
///     (the default), the layout engine auto-numbers notes 1, 2, 3, … in the
///     order they appear. A non-empty marker text overrides that for one note
///     only — used to print citation-style markers like
///     `"MacDonald(2002)"`.
///   - **Body** — the paragraphs rendered in the footnote block at the
///     bottom of the page (footnote) or at the end of the document
///     (endnote).
///
/// The single-argument constructor mirrors `Aspose.PDF.Note(string)`:
/// the argument is the _body text_, not the marker. To override the
/// marker, call [#setText(String)] after construction.
public class Note {

    /// Marker-text override; empty means "auto-number on render".
    private String markerText = "";
    private final Paragraphs paragraphs = new Paragraphs();

    /// Creates an empty note. Use [#getParagraphs()] to populate the
    /// body and [#setText(String)] to override the marker.
    public Note() {
    }

    /// Creates a note whose body is a single [TextFragment] with the
    /// given text. The marker is auto-generated unless overridden via
    /// [#setText(String)].
    ///
    /// @param bodyText the note body text; `null` is treated as empty
    public Note(String bodyText) {
        if (bodyText != null && !bodyText.isEmpty()) {
            paragraphs.add(new TextFragment(bodyText));
        }
    }

    /// Returns the marker-text override. An empty string means the layout
    /// engine should auto-number this note.
    ///
    /// @return the marker text (never `null`)
    public String getText() {
        return markerText;
    }

    /// Overrides the marker text for this single note. Pass an empty string
    /// (or `null`) to restore auto-numbering.
    ///
    /// @param text the marker text
    public void setText(String text) {
        this.markerText = text != null ? text : "";
    }

    /// Returns the paragraphs that make up the body of this note. Mutate
    /// directly to add or remove body content.
    ///
    /// @return the body paragraphs (never `null`)
    public Paragraphs getParagraphs() {
        return paragraphs;
    }
}
