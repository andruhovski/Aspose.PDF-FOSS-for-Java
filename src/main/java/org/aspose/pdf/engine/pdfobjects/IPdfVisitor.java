package org.aspose.pdf.engine.pdfobjects;

/// Visitor interface for type-safe traversal of PDF object graphs.
///
/// Implements the Visitor pattern (GoF) to avoid chains of `instanceof` checks.
/// Each PDF object type calls the corresponding `visit*` method.
///
/// @param <T> the return type of the visitor methods
public interface IPdfVisitor<T> {

    /// Visit a boolean object.
    ///
    /// @param obj the boolean object
    /// @return visitor result
    T visitBoolean(PdfBoolean obj);

    /// Visit an integer object.
    ///
    /// @param obj the integer object
    /// @return visitor result
    T visitInteger(PdfInteger obj);

    /// Visit a float object.
    ///
    /// @param obj the float object
    /// @return visitor result
    T visitFloat(PdfFloat obj);

    /// Visit a name object.
    ///
    /// @param obj the name object
    /// @return visitor result
    T visitName(PdfName obj);

    /// Visit a string object.
    ///
    /// @param obj the string object
    /// @return visitor result
    T visitString(PdfString obj);

    /// Visit a null object.
    ///
    /// @param obj the null object
    /// @return visitor result
    T visitNull(PdfNull obj);

    /// Visit an array object.
    ///
    /// @param obj the array object
    /// @return visitor result
    T visitArray(PdfArray obj);

    /// Visit a dictionary object.
    ///
    /// @param obj the dictionary object
    /// @return visitor result
    T visitDictionary(PdfDictionary obj);

    /// Visit a stream object.
    ///
    /// @param obj the stream object
    /// @return visitor result
    T visitStream(PdfStream obj);
}
