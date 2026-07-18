package org.aspose.pdf.operators;

import org.aspose.pdf.Operator;

/// Visitor interface for traversing an `OperatorCollection`.
///
/// Implementations are passed to
/// [org.aspose.pdf.OperatorCollection#accept(IOperatorSelector)], which
/// invokes [#visit(Operator)] once for every operator in the collection
/// (ISO 32000-1:2008, §8.2). Mirrors the Aspose.PDF for .NET
/// `IOperatorSelector` visitor.
///
public interface IOperatorSelector {

    /// Called by `OperatorCollection.accept` for each operator in turn.
    ///
    /// @param operator the operator being visited
    void visit(Operator operator);
}
