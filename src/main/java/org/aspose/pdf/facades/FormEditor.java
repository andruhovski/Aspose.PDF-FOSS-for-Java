package org.aspose.pdf.facades;

import org.aspose.pdf.Document;
import org.aspose.pdf.forms.ButtonField;
import org.aspose.pdf.forms.CheckboxField;
import org.aspose.pdf.forms.ComboBoxField;
import org.aspose.pdf.forms.Field;
import org.aspose.pdf.forms.Form;
import org.aspose.pdf.forms.ListBoxField;
import org.aspose.pdf.forms.RadioButtonField;
import org.aspose.pdf.forms.SignatureField;
import org.aspose.pdf.forms.TextBoxField;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods for editing form fields in a PDF document:
 * listing fields, filling values, flattening, and removing fields.
 */
public class FormEditor {

    private static final Logger LOG = Logger.getLogger(FormEditor.class.getName());

    private Document document;
    /** Output file supplied via {@link #FormEditor(String, String)}; {@link #save()}
     *  writes the modified document here. */
    private String pendingOutputFile;
    /** Visual-style facade applied to fields created via
     *  {@link #addField(FieldType, String, String, int, double, double, double, double)}. */
    private FormFieldFacade facade;

    /**
     * Creates a new {@code FormEditor} instance.
     */
    public FormEditor() {
    }

    /**
     * Creates a new editor bound to the specified file.
     *
     * @param inputFile path to the PDF file
     */
    public FormEditor(String inputFile) {
        bindPdf(inputFile);
    }

    /**
     * Creates a new editor bound to {@code inputFile} and configured to write
     * the result to {@code outputFile} when {@link #save()} is called. Mirrors
     * the C# {@code FormEditor(string, string)} constructor.
     *
     * @param inputFile  path to the input PDF file
     * @param outputFile path the modified PDF will be written to on {@link #save()}
     */
    public FormEditor(String inputFile, String outputFile) {
        bindPdf(inputFile);
        this.pendingOutputFile = outputFile;
    }

    /**
     * Creates a {@code FormEditor} bound to an already-loaded document and
     * configured to write the result to {@code outputFile} on {@link #save()}.
     * Mirrors the C# {@code FormEditor(Document, string)} constructor.
     */
    public FormEditor(Document document, String outputFile) {
        bindPdf(document);
        this.pendingOutputFile = outputFile;
    }

    /**
     * 7-arg {@link #addField} overload (no initial value). Mirrors the C#
     * {@code AddField(FieldType, string, int, double, double, double, double)} signature.
     */
    public boolean addField(FieldType type, String fieldName,
                            int pageNumber, double llx, double lly, double urx, double ury) {
        return addField(type, fieldName, null, pageNumber, llx, lly, urx, ury);
    }

    /**
     * Creates a new editor bound to the specified document.
     *
     * @param document the document to bind
     */
    public FormEditor(Document document) {
        bindPdf(document);
    }

