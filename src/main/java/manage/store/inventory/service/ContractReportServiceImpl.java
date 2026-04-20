package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.ContractReportAlertDTO;
import manage.store.inventory.dto.ContractReportCreateDTO;
import manage.store.inventory.dto.ContractReportDashboardDTO;
import manage.store.inventory.dto.ContractReportHistoryDTO;
import manage.store.inventory.dto.ContractReportListDTO;
import manage.store.inventory.dto.ContractReportUpdateDTO;
import manage.store.inventory.entity.Customer;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.OrderHistory;
import manage.store.inventory.entity.Unit;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.CustomerType;
import manage.store.inventory.entity.enums.OrderHistoryAction;
import manage.store.inventory.entity.enums.OrderStatus;
import manage.store.inventory.entity.enums.ReportPhase;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.CustomerRepository;
import manage.store.inventory.repository.OrderHistoryRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.UnitRepository;
import manage.store.inventory.repository.UserRepository;

/**
 * Legacy ContractReportService — refactored per G0-1 decision (Option C: dual routing).
 * Internally delegates to OrderRepository/OrderHistoryRepository but exposes
 * the original ContractReport* DTO shapes so mobile/FE không break.
 *
 * Sunset date: 2026-07-19 (3 tháng sau khi deploy V19).
 */
