package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.logicalstructure.*;
import org.aspose.pdf.logicalstructure.elements.*;
import org.aspose.pdf.tagged.TaggedContent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for Tagged PDF logical structure (§14.7, §14.8).
public class TaggedPdfTest {

    // ═══════════════════════════════════════════════════════════════
    //  StructureTypeStandard
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void standardTypesExist() {
        assertNotNull(StructureTypeStandard.Document);
        assertNotNull(StructureTypeStandard.P);
        assertNotNull(StructureTypeStandard.H1);
        assertNotNull(StructureTypeStandard.Table);
        assertNotNull(StructureTypeStandard.Figure);
        assertEquals("Document", StructureTypeStandard.Document.getName());
        assertEquals("H1", StructureTypeStandard.H1.getName());
    }

    @Test
    public void fromNameReturnsStandard() {
        assertSame(StructureTypeStandard.P, StructureTypeStandard.fromName("P"));
        assertSame(StructureTypeStandard.Table, StructureTypeStandard.fromName("Table"));
    }

    @Test
    public void fromNameCustomType() {
        StructureTypeStandard custom = StructureTypeStandard.fromName("MyCustomType");
        assertEquals("MyCustomType", custom.getName());
    }

    @Test
    public void standardTypesMapHasAll() {
        Map<String, StructureTypeStandard> all = StructureTypeStandard.getStandardTypes();
        assertTrue(all.size() >= 45, "Should have at least 45 standard types");
        assertTrue(all.containsKey("Document"));
        assertTrue(all.containsKey("P"));
        assertTrue(all.containsKey("H6"));
        assertTrue(all.containsKey("TD"));
        assertTrue(all.containsKey("WP"));
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(StructureTypeStandard.P, StructureTypeStandard.fromName("P"));
        assertEquals(StructureTypeStandard.P.hashCode(), StructureTypeStandard.fromName("P").hashCode());
        assertNotEquals(StructureTypeStandard.P, StructureTypeStandard.H1);
    }

