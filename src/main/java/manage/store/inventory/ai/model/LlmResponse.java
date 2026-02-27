package manage.store.inventory.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    private String text;
    private String model;
    private Integer tokensUsed;
}
