package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.xfa.binding.FormDom;
import org.aspose.pdf.engine.xfa.binding.FormField;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.forms.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;
import java.util.logging.Logger;

/// Maps a merged XFA [FormDom] into ordinary AcroForm fields on a
/// [Document], so generic PDF viewers (which cannot render XFA) display the
/// form and its data.
///
/// **Naming note:** this internal class keeps the historical `XfaFlattener`
/// /`flatten` names, but at the public API it is exposed as a _conversion_
/// — `XfaForm.convertToAcroForm(...)` produces **editable** AcroForm fields
/// (equivalent to `Form.setType(FormType.Standard)`). It is NOT an Aspose
/// "flatten" (which burns field appearances into the page content and removes the
/// fields — `Form.flatten()`). The internal name is retained because the
/// `Result` type and `flatten` entry point are referenced across the
/// flatten package and the layout track; renaming them would ripple widely for no
/// behavioural gain.
///
/// This is the Stage-A deliverable: correct field set, types and bound values,
/// placed at their statically-resolvable geometry. It does NOT perform XFA dynamic
/// layout or static rendering (Stage C); fields whose geometry is flowed (no
/// `x`/`y`) are still created — so their bound value is carried — but at
/// a flagged placeholder position, recorded in [Result#geometryFallback].
public final class XfaFlattener {

    private static final Logger LOG = Logger.getLogger(XfaFlattener.class.getName());

    /// Policy for the AcroForm's `/XFA` entry after flattening.
    public enum XfaPolicy {
        /// Remove `/XFA` — pure AcroForm; generic viewers render it (default).
        DROP,
        /// Keep `/XFA` — hybrid document (XFA-aware viewers still use XFA).
        KEEP
    }

    /// Outcome of a flatten operation (the A5.4 acceptance numbers).
    public static final class Result {
        /// AcroForm fields created.
        public int fieldsAdded;
        /// Fields carrying a non-empty bound value (the headline number).
        public int boundValuesCarried;
        /// Fields placed with statically-resolved on-page geometry.
        public int geometryResolved;
        /// Fields placed at a flagged placeholder position (flowed/dynamic — Stage C).
        public int geometryFallback;
        /// Fields clamped because their resolved rect fell off the page.
        public int offPageClamped;
        /// contentAreas found in the template pageSet (1 = static; >1 deferred to C4 pagination).
        public int contentAreaCount;
        /// Field count by AcroForm field class.
        public final Map<String, Integer> byType = new LinkedHashMap<>();
        /// Nodes not mapped to a field, with the reason.
        public final List<String> unmapped = new ArrayList<>();

        void type(String t) {
            byType.merge(t, 1, Integer::sum);
        }
    }

    private XfaFlattener() {
    }

    /// Flattens the Form DOM onto the document's AcroForm.
    ///
    /// @param doc    the target document (fields are added to `doc.getForm()`)
    /// @param dom    the merged Form DOM
    /// @param policy the `/XFA` handling policy
    /// @param acroForm the AcroForm dictionary (for the `/XFA` policy), or `null`
    /// @return the flatten result
    /// @throws Exception on document access failure
    public static Result flatten(Document doc, FormDom dom, XfaPolicy policy,
                                 org.aspose.pdf.engine.pdfobjects.PdfDictionary acroForm) throws Exception {
        return flatten(doc, dom, null, policy, acroForm);
    }

