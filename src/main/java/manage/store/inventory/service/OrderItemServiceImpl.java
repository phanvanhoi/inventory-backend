package manage.store.inventory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.OrderItemCreateDTO;
import manage.store.inventory.dto.OrderItemDTO;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.OrderItem;
import manage.store.inventory.entity.Product;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.OrderItemRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.ProductRepository;

@Service
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItemServiceImpl(
            OrderItemRepository itemRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Long addItem(Long orderId, OrderItemCreateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        applyFields(dto, item);
        item.setCreatedAt(LocalDateTime.now());
        itemRepository.save(item);
        return item.getOrderItemId();
    }

    @Override
    public void updateItem(Long itemId, OrderItemCreateDTO dto) {
        OrderItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));
        applyFields(dto, item);
        itemRepository.save(item);
    }

    @Override
    public void deleteItem(Long itemId) {
        OrderItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));
        item.setDeletedAt(LocalDateTime.now());
        itemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemDTO> getItemsByOrderId(Long orderId) {
        return itemRepository.findByOrderId(orderId).stream()
                .map(OrderItemDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemDTO getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .map(OrderItemDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));
    }

    private void applyFields(OrderItemCreateDTO dto, OrderItem item) {
        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
            item.setProduct(product);
            if (item.getProductName() == null || item.getProductName().isBlank()) {
                item.setProductName(product.getProductName());
            }
        }
        if (dto.getProductName() != null) item.setProductName(dto.getProductName());
        if (dto.getQtyContract() != null) item.setQtyContract(dto.getQtyContract());
        item.setQtySettlement(dto.getQtySettlement());
        if (dto.getUnitPrice() != null) item.setUnitPrice(dto.getUnitPrice());
        item.setNote(dto.getNote());
    }
}