@Service
@Transactional
public class ContractReportServiceImpl implements ContractReportService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository historyRepository;
    private final CustomerRepository customerRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;

    public ContractReportServiceImpl(
            OrderRepository orderRepository,
            OrderHistoryRepository historyRepository,
            CustomerRepository customerRepository,
            UnitRepository unitRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.customerRepository = customerRepository;
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Long createReport(ContractReportCreateDTO dto, Long userId) {
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vị không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        // Find-or-create Customer by (unit, year)
        Customer customer = findOrCreateCustomer(unit, dto.getContractYear());

        Order order = new Order();
        order.setCustomer(customer);
        order.setCreatedByUser(user);
        order.setStatus(OrderStatus.NEW);
        order.setCurrentPhase(ReportPhase.SALES_INPUT);
        order.setCreatedAt(LocalDateTime.now());
        applySalesFields(dto, order);
        int year = dto.getContractYear() != null ? dto.getContractYear() : Year.now().getValue();
        order.setOrderCode(generateOrderCode(year));

        orderRepository.save(order);
        return order.getOrderId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractReportListDTO> getAllReports() {
        return orderRepository.findAllWithRelations().stream()
                .map(this::toListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContractReportListDTO getReportById(Long id) {
        Order order = orderRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Báo cáo không tồn tại"));
        return toListDTO(order);
    }

    @Override
    public void updateReport(Long id, ContractReportUpdateDTO dto, Long userId, Set<String> userRoles) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Báo cáo không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        boolean isAdmin = userRoles.contains("ADMIN");
        ReportPhase phase = order.getCurrentPhase();

        if (isAdmin) {
            updateAllFields(order, dto, user);
        } else if (userRoles.contains("SALES") && phase == ReportPhase.SALES_INPUT) {
            updateSalesFields(order, dto, user);
        } else if (userRoles.contains("MEASUREMENT") && phase == ReportPhase.MEASUREMENT_INPUT) {
            updateMeasurementFields(order, dto, user);
        } else if (userRoles.contains("PRODUCTION") && phase == ReportPhase.PRODUCTION_INPUT) {
            updateProductionFields(order, dto, user);
        } else if (userRoles.contains("STOCKKEEPER") && phase == ReportPhase.STOCKKEEPER_INPUT) {
            updateStockkeeperFields(order, dto, user);
        } else {
            throw new BusinessException("Bạn không có quyền sửa báo cáo ở giai đoạn này");
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void deleteReport(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Báo cáo không tồn tại"));
        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void advancePhase(Long id, Long userId, Set<String> userRoles) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Báo cáo không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        ReportPhase currentPhase = order.getCurrentPhase();
        if (currentPhase == ReportPhase.COMPLETED) {
            throw new BusinessException("Báo cáo đã hoàn tất");
        }
        String ownerRole = currentPhase.ownerRole();
        if (!userRoles.contains(ownerRole) && !userRoles.contains("ADMIN")) {
            throw new BusinessException("Chỉ " + ownerRole + " mới được chuyển giai đoạn này");
        }

        ReportPhase nextPhase = currentPhase.next();
        order.setCurrentPhase(nextPhase);
        orderRepository.save(order);

        logHistory(order, user, OrderHistoryAction.ADVANCE,
                "current_phase", currentPhase.name(), nextPhase.name(), null);
    }

    @Override
    public void returnPhase(Long id, String reason, Long userId, Set<String> userRoles) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Báo cáo không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        ReportPhase currentPhase = order.getCurrentPhase();
        if (currentPhase == ReportPhase.SALES_INPUT) {
            throw new BusinessException("Không thể trả lại từ giai đoạn đầu tiên");
        }
        if (currentPhase == ReportPhase.COMPLETED) {
            throw new BusinessException("Báo cáo đã hoàn tất, không thể trả lại");
        }
        String ownerRole = currentPhase.ownerRole();
        if (!userRoles.contains(ownerRole) && !userRoles.contains("ADMIN")) {
            throw new BusinessException("Chỉ " + ownerRole + " mới được trả lại giai đoạn này");
        }

        ReportPhase prevPhase = currentPhase.previous();
        order.setCurrentPhase(prevPhase);
        orderRepository.save(order);

        logHistory(order, user, OrderHistoryAction.RETURN,
                "current_phase", currentPhase.name(), prevPhase.name(), reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractReportHistoryDTO> getHistory(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Báo cáo không tồn tại");
        }
        return historyRepository.findByOrderId(id).stream()
                .map(h -> new ContractReportHistoryDTO(
                        h.getHistoryId(),
                        h.getAction() != null ? h.getAction().name() : null,
                        h.getFieldName(),
                        h.getOldValue(),
                        h.getNewValue(),
                        h.getReason(),
                        h.getChangedByUser() != null ? h.getChangedByUser().getFullName() : null,
                        h.getChangedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractReportAlertDTO> getAlerts() {
        LocalDate today = LocalDate.now();
        List<ContractReportAlertDTO> alerts = new ArrayList<>();

        orderRepository.findLateDeliveries(today).forEach(o -> {
            long days = ChronoUnit.DAYS.between(o.getExpectedDeliveryDate(), today);
            alerts.add(new ContractReportAlertDTO(
                    o.getOrderId(), unitName(o), salesPersonName(o),
                    "LATE_DELIVERY", o.getExpectedDeliveryDate(), (int) days));
        });

        orderRepository.findUpcomingDeliveries(today, today.plusDays(14)).forEach(o -> {
            long days = ChronoUnit.DAYS.between(today, o.getExpectedDeliveryDate());
            alerts.add(new ContractReportAlertDTO(
                    o.getOrderId(), unitName(o), salesPersonName(o),
                    "UPCOMING_DELIVERY", o.getExpectedDeliveryDate(), (int) days));
        });

        orderRepository.findLateTailorReturns(today).forEach(o -> {
            long days = ChronoUnit.DAYS.between(o.getTailorExpectedReturn(), today);
            alerts.add(new ContractReportAlertDTO(
                    o.getOrderId(), unitName(o), salesPersonName(o),
                    "LATE_TAILOR_RETURN", o.getTailorExpectedReturn(), (int) days));
        });

        return alerts;
    }

    @Override
    @Transactional(readOnly = true)
    public ContractReportDashboardDTO getDashboard() {
        LocalDate today = LocalDate.now();

        Object[] counts = orderRepository.getDashboardCounts();
        Object[] row = (Object[]) counts[0];

        long total = toLong(row[0]);
        long completed = toLong(row[1]);
        long salesInput = toLong(row[2]);
        long measurementInput = toLong(row[3]);
        long productionInput = toLong(row[4]);
        long stockkeeperInput = toLong(row[5]);

        List<ContractReportAlertDTO> lateDeliveries = orderRepository.findLateDeliveries(today).stream()
                .map(o -> new ContractReportAlertDTO(
                        o.getOrderId(), unitName(o), salesPersonName(o),
                        "LATE_DELIVERY", o.getExpectedDeliveryDate(),
                        (int) ChronoUnit.DAYS.between(o.getExpectedDeliveryDate(), today)))
                .collect(Collectors.toList());

        List<ContractReportAlertDTO> upcomingDeliveries = orderRepository.findUpcomingDeliveries(today, today.plusDays(14)).stream()
                .map(o -> new ContractReportAlertDTO(
                        o.getOrderId(), unitName(o), salesPersonName(o),
                        "UPCOMING_DELIVERY", o.getExpectedDeliveryDate(),
                        (int) ChronoUnit.DAYS.between(today, o.getExpectedDeliveryDate())))
                .collect(Collectors.toList());

        List<ContractReportAlertDTO> lateTailorReturns = orderRepository.findLateTailorReturns(today).stream()
                .map(o -> new ContractReportAlertDTO(
                        o.getOrderId(), unitName(o), salesPersonName(o),
                        "LATE_TAILOR_RETURN", o.getTailorExpectedReturn(),
                        (int) ChronoUnit.DAYS.between(o.getTailorExpectedReturn(), today)))
                .collect(Collectors.toList());

        return new ContractReportDashboardDTO(
                total, completed, salesInput, measurementInput, productionInput, stockkeeperInput,
                lateDeliveries, upcomingDeliveries, lateTailorReturns);
    }

    // ========== Role-scoped field updates ==========

    private void updateSalesFields(Order order, ContractReportUpdateDTO dto, User user) {
        if (dto.getUnitId() != null && !dto.getUnitId().equals(order.getCustomer().getUnit().getUnitId())) {
            Unit unit = unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Đơn vị không tồn tại"));
            Customer newCustomer = findOrCreateCustomer(unit, dto.getContractYear() != null ? dto.getContractYear() : order.getContractYear());
            logHistory(order, user, OrderHistoryAction.EDIT, "unit_id",
                    String.valueOf(order.getCustomer().getUnit().getUnitId()),
                    String.valueOf(dto.getUnitId()), null);
            order.setCustomer(newCustomer);
        }
        logAndSet(order, user, "unit_type", order.getUnitType(), dto.getUnitType(),
                () -> order.setUnitType(dto.getUnitType()));
        logAndSet(order, user, "contract_year", str(order.getContractYear()), str(dto.getContractYear()),
                () -> order.setContractYear(dto.getContractYear()));
        logAndSet(order, user, "sales_person_name", order.getSalesPersonName(), dto.getSalesPerson(),
                () -> order.setSalesPersonName(dto.getSalesPerson()));
        logAndSet(order, user, "expected_delivery_date", str(order.getExpectedDeliveryDate()), str(dto.getExpectedDeliveryDate()),
                () -> order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate()));
        logAndSet(order, user, "finalized_list_sent_date", str(order.getFinalizedListSentDate()), str(dto.getFinalizedListSentDate()),
                () -> order.setFinalizedListSentDate(dto.getFinalizedListSentDate()));
        logAndSet(order, user, "finalized_list_received_date", str(order.getFinalizedListReceivedDate()), str(dto.getFinalizedListReceivedDate()),
                () -> order.setFinalizedListReceivedDate(dto.getFinalizedListReceivedDate()));
        logAndSet(order, user, "delivery_method", order.getDeliveryMethod(), dto.getDeliveryMethod(),
                () -> order.setDeliveryMethod(dto.getDeliveryMethod()));
        logAndSet(order, user, "extra_payment_date", str(order.getExtraPaymentDate()), str(dto.getExtraPaymentDate()),
                () -> order.setExtraPaymentDate(dto.getExtraPaymentDate()));
        logAndSet(order, user, "extra_payment_amount", str(order.getExtraPaymentAmount()), str(dto.getExtraPaymentAmount()),
                () -> order.setExtraPaymentAmount(dto.getExtraPaymentAmount()));
        logAndSet(order, user, "note", order.getNote(), dto.getNote(),
                () -> order.setNote(dto.getNote()));
    }

    private void updateMeasurementFields(Order order, ContractReportUpdateDTO dto, User user) {
        logAndSet(order, user, "measurement_start", str(order.getMeasurementStart()), str(dto.getMeasurementStart()),
                () -> order.setMeasurementStart(dto.getMeasurementStart()));
        logAndSet(order, user, "measurement_end", str(order.getMeasurementEnd()), str(dto.getMeasurementEnd()),
                () -> order.setMeasurementEnd(dto.getMeasurementEnd()));
        logAndSet(order, user, "technician_name", order.getTechnicianName(), dto.getTechnicianName(),
                () -> order.setTechnicianName(dto.getTechnicianName()));
        logAndSet(order, user, "measurement_received_date", str(order.getMeasurementReceivedDate()), str(dto.getMeasurementReceivedDate()),
                () -> order.setMeasurementReceivedDate(dto.getMeasurementReceivedDate()));
        logAndSet(order, user, "measurement_handler", order.getMeasurementHandler(), dto.getMeasurementHandler(),
                () -> order.setMeasurementHandler(dto.getMeasurementHandler()));
        logAndSet(order, user, "skip_measurement", str(order.getSkipMeasurement()), str(dto.getSkipMeasurement()),
                () -> order.setSkipMeasurement(dto.getSkipMeasurement() != null ? dto.getSkipMeasurement() : false));
        logAndSet(order, user, "production_handover_date", str(order.getProductionHandoverDate()), str(dto.getProductionHandoverDate()),
                () -> order.setProductionHandoverDate(dto.getProductionHandoverDate()));
    }

    private void updateProductionFields(Order order, ContractReportUpdateDTO dto, User user) {
        logAndSet(order, user, "packing_return_date", str(order.getPackingReturnDate()), str(dto.getPackingReturnDate()),
                () -> order.setPackingReturnDate(dto.getPackingReturnDate()));
        logAndSet(order, user, "tailor_start_date", str(order.getTailorStartDate()), str(dto.getTailorStartDate()),
                () -> order.setTailorStartDate(dto.getTailorStartDate()));
        logAndSet(order, user, "tailor_expected_return", str(order.getTailorExpectedReturn()), str(dto.getTailorExpectedReturn()),
                () -> order.setTailorExpectedReturn(dto.getTailorExpectedReturn()));
        logAndSet(order, user, "tailor_actual_return", str(order.getTailorActualReturn()), str(dto.getTailorActualReturn()),
                () -> order.setTailorActualReturn(dto.getTailorActualReturn()));
    }

    private void updateStockkeeperFields(Order order, ContractReportUpdateDTO dto, User user) {
        logAndSet(order, user, "actual_shipping_date", str(order.getActualShippingDate()), str(dto.getActualShippingDate()),
                () -> order.setActualShippingDate(dto.getActualShippingDate()));
    }

    private void updateAllFields(Order order, ContractReportUpdateDTO dto, User user) {
        updateSalesFields(order, dto, user);
        updateMeasurementFields(order, dto, user);
        updateProductionFields(order, dto, user);
        updateStockkeeperFields(order, dto, user);
    }

    // ========== Helpers ==========

    private Customer findOrCreateCustomer(Unit unit, Integer year) {
        // Prefer existing with same unit + year + seed_source MIGRATED_FROM_CR
        List<Customer> candidates = customerRepository.findByUnitUnitId(unit.getUnitId());
        for (Customer c : candidates) {
            if (Objects.equals(c.getContractYear(), year)) return c;
        }
        // Tạo mới
        Customer c = new Customer();
        c.setUnit(unit);
        c.setContractYear(year);
        c.setCustomerType(CustomerType.NEW);
        c.setCreatedAt(LocalDateTime.now());
        customerRepository.save(c);
        return c;
    }

    private String generateOrderCode(int year) {
        String prefix = String.format("ORD-%d-", year);
        Long maxSeq = orderRepository.findMaxSequenceByYearPrefix(prefix);
        long next = (maxSeq == null ? 0L : maxSeq) + 1L;
        if (next >= 10000L) return prefix + next;
        return String.format("%s%04d", prefix, next);
    }

    private void applySalesFields(ContractReportCreateDTO dto, Order order) {
        order.setUnitType(dto.getUnitType());
        order.setContractYear(dto.getContractYear());
        order.setSalesPersonName(dto.getSalesPerson());
        order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        order.setFinalizedListSentDate(dto.getFinalizedListSentDate());
        order.setFinalizedListReceivedDate(dto.getFinalizedListReceivedDate());
        order.setDeliveryMethod(dto.getDeliveryMethod());
        order.setExtraPaymentDate(dto.getExtraPaymentDate());
        if (dto.getExtraPaymentAmount() != null) order.setExtraPaymentAmount(dto.getExtraPaymentAmount());
        order.setNote(dto.getNote());
    }

    private void logAndSet(Order order, User user, String fieldName,
                           String oldValue, String newValue, Runnable setter) {
        if (!Objects.equals(oldValue, newValue)) {
            logHistory(order, user, OrderHistoryAction.EDIT, fieldName, oldValue, newValue, null);
            setter.run();
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

    private ContractReportListDTO toListDTO(Order o) {
        ContractReportListDTO dto = new ContractReportListDTO();
        dto.setReportId(o.getOrderId());
        dto.setCurrentPhase(o.getCurrentPhase() != null ? o.getCurrentPhase().name() : null);
        if (o.getCustomer() != null && o.getCustomer().getUnit() != null) {
            dto.setUnitId(o.getCustomer().getUnit().getUnitId());
            dto.setUnitName(o.getCustomer().getUnit().getUnitName());
        }
        dto.setUnitType(o.getUnitType());
        dto.setContractYear(o.getContractYear());
        dto.setSalesPerson(o.getSalesPersonName());
        dto.setExpectedDeliveryDate(o.getExpectedDeliveryDate());
        dto.setFinalizedListSentDate(o.getFinalizedListSentDate());
        dto.setFinalizedListReceivedDate(o.getFinalizedListReceivedDate());
        dto.setDeliveryMethod(o.getDeliveryMethod());
        dto.setExtraPaymentDate(o.getExtraPaymentDate());
        dto.setExtraPaymentAmount(o.getExtraPaymentAmount());
        dto.setNote(o.getNote());
        dto.setMeasurementStart(o.getMeasurementStart());
        dto.setMeasurementEnd(o.getMeasurementEnd());
        dto.setTechnicianName(o.getTechnicianName());
        dto.setMeasurementReceivedDate(o.getMeasurementReceivedDate());
        dto.setMeasurementHandler(o.getMeasurementHandler());
        dto.setSkipMeasurement(o.getSkipMeasurement());
        dto.setProductionHandoverDate(o.getProductionHandoverDate());
        dto.setPackingReturnDate(o.getPackingReturnDate());
        dto.setTailorStartDate(o.getTailorStartDate());
        dto.setTailorExpectedReturn(o.getTailorExpectedReturn());
        dto.setTailorActualReturn(o.getTailorActualReturn());
        dto.setActualShippingDate(o.getActualShippingDate());
        dto.setCreatedByName(o.getCreatedByUser() != null ? o.getCreatedByUser().getFullName() : null);
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUpdatedAt(o.getUpdatedAt());
        dto.setDaysLate(calculateDaysLate(o));
        return dto;
    }

    private Integer calculateDaysLate(Order o) {
        if (o.getExpectedDeliveryDate() == null || o.getActualShippingDate() != null) return null;
        LocalDate today = LocalDate.now();
        if (today.isAfter(o.getExpectedDeliveryDate())) {
            return (int) ChronoUnit.DAYS.between(o.getExpectedDeliveryDate(), today);
        }
        return null;
    }

    private String unitName(Order o) {
        return (o.getCustomer() != null && o.getCustomer().getUnit() != null)
                ? o.getCustomer().getUnit().getUnitName() : null;
    }

    private String salesPersonName(Order o) {
        if (o.getSalesPersonUser() != null) return o.getSalesPersonUser().getFullName();
        return o.getSalesPersonName();
    }

    private String str(Object value) {
        return value == null ? null : value.toString();
    }

    private long toLong(Object value) {
        if (value == null) return 0;
        return ((Number) value).longValue();
    }
}
