package org.aspose.pdf.engine.xfa.model.template;

import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;

/// Typed XFA template element `proto`. Attribute and child
/// accessors are typed; unknown attributes/children round-trip via the DOM.
public final class Proto extends XfaNode {

    /// Wraps a backing `proto` element.
    public Proto(Element element, XfaNode parent) {
        super(element, parent);
    }

    /// @return the `appearanceFilter` children (typed).
    public java.util.List<AppearanceFilter> getAppearanceFilterList() {
        java.util.List<AppearanceFilter> r = new java.util.ArrayList<AppearanceFilter>();
        for (XfaNode n : getChildren("appearanceFilter")) { r.add((AppearanceFilter) n); }
        return r;
    }
    /// Appends a new `appearanceFilter` child.
    public AppearanceFilter addAppearanceFilter() { return (AppearanceFilter) addChild("appearanceFilter"); }

    /// @return the `arc` children (typed).
    public java.util.List<Arc> getArcList() {
        java.util.List<Arc> r = new java.util.ArrayList<Arc>();
        for (XfaNode n : getChildren("arc")) { r.add((Arc) n); }
        return r;
    }
    /// Appends a new `arc` child.
    public Arc addArc() { return (Arc) addChild("arc"); }

    /// @return the `area` children (typed).
    public java.util.List<Area> getAreaList() {
        java.util.List<Area> r = new java.util.ArrayList<Area>();
        for (XfaNode n : getChildren("area")) { r.add((Area) n); }
        return r;
    }
    /// Appends a new `area` child.
    public Area addArea() { return (Area) addChild("area"); }

    /// @return the `assist` children (typed).
    public java.util.List<Assist> getAssistList() {
        java.util.List<Assist> r = new java.util.ArrayList<Assist>();
        for (XfaNode n : getChildren("assist")) { r.add((Assist) n); }
        return r;
    }
    /// Appends a new `assist` child.
    public Assist addAssist() { return (Assist) addChild("assist"); }

    /// @return the `barcode` children (typed).
    public java.util.List<Barcode> getBarcodeList() {
        java.util.List<Barcode> r = new java.util.ArrayList<Barcode>();
        for (XfaNode n : getChildren("barcode")) { r.add((Barcode) n); }
        return r;
    }
    /// Appends a new `barcode` child.
    public Barcode addBarcode() { return (Barcode) addChild("barcode"); }

    /// @return the `bindItems` children (typed).
    public java.util.List<BindItems> getBindItemsList() {
        java.util.List<BindItems> r = new java.util.ArrayList<BindItems>();
        for (XfaNode n : getChildren("bindItems")) { r.add((BindItems) n); }
        return r;
    }
    /// Appends a new `bindItems` child.
    public BindItems addBindItems() { return (BindItems) addChild("bindItems"); }

    /// @return the `bookend` children (typed).
    public java.util.List<Bookend> getBookendList() {
        java.util.List<Bookend> r = new java.util.ArrayList<Bookend>();
        for (XfaNode n : getChildren("bookend")) { r.add((Bookend) n); }
        return r;
    }
    /// Appends a new `bookend` child.
    public Bookend addBookend() { return (Bookend) addChild("bookend"); }

    /// @return the `boolean` children (typed).
    public java.util.List<Boolean> getBooleanList() {
        java.util.List<Boolean> r = new java.util.ArrayList<Boolean>();
        for (XfaNode n : getChildren("boolean")) { r.add((Boolean) n); }
        return r;
    }
    /// Appends a new `boolean` child.
    public Boolean addBoolean() { return (Boolean) addChild("boolean"); }

    /// @return the `border` children (typed).
    public java.util.List<Border> getBorderList() {
        java.util.List<Border> r = new java.util.ArrayList<Border>();
        for (XfaNode n : getChildren("border")) { r.add((Border) n); }
        return r;
    }
    /// Appends a new `border` child.
    public Border addBorder() { return (Border) addChild("border"); }

    /// @return the `break` children (typed).
    public java.util.List<Break> getBreakList() {
        java.util.List<Break> r = new java.util.ArrayList<Break>();
        for (XfaNode n : getChildren("break")) { r.add((Break) n); }
        return r;
    }
    /// Appends a new `break` child.
    public Break addBreak() { return (Break) addChild("break"); }

    /// @return the `breakAfter` children (typed).
    public java.util.List<BreakAfter> getBreakAfterList() {
        java.util.List<BreakAfter> r = new java.util.ArrayList<BreakAfter>();
        for (XfaNode n : getChildren("breakAfter")) { r.add((BreakAfter) n); }
        return r;
    }
    /// Appends a new `breakAfter` child.
    public BreakAfter addBreakAfter() { return (BreakAfter) addChild("breakAfter"); }

    /// @return the `breakBefore` children (typed).
    public java.util.List<BreakBefore> getBreakBeforeList() {
        java.util.List<BreakBefore> r = new java.util.ArrayList<BreakBefore>();
        for (XfaNode n : getChildren("breakBefore")) { r.add((BreakBefore) n); }
        return r;
    }
    /// Appends a new `breakBefore` child.
    public BreakBefore addBreakBefore() { return (BreakBefore) addChild("breakBefore"); }

