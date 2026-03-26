package manage.store.inventory.dto;

import java.util.List;

import lombok.Data;

@Data
public class RequestSetCreateDTO {

    private String setName;
    private String description;
    private String category; // enum value: VAI_GIAO_THO, PHU_LIEU, HANG_MAY_SAN, ...
    private Long createdBy;
    private List<InventoryRequestCreateDTO> requests;
}
