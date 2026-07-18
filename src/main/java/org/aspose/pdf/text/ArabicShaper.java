package org.aspose.pdf.text;

import java.util.logging.Logger;

/// Clean-room contextual Arabic shaper: maps plain Arabic letters
/// (U+0621–U+064A) to their Unicode Arabic Presentation Forms-B
/// (U+FE70–U+FEFC) glyph variants — isolated, final, initial, medial —
/// according to the cursive joining rules of the script.
///
/// Used when replacement text is written back into a content stream whose
/// original run was already stored as presentation forms (the common shape
/// for PDFs produced from Arabic documents): the replacement must be shaped
/// the same way or it will neither render nor re-extract like the
/// surrounding text.
///
/// Rules implemented (Unicode Standard ch. 9.2 "Arabic", joining classes):
///
///   - Dual-joining letters take initial/medial/final/isolated forms.
///   - Right-joining letters (alef, dal, thal, reh, zain, waw, …) take only
///     final/isolated forms and never connect to the following letter.
///   - Combining marks (harakat, U+064B–U+065F, U+0670) are transparent:
///     they pass through unchanged and do not interrupt joining.
///   - Any other character — including text already encoded as
///     presentation forms — is a non-joining boundary and passes through
///     unchanged (matches Aspose.PDF behaviour: pre-shaped input is kept
///     verbatim, plain letters around it shape as if next to a space).
///   - The lam-alef pairs compose into the mandatory ligatures
///     U+FEF5–U+FEFC.
public final class ArabicShaper {

    private static final Logger LOG = Logger.getLogger(ArabicShaper.class.getName());

    private ArabicShaper() {
    }

    /// Joining class: does not connect on either side (hamza).
    private static final int NON_JOINING = 0;
    /// Joining class: connects only to the preceding letter (isolated/final).
    private static final int RIGHT_JOINING = 1;
    /// Joining class: connects on both sides (all four forms).
    private static final int DUAL_JOINING = 2;

    /// Presentation forms per letter, indexed by `char - 0x0621`:
    /// `{isolated, final, initial, medial}`. Right-joining letters carry
    /// only isolated/final (initial/medial repeat the isolated/final forms and
    /// are never selected for them). A zero row means "not a shapeable letter".
    private static final char[][] FORMS = new char[0x064B - 0x0621][];
    private static final int[] JOINING = new int[0x064B - 0x0621];

    private static void def(char letter, int joining, char isolated, char fin, char init, char medial) {
        FORMS[letter - 0x0621] = new char[]{isolated, fin, init, medial};
        JOINING[letter - 0x0621] = joining;
    }

