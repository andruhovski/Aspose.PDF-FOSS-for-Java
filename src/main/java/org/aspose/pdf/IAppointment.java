package org.aspose.pdf;

/**
 * Marker interface for PDF destinations — either an inline
 * {@link ExplicitDestination} (page + coordinates) or a
 * {@link NamedDestination} (name resolved through the document's name tree
 * at use time, ISO 32000-1:2008 §12.3.2.3).
 *
 * <p>Both forms are interchangeable as the target of {@link GoToAction} or
 * {@link OutlineItemCollection}; consumers that need to know which form they
 * received should test with {@code instanceof}.</p>
 */
public interface IAppointment {
}
