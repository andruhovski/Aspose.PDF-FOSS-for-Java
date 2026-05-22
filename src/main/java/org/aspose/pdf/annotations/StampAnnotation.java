package org.aspose.pdf.annotations;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stamp annotation (ISO 32000-1:2008, Section 12.5.6.12, /Subtype /Stamp).
 * <p>
 * A stamp annotation displays text or a graphic intended to look as if it were
 * stamped on the page with a rubber stamp. Common stamp names include "Approved",
 * "Experimental", "NotApproved", "AsIs", "Expired", "Draft", etc.
 * </p>
 */
public class StampAnnotation extends MarkupAnnotation {

    /**
     * Constructs a stamp annotation from an existing COS dictionary.
     *
     * @param dict the COS dictionary backing this annotation
     * @param page the page this annotation belongs to
     */
    public StampAnnotation(COSDictionary dict, Page page) {
        super(dict, page);
    }

    /**
     * Constructs a new stamp annotation with the given rectangle on the specified page.
     *
     * @param page the page this annotation belongs to
     * @param rect the annotation rectangle
     */
    public StampAnnotation(Page page, Rectangle rect) {
        super(page, rect);
        dict.set(COSName.of("Subtype"), COSName.of("Stamp"));
    }

    /**
     * Constructs a stamp annotation associated with a document but not yet attached to a page.
     *
     * @param document the document this stamp annotation belongs to
     */
    public StampAnnotation(Document document) {
        super((COSDictionary) null, (Page) null);
        dict.set(COSName.of("Type"), COSName.of("Annot"));
        dict.set(COSName.of("Subtype"), COSName.of("Stamp"));
    }

    /** Stored image data for the stamp appearance. */
    private byte[] imageData;

    /**
     * Sets the image for this stamp annotation from an input stream.
     * <p>
     * The image data is stored and will be used to build the stamp's appearance stream.
     * </p>
     *
     * @param imageStream the input stream containing the image data
     * @throws IOException if reading the stream fails
     */
    public void setImage(InputStream imageStream) throws IOException {
        if (imageStream == null) {
            this.imageData = null;
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = imageStream.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        this.imageData = baos.toByteArray();
    }

    /**
     * Returns the image data previously set via {@link #setImage(InputStream)},
     * or null if no image has been set.
     *
     * @return the image data as a byte array, or null
     */
    public byte[] getImage() {
        return imageData;
    }

    /**
     * Returns the icon name for this stamp annotation (/Name entry).
     *
     * @return the stamp name (e.g. "Approved", "Draft", "Confidential"), default "Draft"
     */
    public String getIcon() {
        String name = dict.getNameAsString("Name");
        return name != null ? name : "Draft";
    }

    /**
     * Sets the icon name for this stamp annotation (/Name entry).
     *
     * @param icon the stamp name (e.g. "Approved", "Draft", "Confidential", "Final", "Expired")
     */
    public void setIcon(String icon) {
        if (icon != null) {
            dict.set(COSName.of("Name"), COSName.of(icon));
        }
    }
}
