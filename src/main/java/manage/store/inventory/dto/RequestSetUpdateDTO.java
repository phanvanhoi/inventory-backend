package manage.store.inventory.dto;

import java.util.List;

import lombok.Data;

/**
 * DTO để cập nhật bộ phiếu đã bị từ chối.
 * Cấu trúc tương tự RequestSetCreateDTO nhưng dùng cho PUT request.
 */
@Data
public class RequestSetUpdateDTO {

    private String setName;
    private String description;
    private List<InventoryRequestCreateDTO> requests;
}
