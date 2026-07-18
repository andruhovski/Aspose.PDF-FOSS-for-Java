package org.aspose.pdf;

import org.aspose.pdf.forms.Field;
import org.aspose.pdf.forms.RadioButtonOptionField;

/// [BaseParagraph] adapter that lets form-field widgets participate in
/// paragraph-based collections such as [Cell#getParagraphs()],
/// [Page#getParagraphs()] and [FloatingBox#getParagraphs()].
///
/// The wrapped object is one of:
///
///   - a [Field] subclass (TextBoxField, CheckboxField, ButtonField, …)
///   - a [RadioButtonOptionField] (which does not extend Field today)
///
/// The adapter only carries the wrapped reference and the standard
/// [BaseParagraph] layout properties (margin, alignment, in-line). The
/// actual widget positioning happens at save/render time when the layout
/// pipeline visits the cell's paragraphs and discovers a FormFieldParagraph —
/// pipelines that do not yet know about this adapter simply leave the widget at
/// its existing `/Rect` (annotations are still rendered via the page's
/// `/Annots` array).
///
/// Closes the structural blocker surfaced in Sprint 18 Part B where C# tests
/// written as `cell.Paragraphs.Add(option)` could not be ported because
/// `RadioButtonOptionField` extends [Object] rather than
/// [BaseParagraph].
public class FormFieldParagraph extends BaseParagraph {

    private final Object field;

    /// Wraps a [Field] subclass (e.g. TextBoxField, CheckboxField).
    ///
    /// @param field the field to embed (must not be null)
    public FormFieldParagraph(Field field) {
        if (field == null) {
            throw new IllegalArgumentException("field must not be null");
        }
        this.field = field;
    }

    /// Wraps a [RadioButtonOptionField] option.
    ///
    /// @param option the option widget to embed (must not be null)
    public FormFieldParagraph(RadioButtonOptionField option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not be null");
        }
        this.field = option;
    }

    /// Returns the wrapped form field. Callers should use `instanceof`
    /// to discriminate between [Field] subclasses and
    /// [RadioButtonOptionField].
    ///
    /// @return the wrapped field reference
    public Object getField() {
        return field;
    }

    /// Convenience: returns the wrapped object as a [Field], or null if
    /// it is a [RadioButtonOptionField] (which does not extend Field).
    ///
    /// @return the Field instance, or null
    public Field asField() {
        return (field instanceof Field) ? (Field) field : null;
    }

    /// Convenience: returns the wrapped object as a [RadioButtonOptionField],
    /// or null if it is a [Field] subclass.
    ///
    /// @return the option, or null
    public RadioButtonOptionField asOption() {
        return (field instanceof RadioButtonOptionField) ? (RadioButtonOptionField) field : null;
    }
}
