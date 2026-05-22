package org.aspose.pdf.facades;

/**
 * AcroForm field types recognised by the {@link FormEditor} facade. Mirrors
 * {@code Aspose.Pdf.Facades.FieldType}.
 */
public enum FieldType {
    /** Single-line or multi-line text input. */
    Text,
    /** Push button (action). */
    PushButton,
    /** Reset-form button (uses Reset action). */
    Reset,
    /** Submit-form button (uses Submit action). */
    Submit,
    /** Two-state toggle. */
    CheckBox,
    /** One-of-N radio group. */
    RadioButton,
    /** Drop-down combo box (single selection from list). */
    ComboBox,
    /** Multi-line list box (single or multi selection). */
    ListBox,
    /** Image-bearing widget (typically a button with an icon). */
    Image,
    /** Numeric-only text input. */
    Numeric,
    /** Barcode field (rendered glyph series). */
    Barcode,
    /** Signature placeholder. */
    Signature,
    /** Field with no recognisable type (or unsupported subtype). */
    InvalidName
}
