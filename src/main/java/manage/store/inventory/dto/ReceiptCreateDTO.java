package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReceiptCreateDTO {

    private String note;

    // G6, V24 — Optional link to OrderItem (Lark integration, nullable for legacy RequestSet flow)
    private Long orderItemId;

    @NotEmpty(message = "Danh sách hàng nhận không được để trống")
    @Valid
    private List<ReceiptItemDTO> items;

    @Data
    public static class ReceiptItemDTO {

        @NotNull(message = "requestId không được để trống")
        private Long requestId;

        @NotNull(message = "variantId không được để trống")
        private Long variantId;

        @NotNull(message = "Số lượng nhận không được để trống")
        @Positive(message = "Số lượng nhận phải > 0")
        private BigDecimal receivedQuantity;

        // G6, V24 — Optional link to TailorAssignment (nullable for legacy)
        private Long tailorAssignmentId;
    }
}
