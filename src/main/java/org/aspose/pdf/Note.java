package org.aspose.pdf;

import org.aspose.pdf.text.TextFragment;

/**
 * Represents a footnote or endnote attached to a {@link TextFragment}
 * (Aspose.PDF API compatibility).
 *
 * <p>A note has two parts:</p>
 * <ul>
 *   <li><b>Marker</b> — the small symbol that appears as superscript at the
 *   attachment point in the body text (e.g. {@code "1"}, {@code "2"},
 *   {@code "*"}). When the {@linkplain #getText() marker text} is empty
 *   (the default), the layout engine auto-numbers notes 1, 2, 3, … in the
 *   order they appear. A non-empty marker text overrides that for one note
 *   only — used to print citation-style markers like
 *   {@code "MacDonald(2002)"}.</li>
 *   <li><b>Body</b> — the paragraphs rendered in the footnote block at the
 *   bottom of the page (footnote) or at the end of the document
 *   (endnote).</li>
 * </ul>
 *
 * <p>The single-argument constructor mirrors {@code Aspose.PDF.Note(string)}:
 * the argument is the <em>body text</em>, not the marker. To override the
 * marker, call {@link #setText(String)} after construction.</p>
 */
public class Note {

    /** Marker-text override; empty means "auto-number on render". */
    private String markerText = "";
    private final Paragraphs paragraphs = new Paragraphs();

    /**
     * Creates an empty note. Use {@link #getParagraphs()} to populate the
     * body and {@link #setText(String)} to override the marker.
     */
    public Note() {
    }

    /**
     * Creates a note whose body is a single {@link TextFragment} with the
     * given text. The marker is auto-generated unless overridden via
     * {@link #setText(String)}.
     *
     * @param bodyText the note body text; {@code null} is treated as empty
     */
    public Note(String bodyText) {
        if (bodyText != null && !bodyText.isEmpty()) {
            paragraphs.add(new TextFragment(bodyText));
        }
    }

    /**
     * Returns the marker-text override. An empty string means the layout
     * engine should auto-number this note.
     *
     * @return the marker text (never {@code null})
     */
    public String getText() {
        return markerText;
    }

    /**
     * Overrides the marker text for this single note. Pass an empty string
     * (or {@code null}) to restore auto-numbering.
     *
     * @param text the marker text
     */
    public void setText(String text) {
        this.markerText = text != null ? text : "";
    }

    /**
     * Returns the paragraphs that make up the body of this note. Mutate
     * directly to add or remove body content.
     *
     * @return the body paragraphs (never {@code null})
     */
    public Paragraphs getParagraphs() {
        return paragraphs;
    }
}
