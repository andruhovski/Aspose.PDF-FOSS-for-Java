package org.aspose.pdf.forms;

import org.aspose.pdf.XForm;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/// Typed view over a form field's `/AP` appearance dictionary
/// (ISO 32000-1:2008 §12.5.5).
///
/// Per spec, `/AP` has up to three sub-entries:
///
///   - `/N` — Normal appearance (required when /AP is present)
///   - `/R` — Rollover appearance (optional)
///   - `/D` — Down appearance (optional)
///
/// Each sub-entry is either a single Form XObject stream (for single-state
/// widgets like text fields) or a sub-dictionary keyed by appearance-state
/// name (for multi-state widgets like checkboxes whose /N has keys
/// `"Off"` and `"Yes"`, or radio groups whose /N has keys for
/// each option name).
///
/// This is a live view: mutations to the underlying dictionary are visible
/// on the next access — there is no internal cache of streams or names.
public class AppearanceDictionary {

    private final PdfDictionary apDict;

    /// Wraps the given /AP dictionary. The caller is responsible for ensuring
    /// `apDict` is the actual /AP sub-dictionary of a field, not the
    /// field's own dictionary.
    ///
    /// @param apDict the /AP dictionary (must not be null)
    /// @throws IllegalArgumentException if `apDict` is null
    public AppearanceDictionary(PdfDictionary apDict) {
        if (apDict == null) {
            throw new IllegalArgumentException("apDict must not be null");
        }
        this.apDict = apDict;
    }

    /// Returns the set of state names available under `/N` (normal
    /// appearance) for multi-state widgets. For single-state widgets (where
    /// /AP/N is a stream rather than a dictionary) returns an empty set.
    ///
    /// @return the state names in insertion order (immutable view)
    public Set<String> getStateNames() {
        PdfBase n = apDict.get(PdfName.N);
        // PdfStream extends PdfDictionary, so check stream first to exclude it.
        if (n instanceof PdfDictionary && !(n instanceof PdfStream)) {
            Set<String> out = new LinkedHashSet<>();
            for (PdfName key : ((PdfDictionary) n).keySet()) {
                out.add(key.getName());
            }
            return out;
        }
        return Collections.emptySet();
    }

    /// Returns the normal appearance stream for the given state, or `null`
    /// if the state name is not present or if the field is single-state.
    ///
    /// @param stateName the appearance state name (e.g. `"Off"`,
    ///                  `"Yes"`, or a radio option name)
    /// @return the XForm wrapping the appearance stream, or null
    public XForm get(String stateName) {
        if (stateName == null) return null;
        PdfBase n = apDict.get(PdfName.N);
        if (n instanceof PdfDictionary && !(n instanceof PdfStream)) {
            PdfBase entry = ((PdfDictionary) n).get(PdfName.of(stateName));
            if (entry instanceof PdfStream) {
                return new XForm((PdfStream) entry, stateName, null);
            }
        }
        return null;
    }

    /// Returns the single normal appearance stream for a single-state widget
    /// (text fields, buttons, etc.), or `null` if the field is
    /// multi-state or has no /N entry.
    ///
    /// @return the XForm wrapping /AP/N (when it is a stream), or null
    public XForm getNormal() {
        PdfBase n = apDict.get(PdfName.N);
        if (n instanceof PdfStream) {
            return new XForm((PdfStream) n, "N", null);
        }
        return null;
    }

    /// Returns `true` when `/AP/N` is a dictionary (multi-state),
    /// `false` when it is a stream (single-state) or absent.
    ///
    /// @return whether this appearance is multi-state
    public boolean isMultiState() {
        PdfBase n = apDict.get(PdfName.N);
        return n instanceof PdfDictionary && !(n instanceof PdfStream);
    }

    /// Returns the underlying /AP PDF dictionary for engine-side use.
    ///
    /// @return the wrapped dictionary
    public PdfDictionary getPdfDictionary() {
        return apDict;
    }
}
