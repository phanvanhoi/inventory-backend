package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.OrderCreateDTO;
import manage.store.inventory.dto.OrderHistoryDTO;
import manage.store.inventory.dto.OrderItemCreateDTO;
import manage.store.inventory.dto.OrderListDTO;
import manage.store.inventory.dto.OrderUpdateDTO;
import manage.store.inventory.entity.Customer;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.OrderHistory;
import manage.store.inventory.entity.OrderItem;
import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.OrderHistoryAction;
import manage.store.inventory.entity.enums.OrderStatus;
import manage.store.inventory.entity.enums.ReportPhase;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.CustomerRepository;
import manage.store.inventory.repository.OrderHistoryRepository;
import manage.store.inventory.repository.OrderItemRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.repository.UserRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final OrderHistoryRepository historyRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository itemRepository,
            OrderHistoryRepository historyRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.historyRepository = historyRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // =====================================================
    // CREATE / UPDATE / DELETE
    // =====================================================

    @Override
    public Long createOrder(OrderCreateDTO dto, Long userId) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setCreatedByUser(user);
        order.setStatus(OrderStatus.NEW);
        order.setCurrentPhase(ReportPhase.SALES_INPUT);
        order.setCreatedAt(LocalDateTime.now());

        applyCreateFields(dto, order);

        // Generate order code nếu user không cung cấp
        if (order.getOrderCode() == null || order.getOrderCode().isBlank()) {
            int year = order.getContractYear() != null ? order.getContractYear() : Year.now().getValue();
            order.setOrderCode(generateOrderCode(year));
        }

        orderRepository.save(order);

        // Tạo items nếu có
        if (dto.getItems() != null) {
            for (OrderItemCreateDTO itemDto : dto.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                applyItemFields(itemDto, item);
                item.setCreatedAt(LocalDateTime.now());
                itemRepository.save(item);
            }
        }

        return order.getOrderId();
    }

    @Override
    public void updateOrder(Long orderId, OrderUpdateDTO dto, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        applyUpdateFields(dto, order, user);
        orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    // =====================================================
    // STATE TRANSITIONS
    // =====================================================

    @Override
    public void advancePhase(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        ReportPhase current = order.getCurrentPhase();
        if (current == ReportPhase.COMPLETED) {
            throw new BusinessException("Đơn hàng đã hoàn thành, không thể chuyển tiếp");
        }
        ReportPhase next = current.next();
        order.setCurrentPhase(next);
        order.setUpdatedAt(LocalDateTime.now());

        logHistory(order, user, OrderHistoryAction.ADVANCE,
                "current_phase", current.name(), next.name(), null);
        orderRepository.save(order);
    }

    @Override
    public void returnPhase(Long orderId, String reason, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Lý do trả lại không được để trống");
        }

        ReportPhase current = order.getCurrentPhase();
        ReportPhase previous = current.previous();
        order.setCurrentPhase(previous);
        order.setUpdatedAt(LocalDateTime.now());

        logHistory(order, user, OrderHistoryAction.RETURN,
                "current_phase", current.name(), previous.name(), reason);
        orderRepository.save(order);
    }

    @Override
    public void changeStatus(Long orderId, OrderStatus newStatus, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        if (newStatus == null) {
            throw new BusinessException("Trạng thái mới không được để trống");
        }
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus.isTerminal() && newStatus != oldStatus) {
            throw new BusinessException("Không thể đổi trạng thái từ " + oldStatus);
        }
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.CANCELLED) order.setCancelled(true);
        order.setUpdatedAt(LocalDateTime.now());

        logHistory(order, user, OrderHistoryAction.STATUS_CHANGE,
                "status", oldStatus.name(), newStatus.name(), null);
        orderRepository.save(order);
    }

    // =====================================================
    // READ
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getAllOrders() {
        return orderRepository.findAllWithRelations().stream()
                .map(OrderListDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderListDTO getOrderById(Long orderId) {
        return orderRepository.findByIdWithRelations(orderId)
                .map(OrderListDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerCustomerId(customerId).stream()
                .map(OrderListDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getOrdersBySalesPerson(Long userId) {
        return orderRepository.findBySalesPersonUserUserId(userId).stream()
                .map(OrderListDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getOrdersByPhase(ReportPhase phase) {
        return orderRepository.findByCurrentPhase(phase).stream()
                .map(OrderListDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(OrderListDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getLateDeliveries(LocalDate today) {
        return orderRepository.findLateDeliveries(today).stream()
                .map(OrderListDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> getUpcomingDeliveries(LocalDate today, int daysAhead) {
        LocalDate deadline = today.plusDays(daysAhead);
        return orderRepository.findUpcomingDeliveries(today, deadline).stream()
                .map(OrderListDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getOrderHistory(Long orderId) {
        return historyRepository.findByOrderId(orderId).stream()
                .map(OrderHistoryDTO::from)
                .collect(Collectors.toList());
    }

    // =====================================================
    // HELPERS
    // =====================================================

    /**
     * Generate order code format "ORD-YYYY-NNNN" (4-digit seq per year).
     * Fallback to "ORD-YYYY-N" without padding if seq >= 10000.
     */
    private String generateOrderCode(int year) {
        String prefix = String.format("ORD-%d-", year);
        Long maxSeq = orderRepository.findMaxSequenceByYearPrefix(prefix);
        long next = (maxSeq == null ? 0L : maxSeq) + 1L;
        if (next >= 10000L) {
            return prefix + next;
        }
        return String.format("%s%04d", prefix, next);
    }

    private void applyCreateFields(OrderCreateDTO dto, Order order) {
        if (dto.getOrderCode() != null && !dto.getOrderCode().isBlank()) {
            order.setOrderCode(dto.getOrderCode());
        }
        if (dto.getSalesPersonUserId() != null) {
            User sp = userRepository.findById(dto.getSalesPersonUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales user không tồn tại"));
            order.setSalesPersonUser(sp);
        }
        order.setSalesPersonName(dto.getSalesPersonName());
        order.setUnitType(dto.getUnitType());
        order.setContractYear(dto.getContractYear());
        if (dto.getTotalBeforeVat() != null) order.setTotalBeforeVat(dto.getTotalBeforeVat());
        if (dto.getVatAmount() != null) order.setVatAmount(dto.getVatAmount());
        if (dto.getTotalAfterVat() != null) order.setTotalAfterVat(dto.getTotalAfterVat());
        order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        order.setFinalizedListSentDate(dto.getFinalizedListSentDate());
        order.setFinalizedListReceivedDate(dto.getFinalizedListReceivedDate());
        order.setDeliveryMethod(dto.getDeliveryMethod());
        order.setExtraPaymentDate(dto.getExtraPaymentDate());
        if (dto.getExtraPaymentAmount() != null) order.setExtraPaymentAmount(dto.getExtraPaymentAmount());
        order.setNote(dto.getNote());
    }

    private void applyUpdateFields(OrderUpdateDTO dto, Order order, User user) {
        if (dto.getOrderCode() != null) order.setOrderCode(dto.getOrderCode());
        if (dto.getSalesPersonUserId() != null) {
            User sp = userRepository.findById(dto.getSalesPersonUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales user không tồn tại"));
            order.setSalesPersonUser(sp);
        }
        if (dto.getSalesPersonName() != null) order.setSalesPersonName(dto.getSalesPersonName());
        if (dto.getUnitType() != null) order.setUnitType(dto.getUnitType());
        if (dto.getContractYear() != null) order.setContractYear(dto.getContractYear());

        // Audit log cho các field quan trọng
        logIfChanged(order, user, "total_before_vat", order.getTotalBeforeVat(), dto.getTotalBeforeVat());
        if (dto.getTotalBeforeVat() != null) order.setTotalBeforeVat(dto.getTotalBeforeVat());
        if (dto.getVatAmount() != null) order.setVatAmount(dto.getVatAmount());
        if (dto.getTotalAfterVat() != null) order.setTotalAfterVat(dto.getTotalAfterVat());

        // SALES
        if (dto.getExpectedDeliveryDate() != null) order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        if (dto.getFinalizedListSentDate() != null) order.setFinalizedListSentDate(dto.getFinalizedListSentDate());
        if (dto.getFinalizedListReceivedDate() != null) order.setFinalizedListReceivedDate(dto.getFinalizedListReceivedDate());
        if (dto.getDeliveryMethod() != null) order.setDeliveryMethod(dto.getDeliveryMethod());
        if (dto.getExtraPaymentDate() != null) order.setExtraPaymentDate(dto.getExtraPaymentDate());
        if (dto.getExtraPaymentAmount() != null) order.setExtraPaymentAmount(dto.getExtraPaymentAmount());

        // MEASUREMENT
        if (dto.getMeasurementStart() != null) order.setMeasurementStart(dto.getMeasurementStart());
        if (dto.getMeasurementEnd() != null) order.setMeasurementEnd(dto.getMeasurementEnd());
        if (dto.getTechnicianName() != null) order.setTechnicianName(dto.getTechnicianName());
        if (dto.getMeasurementReceivedDate() != null) order.setMeasurementReceivedDate(dto.getMeasurementReceivedDate());
        if (dto.getMeasurementHandler() != null) order.setMeasurementHandler(dto.getMeasurementHandler());
        if (dto.getSkipMeasurement() != null) order.setSkipMeasurement(dto.getSkipMeasurement());
        if (dto.getProductionHandoverDate() != null) order.setProductionHandoverDate(dto.getProductionHandoverDate());

        // MEASUREMENT detail (G3, V21)
        if (dto.getCustomerRegistrationSentDate() != null) order.setCustomerRegistrationSentDate(dto.getCustomerRegistrationSentDate());
        if (dto.getTechBookReturnDate() != null) order.setTechBookReturnDate(dto.getTechBookReturnDate());
        if (dto.getMeasurementReceivedFromTechDate() != null) order.setMeasurementReceivedFromTechDate(dto.getMeasurementReceivedFromTechDate());
        if (dto.getListSentToCustomerDate() != null) order.setListSentToCustomerDate(dto.getListSentToCustomerDate());
        if (dto.getListFinalizedDate() != null) order.setListFinalizedDate(dto.getListFinalizedDate());
        if (dto.getMeasurementHandoverDateV2() != null) order.setMeasurementHandoverDateV2(dto.getMeasurementHandoverDateV2());
        if (dto.getMeasurementTakerUserId() != null) {
            User taker = userRepository.findById(dto.getMeasurementTakerUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người đi đo không tồn tại"));
            order.setMeasurementTakerUser(taker);
        }
        if (dto.getMeasurementComposerUserId() != null) {
            User composer = userRepository.findById(dto.getMeasurementComposerUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người soạn số đo không tồn tại"));
            order.setMeasurementComposerUser(composer);
        }

        // Files (G3, V21)
        if (dto.getContractFileUrl() != null) order.setContractFileUrl(dto.getContractFileUrl());
        if (dto.getHandoverRecordUrl() != null) order.setHandoverRecordUrl(dto.getHandoverRecordUrl());
        if (dto.getLiquidationRecordUrl() != null) order.setLiquidationRecordUrl(dto.getLiquidationRecordUrl());
        if (dto.getCustomerMeasurementFileUrl() != null) order.setCustomerMeasurementFileUrl(dto.getCustomerMeasurementFileUrl());

        // PRODUCTION
        if (dto.getTailorStartDate() != null) order.setTailorStartDate(dto.getTailorStartDate());
        if (dto.getTailorExpectedReturn() != null) order.setTailorExpectedReturn(dto.getTailorExpectedReturn());
        if (dto.getTailorActualReturn() != null) order.setTailorActualReturn(dto.getTailorActualReturn());
        if (dto.getPackingReturnDate() != null) order.setPackingReturnDate(dto.getPackingReturnDate());

        // STOCKKEEPER
        if (dto.getActualShippingDate() != null) order.setActualShippingDate(dto.getActualShippingDate());

        // Flags
        if (dto.getSkipDesign() != null) order.setSkipDesign(dto.getSkipDesign());
        if (dto.getDesignReady() != null) order.setDesignReady(dto.getDesignReady());
        if (dto.getSkipKcs() != null) order.setSkipKcs(dto.getSkipKcs());
        if (dto.getQcPassed() != null) order.setQcPassed(dto.getQcPassed());
        if (dto.getHasRepair() != null) order.setHasRepair(dto.getHasRepair());

        if (dto.getNote() != null) order.setNote(dto.getNote());

        order.setUpdatedAt(LocalDateTime.now());
    }

    private void applyItemFields(OrderItemCreateDTO dto, OrderItem item) {
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

    private void logIfChanged(Order order, User user, String fieldName, Object oldValue, Object newValue) {
        if (newValue != null && !Objects.equals(oldValue, newValue)) {
            logHistory(order, user, OrderHistoryAction.EDIT, fieldName,
                    oldValue == null ? null : oldValue.toString(),
                    newValue.toString(), null);
        }
    }

    private void logHistory(Order order, User user, OrderHistoryAction action,
                            String fieldName, String oldValue, String newValue, String reason) {
        OrderHistory h = new OrderHistory();
        h.setOrder(order);
        h.setChangedByUser(user);
        h.setChangedAt(LocalDateTime.now());
        h.setAction(action);
        h.setFieldName(fieldName);
        h.setOldValue(oldValue);
        h.setNewValue(newValue);
        h.setReason(reason);
        historyRepository.save(h);
    }
}
