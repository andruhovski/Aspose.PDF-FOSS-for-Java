package org.aspose.pdf.engine.script.js.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// A native ECMAScript object: an ordered map of named properties plus a
/// prototype link (ECMA-262 3rd ed., sec 8.6).
///
/// Property attributes model the ES3 internal attributes: `writable`
/// (not ReadOnly), `enumerable` (not DontEnum) and `configurable`
/// (not DontDelete). Insertion order is preserved to give `for-in` a
/// deterministic order.
public class JSObject {

    /// A single property slot with its ES3 attributes.
    public static final class Property {
        public Object value;
        public boolean writable;
        public boolean enumerable;
        public boolean configurable;

        Property(Object value, boolean writable, boolean enumerable, boolean configurable) {
            this.value = value;
            this.writable = writable;
            this.enumerable = enumerable;
            this.configurable = configurable;
        }
    }

    private final Map<String, Property> properties = new LinkedHashMap<>();
    private JSObject prototype;
    private String className = "Object";

    /// Optional primitive value wrapped by this object (for Number/String/Boolean/Date wrappers).
    public Object primitiveValue;

    /// Creates an object with no prototype.
    public JSObject() { }

    /// Creates an object with the given prototype.
    ///
    /// @param prototype the prototype object (may be `null`)
    public JSObject(JSObject prototype) {
        this.prototype = prototype;
    }

    /// @return the `[[Prototype]]` (may be `null`).
    public JSObject getPrototype() {
        return prototype;
    }

    /// Sets the prototype link.
    ///
    /// @param prototype new prototype (may be `null`)
    public void setPrototype(JSObject prototype) {
        this.prototype = prototype;
    }

    /// @return the `[[Class]]` string used by `Object.prototype.toString`.
    public String getClassName() {
        return className;
    }

    /// Sets the `[[Class]]` string.
    ///
    /// @param className class name
    public void setClassName(String className) {
        this.className = className;
    }

    /// Whether this object is **falsy** in a boolean context. Per ECMA-262 §9.2 a JS object is always
    /// truthy, so this returns `false` for every standard object and the engine's behaviour is
    /// unchanged. It is a host extension point: a null-object that must behave like "absent" (falsy,
    /// so `if (node) …` / `while (node.child) …` guards work) overrides this to return
    /// `true`. Inert for all built-in and user JS objects; the only opt-in is the XFA absent node.
    ///
    /// @return `true` if `ToBoolean(this)` should be `false`
    public boolean isFalsy() {
        return false;
    }

    /* --------------------------- [[Get]] ----------------------------- */

    /// `[[Get]]`: resolves a property along the prototype chain.
    ///
    /// @param name property name
    /// @return the value, or [Undefined#INSTANCE] if absent
    public Object get(String name) {
        JSObject o = this;
        while (o != null) {
            Property p = o.properties.get(name);
            if (p != null) {
                return p.value;
            }
            o = o.prototype;
        }
        return Undefined.INSTANCE;
    }

    /// @return the own property slot or `null`.
    public Property getOwnProperty(String name) {
        return properties.get(name);
    }

    /// @return `true` if this object (own) has the property.
    public boolean hasOwnProperty(String name) {
        return properties.containsKey(name);
    }

    /// `[[HasProperty]]`: own or inherited.
    public boolean hasProperty(String name) {
        JSObject o = this;
        while (o != null) {
            if (o.properties.containsKey(name)) {
                return true;
            }
            o = o.prototype;
        }
        return false;
    }

    /* --------------------------- [[Put]] ----------------------------- */

    /// `[[Put]]`: assigns a property honouring ReadOnly on the chain.
    ///
    /// @param name  property name
    /// @param value value to store
    public void put(String name, Object value) {
        Property own = properties.get(name);
        if (own != null) {
            if (own.writable) {
                own.value = value;
            }
            return;
        }
        // CanPut check along prototype chain for an inherited read-only property.
        JSObject o = prototype;
        while (o != null) {
            Property p = o.properties.get(name);
            if (p != null) {
                if (!p.writable) {
                    return; // inherited read-only blocks the put
                }
                break;
            }
            o = o.prototype;
        }
        properties.put(name, new Property(value, true, true, true));
    }

    /// Defines or replaces an own property with explicit attributes.
    ///
    /// @param name         property name
    /// @param value        value
    /// @param writable     writable attribute
    /// @param enumerable   enumerable attribute
    /// @param configurable configurable attribute
    public void define(String name, Object value, boolean writable,
                       boolean enumerable, boolean configurable) {
        properties.put(name, new Property(value, writable, enumerable, configurable));
    }

    /// Defines a non-enumerable, writable, configurable property (typical for
    /// built-in methods and internal slots).
    ///
    /// @param name  property name
    /// @param value value
    public void defineHidden(String name, Object value) {
        properties.put(name, new Property(value, true, false, true));
    }

    /// `[[Delete]]`: removes a configurable own property.
    public boolean delete(String name) {
        Property p = properties.get(name);
        if (p == null) {
            return true;
        }
        if (!p.configurable) {
            return false;
        }
        properties.remove(name);
        return true;
    }

    /// @return own enumerable property names, in insertion order.
    public List<String> ownEnumerableKeys() {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, Property> e : properties.entrySet()) {
            if (e.getValue().enumerable) {
                keys.add(e.getKey());
            }
        }
        return keys;
    }

    /// @return all own property names, in insertion order.
    public List<String> ownKeys() {
        return new ArrayList<>(properties.keySet());
    }

    @Override
    public String toString() {
        return "[object " + className + "]";
    }
}
