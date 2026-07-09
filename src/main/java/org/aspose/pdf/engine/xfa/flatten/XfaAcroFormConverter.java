package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.layout.ContentStreamBuilder;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.binding.FormField;
import org.aspose.pdf.engine.xfa.flatten.layout.XfaBookends;
import org.aspose.pdf.engine.xfa.flatten.layout.XfaFlowLayout;
import org.aspose.pdf.engine.xfa.flatten.layout.XfaLayoutNode;
import org.aspose.pdf.engine.xfa.flatten.layout.XfaPageSplitter;
import org.aspose.pdf.engine.xfa.flatten.layout.XfaPaginator;
import org.aspose.pdf.engine.xfa.flatten.paint.XfaFontResolver;
import org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.aspose.pdf.forms.Form;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts an XFA form to a properly rendered, editable AcroForm document — the public
 * {@code XfaForm.convertToAcroForm(...)} path (Aspose {@code Form.Type=FormType.Standard}).
 *
 * <p>Unlike the Stage-A {@link XfaFlattener} (which only added fields at statically-resolvable
 * geometry, placing flowed fields in a placeholder grid and painting no static content), this
 * converter drives the Stage-C layout/render track ({@link XfaPaginator}/{@link XfaFlowLayout}/
 * {@link XfaPainter}) so the output is what a generic viewer should show:</p>
 * <ul>
 *   <li>the XFA <b>static content</b> (captions, draws, boilerplate text, boxes/borders) is painted
 *       onto fresh pages — no longer dropped;</li>
 *   <li>interactive AcroForm widgets are placed at their <b>real laid-out positions</b> (from the
 *       paginated layout), not in a synthetic grid;</li>
 *   <li>the XFA <b>"Please wait…" placeholder page(s)</b> shipped in dynamic XFA PDFs are removed
 *       and replaced by the rendered pages.</li>
 * </ul>
 *
 * <p>For each placed field the box + caption are painted (static), while the bound <em>value</em> is
 * carried by the interactive widget (painted with {@code ff=null} so the value is not double-drawn).</p>
 */
public final class XfaAcroFormConverter {

    private XfaAcroFormConverter() {
    }