    /// @return the `button` children (typed).
    public java.util.List<Button> getButtonList() {
        java.util.List<Button> r = new java.util.ArrayList<Button>();
        for (XfaNode n : getChildren("button")) { r.add((Button) n); }
        return r;
    }
    /// Appends a new `button` child.
    public Button addButton() { return (Button) addChild("button"); }

    /// @return the `calcProperty` children (typed).
    public java.util.List<CalcProperty> getCalcPropertyList() {
        java.util.List<CalcProperty> r = new java.util.ArrayList<CalcProperty>();
        for (XfaNode n : getChildren("calcProperty")) { r.add((CalcProperty) n); }
        return r;
    }
    /// Appends a new `calcProperty` child.
    public CalcProperty addCalcProperty() { return (CalcProperty) addChild("calcProperty"); }

    /// @return the `calculate` children (typed).
    public java.util.List<Calculate> getCalculateList() {
        java.util.List<Calculate> r = new java.util.ArrayList<Calculate>();
        for (XfaNode n : getChildren("calculate")) { r.add((Calculate) n); }
        return r;
    }
    /// Appends a new `calculate` child.
    public Calculate addCalculate() { return (Calculate) addChild("calculate"); }

    /// @return the `caption` children (typed).
    public java.util.List<Caption> getCaptionList() {
        java.util.List<Caption> r = new java.util.ArrayList<Caption>();
        for (XfaNode n : getChildren("caption")) { r.add((Caption) n); }
        return r;
    }
    /// Appends a new `caption` child.
    public Caption addCaption() { return (Caption) addChild("caption"); }

    /// @return the `certificate` children (typed).
    public java.util.List<Certificate> getCertificateList() {
        java.util.List<Certificate> r = new java.util.ArrayList<Certificate>();
        for (XfaNode n : getChildren("certificate")) { r.add((Certificate) n); }
        return r;
    }
    /// Appends a new `certificate` child.
    public Certificate addCertificate() { return (Certificate) addChild("certificate"); }

    /// @return the `certificates` children (typed).
    public java.util.List<Certificates> getCertificatesList() {
        java.util.List<Certificates> r = new java.util.ArrayList<Certificates>();
        for (XfaNode n : getChildren("certificates")) { r.add((Certificates) n); }
        return r;
    }
    /// Appends a new `certificates` child.
    public Certificates addCertificates() { return (Certificates) addChild("certificates"); }

    /// @return the `checkButton` children (typed).
    public java.util.List<CheckButton> getCheckButtonList() {
        java.util.List<CheckButton> r = new java.util.ArrayList<CheckButton>();
        for (XfaNode n : getChildren("checkButton")) { r.add((CheckButton) n); }
        return r;
    }
    /// Appends a new `checkButton` child.
    public CheckButton addCheckButton() { return (CheckButton) addChild("checkButton"); }

    /// @return the `choiceList` children (typed).
    public java.util.List<ChoiceList> getChoiceListList() {
        java.util.List<ChoiceList> r = new java.util.ArrayList<ChoiceList>();
        for (XfaNode n : getChildren("choiceList")) { r.add((ChoiceList) n); }
        return r;
    }
    /// Appends a new `choiceList` child.
    public ChoiceList addChoiceList() { return (ChoiceList) addChild("choiceList"); }

    /// @return the `color` children (typed).
    public java.util.List<Color> getColorList() {
        java.util.List<Color> r = new java.util.ArrayList<Color>();
        for (XfaNode n : getChildren("color")) { r.add((Color) n); }
        return r;
    }
    /// Appends a new `color` child.
    public Color addColor() { return (Color) addChild("color"); }

    /// @return the `comb` children (typed).
    public java.util.List<Comb> getCombList() {
        java.util.List<Comb> r = new java.util.ArrayList<Comb>();
        for (XfaNode n : getChildren("comb")) { r.add((Comb) n); }
        return r;
    }
    /// Appends a new `comb` child.
    public Comb addComb() { return (Comb) addChild("comb"); }

    /// @return the `connect` children (typed).
    public java.util.List<Connect> getConnectList() {
        java.util.List<Connect> r = new java.util.ArrayList<Connect>();
        for (XfaNode n : getChildren("connect")) { r.add((Connect) n); }
        return r;
    }
    /// Appends a new `connect` child.
    public Connect addConnect() { return (Connect) addChild("connect"); }

    /// @return the `contentArea` children (typed).
    public java.util.List<ContentArea> getContentAreaList() {
        java.util.List<ContentArea> r = new java.util.ArrayList<ContentArea>();
        for (XfaNode n : getChildren("contentArea")) { r.add((ContentArea) n); }
        return r;
    }
    /// Appends a new `contentArea` child.
    public ContentArea addContentArea() { return (ContentArea) addChild("contentArea"); }

    /// @return the `corner` children (typed).
    public java.util.List<Corner> getCornerList() {
        java.util.List<Corner> r = new java.util.ArrayList<Corner>();
        for (XfaNode n : getChildren("corner")) { r.add((Corner) n); }
        return r;
    }
    /// Appends a new `corner` child.
    public Corner addCorner() { return (Corner) addChild("corner"); }

    /// @return the `date` children (typed).
    public java.util.List<Date> getDateList() {
        java.util.List<Date> r = new java.util.ArrayList<Date>();
        for (XfaNode n : getChildren("date")) { r.add((Date) n); }
        return r;
    }
    /// Appends a new `date` child.
    public Date addDate() { return (Date) addChild("date"); }

