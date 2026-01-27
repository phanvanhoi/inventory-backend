package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.ItemCreateDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.dto.ItemUpdateDTO;

public interface ItemService {

    // Tạo item mới
    Long createItem(ItemCreateDTO dto);

    // Lấy item theo ID
    ItemDetailDTO getItemById(Long itemId);

    // Lấy tất cả items của một request
    List<ItemDetailDTO> getItemsByRequestId(Long requestId);

    // Cập nhật item
    ItemDetailDTO updateItem(Long itemId, ItemUpdateDTO dto);

    // Xóa item
    void deleteItem(Long itemId);
}
