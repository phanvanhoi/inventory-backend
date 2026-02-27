package manage.store.inventory.ai.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseMetadata {
    private String source;
    private LocalDateTime queryTime;
    private String intent;
    private String reason;
}