    /// @return the `dateTime` children (typed).
    public java.util.List<DateTime> getDateTimeList() {
        java.util.List<DateTime> r = new java.util.ArrayList<DateTime>();
        for (XfaNode n : getChildren("dateTime")) { r.add((DateTime) n); }
        return r;
    }
    /// Appends a new `dateTime` child.
    public DateTime addDateTime() { return (DateTime) addChild("dateTime"); }

    /// @return the `dateTimeEdit` children (typed).
    public java.util.List<DateTimeEdit> getDateTimeEditList() {
        java.util.List<DateTimeEdit> r = new java.util.ArrayList<DateTimeEdit>();
        for (XfaNode n : getChildren("dateTimeEdit")) { r.add((DateTimeEdit) n); }
        return r;
    }
    /// Appends a new `dateTimeEdit` child.
    public DateTimeEdit addDateTimeEdit() { return (DateTimeEdit) addChild("dateTimeEdit"); }

    /// @return the `decimal` children (typed).
    public java.util.List<Decimal> getDecimalList() {
        java.util.List<Decimal> r = new java.util.ArrayList<Decimal>();
        for (XfaNode n : getChildren("decimal")) { r.add((Decimal) n); }
        return r;
    }
    /// Appends a new `decimal` child.
    public Decimal addDecimal() { return (Decimal) addChild("decimal"); }

    /// @return the `defaultUi` children (typed).
    public java.util.List<DefaultUi> getDefaultUiList() {
        java.util.List<DefaultUi> r = new java.util.ArrayList<DefaultUi>();
        for (XfaNode n : getChildren("defaultUi")) { r.add((DefaultUi) n); }
        return r;
    }
    /// Appends a new `defaultUi` child.
    public DefaultUi addDefaultUi() { return (DefaultUi) addChild("defaultUi"); }

    /// @return the `desc` children (typed).
    public java.util.List<Desc> getDescList() {
        java.util.List<Desc> r = new java.util.ArrayList<Desc>();
        for (XfaNode n : getChildren("desc")) { r.add((Desc) n); }
        return r;
    }
    /// Appends a new `desc` child.
    public Desc addDesc() { return (Desc) addChild("desc"); }

    /// @return the `digestMethod` children (typed).
    public java.util.List<DigestMethod> getDigestMethodList() {
        java.util.List<DigestMethod> r = new java.util.ArrayList<DigestMethod>();
        for (XfaNode n : getChildren("digestMethod")) { r.add((DigestMethod) n); }
        return r;
    }
    /// Appends a new `digestMethod` child.
    public DigestMethod addDigestMethod() { return (DigestMethod) addChild("digestMethod"); }

    /// @return the `digestMethods` children (typed).
    public java.util.List<DigestMethods> getDigestMethodsList() {
        java.util.List<DigestMethods> r = new java.util.ArrayList<DigestMethods>();
        for (XfaNode n : getChildren("digestMethods")) { r.add((DigestMethods) n); }
        return r;
    }
    /// Appends a new `digestMethods` child.
    public DigestMethods addDigestMethods() { return (DigestMethods) addChild("digestMethods"); }

    /// @return the `draw` children (typed).
    public java.util.List<Draw> getDrawList() {
        java.util.List<Draw> r = new java.util.ArrayList<Draw>();
        for (XfaNode n : getChildren("draw")) { r.add((Draw) n); }
        return r;
    }
    /// Appends a new `draw` child.
    public Draw addDraw() { return (Draw) addChild("draw"); }

    /// @return the `edge` children (typed).
    public java.util.List<Edge> getEdgeList() {
        java.util.List<Edge> r = new java.util.ArrayList<Edge>();
        for (XfaNode n : getChildren("edge")) { r.add((Edge) n); }
        return r;
    }
    /// Appends a new `edge` child.
    public Edge addEdge() { return (Edge) addChild("edge"); }

    /// @return the `encoding` children (typed).
    public java.util.List<Encoding> getEncodingList() {
        java.util.List<Encoding> r = new java.util.ArrayList<Encoding>();
        for (XfaNode n : getChildren("encoding")) { r.add((Encoding) n); }
        return r;
    }
    /// Appends a new `encoding` child.
    public Encoding addEncoding() { return (Encoding) addChild("encoding"); }

    /// @return the `encodings` children (typed).
    public java.util.List<Encodings> getEncodingsList() {
        java.util.List<Encodings> r = new java.util.ArrayList<Encodings>();
        for (XfaNode n : getChildren("encodings")) { r.add((Encodings) n); }
        return r;
    }
    /// Appends a new `encodings` child.
    public Encodings addEncodings() { return (Encodings) addChild("encodings"); }

    /// @return the `encrypt` children (typed).
    public java.util.List<Encrypt> getEncryptList() {
        java.util.List<Encrypt> r = new java.util.ArrayList<Encrypt>();
        for (XfaNode n : getChildren("encrypt")) { r.add((Encrypt) n); }
        return r;
    }
    /// Appends a new `encrypt` child.
    public Encrypt addEncrypt() { return (Encrypt) addChild("encrypt"); }

    /// @return the `event` children (typed).
    public java.util.List<Event> getEventList() {
        java.util.List<Event> r = new java.util.ArrayList<Event>();
        for (XfaNode n : getChildren("event")) { r.add((Event) n); }
        return r;
    }
    /// Appends a new `event` child.
    public Event addEvent() { return (Event) addChild("event"); }

