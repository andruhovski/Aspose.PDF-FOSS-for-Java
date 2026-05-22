package org.aspose.pdf.facades;

/**
 * Represents the access privileges (permissions) for a PDF document
 * (ISO 32000-1:2008, Table 22).
 * <p>
 * Permission flags control what operations are allowed when a document
 * is opened with the user password. The owner password always grants full access.
 * </p>
 */
public class DocumentPrivilege implements Comparable<Object> {

    /** Bit 3: Allow printing. */
    public static final int ALLOW_PRINT = 1 << 2;

    /** Bit 4: Allow content modification. */
    public static final int ALLOW_MODIFY_CONTENTS = 1 << 3;

    /** Bit 5: Allow text/graphic extraction. */
    public static final int ALLOW_COPY = 1 << 4;

    /** Bit 6: Allow adding/modifying annotations and form fields. */
    public static final int ALLOW_MODIFY_ANNOTATIONS = 1 << 5;

    /** Bit 9: Allow filling in form fields (R>=3). */
    public static final int ALLOW_FILL_IN = 1 << 8;

    /** Bit 10: Allow text extraction for accessibility (R>=3). */
    public static final int ALLOW_SCREEN_READERS = 1 << 9;

    /** Bit 11: Allow document assembly (R>=3). */
    public static final int ALLOW_ASSEMBLY = 1 << 10;

    /** Bit 12: Allow high-quality printing (R>=3). */
    public static final int ALLOW_DEGRADED_PRINTING = 1 << 11;

    private int value;

    /**
     * Creates a DocumentPrivilege with the given permission flags.
     *
     * @param value the permission bitmask
     */
    public DocumentPrivilege(int value) {
        this.value = value;
    }

    /**
     * Returns a privilege set that allows all operations.
     *
     * @return a DocumentPrivilege with all permissions enabled
     */
    public static DocumentPrivilege getAllowAll() {
        return new DocumentPrivilege(
            ALLOW_PRINT | ALLOW_MODIFY_CONTENTS | ALLOW_COPY |
            ALLOW_MODIFY_ANNOTATIONS | ALLOW_FILL_IN | ALLOW_SCREEN_READERS |
            ALLOW_ASSEMBLY | ALLOW_DEGRADED_PRINTING
        );
    }

    /**
     * Returns a privilege set that forbids all operations.
     *
     * @return a DocumentPrivilege with no permissions
     */
    public static DocumentPrivilege getForbidAll() {
        return new DocumentPrivilege(0);
    }

    /**
     * Returns the permission bitmask value.
     *
     * @return the permission flags
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the permission bitmask value.
     *
     * @param value the permission flags
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Returns whether printing is allowed.
     *
     * @return true if printing is allowed
     */
    public boolean isAllowPrint() {
        return (value & ALLOW_PRINT) != 0;
    }

    /**
     * Sets whether printing is allowed.
     *
     * @param allow true to allow printing
     */
    public void setAllowPrint(boolean allow) {
        if (allow) value |= ALLOW_PRINT;
        else value &= ~ALLOW_PRINT;
    }

    /**
     * Returns whether content modification is allowed.
     *
     * @return true if modification is allowed
     */
    public boolean isAllowModifyContents() {
        return (value & ALLOW_MODIFY_CONTENTS) != 0;
    }

    /**
     * Sets whether content modification is allowed.
     *
     * @param allow true to allow modification
     */
    public void setAllowModifyContents(boolean allow) {
        if (allow) value |= ALLOW_MODIFY_CONTENTS;
        else value &= ~ALLOW_MODIFY_CONTENTS;
    }

    /**
     * Returns whether copying content is allowed.
     *
     * @return true if copying is allowed
     */
    public boolean isAllowCopy() {
        return (value & ALLOW_COPY) != 0;
    }

    /**
     * Sets whether copying content is allowed.
     *
     * @param allow true to allow copying
     */
    public void setAllowCopy(boolean allow) {
        if (allow) value |= ALLOW_COPY;
        else value &= ~ALLOW_COPY;
    }

    /**
     * Returns whether modifying annotations is allowed.
     *
     * @return true if annotation modification is allowed
     */
    public boolean isAllowModifyAnnotations() {
        return (value & ALLOW_MODIFY_ANNOTATIONS) != 0;
    }

    /**
     * Sets whether modifying annotations is allowed.
     *
     * @param allow true to allow annotation modification
     */
    public void setAllowModifyAnnotations(boolean allow) {
        if (allow) value |= ALLOW_MODIFY_ANNOTATIONS;
        else value &= ~ALLOW_MODIFY_ANNOTATIONS;
    }

    /**
     * Returns whether filling in forms is allowed.
     *
     * @return true if form filling is allowed
     */
    public boolean isAllowFillIn() {
        return (value & ALLOW_FILL_IN) != 0;
    }

    /**
     * Sets whether filling in forms is allowed.
     *
     * @param allow true to allow form filling
     */
    public void setAllowFillIn(boolean allow) {
        if (allow) value |= ALLOW_FILL_IN;
        else value &= ~ALLOW_FILL_IN;
    }

    /**
     * Returns whether screen readers are allowed.
     *
     * @return true if screen readers are allowed
     */
    public boolean isAllowScreenReaders() {
        return (value & ALLOW_SCREEN_READERS) != 0;
    }