    /// Flattens the Form DOM onto the document's AcroForm, using the template's
    /// `pageSet` contentArea as the positioned-layout origin (C1).
    ///
    /// @param doc      the target document
    /// @param dom      the merged Form DOM
    /// @param tpl      the XFA template (for the contentArea layout origin), or `null`
    /// @param policy   the `/XFA` handling policy
    /// @param acroForm the AcroForm dictionary (for the `/XFA` policy), or `null`
    /// @return the flatten result
    /// @throws Exception on document access failure
    public static Result flatten(Document doc, FormDom dom,
                                 org.aspose.pdf.engine.xfa.model.template.Template tpl,
                                 XfaPolicy policy,
                                 org.aspose.pdf.engine.pdfobjects.PdfDictionary acroForm) throws Exception {
        Result r = new Result();
        Form form = doc.getForm();
        if (doc.getPages().getCount() == 0) {
            doc.getPages().add(); // XFA-only documents may have no page box yet
        }
        Page page = doc.getPages().get(1);
        // Normalize: some XFA-source PDFs carry an inverted MediaBox (lly/ury swapped),
        // so getHeight()/getWidth() can be negative — that would collapse every Y-flipped
        // rect to zero on clamp. Use the absolute extents for layout.
        double pageH = Math.abs(page.getRect().getHeight());
        double pageW = Math.abs(page.getRect().getWidth());

        // C1: positioned-layout origin = the first contentArea of the template pageSet
        // (single-area static case). Multiple contentAreas mean pagination chooses the
        // area per fragment — deferred to C4; here we use the first and flag the count.
        double[] base = contentAreaOrigin(tpl, r);

        // Map each Form DOM field element to its FormField (binding result).
        Map<Element, FormField> byElement = new IdentityHashMap<>();
        for (FormField f : dom.getFields()) {
            if (f.getFormNode() != null) {
                byElement.put(f.getFormNode().getElement(), f);
            }
        }

        Set<Element> handled = new HashSet<>();
        Set<String> usedNames = new HashSet<>();
        PlaceCursor cursor = new PlaceCursor(pageW, pageH);

        // 1) exclusion groups -> radio button fields (one group, N options).
        for (Element ex : exclGroups(dom.getRoot())) {
            buildRadioGroup(ex, byElement, handled, usedNames, form, page, pageH, base[0], base[1], cursor, r);
        }

        // 2) leaf fields. exclGroup values are now surfaced as FormFields (Stage-A
        // A4-EXCL), but the radio group is built from the exclGroup DOM element in step 1
        // above; skip the exclGroup's own FormField here so it is not also mapped as a
        // scalar field. (A5 can later use FormField.getSelectedItemIndex() to mark the
        // chosen option — not done here.)
        for (FormField f : dom.getFields()) {
            XfaNode node = f.getFormNode();
            if (node == null || handled.contains(node.getElement()) || "exclGroup".equals(f.getUiType())) {
                continue;
            }
            mapField(f, form, page, pageH, base[0], base[1], cursor, usedNames, r);
        }

        // 3) make values display in generic viewers.
        try {
            form.setNeedAppearances(true);
        } catch (RuntimeException ignore) {
            // setNeedAppearances best-effort; field setValue() also regenerates appearances
        }

        // 4) /XFA policy.
        if (policy == XfaPolicy.DROP && acroForm != null) {
            acroForm.set("XFA", null); // null value removes the key (ISO 32000 §7.3.7)
        }
        return r;
    }

    /* --------------------------- field mapping --------------------------- */

    private static void mapField(FormField f, Form form, Page page, double pageH,
                                 double baseX, double baseY,
                                 PlaceCursor cursor, Set<String> usedNames, Result r) {
        Rectangle rect = geometry(f.getFormNode(), page, pageH, baseX, baseY, cursor, r);
        placeField(f, form, page, rect, usedNames, r);
    }

