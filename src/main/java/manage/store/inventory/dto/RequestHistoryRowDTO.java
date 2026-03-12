package manage.store.inventory.dto;

import java.math.BigDecimal;
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
    private Long createdBy;
    private String createdByName;

    // For STRUCTURED: Map<sizeValue, quantity> - VD: {"35": 10.00, "36": 5.50, ...}
    private Map<String, BigDecimal> sizes;

    // For ITEM_BASED: single variant info per row
    private Long variantId;
    private String itemCode;
    private String itemName;
    private String unit;
    private BigDecimal quantity;
}
