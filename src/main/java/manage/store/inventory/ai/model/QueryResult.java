package manage.store.inventory.ai.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class QueryResult {
    private AiIntent intent;
    private String queryUsed;
    private List<Map<String, Object>> data;
    private int rowCount;
    private long executionTimeMs;
    private String source;
}