    /**
     * Sets whether screen readers are allowed.
     *
     * @param allow true to allow screen readers
     */
    public void setAllowScreenReaders(boolean allow) {
        if (allow) value |= ALLOW_SCREEN_READERS;
        else value &= ~ALLOW_SCREEN_READERS;
    }

    /**
     * Returns whether document assembly is allowed.
     *
     * @return true if assembly is allowed
     */
    public boolean isAllowAssembly() {
        return (value & ALLOW_ASSEMBLY) != 0;
    }

    /**
     * Sets whether document assembly is allowed.
     *
     * @param allow true to allow assembly
     */
    public void setAllowAssembly(boolean allow) {
        if (allow) value |= ALLOW_ASSEMBLY;
        else value &= ~ALLOW_ASSEMBLY;
    }

    /**
     * Returns whether degraded printing is allowed.
     *
     * @return true if degraded printing is allowed
     */
    public boolean isAllowDegradedPrinting() {
        return (value & ALLOW_DEGRADED_PRINTING) != 0;
    }

    /**
     * Sets whether degraded printing is allowed.
     *
     * @param allow true to allow degraded printing
     */
    public void setAllowDegradedPrinting(boolean allow) {
        if (allow) value |= ALLOW_DEGRADED_PRINTING;
        else value &= ~ALLOW_DEGRADED_PRINTING;
    }

    /**
     * Returns Adobe-style printing permission level.
     * <p>
     * 0 = none, 1 = low resolution, 2 = high resolution.
     * </p>
     *
     * @return the print allow level
     */
    public int getPrintAllowLevel() {
        if (!isAllowPrint()) {
            return 0;
        }
        return isAllowDegradedPrinting() ? 2 : 1;
    }

    /**
     * Sets Adobe-style printing permission level.
     *
     * @param level 0 = none, 1 = low resolution, 2 = high resolution
     */
    public void setPrintAllowLevel(int level) {
        switch (level) {
            case 1:
                setAllowPrint(true);
                setAllowDegradedPrinting(false);
                break;
            case 2:
                setAllowPrint(true);
                setAllowDegradedPrinting(true);
                break;
            case 0:
            default:
                setAllowPrint(false);
                setAllowDegradedPrinting(false);
                break;
        }
    }

    /**
     * Returns Adobe-style change permission level.
     * <p>
     * 0 = none, 1 = assembly, 2 = fill forms, 3 = comment/fill, 4 = full modify.
     * Returns -1 for mixed combinations that do not map cleanly to one preset.
     * </p>
     *
     * @return the change allow level
     */
    public int getChangeAllowLevel() {
        boolean modify = isAllowModifyContents();
        boolean annotate = isAllowModifyAnnotations();
        boolean fill = isAllowFillIn();
        boolean assembly = isAllowAssembly();
        if (!modify && !annotate && !fill && !assembly) {
            return 0;
        }
        if (!modify && !annotate && !fill && assembly) {
            return 1;
        }
        if (!modify && !annotate && fill && !assembly) {
            return 2;
        }
        if (!modify && annotate && fill && !assembly) {
            return 3;
        }
        if (modify && annotate && fill && !assembly) {
            return 4;
        }
        return -1;
    }

    /**
     * Sets Adobe-style change permission level.
     *
     * @param level 0 = none, 1 = assembly, 2 = fill forms, 3 = comment/fill, 4 = full modify
     */
    public void setChangeAllowLevel(int level) {
        switch (level) {
            case 1:
                setAllowModifyContents(false);
                setAllowModifyAnnotations(false);
                setAllowFillIn(false);
                setAllowAssembly(true);
                break;
            case 2:
                setAllowModifyContents(false);
                setAllowModifyAnnotations(false);
                setAllowFillIn(true);
                setAllowAssembly(false);
                break;
            case 3:
                setAllowModifyContents(false);
                setAllowModifyAnnotations(true);
                setAllowFillIn(true);
                setAllowAssembly(false);
                break;
            case 4:
                setAllowModifyContents(true);
                setAllowModifyAnnotations(true);
                setAllowFillIn(true);
                setAllowAssembly(false);
                break;
            case 0:
            default:
                setAllowModifyContents(false);
                setAllowModifyAnnotations(false);
                setAllowFillIn(false);
                setAllowAssembly(false);
                break;
        }
    }

    /**
     * Returns Adobe-style copy permission level.
     * <p>
     * 0 = none, 1 = screen readers only, 2 = full copy.
     * </p>
     *
     * @return the copy allow level
     */
    public int getCopyAllowLevel() {
        if (isAllowCopy()) {
            return 2;
        }
        return isAllowScreenReaders() ? 1 : 0;
    }

    /**
     * Sets Adobe-style copy permission level.
     *
     * @param level 0 = none, 1 = screen readers only, 2 = full copy
     */
    public void setCopyAllowLevel(int level) {
        switch (level) {
            case 1:
                setAllowCopy(false);
                setAllowScreenReaders(true);
                break;
            case 2:
                setAllowCopy(true);
                setAllowScreenReaders(true);
                break;
            case 0:
            default:
                setAllowCopy(false);
                setAllowScreenReaders(false);
                break;
        }
    }

    @Override
    public int compareTo(Object obj) {
        if (!(obj instanceof DocumentPrivilege)) {
            return 1;
        }
        DocumentPrivilege other = (DocumentPrivilege) obj;
        return Integer.compare(other.value, value);
    }
}
