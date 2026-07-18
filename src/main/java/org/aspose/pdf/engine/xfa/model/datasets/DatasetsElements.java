package org.aspose.pdf.engine.xfa.model.datasets;

import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;

/// Registry of generated typed element constructors for this XFA grammar
/// (element local name -> typed node).
public final class DatasetsElements {

    private DatasetsElements() { }

    /// The grammar's (version-independent) target namespace.
    public static final String NAMESPACE = "http://www.xfa.org/schema/xfa-data/";

    /// Number of generated typed element classes.
    public static final int COUNT = 1;

    /// Registers all typed element constructors.
    /// @param reg the factory registry map
    public static void registerAll(java.util.Map<String, XfaNodeFactory.Ctor> reg) {
        reg.put("data", Data::new);
    }
}
