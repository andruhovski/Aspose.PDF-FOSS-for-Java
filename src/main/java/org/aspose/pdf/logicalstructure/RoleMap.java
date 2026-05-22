package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps custom structure type names to standard types
 * (ISO 32000-1:2008, §14.7.3).
 *
 * <p>The /RoleMap entry in StructTreeRoot maps non-standard structure type names
 * (e.g., "MyParagraph") to standard ones (e.g., "P").</p>
 */
public class RoleMap {

    private final Map<String, StructureTypeStandard> map = new LinkedHashMap<>();

    /**
     * Parses a RoleMap from a COS dictionary.
     *
     * @param roleMapDict the /RoleMap dictionary (may be null)
     * @return the parsed role map
     */
    public static RoleMap parse(COSDictionary roleMapDict) {
        RoleMap rm = new RoleMap();
        if (roleMapDict == null) return rm;
        for (COSName key : roleMapDict.keySet()) {
            COSBase val = roleMapDict.get(key);
            if (val instanceof COSName) {
                rm.map.put(key.getName(),
                        StructureTypeStandard.fromName(((COSName) val).getName()));
            }
        }
        return rm;
    }

    /**
     * Resolves a type name through the role map.
     * If the name is a custom type with a mapping, returns the mapped standard type.
     * Otherwise returns the type as-is.
     *
     * @param typeName the type name to resolve
     * @return the resolved standard type
     */
    public StructureTypeStandard resolve(String typeName) {
        StructureTypeStandard mapped = map.get(typeName);
        return mapped != null ? mapped : StructureTypeStandard.fromName(typeName);
    }

    /**
     * Returns an unmodifiable view of the role map entries.
     *
     * @return the map from custom names to standard types
     */
    public Map<String, StructureTypeStandard> getMap() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the number of mappings.
     *
     * @return the map size
     */
    public int size() { return map.size(); }
}
