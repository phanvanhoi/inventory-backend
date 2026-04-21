package manage.store.inventory.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.dto.OrderCreateDTO;
import manage.store.inventory.dto.OrderHistoryDTO;
import manage.store.inventory.dto.OrderItemCreateDTO;
import manage.store.inventory.dto.OrderItemDTO;
import manage.store.inventory.dto.OrderListDTO;
import manage.store.inventory.dto.OrderUpdateDTO;
import manage.store.inventory.dto.ReportReturnReasonDTO;
import manage.store.inventory.entity.enums.OrderStatus;
import manage.store.inventory.entity.enums.ReportPhase;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.OrderItemService;
import manage.store.inventory.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService itemService;
    private final CurrentUser currentUser;

    public OrderController(OrderService orderService,
                           OrderItemService itemService,
                           CurrentUser currentUser) {
        this.orderService = orderService;
        this.itemService = itemService;
        this.currentUser = currentUser;
    }

    // ===== ORDER CRUD =====

    @PostMapping
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Long> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        Long id = orderService.createOrder(dto, currentUser.getUserId());
        return ResponseEntity.ok(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALES','MEASUREMENT','PRODUCTION','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> updateOrder(@PathVariable Long id,
                                             @Valid @RequestBody OrderUpdateDTO dto) {
        orderService.updateOrder(id, dto, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    // ===== PHASE TRANSITIONS =====

    @PostMapping("/{id}/advance")
    @PreAuthorize("hasAnyRole('SALES','MEASUREMENT','PRODUCTION','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> advance(@PathVariable Long id) {
        orderService.advancePhase(id, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('SALES','MEASUREMENT','PRODUCTION','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> returnPhase(@PathVariable Long id,
                                              @RequestBody ReportReturnReasonDTO dto) {
        orderService.returnPhase(id, dto.getReason(), currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SALES','MEASUREMENT','PRODUCTION','STOCKKEEPER','DESIGNER','KCS','PACKER','REPAIRER','PURCHASER')")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id,
                                              @RequestParam OrderStatus status) {
        orderService.changeStatus(id, status, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    // ===== READ =====

    @GetMapping
    public List<OrderListDTO> getOrders(@RequestParam(required = false) ReportPhase phase,
                                         @RequestParam(required = false) OrderStatus status,
                                         @RequestParam(required = false) Long customerId,
                                         @RequestParam(required = false) Long salesPersonUserId) {
        if (phase != null) return orderService.getOrdersByPhase(phase);
        if (status != null) return orderService.getOrdersByStatus(status);
        if (customerId != null) return orderService.getOrdersByCustomer(customerId);
        if (salesPersonUserId != null) return orderService.getOrdersBySalesPerson(salesPersonUserId);
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public OrderListDTO getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // ===== ALERTS =====

    @GetMapping("/alerts/late")
    public List<OrderListDTO> getLateDeliveries() {
        return orderService.getLateDeliveries(LocalDate.now());
    }

    @GetMapping("/alerts/upcoming")
    public List<OrderListDTO> getUpcomingDeliveries(
            @RequestParam(defaultValue = "7") int daysAhead) {
        return orderService.getUpcomingDeliveries(LocalDate.now(), daysAhead);
    }

    // ===== HISTORY =====

    @GetMapping("/{id}/history")
    public List<OrderHistoryDTO> getOrderHistory(@PathVariable Long id) {
        return orderService.getOrderHistory(id);
    }

    // ===== ORDER ITEMS =====

    @GetMapping("/{id}/items")
    public List<OrderItemDTO> getItems(@PathVariable Long id) {
        return itemService.getItemsByOrderId(id);
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Long> addItem(@PathVariable Long id,
                                          @Valid @RequestBody OrderItemCreateDTO dto) {
        Long itemId = itemService.addItem(id, dto);
        return ResponseEntity.ok(itemId);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> updateItem(@PathVariable Long itemId,
                                             @Valid @RequestBody OrderItemCreateDTO dto) {
        itemService.updateItem(itemId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok().build();
    }
}
