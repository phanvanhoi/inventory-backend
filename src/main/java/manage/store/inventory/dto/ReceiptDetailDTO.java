package manage.store.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDetailDTO {

    private Long receiptId;
    private Long setId;
    private Long receivedBy;
    private String receivedByName;
    private LocalDateTime receivedAt;
    private String note;
    private List<ReceiptItemDetailDTO> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReceiptItemDetailDTO {
        private Long receiptItemId;
        private Long requestId;
        private Long variantId;
        private String styleName;
        private Integer sizeValue;
        private String lengthCode;
        private Integer receivedQuantity;
    }
}
