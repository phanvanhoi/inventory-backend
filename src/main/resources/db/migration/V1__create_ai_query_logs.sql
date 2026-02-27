-- AI Query Logs - Audit table for AI Assistant queries
CREATE TABLE IF NOT EXISTS ai_query_logs (
    log_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    username        VARCHAR(100) NOT NULL,
    conversation_id VARCHAR(100),

    -- Request
    user_question   TEXT NOT NULL,
    detected_intent VARCHAR(50) NOT NULL,
    extracted_params JSON,

    -- Execution
    query_used      VARCHAR(100),
    execution_time_ms INT,
    rows_returned   INT,

    -- Response
    answer          TEXT,
    data_returned   BOOLEAN DEFAULT FALSE,
    llm_model       VARCHAR(50),
    llm_tokens_used INT,
    success         BOOLEAN DEFAULT TRUE,
    error_message   TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_ai_logs_user (user_id),
    INDEX idx_ai_logs_intent (detected_intent),
    INDEX idx_ai_logs_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
