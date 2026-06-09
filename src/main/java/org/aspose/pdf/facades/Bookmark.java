package org.aspose.pdf.facades;

import org.aspose.pdf.ExplicitDestination;
import org.aspose.pdf.XYZExplicitDestination;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a bookmark (outline item) in a PDF document.
 * This is a simple data class used by {@link PdfBookmarkEditor} to expose
 * bookmark information without requiring direct access to the PDF object layer.
 */
public class Bookmark {

    private static final Logger LOG = Logger.getLogger(Bookmark.class.getName());

    private String title;
    private int pageNumber;
    private String action;
    private int level;
    private ExplicitDestination destination;
    private double pageDisplayZoom;
    private List<Bookmark> childItems;

    /**
     * Creates a new empty bookmark.
     */
    public Bookmark() {
    }

    /**
     * Returns the title of this bookmark.
     *
     * @return the bookmark title, or {@code null} if not set
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this bookmark.
     *
     * @param title the bookmark title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the 1-based page number this bookmark points to.
     *
     * @return the page number, or 0 if not set
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Sets the 1-based page number this bookmark points to.
     *
     * @param pageNumber the page number
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Returns the action string associated with this bookmark.
     *
     * @return the action string, or {@code null} if not set
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action string associated with this bookmark.
     *
     * @param action the action string
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Returns the child bookmarks. The list is lazily initialized on first access.
     *
     * @return the list of child bookmarks, never {@code null}
     */
    public List<Bookmark> getChildItems() {
        if (childItems == null) {
            childItems = new ArrayList<>();
        }
        return childItems;
    }

    /**
     * Sets the child bookmarks.
     *
     * @param childItems the list of child bookmarks
     */
    public void setChildItems(List<Bookmark> childItems) {
        this.childItems = childItems;
    }

    /**
     * Returns the nesting level of this bookmark.
     * Top-level bookmarks have level 1, their children level 2, etc.
     *
     * @return the nesting level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the nesting level of this bookmark.
     *
     * @param level the nesting level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the destination associated with this bookmark.
     *
     * @return the explicit destination, or {@code null} if not set
     */
    public ExplicitDestination getDestination() {
        return destination;
    }

    /**
     * Sets the destination for this bookmark.
     *
     * @param destination the explicit destination
     */
    public void setDestination(ExplicitDestination destination) {
        this.destination = destination;
    }

    /**
     * Sets the page display destination for this bookmark.
     * Convenience alias for {@link #setDestination(ExplicitDestination)}.
     *
     * @param dest the explicit destination
     */
    public void setPageDisplay(ExplicitDestination dest) {
        this.destination = dest;
    }

    /**
     * Returns the page display destination for this bookmark.
     * Convenience alias for {@link #getDestination()}.
     *
     * @return the explicit destination, or {@code null}
     */
    public ExplicitDestination getPageDisplay() {
        return destination;
    }

    /**
     * Returns the zoom factor from the XYZ destination, or 0 if not set.
     *
     * @return the zoom factor
     */
    public double getPageDisplay_Zoom() {
        if (destination instanceof XYZExplicitDestination) {
            return ((XYZExplicitDestination) destination).getZoom();
        }
        return pageDisplayZoom;
    }

    /**
     * Sets the zoom factor for the page display. If the current destination is XYZ,
     * it will be updated with the new zoom. Otherwise, the zoom value is stored
     * for later use.
     *
     * @param zoom the zoom factor (1.0 = 100%)
     */
    public void setPageDisplay_Zoom(double zoom) {
        this.pageDisplayZoom = zoom;
        if (destination instanceof XYZExplicitDestination) {
            XYZExplicitDestination xyz = (XYZExplicitDestination) destination;
            this.destination = new XYZExplicitDestination(
                    xyz.getPage(), xyz.getLeft(), xyz.getTop(), zoom);
        }
    }

    @Override
    public String toString() {
        return "Bookmark{title='" + title + "', page=" + pageNumber + "}";
    }
}
