package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;

/**
 * Typed XFA template element <code>ui</code>. Attribute and child
 * accessors are typed; unknown attributes/children round-trip via the DOM.
 */
public final class Ui extends XfaNode {

    /** Wraps a backing <code>ui</code> element. */
    public Ui(Element element, XfaNode parent) {
        super(element, parent);
    }

    /** @return the typed <code>id</code> attribute, or null. */
    public String getId() { return getString("id"); }
    /** Sets the <code>id</code> attribute. */
    public void setId(String value) { setAttribute("id", value); }

    /** @return the typed <code>use</code> attribute, or null. */
    public String getUse() { return getString("use"); }
    /** Sets the <code>use</code> attribute. */
    public void setUse(String value) { setAttribute("use", value); }

    /** @return the typed <code>usehref</code> attribute, or null. */
    public String getUsehref() { return getString("usehref"); }
    /** Sets the <code>usehref</code> attribute. */
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /** @return the <code>extras</code> child (typed), or null. */
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /** Ensures and returns the <code>extras</code> child. */
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /** @return the <code>picture</code> child (typed), or null. */
    public Picture getPicture() { return (Picture) getChild("picture"); }
    /** Ensures and returns the <code>picture</code> child. */
    public Picture ensurePicture() { return (Picture) ensureChild("picture"); }

    /** @return the <code>barcode</code> child (typed), or null. */
    public Barcode getBarcode() { return (Barcode) getChild("barcode"); }
    /** Ensures and returns the <code>barcode</code> child. */
    public Barcode ensureBarcode() { return (Barcode) ensureChild("barcode"); }

    /** @return the <code>button</code> child (typed), or null. */
    public Button getButton() { return (Button) getChild("button"); }
    /** Ensures and returns the <code>button</code> child. */
    public Button ensureButton() { return (Button) ensureChild("button"); }

    /** @return the <code>checkButton</code> child (typed), or null. */
    public CheckButton getCheckButton() { return (CheckButton) getChild("checkButton"); }
    /** Ensures and returns the <code>checkButton</code> child. */
    public CheckButton ensureCheckButton() { return (CheckButton) ensureChild("checkButton"); }

    /** @return the <code>choiceList</code> child (typed), or null. */
    public ChoiceList getChoiceList() { return (ChoiceList) getChild("choiceList"); }
    /** Ensures and returns the <code>choiceList</code> child. */
    public ChoiceList ensureChoiceList() { return (ChoiceList) ensureChild("choiceList"); }

    /** @return the <code>dateTimeEdit</code> child (typed), or null. */
    public DateTimeEdit getDateTimeEdit() { return (DateTimeEdit) getChild("dateTimeEdit"); }
    /** Ensures and returns the <code>dateTimeEdit</code> child. */
    public DateTimeEdit ensureDateTimeEdit() { return (DateTimeEdit) ensureChild("dateTimeEdit"); }

    /** @return the <code>defaultUi</code> child (typed), or null. */
    public DefaultUi getDefaultUi() { return (DefaultUi) getChild("defaultUi"); }
    /** Ensures and returns the <code>defaultUi</code> child. */
    public DefaultUi ensureDefaultUi() { return (DefaultUi) ensureChild("defaultUi"); }

    /** @return the <code>imageEdit</code> child (typed), or null. */
    public ImageEdit getImageEdit() { return (ImageEdit) getChild("imageEdit"); }
    /** Ensures and returns the <code>imageEdit</code> child. */
    public ImageEdit ensureImageEdit() { return (ImageEdit) ensureChild("imageEdit"); }

    /** @return the <code>numericEdit</code> child (typed), or null. */
    public NumericEdit getNumericEdit() { return (NumericEdit) getChild("numericEdit"); }
    /** Ensures and returns the <code>numericEdit</code> child. */
    public NumericEdit ensureNumericEdit() { return (NumericEdit) ensureChild("numericEdit"); }

    /** @return the <code>passwordEdit</code> child (typed), or null. */
    public PasswordEdit getPasswordEdit() { return (PasswordEdit) getChild("passwordEdit"); }
    /** Ensures and returns the <code>passwordEdit</code> child. */
    public PasswordEdit ensurePasswordEdit() { return (PasswordEdit) ensureChild("passwordEdit"); }

    /** @return the <code>signature</code> child (typed), or null. */
    public Signature getSignature() { return (Signature) getChild("signature"); }
    /** Ensures and returns the <code>signature</code> child. */
    public Signature ensureSignature() { return (Signature) ensureChild("signature"); }

    /** @return the <code>textEdit</code> child (typed), or null. */
    public TextEdit getTextEdit() { return (TextEdit) getChild("textEdit"); }
    /** Ensures and returns the <code>textEdit</code> child. */
    public TextEdit ensureTextEdit() { return (TextEdit) ensureChild("textEdit"); }
}
