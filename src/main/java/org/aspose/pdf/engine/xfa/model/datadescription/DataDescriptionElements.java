package org.aspose.pdf.engine.xfa.model.datadescription;

import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;

/**
 * Registry of generated typed element constructors for this XFA grammar
 * (element local name -> typed node).
 */
public final class DataDescriptionElements {

    private DataDescriptionElements() { }

    /** The grammar's (version-independent) target namespace. */
    public static final String NAMESPACE = "http://ns.adobe.com/data-description/";

    /** Number of generated typed element classes. */
    public static final int COUNT = 2;

    /**
     * Registers all typed element constructors.
     * @param reg the factory registry map
     */
    public static void registerAll(java.util.Map<String, XfaNodeFactory.Ctor> reg) {
        reg.put("dataDescription", DataDescription::new);
        reg.put("dd:group", Dd_group::new);
    }
}
