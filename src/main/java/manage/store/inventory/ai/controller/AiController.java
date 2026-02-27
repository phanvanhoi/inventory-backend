package manage.store.inventory.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.ai.config.RateLimiter;
import manage.store.inventory.ai.dto.AiQueryRequestDTO;
import manage.store.inventory.ai.dto.AiQueryResponseDTO;
import manage.store.inventory.ai.service.AiQueryService;
import manage.store.inventory.security.CurrentUser;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiQueryService aiQueryService;
    private final RateLimiter rateLimiter;
    private final CurrentUser currentUser;

    public AiController(AiQueryService aiQueryService,
                         RateLimiter rateLimiter,
                         CurrentUser currentUser) {
        this.aiQueryService = aiQueryService;
        this.rateLimiter = rateLimiter;
        this.currentUser = currentUser;
    }

    @PostMapping("/query")
    public ResponseEntity<AiQueryResponseDTO> query(@Valid @RequestBody AiQueryRequestDTO request) {
        Long userId = currentUser.getUserId();

        // Rate limiting
        if (!rateLimiter.isAllowed(userId)) {
            return ResponseEntity.status(429).body(
                    AiQueryResponseDTO.builder()
                            .success(false)
                            .answer("Bạn đã gửi quá nhiều câu hỏi. Vui lòng thử lại sau 1 phút.")
                            .build()
            );
        }

        AiQueryResponseDTO response = aiQueryService.processQuery(request);
        return ResponseEntity.ok(response);
    }
}
