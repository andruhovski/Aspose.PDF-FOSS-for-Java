package org.aspose.pdf.text;

import org.aspose.pdf.Color;
import org.aspose.pdf.HorizontalAlignment;
import org.aspose.pdf.MarginInfo;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.VerticalAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a multi-line text paragraph that can be appended to a page via {@link TextBuilder}.
 * <p>
 * A paragraph consists of one or more lines (each a {@link TextFragment}), positioned at a
 * specific location on the page. Lines are laid out vertically using the configured line spacing.
 * </p>
 */
public class TextParagraph {

    private static final Logger LOG = Logger.getLogger(TextParagraph.class.getName());

    private final List<TextFragment> lines = new ArrayList<>();
    private final List<TextFragment> remainingLines = new ArrayList<>();
    private Position position;
    private Rectangle rectangle;
    private double lineSpacing = 1.2;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.None;
    private VerticalAlignment verticalAlignment = VerticalAlignment.Top;
    private Color backgroundColor;
    private TextFormattingOptions formattingOptions;
    private MarginInfo margin;
    private boolean limitWithBounds;
    private String hyphenSymbol;
    private int editDepth;
    private boolean dirty;
    private int updatePositioningCalls;

    /**
     * Creates an empty TextParagraph.
     */
    public TextParagraph() {
    }

    /**
     * Appends a line (text fragment) to this paragraph.
     *
     * @param line the text fragment to add as a new line
     * @throws IllegalArgumentException if line is null
     */
    public void appendLine(TextFragment line) {
        if (line == null) {
            throw new IllegalArgumentException("Line must not be null");
        }
        lines.add(line);
        markDirty();
    }

    /**
     * Appends a plain string as a new line. The line is wrapped in a default
     * {@link TextFragment} with no explicit text state.
     *
     * @param text the line text (null is treated as empty)
     */
    public void appendLine(String text) {
        lines.add(new TextFragment(text != null ? text : ""));
        markDirty();
    }

    /**
     * Appends a plain string as a new line, applying the given text state.
     *
     * @param text  the line text (null is treated as empty)
     * @param state the text state to apply (font, size, color, ...). If null,
     *              the fragment receives a default state.
     */
    public void appendLine(String text, TextState state) {
        TextFragment fragment = new TextFragment(text != null ? text : "");
        if (state != null) {
            fragment.setTextState(state);
        }
        lines.add(fragment);
        markDirty();
    }

    /**
     * Returns the lines as an unmodifiable {@link List} (engine-internal view).
     * Public clients should use {@link #getLines()} which returns the
     * Aspose-compatible 1-based {@link TextFragmentCollection}.
     *
     * @return an unmodifiable list of text fragment lines (0-based)
     */
    public List<TextFragment> getLinesList() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Returns the position of this paragraph on the page.
     *
     * @return the position, or null if not set
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the position of this paragraph on the page.
     *
     * @param position the position
     */
    public void setPosition(Position position) {
        this.position = position;
        markDirty();
    }

