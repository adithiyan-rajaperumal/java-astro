package org.vedic.astro.util;

public class IndicPreShaper {
    private static final ThreadLocal<Boolean> pdfMode = ThreadLocal.withInitial(() -> false);

    public static void setPdfMode(boolean enabled) {
        pdfMode.set(enabled);
    }

    public static boolean isPdfMode() {
        return pdfMode.get();
    }

    /**
     * Re-orders character byte streams to enforce proper layout rendering
     * inside primitive, non-CTL PDF generation modules.
     */
    public static String shape(String text) {
        if (text == null || text.isEmpty()) return text;

        // If the text contains Tamil characters, convert to Bamini instead!
        boolean hasTamil = false;
        for (int idx = 0; idx < text.length(); idx++) {
            char c = text.charAt(idx);
            if (c >= '\u0B80' && c <= '\u0BFF') {
                hasTamil = true;
                break;
            }
        }
        if (hasTamil) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        int i = 0;

        while (i < chars.length) {
            char c = chars[i];

            // Look ahead to check if the next character is a dependent vowel modifier
            if (i + 1 < chars.length) {
                char next = chars[i + 1];

                // ==========================================
                // TAMIL TEXT SHAPING RULES
                // ==========================================
                // Left-side standalone vowels: ெ, ே, ை
                if (next == '\u0BC6' || next == '\u0BC7' || next == '\u0BC8') {
                    sb.append(next).append(c);
                    i += 2;
                    continue;
                }
                // Two-part vowel: ொ -> Shuffles to: ெ + Consonant + ா
                if (next == '\u0BCA') {
                    sb.append('\u0BC6').append(c).append('\u0BBE');
                    i += 2;
                    continue;
                }
                // Two-part vowel: ோ -> Shuffles to: ே + Consonant + ா
                if (next == '\u0BCB') {
                    sb.append('\u0BC7').append(c).append('\u0BBE');
                    i += 2;
                    continue;
                }
                // Two-part vowel: ௌ -> Shuffles to: ெ + Consonant + ௗ
                if (next == '\u0BCC') {
                    sb.append('\u0BC6').append(c).append('\u0BD7');
                    i += 2;
                    continue;
                }

                // ==========================================
                // HINDI (DEVANAGARI) TEXT SHAPING RULES
                // ==========================================
                // Left-side short vowel marker: ि (e.g., కి / कि)
                if (next == '\u093F') {
                    sb.append(next).append(c);
                    i += 2;
                    continue;
                }

                // ==========================================
                // MALAYALAM TEXT SHAPING RULES
                // ==========================================
                // Left-side standalone vowels: െ, േ, ൈ
                if (next == '\u0D46' || next == '\u0D47' || next == '\u0D48') {
                    sb.append(next).append(c);
                    i += 2;
                    continue;
                }
                // Two-part vowel: ൊ -> Shuffles to: െ + Consonant + ാ
                if (next == '\u0D4A') {
                    sb.append('\u0D46').append(c).append('\u0D3E');
                    i += 2;
                    continue;
                }
                // Two-part vowel: ോ -> Shuffles to: േ + Consonant + ാ
                if (next == '\u0D4B') {
                    sb.append('\u0D47').append(c).append('\u0D3E');
                    i += 2;
                    continue;
                }
                // Two-part vowel: ൌ -> Shuffles to: െ + Consonant + ൗ
                if (next == '\u0D4C') {
                    sb.append('\u0D46').append(c).append('\u0D57');
                    i += 2;
                    continue;
                }
            }

            sb.append(c);
            i++;
        }

        return sb.toString();
    }
}
