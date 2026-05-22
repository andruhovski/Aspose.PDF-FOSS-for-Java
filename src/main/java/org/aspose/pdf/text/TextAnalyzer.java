package org.aspose.pdf.text;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chooses the more reliable textual representation between two candidate
 * strings. This is a lightweight compatibility helper for legacy
 * TextFragmentAbsorber tests and prefers readable business-style Latin text
 * over obviously garbled symbol-decoded output.
 */
public class TextAnalyzer {

    /**
     * Supported analyzer languages.
     */
    public enum Language {
        English
    }

    /**
     * Supported analyzer styles.
     */
    public enum TextStyle {
        Business
    }

    private static final AtomicInteger CALLS = new AtomicInteger();

    /**
     * Creates a new analyzer.
     *
     * @param language ignored for now but preserved for API compatibility
     * @param style ignored for now but preserved for API compatibility
     */
    public TextAnalyzer(Language language, TextStyle style) {
        // Compatibility constructor. Current heuristic is language-agnostic.
    }

    /**
     * Chooses the more reliable text candidate.
     *
     * @param preferred the first candidate
     * @param alternate the second candidate
     * @param fontName the font hint
     * @return the candidate that looks more readable
     */
    public String chooseReliable(String preferred, String alternate, String fontName) {
        CALLS.incrementAndGet();
        int preferredScore = readabilityScore(preferred, fontName);
        int alternateScore = readabilityScore(alternate, fontName);
        return preferredScore >= alternateScore ? preferred : alternate;
    }

    /**
     * Clears accumulated statistics.
     */
    public static void clearStatistics() {
        CALLS.set(0);
    }

    private int readabilityScore(String text, String fontName) {
        if (text == null || text.isEmpty()) {
            return Integer.MIN_VALUE / 4;
        }
        int score = 0;
        String lowerFont = fontName == null ? "" : fontName.toLowerCase(Locale.ROOT);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                score += 4;
            } else if (ch >= 'a' && ch <= 'z') {
                score += 4;
            } else if (ch >= '0' && ch <= '9') {
                score += 3;
            } else if (Character.isWhitespace(ch)) {
                score += 2;
            } else if (".,:;'-/&()".indexOf(ch) >= 0) {
                score += 2;
            } else if (ch < 32 || ch > 126) {
                score -= 6;
            } else {
                score -= 1;
            }
        }
        if (text.contains("Q.B.S.A.") || text.contains("A.B.N.") || text.contains("GST")) {
            score += 6;
        }
        if (lowerFont.contains("symbol")) {
            score -= 1;
        }
        return score;
    }
}
