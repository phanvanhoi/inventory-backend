package manage.store.inventory.ai.service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import manage.store.inventory.ai.model.ExtractedParams;
import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.Unit;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.repository.UnitRepository;

/**
 * Trích xuất tham số từ câu hỏi tiếng Việt.
 * Cache reference data từ DB tại startup.
 */
@Component
public class ParameterExtractor {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern SIZE_PATTERN = Pattern.compile("\\b(?:size\\s*|s|cỡ\\s*|co\\s*)(\\d{2})\\b|\\bsize\\s*(\\d{2})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern BARE_SIZE_PATTERN = Pattern.compile("\\b(3[5-9]|4[0-5])\\b");

    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;

    // Cached reference data
    private List<Unit> cachedUnits;
    private List<Product> cachedProducts;
    private Map<String, String> styleMapping;

    public ParameterExtractor(UnitRepository unitRepository, ProductRepository productRepository) {
        this.unitRepository = unitRepository;
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void init() {
        cachedUnits = unitRepository.findAll();
        cachedProducts = productRepository.findAll();
        initStyleMapping();
    }

    private void initStyleMapping() {
        styleMapping = new HashMap<>();
        // Map normalized keywords → actual DB style_name
        // Longer matches first to avoid "slim" matching before "slim ngan"
        styleMapping.put("co dien ngan", "CỔ ĐIỂN NGẮN");
        styleMapping.put("classic short", "CỔ ĐIỂN NGẮN");
        styleMapping.put("slim ngan", "SLIM Ngắn");
        styleMapping.put("slim short", "SLIM Ngắn");
        styleMapping.put("slim ngn", "SLIM Ngắn");
        styleMapping.put("co dien", "CỔ ĐIỂN");
        styleMapping.put("classic", "CỔ ĐIỂN");
        styleMapping.put("slim", "SLIM");
    }

    public ExtractedParams extract(String question) {
        ExtractedParams params = new ExtractedParams();

        if (question == null || question.isBlank()) {
            return params;
        }

        String normalized = normalize(question);
        String original = question.toLowerCase();

        // Extract style
        params.setStyleName(extractStyle(normalized));

        // Extract size
        params.setSizeValue(extractSize(original, question));

        // Extract length
        params.setLengthCode(extractLength(normalized));

        // Extract unit
        extractUnit(original, question, params);

        // Extract/resolve product
        extractProduct(original, question, params);

        return params;
    }

    private String extractStyle(String normalized) {
        // Check longer matches first
        for (Map.Entry<String, String> entry : styleMapping.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Integer extractSize(String lower, String original) {
        // Try explicit "size XX" pattern first
        Matcher sizeMatcher = SIZE_PATTERN.matcher(original);
        if (sizeMatcher.find()) {
            String sizeStr = sizeMatcher.group(1) != null ? sizeMatcher.group(1) : sizeMatcher.group(2);
            if (sizeStr != null) {
                int size = Integer.parseInt(sizeStr);
                if (size >= 35 && size <= 45) {
                    return size;
                }
            }
        }

        // Fallback: look for bare 2-digit numbers in valid range
        Matcher bareMatcher = BARE_SIZE_PATTERN.matcher(original);
        if (bareMatcher.find()) {
            return Integer.parseInt(bareMatcher.group(1));
        }

        return null;
    }

    private String extractLength(String normalized) {
        // Check for "dai"/"dài" keywords
        if (containsWord(normalized, "dai") || containsWord(normalized, "long")
                || normalized.contains("tay dai")) {
            return "DAI";
        }
        // Check for "coc"/"cộc" keywords
        if (containsWord(normalized, "coc") || containsWord(normalized, "short")
                || normalized.contains("tay ngan") || normalized.contains("tay coc")) {
            return "COC";
        }
        return null;
    }

    private void extractUnit(String lower, String original, ExtractedParams params) {
        String normalizedQuestion = normalize(original);

        // Remove "kho " prefix for matching
        String questionForMatch = normalizedQuestion.replace("kho ", "");

        Unit bestMatch = null;
        int bestLength = 0;

        for (Unit unit : cachedUnits) {
            String normalizedUnitName = normalize(unit.getUnitName());
            // Match unit name in question text
            if (normalizedQuestion.contains(normalizedUnitName)
                    || questionForMatch.contains(normalizedUnitName)) {
                if (normalizedUnitName.length() > bestLength) {
                    bestLength = normalizedUnitName.length();
                    bestMatch = unit;
                }
            }
        }

        if (bestMatch != null) {
            params.setUnitId(bestMatch.getUnitId());
            params.setUnitName(bestMatch.getUnitName());
        }
    }

    private void extractProduct(String lower, String original, ExtractedParams params) {
        String normalizedQuestion = normalize(original);

        // Try to match product name
        for (Product product : cachedProducts) {
            String normalizedProductName = normalize(product.getProductName());
            if (normalizedQuestion.contains(normalizedProductName)) {
                params.setProductId(product.getProductId());
                return;
            }
        }

        // Default: if only one product exists, use it
        if (cachedProducts.size() == 1) {
            params.setProductId(cachedProducts.get(0).getProductId());
        }
    }

    /**
     * Refresh cached data (call when master data changes).
     */
    public void refreshCache() {
        cachedUnits = unitRepository.findAll();
        cachedProducts = productRepository.findAll();
    }

    private String normalize(String text) {
        String lower = text.toLowerCase();
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D");
    }

    private boolean containsWord(String text, String word) {
        // Check if word appears as a standalone word (not part of another word)
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }
}