    /**
     * Converts {@code dom} into a rendered editable AcroForm on {@code doc}.
     *
     * @param doc      the target document (its existing placeholder pages are replaced)
     * @param dom      the merged Form DOM
     * @param tpl      the XFA template (medium + pageSet + layout), or {@code null}
     * @param policy   the {@code /XFA} handling policy
     * @param acroForm the AcroForm dictionary (for the {@code /XFA} policy), or {@code null}
     * @return the conversion result (field counts + carried values)
     * @throws Exception on document access failure
     */
    public static XfaFlattener.Result convert(Document doc, FormDom dom, Template tpl,
                                              XfaFlattener.XfaPolicy policy,
                                              org.aspose.pdf.engine.pdfobjects.PdfDictionary acroForm)
            throws Exception {
        XfaFontResolver resolver = XfaFontResolver.create(null);
        XfaFlattener.Result r = new XfaFlattener.Result();

        // 1) lay out + paginate (the same pipeline the render track uses). Convert mode keeps the
        // interactive +/- row buttons (relevant="-print") in the layout so they become clickable widgets.
        XfaFlowLayout.Result layout;
        XfaBookends.Spec bookends;
        XfaPageSplitter.SplitPlan plan;
        XfaPaginator.PaginatedLayout pag;
        XfaFlowLayout.setIncludeInteractive(true);
        try {
            layout = XfaFlowLayout.layout(dom, tpl);
            bookends = XfaBookends.overflow(dom, tpl, layout.regionWidth);
            double splitRegionH = layout.regionHeight - bookends.leaderHeight() - bookends.trailerHeight();
            if (splitRegionH < 1) {
                splitRegionH = layout.regionHeight;
            }
            plan = XfaPageSplitter.split(layout, splitRegionH);
            pag = XfaPaginator.paginate(layout, plan, tpl, bookends, dom);
        } finally {
            XfaFlowLayout.setIncludeInteractive(false);
        }
        r.contentAreaCount = pag.pageCount();

        Map<Element, FormField> byElement = new IdentityHashMap<>();
        for (FormField f : dom.getFields()) {
            if (f.getFormNode() != null) {
                byElement.put(f.getFormNode().getElement(), f);
            }
        }
        // Master-page furniture (header / footer / "Page N of M" / address blocks) — the same channel
        // the render track paints, so the converted AcroForm looks like the original (its first page is
        // the positioned header page, not blank; every page carries its footer).
        Map<Element, FormField> masterByElement = new IdentityHashMap<>();
        for (FormField f : dom.getMasterFields()) {
            if (f.getFormNode() != null) {
                masterByElement.put(f.getFormNode().getElement(), f);
            }
        }
        // Master fields are disjoint from the flow fields; merge them so placeWidget() resolves a
        // captured furniture field (header / conditions) to its FormField for the editable widget.
        byElement.putAll(masterByElement);

        // 2) replace the document's placeholder pages ("Please wait…") with fresh paginated pages.
        Form form = doc.getForm();
        while (doc.getPages().getCount() > 0) {
            doc.getPages().delete(1);
        }

        Set<String> usedNames = new HashSet<>();
        Set<Element> placed = new HashSet<>(); // field elements realised as widgets (for the fallback)
        // 3) paint each page (static content + field box/caption) and create the field widgets.
        for (int p = 0; p < pag.pageCount(); p++) {
            XfaPaginator.PageLayout pl = pag.pages.get(p);
            // Size each page by its own assigned pageArea's medium (a pageSet may mix landscape/portrait).
            double pageW = pl.region.mediumW > 0 ? pl.region.mediumW : pag.mediumW;
            double pageH = pl.region.mediumH > 0 ? pl.region.mediumH : pag.mediumH;
            Page page = doc.getPages().add();
            page.setPageSize(pageW, pageH);
            ContentStreamBuilder b = new ContentStreamBuilder();
            XfaPainter.Result pr = new XfaPainter.Result();
            Set<Element> handled = new HashSet<>();
            List<Place> widgets = new ArrayList<>();
            // Master-page furniture FIRST (background), matching the render track. Capture the positioned
            // furniture FIELDS' value rects so they become editable widgets too (header / conditions
            // fields like "Vendor Contact" / "Measure-Install Start Date" — otherwise painted as static).
            List<Object[]> masterFieldRects = new ArrayList<>();
            XfaPainter.beginFieldCapture((fel, frect) -> masterFieldRects.add(new Object[]{fel, frect}));
            try {
                XfaPaginator.paintPageFurniture(dom, masterByElement, pl, p + 1, pag.pageCount(),
                        pageW, pageH, b, pr, resolver);
            } finally {
                XfaPainter.endFieldCapture();
            }
            for (Object[] mr : masterFieldRects) {
                Element fel = (Element) mr[0];
                Rectangle frect = (Rectangle) mr[1];
                if (handled.contains(fel) || !masterByElement.containsKey(fel)
                        || frect.getWidth() <= 0 || frect.getHeight() <= 0) {
                    continue;
                }
                handled.add(fel);
                boolean invalid = XfaPainter.isMandatory(fel)
                        && isEmptyValue(masterByElement.get(fel));
                if (invalid) {
                    XfaPainter.paintInvalidBorder(frect, b, pr);
                }
                widgets.add(new Place(fel, frect, null, null, false, invalid));
            }
            for (XfaLayoutNode unit : pl.units) {
                walk(unit, byElement, pageH, pl.region.contentX, pl.region.contentY,
                        b, pr, resolver, handled, widgets);
            }
            XfaPainter.attach(page, b);
            // create the interactive widgets at their laid-out rectangles
            for (Place pw : widgets) {
                placeWidget(pw, form, page, byElement, usedNames, r);
                placed.add(pw.el);
                if (pw.opts != null) {
                    placed.addAll(pw.opts);
                }
            }
        }

        // 3b) data-preservation fallback — a visible BOUND field the flow layout did not place still
        // gets a widget carrying its value, so no data is lost (the laid-out fields keep their real
        // positions; only these stragglers fall back). Empty/hidden unplaced fields are dropped (they
        // were placeholder-grid noise). Stragglers go on a trailing page so they never overlap content.
        placeUnplacedBoundFields(doc, dom, form, byElement, placed, usedNames,
                pag.mediumW, pag.mediumH, r);

        // 4) make values display in generic viewers + apply the /XFA policy.
        try {
            form.setNeedAppearances(true);
        } catch (RuntimeException ignore) {
            // best-effort; setValue() also regenerates appearances
        }
        if (policy == XfaFlattener.XfaPolicy.DROP && acroForm != null) {
            acroForm.set("XFA", null);
        }
        // Convert is a wholesale restructure (every page replaced, /XFA dropped, all fields rebuilt).
        // Force a clean full rewrite on save: an incremental update over an xref-stream/object-stream
        // source leaves a stale /Prev chain referencing the removed pages/XFA, which strict viewers
        // (Acrobat/Chrome) report as "damaged, but can be repaired". A full rewrite emits a fresh,
        // self-contained xref.
        doc.requestFullRewrite();
        return r;
    }

