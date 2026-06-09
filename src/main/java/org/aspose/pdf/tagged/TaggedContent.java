package org.aspose.pdf.tagged;

import org.aspose.pdf.Document;
import org.aspose.pdf.DocumentInfo;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.logicalstructure.StructTreeRoot;
import org.aspose.pdf.logicalstructure.StructureElement;
import org.aspose.pdf.logicalstructure.StructureTextState;
import org.aspose.pdf.logicalstructure.StructureTypeStandard;
import org.aspose.pdf.logicalstructure.elements.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides access to a document's tagged (structured) content
 * (ISO 32000-1:2008, §14.8).
 *
 * <p>This is the main entry point for reading and modifying the document's
 * logical structure tree. Implements {@link ITaggedContent} to provide
 * factory methods for creating typed structure elements.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   ITaggedContent tc = document.getTaggedContent();
 *   tc.setTitle("My Document");
 *   tc.setLanguage("en-US");
 *   StructureElement root = tc.getRootElement();
 *   ParagraphElement p = tc.createParagraphElement();
 *   p.setText("Hello!");
 *   root.appendChild(p.getStructureElement());
 * </pre>
 */
public class TaggedContent implements ITaggedContent {

    private static final Logger LOG = Logger.getLogger(TaggedContent.class.getName());

    private final Document document;
    private final PdfDictionary catalog;
    private final PDFParser parser;
    private StructTreeRoot structTreeRoot;
    private final List<TOCElement> tocElements = new ArrayList<>();
    private StructureTextState documentTextState;

    /**
     * Creates a TaggedContent accessor.
     *
     * @param document the document
     * @param catalog  the document catalog dictionary
     * @param parser   the PDF parser (may be null for new documents)
     */
    public TaggedContent(Document document, PdfDictionary catalog, PDFParser parser) {
        this.document = document;
        this.catalog = catalog;
        this.parser = parser;
    }

