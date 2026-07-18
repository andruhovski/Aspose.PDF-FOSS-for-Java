package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `ui`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Ui extends XfaNode {

    /// Wraps a backing `ui` element.
    public Ui(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the typed `id` attribute, or null.
    public String getId() { return getString("id"); }
    /// Sets the `id` attribute.
    public void setId(String value) { setAttribute("id", value); }

    /// @return the typed `use` attribute, or null.
    public String getUse() { return getString("use"); }
    /// Sets the `use` attribute.
    public void setUse(String value) { setAttribute("use", value); }

    /// @return the typed `usehref` attribute, or null.
    public String getUsehref() { return getString("usehref"); }
    /// Sets the `usehref` attribute.
    public void setUsehref(String value) { setAttribute("usehref", value); }

    /// @return the `extras` child (typed), or null.
    public Extras getExtras() { return (Extras) getChild("extras"); }
    /// Ensures and returns the `extras` child.
    public Extras ensureExtras() { return (Extras) ensureChild("extras"); }

    /// @return the `picture` child (typed), or null.
    public Picture getPicture() { return (Picture) getChild("picture"); }
    /// Ensures and returns the `picture` child.
    public Picture ensurePicture() { return (Picture) ensureChild("picture"); }

    /// @return the `barcode` child (typed), or null.
    public Barcode getBarcode() { return (Barcode) getChild("barcode"); }
    /// Ensures and returns the `barcode` child.
    public Barcode ensureBarcode() { return (Barcode) ensureChild("barcode"); }

    /// @return the `button` child (typed), or null.
    public Button getButton() { return (Button) getChild("button"); }
    /// Ensures and returns the `button` child.
    public Button ensureButton() { return (Button) ensureChild("button"); }

    /// @return the `checkButton` child (typed), or null.
    public CheckButton getCheckButton() { return (CheckButton) getChild("checkButton"); }
    /// Ensures and returns the `checkButton` child.
    public CheckButton ensureCheckButton() { return (CheckButton) ensureChild("checkButton"); }

    /// @return the `choiceList` child (typed), or null.
    public ChoiceList getChoiceList() { return (ChoiceList) getChild("choiceList"); }
    /// Ensures and returns the `choiceList` child.
    public ChoiceList ensureChoiceList() { return (ChoiceList) ensureChild("choiceList"); }

    /// @return the `dateTimeEdit` child (typed), or null.
    public DateTimeEdit getDateTimeEdit() { return (DateTimeEdit) getChild("dateTimeEdit"); }
    /// Ensures and returns the `dateTimeEdit` child.
    public DateTimeEdit ensureDateTimeEdit() { return (DateTimeEdit) ensureChild("dateTimeEdit"); }

    /// @return the `defaultUi` child (typed), or null.
    public DefaultUi getDefaultUi() { return (DefaultUi) getChild("defaultUi"); }
    /// Ensures and returns the `defaultUi` child.
    public DefaultUi ensureDefaultUi() { return (DefaultUi) ensureChild("defaultUi"); }

    /// @return the `imageEdit` child (typed), or null.
    public ImageEdit getImageEdit() { return (ImageEdit) getChild("imageEdit"); }
    /// Ensures and returns the `imageEdit` child.
    public ImageEdit ensureImageEdit() { return (ImageEdit) ensureChild("imageEdit"); }

    /// @return the `numericEdit` child (typed), or null.
    public NumericEdit getNumericEdit() { return (NumericEdit) getChild("numericEdit"); }
    /// Ensures and returns the `numericEdit` child.
    public NumericEdit ensureNumericEdit() { return (NumericEdit) ensureChild("numericEdit"); }

    /// @return the `passwordEdit` child (typed), or null.
    public PasswordEdit getPasswordEdit() { return (PasswordEdit) getChild("passwordEdit"); }
    /// Ensures and returns the `passwordEdit` child.
    public PasswordEdit ensurePasswordEdit() { return (PasswordEdit) ensureChild("passwordEdit"); }

    /// @return the `signature` child (typed), or null.
    public Signature getSignature() { return (Signature) getChild("signature"); }
    /// Ensures and returns the `signature` child.
    public Signature ensureSignature() { return (Signature) ensureChild("signature"); }

    /// @return the `textEdit` child (typed), or null.
    public TextEdit getTextEdit() { return (TextEdit) getChild("textEdit"); }
    /// Ensures and returns the `textEdit` child.
    public TextEdit ensureTextEdit() { return (TextEdit) ensureChild("textEdit"); }
}
