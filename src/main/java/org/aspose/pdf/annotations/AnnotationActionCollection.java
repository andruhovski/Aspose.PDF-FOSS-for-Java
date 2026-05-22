package org.aspose.pdf.annotations;

import org.aspose.pdf.PdfAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of actions associated with an annotation
 * (ISO 32000-1:2008, Section 12.6.3).
 * <p>
 * Provides access to additional actions (/AA entry) on annotations,
 * such as mouse enter/exit, page open/close, etc.
 * </p>
 */
public class AnnotationActionCollection {

    private final List<PdfAction> actions = new ArrayList<>();

    /**
     * Creates an empty annotation action collection.
     */
    public AnnotationActionCollection() {
    }

    /**
     * Adds an action to this collection.
     *
     * @param action the action to add
     */
    public void add(PdfAction action) {
        if (action != null) {
            actions.add(action);
        }
    }

    /**
     * Returns the number of actions in this collection.
     *
     * @return the action count
     */
    public int size() {
        return actions.size();
    }

    /**
     * Returns the action at the specified index.
     *
     * @param index the zero-based index
     * @return the action at the given index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public PdfAction get(int index) {
        return actions.get(index);
    }

    /** Action triggered when cursor exits the annotation area (/AA /X). */
    private PdfAction onExit;

    /**
     * Returns the action triggered when the cursor exits the annotation area.
     *
     * @return the on-exit action, or null
     */
    public PdfAction getOnExit() {
        return onExit;
    }

    /**
     * Sets the action triggered when the cursor exits the annotation area.
     *
     * @param action the on-exit action
     */
    public void setOnExit(PdfAction action) {
        this.onExit = action;
    }

    /** Action triggered when cursor enters the annotation area (/AA /E). */
    private PdfAction onEnter;

    /** Action triggered when the mouse button is pressed on the annotation (/AA /D). */
    private PdfAction onPressMouseBtn;

    /**
     * Returns the action triggered when the cursor enters the annotation area.
     *
     * @return the on-enter action, or null
     */
    public PdfAction getOnEnter() {
        return onEnter;
    }

    /**
     * Sets the action triggered when the cursor enters the annotation area.
     *
     * @param action the on-enter action
     */
    public void setOnEnter(PdfAction action) {
        this.onEnter = action;
    }

    /**
     * Returns the action triggered when the mouse button is pressed.
     *
     * @return the on-press action, or null
     */
    public PdfAction getOnPressMouseBtn() {
        return onPressMouseBtn;
    }

    /**
     * Sets the action triggered when the mouse button is pressed.
     *
     * @param action the on-press action
     */
    public void setOnPressMouseBtn(PdfAction action) {
        this.onPressMouseBtn = action;
    }
}
