package manage.store.inventory.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistoryMatrixDTO {

    private Long productId;
    private String productName;
    private String variantType;
    private String filterValue; // styleName for STRUCTURED, or null for ITEM_BASED

    // Danh sách các size columns: ["35", "36", ...] hoặc ["XS", "S", "M", ...]
    private List<String> sizeColumns;

    // Danh sách các rows, mỗi row là 1 request với quantity theo từng size
    private List<RequestHistoryRowDTO> rows;
}
