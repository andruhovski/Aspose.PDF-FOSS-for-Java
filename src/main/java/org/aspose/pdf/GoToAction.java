package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;

/// Go-To action — navigate to a destination within the document (ISO 32000-1:2008, §12.6.4.2).
public class GoToAction extends PdfAction {

    /// May be either [ExplicitDestination] or [NamedDestination].
    private IAppointment destination;

    /// Creates a GoToAction targeting a page with default XYZ destination.
    ///
    /// @param page the target page
    public GoToAction(Page page) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("GoTo"));
        ExplicitDestination dest = new XYZExplicitDestination(page, Double.NaN, Double.NaN, 0);
        this.destination = dest;
        actionDict.set(PdfName.of("D"), dest.toPdfArray());
    }

    /// Creates a GoToAction with no preset destination — used by callers that
    /// subsequently invoke [#setDestination(IAppointment)].
    public GoToAction() {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("GoTo"));
    }

    /// Creates a GoToAction with a specific destination.
    ///
    /// @param dest the explicit destination
    public GoToAction(ExplicitDestination dest) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("GoTo"));
        this.destination = dest;
        if (dest != null) {
            actionDict.set(PdfName.of("D"), dest.toPdfArray());
        }
    }

    /// Creates a GoToAction with a named destination.
    ///
    /// @param dest the named destination
    public GoToAction(NamedDestination dest) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("GoTo"));
        this.destination = dest;
        if (dest != null) {
            actionDict.set(PdfName.of("D"), dest.toCos());
        }
    }

    /// Parses a GoToAction from an existing dictionary.
    ///
    /// @param dict the action dictionary
    /// @param doc  the document for page resolution (may be null)
    /// @throws IOException if parsing fails
    public GoToAction(PdfDictionary dict, Document doc) throws IOException {
        this.actionDict = dict;
        PdfBase d = resolve(dict.get("D"));
        if (d instanceof PdfArray) {
            this.destination = ExplicitDestination.fromPdfArray((PdfArray) d, doc);
        } else if (d instanceof PdfString || d instanceof PdfName) {
            // Named destination
            String name = (d instanceof PdfString)
                    ? ((PdfString) d).getString()
                    : ((PdfName) d).getName();
            if (doc != null) {
                NamedDestinations nd = doc.getNamedDestinations();
                if (nd != null) this.destination = nd.get(name);
            }
        }
    }

    /// Returns the destination as an [ExplicitDestination] when one is set
    /// (either inline or already resolved during construction); for a
    /// [NamedDestination], returns the lazy-resolved explicit destination,
    /// or `null` if the name cannot be resolved.
    ///
    /// @return the resolved explicit destination, or null
    public ExplicitDestination getDestination() {
        if (destination instanceof ExplicitDestination) {
            return (ExplicitDestination) destination;
        }
        if (destination instanceof NamedDestination) {
            return ((NamedDestination) destination).resolve();
        }
        return null;
    }

    /// Returns the raw destination ([ExplicitDestination] or
    /// [NamedDestination]). Use this when the named-vs-explicit
    /// distinction matters.
    ///
    /// @return the appointment, or null
    public IAppointment getAppointment() {
        return destination;
    }

    /// Sets the destination (accepts both [ExplicitDestination] and
    /// [NamedDestination]).
    ///
    /// @param dest the destination
    public void setDestination(IAppointment dest) {
        this.destination = dest;
        if (dest instanceof ExplicitDestination) {
            actionDict.set(PdfName.of("D"), ((ExplicitDestination) dest).toPdfArray());
        } else if (dest instanceof NamedDestination) {
            actionDict.set(PdfName.of("D"), ((NamedDestination) dest).toCos());
        } else if (dest == null) {
            actionDict.remove(PdfName.of("D"));
        }
    }
}