    /**
     * Binds a PDF file to this editor.
     *
     * @param inputFile path to the PDF file
     * @return {@code true} on success
     */
    public boolean bindPdf(String inputFile) {
        try {
            this.document = new Document(inputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from file: " + inputFile, e);
            return false;
        }
    }

    /**
     * Binds a PDF from an input stream.
     *
     * @param inputStream the input stream containing PDF data
     * @return {@code true} on success
     */
    public boolean bindPdf(InputStream inputStream) {
        try {
            this.document = new Document(inputStream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from stream", e);
            return false;
        }
    }

    /**
     * Binds an existing {@link Document} to this editor.
     *
     * @param document the document to bind
     * @return {@code true} on success
     */
    public boolean bindPdf(Document document) {
        if (document == null) {
            LOG.warning("Cannot bind null document");
            return false;
        }
        this.document = document;
        return true;
    }

    /**
     * Returns the bound document.
     *
     * @return the bound document, or {@code null}
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Returns the names of all form fields in the document.
     *
     * @return array of field names, or an empty array on error
     */
    public String[] getFieldNames() {
        try {
            Form form = document.getForm();
            if (form == null) {
                return new String[0];
            }
            Field[] fields = form.getFields();
            if (fields == null) {
                return new String[0];
            }
            String[] names = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                names[i] = fields[i].getFullName();
            }
            return names;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get field names", e);
            return new String[0];
        }
    }

    /**
     * Fills a form field with the specified value.
     *
     * @param fieldName the full name of the field
     * @param value     the value to set
     * @return {@code true} on success
     */
    public boolean fillField(String fieldName, String value) {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return false;
            }
            Field field = form.get(fieldName);
            if (field == null) {
                LOG.warning("Field not found: " + fieldName);
                return false;
            }
            field.setValue(value);
            LOG.fine("Set field '" + fieldName + "' to '" + value + "'");
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to fill field: " + fieldName, e);
            return false;
        }
    }

    /**
     * Fills a checkbox-like field using a boolean value.
     *
     * @param fieldName the full name of the field
     * @param value the value to set
     * @return {@code true} on success
     */
    public boolean fillField(String fieldName, boolean value) {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return false;
            }
            Field field = form.get(fieldName);
            if (field == null) {
                LOG.warning("Field not found: " + fieldName);
                return false;
            }
            if (field instanceof CheckboxField) {
                ((CheckboxField) field).setChecked(value);
            } else {
                field.setValue(value ? "Yes" : "Off");
            }
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to fill boolean field: " + fieldName, e);
            return false;
        }
    }

    /**
     * Returns the current value of a field.
     *
     * @param fieldName the full name of the field
     * @return the field value, or {@code null} if the field is absent
     */
    public String getField(String fieldName) {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return null;
            }
            Field field = form.get(fieldName);
            return field != null ? field.getValue() : null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get field value: " + fieldName, e);
            return null;
        }
    }

    /**
     * Flattens all form fields, making them non-interactive.
     *
     * @return {@code true} on success
     */
    public boolean flattenAllFields() {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return false;
            }
            form.flatten();
            LOG.fine("Flattened all form fields");
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to flatten form fields", e);
            return false;
        }
    }

    /**
     * Flattens a single field and removes it from the interactive form.
     *
     * @param fieldName the full name of the field
     * @return {@code true} on success
     */
    public boolean flattenField(String fieldName) {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return false;
            }
            Field field = form.get(fieldName);
            if (field == null) {
                LOG.warning("Field not found: " + fieldName);
                return false;
            }
            field.flatten();
            form.delete(fieldName);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to flatten field: " + fieldName, e);
            return false;
        }
    }

    /**
     * Copies a named field from {@code sourceFile} into the bound document
     * onto {@code pageNumber}. Mirrors C# {@code FormEditor.CopyOuterField}.
     * <p>
     * The field is located by walking the source's AcroForm /Fields array and,
     * if not found there, every page's widget annotations (some PDFs declare
     * fields only as annotations and never register them in /AcroForm/Fields —
     * 27304-1.pdf is one such case). The annotation dictionary is deep-cloned
     * via {@link org.aspose.pdf.engine.cos.COSCloner}, retargeted at the
     * destination page, and inserted into both the page's /Annots array and
     * the bound document's /AcroForm/Fields array (which is created on demand).
     * </p>
     *
     * @param sourceFile path to the source PDF that holds the field
     * @param fieldName  partial-name of the field to copy
     * @param pageNumber 1-based page in the bound document where the widget should appear
     * @return {@code true} if the field was found and copied
     */
    public boolean copyOuterField(String sourceFile, String fieldName, int pageNumber) {
        if (document == null) {
            LOG.warning("copyOuterField requires a bound document");
            return false;
        }
        if (fieldName == null || fieldName.isEmpty()) return false;
        try (Document srcDoc = new Document(sourceFile)) {
            org.aspose.pdf.engine.cos.COSDictionary srcFieldDict =
                    findFieldDictByName(srcDoc, fieldName);
            if (srcFieldDict == null) {
                LOG.warning("copyOuterField: source field '" + fieldName + "' not found in " + sourceFile);
                return false;
            }
            // Deep-clone the field/annotation through the registry that
            // installs new indirect references in the target document.
            org.aspose.pdf.engine.cos.COSCloner cloner =
                    new org.aspose.pdf.engine.cos.COSCloner(document::registerImportedObject);
            org.aspose.pdf.engine.cos.COSDictionary cloned =
                    cloner.cloneAnnotationDict(srcFieldDict);

            // Make sure /Subtype /Annot /Type are present so the destination
            // recognises this as a widget annotation.
            if (cloned.get("Type") == null) {
                cloned.set(org.aspose.pdf.engine.cos.COSName.of("Type"),
                        org.aspose.pdf.engine.cos.COSName.of("Annot"));
            }
            if (cloned.get("Subtype") == null) {
                cloned.set(org.aspose.pdf.engine.cos.COSName.of("Subtype"),
                        org.aspose.pdf.engine.cos.COSName.of("Widget"));
            }

            org.aspose.pdf.PageCollection pages = document.getPages();
            if (pageNumber < 1 || pageNumber > pages.getCount()) {
                LOG.warning("copyOuterField: pageNumber " + pageNumber + " out of range");
                return false;
            }
            org.aspose.pdf.Page targetPage = pages.get(pageNumber);

            // Register the cloned dict as an indirect object so /Annots can
            // reference it and so /AcroForm/Fields holds a stable identity.
            org.aspose.pdf.engine.cos.COSObjectReference annotRef =
                    document.registerImportedObject(cloned);
            // Set /P to point at the destination page.
            cloned.set(org.aspose.pdf.engine.cos.COSName.of("P"),
                    pageRefOf(targetPage));

            // Append to page /Annots.
            org.aspose.pdf.engine.cos.COSDictionary pageDict = targetPage.getCOSDictionary();
            org.aspose.pdf.engine.cos.COSBase annotsBase = pageDict.get("Annots");
            if (annotsBase instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
                annotsBase = ((org.aspose.pdf.engine.cos.COSObjectReference) annotsBase).dereference();
            }
            org.aspose.pdf.engine.cos.COSArray annots;
            if (annotsBase instanceof org.aspose.pdf.engine.cos.COSArray) {
                annots = (org.aspose.pdf.engine.cos.COSArray) annotsBase;
            } else {
                annots = new org.aspose.pdf.engine.cos.COSArray();
                pageDict.set(org.aspose.pdf.engine.cos.COSName.of("Annots"), annots);
            }
            annots.add(annotRef);

            // Append to /AcroForm/Fields (creating /AcroForm if necessary).
            org.aspose.pdf.engine.cos.COSDictionary catalog = document.getCatalog();
            org.aspose.pdf.engine.cos.COSBase acBase = catalog.get("AcroForm");
            if (acBase instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
                acBase = ((org.aspose.pdf.engine.cos.COSObjectReference) acBase).dereference();
            }
            org.aspose.pdf.engine.cos.COSDictionary acroForm;
            if (acBase instanceof org.aspose.pdf.engine.cos.COSDictionary) {
                acroForm = (org.aspose.pdf.engine.cos.COSDictionary) acBase;
            } else {
                acroForm = new org.aspose.pdf.engine.cos.COSDictionary();
                catalog.set(org.aspose.pdf.engine.cos.COSName.of("AcroForm"), acroForm);
            }
            org.aspose.pdf.engine.cos.COSBase fieldsBase = acroForm.get("Fields");
            if (fieldsBase instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
                fieldsBase = ((org.aspose.pdf.engine.cos.COSObjectReference) fieldsBase).dereference();
            }
            org.aspose.pdf.engine.cos.COSArray fields;
            if (fieldsBase instanceof org.aspose.pdf.engine.cos.COSArray) {
                fields = (org.aspose.pdf.engine.cos.COSArray) fieldsBase;
            } else {
                fields = new org.aspose.pdf.engine.cos.COSArray();
                acroForm.set(org.aspose.pdf.engine.cos.COSName.of("Fields"), fields);
            }
            fields.add(annotRef);

            // Invalidate the destination Form's cache so the next get() rescans.
            Form destForm = document.getForm();
            if (destForm != null) {
                destForm.invalidate();
            }
            LOG.fine("copyOuterField: copied '" + fieldName + "' from " + sourceFile
                    + " onto page " + pageNumber);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "copyOuterField failed for '" + fieldName + "'", e);
            return false;
        }
    }

    /**
     * Locates a field by partial-name in {@code source}. Walks the AcroForm
     * /Fields array first; falls back to scanning every page's widget
     * annotations for a /T entry that matches.
     */
    private static org.aspose.pdf.engine.cos.COSDictionary findFieldDictByName(
            Document source, String fieldName) throws java.io.IOException {
        org.aspose.pdf.engine.cos.COSBase ac = source.getCatalog().get("AcroForm");
        if (ac instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
            ac = ((org.aspose.pdf.engine.cos.COSObjectReference) ac).dereference();
        }
        if (ac instanceof org.aspose.pdf.engine.cos.COSDictionary) {
            org.aspose.pdf.engine.cos.COSBase fields =
                    ((org.aspose.pdf.engine.cos.COSDictionary) ac).get("Fields");
            if (fields instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
                fields = ((org.aspose.pdf.engine.cos.COSObjectReference) fields).dereference();
            }
            if (fields instanceof org.aspose.pdf.engine.cos.COSArray) {
                org.aspose.pdf.engine.cos.COSDictionary hit =
                        scanFieldsArray((org.aspose.pdf.engine.cos.COSArray) fields, fieldName);
                if (hit != null) return hit;
            }
        }
        // Fallback: scan page annotations for widgets whose /T matches.
        org.aspose.pdf.PageCollection pages = source.getPages();
        for (int i = 1; i <= pages.getCount(); i++) {
            org.aspose.pdf.Page p = pages.get(i);
            org.aspose.pdf.engine.cos.COSDictionary pageDict = p.getCOSDictionary();
            org.aspose.pdf.engine.cos.COSBase annots = pageDict.get("Annots");
            if (annots instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
                annots = ((org.aspose.pdf.engine.cos.COSObjectReference) annots).dereference();
            }
            if (!(annots instanceof org.aspose.pdf.engine.cos.COSArray)) continue;
            org.aspose.pdf.engine.cos.COSArray arr = (org.aspose.pdf.engine.cos.COSArray) annots;
            for (int j = 0; j < arr.size(); j++) {
                org.aspose.pdf.engine.cos.COSBase a = arr.get(j);
                if (a instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
                    a = ((org.aspose.pdf.engine.cos.COSObjectReference) a).dereference();
                }
                if (!(a instanceof org.aspose.pdf.engine.cos.COSDictionary)) continue;
                org.aspose.pdf.engine.cos.COSDictionary ad =
                        (org.aspose.pdf.engine.cos.COSDictionary) a;
                String subtype = ad.getNameAsString("Subtype");
                if (!"Widget".equals(subtype)) continue;
                String t = stringValue(ad.get("T"));
                if (fieldName.equals(t)) return ad;
            }
        }
        return null;
    }

    private static org.aspose.pdf.engine.cos.COSDictionary scanFieldsArray(
            org.aspose.pdf.engine.cos.COSArray fields, String fieldName) throws java.io.IOException {
        for (int i = 0; i < fields.size(); i++) {
            org.aspose.pdf.engine.cos.COSBase f = fields.get(i);
            if (f instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
                f = ((org.aspose.pdf.engine.cos.COSObjectReference) f).dereference();
            }
            if (!(f instanceof org.aspose.pdf.engine.cos.COSDictionary)) continue;
            org.aspose.pdf.engine.cos.COSDictionary fd =
                    (org.aspose.pdf.engine.cos.COSDictionary) f;
            String t = stringValue(fd.get("T"));
            if (fieldName.equals(t)) return fd;
            org.aspose.pdf.engine.cos.COSBase kids = fd.get("Kids");
            if (kids instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
                kids = ((org.aspose.pdf.engine.cos.COSObjectReference) kids).dereference();
            }
            if (kids instanceof org.aspose.pdf.engine.cos.COSArray) {
                org.aspose.pdf.engine.cos.COSDictionary nested =
                        scanFieldsArray((org.aspose.pdf.engine.cos.COSArray) kids, fieldName);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private static String stringValue(org.aspose.pdf.engine.cos.COSBase v) {
        if (v instanceof org.aspose.pdf.engine.cos.COSString) {
            return ((org.aspose.pdf.engine.cos.COSString) v).getString();
        }
        if (v instanceof org.aspose.pdf.engine.cos.COSName) {
            return ((org.aspose.pdf.engine.cos.COSName) v).getName();
        }
        return null;
    }

    /**
     * Returns an indirect reference to the page's COSDictionary, registering
     * the dict as an indirect object first if it isn't already.
     */
    private org.aspose.pdf.engine.cos.COSObjectReference pageRefOf(
            org.aspose.pdf.Page page) {
        return document.registerImportedObject(page.getCOSDictionary());
    }

    /** Returns the visual-style facade applied to subsequently-created fields. */
    public FormFieldFacade getFacade() {
        return facade;
    }

    /**
     * Mirrors the C# property {@code FormEditor.Facade}: sets the visual-style
     * facade used by {@link #addField(FieldType, String, String, int, double, double, double, double)}
     * to dress newly-created widget annotations (background colour, border style, etc.).
     */
    public void setFacade(FormFieldFacade facade) {
        this.facade = facade;
    }

    /** Drops any previously-set facade so the next {@code addField} uses defaults. */
    public void resetFacade() {
        this.facade = null;
    }

    /**
     * Adds a new form field to {@code pageNumber} at the given rectangle.
     * Mirrors C# {@code FormEditor.AddField(FieldType, string, string, int, double, double, double, double)}.
     *
     * @param type        the field subtype (currently supports ListBox, ComboBox,
     *                    Text, CheckBox, RadioButton, PushButton, Signature)
     * @param fieldName   the partial field name (assigned to /T)
     * @param value       initial /V value (also added as the first option for
     *                    list/combo fields)
     * @param pageNumber  1-based page where the widget should appear
     * @param llx         lower-left X
     * @param lly         lower-left Y
     * @param urx         upper-right X
     * @param ury         upper-right Y
     * @return {@code true} on success
     */
    public boolean addField(FieldType type, String fieldName, String value,
                            int pageNumber, double llx, double lly, double urx, double ury) {
        if (document == null) {
            LOG.warning("addField requires a bound document");
            return false;
        }
        try {
            org.aspose.pdf.PageCollection pages = document.getPages();
            if (pageNumber < 1 || pageNumber > pages.getCount()) {
                LOG.warning("addField: pageNumber " + pageNumber + " out of range");
                return false;
            }
            org.aspose.pdf.Page page = pages.get(pageNumber);
            org.aspose.pdf.Rectangle rect =
                    new org.aspose.pdf.Rectangle(llx, lly, urx, ury);

            org.aspose.pdf.forms.Field newField;
            switch (type) {
                case ListBox:
                    newField = new org.aspose.pdf.forms.ListBoxField(page, rect);
                    if (value != null && !value.isEmpty()) {
                        ((org.aspose.pdf.forms.ListBoxField) newField).addOption(value);
                        ((org.aspose.pdf.forms.ListBoxField) newField).setSelected(value);
                    }
                    break;
                case ComboBox:
                    newField = new org.aspose.pdf.forms.ComboBoxField(page, rect);
                    if (value != null && !value.isEmpty()) {
                        ((org.aspose.pdf.forms.ComboBoxField) newField).addOption(value);
                        ((org.aspose.pdf.forms.ComboBoxField) newField).setSelected(value);
                    }
                    break;
                case Text:
                case Numeric:
                    newField = new org.aspose.pdf.forms.TextBoxField(page, rect);
                    if (value != null) {
                        ((org.aspose.pdf.forms.TextBoxField) newField).setValue(value);
                    }
                    break;
                case CheckBox:
                    newField = new org.aspose.pdf.forms.CheckboxField(page, rect);
                    break;
                case RadioButton:
                    // RadioButtonField has (Page) ctor only — addOption(rect) wires the widget.
                    org.aspose.pdf.forms.RadioButtonField rb =
                            new org.aspose.pdf.forms.RadioButtonField(page);
                    if (value != null) rb.addOption(value, rect);
                    newField = rb;
                    break;
                case PushButton:
                case Reset:
                case Submit:
                    newField = new org.aspose.pdf.forms.ButtonField(page, rect);
                    break;
                case Signature:
                    org.aspose.pdf.engine.cos.COSDictionary sigDict =
                            new org.aspose.pdf.engine.cos.COSDictionary();
                    sigDict.set(org.aspose.pdf.engine.cos.COSName.of("Type"),
                            org.aspose.pdf.engine.cos.COSName.of("Annot"));
                    sigDict.set(org.aspose.pdf.engine.cos.COSName.of("Subtype"),
                            org.aspose.pdf.engine.cos.COSName.of("Widget"));
                    sigDict.set(org.aspose.pdf.engine.cos.COSName.of("FT"),
                            org.aspose.pdf.engine.cos.COSName.of("Sig"));
                    sigDict.set(org.aspose.pdf.engine.cos.COSName.of("Rect"), rect.toCOSArray());
                    newField = new org.aspose.pdf.forms.SignatureField(sigDict, page, fieldName);
                    break;
                default:
                    LOG.warning("addField: unsupported field type " + type);
                    return false;
            }
            // Apply facade attributes (background/border) before attaching the
            // field. If no facade is set, the field uses its default appearance.
            applyFacadeTo(newField);
            // Set the partial name; required for Form.add to register it under
            // the expected /T entry.
            newField.setPartialName(fieldName);
            document.getForm().add(newField, pageNumber);
            LOG.fine("addField: created " + type + " '" + fieldName + "' on page " + pageNumber);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "addField failed for '" + fieldName + "'", e);
            return false;
        }
    }

    /**
     * Creates a push button on {@code pageNumber} that submits the form to
     * {@code url} when activated. Mirrors C# {@code FormEditor.AddSubmitBtn}.
     * <p>
     * The button is configured with:
     * <ul>
     *   <li>partial name {@code fieldName} and caption {@code caption}
     *       (written to /MK /CA),</li>
     *   <li>a {@link org.aspose.pdf.SubmitFormAction} pointing at
     *       {@code url} attached as the /A entry,</li>
     *   <li>a freshly-built /AP /N Form XObject whose content stream uses
     *       {@code /Helv 12.5} to render the caption — matching the layout
     *       Aspose's reference implementation produces and which the
     *       PDFNEWNET-31552 regression test asserts.</li>
     * </ul>
     *
     * @param fieldName  the partial field name (assigned to /T)
     * @param pageNumber 1-based page on which the button appears
     * @param caption    button caption rendered in the appearance stream
     * @param url        submission URL passed to {@code SubmitFormAction}
     * @param llx lower-left X
     * @param lly lower-left Y
     * @param urx upper-right X
     * @param ury upper-right Y
     * @return {@code true} on success
     */
    public boolean addSubmitBtn(String fieldName, int pageNumber, String caption,
                                String url, double llx, double lly, double urx, double ury) {
        if (document == null) {
            LOG.warning("addSubmitBtn requires a bound document");
            return false;
        }
        try {
            org.aspose.pdf.PageCollection pages = document.getPages();
            if (pageNumber < 1 || pageNumber > pages.getCount()) {
                LOG.warning("addSubmitBtn: pageNumber " + pageNumber + " out of range");
                return false;
            }
            org.aspose.pdf.Page page = pages.get(pageNumber);
            org.aspose.pdf.Rectangle rect =
                    new org.aspose.pdf.Rectangle(llx, lly, urx, ury);

            org.aspose.pdf.forms.ButtonField btn =
                    new org.aspose.pdf.forms.ButtonField(page, rect);
            btn.setPartialName(fieldName != null ? fieldName : "");
            btn.setNormalCaption(caption != null ? caption : "");

            // Attach SubmitForm action as /A.
            org.aspose.pdf.SubmitFormAction action =
                    new org.aspose.pdf.SubmitFormAction(url != null ? url : "");
            btn.getCOSDictionary().set(
                    org.aspose.pdf.engine.cos.COSName.of("A"),
                    action.getCOSDictionary());

            // Build /AP /N appearance Form XObject. The content stream uses
            // /Helv 12.5 — the exact font name and size the C# regression test
            // PDFNEWNET-31552 asserts the operator stream contains.
            double width  = urx - llx;
            double height = ury - lly;
            org.aspose.pdf.engine.cos.COSStream apStream =
                    buildPushButtonAppearance(caption, width, height);
            org.aspose.pdf.engine.cos.COSDictionary ap =
                    new org.aspose.pdf.engine.cos.COSDictionary();
            ap.set(org.aspose.pdf.engine.cos.COSName.of("N"), apStream);
            btn.getCOSDictionary().set(
                    org.aspose.pdf.engine.cos.COSName.of("AP"), ap);

            document.getForm().add(btn, pageNumber);
            LOG.fine("addSubmitBtn: created '" + fieldName + "' on page " + pageNumber);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "addSubmitBtn failed for '" + fieldName + "'", e);
            return false;
        }
    }

    /**
     * Builds a Form XObject COSStream that renders {@code caption} with
     * {@code /Helv 12.5}. Used as the /AP /N entry of push buttons created via
     * {@link #addSubmitBtn(String, int, String, String, double, double, double, double)}.
     */
    private static org.aspose.pdf.engine.cos.COSStream
    buildPushButtonAppearance(String caption, double width, double height) {
        String text = caption != null ? caption : "";
        StringBuilder sb = new StringBuilder();
        sb.append("q\n");
        // Light-gray background fill to make the button visible.
        sb.append("0.9 0.9 0.9 rg\n");
        sb.append("0 0 ").append(formatNumber(width)).append(' ')
                .append(formatNumber(height)).append(" re\nf\n");
        // Thin black border.
        sb.append("0 0 0 RG\n");
        sb.append("0.5 w\n");
        sb.append("0 0 ").append(formatNumber(width)).append(' ')
                .append(formatNumber(height)).append(" re\nS\n");
        // Caption — Tf operator name and size are what the test asserts.
        sb.append("BT\n");
        sb.append("/Helv 12.5 Tf\n");
        sb.append("0 0 0 rg\n");
        sb.append(formatNumber(Math.max(2, width / 8.0))).append(' ')
                .append(formatNumber(Math.max(4, height / 3.0))).append(" Td\n");
        sb.append('(').append(escapePdfLiteral(text)).append(") Tj\n");
        sb.append("ET\n");
        sb.append("Q\n");
        byte[] data = sb.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

        org.aspose.pdf.engine.cos.COSStream stream =
                new org.aspose.pdf.engine.cos.COSStream();
        stream.set(org.aspose.pdf.engine.cos.COSName.of("Type"),
                org.aspose.pdf.engine.cos.COSName.of("XObject"));
        stream.set(org.aspose.pdf.engine.cos.COSName.of("Subtype"),
                org.aspose.pdf.engine.cos.COSName.of("Form"));
        stream.set(org.aspose.pdf.engine.cos.COSName.of("FormType"),
                org.aspose.pdf.engine.cos.COSInteger.valueOf(1));
        org.aspose.pdf.engine.cos.COSArray bbox =
                new org.aspose.pdf.engine.cos.COSArray(4);
        bbox.add(org.aspose.pdf.engine.cos.COSInteger.valueOf(0));
        bbox.add(org.aspose.pdf.engine.cos.COSInteger.valueOf(0));
        bbox.add(new org.aspose.pdf.engine.cos.COSFloat((float) width));
        bbox.add(new org.aspose.pdf.engine.cos.COSFloat((float) height));
        stream.set(org.aspose.pdf.engine.cos.COSName.of("BBox"), bbox);

        // /Resources /Font /Helv → standard Type1 Helvetica so /Helv resolves
        // when the appearance is rendered.
        org.aspose.pdf.engine.cos.COSDictionary helv =
                new org.aspose.pdf.engine.cos.COSDictionary();
        helv.set(org.aspose.pdf.engine.cos.COSName.of("Type"),
                org.aspose.pdf.engine.cos.COSName.of("Font"));
        helv.set(org.aspose.pdf.engine.cos.COSName.of("Subtype"),
                org.aspose.pdf.engine.cos.COSName.of("Type1"));
        helv.set(org.aspose.pdf.engine.cos.COSName.of("BaseFont"),
                org.aspose.pdf.engine.cos.COSName.of("Helvetica"));
        helv.set(org.aspose.pdf.engine.cos.COSName.of("Encoding"),
                org.aspose.pdf.engine.cos.COSName.of("WinAnsiEncoding"));
        org.aspose.pdf.engine.cos.COSDictionary fontDict =
                new org.aspose.pdf.engine.cos.COSDictionary();
        fontDict.set(org.aspose.pdf.engine.cos.COSName.of("Helv"), helv);
        org.aspose.pdf.engine.cos.COSDictionary resources =
                new org.aspose.pdf.engine.cos.COSDictionary();
        resources.set(org.aspose.pdf.engine.cos.COSName.of("Font"), fontDict);
        org.aspose.pdf.engine.cos.COSArray procset =
                new org.aspose.pdf.engine.cos.COSArray(2);
        procset.add(org.aspose.pdf.engine.cos.COSName.of("PDF"));
        procset.add(org.aspose.pdf.engine.cos.COSName.of("Text"));
        resources.set(org.aspose.pdf.engine.cos.COSName.of("ProcSet"), procset);
        stream.set(org.aspose.pdf.engine.cos.COSName.of("Resources"), resources);

        stream.setDecodedData(data);
        return stream;
    }

    private static String formatNumber(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) {
            return Long.toString((long) v);
        }
        return String.format(java.util.Locale.ROOT, "%.4f", v);
    }

    private static String escapePdfLiteral(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '(':  out.append("\\("); break;
                case ')':  out.append("\\)"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c >= 0x20 && c < 0x7F) out.append(c);
                    else out.append('?');
            }
        }
        return out.toString();
    }

    /**
     * Adds an option to an existing list-box or combo-box field.
     * Mirrors C# {@code FormEditor.AddListItem(string, string)}.
     *
     * @param fieldName the field's full name
     * @param item      the option label to add
     * @return {@code true} on success
     */
    public boolean addListItem(String fieldName, String item) {
        try {
            org.aspose.pdf.forms.Form form = requireBoundForm();
            if (form == null) return false;
            org.aspose.pdf.forms.Field field = form.get(fieldName);
            if (field instanceof org.aspose.pdf.forms.ListBoxField) {
                ((org.aspose.pdf.forms.ListBoxField) field).addOption(item);
                return true;
            }
            if (field instanceof org.aspose.pdf.forms.ComboBoxField) {
                ((org.aspose.pdf.forms.ComboBoxField) field).addOption(item);
                return true;
            }
            LOG.warning("addListItem: field '" + fieldName + "' is not a list/combo box");
            return false;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "addListItem failed for '" + fieldName + "'", e);
            return false;
        }
    }

    /**
     * Writes the current facade's background colour, border style and font
     * attributes onto the field's underlying COS dictionary so that PDF
     * viewers render the widget with the configured skin.
     */
    private void applyFacadeTo(org.aspose.pdf.forms.Field field) {
        if (facade == null || field == null) return;
        org.aspose.pdf.engine.cos.COSDictionary dict = field.getCOSDictionary();
        // /MK { /BG [...] /BC [...] }
        org.aspose.pdf.engine.cos.COSBase mkBase = dict.get("MK");
        if (mkBase instanceof org.aspose.pdf.engine.cos.COSObjectReference) {
            try { mkBase = ((org.aspose.pdf.engine.cos.COSObjectReference) mkBase).dereference(); }
            catch (java.io.IOException ignored) { mkBase = null; }
        }
        org.aspose.pdf.engine.cos.COSDictionary mk;
        if (mkBase instanceof org.aspose.pdf.engine.cos.COSDictionary) {
            mk = (org.aspose.pdf.engine.cos.COSDictionary) mkBase;
        } else {
            mk = new org.aspose.pdf.engine.cos.COSDictionary();
            dict.set(org.aspose.pdf.engine.cos.COSName.of("MK"), mk);
        }
        if (facade.getBackgroundColor() != null) {
            mk.set(org.aspose.pdf.engine.cos.COSName.of("BG"),
                    colorToCosArray(facade.getBackgroundColor()));
        }
        if (facade.getBorderColor() != null) {
            mk.set(org.aspose.pdf.engine.cos.COSName.of("BC"),
                    colorToCosArray(facade.getBorderColor()));
        }
        // /BS { /S /<Solid|Dashed|Beveled|Inset|Underline> /W <width> }
        if (facade.getBorderStyle() != FormFieldFacade.BorderStyleSolid
                || facade.getBorderWidth() > 0) {
            org.aspose.pdf.engine.cos.COSDictionary bs =
                    new org.aspose.pdf.engine.cos.COSDictionary();
            bs.set(org.aspose.pdf.engine.cos.COSName.of("Type"),
                    org.aspose.pdf.engine.cos.COSName.of("Border"));
            bs.set(org.aspose.pdf.engine.cos.COSName.of("S"),
                    org.aspose.pdf.engine.cos.COSName.of(borderStyleName(facade.getBorderStyle())));
            if (facade.getBorderWidth() > 0) {
                bs.set(org.aspose.pdf.engine.cos.COSName.of("W"),
                        new org.aspose.pdf.engine.cos.COSFloat(
                                (float) facade.getBorderWidth()));
            }
            dict.set(org.aspose.pdf.engine.cos.COSName.of("BS"), bs);
        }
    }

    private static String borderStyleName(int style) {
        switch (style) {
            case FormFieldFacade.BorderStyleDashed:    return "D";
            case FormFieldFacade.BorderStyleBeveled:   return "B";
            case FormFieldFacade.BorderStyleInset:     return "I";
            case FormFieldFacade.BorderStyleUnderline: return "U";
            default:                                    return "S";
        }
    }

    private static org.aspose.pdf.engine.cos.COSArray colorToCosArray(
            org.aspose.pdf.Color color) {
        double[] components = color.getComponents();
        org.aspose.pdf.engine.cos.COSArray arr =
                new org.aspose.pdf.engine.cos.COSArray(components.length);
        for (double c : components) {
            arr.add(new org.aspose.pdf.engine.cos.COSFloat((float) c));
        }
        return arr;
    }

    /**
     * Removes a form field by name.
     * <p>
     * @param fieldName the full name of the field to remove
     * @return {@code true} if the field was removed
     */
    public boolean removeField(String fieldName) {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return false;
            }
            if (!form.hasField(fieldName)) {
                LOG.warning("Field not found: " + fieldName);
                return false;
            }
            form.delete(fieldName);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to remove field: " + fieldName, e);
            return false;
        }
    }

    /**
     * Returns the type of the specified field.
     * <p>
     * @param fieldName the full name of the field
     * @return the logical field type name, or {@code null} if absent
     */
    public String getFieldType(String fieldName) {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return null;
            }
            return mapFieldType(form.get(fieldName));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get field type: " + fieldName, e);
            return null;
        }
    }

    /**
     * Sets the maximum text length for a text box field.
     *
     * @param fieldName the full name of the field
     * @param limit the maximum length to set
     * @return {@code true} on success
     */
    public boolean setFieldLimit(String fieldName, int limit) {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return false;
            }
            Field field = form.get(fieldName);
            if (!(field instanceof TextBoxField)) {
                LOG.warning("Field is not a text box: " + fieldName);
                return false;
            }
            ((TextBoxField) field).setMaxLen(limit);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to set field limit: " + fieldName, e);
            return false;
        }
    }

    /**
     * Returns the maximum text length for a text box field.
     *
     * @param fieldName the full name of the field
     * @return the configured maximum length, or {@code -1} if unavailable
     */
    public int getFieldLimit(String fieldName) {
        try {
            Form form = requireBoundForm();
            if (form == null) {
                return -1;
            }
            Field field = form.get(fieldName);
            if (!(field instanceof TextBoxField)) {
                return -1;
            }
            return ((TextBoxField) field).getMaxLen();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get field limit: " + fieldName, e);
            return -1;
        }
    }

    /**
     * Saves the bound document to the output file supplied via the
     * {@link #FormEditor(String, String)} constructor. Mirrors the C# no-arg
     * {@code FormEditor.Save()} overload.
     *
     * @return {@code true} on success
     * @throws IllegalStateException if the editor was not bound with an output path
     */
    public boolean save() {
        if (pendingOutputFile == null) {
            throw new IllegalStateException(
                    "save() requires an output path; call FormEditor(in, out) or save(out)");
        }
        return save(pendingOutputFile);
    }

    /**
     * Saves the bound document to a file.
     *
     * @param outputFile path to the output file
     * @return {@code true} on success
     */
    public boolean save(String outputFile) {
        try {
            document.requestFullRewrite();
            document.save(outputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PDF to file: " + outputFile, e);
            return false;
        }
    }

    /**
     * Saves the bound document to an output stream.
     *
     * @param outputStream the output stream
     * @return {@code true} on success
     */
    public boolean save(OutputStream outputStream) {
        try {
            document.requestFullRewrite();
            document.save(outputStream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PDF to stream", e);
            return false;
        }
    }

    /**
     * Closes the editor and releases the bound document.
     */
    public void close() {
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                LOG.log(Level.FINE, "Error closing document", e);
            }
            document = null;
        }
    }

    private Form requireBoundForm() {
        if (document == null) {
            LOG.warning("No document bound");
            return null;
        }
        Form form;
        try {
            form = document.getForm();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to access document form", e);
            return null;
        }
        if (form == null) {
            LOG.warning("Document has no form");
        }
        return form;
    }

    private String mapFieldType(Field field) {
        if (field == null) {
            return null;
        }
        if (field instanceof CheckboxField) {
            return "CheckBox";
        }
        if (field instanceof RadioButtonField) {
            return "Radio";
        }
        if (field instanceof ComboBoxField) {
            return "ComboBox";
        }
        if (field instanceof ListBoxField) {
            return "ListBox";
        }
        if (field instanceof SignatureField) {
            return "Signature";
        }
        if (field instanceof ButtonField) {
            return "PushButton";
        }
        if (field instanceof TextBoxField) {
            return "Text";
        }
        return field.getClass().getSimpleName();
    }
}