    /** A field/exclGroup/button to realise as an interactive widget at a laid-out rect. */
    private static final class Place {
        final Element el;
        final Rectangle rect;
        final List<Element> opts;   // exclGroup option elements (null for a scalar field)
        final List<Rectangle> optRects;
        final boolean button;       // interactive push button (+/- instance control)
        final boolean invalid;      // mandatory-and-empty → red-highlighted widget
        Place(Element el, Rectangle rect, List<Element> opts, List<Rectangle> optRects) {
            this(el, rect, opts, optRects, false, false);
        }
        Place(Element el, Rectangle rect, List<Element> opts, List<Rectangle> optRects,
              boolean button, boolean invalid) {
            this.el = el;
            this.rect = rect;
            this.opts = opts;
            this.optRects = optRects;
            this.button = button;
            this.invalid = invalid;
        }
    }

    /**
     * Walks a placed layout node: paints static content (draws/captions/boxes) via the reused C2
     * painter, and records field/exclGroup nodes for widget creation. A field's box + caption are
     * painted ({@code ff=null}, so the bound value is left for the widget), then the widget carries
     * the value.
     */
    private static void walk(XfaLayoutNode node, Map<Element, FormField> byElement, double mediumH,
                             double contentX, double contentY, ContentStreamBuilder b, XfaPainter.Result r,
                             XfaFontResolver resolver, Set<Element> handled, List<Place> widgets) {
        Element el = node.getSource();
        if (el != null) {
            if (handled.contains(el)) {
                return;
            }
            // An interactive +/- button (relevant="-print", so isPaintableForPrint is false): paint its
            // raised box + caption and record it for a clickable widget. Handled BEFORE the print gate.
            if (XfaPainter.isInteractiveButton(el)) {
                handled.add(el);
                Rectangle brect = rectOf(node, mediumH, contentX, contentY);
                XfaPainter.paintButton(el, brect, b, r, resolver);
                if (brect.getWidth() > 0 && brect.getHeight() > 0) {
                    widgets.add(new Place(el, brect, null, null, true, false));
                }
                return;
            }
            if (!XfaPainter.isPaintableForPrint(el)) {
                return;
            }
            Rectangle rect = rectOf(node, mediumH, contentX, contentY);
            String ln = local(el);
            if ("exclGroup".equals(ln)) {
                handled.add(el);
                List<Element> opts = new ArrayList<>();
                List<Rectangle> optRects = new ArrayList<>();
                XfaPainter.paintBoxAndCaption(el, rect, b, r, resolver); // group chrome
                for (XfaLayoutNode c : node.getChildren()) {
                    Element ce = c.getSource();
                    if (ce != null && "field".equals(local(ce))) {
                        Rectangle orc = rectOf(c, mediumH, contentX, contentY);
                        opts.add(ce);
                        optRects.add(orc);
                        handled.add(ce);
                        // Paint the radio option visually (its circle marker + label caption) exactly as
                        // the render track does, so the "01 Fyzická ● 02 Právnická" selector row appears;
                        // the interactive radio widget is layered on top for editability.
                        XfaPainter.paintPlaced(ce, byElement.get(ce), byElement, orc, b, r, resolver);
                    }
                }
                if (!opts.isEmpty()) {
                    widgets.add(new Place(el, rect, opts, optRects));
                }
                return; // options consumed
            }
            if ("field".equals(ln)) {
                handled.add(el);
                // A barcode (QR) or embedded-image field has a GENERATED visual value a text widget
                // cannot carry — paint it fully (symbol/picture + box + caption) exactly as the render
                // track does, and place no editable widget. Its value is still preserved as a hidden
                // data field by the unplaced-bound-fields pass, so nothing is lost.
                if (XfaPainter.isGeneratedVisualField(el)) {
                    XfaPainter.paintPlaced(el, byElement.get(el), byElement, rect, b, r, resolver);
                    return;
                }
                XfaPainter.paintBoxAndCaption(el, rect, b, r, resolver); // box + caption, NOT the value
                if (rect.getWidth() > 0 && rect.getHeight() > 0 && byElement.containsKey(el)) {
                    Rectangle vr = XfaPainter.valueRect(el, rect, resolver);
                    // A mandatory field (validate nullTest=error) that is still empty is "incorrectly
                    // filled": highlight it with a red border (painted now + set on the widget), the way
                    // Adobe flags required fields in the interactive XFA form.
                    FormField ff = byElement.get(el);
                    boolean invalid = XfaPainter.isMandatory(el)
                            && (ff == null || ff.getValue() == null || ff.getValue().isEmpty());
                    if (invalid) {
                        XfaPainter.paintInvalidBorder(vr, b, r);
                    }
                    // Place the editable widget over the VALUE sub-rect (full box minus caption reserve)
                    // so its value sits beside the caption, not on top of it.
                    widgets.add(new Place(el, vr, null, null, false, invalid));
                }
                return;
            }
            // static node (draw / subform / area boilerplate): paint fully.
            XfaPainter.paintPlaced(el, byElement.get(el), rect, b, r, resolver);
        }
        for (XfaLayoutNode child : node.getChildren()) {
            walk(child, byElement, mediumH, contentX, contentY, b, r, resolver, handled, widgets);
        }
    }