    /// @return the `exclGroup` children (typed).
    public java.util.List<ExclGroup> getExclGroupList() {
        java.util.List<ExclGroup> r = new java.util.ArrayList<ExclGroup>();
        for (XfaNode n : getChildren("exclGroup")) { r.add((ExclGroup) n); }
        return r;
    }
    /// Appends a new `exclGroup` child.
    public ExclGroup addExclGroup() { return (ExclGroup) addChild("exclGroup"); }

    /// @return the `exData` children (typed).
    public java.util.List<ExData> getExDataList() {
        java.util.List<ExData> r = new java.util.ArrayList<ExData>();
        for (XfaNode n : getChildren("exData")) { r.add((ExData) n); }
        return r;
    }
    /// Appends a new `exData` child.
    public ExData addExData() { return (ExData) addChild("exData"); }

    /// @return the `execute` children (typed).
    public java.util.List<Execute> getExecuteList() {
        java.util.List<Execute> r = new java.util.ArrayList<Execute>();
        for (XfaNode n : getChildren("execute")) { r.add((Execute) n); }
        return r;
    }
    /// Appends a new `execute` child.
    public Execute addExecute() { return (Execute) addChild("execute"); }

    /// @return the `exObject` children (typed).
    public java.util.List<ExObject> getExObjectList() {
        java.util.List<ExObject> r = new java.util.ArrayList<ExObject>();
        for (XfaNode n : getChildren("exObject")) { r.add((ExObject) n); }
        return r;
    }
    /// Appends a new `exObject` child.
    public ExObject addExObject() { return (ExObject) addChild("exObject"); }

    /// @return the `extras` children (typed).
    public java.util.List<Extras> getExtrasList() {
        java.util.List<Extras> r = new java.util.ArrayList<Extras>();
        for (XfaNode n : getChildren("extras")) { r.add((Extras) n); }
        return r;
    }
    /// Appends a new `extras` child.
    public Extras addExtras() { return (Extras) addChild("extras"); }

    /// @return the `field` children (typed).
    public java.util.List<Field> getFieldList() {
        java.util.List<Field> r = new java.util.ArrayList<Field>();
        for (XfaNode n : getChildren("field")) { r.add((Field) n); }
        return r;
    }
    /// Appends a new `field` child.
    public Field addField() { return (Field) addChild("field"); }

    /// @return the `fill` children (typed).
    public java.util.List<Fill> getFillList() {
        java.util.List<Fill> r = new java.util.ArrayList<Fill>();
        for (XfaNode n : getChildren("fill")) { r.add((Fill) n); }
        return r;
    }
    /// Appends a new `fill` child.
    public Fill addFill() { return (Fill) addChild("fill"); }

    /// @return the `filter` children (typed).
    public java.util.List<Filter> getFilterList() {
        java.util.List<Filter> r = new java.util.ArrayList<Filter>();
        for (XfaNode n : getChildren("filter")) { r.add((Filter) n); }
        return r;
    }
    /// Appends a new `filter` child.
    public Filter addFilter() { return (Filter) addChild("filter"); }

    /// @return the `float` children (typed).
    public java.util.List<Float> getFloatList() {
        java.util.List<Float> r = new java.util.ArrayList<Float>();
        for (XfaNode n : getChildren("float")) { r.add((Float) n); }
        return r;
    }
    /// Appends a new `float` child.
    public Float addFloat() { return (Float) addChild("float"); }

    /// @return the `font` children (typed).
    public java.util.List<Font> getFontList() {
        java.util.List<Font> r = new java.util.ArrayList<Font>();
        for (XfaNode n : getChildren("font")) { r.add((Font) n); }
        return r;
    }
    /// Appends a new `font` child.
    public Font addFont() { return (Font) addChild("font"); }

    /// @return the `format` children (typed).
    public java.util.List<Format> getFormatList() {
        java.util.List<Format> r = new java.util.ArrayList<Format>();
        for (XfaNode n : getChildren("format")) { r.add((Format) n); }
        return r;
    }
    /// Appends a new `format` child.
    public Format addFormat() { return (Format) addChild("format"); }

    /// @return the `handler` children (typed).
    public java.util.List<Handler> getHandlerList() {
        java.util.List<Handler> r = new java.util.ArrayList<Handler>();
        for (XfaNode n : getChildren("handler")) { r.add((Handler) n); }
        return r;
    }
    /// Appends a new `handler` child.
    public Handler addHandler() { return (Handler) addChild("handler"); }

    /// @return the `hyphenation` children (typed).
    public java.util.List<Hyphenation> getHyphenationList() {
        java.util.List<Hyphenation> r = new java.util.ArrayList<Hyphenation>();
        for (XfaNode n : getChildren("hyphenation")) { r.add((Hyphenation) n); }
        return r;
    }
    /// Appends a new `hyphenation` child.
    public Hyphenation addHyphenation() { return (Hyphenation) addChild("hyphenation"); }

    /// @return the `image` children (typed).
    public java.util.List<Image> getImageList() {
        java.util.List<Image> r = new java.util.ArrayList<Image>();
        for (XfaNode n : getChildren("image")) { r.add((Image) n); }
        return r;
    }
    /// Appends a new `image` child.
    public Image addImage() { return (Image) addChild("image"); }

