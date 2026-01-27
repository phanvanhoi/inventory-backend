package manage.store.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.dto.ItemCreateDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.dto.ItemUpdateDTO;
import manage.store.inventory.service.ItemService;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // Tạo item mới
    @PostMapping
    public ResponseEntity<Long> createItem(@RequestBody ItemCreateDTO dto) {
        Long itemId = itemService.createItem(dto);
        return ResponseEntity.ok(itemId);
    }

    // Lấy item theo ID
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailDTO> getItemById(@PathVariable Long itemId) {
        ItemDetailDTO item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    // Lấy tất cả items của một request
    @GetMapping("/request/{requestId}")
    public List<ItemDetailDTO> getItemsByRequestId(@PathVariable Long requestId) {
        return itemService.getItemsByRequestId(requestId);
    }

    // Cập nhật item (chỉ quantity)
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemDetailDTO> updateItem(
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDTO dto
    ) {
        ItemDetailDTO item = itemService.updateItem(itemId, dto);
        return ResponseEntity.ok(item);
    }

    // Xóa item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
