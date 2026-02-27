package manage.store.inventory.ai.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.ObjectMapper;

import manage.store.inventory.ai.config.ClaudeProperties;
import manage.store.inventory.ai.model.AiIntent;
import manage.store.inventory.ai.model.ExtractedParams;
import manage.store.inventory.ai.model.LlmResponse;
import manage.store.inventory.ai.model.QueryResult;

/**
 * Gọi Claude API để sinh câu trả lời ngôn ngữ tự nhiên từ dữ liệu có sẵn.
 * LLM KHÔNG truy cập DB, chỉ nhận dữ liệu đã được backend xử lý.
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient restClient;
    private final ClaudeProperties claudeProperties;
    private final ObjectMapper objectMapper;

    public LlmService(ClaudeProperties claudeProperties, ObjectMapper objectMapper) {
        this.claudeProperties = claudeProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(claudeProperties.getBaseUrl())
                .defaultHeader("x-api-key", claudeProperties.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public LlmResponse generateAnswer(String userQuestion, AiIntent intent,
                                        ExtractedParams params, QueryResult queryResult) {
        try {
            String systemPrompt = buildSystemPrompt(queryResult);
            String userMessage = buildUserMessage(userQuestion, params, queryResult);

            Map<String, Object> requestBody = Map.of(
                    "model", claudeProperties.getModel(),
                    "max_tokens", claudeProperties.getMaxTokens(),
                    "system", systemPrompt,
                    "messages", List.of(Map.of("role", "user", "content", userMessage))
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/messages")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return parseLlmResponse(response);

        } catch (Exception e) {
            log.warn("Claude API call failed, using fallback: {}", e.getMessage());
            return buildFallbackResponse(intent, params, queryResult);
        }
    }

    private String buildSystemPrompt(QueryResult queryResult) {
        return """
                You are an AI assistant for HangFashion inventory management system (Hệ thống quản lý kho HangFashion).

                STRICT RULES:
                1. You can ONLY answer based on the data provided in the DATA CONTEXT below
                2. You CANNOT make calculations or estimates beyond what is provided
                3. You CANNOT access any data outside the provided context
                4. If data is insufficient, say "Không đủ dữ liệu để trả lời"
                5. Always cite the data source
                6. Always respond in Vietnamese
                7. Be concise and precise

                BUSINESS CONTEXT:
                - Sản phẩm: Áo đồng phục bưu điện (fashion uniforms)
                - Biến thể (variant): Kiểu dáng (CỔ ĐIỂN, CỔ ĐIỂN NGẮN, SLIM, SLIM Ngắn) × Kích cỡ (35-45) × Độ dài (COC=cộc/ngắn tay, DAI=dài/dài tay)
                - Đơn vị (unit): 72 bưu điện/chi nhánh
                - Tồn kho = SUM(Nhập IN) - SUM(Xuất OUT) từ các phiếu đã thực hiện (EXECUTED)
                - Tồn kho có thể âm (cho phép backorder)
                - IN = Nhập kho, OUT = Xuất kho
                """;
    }

    private String buildUserMessage(String userQuestion, ExtractedParams params, QueryResult queryResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("DATA CONTEXT:\n");
        sb.append("- Nguồn dữ liệu: ").append(queryResult.getSource()).append("\n");
        sb.append("- Thời gian truy vấn: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        sb.append("- Số dòng dữ liệu: ").append(queryResult.getRowCount()).append("\n");

        if (params.getUnitName() != null) {
            sb.append("- Đơn vị: ").append(params.getUnitName()).append("\n");
        }
        if (params.getStyleName() != null) {
            sb.append("- Kiểu dáng: ").append(params.getStyleName()).append("\n");
        }
        if (params.getSizeValue() != null) {
            sb.append("- Kích cỡ: ").append(params.getSizeValue()).append("\n");
        }
        if (params.getLengthCode() != null) {
            sb.append("- Độ dài: ").append("DAI".equals(params.getLengthCode()) ? "Dài" : "Cộc").append("\n");
        }

        sb.append("\nDỮ LIỆU:\n");
        try {
            sb.append(objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(queryResult.getData()));
        } catch (Exception e) {
            sb.append(queryResult.getData().toString());
        }

        sb.append("\n\nCÂU HỎI CỦA NGƯỜI DÙNG: ").append(userQuestion);
        sb.append("\n\nHãy trả lời ngắn gọn, chính xác dựa trên dữ liệu trên. Nếu không đủ dữ liệu, nói rõ lý do.");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private LlmResponse parseLlmResponse(Map<String, Object> response) {
        LlmResponse result = new LlmResponse();
        result.setModel(claudeProperties.getModel());

        if (response == null) {
            result.setText("Không thể xử lý phản hồi từ AI.");
            return result;
        }

        // Extract text from Claude Messages API response
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        if (content != null && !content.isEmpty()) {
            result.setText((String) content.get(0).get("text"));
        }

        // Extract token usage
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        if (usage != null) {
            Integer inputTokens = (Integer) usage.get("input_tokens");
            Integer outputTokens = (Integer) usage.get("output_tokens");
            result.setTokensUsed((inputTokens != null ? inputTokens : 0)
                    + (outputTokens != null ? outputTokens : 0));
        }

        return result;
    }

    /**
     * Fallback khi Claude API không khả dụng.
     * Tạo câu trả lời template-based từ dữ liệu có sẵn.
     */
    private LlmResponse buildFallbackResponse(AiIntent intent, ExtractedParams params, QueryResult queryResult) {
        LlmResponse response = new LlmResponse();
        response.setModel("fallback");
        response.setTokensUsed(0);

        if (queryResult.getRowCount() == 0) {
            response.setText("Không tìm thấy dữ liệu phù hợp với yêu cầu của bạn.");
            return response;
        }

        StringBuilder sb = new StringBuilder();

        switch (intent) {
            case QUERY_BALANCE -> {
                sb.append("Kết quả truy vấn tồn kho");
                if (params.getUnitName() != null) {
                    sb.append(" tại ").append(params.getUnitName());
                }
                sb.append(":\n");
                for (Map<String, Object> row : queryResult.getData()) {
                    sb.append("- ").append(row.get("styleName"))
                            .append(" size ").append(row.get("sizeValue"))
                            .append(" ").append("DAI".equals(row.get("lengthCode")) ? "dài" : "cộc")
                            .append(": ").append(row.get("actualQuantity")).append(" chiếc\n");
                }
            }
            case QUERY_NEGATIVE -> {
                sb.append("Có ").append(queryResult.getRowCount()).append(" biến thể đang âm kho");
                if (params.getUnitName() != null) {
                    sb.append(" tại ").append(params.getUnitName());
                }
                sb.append(":\n");
                for (Map<String, Object> row : queryResult.getData()) {
                    sb.append("- ").append(row.get("styleName"))
                            .append(" size ").append(row.get("sizeValue"))
                            .append(" ").append("DAI".equals(row.get("lengthCode")) ? "dài" : "cộc")
                            .append(": ").append(row.get("actualQuantity")).append("\n");
                }
            }
            case EXPLAIN_BALANCE -> {
                sb.append("Lịch sử giao dịch:\n");
                for (Map<String, Object> row : queryResult.getData()) {
                    String type = (String) row.get("type");
                    if ("transaction".equals(type)) {
                        sb.append("- ").append(row.get("requestType"))
                                .append(": ").append(row.get("quantity"))
                                .append(" (").append(row.get("setName")).append(")\n");
                    } else if ("calculation".equals(type)) {
                        sb.append("\nTổng nhập: ").append(row.get("totalIn"))
                                .append(", Tổng xuất: ").append(row.get("totalOut"))
                                .append(", Tồn kho: ").append(row.get("balance"));
                    }
                }
            }
            case COMPARE_UNITS -> {
                sb.append("So sánh tồn kho giữa các đơn vị:\n");
                for (Map<String, Object> row : queryResult.getData()) {
                    sb.append("- ").append(row.get("unitName"))
                            .append(": ").append(row.get("totalBalance"))
                            .append(" (").append(row.get("variantCount")).append(" biến thể)\n");
                }
            }
            default -> sb.append("Dữ liệu đã được truy xuất thành công.");
        }

        response.setText(sb.toString());
        return response;
    }
}
