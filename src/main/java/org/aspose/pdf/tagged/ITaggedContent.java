package org.aspose.pdf.tagged;

import org.aspose.pdf.logicalstructure.StructTreeRoot;
import org.aspose.pdf.logicalstructure.StructureElement;
import org.aspose.pdf.logicalstructure.StructureTextState;
import org.aspose.pdf.logicalstructure.elements.*;

/// Interface for accessing and modifying tagged (structured) content
/// in a PDF document (ISO 32000-1:2008, §14.8).
///
/// Provides factory methods for creating typed structure elements
/// and document-level metadata accessors for title and language.
///
/// Usage example:
///
/// <pre>
///   ITaggedContent content = document.getTaggedContent();
///   content.setTitle("My Document");
///   content.setLanguage("en-US");
///   StructureElement root = content.getRootElement();
///   ParagraphElement p = content.createParagraphElement();
///   p.setText("Hello, world!");
///   root.appendChild(p.getStructureElement());
/// </pre>
public interface ITaggedContent {

    /// Returns the root structure element (typically /S = /Document).
    ///
    /// @return the root element, or `null` if no structure tree exists
    StructureElement getRootElement();

    /// Sets the document title.
    ///
    /// @param title the document title
    void setTitle(String title);

    /// Returns the document title.
    ///
    /// @return the title, or `null`
    String getTitle();

    /// Sets the document language (/Lang in catalog).
    ///
    /// @param language the language tag (e.g., "en-US")
    void setLanguage(String language);

    /// Returns the document language.
    ///
    /// @return the language tag, or `null`
    String getLanguage();

    // ═══════════════════════════════════════════════════════════════
    //  Factory methods for creating typed structure elements
    // ═══════════════════════════════════════════════════════════════

    /// Creates a new TOC (Table of Contents) structure element.
    ///
    /// @return a new TOCElement
    TOCElement createTOCElement();

    /// Creates a new TOCI (Table of Contents Item) structure element.
    ///
    /// @return a new TOCIElement
    TOCIElement createTOCIElement();

    /// Creates a new generic header (H) structure element.
    ///
    /// @return a new HeaderElement
    HeaderElement createHeaderElement();

    /// Creates a new header structure element at the specified level (H1–H6).
    ///
    /// @param level the heading level (1–6)
    /// @return a new HeaderElement with the specified level
    HeaderElement createHeaderElement(int level);

    /// Creates a new paragraph (P) structure element.
    ///
    /// @return a new ParagraphElement
    ParagraphElement createParagraphElement();

    /// Creates a new division (Div) grouping element.
    ///
    /// @return a new DivElement
    DivElement createDivElement();

    /// Creates a new span (Span) inline element.
    ///
    /// @return a new SpanElement
    SpanElement createSpanElement();

    /// Creates a new link (Link) inline element.
    ///
    /// @return a new LinkElement
    LinkElement createLinkElement();

    /// Creates a new figure (Figure) illustration element.
    ///
    /// @return a new FigureElement
    FigureElement createFigureElement();

    /// Creates a new table (Table) structure element.
    ///
    /// @return a new TableElement
    TableElement createTableElement();

    /// Creates a new table row (TR) structure element.
    ///
    /// @return a new TableTRElement
    TableTRElement createTableTRElement();

    /// Creates a new table header cell (TH) structure element.
    ///
    /// @return a new TableTHElement
    TableTHElement createTableTHElement();

    /// Creates a new table data cell (TD) structure element.
    ///
    /// @return a new TableTDElement
    TableTDElement createTableTDElement();

    /// Creates a new list (L) structure element.
    ///
    /// @return a new ListElement
    ListElement createListElement();

    /// Creates a new list item (LI) structure element.
    ///
    /// @return a new ListLIElement
    ListLIElement createListLIElement();

    /// Creates a new section (Sect) grouping element.
    ///
    /// @return a new SectElement
    SectElement createSectElement();

    /// Creates a new part (Part) grouping element.
    ///
    /// @return a new PartElement
    PartElement createPartElement();

    /// Creates a new note (Note) inline element.
    ///
    /// @return a new NoteElement
    NoteElement createNoteElement();

    /// Creates a new quote (Quote) inline element.
    ///
    /// @return a new QuoteElement
    QuoteElement createQuoteElement();

    /// Creates a new form (Form) illustration element.
    ///
    /// @return a new FormElement
    FormElement createFormElement();

    /// Creates a new formula (Formula) illustration element.
    ///
    /// @return a new FormulaElement
    FormulaElement createFormulaElement();

    /// Creates a new list label (Lbl) element.
    ///
    /// @return a new ListLblElement
    ListLblElement createListLblElement();

    /// Creates a new list body (LBody) element.
    ///
    /// @return a new ListLBodyElement
    ListLBodyElement createListLBodyElement();

    /// Creates a new table header (THead) grouping element.
    ///
    /// @return a new TableTHeadElement
    TableTHeadElement createTableTHeadElement();

    /// Creates a new table body (TBody) grouping element.
    ///
    /// @return a new TableTBodyElement
    TableTBodyElement createTableTBodyElement();

    /// Creates a new table footer (TFoot) grouping element.
    ///
    /// @return a new TableTFootElement
    TableTFootElement createTableTFootElement();

    /// Returns the StructTreeRoot wrapper for this document.
    ///
    /// @return the StructTreeRoot
    StructTreeRoot getStructTreeRootElement();

    /// Returns the document-level structure text state settings.
    ///
    /// @return the structure text state
    StructureTextState getStructureTextState();
}
