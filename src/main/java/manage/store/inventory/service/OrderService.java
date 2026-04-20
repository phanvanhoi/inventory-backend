package manage.store.inventory.service;

import java.time.LocalDate;
import java.util.List;

import manage.store.inventory.dto.OrderCreateDTO;
import manage.store.inventory.dto.OrderHistoryDTO;
import manage.store.inventory.dto.OrderListDTO;
import manage.store.inventory.dto.OrderUpdateDTO;
import manage.store.inventory.entity.enums.OrderStatus;
import manage.store.inventory.entity.enums.ReportPhase;

public interface OrderService {

    Long createOrder(OrderCreateDTO dto, Long userId);

    void updateOrder(Long orderId, OrderUpdateDTO dto, Long userId);

    void deleteOrder(Long orderId);

    void advancePhase(Long orderId, Long userId);

    void returnPhase(Long orderId, String reason, Long userId);

    void changeStatus(Long orderId, OrderStatus newStatus, Long userId);

    List<OrderListDTO> getAllOrders();

    OrderListDTO getOrderById(Long orderId);

    List<OrderListDTO> getOrdersByCustomer(Long customerId);

    List<OrderListDTO> getOrdersBySalesPerson(Long userId);

    List<OrderListDTO> getOrdersByPhase(ReportPhase phase);

    List<OrderListDTO> getOrdersByStatus(OrderStatus status);

    List<OrderListDTO> getLateDeliveries(LocalDate today);

    List<OrderListDTO> getUpcomingDeliveries(LocalDate today, int daysAhead);

    List<OrderHistoryDTO> getOrderHistory(Long orderId);
}