    /**
     * Returns the structure tree root, creating one if it doesn't exist.
     *
     * @return the structure tree root
     */
    public StructTreeRoot getStructTreeRoot() {
        if (structTreeRoot == null) {
            PdfBase strObj = resolve(catalog.get("StructTreeRoot"));
            if (strObj instanceof PdfDictionary) {
                structTreeRoot = new StructTreeRoot((PdfDictionary) strObj, parser);
            } else {
                structTreeRoot = StructTreeRoot.createNew();
                catalog.set(PdfName.of("StructTreeRoot"), structTreeRoot.getPdfDictionary());
                ensureMarkInfo();
            }
        }
        return structTreeRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StructureElement getRootElement() {
        return getStructTreeRoot().getRootElement();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Document-level metadata
    // ═══════════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(String title) {
        if (document != null) {
            try {
                document.getOrCreateInfo().setTitle(title);
            } catch (Exception e) {
                LOG.fine(() -> "Failed to set title: " + e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (document != null) {
            try {
                DocumentInfo di = document.getInfo();
                return di != null ? di.getTitle() : null;
            } catch (Exception e) {
                LOG.fine(() -> "Failed to get title: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLanguage(String lang) {
        catalog.set(PdfName.of("Lang"), new PdfString(lang));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLanguage() {
        PdfBase lang = catalog.get("Lang");
        if (lang instanceof PdfString) return ((PdfString) lang).getString();
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Structure creation (raw)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates a new structure element with the given type.
     * The element is not yet attached to the tree — use
     * {@link StructureElement#appendChild(StructureElement)} to add it.
     *
     * @param type the structure type
     * @return the new element
     */
    public StructureElement createElement(StructureTypeStandard type) {
        PdfDictionary elemDict = new PdfDictionary();
        elemDict.set(PdfName.of("Type"), PdfName.of("StructElem"));
        elemDict.set(PdfName.of("S"), PdfName.of(type.getName()));
        return new StructureElement(elemDict, parser);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Typed element factory methods (ITaggedContent)
    // ═══════════════════════════════════════════════════════════════

    /** {@inheritDoc} */
    @Override
    public TOCElement createTOCElement() {
        TOCElement toc = new TOCElement(createElement(StructureTypeStandard.TOC));
        tocElements.add(toc);
        return toc;
    }

    /** {@inheritDoc} */
    @Override
    public TOCIElement createTOCIElement() {
        return new TOCIElement(createElement(StructureTypeStandard.TOCI));
    }

    /** {@inheritDoc} */
    @Override
    public HeaderElement createHeaderElement() {
        return new HeaderElement(createElement(StructureTypeStandard.H));
    }

    /** {@inheritDoc} */
    @Override
    public HeaderElement createHeaderElement(int level) {
        StructureTypeStandard type;
        switch (level) {
            case 1: type = StructureTypeStandard.H1; break;
            case 2: type = StructureTypeStandard.H2; break;
            case 3: type = StructureTypeStandard.H3; break;
            case 4: type = StructureTypeStandard.H4; break;
            case 5: type = StructureTypeStandard.H5; break;
            case 6: type = StructureTypeStandard.H6; break;
            default: type = StructureTypeStandard.H; break;
        }
        return new HeaderElement(createElement(type), level);
    }

    /** {@inheritDoc} */
    @Override
    public ParagraphElement createParagraphElement() {
        return new ParagraphElement(createElement(StructureTypeStandard.P));
    }

    /** {@inheritDoc} */
    @Override
    public DivElement createDivElement() {
        return new DivElement(createElement(StructureTypeStandard.Div));
    }

    /** {@inheritDoc} */
    @Override
    public SpanElement createSpanElement() {
        return new SpanElement(createElement(StructureTypeStandard.Span));
    }

    /** {@inheritDoc} */
    @Override
    public LinkElement createLinkElement() {
        return new LinkElement(createElement(StructureTypeStandard.Link));
    }

    /** {@inheritDoc} */
    @Override
    public FigureElement createFigureElement() {
        return new FigureElement(createElement(StructureTypeStandard.Figure));
    }

    /** {@inheritDoc} */
    @Override
    public TableElement createTableElement() {
        return new TableElement(createElement(StructureTypeStandard.Table));
    }

    /** {@inheritDoc} */
    @Override
    public TableTRElement createTableTRElement() {
        return new TableTRElement(createElement(StructureTypeStandard.TR));
    }

    /** {@inheritDoc} */
    @Override
    public TableTHElement createTableTHElement() {
        return new TableTHElement(createElement(StructureTypeStandard.TH));
    }

    /** {@inheritDoc} */
    @Override
    public TableTDElement createTableTDElement() {
        return new TableTDElement(createElement(StructureTypeStandard.TD));
    }

    /** {@inheritDoc} */
    @Override
    public ListElement createListElement() {
        return new ListElement(createElement(StructureTypeStandard.L));
    }

    /** {@inheritDoc} */
    @Override
    public ListLIElement createListLIElement() {
        return new ListLIElement(createElement(StructureTypeStandard.LI));
    }

    /** {@inheritDoc} */
    @Override
    public SectElement createSectElement() {
        return new SectElement(createElement(StructureTypeStandard.Sect));
    }

    /** {@inheritDoc} */
    @Override
    public PartElement createPartElement() {
        return new PartElement(createElement(StructureTypeStandard.Part));
    }

    /** {@inheritDoc} */
    @Override
    public NoteElement createNoteElement() {
        return new NoteElement(createElement(StructureTypeStandard.Note));
    }

    /** {@inheritDoc} */
    @Override
    public QuoteElement createQuoteElement() {
        return new QuoteElement(createElement(StructureTypeStandard.Quote));
    }

    /** {@inheritDoc} */
    @Override
    public FormElement createFormElement() {
        return new FormElement(createElement(StructureTypeStandard.Form));
    }

    /** {@inheritDoc} */
    @Override
    public FormulaElement createFormulaElement() {
        return new FormulaElement(createElement(StructureTypeStandard.Formula));
    }

    /** {@inheritDoc} */
    @Override
    public ListLblElement createListLblElement() {
        return new ListLblElement(createElement(StructureTypeStandard.Lbl));
    }

    /** {@inheritDoc} */
    @Override
    public ListLBodyElement createListLBodyElement() {
        return new ListLBodyElement(createElement(StructureTypeStandard.LBody));
    }

    /** {@inheritDoc} */
    @Override
    public TableTHeadElement createTableTHeadElement() {
        return new TableTHeadElement(createElement(StructureTypeStandard.THead));
    }

    /** {@inheritDoc} */
    @Override
    public TableTBodyElement createTableTBodyElement() {
        return new TableTBodyElement(createElement(StructureTypeStandard.TBody));
    }

    /** {@inheritDoc} */
    @Override
    public TableTFootElement createTableTFootElement() {
        return new TableTFootElement(createElement(StructureTypeStandard.TFoot));
    }

    /** {@inheritDoc} */
    @Override
    public StructTreeRoot getStructTreeRootElement() {
        return getStructTreeRoot();
    }

    /** {@inheritDoc} */
    @Override
    public StructureTextState getStructureTextState() {
        if (documentTextState == null) {
            documentTextState = new StructureTextState();
        }
        return documentTextState;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Validation
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validates the tagged structure before saving. Called automatically
     * by {@code Document.save()}.
     *
     * @throws HeaderElementTextConflictException if a header linked to a TOC page
     *         has text that conflicts with the TOC page title
     */
    public void validateBeforeSave() {
        for (TOCElement toc : tocElements) {
            if (toc.getLinkedTocPage() != null && toc.getLinkedTitleHeader() != null) {
                HeaderElement header = toc.getLinkedTitleHeader();
                String headerText = header.getText();
                if (headerText != null && !headerText.isEmpty()) {
                    // Header text was set independently — this conflicts with
                    // the TOC page title linkage
                    String tocTitle = null;
                    if (toc.getLinkedTocPage().getTocInfo() != null
                            && toc.getLinkedTocPage().getTocInfo().getTitle() != null) {
                        tocTitle = toc.getLinkedTocPage().getTocInfo().getTitle().getText();
                    }
                    if (tocTitle != null && !tocTitle.equals(headerText)) {
                        throw new HeaderElementTextConflictException(
                            "Header element text '" + headerText
                            + "' conflicts with TOC page title '" + tocTitle + "'");
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MarkInfo management
    // ═══════════════════════════════════════════════════════════════

    /**
     * Ensures /MarkInfo exists in the catalog with /Marked = true.
     */
    private void ensureMarkInfo() {
        PdfBase miObj = resolve(catalog.get("MarkInfo"));
        PdfDictionary markInfo;
        if (miObj instanceof PdfDictionary) {
            markInfo = (PdfDictionary) miObj;
        } else {
            markInfo = new PdfDictionary();
            catalog.set(PdfName.of("MarkInfo"), markInfo);
        }
        markInfo.setBoolean("Marked", true);
    }

    private static PdfBase resolve(PdfBase obj) {
        if (obj instanceof PdfObjectReference) {
            try { return ((PdfObjectReference) obj).dereference(); }
            catch (IOException e) { return null; }
        }
        return obj;
    }
}