    /**
     * Preserves the value of any bound field the flow layout did not place — as a <b>hidden</b>
     * widget on the last existing page, so the data is never lost yet the converted AcroForm renders
     * <em>page-for-page identical to the XFA render</em> (no spurious trailing page).
     *
     * <p>These stragglers are the fields the shared layout/paginate pipeline did not position — almost
     * always {@code presence}=hidden/invisible internal fields (form version, timestamps, record IDs).
     * The render track ({@link XfaPaginator#paintPaginatedContent}) likewise never shows them, so
     * hiding their widgets is exactly what keeps the converted AcroForm matching the rendered form,
     * while keeping the values programmatically accessible (and flatten-safe).</p>
     */
    private static void placeUnplacedBoundFields(Document doc, FormDom dom, Form form,
                                                 Map<Element, FormField> byElement, Set<Element> placed,
                                                 Set<String> usedNames, double mediumW, double mediumH,
                                                 XfaFlattener.Result r) throws Exception {
        List<FormField> leftover = new ArrayList<>();
        for (FormField f : dom.getFields()) {
            if (f.getFormNode() == null) {
                continue;
            }
            Element el = f.getFormNode().getElement();
            if (el == null || placed.contains(el) || "exclGroup".equals(f.getUiType())) {
                continue;
            }
            String v = f.getValue();
            if (v == null || v.isEmpty()) {
                continue; // carry every bound value the layout did not already place (incl. hidden)
            }
            leftover.add(f);
        }
        if (leftover.isEmpty()) {
            return;
        }
        // Host them on the last existing page (never add a page); off-content, zero-visible, Hidden.
        Page page;
        if (doc.getPages().getCount() > 0) {
            page = doc.getPages().get(doc.getPages().getCount());
        } else {
            page = doc.getPages().add();
            page.setPageSize(mediumW, mediumH);
        }
        org.aspose.pdf.Rectangle rect = new org.aspose.pdf.Rectangle(0, 0, 1, 1);
        for (FormField f : leftover) {
            org.aspose.pdf.forms.Field field = XfaFlattener.placeField(f, form, page, rect, usedNames, r);
            if (field != null) {
                // Hidden + NoView so generic viewers and our rasterizer skip it (ISO 32000-1 §12.5.3).
                field.setFlags(org.aspose.pdf.annotations.AnnotationFlags.Hidden,
                        org.aspose.pdf.annotations.AnnotationFlags.NoView);
            }
        }
    }

