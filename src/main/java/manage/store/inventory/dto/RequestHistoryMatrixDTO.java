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
    private String styleName;

    // Danh sách các size columns: [35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45]
    private List<Integer> sizeColumns;

    // Danh sách các rows, mỗi row là 1 request với quantity theo từng size
    private List<RequestHistoryRowDTO> rows;
}
