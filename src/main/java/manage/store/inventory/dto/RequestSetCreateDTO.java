package manage.store.inventory.dto;

import java.util.List;

import lombok.Data;

@Data
public class RequestSetCreateDTO {

    private String setName;
    private String description;
    private Long createdBy;
    private List<InventoryRequestCreateDTO> requests;
}