    private static void placeWidget(Place pw, Form form, Page page, Map<Element, FormField> byElement,
                                    Set<String> usedNames, XfaFlattener.Result r) {
        if (pw.button) {
            placeButton(pw.el, pw.rect, form, page, usedNames, r);
            return;
        }
        if (pw.opts != null) {
            XfaFlattener.placeRadio(pw.el, pw.opts, pw.optRects, byElement.get(pw.el), byElement,
                    usedNames, form, page, r);
            return;
        }
        FormField f = byElement.get(pw.el);
        if (f != null) {
            org.aspose.pdf.forms.Field field = XfaFlattener.placeField(f, form, page, pw.rect, usedNames, r);
            if (field != null && pw.invalid) {
                // Red border colour on the widget too (so a viewer redraws it red on focus/print),
                // matching the painted highlight. ISO 32000-1 §12.5.6.19 /MK /BC.
                field.getCharacteristics().setBorder(org.aspose.pdf.Color.fromRgb(0.8, 0.0, 0.0));
            }
        }
    }

    /**
     * Creates the clickable push-button widget for an interactive {@code +}/{@code -} control: a
     * {@link org.aspose.pdf.forms.ButtonField} carrying the caption and the field's original XFA click
     * script as a {@code /JavaScript} action. (Full dynamic add/remove still needs the XFA runtime; in a
     * flat AcroForm the button is wired and visible, and the script is preserved.)
     */
    private static void placeButton(Element el, Rectangle rect, Form form, Page page,
                                    Set<String> usedNames, XfaFlattener.Result r) {
        try {
            org.aspose.pdf.forms.ButtonField btn = new org.aspose.pdf.forms.ButtonField(page, rect);
            String caption = XfaPainter.captionTextOf(el);
            if (caption != null && !caption.isEmpty()) {
                btn.setNormalCaption(caption);
            }
            String name = XfaFlattener.uniqueName(null, localName(el), usedNames);
            btn.setPartialName(name);
            btn.setOnClickJavaScript(XfaPainter.clickScript(el));
            form.add(btn);
            r.fieldsAdded++;
            r.type("ButtonField");
        } catch (RuntimeException ignore) {
            // best effort: a button that fails to realise just isn't added (chrome is already painted)
        }
    }

    private static String localName(Element e) {
        return e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
    }

    private static boolean isEmptyValue(FormField f) {
        return f == null || f.getValue() == null || f.getValue().isEmpty();
    }

    /** page-local content coords → PDF user space (flip Y against the medium height). */
    private static Rectangle rectOf(XfaLayoutNode node, double mediumH, double contentX, double contentY) {
        double llx = contentX + node.getX();
        double topY = contentY + node.getY();
        double ury = mediumH - topY;
        double lly = ury - node.getHeight();
        double urx = llx + node.getWidth();
        return new Rectangle(llx, lly, urx, ury);
    }

    private static String local(Element e) {
        return e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
    }
}