    /// @return the `imageEdit` children (typed).
    public java.util.List<ImageEdit> getImageEditList() {
        java.util.List<ImageEdit> r = new java.util.ArrayList<ImageEdit>();
        for (XfaNode n : getChildren("imageEdit")) { r.add((ImageEdit) n); }
        return r;
    }
    /// Appends a new `imageEdit` child.
    public ImageEdit addImageEdit() { return (ImageEdit) addChild("imageEdit"); }

    /// @return the `integer` children (typed).
    public java.util.List<Integer> getIntegerList() {
        java.util.List<Integer> r = new java.util.ArrayList<Integer>();
        for (XfaNode n : getChildren("integer")) { r.add((Integer) n); }
        return r;
    }
    /// Appends a new `integer` child.
    public Integer addInteger() { return (Integer) addChild("integer"); }

    /// @return the `issuers` children (typed).
    public java.util.List<Issuers> getIssuersList() {
        java.util.List<Issuers> r = new java.util.ArrayList<Issuers>();
        for (XfaNode n : getChildren("issuers")) { r.add((Issuers) n); }
        return r;
    }
    /// Appends a new `issuers` child.
    public Issuers addIssuers() { return (Issuers) addChild("issuers"); }

    /// @return the `items` children (typed).
    public java.util.List<Items> getItemsList() {
        java.util.List<Items> r = new java.util.ArrayList<Items>();
        for (XfaNode n : getChildren("items")) { r.add((Items) n); }
        return r;
    }
    /// Appends a new `items` child.
    public Items addItems() { return (Items) addChild("items"); }

    /// @return the `keep` children (typed).
    public java.util.List<Keep> getKeepList() {
        java.util.List<Keep> r = new java.util.ArrayList<Keep>();
        for (XfaNode n : getChildren("keep")) { r.add((Keep) n); }
        return r;
    }
    /// Appends a new `keep` child.
    public Keep addKeep() { return (Keep) addChild("keep"); }

    /// @return the `keyUsage` children (typed).
    public java.util.List<KeyUsage> getKeyUsageList() {
        java.util.List<KeyUsage> r = new java.util.ArrayList<KeyUsage>();
        for (XfaNode n : getChildren("keyUsage")) { r.add((KeyUsage) n); }
        return r;
    }
    /// Appends a new `keyUsage` child.
    public KeyUsage addKeyUsage() { return (KeyUsage) addChild("keyUsage"); }

    /// @return the `line` children (typed).
    public java.util.List<Line> getLineList() {
        java.util.List<Line> r = new java.util.ArrayList<Line>();
        for (XfaNode n : getChildren("line")) { r.add((Line) n); }
        return r;
    }
    /// Appends a new `line` child.
    public Line addLine() { return (Line) addChild("line"); }

    /// @return the `linear` children (typed).
    public java.util.List<Linear> getLinearList() {
        java.util.List<Linear> r = new java.util.ArrayList<Linear>();
        for (XfaNode n : getChildren("linear")) { r.add((Linear) n); }
        return r;
    }
    /// Appends a new `linear` child.
    public Linear addLinear() { return (Linear) addChild("linear"); }

    /// @return the `lockDocument` children (typed).
    public java.util.List<LockDocument> getLockDocumentList() {
        java.util.List<LockDocument> r = new java.util.ArrayList<LockDocument>();
        for (XfaNode n : getChildren("lockDocument")) { r.add((LockDocument) n); }
        return r;
    }
    /// Appends a new `lockDocument` child.
    public LockDocument addLockDocument() { return (LockDocument) addChild("lockDocument"); }

    /// @return the `manifest` children (typed).
    public java.util.List<Manifest> getManifestList() {
        java.util.List<Manifest> r = new java.util.ArrayList<Manifest>();
        for (XfaNode n : getChildren("manifest")) { r.add((Manifest) n); }
        return r;
    }
    /// Appends a new `manifest` child.
    public Manifest addManifest() { return (Manifest) addChild("manifest"); }

    /// @return the `margin` children (typed).
    public java.util.List<Margin> getMarginList() {
        java.util.List<Margin> r = new java.util.ArrayList<Margin>();
        for (XfaNode n : getChildren("margin")) { r.add((Margin) n); }
        return r;
    }
    /// Appends a new `margin` child.
    public Margin addMargin() { return (Margin) addChild("margin"); }

    /// @return the `mdp` children (typed).
    public java.util.List<Mdp> getMdpList() {
        java.util.List<Mdp> r = new java.util.ArrayList<Mdp>();
        for (XfaNode n : getChildren("mdp")) { r.add((Mdp) n); }
        return r;
    }
    /// Appends a new `mdp` child.
    public Mdp addMdp() { return (Mdp) addChild("mdp"); }

    /// @return the `medium` children (typed).
    public java.util.List<Medium> getMediumList() {
        java.util.List<Medium> r = new java.util.ArrayList<Medium>();
        for (XfaNode n : getChildren("medium")) { r.add((Medium) n); }
        return r;
    }
    /// Appends a new `medium` child.
    public Medium addMedium() { return (Medium) addChild("medium"); }