    static {
        def('ء', NON_JOINING,   'ﺀ', 'ﺀ', 'ﺀ', 'ﺀ'); // hamza
        def('آ', RIGHT_JOINING, 'ﺁ', 'ﺂ', 'ﺁ', 'ﺂ'); // alef madda
        def('أ', RIGHT_JOINING, 'ﺃ', 'ﺄ', 'ﺃ', 'ﺄ'); // alef hamza above
        def('ؤ', RIGHT_JOINING, 'ﺅ', 'ﺆ', 'ﺅ', 'ﺆ'); // waw hamza
        def('إ', RIGHT_JOINING, 'ﺇ', 'ﺈ', 'ﺇ', 'ﺈ'); // alef hamza below
        def('ئ', DUAL_JOINING,  'ﺉ', 'ﺊ', 'ﺋ', 'ﺌ'); // yeh hamza
        def('ا', RIGHT_JOINING, 'ﺍ', 'ﺎ', 'ﺍ', 'ﺎ'); // alef
        def('ب', DUAL_JOINING,  'ﺏ', 'ﺐ', 'ﺑ', 'ﺒ'); // beh
        def('ة', RIGHT_JOINING, 'ﺓ', 'ﺔ', 'ﺓ', 'ﺔ'); // teh marbuta
        def('ت', DUAL_JOINING,  'ﺕ', 'ﺖ', 'ﺗ', 'ﺘ'); // teh
        def('ث', DUAL_JOINING,  'ﺙ', 'ﺚ', 'ﺛ', 'ﺜ'); // theh
        def('ج', DUAL_JOINING,  'ﺝ', 'ﺞ', 'ﺟ', 'ﺠ'); // jeem
        def('ح', DUAL_JOINING,  'ﺡ', 'ﺢ', 'ﺣ', 'ﺤ'); // hah
        def('خ', DUAL_JOINING,  'ﺥ', 'ﺦ', 'ﺧ', 'ﺨ'); // khah
        def('د', RIGHT_JOINING, 'ﺩ', 'ﺪ', 'ﺩ', 'ﺪ'); // dal
        def('ذ', RIGHT_JOINING, 'ﺫ', 'ﺬ', 'ﺫ', 'ﺬ'); // thal
        def('ر', RIGHT_JOINING, 'ﺭ', 'ﺮ', 'ﺭ', 'ﺮ'); // reh
        def('ز', RIGHT_JOINING, 'ﺯ', 'ﺰ', 'ﺯ', 'ﺰ'); // zain
        def('س', DUAL_JOINING,  'ﺱ', 'ﺲ', 'ﺳ', 'ﺴ'); // seen
        def('ش', DUAL_JOINING,  'ﺵ', 'ﺶ', 'ﺷ', 'ﺸ'); // sheen
        def('ص', DUAL_JOINING,  'ﺹ', 'ﺺ', 'ﺻ', 'ﺼ'); // sad
        def('ض', DUAL_JOINING,  'ﺽ', 'ﺾ', 'ﺿ', 'ﻀ'); // dad
        def('ط', DUAL_JOINING,  'ﻁ', 'ﻂ', 'ﻃ', 'ﻄ'); // tah
        def('ظ', DUAL_JOINING,  'ﻅ', 'ﻆ', 'ﻇ', 'ﻈ'); // zah
        def('ع', DUAL_JOINING,  'ﻉ', 'ﻊ', 'ﻋ', 'ﻌ'); // ain
        def('غ', DUAL_JOINING,  'ﻍ', 'ﻎ', 'ﻏ', 'ﻐ'); // ghain
        def('ف', DUAL_JOINING,  'ﻑ', 'ﻒ', 'ﻓ', 'ﻔ'); // feh
        def('ق', DUAL_JOINING,  'ﻕ', 'ﻖ', 'ﻗ', 'ﻘ'); // qaf
        def('ك', DUAL_JOINING,  'ﻙ', 'ﻚ', 'ﻛ', 'ﻜ'); // kaf
        def('ل', DUAL_JOINING,  'ﻝ', 'ﻞ', 'ﻟ', 'ﻠ'); // lam
        def('م', DUAL_JOINING,  'ﻡ', 'ﻢ', 'ﻣ', 'ﻤ'); // meem
        def('ن', DUAL_JOINING,  'ﻥ', 'ﻦ', 'ﻧ', 'ﻨ'); // noon
        def('ه', DUAL_JOINING,  'ﻩ', 'ﻪ', 'ﻫ', 'ﻬ'); // heh
        def('و', RIGHT_JOINING, 'ﻭ', 'ﻮ', 'ﻭ', 'ﻮ'); // waw
        def('ى', RIGHT_JOINING, 'ﻯ', 'ﻰ', 'ﻯ', 'ﻰ'); // alef maksura
        def('ي', DUAL_JOINING,  'ﻱ', 'ﻲ', 'ﻳ', 'ﻴ'); // yeh
    }

