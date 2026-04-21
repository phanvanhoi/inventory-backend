package manage.store.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.CustomerDTO;
import manage.store.inventory.dto.CustomerRollupDTO;
import manage.store.inventory.dto.DashboardRowDTO;
import manage.store.inventory.dto.DashboardSummaryDTO;
import manage.store.inventory.entity.Advance;
import manage.store.inventory.entity.Customer;
import manage.store.inventory.entity.Guarantee;
import manage.store.inventory.entity.Invoice;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.PackingBatch;
import manage.store.inventory.entity.Payment;
import manage.store.inventory.entity.enums.GuaranteeType;
import manage.store.inventory.entity.enums.InvoiceStatus;
import manage.store.inventory.entity.enums.OrderStatus;
import manage.store.inventory.entity.enums.PaymentStatus;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.AdvanceRepository;
import manage.store.inventory.repository.CustomerRepository;
import manage.store.inventory.repository.GuaranteeRepository;
import manage.store.inventory.repository.InvoiceRepository;
import manage.store.inventory.repository.MissingItemRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.PackingBatchRepository;
import manage.store.inventory.repository.PaymentRepository;
import manage.store.inventory.repository.QualityCheckRepository;
import manage.store.inventory.repository.RepairRequestRepository;
import manage.store.inventory.repository.TailorAssignmentRepository;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AdvanceRepository advanceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final GuaranteeRepository guaranteeRepository;
    private final PackingBatchRepository packingBatchRepository;
    private final MissingItemRepository missingItemRepository;
    private final QualityCheckRepository qcRepository;
    private final TailorAssignmentRepository tailorAssignmentRepository;
    private final RepairRequestRepository repairRepository;

    public DashboardServiceImpl(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            AdvanceRepository advanceRepository,
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            GuaranteeRepository guaranteeRepository,
            PackingBatchRepository packingBatchRepository,
            MissingItemRepository missingItemRepository,
            QualityCheckRepository qcRepository,
            TailorAssignmentRepository tailorAssignmentRepository,
            RepairRequestRepository repairRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.advanceRepository = advanceRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.guaranteeRepository = guaranteeRepository;
        this.packingBatchRepository = packingBatchRepository;
        this.missingItemRepository = missingItemRepository;
        this.qcRepository = qcRepository;
        this.tailorAssignmentRepository = tailorAssignmentRepository;
        this.repairRepository = repairRepository;
    }

    // =====================================================
    // SUMMARY
    // =====================================================

    @Override
    public DashboardSummaryDTO getSummary(Integer year, Long userId, Set<String> roles) {
        List<Order> scope = scopedOrders(year, userId, roles);

        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setYear(year);
        dto.setScope(primaryScope(roles));
        dto.setTotalOrders((long) scope.size());

        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) byStatus.put(s, 0L);
        for (Order o : scope) {
            byStatus.merge(o.getStatus(), 1L, Long::sum);
        }
        dto.setOrdersByStatus(byStatus);

        BigDecimal revContract = scope.stream()
                .map(Order::getTotalAfterVat)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalRevenueContract(revContract);

        BigDecimal revPaid = scope.stream()
                .map(o -> paymentRepository.sumPaidByOrderId(o.getOrderId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalRevenuePaid(revPaid);
        dto.setTotalOutstanding(revContract.subtract(revPaid));

        LocalDate today = LocalDate.now();
        LocalDate in7 = today.plusDays(7);

        long lateDelivery = packingBatchRepository.findOverdue(today).stream()
                .filter(pb -> isInScope(pb.getOrder(), scope))
                .count();
        dto.setLateDeliveryCount(lateDelivery);

        // upcoming: contract_delivery between today..+7 AND actual=null
        long upcoming = packingBatchRepository.findAll().stream()
                .filter(pb -> pb.getContractDeliveryDate() != null
                        && !pb.getContractDeliveryDate().isBefore(today)
                        && !pb.getContractDeliveryDate().isAfter(in7)
                        && pb.getActualDeliveryDate() == null
                        && isInScope(pb.getOrder(), scope))
                .count();
        dto.setUpcomingDeliveryCount(upcoming);

        long overdueTailor = tailorAssignmentRepository.findOverdue(today).stream()
                .filter(ta -> isInScope(ta.getOrderItem() != null ? ta.getOrderItem().getOrder() : null, scope))
                .count();
        dto.setOverdueTailorCount(overdueTailor);

        long pendingQc = scope.stream()
                .flatMap(o -> qcRepository.findByOrderId(o.getOrderId()).stream())
                .filter(qc -> qc.getStatus() != null
                        && (qc.getStatus().name().equals("PENDING")
                            || qc.getStatus().name().equals("IN_PROGRESS")))
                .count();
        dto.setPendingQcCount(pendingQc);

        long unresolvedMissing = scope.stream()
                .flatMap(o -> missingItemRepository.findUnresolvedByOrderId(o.getOrderId()).stream())
                .count();
        dto.setUnresolvedMissingCount(unresolvedMissing);

        long activeRepair = scope.stream()
                .mapToLong(o -> repairRepository.countActiveByOrderId(o.getOrderId()))
                .sum();
        dto.setActiveRepairCount(activeRepair);

        long expiring = guaranteeRepository.findExpiringBetween(today, today.plusDays(30)).stream()
                .filter(g -> isInScope(g.getOrder(), scope))
                .count();
        dto.setExpiringGuaranteeCount(expiring);

        long expired = guaranteeRepository.findExpired(today).stream()
                .filter(g -> isInScope(g.getOrder(), scope))
                .count();
        dto.setExpiredGuaranteeCount(expired);

        long overduePayment = paymentRepository.findOverdue(today).stream()
                .filter(p -> isInScope(p.getOrder(), scope))
                .count();
        dto.setOverduePaymentCount(overduePayment);

        return dto;
    }

    // =====================================================
    // REPORTS (36-col)
    // =====================================================

    @Override
    public List<DashboardRowDTO> getReports(
            Integer year,
            String status,
            String province,
            Long customerId,
            Long userId,
            Set<String> roles) {

        List<Order> scope = scopedOrders(year, userId, roles);

        return scope.stream()
                .filter(o -> status == null || o.getStatus().name().equals(status))
                .filter(o -> province == null
                        || (o.getCustomer() != null
                            && province.equalsIgnoreCase(o.getCustomer().getProvince())))
                .filter(o -> customerId == null
                        || (o.getCustomer() != null
                            && customerId.equals(o.getCustomer().getCustomerId())))
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    // =====================================================
    // CUSTOMER ROLLUP
    // =====================================================

    @Override
    public CustomerRollupDTO getCustomerRollup(Long customerId, Integer year) {
        Customer parent = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));

        CustomerRollupDTO dto = new CustomerRollupDTO();
        dto.setParentCustomerId(customerId);
        if (parent.getUnit() != null) dto.setParentName(parent.getUnit().getUnitName());
        dto.setYear(year);

        List<Customer> children = customerRepository.findByParentCustomerCustomerId(customerId);
        dto.setChildren(children.stream().map(CustomerDTO::from).collect(Collectors.toList()));

        // Aggregate across parent + children
        List<Long> allIds = new ArrayList<>();
        allIds.add(customerId);
        children.forEach(c -> allIds.add(c.getCustomerId()));

        List<Order> orders = orderRepository.findAllWithRelations().stream()
                .filter(o -> o.getCustomer() != null
                        && allIds.contains(o.getCustomer().getCustomerId()))
                .filter(o -> year == null || year.equals(o.getContractYear()))
                .collect(Collectors.toList());

        dto.setTotalOrders((long) orders.size());
        BigDecimal contract = orders.stream()
                .map(Order::getTotalAfterVat)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalRevenueContract(contract);

        BigDecimal paid = orders.stream()
                .map(o -> paymentRepository.sumPaidByOrderId(o.getOrderId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalRevenuePaid(paid);
        dto.setTotalOutstanding(contract.subtract(paid));

        dto.setSuccessOrders(orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.SUCCESS
                        || o.getStatus() == OrderStatus.LIQUIDATED).count());
        dto.setCancelledOrders(orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());
        dto.setActiveOrders(orders.stream()
                .filter(o -> !o.getStatus().isTerminal()).count());

        return dto;
    }

    // =====================================================
    // INTERNAL
    // =====================================================

    /**
     * Role-based scope filter (per decision B6, 2026-04-18).
     * ADMIN: all
     * SALES: own (sales_person_user_id = userId)
     * DESIGNER/KCS/PACKER/REPAIRER: orders with active phase tương ứng
     * MEASUREMENT/PRODUCTION/STOCKKEEPER: orders at phase họ phụ trách
     */
    private List<Order> scopedOrders(Integer year, Long userId, Set<String> roles) {
        List<Order> all = orderRepository.findAllWithRelations();
        if (year != null) {
            all = all.stream()
                    .filter(o -> year.equals(o.getContractYear()))
                    .collect(Collectors.toList());
        }
        if (roles.contains("ADMIN")) return all;
        // PURCHASER quản lý vật tư cho toàn bộ đơn → thấy all
        if (roles.contains("PURCHASER")) return all;
        if (roles.contains("SALES")) {
            return all.stream()
                    .filter(o -> o.getSalesPersonUser() != null
                            && userId != null
                            && userId.equals(o.getSalesPersonUser().getUserId()))
                    .collect(Collectors.toList());
        }
        // phase-based roles → filter by current_phase
        return all.stream()
                .filter(o -> phaseMatchesRole(o, roles))
                .collect(Collectors.toList());
    }

    private boolean phaseMatchesRole(Order o, Set<String> roles) {
        String phase = o.getCurrentPhase() != null ? o.getCurrentPhase().name() : "";
        if (roles.contains("MEASUREMENT") && phase.equals("MEASUREMENT_INPUT")) return true;
        if (roles.contains("PRODUCTION") && phase.equals("PRODUCTION_INPUT")) return true;
        if (roles.contains("STOCKKEEPER") && phase.equals("STOCKKEEPER_INPUT")) return true;
        // Flag-based active phases for G4/G7
        if (roles.contains("DESIGNER") && !Boolean.TRUE.equals(o.getSkipDesign())
                && !Boolean.TRUE.equals(o.getDesignReady())) return true;
        if (roles.contains("KCS") && !Boolean.TRUE.equals(o.getSkipKcs())
                && !Boolean.TRUE.equals(o.getQcPassed())) return true;
        if (roles.contains("PACKER")) return true;     // packers see all for packing view
        if (roles.contains("REPAIRER") && Boolean.TRUE.equals(o.getHasRepair())) return true;
        return false;
    }

    private boolean isInScope(Order target, List<Order> scope) {
        if (target == null) return false;
        return scope.stream().anyMatch(o -> Objects.equals(o.getOrderId(), target.getOrderId()));
    }

    private String primaryScope(Set<String> roles) {
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("SALES")) return "SALES";
        for (String r : roles) return r;
        return "NONE";
    }

    private DashboardRowDTO toRow(Order o) {
        DashboardRowDTO r = new DashboardRowDTO();
        r.setOrderId(o.getOrderId());
        r.setOrderCode(o.getOrderCode());
        r.setLarkLegacyId(o.getLarkLegacyId());

        if (o.getCustomer() != null) {
            r.setCustomerId(o.getCustomer().getCustomerId());
            if (o.getCustomer().getUnit() != null) {
                r.setUnitId(o.getCustomer().getUnit().getUnitId());
                r.setUnitName(o.getCustomer().getUnit().getUnitName());
            }
            r.setProvince(o.getCustomer().getProvince());
            if (o.getCustomer().getCustomerType() != null) {
                r.setCustomerType(o.getCustomer().getCustomerType().name());
            }
        }
        r.setUnitType(o.getUnitType());
        r.setContractYear(o.getContractYear());
        r.setStatus(o.getStatus());
        r.setCurrentPhase(o.getCurrentPhase() != null ? o.getCurrentPhase().name() : null);
        r.setSalesPersonName(salesPersonName(o));

        r.setMeasurementStart(o.getMeasurementStart());
        r.setMeasurementEnd(o.getMeasurementEnd());
        r.setMeasurementReceivedDate(o.getMeasurementReceivedDate());
        r.setMeasurementHandoverDate(o.getMeasurementHandoverDateV2());
        if (o.getMeasurementTakerUser() != null) {
            r.setMeasurementTakerName(o.getMeasurementTakerUser().getFullName());
        }
        if (o.getMeasurementComposerUser() != null) {
            r.setMeasurementComposerName(o.getMeasurementComposerUser().getFullName());
        }

        r.setExpectedDeliveryDate(o.getExpectedDeliveryDate());

        // Latest packing batch
        List<PackingBatch> batches = packingBatchRepository.findByOrderId(o.getOrderId());
        if (!batches.isEmpty()) {
            PackingBatch latest = batches.get(0);
            r.setLatestDeliveryStatus(latest.getDeliveryStatus());
            r.setContractDeliveryDate(latest.getContractDeliveryDate());
            if (r.getExpectedDeliveryDate() == null) {
                r.setExpectedDeliveryDate(latest.getExpectedDeliveryDate());
            }
            r.setActualDeliveryDate(latest.getActualDeliveryDate());
        }

        // Invoice
        List<Invoice> invs = invoiceRepository.findByOrderOrderIdOrderByIssuedDateDesc(o.getOrderId());
        Invoice latestInv = invs.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.ISSUED)
                .findFirst()
                .orElse(invs.isEmpty() ? null : invs.get(0));
        if (latestInv != null) {
            r.setInvoiceStatus(latestInv.getStatus());
            r.setInvoiceIssuedDate(latestInv.getIssuedDate());
            r.setInvoiceNumber(latestInv.getInvoiceNumber());
        }

        // Financial
        r.setTotalBeforeVat(o.getTotalBeforeVat());
        r.setVatAmount(o.getVatAmount());
        r.setTotalAfterVat(o.getTotalAfterVat());
        BigDecimal advanceTotal = advanceRepository.sumByOrderId(o.getOrderId());
        BigDecimal paidTotal = paymentRepository.sumPaidByOrderId(o.getOrderId());
        r.setTotalAdvance(advanceTotal);
        r.setTotalPaid(paidTotal);
        BigDecimal after = o.getTotalAfterVat() != null ? o.getTotalAfterVat() : BigDecimal.ZERO;
        r.setRemaining(after.subtract(advanceTotal).subtract(paidTotal));

        // Latest advance
        List<Advance> advances = advanceRepository.findByOrderOrderIdOrderByAdvanceDateDesc(o.getOrderId());
        if (!advances.isEmpty()) {
            Advance a = advances.get(0);
            r.setLatestAdvanceDate(a.getAdvanceDate());
            r.setLatestAdvanceBank(a.getBank());
        }

        // Latest payment (actual or scheduled)
        List<Payment> pays = paymentRepository.findByOrderOrderIdOrderByScheduledDateAsc(o.getOrderId());
        Payment paidPayment = pays.stream()
                .filter(p -> p.getStatus() == PaymentStatus.CONFIRMED || p.getStatus() == PaymentStatus.PAID)
                .reduce((first, second) -> second).orElse(null); // last one
        if (paidPayment != null) {
            r.setLatestPaymentScheduledDate(paidPayment.getScheduledDate());
            r.setLatestPaymentActualDate(paidPayment.getActualDate());
            r.setLatestPaymentBank(paidPayment.getBank());
        } else if (!pays.isEmpty()) {
            Payment p = pays.get(0);
            r.setLatestPaymentScheduledDate(p.getScheduledDate());
            r.setLatestPaymentActualDate(p.getActualDate());
            r.setLatestPaymentBank(p.getBank());
        }

        // Guarantees (bucket by type)
        List<Guarantee> gs = guaranteeRepository.findByOrderOrderIdOrderByTypeAsc(o.getOrderId());
        for (Guarantee g : gs) {
            if (g.getType() == GuaranteeType.BIDDING) {
                r.setBiddingGuaranteeForm(g.getForm() != null ? g.getForm().name() : null);
                r.setBiddingGuaranteeAmount(g.getAmount());
                r.setBiddingGuaranteeExpiry(g.getExpiryDate());
            } else if (g.getType() == GuaranteeType.PERFORMANCE) {
                r.setPerformanceGuaranteeForm(g.getForm() != null ? g.getForm().name() : null);
                r.setPerformanceGuaranteeAmount(g.getAmount());
                r.setPerformanceGuaranteeExpiry(g.getExpiryDate());
            } else if (g.getType() == GuaranteeType.WARRANTY) {
                r.setWarrantyGuaranteeForm(g.getForm() != null ? g.getForm().name() : null);
                r.setWarrantyGuaranteeAmount(g.getAmount());
                r.setWarrantyGuaranteeExpiry(g.getExpiryDate());
            }
        }

        // Flags
        long missing = missingItemRepository.findUnresolvedByOrderId(o.getOrderId()).size();
        r.setHasMissing(missing > 0);
        r.setHasRepair(Boolean.TRUE.equals(o.getHasRepair()));

        // Days late
        LocalDate target = r.getContractDeliveryDate() != null
                ? r.getContractDeliveryDate() : r.getExpectedDeliveryDate();
        if (target != null && r.getActualDeliveryDate() == null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(target, LocalDate.now());
            if (days > 0) r.setDaysLate(days);
        }

        return r;
    }

    private String salesPersonName(Order o) {
        if (o.getSalesPersonUser() != null) return o.getSalesPersonUser().getFullName();
        return o.getSalesPersonName();
    }
}
