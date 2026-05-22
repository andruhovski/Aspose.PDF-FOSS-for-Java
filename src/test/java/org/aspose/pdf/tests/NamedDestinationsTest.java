package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
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
        COSDictionary catalog = new COSDictionary();
        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        assertNull(nd.get("nonexistent"));
    }

    @Test
    public void destsDictResolvesDestination() throws IOException {
        // Build catalog with /Dests dict containing a named destination
        COSDictionary catalog = new COSDictionary();
        COSDictionary dests = new COSDictionary();
        // Destination value: explicit array [null /Fit]
        COSArray destArr = new COSArray();
        destArr.add(COSNull.INSTANCE);
        destArr.add(COSName.of("Fit"));
        dests.set(COSName.of("chapter1"), destArr);
        catalog.set(COSName.of("Dests"), dests);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        ExplicitDestination dest = nd.get("chapter1");
        assertNotNull(dest, "Should resolve named destination from /Dests dict");
    }

    @Test
    public void destsDictWithDestDictionary() throws IOException {
        // /Dests entry can be a dict with /D pointing to the explicit dest
        COSDictionary catalog = new COSDictionary();
        COSDictionary dests = new COSDictionary();
        COSDictionary destDict = new COSDictionary();
        COSArray destArr = new COSArray();
        destArr.add(COSNull.INSTANCE);
        destArr.add(COSName.of("FitH"));
        destArr.add(COSInteger.valueOf(500));
        destDict.set(COSName.of("D"), destArr);
        dests.set(COSName.of("section2"), destDict);
        catalog.set(COSName.of("Dests"), dests);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        ExplicitDestination dest = nd.get("section2");
        assertNotNull(dest, "Should resolve destination dictionary with /D");
    }

    // ═══════════════════════════════════════════════════════════════
    //  NamedDestinations from /Names→/Dests name tree (PDF 1.2+)
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void nameTreeResolvesDestination() throws IOException {
        COSDictionary catalog = buildCatalogWithNameTree(
                new String[]{"alpha", "beta"},
                new COSBase[]{
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
        COSDictionary catalog = new COSDictionary();
        COSDictionary namesDict = new COSDictionary();
        COSDictionary destsTree = new COSDictionary();

        COSArray limits = new COSArray();
        limits.add(new COSString("alpha"));
        limits.add(new COSString("beta"));
        destsTree.set(COSName.of("Limits"), limits);

        COSArray names = new COSArray();
        names.add(new COSString("alpha")); names.add(buildFitDest());
        names.add(new COSString("beta")); names.add(buildFitDest());
        destsTree.set(COSName.of("Names"), names);

        namesDict.set(COSName.of("Dests"), destsTree);
        catalog.set(COSName.of("Names"), namesDict);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        assertNotNull(nd.get("alpha"));
        assertNotNull(nd.get("beta"));
        assertNull(nd.get("gamma"), "Out of limits range → null");
        assertNull(nd.get("a"), "Before limits min → null");
    }

    @Test
    public void nameTreeWithKidsMultiLevel() throws IOException {
        // Build a two-level name tree with /Kids
        COSDictionary catalog = new COSDictionary();
        COSDictionary namesDict = new COSDictionary();

        // Leaf 1: "alpha"
        COSDictionary leaf1 = new COSDictionary();
        COSArray names1 = new COSArray();
        names1.add(new COSString("alpha")); names1.add(buildFitDest());
        leaf1.set(COSName.of("Names"), names1);
        COSArray limits1 = new COSArray();
        limits1.add(new COSString("alpha")); limits1.add(new COSString("alpha"));
        leaf1.set(COSName.of("Limits"), limits1);

        // Leaf 2: "beta"
        COSDictionary leaf2 = new COSDictionary();
        COSArray names2 = new COSArray();
        names2.add(new COSString("beta")); names2.add(buildFitDest());
        leaf2.set(COSName.of("Names"), names2);
        COSArray limits2 = new COSArray();
        limits2.add(new COSString("beta")); limits2.add(new COSString("beta"));
        leaf2.set(COSName.of("Limits"), limits2);

        // Root with /Kids
        COSDictionary root = new COSDictionary();
        COSArray kids = new COSArray();
        kids.add(leaf1);
        kids.add(leaf2);
        root.set(COSName.of("Kids"), kids);

        namesDict.set(COSName.of("Dests"), root);
        catalog.set(COSName.of("Names"), namesDict);

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
        COSDictionary catalog = new COSDictionary();
        // /Dests dict
        COSDictionary dests = new COSDictionary();
        dests.set(COSName.of("fromDict"), buildFitDest());
        catalog.set(COSName.of("Dests"), dests);
        // /Names → /Dests tree
        COSDictionary namesDict = new COSDictionary();
        COSDictionary destsTree = new COSDictionary();
        COSArray names = new COSArray();
        names.add(new COSString("fromTree")); names.add(buildFitDest());
        destsTree.set(COSName.of("Names"), names);
        namesDict.set(COSName.of("Dests"), destsTree);
        catalog.set(COSName.of("Names"), namesDict);

        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        List<String> allNames = nd.getNames();
        assertTrue(allNames.contains("fromTree"));
        assertTrue(allNames.contains("fromDict"));
        assertEquals(2, nd.getCount());
    }

    @Test
    public void nonExistentNameReturnsNull() throws IOException {
        COSDictionary catalog = new COSDictionary();
        NamedDestinations nd = new NamedDestinations(catalog, null, null);
        assertNull(nd.get("doesNotExist"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  LinkAnnotation with named destination
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void linkAnnotationWithStringDest() throws IOException {
        // LinkAnnotation with /Dest as COSString (named dest)
        // Without a Document with named dests, should return null
        COSDictionary linkDict = new COSDictionary();
        linkDict.set(COSName.of("Subtype"), COSName.of("Link"));
        linkDict.set(COSName.of("Dest"), new COSString("chapter1"));

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
        // GoToAction with /D as COSName (named dest)
        COSDictionary dict = new COSDictionary();
        dict.set(COSName.of("S"), COSName.of("GoTo"));
        dict.set(COSName.of("D"), COSName.of("chapter1"));

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

    private COSArray buildFitDest() {
        COSArray arr = new COSArray();
        arr.add(COSNull.INSTANCE);
        arr.add(COSName.of("Fit"));
        return arr;
    }

    private COSDictionary buildCatalogWithNameTree(String[] keys, COSBase[] values) {
        COSDictionary catalog = new COSDictionary();
        COSDictionary namesDict = new COSDictionary();
        COSDictionary destsTree = new COSDictionary();
        COSArray names = new COSArray();
        for (int i = 0; i < keys.length; i++) {
            names.add(new COSString(keys[i]));
            names.add(values[i]);
        }
        destsTree.set(COSName.of("Names"), names);
        namesDict.set(COSName.of("Dests"), destsTree);
        catalog.set(COSName.of("Names"), namesDict);
        return catalog;
    }
}