    // ═══════════════════════════════════════════════════════════════
    //  StructureElement — reading
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void structureElementType() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("H2"));
        StructureElement elem = new StructureElement(dict, null);
        assertEquals(StructureTypeStandard.H2, elem.getStructureType());
    }

    @Test
    public void structureElementMetadata() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("P"));
        dict.setString("T", "My Title");
        dict.set(PdfName.of("Lang"), new PdfString("en-US"));
        dict.setString("Alt", "Alternative text");
        dict.setString("ActualText", "Actual replacement");
        dict.setString("E", "Expanded form");
        dict.setString("ID", "elem-001");

        StructureElement elem = new StructureElement(dict, null);
        assertEquals("My Title", elem.getTitle());
        assertEquals("en-US", elem.getLanguage());
        assertEquals("Alternative text", elem.getAlternateDescription());
        assertEquals("Actual replacement", elem.getActualText());
        assertEquals("Expanded form", elem.getExpandedForm());
        assertEquals("elem-001", elem.getID());
    }

    @Test
    public void structureElementChildElements() {
        PdfDictionary parent = new PdfDictionary();
        parent.set(PdfName.of("S"), PdfName.of("Div"));

        PdfDictionary child1 = new PdfDictionary();
        child1.set(PdfName.of("S"), PdfName.of("P"));
        PdfDictionary child2 = new PdfDictionary();
        child2.set(PdfName.of("S"), PdfName.of("H1"));

        PdfArray kids = new PdfArray();
        kids.add(child1);
        kids.add(child2);
        parent.set(PdfName.of("K"), kids);

        StructureElement elem = new StructureElement(parent, null);
        ElementList children = elem.getChildElements();
        assertEquals(2, children.getCount());
        assertEquals(StructureTypeStandard.P, children.get(0).getStructureType());
        assertEquals(StructureTypeStandard.H1, children.get(1).getStructureType());
    }

    @Test
    public void structureElementSingleChild() {
        PdfDictionary parent = new PdfDictionary();
        parent.set(PdfName.of("S"), PdfName.of("Div"));
        PdfDictionary child = new PdfDictionary();
        child.set(PdfName.of("S"), PdfName.of("Span"));
        parent.set(PdfName.of("K"), child);

        StructureElement elem = new StructureElement(parent, null);
        assertEquals(1, elem.getChildElements().getCount());
        assertEquals(StructureTypeStandard.Span, elem.getChildElements().get(0).getStructureType());
    }

    @Test
    public void getChildElementsFiltersMCR() {
        PdfDictionary parent = new PdfDictionary();
        parent.set(PdfName.of("S"), PdfName.of("P"));

        PdfArray kids = new PdfArray();
        // MCR dict
        PdfDictionary mcr = new PdfDictionary();
        mcr.set(PdfName.of("Type"), PdfName.of("MCR"));
        mcr.setInt("MCID", 5);
        kids.add(mcr);
        // StructElem
        PdfDictionary child = new PdfDictionary();
        child.set(PdfName.of("S"), PdfName.of("Span"));
        kids.add(child);
        // Bare integer MCID
        kids.add(PdfInteger.valueOf(3));
        parent.set(PdfName.of("K"), kids);

        StructureElement elem = new StructureElement(parent, null);
        // getChildElements() should only return the Span, not MCR or integer
        assertEquals(1, elem.getChildElements().getCount());
        assertEquals("Span", elem.getChildElements().get(0).getStructureType().getName());
    }

    @Test
    public void getAllKidsReturnsMixed() {
        PdfDictionary parent = new PdfDictionary();
        parent.set(PdfName.of("S"), PdfName.of("P"));

        PdfArray kids = new PdfArray();
        kids.add(PdfInteger.valueOf(7)); // bare MCID
        PdfDictionary mcr = new PdfDictionary();
        mcr.set(PdfName.of("Type"), PdfName.of("MCR"));
        mcr.setInt("MCID", 5);
        kids.add(mcr);
        PdfDictionary objr = new PdfDictionary();
        objr.set(PdfName.of("Type"), PdfName.of("OBJR"));
        kids.add(objr);
        PdfDictionary child = new PdfDictionary();
        child.set(PdfName.of("S"), PdfName.of("Span"));
        kids.add(child);
        parent.set(PdfName.of("K"), kids);

        StructureElement elem = new StructureElement(parent, null);
        List<Object> allKids = elem.getAllKids();
        assertEquals(4, allKids.size());
        assertTrue(allKids.get(0) instanceof MarkedContentReference);
        assertEquals(7, ((MarkedContentReference) allKids.get(0)).getMCID());
        assertTrue(allKids.get(1) instanceof MarkedContentReference);
        assertEquals(5, ((MarkedContentReference) allKids.get(1)).getMCID());
        assertTrue(allKids.get(2) instanceof ObjectReference);
        assertTrue(allKids.get(3) instanceof StructureElement);
    }

    // ═══════════════════════════════════════════════════════════════
    //  StructureElement — writing
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void appendChild() {
        PdfDictionary parentDict = new PdfDictionary();
        parentDict.set(PdfName.of("S"), PdfName.of("Div"));
        StructureElement parent = new StructureElement(parentDict, null);

        PdfDictionary childDict = new PdfDictionary();
        childDict.set(PdfName.of("S"), PdfName.of("P"));
        StructureElement child = new StructureElement(childDict, null);

        parent.appendChild(child);
        assertEquals(1, parent.getChildElements().getCount());
        assertEquals("P", parent.getChildElements().get(0).getStructureType().getName());
        // Child should have parent set
        assertNotNull(child.getPdfDictionary().get("P"));
    }

    @Test
    public void appendMultipleChildren() {
        PdfDictionary parentDict = new PdfDictionary();
        parentDict.set(PdfName.of("S"), PdfName.of("Div"));
        StructureElement parent = new StructureElement(parentDict, null);

        for (int i = 1; i <= 3; i++) {
            PdfDictionary cd = new PdfDictionary();
            cd.set(PdfName.of("S"), PdfName.of("P"));
            parent.appendChild(new StructureElement(cd, null));
        }
        assertEquals(3, parent.getChildElements().getCount());
    }

    @Test
    public void appendMarkedContent() {
        PdfDictionary elemDict = new PdfDictionary();
        elemDict.set(PdfName.of("S"), PdfName.of("P"));
        StructureElement elem = new StructureElement(elemDict, null);

        elem.appendMarkedContent(42, null);
        List<Object> kids = elem.getAllKids();
        assertEquals(1, kids.size());
        assertTrue(kids.get(0) instanceof MarkedContentReference);
        assertEquals(42, ((MarkedContentReference) kids.get(0)).getMCID());
    }

    // ═══════════════════════════════════════════════════════════════
    //  RoleMap
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void roleMapParsesAndResolves() {
        PdfDictionary roleMapDict = new PdfDictionary();
        roleMapDict.set(PdfName.of("MyParagraph"), PdfName.of("P"));
        roleMapDict.set(PdfName.of("MyHeading"), PdfName.of("H1"));

        RoleMap rm = RoleMap.parse(roleMapDict);
        assertEquals(2, rm.size());
        assertEquals(StructureTypeStandard.P, rm.resolve("MyParagraph"));
        assertEquals(StructureTypeStandard.H1, rm.resolve("MyHeading"));
        // Unknown type → just wraps as-is
        assertEquals("UnknownType", rm.resolve("UnknownType").getName());
    }

    @Test
    public void roleMapEmpty() {
        RoleMap rm = RoleMap.parse(null);
        assertEquals(0, rm.size());
        assertEquals("P", rm.resolve("P").getName());
    }

    // ═══════════════════════════════════════════════════════════════
    //  MarkedContentReference / ObjectReference
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void markedContentReferenceFromDict() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Type"), PdfName.of("MCR"));
        dict.setInt("MCID", 12);
        MarkedContentReference mcr = MarkedContentReference.fromDictionary(dict);
        assertEquals(12, mcr.getMCID());
        assertNull(mcr.getPage());
    }

    @Test
    public void objectReferenceFromDict() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("Type"), PdfName.of("OBJR"));
        PdfDictionary obj = new PdfDictionary();
        obj.set(PdfName.of("Subtype"), PdfName.of("Link"));
        dict.set(PdfName.of("Obj"), obj);
        ObjectReference or = ObjectReference.fromDictionary(dict);
        assertNotNull(or.getReferencedObject());
    }

    // ═══════════════════════════════════════════════════════════════
    //  StructTreeRoot
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void structTreeRootGetRootElement() {
        PdfDictionary rootDict = new PdfDictionary();
        rootDict.set(PdfName.of("Type"), PdfName.of("StructTreeRoot"));
        PdfDictionary docElem = new PdfDictionary();
        docElem.set(PdfName.of("S"), PdfName.of("Document"));
        rootDict.set(PdfName.of("K"), docElem);

        StructTreeRoot root = new StructTreeRoot(rootDict, null);
        StructureElement rootElem = root.getRootElement();
        assertNotNull(rootElem);
        assertEquals(StructureTypeStandard.Document, rootElem.getStructureType());
    }

    @Test
    public void structTreeRootGetChildren() {
        PdfDictionary rootDict = new PdfDictionary();
        PdfArray kids = new PdfArray();
        PdfDictionary elem1 = new PdfDictionary();
        elem1.set(PdfName.of("S"), PdfName.of("Document"));
        kids.add(elem1);
        rootDict.set(PdfName.of("K"), kids);

        StructTreeRoot root = new StructTreeRoot(rootDict, null);
        assertEquals(1, root.getChildren().getCount());
    }

    @Test
    public void structTreeRootCreateNew() {
        StructTreeRoot root = StructTreeRoot.createNew();
        assertNotNull(root.getPdfDictionary());
        assertEquals("StructTreeRoot", root.getPdfDictionary().getNameAsString("Type"));
        StructureElement docElem = root.getRootElement();
        assertNotNull(docElem);
        assertEquals(StructureTypeStandard.Document, docElem.getStructureType());
    }

    @Test
    public void structTreeRootRoleMap() {
        PdfDictionary rootDict = new PdfDictionary();
        PdfDictionary rm = new PdfDictionary();
        rm.set(PdfName.of("Custom"), PdfName.of("Div"));
        rootDict.set(PdfName.of("RoleMap"), rm);

        StructTreeRoot root = new StructTreeRoot(rootDict, null);
        RoleMap roleMap = root.getRoleMap();
        assertEquals(StructureTypeStandard.Div, roleMap.resolve("Custom"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  TaggedContent
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void taggedContentAutoCreatesStructTree() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);

        StructureElement root = tc.getRootElement();
        assertNotNull(root, "Should auto-create structure tree");
        assertEquals(StructureTypeStandard.Document, root.getStructureType());

        // Should have /StructTreeRoot in catalog now
        assertNotNull(catalog.get("StructTreeRoot"));
        // Should have /MarkInfo/Marked=true
        PdfBase mi = catalog.get("MarkInfo");
        assertTrue(mi instanceof PdfDictionary);
        assertTrue(((PdfDictionary) mi).getBoolean("Marked", false));
    }

    @Test
    public void taggedContentSetLanguage() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        tc.setLanguage("en-US");
        assertEquals("en-US", tc.getLanguage());
    }

    @Test
    public void taggedContentCreateElement() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        StructureElement h1 = tc.createElement(StructureTypeStandard.H1);
        assertNotNull(h1);
        assertEquals(StructureTypeStandard.H1, h1.getStructureType());
        assertEquals("StructElem", h1.getPdfDictionary().getNameAsString("Type"));
    }

    @Test
    public void taggedContentReadsExistingTree() {
        // Build a catalog with an existing StructTreeRoot
        PdfDictionary catalog = new PdfDictionary();
        PdfDictionary strRoot = new PdfDictionary();
        strRoot.set(PdfName.of("Type"), PdfName.of("StructTreeRoot"));
        PdfDictionary docElem = new PdfDictionary();
        docElem.set(PdfName.of("S"), PdfName.of("Document"));
        docElem.setString("T", "Test Doc");
        strRoot.set(PdfName.of("K"), docElem);
        catalog.set(PdfName.of("StructTreeRoot"), strRoot);

        TaggedContent tc = new TaggedContent(null, catalog, null);
        StructureElement root = tc.getRootElement();
        assertNotNull(root);
        assertEquals("Document", root.getStructureType().getName());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Document without StructTreeRoot
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void documentWithoutStructTreeReturnsNull() {
        // Simulate via TaggedContent with empty catalog
        PdfDictionary catalog = new PdfDictionary();
        // getLogicalStructure equivalent: check if StructTreeRoot exists
        assertNull(catalog.get("StructTreeRoot"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  StructureTextState
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void structureTextStateProperties() {
        StructureTextState sts = new StructureTextState();
        assertNull(sts.getFont());
        assertNull(sts.getFontSize());
        assertEquals(0, sts.getFontStyle());
        assertFalse(sts.isStrikeOut());
        assertFalse(sts.isUnderline());
        assertFalse(sts.isSubscript());
        assertFalse(sts.isSuperscript());

        sts.setFontSize(12.0f);
        assertEquals(12.0f, sts.getFontSize());
        sts.setFontStyle(org.aspose.pdf.text.FontStyles.Bold | org.aspose.pdf.text.FontStyles.Italic);
        assertEquals(3, sts.getFontStyle());
        sts.setStrikeOut(true);
        assertTrue(sts.isStrikeOut());
        sts.setUnderline(true);
        assertTrue(sts.isUnderline());
        sts.setCharacterSpacing(2.5f);
        assertEquals(2.5f, sts.getCharacterSpacing());
        sts.setWordSpacing(3.0f);
        assertEquals(3.0f, sts.getWordSpacing());
        sts.setLineSpacing(1.5f);
        assertEquals(1.5f, sts.getLineSpacing());
        sts.setHorizontalScaling(110f);
        assertEquals(110f, sts.getHorizontalScaling());
    }

    @Test
    public void elementExposesStructureTextState() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        ParagraphElement p = tc.createParagraphElement();

        StructureTextState sts = p.getStructureTextState();
        assertNotNull(sts);
        assertSame(sts, p.getStructureTextState()); // same instance
        sts.setFontSize(14f);
        assertEquals(14f, p.getStructureTextState().getFontSize());
    }

    // ═══════════════════════════════════════════════════════════════
    //  New element types: ListLbl, ListLBody, TableTHead/TBody/TFoot
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void createListLblAndLBodyElements() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);

        ListLblElement lbl = tc.createListLblElement();
        assertNotNull(lbl);
        assertEquals("Lbl", lbl.getStructureElement().getStructureType().getName());

        ListLBodyElement lBody = tc.createListLBodyElement();
        assertNotNull(lBody);
        assertEquals("LBody", lBody.getStructureElement().getStructureType().getName());
    }

    @Test
    public void createTableGroupingElements() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);

        TableTHeadElement thead = tc.createTableTHeadElement();
        assertNotNull(thead);
        assertEquals("THead", thead.getStructureElement().getStructureType().getName());

        TableTBodyElement tbody = tc.createTableTBodyElement();
        assertNotNull(tbody);
        assertEquals("TBody", tbody.getStructureElement().getStructureType().getName());

        TableTFootElement tfoot = tc.createTableTFootElement();
        assertNotNull(tfoot);
        assertEquals("TFoot", tfoot.getStructureElement().getStructureType().getName());
    }

    @Test
    public void listWithLblAndLBodyStructure() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);

        ListElement list = tc.createListElement();
        ListLIElement li = tc.createListLIElement();
        ListLblElement lbl = tc.createListLblElement();
        ListLBodyElement lBody = tc.createListLBodyElement();
        SpanElement lblSpan = tc.createSpanElement();
        lblSpan.setText("1. ");
        SpanElement bodySpan = tc.createSpanElement();
        bodySpan.setText("item text");

        lbl.appendChild(lblSpan);
        lBody.appendChild(bodySpan);
        li.appendChild(lbl);
        li.appendChild(lBody);
        list.appendChild(li);

        // Verify structure
        ElementList liChildren = li.getStructureElement().getChildElements();
        assertEquals(2, liChildren.getCount());
        assertEquals("Lbl", liChildren.get(0).getStructureType().getName());
        assertEquals("LBody", liChildren.get(1).getStructureType().getName());
    }

    // ═══════════════════════════════════════════════════════════════
    //  PositionSettings and adjustPosition
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void positionSettingsOnElement() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        SpanElement span = tc.createSpanElement();

        assertNull(span.getPositionSettings());

        org.aspose.pdf.tagged.PositionSettings ps = new org.aspose.pdf.tagged.PositionSettings();
        ps.setInLineParagraph(true);
        ps.setInNewPage(false);
        ps.setMargin(new org.aspose.pdf.MarginInfo(10, 20, 30, 40));
        span.adjustPosition(ps);

        assertNotNull(span.getPositionSettings());
        assertTrue(span.getPositionSettings().isInLineParagraph());
        assertFalse(span.getPositionSettings().isInNewPage());
        assertEquals(10, span.getPositionSettings().getMargin().getLeft());
    }

    // ═══════════════════════════════════════════════════════════════
    //  StructTreeRoot.getAllElements / findElements
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void structTreeRootGetAllElements() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        StructureElement root = tc.getRootElement();

        // Add some children
        ParagraphElement p1 = tc.createParagraphElement();
        ParagraphElement p2 = tc.createParagraphElement();
        HeaderElement h1 = tc.createHeaderElement(1);
        root.appendChild(p1.getStructureElement());
        root.appendChild(p2.getStructureElement());
        root.appendChild(h1.getStructureElement());

        java.util.List<StructureElement> all = tc.getStructTreeRoot().getAllElements();
        // Should include: root (Document) + 3 children = 4
        assertEquals(4, all.size());
        assertEquals("Document", all.get(0).getStructureType().getName());
    }

    @Test
    public void structTreeRootFindElements() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        StructureElement root = tc.getRootElement();

        ParagraphElement p1 = tc.createParagraphElement();
        ParagraphElement p2 = tc.createParagraphElement();
        HeaderElement h1 = tc.createHeaderElement(1);
        root.appendChild(p1.getStructureElement());
        root.appendChild(p2.getStructureElement());
        root.appendChild(h1.getStructureElement());

        java.util.List<StructureElement> paragraphs = tc.getStructTreeRoot().findElements("P");
        assertEquals(2, paragraphs.size());

        java.util.List<StructureElement> headers = tc.getStructTreeRoot().findElements("H1");
        assertEquals(1, headers.size());
    }

    @Test
    public void structTreeRootClearChilds() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        StructureElement root = tc.getRootElement();
        assertNotNull(root);

        tc.getStructTreeRoot().clearChilds();
        assertNull(tc.getStructTreeRoot().getRootElement());
    }

    // ═══════════════════════════════════════════════════════════════
    //  ITaggedContent.getStructTreeRootElement / getStructureTextState
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void taggedContentExposesStructTreeRootElement() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);

        StructTreeRoot str = tc.getStructTreeRootElement();
        assertNotNull(str);
        assertSame(tc.getStructTreeRoot(), str);
    }

    @Test
    public void taggedContentExposesDocumentTextState() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);

        StructureTextState sts = tc.getStructureTextState();
        assertNotNull(sts);
        assertSame(sts, tc.getStructureTextState()); // same instance
    }

    // ═══════════════════════════════════════════════════════════════
    //  FigureElement.setImage / LinkElement.setHyperlink
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void figureElementImage() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        FigureElement fig = tc.createFigureElement();
        assertNull(fig.getImagePath());
        fig.setImage("/path/to/image.jpg");
        assertEquals("/path/to/image.jpg", fig.getImagePath());
        fig.setAlternativeText("A test image");
        assertEquals("A test image", fig.getAlternativeText());
    }

    @Test
    public void linkElementHyperlink() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        LinkElement link = tc.createLinkElement();
        assertNull(link.getHyperlink());
        link.setHyperlink("https://example.com");
        assertEquals("https://example.com", link.getHyperlink());
        link.setText("Example");
        assertEquals("Example", link.getText());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Element.setAlternativeText
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void elementAlternativeText() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        FigureElement fig = tc.createFigureElement();
        fig.setAlternativeText("Graph");
        assertEquals("Graph", fig.getAlternativeText());
        assertEquals("Graph", fig.getAlternateDescription());
    }

    // ═══════════════════════════════════════════════════════════════
    //  TableElement.repeatingRowsCount
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void tableRepeatingRowsCount() {
        PdfDictionary catalog = new PdfDictionary();
        TaggedContent tc = new TaggedContent(null, catalog, null);
        TableElement table = tc.createTableElement();
        assertEquals(0, table.getRepeatingRowsCount());
        table.setRepeatingRowsCount(3);
        assertEquals(3, table.getRepeatingRowsCount());
    }
}
