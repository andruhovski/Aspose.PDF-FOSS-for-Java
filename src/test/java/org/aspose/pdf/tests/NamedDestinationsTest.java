package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for named destinations and destination resolution.
 */
public class NamedDestinationsTest {

    // ═══════════════════════════════════════════════════════════════
    //  NamedDestinations from /Dests dictionary (PDF 1.1)
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void emptyDestsDictReturnsNull() throws IOException {
        PdfDictionary catalog = new PdfDictionary();
        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        assertNull(nd.get("nonexistent"));
    }

    @Test
    public void destsDictResolvesDestination() throws IOException {
        // Build catalog with /Dests dict containing a named destination
        PdfDictionary catalog = new PdfDictionary();
        PdfDictionary dests = new PdfDictionary();
        // Destination value: explicit array [null /Fit]
        PdfArray destArr = new PdfArray();
        destArr.add(PdfNull.INSTANCE);
        destArr.add(PdfName.of("Fit"));
        dests.set(PdfName.of("chapter1"), destArr);
        catalog.set(PdfName.of("Dests"), dests);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        ExplicitDestination dest = nd.get("chapter1");
        assertNotNull(dest, "Should resolve named destination from /Dests dict");
    }

    @Test
    public void destsDictWithDestDictionary() throws IOException {
        // /Dests entry can be a dict with /D pointing to the explicit dest
        PdfDictionary catalog = new PdfDictionary();
        PdfDictionary dests = new PdfDictionary();
        PdfDictionary destDict = new PdfDictionary();
        PdfArray destArr = new PdfArray();
        destArr.add(PdfNull.INSTANCE);
        destArr.add(PdfName.of("FitH"));
        destArr.add(PdfInteger.valueOf(500));
        destDict.set(PdfName.of("D"), destArr);
        dests.set(PdfName.of("section2"), destDict);
        catalog.set(PdfName.of("Dests"), dests);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        ExplicitDestination dest = nd.get("section2");
        assertNotNull(dest, "Should resolve destination dictionary with /D");
    }

    // ═══════════════════════════════════════════════════════════════
    //  NamedDestinations from /Names→/Dests name tree (PDF 1.2+)
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void nameTreeResolvesDestination() throws IOException {
        PdfDictionary catalog = buildCatalogWithNameTree(
                new String[]{"alpha", "beta"},
                new PdfBase[]{
                        buildFitDest(),
                        buildFitDest()
                });

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        assertNotNull(nd.get("alpha"));
        assertNotNull(nd.get("beta"));
        assertNull(nd.get("gamma"));
    }

    @Test
    public void nameTreeWithLimitsRangeCheck() throws IOException {
        // Build a name tree node with /Limits ["alpha", "beta"]
        PdfDictionary catalog = new PdfDictionary();
        PdfDictionary namesDict = new PdfDictionary();
        PdfDictionary destsTree = new PdfDictionary();

        PdfArray limits = new PdfArray();
        limits.add(new PdfString("alpha"));
        limits.add(new PdfString("beta"));
        destsTree.set(PdfName.of("Limits"), limits);

        PdfArray names = new PdfArray();
        names.add(new PdfString("alpha")); names.add(buildFitDest());
        names.add(new PdfString("beta")); names.add(buildFitDest());
        destsTree.set(PdfName.of("Names"), names);

        namesDict.set(PdfName.of("Dests"), destsTree);
        catalog.set(PdfName.of("Names"), namesDict);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        assertNotNull(nd.get("alpha"));
        assertNotNull(nd.get("beta"));
        assertNull(nd.get("gamma"), "Out of limits range → null");
        assertNull(nd.get("a"), "Before limits min → null");
    }

