package org.aspose.pdf.engine.cos;

/**
 * Visitor interface for type-safe traversal of COS object graphs.
 * <p>
 * Implements the Visitor pattern (GoF) to avoid chains of {@code instanceof} checks.
 * Each COS object type calls the corresponding {@code visit*} method.
 * </p>
 *
 * @param <T> the return type of the visitor methods
 */
public interface ICOSVisitor<T> {

    /**
     * Visit a boolean object.
     *
     * @param obj the boolean object
     * @return visitor result
     */
    T visitBoolean(COSBoolean obj);

    /**
     * Visit an integer object.
     *
     * @param obj the integer object
     * @return visitor result
     */
    T visitInteger(COSInteger obj);

    /**
     * Visit a float object.
     *
     * @param obj the float object
     * @return visitor result
     */
    T visitFloat(COSFloat obj);

    /**
     * Visit a name object.
     *
     * @param obj the name object
     * @return visitor result
     */
    T visitName(COSName obj);

    /**
     * Visit a string object.
     *
     * @param obj the string object
     * @return visitor result
     */
    T visitString(COSString obj);

    /**
     * Visit a null object.
     *
     * @param obj the null object
     * @return visitor result
     */
    T visitNull(COSNull obj);

    /**
     * Visit an array object.
     *
     * @param obj the array object
     * @return visitor result
     */
    T visitArray(COSArray obj);

    /**
     * Visit a dictionary object.
     *
     * @param obj the dictionary object
     * @return visitor result
     */
    T visitDictionary(COSDictionary obj);

    /**
     * Visit a stream object.
     *
     * @param obj the stream object
     * @return visitor result
     */
    T visitStream(COSStream obj);
}
