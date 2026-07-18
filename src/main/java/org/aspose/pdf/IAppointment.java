package org.aspose.pdf;

/// Marker interface for PDF destinations — either an inline
/// [ExplicitDestination] (page + coordinates) or a
/// [NamedDestination] (name resolved through the document's name tree
/// at use time, ISO 32000-1:2008 §12.3.2.3).
///
/// Both forms are interchangeable as the target of [GoToAction] or
/// [OutlineItemCollection]; consumers that need to know which form they
/// received should test with `instanceof`.
public interface IAppointment {
}
