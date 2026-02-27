package manage.store.inventory.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQueryResponseDTO {
    private boolean success;
    private String answer;
    private Object data;
    private AiResponseMetadata metadata;
    private List<String> suggestions;
}
