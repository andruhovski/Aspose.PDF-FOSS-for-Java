package org.aspose.pdf.engine.xfa.namespace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// Canonicalises XFA namespace URIs to their version-independent family.
///
/// XFA producers emit namespace URIs carrying the minor version they wrote,
/// e.g. `.../xfa-template/2.8/`, `.../xfa-template/3.3/`. This
/// library's typed element/attribute vocabulary targets the XFA 3.0 family; to
/// recognise elements across real-world XFA versions, name matching compares
/// the version-stripped family
/// (`http://www.xfa.org/schema/xfa-template/`). This is a deliberate,
/// documented tolerance. No schema files are bundled with this library.
public final class XfaNamespaces {

    private static final Pattern XFA_VERSIONED =
            Pattern.compile("^(http://www\\.xfa\\.org/schema/[^/]+/)\\d+(?:\\.\\d+)*/?$");

    private XfaNamespaces() { }

    /// Returns the version-independent family of an XFA namespace, or the input
    /// unchanged for non-XFA namespaces.
    ///
    /// @param ns a namespace URI (may be `null`)
    /// @return the canonical family URI (`""` for `null`)
    public static String canonical(String ns) {
        if (ns == null) {
            return "";
        }
        Matcher m = XFA_VERSIONED.matcher(ns);
        if (m.matches()) {
            return m.group(1);
        }
        return ns;
    }
}
