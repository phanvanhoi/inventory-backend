package manage.store.inventory.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import manage.store.inventory.ai.dto.AiQueryRequestDTO;
import manage.store.inventory.ai.entity.AiQueryLog;
import manage.store.inventory.ai.model.AiIntent;
import manage.store.inventory.ai.model.ExtractedParams;
import manage.store.inventory.ai.model.QueryResult;
import manage.store.inventory.ai.repository.AiQueryLogRepository;

/**
 * Audit logging service cho AI queries.
 * Ghi log bất đồng bộ để không ảnh hưởng response time.
 */
@Service
public class AiAuditService {

    private static final Logger log = LoggerFactory.getLogger(AiAuditService.class);

    private final AiQueryLogRepository logRepository;
    private final ObjectMapper objectMapper;

    public AiAuditService(AiQueryLogRepository logRepository, ObjectMapper objectMapper) {
        this.logRepository = logRepository;
        this.objectMapper = objectMapper;
    }

    @Async
    @Transactional
    public void logQuery(Long userId, String username, AiQueryRequestDTO request,
                          AiIntent intent, ExtractedParams params,
                          QueryResult queryResult, String answer,
                          String llmModel, Integer llmTokens,
                          boolean success, String errorMessage) {
        try {
            AiQueryLog logEntry = new AiQueryLog();
            logEntry.setUserId(userId);
            logEntry.setUsername(username);
            logEntry.setConversationId(request.getConversationId());
            logEntry.setUserQuestion(request.getQuestion());
            logEntry.setDetectedIntent(intent != null ? intent.name() : "UNKNOWN");
            logEntry.setExtractedParams(toJson(params));
            logEntry.setQueryUsed(queryResult != null ? queryResult.getQueryUsed() : null);
            logEntry.setExecutionTimeMs(queryResult != null ? (int) queryResult.getExecutionTimeMs() : null);
            logEntry.setRowsReturned(queryResult != null ? queryResult.getRowCount() : null);
            logEntry.setAnswer(answer);
            logEntry.setDataReturned(queryResult != null && queryResult.getRowCount() > 0);
            logEntry.setLlmModel(llmModel);
            logEntry.setLlmTokensUsed(llmTokens);
            logEntry.setSuccess(success);
            logEntry.setErrorMessage(errorMessage);

            logRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save AI audit log: {}", e.getMessage(), e);
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
