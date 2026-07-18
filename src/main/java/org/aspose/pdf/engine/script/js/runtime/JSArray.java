package org.aspose.pdf.engine.script.js.runtime;

/// An ECMAScript Array exotic object (ECMA-262 3rd ed., sec 15.4).
///
/// Maintains the special `length` invariant: assigning to an array
/// index ≥ length grows length; assigning a smaller `length` deletes
/// the out-of-range indexed elements.
public final class JSArray extends JSObject {

    /// Creates an empty array.
    ///
    /// @param arrayPrototype`Array.prototype`
    public JSArray(JSObject arrayPrototype) {
        super(arrayPrototype);
        setClassName("Array");
        define("length", 0.0, true, false, false);
    }

    /// @return the current array length.
    public long length() {
        Object v = getOwnProperty("length").value;
        return (long) ((Double) v).doubleValue();
    }

    /// Sets the array length directly (used by built-ins), pruning elements.
    ///
    /// @param len new length
    public void setLength(long len) {
        long old = length();
        for (long k = len; k < old; k++) {
            delete(Long.toString(k));
        }
        getOwnProperty("length").value = (double) len;
    }

    @Override
    public void put(String name, Object value) {
        if ("length".equals(name)) {
            long newLen = (long) Types.toNumberPrimitiveOrNaN(value);
            double d = (value instanceof Double) ? (Double) value : newLen;
            // Only finite non-negative integers are valid lengths.
            long len = (long) d;
            setLength(len);
            return;
        }
        long idx = arrayIndex(name);
        super.put(name, value);
        if (idx >= 0) {
            long len = length();
            if (idx >= len) {
                getOwnProperty("length").value = (double) (idx + 1);
            }
        }
    }

    /// Returns the array-index numeric value of a property name, or `-1`
    /// if it is not a canonical array index (sec 15.4: an unsigned 32-bit
    /// integer whose string form round-trips, excluding 2^32-1).
    ///
    /// @param name property name
    /// @return the index, or `-1`
    public static long arrayIndex(String name) {
        if (name.isEmpty() || name.length() > 10) {
            return -1;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
        }
        if (name.length() > 1 && name.charAt(0) == '0') {
            return -1; // no leading zeros
        }
        long v = Long.parseLong(name);
        if (v >= 4294967295L) {
            return -1;
        }
        return v;
    }
}
