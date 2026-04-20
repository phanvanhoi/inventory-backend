package manage.store.inventory.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemCreateDTO {

    private Long productId;

    @NotNull(message = "Tên mặt hàng không được để trống")
    private String productName;

    @Min(value = 0, message = "Số lượng HĐ không âm")
    private Integer qtyContract = 0;

    private Integer qtySettlement;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private String note;
}
