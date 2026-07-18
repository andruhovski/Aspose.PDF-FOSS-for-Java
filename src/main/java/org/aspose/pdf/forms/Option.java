package org.aspose.pdf.forms;

/// A single option in a choice field (ComboBox/ListBox).
///
/// Each option has a value (the export value) and a display name
/// (the text shown to the user). When the /Opt entry is a simple string,
/// both are identical.
///
public class Option {

    /// The export value.
    private final String value;

    /// The display name shown to the user.
    private final String displayName;

    /// Constructs an option with the given value and display name.
    ///
    /// @param value       the export value
    /// @param displayName the display name
    public Option(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /// Returns the export value of this option.
    ///
    /// @return the value
    public String getValue() {
        return value;
    }

    /// Returns the display name of this option.
    ///
    /// @return the display name
    public String getDisplayName() {
        return displayName;
    }

    /// Returns the display name if available, otherwise the value.
    ///
    /// @return string representation
    @Override
    public String toString() {
        return displayName != null ? displayName : value;
    }
}
