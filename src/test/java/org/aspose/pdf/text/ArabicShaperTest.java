package org.aspose.pdf.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the clean-room contextual Arabic shaper used by the RTL text
 * replacement pipeline (RTL2/RTL3_changeText).
 */
public class ArabicShaperTest {

    /** RTL3: روسيا shapes with initial/medial/final contextual forms. */
    @Test
    public void shapesFullyConnectedWord() {
        // reh(isolated) waw(isolated) seen(initial) yeh(medial) alef(final)
        assertEquals("ﺭﻭﺳﻴﺎ",
                ArabicShaper.shape("روسيا"));
    }

    /** RTL2 test 3: presentation-form chars pass through and break joining. */
    @Test
    public void presentationFormsArePreservedAndNonJoining() {
        // Input: ا ﻷ ه ﺪ ا ف (mix of plain letters and presentation forms).
        // Plain letters flanked by presentation forms take isolated forms.
        assertEquals("ﺍﻷﻩﺪﺍﻑ",
                ArabicShaper.shape("اﻷهﺪاف"));
    }

    /** Mandatory lam-alef ligature, isolated and final positions. */
    @Test
    public void lamAlefLigature() {
        assertEquals("ﻻ", ArabicShaper.shape("لا"));
        // beh + lam+alef: beh takes initial, ligature takes final.
        assertEquals("ﺑﻼ", ArabicShaper.shape("بلا"));
    }

    /** Harakat are transparent: joining skips them and they are kept. */
    @Test
    public void combiningMarksAreTransparent() {
        // beh + fatha + yeh: beh initial, yeh final, fatha kept in place.
        assertEquals("ﺑَﻲ",
                ArabicShaper.shape("بَي"));
    }

    /** Non-Arabic text is returned unchanged (same instance). */
    @Test
    public void nonArabicUntouched() {
        String latin = "777 abc";
        assertSame(latin, ArabicShaper.shape(latin));
        assertFalse(ArabicShaper.needsShaping(latin));
        assertTrue(ArabicShaper.needsShaping("رو"));
    }
}
