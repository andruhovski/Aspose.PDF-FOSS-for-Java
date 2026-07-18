package org.aspose.pdf.text;

import org.aspose.pdf.Rectangle;

/// Options for text search operations.
///
/// Supports regular expression search and area-based filtering.
/// Used by [TextFragmentAbsorber] to control search behavior.
///
public class TextSearchOptions {

    private boolean isRegularExpressionUsed;
    private boolean caseSensitive = true;
    private boolean searchForTextRelatedGraphics;
    private boolean limitToPageBounds;
    private boolean searchInAnnotations;
    private boolean ignoreResourceFontErrors;
    private boolean useFontEngineEncoding;
    private Rectangle rectangle;
    private Rectangle[] excludeRectangles;

    /// Creates TextSearchOptions with default settings.
    public TextSearchOptions() {
        this(false);
    }

    /// Creates TextSearchOptions.
    ///
    /// @param isRegex true to enable regular expression matching
    public TextSearchOptions(boolean isRegex) {
        this.isRegularExpressionUsed = isRegex;
    }

    /// Creates TextSearchOptions that restrict search to a rectangle area.
    ///
    /// @param rectangle the search area
    public TextSearchOptions(org.aspose.pdf.Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    /// Returns whether regular expression matching is enabled.
    ///
    /// @return true if regex mode
    public boolean isRegularExpressionUsed() {
        return isRegularExpressionUsed;
    }

    /// Sets whether regular expression matching is enabled.
    ///
    /// @param value true to enable regex
    public void setRegularExpressionUsed(boolean value) {
        this.isRegularExpressionUsed = value;
    }

    /// Returns whether matching is case-sensitive.
    ///
    /// @return `true` if matching is case-sensitive
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /// Sets whether matching is case-sensitive.
    ///
    /// @param caseSensitive`true` for case-sensitive matching
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /// Returns the search area rectangle (may be null for whole page).
    ///
    /// @return the rectangle filter, or null
    public Rectangle getRectangle() {
        return rectangle;
    }

    /// Sets the search area rectangle. Only text within this area will be matched.
    ///
    /// @param rect the rectangle filter, or null for whole page
    public void setRectangle(Rectangle rect) {
        this.rectangle = rect;
    }

    /// Returns whether text-related graphics should be considered during search.
    ///
    /// @return `true` if text-related graphics are considered
    public boolean isSearchForTextRelatedGraphics() {
        return searchForTextRelatedGraphics;
    }

    /// Sets whether text-related graphics should be considered during search.
    ///
    /// @param value`true` to include text-related graphics
    public void setSearchForTextRelatedGraphics(boolean value) {
        this.searchForTextRelatedGraphics = value;
    }

    /// Returns whether found text should be limited to page bounds.
    ///
    /// @return `true` if page bounds are enforced
    public boolean isLimitToPageBounds() {
        return limitToPageBounds;
    }

    /// Sets whether found text should be limited to page bounds.
    ///
    /// @param value`true` to enforce page bounds
    public void setLimitToPageBounds(boolean value) {
        this.limitToPageBounds = value;
    }

    /// Returns whether annotation text should be searched.
    ///
    /// @return `true` if annotation text is included
    public boolean isSearchInAnnotations() {
        return searchInAnnotations;
    }

    /// Sets whether annotation text should be searched.
    ///
    /// @param value`true` to include annotation text
    public void setSearchInAnnotations(boolean value) {
        this.searchInAnnotations = value;
    }

    /// Returns whether resource font errors should be ignored during search.
    ///
    /// @return `true` if such errors should be ignored
    public boolean isIgnoreResourceFontErrors() {
        return ignoreResourceFontErrors;
    }

    /// Sets whether resource font errors should be ignored during search.
    ///
    /// @param value`true` to ignore resource font errors
    public void setIgnoreResourceFontErrors(boolean value) {
        this.ignoreResourceFontErrors = value;
    }

    /// Returns whether font-engine encoding should be used during search.
    ///
    /// @return `true` if font-engine encoding should be used
    public boolean isUseFontEngineEncoding() {
        return useFontEngineEncoding;
    }

    /// Sets whether font-engine encoding should be used during search.
    ///
    /// @param value`true` to use font-engine encoding
    public void setUseFontEngineEncoding(boolean value) {
        this.useFontEngineEncoding = value;
    }

    /// Returns rectangles that should be excluded from search.
    ///
    /// @return excluded rectangles, or `null`
    public Rectangle[] getExcludeRectangles() {
        return excludeRectangles;
    }

    /// Sets rectangles that should be excluded from search.
    ///
    /// @param excludeRectangles rectangles to exclude, or `null`
    public void setExcludeRectangles(Rectangle[] excludeRectangles) {
        this.excludeRectangles = excludeRectangles;
    }
}