    /// @return the `message` children (typed).
    public java.util.List<Message> getMessageList() {
        java.util.List<Message> r = new java.util.ArrayList<Message>();
        for (XfaNode n : getChildren("message")) { r.add((Message) n); }
        return r;
    }
    /// Appends a new `message` child.
    public Message addMessage() { return (Message) addChild("message"); }

    /// @return the `numericEdit` children (typed).
    public java.util.List<NumericEdit> getNumericEditList() {
        java.util.List<NumericEdit> r = new java.util.ArrayList<NumericEdit>();
        for (XfaNode n : getChildren("numericEdit")) { r.add((NumericEdit) n); }
        return r;
    }
    /// Appends a new `numericEdit` child.
    public NumericEdit addNumericEdit() { return (NumericEdit) addChild("numericEdit"); }

    /// @return the `occur` children (typed).
    public java.util.List<Occur> getOccurList() {
        java.util.List<Occur> r = new java.util.ArrayList<Occur>();
        for (XfaNode n : getChildren("occur")) { r.add((Occur) n); }
        return r;
    }
    /// Appends a new `occur` child.
    public Occur addOccur() { return (Occur) addChild("occur"); }

    /// @return the `oid` children (typed).
    public java.util.List<Oid> getOidList() {
        java.util.List<Oid> r = new java.util.ArrayList<Oid>();
        for (XfaNode n : getChildren("oid")) { r.add((Oid) n); }
        return r;
    }
    /// Appends a new `oid` child.
    public Oid addOid() { return (Oid) addChild("oid"); }

    /// @return the `oids` children (typed).
    public java.util.List<Oids> getOidsList() {
        java.util.List<Oids> r = new java.util.ArrayList<Oids>();
        for (XfaNode n : getChildren("oids")) { r.add((Oids) n); }
        return r;
    }
    /// Appends a new `oids` child.
    public Oids addOids() { return (Oids) addChild("oids"); }

    /// @return the `overflow` children (typed).
    public java.util.List<Overflow> getOverflowList() {
        java.util.List<Overflow> r = new java.util.ArrayList<Overflow>();
        for (XfaNode n : getChildren("overflow")) { r.add((Overflow) n); }
        return r;
    }
    /// Appends a new `overflow` child.
    public Overflow addOverflow() { return (Overflow) addChild("overflow"); }

    /// @return the `pageArea` children (typed).
    public java.util.List<PageArea> getPageAreaList() {
        java.util.List<PageArea> r = new java.util.ArrayList<PageArea>();
        for (XfaNode n : getChildren("pageArea")) { r.add((PageArea) n); }
        return r;
    }
    /// Appends a new `pageArea` child.
    public PageArea addPageArea() { return (PageArea) addChild("pageArea"); }

    /// @return the `pageSet` children (typed).
    public java.util.List<PageSet> getPageSetList() {
        java.util.List<PageSet> r = new java.util.ArrayList<PageSet>();
        for (XfaNode n : getChildren("pageSet")) { r.add((PageSet) n); }
        return r;
    }
    /// Appends a new `pageSet` child.
    public PageSet addPageSet() { return (PageSet) addChild("pageSet"); }

    /// @return the `para` children (typed).
    public java.util.List<Para> getParaList() {
        java.util.List<Para> r = new java.util.ArrayList<Para>();
        for (XfaNode n : getChildren("para")) { r.add((Para) n); }
        return r;
    }
    /// Appends a new `para` child.
    public Para addPara() { return (Para) addChild("para"); }

    /// @return the `passwordEdit` children (typed).
    public java.util.List<PasswordEdit> getPasswordEditList() {
        java.util.List<PasswordEdit> r = new java.util.ArrayList<PasswordEdit>();
        for (XfaNode n : getChildren("passwordEdit")) { r.add((PasswordEdit) n); }
        return r;
    }
    /// Appends a new `passwordEdit` child.
    public PasswordEdit addPasswordEdit() { return (PasswordEdit) addChild("passwordEdit"); }

    /// @return the `pattern` children (typed).
    public java.util.List<Pattern> getPatternList() {
        java.util.List<Pattern> r = new java.util.ArrayList<Pattern>();
        for (XfaNode n : getChildren("pattern")) { r.add((Pattern) n); }
        return r;
    }
    /// Appends a new `pattern` child.
    public Pattern addPattern() { return (Pattern) addChild("pattern"); }

    /// @return the `picture` children (typed).
    public java.util.List<Picture> getPictureList() {
        java.util.List<Picture> r = new java.util.ArrayList<Picture>();
        for (XfaNode n : getChildren("picture")) { r.add((Picture) n); }
        return r;
    }
    /// Appends a new `picture` child.
    public Picture addPicture() { return (Picture) addChild("picture"); }

    /// @return the `radial` children (typed).
    public java.util.List<Radial> getRadialList() {
        java.util.List<Radial> r = new java.util.ArrayList<Radial>();
        for (XfaNode n : getChildren("radial")) { r.add((Radial) n); }
        return r;
    }
    /// Appends a new `radial` child.
    public Radial addRadial() { return (Radial) addChild("radial"); }

    /// @return the `reason` children (typed).
    public java.util.List<Reason> getReasonList() {
        java.util.List<Reason> r = new java.util.ArrayList<Reason>();
        for (XfaNode n : getChildren("reason")) { r.add((Reason) n); }
        return r;
    }
    /// Appends a new `reason` child.
    public Reason addReason() { return (Reason) addChild("reason"); }

