package manage.store.inventory.ai.service;

import manage.store.inventory.ai.dto.AiQueryRequestDTO;
import manage.store.inventory.ai.dto.AiQueryResponseDTO;

public interface AiQueryService {
    AiQueryResponseDTO processQuery(AiQueryRequestDTO request);
}
