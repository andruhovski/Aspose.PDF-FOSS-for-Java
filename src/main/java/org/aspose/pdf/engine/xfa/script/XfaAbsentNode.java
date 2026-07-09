package org.aspose.pdf.engine.xfa.script;

import org.aspose.pdf.engine.script.js.runtime.JSObject;
import org.aspose.pdf.engine.script.js.runtime.Undefined;

/**
 * The XFA <b>absent node</b> (B3.5.1, the keystone) — a null-object {@link JSObject} returned when a
 * SOM expression ({@code xfa.resolveNode}/{@code this.resolveNode}/{@code .item}) or a dotted child
 * step ({@code a.b.c}) resolves to nothing.
 *
 * <p>XFA semantics: an unfound node is still an <i>object</i>, so reads of its value/presence are
 * benign empties and a further dotted step yields another absent node — scripts are written assuming
 * this ({@code xfa.resolveNode("missing").rawValue} and {@code a.b.c.rawValue} must not throw). The
 * JS engine, in contrast, would return {@code undefined}, whose property access throws "Cannot read
 * property X of undefined". This wrapper converts that large class of hard crashes into benign
 * empties:</p>
 * <ul>
 *   <li>value-like reads ({@code rawValue}/{@code value}/{@code formattedValue}/{@code name}/…) → {@code ""};</li>
 *   <li>numeric-like reads ({@code count}/{@code length}/{@code min}/{@code max}/{@code index}) → {@code 0};</li>
 *   <li>{@code isNull} → {@code true} (the idiomatic XFA absence test);</li>
 *   <li>any other property (a further dotted step, {@code nodes}, {@code parent}, …) → this same absent
 *       node, so {@code a.b.c.d} never throws;</li>
 *   <li>the node/list/instanceManager method surface ({@code resolveNode}, {@code nodes.append},
 *       {@code execEvent}, …) are benign no-ops so qualified calls do not raise "is not a function";</li>
 *   <li>writes are dropped (an absent node has nowhere to store a value).</li>
 * </ul>
 *
 * <p><b>Falsiness (the enabling primitive).</b> A JS object is always truthy, which would make
 * {@code while (n) n = n.parent} and {@code if (node.child)} spin/diverge on a null-object. So the
 * absent node opts into {@link JSObject#isFalsy()} → {@code true}: it is <i>falsy</i> in boolean
 * context, matching Acrobat's "missing node is falsy" while still answering property reads as empties.
 * That is what lets the chained-navigation no-throw coexist with terminating guard loops. One shared,
 * stateless instance per {@link XfaScriptHost}.</p>
 */
final class XfaAbsentNode extends JSObject {

    XfaAbsentNode(XfaScriptHost host, JSObject proto) {
        super(proto);
        setClassName("XFAObject");
        // Node / list / instanceManager method surface — benign no-ops so chained qualified calls on an
        // absent node never raise "X is not a function". (Loops can't hang: the absent node is falsy.)
        host.realm().method(this, "resolveNode", 1, (i, t, a) -> this);
        host.realm().method(this, "resolveNodes", 1, (i, t, a) -> host.realm().newArray());
        host.realm().method(this, "isPropertySpecified", 1, (i, t, a) -> Boolean.FALSE);
        host.realm().method(this, "addItem", 2, (i, t, a) -> Undefined.INSTANCE);
        host.realm().method(this, "clearItems", 0, (i, t, a) -> Undefined.INSTANCE);
        host.realm().method(this, "getDisplayItem", 1, (i, t, a) -> "");
        host.realm().method(this, "getSaveItem", 1, (i, t, a) -> "");
        host.realm().method(this, "boundItem", 1, (i, t, a) -> "");
        host.realm().method(this, "execEvent", 1, (i, t, a) -> Boolean.FALSE);
        host.realm().method(this, "append", 1, (i, t, a) -> Undefined.INSTANCE);
        host.realm().method(this, "insert", 2, (i, t, a) -> Undefined.INSTANCE);
        host.realm().method(this, "remove", 1, (i, t, a) -> Undefined.INSTANCE);
        host.realm().method(this, "item", 1, (i, t, a) -> this);
        for (String m : new String[]{"addInstance", "removeInstance", "setInstances",
                "insertInstance", "moveInstance"}) {
            host.realm().method(this, m, 1, (i, t, a) -> Undefined.INSTANCE);
        }
    }

    /** The absent node is falsy: {@code if (node)} / {@code while (node.child)} guards terminate. */
    @Override
    public boolean isFalsy() {
        return true;
    }

    @Override
    public Object get(String name) {
        switch (name) {
            case "rawValue":
            case "value":
            case "formattedValue":
            case "name":
            case "className":
            case "somExpression":
            case "access":
            case "color":
            case "fillColor":
                return "";
            case "presence":
                return "visible";
            case "isNull":
                return Boolean.TRUE;
            case "count":
            case "length":
            case "min":
            case "max":
            case "index":
            case "instanceIndex":
                return 0.0;
            case "selectedIndex":
                return -1.0;
            default:
                // a registered method (resolveNode, append, …) wins
                if (super.hasProperty(name)) {
                    return super.get(name);
                }
                // any further dotted step / nodes / parent / instanceManager stays within the null
                // object (safe: the absent node is falsy, so no loop on it can hang) — so a.b.c.d and
                // absent.nodes.append(...) never throw.
                return this;
        }
    }

    @Override
    public void put(String name, Object value) {
        // writes to an absent node are dropped (XFA absent-node semantics: nowhere to store).
    }
}
