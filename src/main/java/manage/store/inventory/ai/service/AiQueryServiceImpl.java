package manage.store.inventory.ai.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.ai.dto.AiQueryRequestDTO;
import manage.store.inventory.ai.dto.AiQueryResponseDTO;
import manage.store.inventory.ai.dto.AiResponseMetadata;
import manage.store.inventory.ai.model.AiIntent;
import manage.store.inventory.ai.model.ExtractedParams;
import manage.store.inventory.ai.model.LlmResponse;
import manage.store.inventory.ai.model.QueryResult;
import manage.store.inventory.security.CurrentUser;

/**
 * Orchestrator chính cho AI Assistant.
 * Pipeline: classify → extract → validate → route → LLM → audit.
 *
 * @Transactional(readOnly = true) đảm bảo AI không thể ghi DB.
 */
@Service
@Transactional(readOnly = true)
public class AiQueryServiceImpl implements AiQueryService {

    private static final Logger log = LoggerFactory.getLogger(AiQueryServiceImpl.class);

    private final IntentClassifier intentClassifier;
    private final ParameterExtractor parameterExtractor;
    private final QueryRouter queryRouter;
    private final LlmService llmService;
    private final AiAuditService auditService;
    private final CurrentUser currentUser;

    public AiQueryServiceImpl(IntentClassifier intentClassifier,
                               ParameterExtractor parameterExtractor,
                               QueryRouter queryRouter,
                               LlmService llmService,
                               AiAuditService auditService,
                               CurrentUser currentUser) {
        this.intentClassifier = intentClassifier;
        this.parameterExtractor = parameterExtractor;
        this.queryRouter = queryRouter;
        this.llmService = llmService;
        this.auditService = auditService;
        this.currentUser = currentUser;
    }

