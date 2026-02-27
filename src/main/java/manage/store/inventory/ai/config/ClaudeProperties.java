package manage.store.inventory.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "ai.claude")
public class ClaudeProperties {
    private String apiKey;
    private String model = "claude-3-haiku-20240307";
    private int maxTokens = 1024;
    private String baseUrl = "https://api.anthropic.com/v1";
}
