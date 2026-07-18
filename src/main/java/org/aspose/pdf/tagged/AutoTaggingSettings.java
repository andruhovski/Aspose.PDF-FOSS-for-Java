package org.aspose.pdf.tagged;

import java.util.logging.Logger;

/// Settings for automatic tagging of PDF document structure.
///
/// When applied during PDF/A or PDF/UA conversion, these settings control
/// which document elements are automatically tagged with appropriate
/// structure elements (headings, tables, images, links, etc.).
///
public class AutoTaggingSettings {

    private static final Logger LOG = Logger.getLogger(AutoTaggingSettings.class.getName());

    private boolean tagDocumentStructure = true;
    private boolean tagHeadings = true;
    private boolean tagTables = true;
    private boolean tagImages = true;
    private boolean tagLinks = true;

    /// Creates AutoTaggingSettings with all tagging options enabled by default.
    public AutoTaggingSettings() {
        LOG.fine("AutoTaggingSettings created with defaults");
    }

    /// Returns a new AutoTaggingSettings instance with all default values
    /// (all tagging options enabled).
    ///
    /// @return a new default AutoTaggingSettings instance
    public static AutoTaggingSettings getDefault() {
        return new AutoTaggingSettings();
    }

    /// Returns whether document structure tagging is enabled.
    ///
    /// @return `true` if document structure elements will be tagged
    public boolean isTagDocumentStructure() {
        return tagDocumentStructure;
    }

    /// Sets whether document structure tagging is enabled.
    ///
    /// @param tagDocumentStructure`true` to enable document structure tagging
    public void setTagDocumentStructure(boolean tagDocumentStructure) {
        this.tagDocumentStructure = tagDocumentStructure;
    }

    /// Returns whether heading tagging is enabled.
    ///
    /// @return `true` if headings will be tagged
    public boolean isTagHeadings() {
        return tagHeadings;
    }

    /// Sets whether heading tagging is enabled.
    ///
    /// @param tagHeadings`true` to enable heading tagging
    public void setTagHeadings(boolean tagHeadings) {
        this.tagHeadings = tagHeadings;
    }

    /// Returns whether table tagging is enabled.
    ///
    /// @return `true` if tables will be tagged
    public boolean isTagTables() {
        return tagTables;
    }

    /// Sets whether table tagging is enabled.
    ///
    /// @param tagTables`true` to enable table tagging
    public void setTagTables(boolean tagTables) {
        this.tagTables = tagTables;
    }

    /// Returns whether image tagging is enabled.
    ///
    /// @return `true` if images will be tagged
    public boolean isTagImages() {
        return tagImages;
    }

    /// Sets whether image tagging is enabled.
    ///
    /// @param tagImages`true` to enable image tagging
    public void setTagImages(boolean tagImages) {
        this.tagImages = tagImages;
    }

    /// Returns whether link tagging is enabled.
    ///
    /// @return `true` if links will be tagged
    public boolean isTagLinks() {
        return tagLinks;
    }

    /// Sets whether link tagging is enabled.
    ///
    /// @param tagLinks`true` to enable link tagging
    public void setTagLinks(boolean tagLinks) {
        this.tagLinks = tagLinks;
    }
}
