package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * DTO cho STOCKKEEPER sửa số lượng items và chuyển RECEIVING luôn.
 * Chỉ cho phép sửa quantity của items đã có (không thêm/xóa item).
 */
@Data
public class EditAndReceiveDTO {

    @NotBlank(message = "Lý do sửa không được để trống")
    private String reason;

    @NotEmpty(message = "Danh sách items không được để trống")
    private List<ItemQuantityUpdate> items;

    @Data
    public static class ItemQuantityUpdate {
        private Long itemId;
        private BigDecimal quantity;
    }
}