    @Test
    public void nameTreeWithKidsMultiLevel() throws IOException {
        // Build a two-level name tree with /Kids
        PdfDictionary catalog = new PdfDictionary();
        PdfDictionary namesDict = new PdfDictionary();

        // Leaf 1: "alpha"
        PdfDictionary leaf1 = new PdfDictionary();
        PdfArray names1 = new PdfArray();
        names1.add(new PdfString("alpha")); names1.add(buildFitDest());
        leaf1.set(PdfName.of("Names"), names1);
        PdfArray limits1 = new PdfArray();
        limits1.add(new PdfString("alpha")); limits1.add(new PdfString("alpha"));
        leaf1.set(PdfName.of("Limits"), limits1);

        // Leaf 2: "beta"
        PdfDictionary leaf2 = new PdfDictionary();
        PdfArray names2 = new PdfArray();
        names2.add(new PdfString("beta")); names2.add(buildFitDest());
        leaf2.set(PdfName.of("Names"), names2);
        PdfArray limits2 = new PdfArray();
        limits2.add(new PdfString("beta")); limits2.add(new PdfString("beta"));
        leaf2.set(PdfName.of("Limits"), limits2);

        // Root with /Kids
        PdfDictionary root = new PdfDictionary();
        PdfArray kids = new PdfArray();
        kids.add(leaf1);
        kids.add(leaf2);
        root.set(PdfName.of("Kids"), kids);

        namesDict.set(PdfName.of("Dests"), root);
        catalog.set(PdfName.of("Names"), namesDict);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        assertNotNull(nd.get("alpha"));
        assertNotNull(nd.get("beta"));
        assertNull(nd.get("gamma"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  getNames() and getCount()
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void getNamesFromBothSources() throws IOException {
        PdfDictionary catalog = new PdfDictionary();
        // /Dests dict
        PdfDictionary dests = new PdfDictionary();
        dests.set(PdfName.of("fromDict"), buildFitDest());
        catalog.set(PdfName.of("Dests"), dests);
        // /Names → /Dests tree
        PdfDictionary namesDict = new PdfDictionary();
        PdfDictionary destsTree = new PdfDictionary();
        PdfArray names = new PdfArray();
        names.add(new PdfString("fromTree")); names.add(buildFitDest());
        destsTree.set(PdfName.of("Names"), names);
        namesDict.set(PdfName.of("Dests"), destsTree);
        catalog.set(PdfName.of("Names"), namesDict);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        List<String> allNames = nd.getNames();
        assertTrue(allNames.contains("fromTree"));
        assertTrue(allNames.contains("fromDict"));
        assertEquals(2, nd.getCount());
    }

    @Test
    public void nonExistentNameReturnsNull() throws IOException {
        PdfDictionary catalog = new PdfDictionary();
        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        assertNull(nd.get("doesNotExist"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  LinkAnnotation with named destination
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void linkAnnotationWithStringDest() throws IOException {
        // LinkAnnotation with /Dest as PdfString (named dest)
        // Without a Document with named dests, should return null
        PdfDictionary linkDict = new PdfDictionary();
        linkDict.set(PdfName.of("Subtype"), PdfName.of("Link"));
        linkDict.set(PdfName.of("Dest"), new PdfString("chapter1"));

        org.aspose.pdf.annotations.LinkAnnotation link =
                new org.aspose.pdf.annotations.LinkAnnotation(linkDict, null);
        // Without doc, named dest can't be resolved
        assertNull(link.getDestination(null));
    }

    // ═══════════════════════════════════════════════════════════════
    //  GoToAction with named destination
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void goToActionWithNameDest() throws IOException {
        // GoToAction with /D as PdfName (named dest)
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("S"), PdfName.of("GoTo"));
        dict.set(PdfName.of("D"), PdfName.of("chapter1"));

        GoToAction action = new GoToAction(dict, null);
        // Without doc, named dest can't be resolved → null destination
        assertNull(action.getDestination());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Write-side API (add / set / remove / getNamesArray / size)
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void addNamedDestination_createsNameTree() throws Exception {
        try (Document doc = new Document()) {
            doc.getPages().add();
            doc.getNamedDestinations().add("Page1Top",
                    new XYZExplicitDestination(1, 0, 800, 1.0));

            ExplicitDestination resolved = doc.getNamedDestinations().get("Page1Top");
            assertNotNull(resolved, "Newly-added destination should be resolvable");
            assertTrue(resolved instanceof XYZExplicitDestination);
            assertEquals(1, doc.getNamedDestinations().size());
        }
    }

    @Test
    public void addOverridesExisting() throws Exception {
        try (Document doc = new Document()) {
            doc.getPages().add();
            doc.getNamedDestinations().add("X", new XYZExplicitDestination(1, 0, 0, 1.0));
            doc.getNamedDestinations().add("X", new XYZExplicitDestination(1, 100, 200, 0.5));

            ExplicitDestination latest = doc.getNamedDestinations().get("X");
            assertTrue(latest instanceof XYZExplicitDestination);
            assertEquals(0.5, ((XYZExplicitDestination) latest).getZoom(), 0.001);
            assertEquals(1, doc.getNamedDestinations().size());
        }
    }

    @Test
    public void setIsAliasForAdd() throws Exception {
        try (Document doc = new Document()) {
            doc.getPages().add();
            doc.getNamedDestinations().set("Y", new XYZExplicitDestination(1, 0, 0, 1.0));
            assertNotNull(doc.getNamedDestinations().get("Y"));
        }
    }

    @Test
    public void getNamesArrayReturnsAllNames() throws Exception {
        try (Document doc = new Document()) {
            doc.getPages().add();
            doc.getNamedDestinations().add("A", new XYZExplicitDestination(1, 0, 0, 1.0));
            doc.getNamedDestinations().add("B", new XYZExplicitDestination(1, 0, 0, 1.0));
            doc.getNamedDestinations().add("C", new XYZExplicitDestination(1, 0, 0, 1.0));

            String[] names = doc.getNamedDestinations().getNamesArray();
            assertEquals(3, names.length);
            java.util.Arrays.sort(names);
            assertArrayEquals(new String[] {"A", "B", "C"}, names);
        }
    }

    @Test
    public void removeNamedDestination() throws Exception {
        try (Document doc = new Document()) {
            doc.getPages().add();
            doc.getNamedDestinations().add("X", new XYZExplicitDestination(1, 0, 0, 1.0));
            assertNotNull(doc.getNamedDestinations().get("X"));

            assertTrue(doc.getNamedDestinations().remove("X"));
            assertNull(doc.getNamedDestinations().get("X"));
            assertFalse(doc.getNamedDestinations().remove("X"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  NamedDestination class
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void namedDestinationReferencesDocumentAndName() throws Exception {
        try (Document doc = new Document()) {
            NamedDestination nd = new NamedDestination(doc, "MyDest");
            assertEquals("MyDest", nd.getName());
            assertSame(doc, nd.getDocument());
        }
    }

    @Test
    public void namedDestinationResolveReturnsExplicit() throws Exception {
        try (Document doc = new Document()) {
            doc.getPages().add();
            doc.getNamedDestinations().add("Target",
                    new XYZExplicitDestination(1, 50, 600, 0.75));

            NamedDestination ref = new NamedDestination(doc, "Target");
            ExplicitDestination resolved = ref.resolve();
            assertNotNull(resolved);
            assertTrue(resolved instanceof XYZExplicitDestination);
            assertEquals(0.75, ((XYZExplicitDestination) resolved).getZoom(), 0.001);
        }
    }

    @Test
    public void namedDestinationResolveMissingReturnsNull() throws Exception {
        try (Document doc = new Document()) {
            NamedDestination ref = new NamedDestination(doc, "DoesNotExist");
            assertNull(ref.resolve());
        }
    }

    @Test
    public void namedDestinationIsIAppointment() throws Exception {
        try (Document doc = new Document()) {
            NamedDestination nd = new NamedDestination(doc, "X");
            assertTrue(nd instanceof IAppointment);
        }
    }

    @Test
    public void explicitDestinationIsIAppointment() {
        XYZExplicitDestination xyz = new XYZExplicitDestination(1, 0, 0, 1.0);
        assertTrue(xyz instanceof IAppointment);
    }

    // ═══════════════════════════════════════════════════════════════
    //  GoToAction.setDestination(IAppointment) accepts both forms
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void goToActionAcceptsNamedDestination() throws Exception {
        try (Document doc = new Document()) {
            doc.getPages().add();
            doc.getNamedDestinations().add("Tgt",
                    new XYZExplicitDestination(1, 0, 0, 0.5));

            GoToAction action = new GoToAction();
            action.setDestination(new NamedDestination(doc, "Tgt"));

            assertNotNull(action.getAppointment());
            assertTrue(action.getAppointment() instanceof NamedDestination);
            // getDestination() lazily resolves
            ExplicitDestination resolved = action.getDestination();
            assertNotNull(resolved);
            assertEquals(0.5, ((XYZExplicitDestination) resolved).getZoom(), 0.001);
        }
    }

    @Test
    public void goToActionAcceptsExplicitDestinationViaIAppointment() throws Exception {
        XYZExplicitDestination xyz = new XYZExplicitDestination(1, 100, 200, 1.0);
        GoToAction action = new GoToAction();
        action.setDestination((IAppointment) xyz);
        assertSame(xyz, action.getDestination());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════

    private PdfArray buildFitDest() {
        PdfArray arr = new PdfArray();
        arr.add(PdfNull.INSTANCE);
        arr.add(PdfName.of("Fit"));
        return arr;
    }

    private PdfDictionary buildCatalogWithNameTree(String[] keys, PdfBase[] values) {
        PdfDictionary catalog = new PdfDictionary();
        PdfDictionary namesDict = new PdfDictionary();
        PdfDictionary destsTree = new PdfDictionary();
        PdfArray names = new PdfArray();
        for (int i = 0; i < keys.length; i++) {
            names.add(new PdfString(keys[i]));
            names.add(values[i]);
        }
        destsTree.set(PdfName.of("Names"), names);
        namesDict.set(PdfName.of("Dests"), destsTree);
        catalog.set(PdfName.of("Names"), namesDict);
        return catalog;
    }
}
