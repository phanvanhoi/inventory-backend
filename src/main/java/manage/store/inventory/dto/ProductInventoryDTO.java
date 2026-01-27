package manage.store.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryDTO {

    private Long productId;
    private String productName;
    private String note;
    private LocalDateTime createdAt;
    private List<InventoryBalanceDTO> data;
}