    /**
     * Returns the bounding rectangle of this paragraph.
     *
     * @return the rectangle, or null if not set
     */
    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * Sets the bounding rectangle of this paragraph.
     *
     * @param rectangle the rectangle
     */
    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
        markDirty();
    }

    /**
     * Returns the line spacing multiplier. Default is 1.2 (120% of font size).
     *
     * @return the line spacing multiplier
     */
    public double getLineSpacing() {
        return lineSpacing;
    }

    /**
     * Sets the line spacing multiplier. A value of 1.0 means single spacing (equal to font size),
     * 1.5 means 150% of font size, etc.
     *
     * @param lineSpacing the line spacing multiplier (must be positive)
     * @throws IllegalArgumentException if lineSpacing is not positive
     */
    public void setLineSpacing(double lineSpacing) {
        if (lineSpacing <= 0) {
            throw new IllegalArgumentException("Line spacing must be positive, got: " + lineSpacing);
        }
        this.lineSpacing = lineSpacing;
        markDirty();
    }

    /**
     * Returns the horizontal alignment of this paragraph.
     *
     * @return the horizontal alignment
     */
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of this paragraph.
     *
     * @param horizontalAlignment the horizontal alignment
     */
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment != null
                ? horizontalAlignment : HorizontalAlignment.None;
        markDirty();
    }

    /**
     * Returns the vertical alignment of this paragraph within its bounding rectangle.
     *
     * @return the vertical alignment
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the vertical alignment of this paragraph within its bounding rectangle.
     *
     * @param verticalAlignment the vertical alignment
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment != null
                ? verticalAlignment : VerticalAlignment.Top;
        markDirty();
    }

    /**
     * Returns the background color filled behind the paragraph's bounding rectangle.
     *
     * @return the background color, or null if transparent
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color filled behind the paragraph's bounding rectangle.
     *
     * @param backgroundColor the background color (null = transparent)
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markDirty();
    }

    /**
     * Returns the formatting options (wrap mode, indents, line spacing).
     * Lazily creates a default instance on first access, mirroring the
     * Aspose.PDF C# API where the property is never null.
     *
     * @return the formatting options (never null)
     */
    public TextFormattingOptions getFormattingOptions() {
        if (formattingOptions == null) {
            formattingOptions = new TextFormattingOptions();
        }
        return formattingOptions;
    }

    /**
     * Sets the formatting options.
     *
     * @param formattingOptions the formatting options
     */
    public void setFormattingOptions(TextFormattingOptions formattingOptions) {
        this.formattingOptions = formattingOptions;
        markDirty();
    }

    /**
     * Returns the paragraph's outer margin (whitespace around the bounding rectangle).
     * Lazily creates a default zero-margin instance on first access.
     *
     * @return the margin info (never null)
     */
    public MarginInfo getMargin() {
        if (margin == null) {
            margin = new MarginInfo();
        }
        return margin;
    }

    /**
     * Sets the paragraph's outer margin.
     *
     * @param margin the margin info
     */
    public void setMargin(MarginInfo margin) {
        this.margin = margin;
        markDirty();
    }

    /**
     * Returns true if the paragraph has any non-empty lines.
     *
     * @return true if at least one line contains text
     */
    public boolean hasContent() {
        for (TextFragment line : lines) {
            if (line.getText() != null && !line.getText().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the lines accumulated in this paragraph as a 1-based collection
     * (Aspose.PDF API convention: {@code paragraph.Lines[1]} is the first line).
     * <p>
     * The returned collection is a view: changes to the returned list
     * (add/remove) do not propagate back to the paragraph.
     * </p>
     *
     * @return a new {@link TextFragmentCollection} containing the paragraph's lines
     */
    public TextFragmentCollection getLines() {
        TextFragmentCollection col = new TextFragmentCollection();
        for (TextFragment line : lines) {
            col.add(line);
        }
        return col;
    }

    /**
     * Returns lines that did not fit into the paragraph's bounding rectangle
     * after the most recent {@code AppendParagraph} call.
     * <p>
     * Our current layout does not perform real text wrapping or overflow
     * detection, so this collection is empty unless the engine explicitly
     * pushed lines into it. The accessor exists for API parity with Aspose
     * so client code that does {@code if (paragraph.getRemainingLines().getCount() &gt; 0)}
     * compiles and short-circuits.
     * </p>
     *
     * @return a {@link TextFragmentCollection} of overflow lines (possibly empty)
     */
    public TextFragmentCollection getRemainingLines() {
        TextFragmentCollection col = new TextFragmentCollection();
        for (TextFragment line : remainingLines) {
            col.add(line);
        }
        return col;
    }

    /**
     * Returns whether text rendering is constrained to the paragraph's bounding
     * rectangle. When true, lines that overflow are dropped from the visible
     * output and added to {@link #getRemainingLines()}.
     *
     * @return the bounds-limit flag
     */
    public boolean getLimitWithBounds() {
        return limitWithBounds;
    }

    /**
     * Returns whether text rendering is constrained to the paragraph's bounding
     * rectangle (alias for {@link #getLimitWithBounds()}).
     *
     * @return the bounds-limit flag
     */
    public boolean isLimitWithBounds() {
        return limitWithBounds;
    }

    /**
     * Sets whether text rendering is constrained to the paragraph's bounding rectangle.
     *
     * @param limitWithBounds true to clip overflow into {@link #getRemainingLines()}
     */
    public void setLimitWithBounds(boolean limitWithBounds) {
        this.limitWithBounds = limitWithBounds;
        markDirty();
    }

    /**
     * Returns the discretionary-hyphenation symbol used by the wrap engine.
     * Default is {@code null} (use the engine default of {@code "-"}).
     *
     * @return the hyphen symbol, or null
     */
    public String getHyphenSymbol() {
        return hyphenSymbol;
    }

    /**
     * Sets the discretionary-hyphenation symbol. An empty string suppresses the hyphen.
     *
     * @param hyphenSymbol the hyphen symbol
     */
    public void setHyphenSymbol(String hyphenSymbol) {
        this.hyphenSymbol = hyphenSymbol;
        markDirty();
    }

    /**
     * Returns the rectangle currently occupied by the paragraph's text.
     * <p>
     * Without a real layout pass we approximate this with the configured
     * bounding {@link #getRectangle() rectangle}. Returns {@code null} if no
     * bounding rectangle has been set.
     * </p>
     *
     * @return the text rectangle, or null
     */
    public Rectangle getTextRectangle() {
        return rectangle;
    }

    /**
     * Begins a deferred-layout edit block.
     * <p>
     * Successive property mutations on the paragraph will not trigger a
     * positioning update until a matching {@link #endEdit()} runs. Nesting
     * is supported via reference counting; only the outermost {@code endEdit}
     * applies the deferred update.
     * </p>
     */
    public void beginEdit() {
        editDepth++;
    }

    /**
     * Ends the most recent {@link #beginEdit()} block.
     * <p>
     * If this is the outermost block and any deferred mutations occurred,
     * the positioning is recomputed (incrementing
     * {@link #getUpdatePositioningCalls()}). Calling {@code endEdit()} without
     * a matching {@code beginEdit()} is a no-op.
     * </p>
     */
    public void endEdit() {
        if (editDepth == 0) {
            return;
        }
        editDepth--;
        if (editDepth == 0 && dirty) {
            applyUpdatePositioning();
        }
    }

    /**
     * Returns the number of times the paragraph has applied a positioning
     * update. Used by performance-sensitive callers to verify that
     * {@link #beginEdit()}/{@link #endEdit()} actually defers rebuilds.
     *
     * @return the call counter
     */
    public int getUpdatePositioningCalls() {
        return updatePositioningCalls;
    }

    /**
     * Marks the paragraph as needing a positioning update. Called internally
     * after every mutation. Outside an edit block the update is applied
     * immediately; inside a block it is deferred until {@link #endEdit()}.
     */
    void markDirty() {
        if (editDepth > 0) {
            dirty = true;
        } else {
            applyUpdatePositioning();
        }
    }

    /**
     * Applies a (possibly deferred) positioning update.
     * Currently this only bumps {@link #updatePositioningCalls}; real layout
     * computation will be added when the paragraph layout engine lands.
     */
    private void applyUpdatePositioning() {
        dirty = false;
        updatePositioningCalls++;
    }
}