    /// @return the `reasons` children (typed).
    public java.util.List<Reasons> getReasonsList() {
        java.util.List<Reasons> r = new java.util.ArrayList<Reasons>();
        for (XfaNode n : getChildren("reasons")) { r.add((Reasons) n); }
        return r;
    }
    /// Appends a new `reasons` child.
    public Reasons addReasons() { return (Reasons) addChild("reasons"); }

    /// @return the `rectangle` children (typed).
    public java.util.List<Rectangle> getRectangleList() {
        java.util.List<Rectangle> r = new java.util.ArrayList<Rectangle>();
        for (XfaNode n : getChildren("rectangle")) { r.add((Rectangle) n); }
        return r;
    }
    /// Appends a new `rectangle` child.
    public Rectangle addRectangle() { return (Rectangle) addChild("rectangle"); }

    /// @return the `ref` children (typed).
    public java.util.List<Ref> getRefList() {
        java.util.List<Ref> r = new java.util.ArrayList<Ref>();
        for (XfaNode n : getChildren("ref")) { r.add((Ref) n); }
        return r;
    }
    /// Appends a new `ref` child.
    public Ref addRef() { return (Ref) addChild("ref"); }

    /// @return the `script` children (typed).
    public java.util.List<Script> getScriptList() {
        java.util.List<Script> r = new java.util.ArrayList<Script>();
        for (XfaNode n : getChildren("script")) { r.add((Script) n); }
        return r;
    }
    /// Appends a new `script` child.
    public Script addScript() { return (Script) addChild("script"); }

    /// @return the `setProperty` children (typed).
    public java.util.List<SetProperty> getSetPropertyList() {
        java.util.List<SetProperty> r = new java.util.ArrayList<SetProperty>();
        for (XfaNode n : getChildren("setProperty")) { r.add((SetProperty) n); }
        return r;
    }
    /// Appends a new `setProperty` child.
    public SetProperty addSetProperty() { return (SetProperty) addChild("setProperty"); }

    /// @return the `signature` children (typed).
    public java.util.List<Signature> getSignatureList() {
        java.util.List<Signature> r = new java.util.ArrayList<Signature>();
        for (XfaNode n : getChildren("signature")) { r.add((Signature) n); }
        return r;
    }
    /// Appends a new `signature` child.
    public Signature addSignature() { return (Signature) addChild("signature"); }

    /// @return the `signData` children (typed).
    public java.util.List<SignData> getSignDataList() {
        java.util.List<SignData> r = new java.util.ArrayList<SignData>();
        for (XfaNode n : getChildren("signData")) { r.add((SignData) n); }
        return r;
    }
    /// Appends a new `signData` child.
    public SignData addSignData() { return (SignData) addChild("signData"); }

    /// @return the `signing` children (typed).
    public java.util.List<Signing> getSigningList() {
        java.util.List<Signing> r = new java.util.ArrayList<Signing>();
        for (XfaNode n : getChildren("signing")) { r.add((Signing) n); }
        return r;
    }
    /// Appends a new `signing` child.
    public Signing addSigning() { return (Signing) addChild("signing"); }

    /// @return the `solid` children (typed).
    public java.util.List<Solid> getSolidList() {
        java.util.List<Solid> r = new java.util.ArrayList<Solid>();
        for (XfaNode n : getChildren("solid")) { r.add((Solid) n); }
        return r;
    }
    /// Appends a new `solid` child.
    public Solid addSolid() { return (Solid) addChild("solid"); }

    /// @return the `speak` children (typed).
    public java.util.List<Speak> getSpeakList() {
        java.util.List<Speak> r = new java.util.ArrayList<Speak>();
        for (XfaNode n : getChildren("speak")) { r.add((Speak) n); }
        return r;
    }
    /// Appends a new `speak` child.
    public Speak addSpeak() { return (Speak) addChild("speak"); }

    /// @return the `stipple` children (typed).
    public java.util.List<Stipple> getStippleList() {
        java.util.List<Stipple> r = new java.util.ArrayList<Stipple>();
        for (XfaNode n : getChildren("stipple")) { r.add((Stipple) n); }
        return r;
    }
    /// Appends a new `stipple` child.
    public Stipple addStipple() { return (Stipple) addChild("stipple"); }

    /// @return the `subform` children (typed).
    public java.util.List<Subform> getSubformList() {
        java.util.List<Subform> r = new java.util.ArrayList<Subform>();
        for (XfaNode n : getChildren("subform")) { r.add((Subform) n); }
        return r;
    }
    /// Appends a new `subform` child.
    public Subform addSubform() { return (Subform) addChild("subform"); }

    /// @return the `subformSet` children (typed).
    public java.util.List<SubformSet> getSubformSetList() {
        java.util.List<SubformSet> r = new java.util.ArrayList<SubformSet>();
        for (XfaNode n : getChildren("subformSet")) { r.add((SubformSet) n); }
        return r;
    }
    /// Appends a new `subformSet` child.
    public SubformSet addSubformSet() { return (SubformSet) addChild("subformSet"); }

    /// @return the `subjectDN` children (typed).
    public java.util.List<SubjectDN> getSubjectDNList() {
        java.util.List<SubjectDN> r = new java.util.ArrayList<SubjectDN>();
        for (XfaNode n : getChildren("subjectDN")) { r.add((SubjectDN) n); }
        return r;
    }
    /// Appends a new `subjectDN` child.
    public SubjectDN addSubjectDN() { return (SubjectDN) addChild("subjectDN"); }

