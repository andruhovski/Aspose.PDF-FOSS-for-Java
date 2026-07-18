package org.aspose.pdf;

/// String constants for commonly used XMP metadata property keys (ISO 16684-1).
///
/// Use these constants as keys with [XmpMetadata#get(String)] and
/// [XmpMetadata#set(String, XmpValue)].
///
public final class DefaultMetadataProperties {

    private DefaultMetadataProperties() {}

    // ── XMP Basic (xmp:) ──

    /// Date the resource was created.
    public static final String CreateDate = "xmp:CreateDate";
    /// Date the resource metadata was last changed.
    public static final String MetadataDate = "xmp:MetadataDate";
    /// Date the resource was last modified.
    public static final String ModifyDate = "xmp:ModifyDate";
    /// The application that created the resource.
    public static final String CreatorTool = "xmp:CreatorTool";
    /// A short informal name for the resource.
    public static final String Label = "xmp:Label";
    /// A user-assigned rating.
    public static final String Rating = "xmp:Rating";
    /// A short informal name.
    public static final String Nickname = "xmp:Nickname";
    /// The base URL for relative URLs in the resource.
    public static final String BaseURL = "xmp:BaseURL";

    // ── Dublin Core (dc:) ──

    /// The document title (Language Alternative).
    public static final String Title = "dc:title";
    /// The document author(s) (ordered array).
    public static final String Creator = "dc:creator";
    /// The document description/subject (Language Alternative).
    public static final String Description = "dc:description";
    /// Keywords/tags (unordered array).
    public static final String Subject = "dc:subject";
    /// The MIME type of the resource.
    public static final String Format = "dc:format";
    /// Copyright information (Language Alternative).
    public static final String Rights = "dc:rights";
    /// Date(s) associated with the resource (ordered array).
    public static final String Date = "dc:date";
    /// Unique identifier for the resource.
    public static final String Identifier = "dc:identifier";
    /// Language(s) of the resource (unordered array).
    public static final String Language = "dc:language";
    /// Publisher(s) (unordered array).
    public static final String Publisher = "dc:publisher";
    /// Related resource(s) (unordered array).
    public static final String Relation = "dc:relation";
    /// The original resource from which this was derived.
    public static final String Source = "dc:source";
    /// Nature or genre (unordered array).
    public static final String Type = "dc:type";
    /// Spatial/temporal coverage.
    public static final String Coverage = "dc:coverage";
    /// Contributor(s) (unordered array).
    public static final String Contributor = "dc:contributor";

    // ── PDF (pdf:) ──

    /// PDF keywords string.
    public static final String Keywords = "pdf:Keywords";
    /// PDF version (e.g. "1.7").
    public static final String PDFVersion = "pdf:PDFVersion";
    /// The application that produced the PDF.
    public static final String Producer = "pdf:Producer";
    /// Whether the document has been trapped.
    public static final String Trapped = "pdf:Trapped";

    // ── XMP Media Management (xmpMM:) ──

    /// Unique document identifier.
    public static final String DocumentID = "xmpMM:DocumentID";
    /// Unique instance identifier.
    public static final String InstanceID = "xmpMM:InstanceID";
    /// Original document identifier.
    public static final String OriginalDocumentID = "xmpMM:OriginalDocumentID";
    /// Rendition class.
    public static final String RenditionClass = "xmpMM:RenditionClass";

    // ── XMP Rights Management (xmpRights:) ──

    /// Whether the resource is rights-managed.
    public static final String Marked = "xmpRights:Marked";
    /// Rights owner(s) (unordered array).
    public static final String Owner = "xmpRights:Owner";
    /// Usage terms (Language Alternative).
    public static final String UsageTerms = "xmpRights:UsageTerms";
    /// Web statement of rights.
    public static final String WebStatement = "xmpRights:WebStatement";
    /// Rights certificate URL.
    public static final String Certificate = "xmpRights:Certificate";

    // ── PDF/A Identification (pdfaid:) ──

    /// PDF/A part number (e.g. "1", "2", "3").
    public static final String PdfAidPart = "pdfaid:part";
    /// PDF/A conformance level (e.g. "A", "B", "U").
    public static final String PdfAidConformance = "pdfaid:conformance";
    /// PDF/A amendment identifier.
    public static final String PdfAidAmd = "pdfaid:amd";
    /// PDF/A corrigendum identifier.
    public static final String PdfAidCorr = "pdfaid:corr";
}
