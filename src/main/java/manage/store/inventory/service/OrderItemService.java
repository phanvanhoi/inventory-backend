package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.OrderItemCreateDTO;
import manage.store.inventory.dto.OrderItemDTO;

public interface OrderItemService {

    Long addItem(Long orderId, OrderItemCreateDTO dto);

    void updateItem(Long itemId, OrderItemCreateDTO dto);

    void deleteItem(Long itemId);

    List<OrderItemDTO> getItemsByOrderId(Long orderId);

    OrderItemDTO getItemById(Long itemId);
}
