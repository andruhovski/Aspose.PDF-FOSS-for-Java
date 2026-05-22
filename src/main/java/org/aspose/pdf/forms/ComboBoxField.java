package org.aspose.pdf.forms;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * Combo box / dropdown field (/FT /Ch, combo flag) (ISO 32000-1:2008, §12.7.4.4).
 * <p>
 * A combo box allows the user to select a single value from a list of options,
 * and optionally type a custom value if the editable flag is set.
 * </p>
 */
public class ComboBoxField extends Field {

    /**
     * Constructs a combo box field from an existing COS dictionary.
     *
     * @param dict     the COS dictionary backing this field
     * @param page     the page this field belongs to (may be null)
     * @param fullName the fully-qualified dotted name
     */
    public ComboBoxField(COSDictionary dict, Page page, String fullName) {
        super(dict, page, fullName);
    }

    /**
     * Constructs a new empty combo box field.
     */
    public ComboBoxField() {
        super(new COSDictionary(), null, "");
        dict.set(COSName.of("Type"), COSName.of("Annot"));
        dict.set(COSName.of("Subtype"), COSName.of("Widget"));
        dict.set(COSName.of("FT"), COSName.of("Ch"));
        dict.set(COSName.of("Ff"), COSInteger.valueOf(1 << 17));
    }

    /**
     * Constructs a new combo box field associated with the given document.
     *
     * @param doc the document this field belongs to
     */
    public ComboBoxField(Document doc) {
        this();
    }

    /**
     * Constructs a new combo box field on the given page with the specified rectangle.
     *
     * @param page the page
     * @param rect the field rectangle
     */
    public ComboBoxField(Page page, Rectangle rect) {
        super(new COSDictionary(), page, "");
        dict.set(COSName.of("Type"), COSName.of("Annot"));
        dict.set(COSName.of("Subtype"), COSName.of("Widget"));
        dict.set(COSName.of("FT"), COSName.of("Ch"));
        // Set combo flag (bit 18)
        dict.set(COSName.of("Ff"), COSInteger.valueOf(1 << 17));
        setRect(rect);
    }

    /**
     * Returns the options (/Opt array) for this combo box.
     *
     * @return the option collection (never null)
     */
    public OptionCollection getOptions() {
        COSBase opt = dict.get("Opt");
        if (opt instanceof COSObjectReference) {
            try {
                opt = ((COSObjectReference) opt).dereference();
            } catch (Exception ignored) {
                opt = null;
            }
        }
        return new OptionCollection(opt instanceof COSArray ? (COSArray) opt : new COSArray());
    }

    /**
     * Returns the currently selected value.
     *
     * @return the selected value, or null
     */
    public String getSelected() {
        return getValue();
    }

    /**
     * Sets the selected value by string.
     *
     * @param value the value to select
     */
    public void setSelected(String value) {
        setValue(value);
    }

    /**
     * Sets the selected option by 1-based index.
     *
     * @param index 1-based index of the option to select
     */
    public void setSelected(int index) {
        OptionCollection opts = getOptions();
        if (index >= 1 && index <= opts.size()) {
            setValue(opts.get(index - 1).getValue());
        }
    }

    /**
     * Adds an option to this combo box (/Opt array).
     *
     * @param value the option value to add
     */
    public void addOption(String value) {
        COSBase opt = dict.get("Opt");
        COSArray arr;
        if (opt instanceof COSArray) {
            arr = (COSArray) opt;
        } else {
            arr = new COSArray();
            dict.set(COSName.of("Opt"), arr);
        }
        arr.add(new COSString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    /**
     * Returns whether this combo box is editable (/Ff bit 19).
     *
     * @return true if the user can type a custom value
     */
    public boolean isEditable() {
        return (getFieldFlags() & (1 << 18)) != 0;
    }
}
