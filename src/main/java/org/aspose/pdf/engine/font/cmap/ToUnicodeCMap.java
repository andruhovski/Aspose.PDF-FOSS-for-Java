package org.aspose.pdf.engine.font.cmap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A parsed ToUnicode CMap (ISO 32000-1:2008, §9.10.3).
 * <p>
 * Maps character codes (CIDs or single-byte codes) to Unicode strings.
 * Supports both single-character and multi-character mappings (e.g., ligatures).
 * </p>
 */
public class ToUnicodeCMap {

    private static final Logger LOG = Logger.getLogger(ToUnicodeCMap.class.getName());

    private final Map<Integer, String> charToUnicode;

    /**
     * Creates a ToUnicodeCMap with the given mappings.
     *
     * @param mappings the character code to Unicode string mappings
     */
    public ToUnicodeCMap(Map<Integer, String> mappings) {
        this.charToUnicode = mappings != null
                ? new HashMap<>(mappings)
                : new HashMap<>();
    }

    /**
     * Looks up the Unicode string for the given character code.
     *
     * @param charCode the character code
     * @return the Unicode string, or null if not mapped
     */
    public String lookup(int charCode) {
        return charToUnicode.get(charCode);
    }

    /**
     * Returns whether the given character code is mapped.
     *
     * @param charCode the character code
     * @return true if mapped
     */
    public boolean contains(int charCode) {
        return charToUnicode.containsKey(charCode);
    }

    /**
     * Returns the number of mappings.
     *
     * @return the mapping count
     */
    public int size() {
        return charToUnicode.size();
    }

    /**
     * Returns an unmodifiable view of all mappings.
     *
     * @return the mappings
     */
    public Map<Integer, String> getMappings() {
        return Collections.unmodifiableMap(charToUnicode);
    }
}