    @Override
    public AiQueryResponseDTO processQuery(AiQueryRequestDTO request) {
        Long userId = currentUser.getUserId();
        String username = currentUser.getUsername();

        AiIntent intent = AiIntent.UNKNOWN;
        ExtractedParams params = null;
        QueryResult queryResult = null;
        String answer = null;
        String llmModel = null;
        Integer llmTokens = null;
        boolean success = false;
        String errorMessage = null;

        try {
            // Step 1: Classify intent
            intent = intentClassifier.classify(request.getQuestion());
            log.debug("Intent classified: {} for question: {}", intent, request.getQuestion());

            // Step 2: Extract parameters
            params = parameterExtractor.extract(request.getQuestion());
            log.debug("Params extracted: {}", params);

            // Step 3: Handle UNKNOWN intent
            if (intent == AiIntent.UNKNOWN) {
                answer = "Tôi không hiểu câu hỏi của bạn. Tôi chỉ có thể trả lời các câu hỏi về tồn kho.";
                success = true;
                return AiQueryResponseDTO.builder()
                        .success(true)
                        .answer(answer)
                        .metadata(AiResponseMetadata.builder()
                                .intent(intent.name())
                                .queryTime(LocalDateTime.now())
                                .reason("UNRECOGNIZED_INTENT")
                                .build())
                        .suggestions(List.of(
                                "Bưu điện Kon Tum còn bao nhiêu áo Slim size 40 dài?",
                                "Những biến thể nào đang âm kho?",
                                "So sánh tồn kho giữa các đơn vị"
                        ))
                        .build();
            }

            // Step 4: Validate required params
            String validationError = validateParams(intent, params);
            if (validationError != null) {
                answer = validationError;
                success = true;
                return buildInsufficientDataResponse(intent, validationError);
            }

            // Step 5: Route to query
            queryResult = queryRouter.route(intent, params);
            log.debug("Query result: {} rows from {}", queryResult.getRowCount(), queryResult.getQueryUsed());

            // Step 6: Check if data was found
            if (queryResult.getRowCount() == 0) {
                answer = buildNoDataMessage(intent, params);
                success = true;
                return AiQueryResponseDTO.builder()
                        .success(true)
                        .answer(answer)
                        .data(queryResult.getData())
                        .metadata(AiResponseMetadata.builder()
                                .source(queryResult.getSource())
                                .queryTime(LocalDateTime.now())
                                .intent(intent.name())
                                .reason("NO_DATA_FOUND")
                                .build())
                        .build();
            }

            // Step 7: Generate natural language answer via LLM
            LlmResponse llmResponse = llmService.generateAnswer(
                    request.getQuestion(), intent, params, queryResult);
            answer = llmResponse.getText();
            llmModel = llmResponse.getModel();
            llmTokens = llmResponse.getTokensUsed();
            success = true;

            // Step 8: Build response
            return AiQueryResponseDTO.builder()
                    .success(true)
                    .answer(answer)
                    .data(queryResult.getData())
                    .metadata(AiResponseMetadata.builder()
                            .source(queryResult.getSource())
                            .queryTime(LocalDateTime.now())
                            .intent(intent.name())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Error processing AI query: {}", e.getMessage(), e);
            errorMessage = e.getMessage();
            answer = "Xin lỗi, đã xảy ra lỗi khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.";
            return AiQueryResponseDTO.builder()
                    .success(false)
                    .answer(answer)
                    .metadata(AiResponseMetadata.builder()
                            .intent(intent.name())
                            .queryTime(LocalDateTime.now())
                            .reason("INTERNAL_ERROR")
                            .build())
                    .build();
        } finally {
            // Step 9: Audit log (async, non-blocking)
            try {
                auditService.logQuery(userId, username, request, intent, params,
                        queryResult, answer, llmModel, llmTokens, success, errorMessage);
            } catch (Exception e) {
                log.error("Failed to submit audit log: {}", e.getMessage());
            }
        }
    }

    /**
     * Validate required params dựa trên intent.
     * Return null nếu hợp lệ, error message nếu thiếu params.
     */
    private String validateParams(AiIntent intent, ExtractedParams params) {
        switch (intent) {
            case QUERY_BALANCE -> {
                if (params.getProductId() == null) {
                    return "Không xác định được sản phẩm. Vui lòng hỏi cụ thể hơn.";
                }
                if (params.getUnitId() == null) {
                    return "Vui lòng chỉ định đơn vị (ví dụ: Bưu điện Kon Tum).";
                }
            }
            case QUERY_NEGATIVE -> {
                if (params.getProductId() == null) {
                    return "Không xác định được sản phẩm. Vui lòng hỏi cụ thể hơn.";
                }
            }
            case EXPLAIN_BALANCE -> {
                if (params.getProductId() == null) {
                    return "Không xác định được sản phẩm. Vui lòng hỏi cụ thể hơn.";
                }
                if (params.getUnitId() == null) {
                    return "Vui lòng chỉ định đơn vị để giải thích tồn kho.";
                }
                if (params.getStyleName() == null && params.getSizeValue() == null) {
                    return "Vui lòng chỉ định biến thể cụ thể (kiểu dáng, size) để giải thích.";
                }
            }
            case COMPARE_UNITS -> {
                if (params.getProductId() == null) {
                    return "Không xác định được sản phẩm để so sánh.";
                }
            }
            default -> {
                // UNKNOWN is handled before validation
            }
        }
        return null;
    }

    private AiQueryResponseDTO buildInsufficientDataResponse(AiIntent intent, String reason) {
        return AiQueryResponseDTO.builder()
                .success(true)
                .answer(reason)
                .metadata(AiResponseMetadata.builder()
                        .intent(intent.name())
                        .queryTime(LocalDateTime.now())
                        .reason("INSUFFICIENT_PARAMS")
                        .build())
                .suggestions(List.of(
                        "Bưu điện Kon Tum còn bao nhiêu áo Slim size 40 dài?",
                        "Những biến thể nào đang âm kho tại Bưu điện Hà Nội?",
                        "Vì sao CỔ ĐIỂN size 39 dài bị âm kho tại Bưu điện Kon Tum?",
                        "So sánh tồn kho giữa các đơn vị"
                ))
                .build();
    }

    private String buildNoDataMessage(AiIntent intent, ExtractedParams params) {
        StringBuilder sb = new StringBuilder("Không tìm thấy dữ liệu ");
        switch (intent) {
            case QUERY_BALANCE -> {
                sb.append("tồn kho");
                if (params.getUnitName() != null) sb.append(" tại ").append(params.getUnitName());
                if (params.getStyleName() != null) sb.append(" cho ").append(params.getStyleName());
                if (params.getSizeValue() != null) sb.append(" size ").append(params.getSizeValue());
            }
            case QUERY_NEGATIVE -> {
                sb.setLength(0);
                sb.append("Không có biến thể nào đang âm kho");
                if (params.getUnitName() != null) sb.append(" tại ").append(params.getUnitName());
            }
            case EXPLAIN_BALANCE -> sb.append("giao dịch cho biến thể được chỉ định");
            case COMPARE_UNITS -> sb.append("tồn kho để so sánh giữa các đơn vị");
            default -> sb.append("phù hợp với yêu cầu");
        }
        sb.append(".");
        return sb.toString();
    }
}
