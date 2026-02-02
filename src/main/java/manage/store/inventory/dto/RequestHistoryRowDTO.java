package manage.store.inventory.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistoryRowDTO {

    private Long requestId;
    private Long setId;
    private String setName;
    private String setStatus;
    private String unitName;
    private String requestType;
    private String lengthCode;
    private String note;
    private LocalDateTime createdAt;
    private Long createdBy;         // ID người tạo request set
    private String createdByName;   // Tên người tạo request set

    // Map<sizeValue, quantity> - VD: {35: 10, 36: 5, 37: 0, ...}
    private Map<Integer, Integer> sizes;
}
