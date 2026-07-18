package org.aspose.pdf.forms;

import org.aspose.pdf.Color;

import java.util.logging.Logger;

/// Customizes the visual appearance of a digital signature field in a PDF document.
///
/// Controls which information labels are displayed (contact, reason, location, date),
/// their label text, font properties, and colors. Applied to a [Signature]
/// via [Signature#setCustomAppearance(SignatureCustomAppearance)].
///
public class SignatureCustomAppearance {

    private static final Logger LOG = Logger.getLogger(SignatureCustomAppearance.class.getName());

    private String contactInfoLabel = "Contact:";
    private String reasonLabel = "Reason:";
    private String locationLabel = "Location:";
    private String dateSignedLabel = "Date:";
    private boolean showContactInfo = true;
    private boolean showReason = true;
    private boolean showLocation = true;
    private String dateTimeFormat = "yyyy.MM.dd HH:mm:ss";
    private Color foregroundColor;
    private String fontFamilyName;
    private double fontSize = 10;

    /// Creates a new SignatureCustomAppearance with default settings.
    public SignatureCustomAppearance() {
    }

    /// Returns the label text displayed before the contact info.
    ///
    /// @return the contact info label
    public String getContactInfoLabel() {
        return contactInfoLabel;
    }

    /// Sets the label text displayed before the contact info.
    ///
    /// @param contactInfoLabel the contact info label
    public void setContactInfoLabel(String contactInfoLabel) {
        this.contactInfoLabel = contactInfoLabel;
    }

    /// Returns the label text displayed before the signing reason.
    ///
    /// @return the reason label
    public String getReasonLabel() {
        return reasonLabel;
    }

    /// Sets the label text displayed before the signing reason.
    ///
    /// @param reasonLabel the reason label
    public void setReasonLabel(String reasonLabel) {
        this.reasonLabel = reasonLabel;
    }

    /// Returns the label text displayed before the signing location.
    ///
    /// @return the location label
    public String getLocationLabel() {
        return locationLabel;
    }

    /// Sets the label text displayed before the signing location.
    ///
    /// @param locationLabel the location label
    public void setLocationLabel(String locationLabel) {
        this.locationLabel = locationLabel;
    }

    /// Returns the label text displayed before the signing date.
    ///
    /// @return the date signed label
    public String getDateSignedLabel() {
        return dateSignedLabel;
    }

    /// Sets the label text displayed before the signing date.
    ///
    /// @param dateSignedLabel the date signed label
    public void setDateSignedLabel(String dateSignedLabel) {
        this.dateSignedLabel = dateSignedLabel;
    }

    /// Returns whether the contact info line is shown in the signature appearance.
    ///
    /// @return true if contact info is shown
    public boolean isShowContactInfo() {
        return showContactInfo;
    }

    /// Sets whether the contact info line is shown in the signature appearance.
    ///
    /// @param showContactInfo true to show contact info
    public void setShowContactInfo(boolean showContactInfo) {
        this.showContactInfo = showContactInfo;
    }

    /// Returns whether the reason line is shown in the signature appearance.
    ///
    /// @return true if reason is shown
    public boolean isShowReason() {
        return showReason;
    }

    /// Sets whether the reason line is shown in the signature appearance.
    ///
    /// @param showReason true to show reason
    public void setShowReason(boolean showReason) {
        this.showReason = showReason;
    }

    /// Returns whether the location line is shown in the signature appearance.
    ///
    /// @return true if location is shown
    public boolean isShowLocation() {
        return showLocation;
    }

    /// Sets whether the location line is shown in the signature appearance.
    ///
    /// @param showLocation true to show location
    public void setShowLocation(boolean showLocation) {
        this.showLocation = showLocation;
    }

    /// Returns the date/time format pattern used to display the signing date.
    ///
    /// @return the date/time format string (e.g. "yyyy.MM.dd HH:mm:ss")
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    /// Sets the date/time format pattern used to display the signing date.
    ///
    /// @param dateTimeFormat the date/time format string
    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    /// Returns the foreground color used for text in the signature appearance.
    ///
    /// @return the foreground color, or null for default
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /// Sets the foreground color used for text in the signature appearance.
    ///
    /// @param foregroundColor the foreground color
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    /// Returns the font family name used in the signature appearance.
    ///
    /// @return the font family name, or null for default
    public String getFontFamilyName() {
        return fontFamilyName;
    }

    /// Sets the font family name used in the signature appearance.
    ///
    /// @param fontFamilyName the font family name
    public void setFontFamilyName(String fontFamilyName) {
        this.fontFamilyName = fontFamilyName;
    }

    /// Returns the font size used in the signature appearance.
    ///
    /// @return the font size in points
    public double getFontSize() {
        return fontSize;
    }

    /// Sets the font size used in the signature appearance.
    ///
    /// @param fontSize the font size in points
    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }
}
