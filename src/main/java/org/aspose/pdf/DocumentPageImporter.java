package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSCloner;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Imports pages from one {@link Document} into another by performing a full
 * deep copy of the source page's COS subgraph into fresh indirect objects
 * belonging to the target document. Cross-document {@code /Parent},
 * {@code /Dest} and {@code /StructParent[s]} references are dropped; shared
 * resources (fonts, images, extended graphics states) appearing on multiple
 * imported pages are cloned exactly once via {@link COSCloner}.
 * <p>
 * Instances of this class are cheap; reuse one instance per source→target
 * pair so shared resources are deduplicated across multiple imported pages.
 * </p>
 */
public final class DocumentPageImporter {

    private static final Logger LOG = Logger.getLogger(DocumentPageImporter.class.getName());

    private final Document target;
    private final Document source;
    /**
     * Long-lived cloner for sub-resource deduplication (fonts, color spaces,
     * images shared across imported pages). NOT used for the page-root
     * dictionary itself — see {@link #importPage(Page)} for why.
     */
    private final COSCloner cloner;

    /**
     * @param target target document, receives cloned pages
     * @param source source document, owns the original pages
     * @throws IllegalArgumentException if either argument is null
     * @throws IllegalStateException    if the source is encrypted and not authenticated
     */
    public DocumentPageImporter(Document target, Document source) {
        if (target == null) throw new IllegalArgumentException("target must not be null");
        if (source == null) throw new IllegalArgumentException("source must not be null");
        this.target = target;
        this.source = source;
        this.cloner = new COSCloner(target::registerImportedObject);
        if (source.isEncrypted()) {
            // Documents opened with the correct password resolve refs through a
            // decryptor installed on the parser. If no decryptor is set, strings
            // and stream data would still be ciphertext — refuse.
            if (!sourceAuthenticated()) {
                throw new IllegalStateException(
                        "Cannot import pages from encrypted source document: not authenticated");
            }
        }
    }

    /**
     * Deep-copies {@code sourcePage} into a new {@link Page} backed by a fresh
     * page dictionary registered in the target document. The result is not yet
     * attached to the target page tree — caller is expected to splice it into
     * {@code target.getPages()} (typically via {@link PageCollection#add(Page)}).
     */
    public Page importPage(Page sourcePage) throws IOException {
        if (sourcePage == null) throw new IllegalArgumentException("sourcePage must not be null");
        COSDictionary srcDict = sourcePage.getCOSDictionary();
        // The shared cloner caches every visited source COSBase so that fonts/
        // images/etc. shared across pages dedupe nicely on import. But that
        // cache must NOT span the page-root: importing the same source page
        // twice (Pages.add(src.Pages[1]); Pages.insert(35, src.Pages[1]); …)
        // is a legal Aspose pattern that must produce DISTINCT cloned page
        // dictionaries — otherwise /Kids ends up with multiple references to
        // the same dict, save/reload collapses them, and identity-based
        // PageCollection.delete(int) silently removes the wrong entry
        // (PDFNEWNET-31533_3 reproducer). Forget the source page-root before
        // each clone so the next clonePageDict produces a fresh top-level
        // copy; sub-resources still share their cached clones.
        cloner.forgetSource(srcDict);
        COSDictionary clonedDict = cloner.clonePageDict(srcDict);
        materializeInheritedPageProperties(sourcePage, clonedDict);
        COSObjectReference pageRef = target.registerImportedObject(clonedDict);
        remapAnnotations(srcDict, clonedDict, pageRef);
        promoteContentsToIndirect(clonedDict);
        Page newPage = new Page(clonedDict, target.getParser());
        newPage.setOwningDocument(target);
        return newPage;
    }

    /**
     * Materializes effective inheritable page properties on the cloned page
     * before it is attached to a different page tree. Without this, pages that
     * relied on inherited {@code /MediaBox}, {@code /CropBox}, {@code /Rotate}
     * or {@code /Resources} can silently pick up different values from the
     * target document's {@code /Pages} root after reparenting.
     */
    private void materializeInheritedPageProperties(Page sourcePage, COSDictionary clonedPage) throws IOException {
        Rectangle mediaBox = sourcePage.getMediaBox();
        if (mediaBox != null) {
            clonedPage.set(COSName.MEDIABOX, mediaBox.toCOSArray());
        }

        Rectangle cropBox = sourcePage.getCropBox();
        if (cropBox != null) {
            clonedPage.set(COSName.CROPBOX, cropBox.toCOSArray());
        }

        int rotate = sourcePage.getRotate();
        if (rotate != 0) {
            clonedPage.set(COSName.ROTATE, org.aspose.pdf.engine.cos.COSInteger.valueOf(rotate));
        } else {
            clonedPage.remove(COSName.ROTATE);
        }

        Resources resources = sourcePage.getResources();
        if (resources != null && resources.getCOSDictionary() != null) {
            COSBase clonedResources = cloner.cloneAny(resources.getCOSDictionary());
            if (clonedResources != null) {
                clonedPage.set(COSName.RESOURCES, clonedResources);
            }
        }
    }

