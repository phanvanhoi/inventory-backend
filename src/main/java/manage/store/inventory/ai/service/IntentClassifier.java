package manage.store.inventory.ai.service;

import java.text.Normalizer;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import manage.store.inventory.ai.model.AiIntent;

/**
 * Rule-based intent classification cho câu hỏi tiếng Việt.
 * Priority: EXPLAIN > NEGATIVE > COMPARE > BALANCE > UNKNOWN
 */
@Component
public class IntentClassifier {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public AiIntent classify(String question) {
        if (question == null || question.isBlank()) {
            return AiIntent.UNKNOWN;
        }

        String normalized = normalize(question);

        // Priority 1: EXPLAIN_BALANCE
        if (isExplainIntent(normalized)) {
            return AiIntent.EXPLAIN_BALANCE;
        }

        // Priority 2: QUERY_NEGATIVE
        if (isNegativeIntent(normalized)) {
            return AiIntent.QUERY_NEGATIVE;
        }

        // Priority 3: COMPARE_UNITS
        if (isCompareIntent(normalized)) {
            return AiIntent.COMPARE_UNITS;
        }

        // Priority 4: QUERY_BALANCE
        if (isBalanceIntent(normalized)) {
            return AiIntent.QUERY_BALANCE;
        }

        return AiIntent.UNKNOWN;
    }

    private boolean isExplainIntent(String text) {
        boolean hasExplainKeyword = containsAny(text,
                "vi sao", "tai sao", "giai thich", "nguyen nhan", "ly do",
                "explain", "why");
        boolean hasBalanceContext = containsAny(text,
                "am", "am kho", "thieu", "ton kho", "balance", "du kien",
                "tang", "giam", "mat");
        return hasExplainKeyword && hasBalanceContext;
    }

    private boolean isNegativeIntent(String text) {
        return containsAny(text,
                "am kho", "ton kho am", "bi am", "duoi 0", "< 0",
                "negative", "thieu hut", "het hang");
    }

    private boolean isCompareIntent(String text) {
        return containsAny(text,
                "so sanh", "compare", "doi chieu",
                "giua cac don vi", "giua cac kho",
                "tong hop cac don vi", "tong hop kho",
                "tat ca don vi", "tat ca kho", "moi don vi", "moi kho");
    }

    private boolean isBalanceIntent(String text) {
        return containsAny(text,
                "con bao nhieu", "ton kho", "inventory", "balance",
                "so luong", "bao nhieu", "con", "co bao nhieu",
                "hang ton", "ton", "kho con",
                "slim", "co dien", "classic", "size", "dai", "coc");
    }

    /**
     * Normalize tiếng Việt: lowercase + remove diacritics.
     * "Cổ Điển" → "co dien", "Bưu điện Kon Tum" → "buu dien kon tum"
     */
    private String normalize(String text) {
        String lower = text.toLowerCase();
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