    /// Creates the AcroForm widget for a field at an explicit `rect` on `page` (the
    /// type mapping + value + caption→tooltip), reused by both the Stage-A grid flattener and the
    /// layout-driven [XfaAcroFormConverter] (which supplies real laid-out rects).
    static Field placeField(FormField f, Form form, Page page, Rectangle rect,
                           Set<String> usedNames, Result r) {
        String ui = f.getUiType();
        String value = f.getValue();
        // The value SHOWN in the widget is the display form (dropdown save code → its paired label, and
        // the numeric <format><picture> applied), matching the render track — so a converted dropdown
        // shows "Krajský soud v Praze" / "0,00", not the raw "KSSTCAB" / "0".
        org.w3c.dom.Element fe = f.getFormNode() != null ? f.getFormNode().getElement() : null;
        String shown = fe != null
                ? org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.displayValue(fe, value) : value;
        String name = uniqueName(f.getSomPath(), f.getName(), usedNames);
        // The widget's /DA carries the field's AUTHORED font size (its <font size>), so the generated
        // appearance renders the value at the same size as the rendered XFA. Without this the default
        // "/Helv 0 Tf" auto-sizes the text to the box height — visibly larger than the source form.
        String authoredDa = fe != null ? authoredDa(fe) : null;
        int authoredQ = fe != null
                ? org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.valueQuadding(fe) : 0;

        try {
            Field field;
            if ("checkButton".equals(ui)) {
                CheckboxField cb = new CheckboxField(page, rect);
                // checked = value equals the field's AUTHORED on-value (first <items> entry) —
                // matching the render track; a bare isTruthy misses on-values like "2"/"03"/"Y".
                cb.setChecked(fe != null
                        ? org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.isCheckedValue(fe, value)
                        : isTruthy(value));
                field = cb;
                r.type("CheckboxField");
            } else if ("choiceList".equals(ui)) {
                boolean listBox = isListBox(f.getFormNode());
                if (listBox) {
                    ListBoxField lb = new ListBoxField(page, rect);
                    if (authoredDa != null) {
                        lb.setDefaultAppearance(authoredDa);
                    }
                    lb.setQuadding(authoredQ);
                    for (String it : f.getItems()) {
                        lb.addOption(it);
                    }
                    if (shown != null && !shown.isEmpty()) {
                        lb.setSelected(shown);
                    }
                    field = lb;
                    r.type("ListBoxField");
                } else {
                    ComboBoxField combo = new ComboBoxField(page, rect);
                    if (authoredDa != null) {
                        combo.setDefaultAppearance(authoredDa);
                    }
                    combo.setQuadding(authoredQ);
                    for (String it : f.getItems()) {
                        combo.addOption(it);
                    }
                    if (shown != null && !shown.isEmpty()) {
                        combo.setSelected(shown);
                    }
                    field = combo;
                    r.type("ComboBoxField");
                }
            } else if ("button".equals(ui) || "imageEdit".equals(ui)) {
                ButtonField b = new ButtonField(page, rect);
                if (shown != null && !shown.isEmpty()) {
                    b.setNormalCaption(shown);
                }
                field = b;
                r.type("ButtonField");
            } else if ("signature".equals(ui)) {
                field = new SignatureField(page, rect);
                r.type("SignatureField");
            } else {
                // textEdit / numericEdit / dateTimeEdit / passwordEdit / barcode / default
                TextBoxField tb = new TextBoxField(page, rect);
                if (authoredDa != null) {
                    tb.setDefaultAppearance(authoredDa);
                }
                tb.setQuadding(authoredQ);
                if (shown != null) {
                    tb.setValue(shown);
                }
                field = tb;
                r.type("TextBoxField");
            }
            field.setPartialName(name);
            String caption = captionOf(f.getFormNode());
            if (caption != null) {
                field.setAlternateName(caption); // XFA caption -> AcroForm tooltip (/TU)
            }
            form.add(field);
            r.fieldsAdded++;
            if (value != null && !value.isEmpty()) {
                r.boundValuesCarried++;
            }
            return field;
        } catch (RuntimeException e) {
            r.unmapped.add(f.getSomPath() + " (" + ui + "): " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    /// Builds a `/DA` default-appearance string from a field's authored XFA `<font>` — the
    /// value font size + colour the render track uses — so the editable widget's generated appearance
    /// matches the rendered XFA (rather than auto-sizing to the box). Font name stays `/Helv`
    /// (resolved via the AcroForm `/DR`); Helvetica ≈ the common XFA Arial visually.
    private static String authoredDa(Element fe) {
        double size = org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.valueFontSize(fe);
        float[] c = org.aspose.pdf.engine.xfa.flatten.paint.XfaPainter.valueFontColor(fe);
        java.util.Locale us = java.util.Locale.US;
        String color = (c[0] == c[1] && c[1] == c[2])
                ? String.format(us, "%.3f g", c[0])
                : String.format(us, "%.3f %.3f %.3f rg", c[0], c[1], c[2]);
        return String.format(us, "/Helv %.2f Tf %s", size, color);
    }

    private static void buildRadioGroup(Element ex, Map<Element, FormField> byElement, Set<Element> handled,
                                        Set<String> usedNames, Form form, Page page, double pageH,
                                        double baseX, double baseY,
                                        PlaceCursor cursor, Result r) {
        List<Element> opts = childFields(ex);
        if (opts.isEmpty()) {
            return;
        }
        List<Rectangle> rects = new ArrayList<>();
        for (Element opt : opts) {
            handled.add(opt);
            rects.add(geometry(org.aspose.pdf.engine.xfa.model.XfaNodeFactory.wrap(opt, null),
                    page, pageH, baseX, baseY, cursor, r));
        }
        placeRadio(ex, opts, rects, byElement.get(ex), byElement, usedNames, form, page, r);
    }

    /// Creates a radio-button group from an exclGroup's option `rects` on `page`, reused
    /// by the Stage-A flattener and the layout-driven converter. Marks the chosen option from the
    /// surfaced exclGroup selection.
    static void placeRadio(Element ex, List<Element> opts, List<Rectangle> rects, FormField groupField,
                           Map<Element, FormField> byElement, Set<String> usedNames, Form form,
                           Page page, Result r) {
        if (opts.isEmpty()) {
            return;
        }
        String somPath = groupField != null && groupField.getSomPath() != null
                ? groupField.getSomPath() : somPathOf(ex, byElement);
        String groupName = uniqueName(somPath, nameAttr(ex), usedNames);
        try {
            RadioButtonField radio = new RadioButtonField(page);
            for (int i = 0; i < opts.size(); i++) {
                Element opt = opts.get(i);
                RadioButtonOptionField o = new RadioButtonOptionField(page, rects.get(i));
                String name = opt.getAttribute("name");
                o.setOptionName(name != null && !name.isEmpty() ? name : "Opt" + i);
                radio.add(o);
            }
            radio.setPartialName(groupName);
            String caption = captionOf(groupField != null ? groupField.getFormNode() : null);
            if (caption != null) {
                radio.setAlternateName(caption);
            }
            form.add(radio);
            r.fieldsAdded++;
            r.type("RadioButtonField");
            // mark the chosen option from the now-surfaced exclGroup selection (A4-EXCL)
            if (groupField != null) {
                int sel = groupField.getSelectedItemIndex();
                if (sel >= 0 && sel < opts.size()) {
                    try {
                        radio.setSelected(sel);
                    } catch (RuntimeException ignore) {
                        // setSelected best-effort
                    }
                }
            }
        } catch (RuntimeException e) {
            r.unmapped.add(groupName + " (exclGroup): " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /* ----------------------------- geometry ------------------------------ */

    private static Rectangle geometry(XfaNode node, Page page, double pageH,
                                      double baseX, double baseY, PlaceCursor cursor, Result r) {
        Rectangle rect = node == null ? null : XfaGeometry.resolve(node, pageH, baseX, baseY);
        if (rect == null || rect.getWidth() <= 0 || rect.getHeight() <= 0) {
            r.geometryFallback++;
            return cursor.next(); // placeholder — flagged; carries the value, not the position
        }
        // clamp on-page (no off-page / negative); normalize width for inverted MediaBoxes
        double pageW = Math.abs(page.getRect().getWidth());
        double llx = clamp(rect.getLLX(), 0, pageW);
        double urx = clamp(rect.getURX(), 0, pageW);
        double lly = clamp(rect.getLLY(), 0, pageH);
        double ury = clamp(rect.getURY(), 0, pageH);
        boolean clamped = llx != rect.getLLX() || urx != rect.getURX()
                || lly != rect.getLLY() || ury != rect.getURY();
        if (urx - llx <= 0 || ury - lly <= 0) {
            r.geometryFallback++;
            return cursor.next();
        }
        if (clamped) {
            r.offPageClamped++;
        }
        r.geometryResolved++;
        return new Rectangle(llx, lly, urx, ury);
    }

    /// Lays placeholder boxes in columns down the page for flowed/unresolved fields.
    private static final class PlaceCursor {
        private final double pageW;
        private final double pageH;
        private double x = 36;
        private double y;

        PlaceCursor(double pageW, double pageH) {
            this.pageW = pageW;
            this.pageH = pageH;
            this.y = pageH - 36;
        }

        Rectangle next() {
            if (y < 36) {
                y = pageH - 36;
                x += 130;
                if (x + 120 > pageW) {
                    x = 36; // overflow — overlap rather than go off-page
                }
            }
            Rectangle r = new Rectangle(x, y - 12, x + 120, y);
            y -= 16;
            return r;
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    /// The positioned-layout origin `{x,y}` (points, from the page top-left) = the
    /// first `contentArea` of the template `pageSet`, or `{0,0}` if the
    /// template has none. Records the total contentArea count on the result (1 = static
    /// single-area; >1 means pagination selects the area per fragment — C4).
    private static double[] contentAreaOrigin(org.aspose.pdf.engine.xfa.model.template.Template tpl, Result r) {
        if (tpl == null) {
            return new double[]{0, 0};
        }
        List<org.aspose.pdf.engine.xfa.model.template.ContentArea> areas = new ArrayList<>();
        collectContentAreas(tpl, areas);
        r.contentAreaCount = areas.size();
        if (areas.isEmpty()) {
            return new double[]{0, 0};
        }
        org.aspose.pdf.engine.xfa.model.template.ContentArea ca = areas.get(0);
        return new double[]{XfaGeometry.toPoints(ca.getX()), XfaGeometry.toPoints(ca.getY())};
    }

    /// Collects contentAreas by descending the layout subtree. The pageSet may be nested
    /// inside the root subform (XFA permits this), so descend containers too — not only
    /// pageSet/pageArea (a pageSet-only walk misses it, the 408975 P3 defect).
    private static void collectContentAreas(XfaNode node,
            List<org.aspose.pdf.engine.xfa.model.template.ContentArea> out) {
        for (XfaNode child : node.getChildren()) {
            if (child instanceof org.aspose.pdf.engine.xfa.model.template.ContentArea) {
                out.add((org.aspose.pdf.engine.xfa.model.template.ContentArea) child);
            } else {
                String ln = child.getElementName();
                if ("pageSet".equals(ln) || "pageArea".equals(ln)
                        || "subform".equals(ln) || "subformSet".equals(ln) || "area".equals(ln)) {
                    collectContentAreas(child, out);
                }
            }
        }
    }

    /* ----------------------------- helpers ------------------------------- */

    /// The XFA field/exclGroup caption text (the on-form label), or `null`.
    private static String captionOf(XfaNode formNode) {
        if (formNode == null) {
            return null;
        }
        XfaNode cap = formNode.getChild("caption");
        if (cap == null) {
            return null;
        }
        String t = cap.getTextContent();
        return (t == null || t.trim().isEmpty()) ? null : t.trim();
    }

    private static boolean isTruthy(String v) {
        if (v == null) {
            return false;
        }
        String s = v.trim();
        return s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on")
                || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("checked");
    }

    private static boolean isListBox(XfaNode fieldNode) {
        // choiceList ui with open="multiSelect" or a listbox hint -> list box; else combo
        XfaNode ui = fieldNode == null ? null : fieldNode.getChild("ui");
        if (ui != null) {
            XfaNode cl = ui.getChild("choiceList");
            if (cl != null) {
                String open = cl.getAttribute("open");
                // "userControl"/"onEntry" => editable combo; "multiSelect" => list box
                return "multiSelect".equals(open);
            }
        }
        return false;
    }

    private static List<Element> exclGroups(XfaNode root) {
        List<Element> out = new ArrayList<>();
        if (root != null) {
            collectExclGroups(root.getElement(), out);
        }
        return out;
    }

    private static void collectExclGroups(Element el, List<Element> out) {
        Node c = el.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                Element ce = (Element) c;
                if ("exclGroup".equals(ce.getLocalName()) || "exclGroup".equals(ce.getNodeName())) {
                    out.add(ce);
                } else {
                    collectExclGroups(ce, out);
                }
            }
            c = c.getNextSibling();
        }
    }

    private static List<Element> childFields(Element ex) {
        List<Element> out = new ArrayList<>();
        Node c = ex.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE
                    && ("field".equals(c.getLocalName()) || "field".equals(c.getNodeName()))) {
                out.add((Element) c);
            }
            c = c.getNextSibling();
        }
        return out;
    }

    private static String nameAttr(Element e) {
        String n = e.getAttribute("name");
        return n == null || n.isEmpty() ? null : n;
    }

    private static String somPathOf(Element ex, Map<Element, FormField> byElement) {
        // exclGroups are not FormFields; derive a name from the first child's path parent, else the name attr
        for (Element child : childFields(ex)) {
            FormField ff = byElement.get(child);
            if (ff != null && ff.getSomPath() != null && ff.getSomPath().contains(".")) {
                return ff.getSomPath().substring(0, ff.getSomPath().lastIndexOf('.'));
            }
        }
        return nameAttr(ex);
    }

    static String uniqueName(String somPath, String fallback, Set<String> used) {
        String base = somPath != null && !somPath.isEmpty() ? somPath
                : (fallback != null && !fallback.isEmpty() ? fallback : "field");
        base = base.replace('[', '_').replace(']', '_');
        String name = base;
        int n = 1;
        while (!used.add(name)) {
            name = base + "#" + (n++);
        }
        return name;
    }
}
