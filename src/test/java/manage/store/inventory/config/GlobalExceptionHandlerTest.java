package manage.store.inventory.config;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("AccessDeniedException trả về 403 với message tiếng Việt")
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().get("status"));
        assertEquals("Forbidden", response.getBody().get("error"));
        assertEquals("Bạn không có quyền thực hiện thao tác này.", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("RuntimeException trả về 400 với message từ exception")
    void handleRuntimeException_returns400() {
        RuntimeException ex = new RuntimeException("Số lượng không hợp lệ");

        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Số lượng không hợp lệ", response.getBody().get("message"));
    }

    @Test
    @DisplayName("RuntimeException giữ nguyên message gốc")
    void handleRuntimeException_preservesOriginalMessage() {
        String originalMessage = "Product not found: 99";
        RuntimeException ex = new RuntimeException(originalMessage);

        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);

        assertEquals(originalMessage, response.getBody().get("message"));
    }
}