    /// Lam-alef ligatures `{isolated, final}` keyed by the alef variant.
    private static char[] lamAlefLigature(char alef) {
        switch (alef) {
            case 'آ': return new char[]{'ﻵ', 'ﻶ'};
            case 'أ': return new char[]{'ﻷ', 'ﻸ'};
            case 'إ': return new char[]{'ﻹ', 'ﻺ'};
            case 'ا': return new char[]{'ﻻ', 'ﻼ'};
            default: return null;
        }
    }

    /// Returns whether `c` is a transparent combining mark (harakat).
    private static boolean isTransparent(char c) {
        return (c >= 'ً' && c <= 'ٟ') || c == 'ٰ';
    }

    /// Returns whether `c` is a shapeable plain Arabic letter.
    private static boolean isShapeable(char c) {
        return c >= 'ء' && c <= 'ي' && FORMS[c - 0x0621] != null;
    }

    /// Returns whether the text contains at least one plain Arabic letter
    /// that this shaper would transform.
    ///
    /// @param text the text to inspect (may be `null`)
    /// @return `true` if shaping would change the text
    public static boolean needsShaping(String text) {
        if (text == null) return false;
        for (int i = 0; i < text.length(); i++) {
            if (isShapeable(text.charAt(i))) return true;
        }
        return false;
    }

    /// Shapes the plain Arabic letters of `text` (in logical order) into
    /// presentation forms. All other characters pass through unchanged.
    ///
    /// @param text logical-order text, possibly mixing plain Arabic letters,
    ///             presentation forms, digits and Latin
    /// @return the shaped text (same length or shorter — lam-alef pairs
    ///         compose into one ligature character)
    public static String shape(String text) {
        if (!needsShaping(text)) {
            return text;
        }
        StringBuilder out = new StringBuilder(text.length());
        int n = text.length();
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            if (!isShapeable(c)) {
                out.append(c);
                continue;
            }
            boolean connPrev = joinsForward(prevBaseChar(text, i));
            int nextIdx = nextBaseIndex(text, i);
            char next = nextIdx >= 0 ? text.charAt(nextIdx) : '\0';

            // Mandatory lam-alef ligature.
            if (c == 'ل' && next != '\0' && lamAlefLigature(next) != null) {
                char[] lig = lamAlefLigature(next);
                out.append(connPrev ? lig[1] : lig[0]);
                // Copy transparent marks between lam and alef, then skip the alef.
                for (int j = i + 1; j < nextIdx; j++) {
                    out.append(text.charAt(j));
                }
                i = nextIdx;
                continue;
            }

            boolean connNext = JOINING[c - 0x0621] == DUAL_JOINING && joinsBackward(next);
            char[] forms = FORMS[c - 0x0621];
            if (connPrev && connNext) {
                out.append(forms[3]);   // medial
            } else if (connPrev) {
                out.append(forms[1]);   // final
            } else if (connNext) {
                out.append(forms[2]);   // initial
            } else {
                out.append(forms[0]);   // isolated
            }
        }
        LOG.fine(() -> "Shaped Arabic run of length " + text.length());
        return out.toString();
    }

    /// The nearest preceding non-transparent char, or `'\\0'`.
    private static char prevBaseChar(String text, int i) {
        for (int j = i - 1; j >= 0; j--) {
            char c = text.charAt(j);
            if (!isTransparent(c)) return c;
        }
        return '\0';
    }

    /// Index of the nearest following non-transparent char, or `-1`.
    private static int nextBaseIndex(String text, int i) {
        for (int j = i + 1; j < text.length(); j++) {
            if (!isTransparent(text.charAt(j))) return j;
        }
        return -1;
    }

    /// Whether `c` cursively connects to the letter AFTER it.
    private static boolean joinsForward(char c) {
        return isShapeable(c) && JOINING[c - 0x0621] == DUAL_JOINING;
    }

    /// Whether `c` cursively accepts a connection from the letter BEFORE it.
    private static boolean joinsBackward(char c) {
        return isShapeable(c) && JOINING[c - 0x0621] != NON_JOINING;
    }
}
