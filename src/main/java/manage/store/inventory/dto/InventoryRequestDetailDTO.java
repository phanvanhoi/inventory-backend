package manage.store.inventory.dto;

import java.util.List;

public class InventoryRequestDetailDTO {

    private InventoryRequestHeaderDTO header;
    private List<InventoryRequestItemDTO> items;

    public InventoryRequestDetailDTO() {
    }

    public InventoryRequestDetailDTO(
            InventoryRequestHeaderDTO header,
            List<InventoryRequestItemDTO> items
    ) {
        this.header = header;
        this.items = items;
    }

    public InventoryRequestHeaderDTO getHeader() {
        return header;
    }

    public void setHeader(InventoryRequestHeaderDTO header) {
        this.header = header;
    }

    public List<InventoryRequestItemDTO> getItems() {
        return items;
    }

    public void setItems(List<InventoryRequestItemDTO> items) {
        this.items = items;
    }
}
