package org.aspose.pdf.engine.xfa.model.sourceset;

import org.aspose.pdf.engine.xfa.model.XfaNodeFactory;

/**
 * Registry of generated typed element constructors for this XFA grammar
 * (element local name -> typed node).
 */
public final class SourceSetElements {

    private SourceSetElements() { }

    /** The grammar's (version-independent) target namespace. */
    public static final String NAMESPACE = "http://www.xfa.org/schema/xfa-source-set/";

    /** Number of generated typed element classes. */
    public static final int COUNT = 19;

    /**
     * Registers all typed element constructors.
     * @param reg the factory registry map
     */
    public static void registerAll(java.util.Map<String, XfaNodeFactory.Ctor> reg) {
        reg.put("bind", Bind::new);
        reg.put("boolean", Boolean::new);
        reg.put("command", Command::new);
        reg.put("connect", Connect::new);
        reg.put("connectString", ConnectString::new);
        reg.put("delete", Delete::new);
        reg.put("extras", Extras::new);
        reg.put("insert", Insert::new);
        reg.put("integer", Integer::new);
        reg.put("map", Map::new);
        reg.put("password", Password::new);
        reg.put("query", Query::new);
        reg.put("recordSet", RecordSet::new);
        reg.put("select", Select::new);
        reg.put("source", Source::new);
        reg.put("sourceSet", SourceSet::new);
        reg.put("text", Text::new);
        reg.put("update", Update::new);
        reg.put("user", User::new);
    }
}
