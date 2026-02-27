package manage.store.inventory.ai.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_query_logs")
@Data
public class AiQueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "user_question", nullable = false, columnDefinition = "TEXT")
    private String userQuestion;

    @Column(name = "detected_intent", nullable = false)
    private String detectedIntent;

    @Column(name = "extracted_params", columnDefinition = "JSON")
    private String extractedParams;

    @Column(name = "query_used")
    private String queryUsed;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "rows_returned")
    private Integer rowsReturned;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "data_returned")
    private Boolean dataReturned;

    @Column(name = "llm_model")
    private String llmModel;

    @Column(name = "llm_tokens_used")
    private Integer llmTokensUsed;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
