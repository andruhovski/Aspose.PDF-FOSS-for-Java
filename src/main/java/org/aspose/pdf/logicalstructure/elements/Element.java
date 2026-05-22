package org.aspose.pdf.logicalstructure.elements;

import org.aspose.pdf.logicalstructure.StructureElement;
import org.aspose.pdf.logicalstructure.StructureTextState;
import org.aspose.pdf.tagged.PositionSettings;

import java.util.logging.Logger;

/**
 * Abstract base class for typed structure elements in the logical structure tree
 * (ISO 32000-1:2008, §14.7.2).
 *
 * <p>Each concrete subclass wraps a {@link StructureElement} and provides
 * convenience methods specific to that element type.</p>
 */
public abstract class Element {

    private static final Logger LOG = Logger.getLogger(Element.class.getName());

    /** The underlying structure element. */
    protected final StructureElement structureElement;

    /** Lazily-created text state for this element. */
    private StructureTextState structureTextState;

    /** Position settings for this element. */
    private PositionSettings positionSettings;

    /**
     * Creates an element wrapping the given structure element.
     *
     * @param se the structure element to wrap
     */
    protected Element(StructureElement se) {
        if (se == null) {
            throw new IllegalArgumentException("StructureElement must not be null");
        }
        this.structureElement = se;
    }

    /**
     * Sets the actual text (/ActualText) for this element.
     *
     * @param text the text content
     */
    public void setText(String text) {
        structureElement.setActualText(text);
    }

    /**
     * Returns the actual text (/ActualText) of this element.
     *
     * @return the text, or {@code null}
     */
    public String getText() {
        return structureElement.getActualText();
    }

    /**
     * Sets the language (/Lang) for this element.
     *
     * @param lang the language tag (e.g., "en-US")
     */
    public void setLanguage(String lang) {
        structureElement.setLanguage(lang);
    }

    /**
     * Returns the language (/Lang) of this element.
     *
     * @return the language tag, or {@code null}
     */
    public String getLanguage() {
        return structureElement.getLanguage();
    }

    /**
     * Appends a child element.
     *
     * @param child the child element to append
     */
    public void appendChild(Element child) {
        structureElement.appendChild(child.getStructureElement());
    }

    /**
     * Returns the underlying structure element.
     *
     * @return the structure element
     */
    public StructureElement getStructureElement() {
        return structureElement;
    }

    /**
     * Sets the title (/T) for this element.
     *
     * @param title the title text
     */
    public void setTitle(String title) {
        structureElement.setTitle(title);
    }

    /**
     * Returns the title (/T) of this element.
     *
     * @return the title, or {@code null}
     */
    public String getTitle() {
        return structureElement.getTitle();
    }

    /**
     * Sets the alternate description (/Alt) for this element.
     *
     * @param alt the alternate description
     */
    public void setAlternateDescription(String alt) {
        structureElement.setAlternateDescription(alt);
    }

    /**
     * Returns the alternate description (/Alt) of this element.
     *
     * @return the alternate description, or {@code null}
     */
    public String getAlternateDescription() {
        return structureElement.getAlternateDescription();
    }

    /**
     * Returns the structure text state for this element.
     * Creates one on first access.
     *
     * @return the structure text state
     */
    public StructureTextState getStructureTextState() {
        if (structureTextState == null) {
            structureTextState = new StructureTextState();
        }
        return structureTextState;
    }

    /**
     * Sets the alternative text (/Alt) for this element.
     * Alias for {@link #setAlternateDescription(String)}.
     *
     * @param text the alternative text
     */
    public void setAlternativeText(String text) {
        structureElement.setAlternateDescription(text);
    }

    /**
     * Returns the alternative text (/Alt) for this element.
     * Alias for {@link #getAlternateDescription()}.
     *
     * @return the alternative text, or {@code null}
     */
    public String getAlternativeText() {
        return structureElement.getAlternateDescription();
    }

    /**
     * Adjusts the position of this element using the given settings.
     *
     * @param settings the position settings
     */
    public void adjustPosition(PositionSettings settings) {
        this.positionSettings = settings;
    }

    /**
     * Returns the position settings for this element.
     *
     * @return the position settings, or {@code null}
     */
    public PositionSettings getPositionSettings() {
        return positionSettings;
    }
}
