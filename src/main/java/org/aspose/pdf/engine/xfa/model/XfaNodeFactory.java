package org.aspose.pdf.engine.xfa.model;

import org.aspose.pdf.engine.xfa.model.config.ConfigElements;
import org.aspose.pdf.engine.xfa.model.connectionset.ConnectionSetElements;
import org.aspose.pdf.engine.xfa.model.datadescription.DataDescriptionElements;
import org.aspose.pdf.engine.xfa.model.datasets.Datasets;
import org.aspose.pdf.engine.xfa.model.datasets.DatasetsElements;
import org.aspose.pdf.engine.xfa.model.localeset.LocaleSetElements;
import org.aspose.pdf.engine.xfa.model.sourceset.SourceSetElements;
import org.aspose.pdf.engine.xfa.model.template.XfaTemplateElements;
import org.aspose.pdf.engine.xfa.namespace.XfaNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Maps an XFA element to its typed {@link XfaNode} subclass, routed by the
 * element's namespace family. Each grammar (template, localeSet, sourceSet,
 * connectionSet, dataDescription, config, datasets/data) contributes a registry
 * keyed under its namespace (version-independent via {@link XfaNamespaces}).
 *
 * <p>Unknown namespaces or element names wrap to a plain {@link XfaNode}, so
 * foreign / open-content / user-data elements still participate in the typed
 * tree and round-trip.</p>
 */
public final class XfaNodeFactory {

    /** Constructor functional interface for a typed node. */
    public interface Ctor {
        /**
         * @param element backing element
         * @param parent  parent node
         * @return the typed node
         */
        XfaNode create(Element element, XfaNode parent);
    }

    /** namespace family -> (local name -> constructor). */
    private static final Map<String, Map<String, Ctor>> BY_NS = new HashMap<>();

    static {
        install(XfaTemplateElements.NAMESPACE, XfaTemplateElements::registerAll);
        install(LocaleSetElements.NAMESPACE, LocaleSetElements::registerAll);
        install(SourceSetElements.NAMESPACE, SourceSetElements::registerAll);
        install(ConnectionSetElements.NAMESPACE, ConnectionSetElements::registerAll);
        install(DataDescriptionElements.NAMESPACE, DataDescriptionElements::registerAll);
        install(ConfigElements.NAMESPACE, ConfigElements::registerAll);
        install(DatasetsElements.NAMESPACE, DatasetsElements::registerAll);
        // The <xfa:datasets> wrapper is not described by the XFA data model; register it here.
        register(DatasetsElements.NAMESPACE, "datasets", Datasets::new);
    }

    private XfaNodeFactory() { }

    private static void install(String namespace, Consumer<Map<String, Ctor>> registrar) {
        Map<String, Ctor> m = BY_NS.computeIfAbsent(
                XfaNamespaces.canonical(namespace), k -> new HashMap<>());
        registrar.accept(m);
    }

    /**
     * Registers (or overrides) a typed constructor for an element name in a namespace.
     *
     * @param namespace element namespace (any version; canonicalised)
     * @param localName element local name
     * @param ctor      constructor
     */
    public static void register(String namespace, String localName, Ctor ctor) {
        BY_NS.computeIfAbsent(XfaNamespaces.canonical(namespace), k -> new HashMap<>())
                .put(localName, ctor);
    }

    /**
     * Wraps a DOM element into its typed node (or a generic {@link XfaNode}).
     *
     * @param element the element
     * @param parent  parent node, or {@code null}
     * @return the typed node
     */
    public static XfaNode wrap(Element element, XfaNode parent) {
        String ns = XfaNamespaces.canonical(element.getNamespaceURI());
        Map<String, Ctor> m = BY_NS.get(ns);
        if (m != null) {
            String ln = element.getLocalName();
            if (ln == null) {
                ln = element.getNodeName();
            }
            Ctor c = m.get(ln);
            if (c != null) {
                return c.create(element, parent);
            }
        }
        return new XfaNode(element, parent);
    }

    /**
     * Loads a packet DOM into the typed tree (its root element).
     *
     * @param packetDoc the packet document
     * @return the typed root node, or {@code null} if the document has no root
     */
    public static XfaNode load(Document packetDoc) {
        if (packetDoc == null || packetDoc.getDocumentElement() == null) {
            return null;
        }
        return wrap(packetDoc.getDocumentElement(), null);
    }

    /** @return the number of registered namespace families. */
    public static int registeredNamespaceCount() {
        return BY_NS.size();
    }
}
