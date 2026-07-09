package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;

/**
 * Registry of generated typed element constructors for this XFA grammar
 * (element local name -> typed node).
 */
public final class XfaTemplateElements {

    private XfaTemplateElements() { }

    /** The grammar's (version-independent) target namespace. */
    public static final String NAMESPACE = "http://www.xfa.org/schema/xfa-template/";

    /** Number of generated typed element classes. */
    public static final int COUNT = 110;

    /**
     * Registers all typed element constructors.
     * @param reg the factory registry map
     */
    public static void registerAll(java.util.Map<String, XfaNodeFactory.Ctor> reg) {
        reg.put("appearanceFilter", AppearanceFilter::new);
        reg.put("arc", Arc::new);
        reg.put("area", Area::new);
        reg.put("assist", Assist::new);
        reg.put("barcode", Barcode::new);
        reg.put("bind", Bind::new);
        reg.put("bindItems", BindItems::new);
        reg.put("bookend", Bookend::new);
        reg.put("boolean", Boolean::new);
        reg.put("border", Border::new);
        reg.put("break", Break::new);
        reg.put("breakAfter", BreakAfter::new);
        reg.put("breakBefore", BreakBefore::new);
        reg.put("button", Button::new);
        reg.put("calcProperty", CalcProperty::new);
        reg.put("calculate", Calculate::new);
        reg.put("caption", Caption::new);
        reg.put("certificate", Certificate::new);
        reg.put("certificates", Certificates::new);
        reg.put("checkButton", CheckButton::new);
        reg.put("choiceList", ChoiceList::new);
        reg.put("color", Color::new);
        reg.put("comb", Comb::new);
        reg.put("connect", Connect::new);
        reg.put("contentArea", ContentArea::new);
        reg.put("corner", Corner::new);
        reg.put("date", Date::new);
        reg.put("dateTime", DateTime::new);
        reg.put("dateTimeEdit", DateTimeEdit::new);
        reg.put("decimal", Decimal::new);
        reg.put("defaultUi", DefaultUi::new);
        reg.put("desc", Desc::new);
        reg.put("digestMethod", DigestMethod::new);
        reg.put("digestMethods", DigestMethods::new);
        reg.put("draw", Draw::new);
        reg.put("edge", Edge::new);
        reg.put("encoding", Encoding::new);
        reg.put("encodings", Encodings::new);
        reg.put("encrypt", Encrypt::new);
        reg.put("event", Event::new);
        reg.put("exData", ExData::new);
        reg.put("exObject", ExObject::new);
        reg.put("exclGroup", ExclGroup::new);
        reg.put("execute", Execute::new);
        reg.put("extras", Extras::new);
        reg.put("field", Field::new);
        reg.put("fill", Fill::new);
        reg.put("filter", Filter::new);
        reg.put("float", Float::new);
        reg.put("font", Font::new);
        reg.put("format", Format::new);
        reg.put("handler", Handler::new);
        reg.put("hyphenation", Hyphenation::new);
        reg.put("image", Image::new);
        reg.put("imageEdit", ImageEdit::new);
        reg.put("integer", Integer::new);
        reg.put("issuers", Issuers::new);
        reg.put("items", Items::new);
        reg.put("keep", Keep::new);
        reg.put("keyUsage", KeyUsage::new);
        reg.put("line", Line::new);
        reg.put("linear", Linear::new);
        reg.put("lockDocument", LockDocument::new);
        reg.put("manifest", Manifest::new);
        reg.put("margin", Margin::new);
        reg.put("mdp", Mdp::new);
        reg.put("medium", Medium::new);
        reg.put("message", Message::new);
        reg.put("numericEdit", NumericEdit::new);
        reg.put("occur", Occur::new);
        reg.put("oid", Oid::new);
        reg.put("oids", Oids::new);
        reg.put("overflow", Overflow::new);
        reg.put("pageArea", PageArea::new);
        reg.put("pageSet", PageSet::new);
        reg.put("para", Para::new);
        reg.put("passwordEdit", PasswordEdit::new);
        reg.put("pattern", Pattern::new);
        reg.put("picture", Picture::new);
        reg.put("proto", Proto::new);
        reg.put("radial", Radial::new);
        reg.put("reason", Reason::new);
        reg.put("reasons", Reasons::new);
        reg.put("rectangle", Rectangle::new);
        reg.put("ref", Ref::new);
        reg.put("script", Script::new);
        reg.put("setProperty", SetProperty::new);
        reg.put("signData", SignData::new);
        reg.put("signature", Signature::new);
        reg.put("signing", Signing::new);
        reg.put("solid", Solid::new);
        reg.put("speak", Speak::new);
        reg.put("stipple", Stipple::new);
        reg.put("subform", Subform::new);
        reg.put("subformSet", SubformSet::new);
        reg.put("subjectDN", SubjectDN::new);
        reg.put("subjectDNs", SubjectDNs::new);
        reg.put("submit", Submit::new);
        reg.put("template", Template::new);
        reg.put("text", Text::new);
        reg.put("textEdit", TextEdit::new);
        reg.put("time", Time::new);
        reg.put("timeStamp", TimeStamp::new);
        reg.put("toolTip", ToolTip::new);
        reg.put("traversal", Traversal::new);
        reg.put("traverse", Traverse::new);
        reg.put("ui", Ui::new);
        reg.put("validate", Validate::new);
        reg.put("value", Value::new);
        reg.put("variables", Variables::new);
    }
}