    /// @return the `subjectDNs` children (typed).
    public java.util.List<SubjectDNs> getSubjectDNsList() {
        java.util.List<SubjectDNs> r = new java.util.ArrayList<SubjectDNs>();
        for (XfaNode n : getChildren("subjectDNs")) { r.add((SubjectDNs) n); }
        return r;
    }
    /// Appends a new `subjectDNs` child.
    public SubjectDNs addSubjectDNs() { return (SubjectDNs) addChild("subjectDNs"); }

    /// @return the `submit` children (typed).
    public java.util.List<Submit> getSubmitList() {
        java.util.List<Submit> r = new java.util.ArrayList<Submit>();
        for (XfaNode n : getChildren("submit")) { r.add((Submit) n); }
        return r;
    }
    /// Appends a new `submit` child.
    public Submit addSubmit() { return (Submit) addChild("submit"); }

    /// @return the `text` children (typed).
    public java.util.List<Text> getTextList() {
        java.util.List<Text> r = new java.util.ArrayList<Text>();
        for (XfaNode n : getChildren("text")) { r.add((Text) n); }
        return r;
    }
    /// Appends a new `text` child.
    public Text addText() { return (Text) addChild("text"); }

    /// @return the `textEdit` children (typed).
    public java.util.List<TextEdit> getTextEditList() {
        java.util.List<TextEdit> r = new java.util.ArrayList<TextEdit>();
        for (XfaNode n : getChildren("textEdit")) { r.add((TextEdit) n); }
        return r;
    }
    /// Appends a new `textEdit` child.
    public TextEdit addTextEdit() { return (TextEdit) addChild("textEdit"); }

    /// @return the `time` children (typed).
    public java.util.List<Time> getTimeList() {
        java.util.List<Time> r = new java.util.ArrayList<Time>();
        for (XfaNode n : getChildren("time")) { r.add((Time) n); }
        return r;
    }
    /// Appends a new `time` child.
    public Time addTime() { return (Time) addChild("time"); }

    /// @return the `timeStamp` children (typed).
    public java.util.List<TimeStamp> getTimeStampList() {
        java.util.List<TimeStamp> r = new java.util.ArrayList<TimeStamp>();
        for (XfaNode n : getChildren("timeStamp")) { r.add((TimeStamp) n); }
        return r;
    }
    /// Appends a new `timeStamp` child.
    public TimeStamp addTimeStamp() { return (TimeStamp) addChild("timeStamp"); }

    /// @return the `toolTip` children (typed).
    public java.util.List<ToolTip> getToolTipList() {
        java.util.List<ToolTip> r = new java.util.ArrayList<ToolTip>();
        for (XfaNode n : getChildren("toolTip")) { r.add((ToolTip) n); }
        return r;
    }
    /// Appends a new `toolTip` child.
    public ToolTip addToolTip() { return (ToolTip) addChild("toolTip"); }

    /// @return the `traversal` children (typed).
    public java.util.List<Traversal> getTraversalList() {
        java.util.List<Traversal> r = new java.util.ArrayList<Traversal>();
        for (XfaNode n : getChildren("traversal")) { r.add((Traversal) n); }
        return r;
    }
    /// Appends a new `traversal` child.
    public Traversal addTraversal() { return (Traversal) addChild("traversal"); }

    /// @return the `traverse` children (typed).
    public java.util.List<Traverse> getTraverseList() {
        java.util.List<Traverse> r = new java.util.ArrayList<Traverse>();
        for (XfaNode n : getChildren("traverse")) { r.add((Traverse) n); }
        return r;
    }
    /// Appends a new `traverse` child.
    public Traverse addTraverse() { return (Traverse) addChild("traverse"); }

    /// @return the `ui` children (typed).
    public java.util.List<Ui> getUiList() {
        java.util.List<Ui> r = new java.util.ArrayList<Ui>();
        for (XfaNode n : getChildren("ui")) { r.add((Ui) n); }
        return r;
    }
    /// Appends a new `ui` child.
    public Ui addUi() { return (Ui) addChild("ui"); }

    /// @return the `validate` children (typed).
    public java.util.List<Validate> getValidateList() {
        java.util.List<Validate> r = new java.util.ArrayList<Validate>();
        for (XfaNode n : getChildren("validate")) { r.add((Validate) n); }
        return r;
    }
    /// Appends a new `validate` child.
    public Validate addValidate() { return (Validate) addChild("validate"); }

    /// @return the `value` children (typed).
    public java.util.List<Value> getValueList() {
        java.util.List<Value> r = new java.util.ArrayList<Value>();
        for (XfaNode n : getChildren("value")) { r.add((Value) n); }
        return r;
    }
    /// Appends a new `value` child.
    public Value addValue() { return (Value) addChild("value"); }

    /// @return the `variables` children (typed).
    public java.util.List<Variables> getVariablesList() {
        java.util.List<Variables> r = new java.util.ArrayList<Variables>();
        for (XfaNode n : getChildren("variables")) { r.add((Variables) n); }
        return r;
    }
    /// Appends a new `variables` child.
    public Variables addVariables() { return (Variables) addChild("variables"); }
}