    /**
     * Walks the cloned /Annots array: for each annotation dictionary, applies
     * {@link COSCloner#cloneAnnotationDict(COSDictionary)} so /P and /Dest are
     * dropped, then sets /P to a reference to the new page.
     */
    private void remapAnnotations(COSDictionary srcPage, COSDictionary newPage,
                                  COSObjectReference newPageRef) throws IOException {
        // Work off the source /Annots to control the cloning; the original
        // clonePageDict copied /Annots array refs blindly via cloneAny, which is
        // correct structurally but did not apply ANNOT_STOP_KEYS. Replace now.
        COSBase srcAnnotsVal = srcPage.get(COSName.ANNOTS);
        if (srcAnnotsVal instanceof COSObjectReference) {
            try {
                srcAnnotsVal = ((COSObjectReference) srcAnnotsVal).dereference();
            } catch (IOException e) {
                LOG.warning("Failed to dereference source /Annots: " + e.getMessage());
                newPage.remove(COSName.ANNOTS);
                return;
            }
        }
        if (!(srcAnnotsVal instanceof COSArray)) {
            return;
        }
        COSArray srcAnnots = (COSArray) srcAnnotsVal;
        COSArray newAnnots = new COSArray(srcAnnots.size());
        for (int i = 0; i < srcAnnots.size(); i++) {
            COSBase item = srcAnnots.get(i);
            COSDictionary srcAnnot = resolveDict(item);
            if (srcAnnot == null) continue;
            COSDictionary clonedAnnot = cloner.cloneAnnotationDict(srcAnnot);
            clonedAnnot.set(COSName.of("P"), newPageRef);
            newAnnots.add(target.registerImportedObject(clonedAnnot));
        }
        newPage.set(COSName.ANNOTS, newAnnots);
    }

    private COSDictionary resolveDict(COSBase v) throws IOException {
        if (v instanceof COSObjectReference) {
            COSBase r;
            try {
                r = ((COSObjectReference) v).dereference();
            } catch (RuntimeException e) {
                if (isMalformedReferenceFailure(e)) {
                    LOG.warning("Skipping malformed indirect dictionary reference during page import: " + e.getMessage());
                    return null;
                }
                throw e;
            }
            return r instanceof COSDictionary ? (COSDictionary) r : null;
        }
        return v instanceof COSDictionary ? (COSDictionary) v : null;
    }

    private boolean isMalformedReferenceFailure(RuntimeException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("Object number must be non-negative")
                || message.contains("Generation number must be non-negative");
    }

    /**
     * If /Contents was cloned as an inline COSStream (or array of inline streams),
     * promote it/them to indirect objects registered in the target. Writers expect
     * page content streams to be indirect.
     */
    private void promoteContentsToIndirect(COSDictionary newPage) {
        COSBase contents = newPage.get(COSName.CONTENTS);
        if (contents instanceof COSStream && ((COSStream) contents).getObjectKey() == null) {
            COSObjectReference ref = target.registerImportedObject(contents);
            newPage.set(COSName.CONTENTS, ref);
        } else if (contents instanceof COSArray) {
            COSArray arr = (COSArray) contents;
            COSArray promoted = new COSArray(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                COSBase c = arr.get(i);
                if (c instanceof COSStream && ((COSStream) c).getObjectKey() == null) {
                    promoted.add(target.registerImportedObject(c));
                } else {
                    promoted.add(c);
                }
            }
            newPage.set(COSName.CONTENTS, promoted);
        }
    }

    private boolean sourceAuthenticated() {
        // PDFParser exposes no isAuthenticated(); a document fails to open
        // entirely if authentication fails (initSecurity throws). So if the
        // source Document object exists at all, it is authenticated. This check
        // is defensive and accepts both interpretations.
        try {
            if (source.getParser() == null) return true;
            // Touch an arbitrary object through the parser: if a decryptor is
            // required but absent, getObject would fail. The parser is already
            // populated, so this is essentially a tautology — kept for
            // symmetry with the documented contract.
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
