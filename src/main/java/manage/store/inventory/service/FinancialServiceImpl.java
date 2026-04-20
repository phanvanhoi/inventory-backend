package manage.store.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.AdvanceCreateDTO;
import manage.store.inventory.dto.AdvanceDTO;
import manage.store.inventory.dto.InvoiceCreateDTO;
import manage.store.inventory.dto.InvoiceDTO;
import manage.store.inventory.dto.OrderFinancialDTO;
import manage.store.inventory.dto.PaymentCreateDTO;
import manage.store.inventory.dto.PaymentDTO;
import manage.store.inventory.entity.Advance;
import manage.store.inventory.entity.Invoice;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.Payment;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.AdvanceRepository;
import manage.store.inventory.repository.InvoiceRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.PaymentRepository;

@Service
@Transactional
public class FinancialServiceImpl implements FinancialService {

    private final OrderRepository orderRepository;
    private final AdvanceRepository advanceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public FinancialServiceImpl(
            OrderRepository orderRepository,
            AdvanceRepository advanceRepository,
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository) {
        this.orderRepository = orderRepository;
        this.advanceRepository = advanceRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    // ===== Aggregate =====

    @Override
    @Transactional(readOnly = true)
    public OrderFinancialDTO getOrderFinancial(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        BigDecimal totalAdvance = advanceRepository.sumByOrderId(orderId);
        BigDecimal totalPaid = paymentRepository.sumPaidByOrderId(orderId);
        BigDecimal totalAfterVat = order.getTotalAfterVat() != null ? order.getTotalAfterVat() : BigDecimal.ZERO;
        BigDecimal remaining = totalAfterVat.subtract(totalAdvance).subtract(totalPaid);

        OrderFinancialDTO dto = new OrderFinancialDTO();
        dto.setOrderId(orderId);
        dto.setTotalAfterVat(totalAfterVat);
        dto.setTotalAdvance(totalAdvance);
        dto.setTotalPaid(totalPaid);
        dto.setRemaining(remaining);
        dto.setAdvances(getAdvancesByOrder(orderId));
        dto.setPayments(getPaymentsByOrder(orderId));
        dto.setInvoices(getInvoicesByOrder(orderId));
        return dto;
    }

    // ===== Advances =====

    @Override
    public Long addAdvance(Long orderId, AdvanceCreateDTO dto) {
        Order order = requireOrder(orderId);
        Advance a = new Advance();
        a.setOrder(order);
        applyAdvance(dto, a);
        a.setCreatedAt(LocalDateTime.now());
        advanceRepository.save(a);
        return a.getAdvanceId();
    }

    @Override
    public void updateAdvance(Long advanceId, AdvanceCreateDTO dto) {
        Advance a = advanceRepository.findById(advanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Tạm ứng không tồn tại"));
        applyAdvance(dto, a);
        advanceRepository.save(a);
    }

    @Override
    public void deleteAdvance(Long advanceId) {
        if (!advanceRepository.existsById(advanceId)) {
            throw new ResourceNotFoundException("Tạm ứng không tồn tại");
        }
        advanceRepository.deleteById(advanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceDTO> getAdvancesByOrder(Long orderId) {
        return advanceRepository.findByOrderOrderIdOrderByAdvanceDateDesc(orderId).stream()
                .map(AdvanceDTO::from)
                .collect(Collectors.toList());
    }

    // ===== Payments =====

    @Override
    public Long addPayment(Long orderId, PaymentCreateDTO dto) {
        Order order = requireOrder(orderId);
        Payment p = new Payment();
        p.setOrder(order);
        applyPayment(dto, p);
        p.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(p);
        return p.getPaymentId();
    }

    @Override
    public void updatePayment(Long paymentId, PaymentCreateDTO dto) {
        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Thanh toán không tồn tại"));
        applyPayment(dto, p);
        paymentRepository.save(p);
    }

    @Override
    public void deletePayment(Long paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new ResourceNotFoundException("Thanh toán không tồn tại");
        }
        paymentRepository.deleteById(paymentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderOrderIdOrderByScheduledDateAsc(orderId).stream()
                .map(PaymentDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getOverduePayments(LocalDate today) {
        return paymentRepository.findOverdue(today).stream()
                .map(PaymentDTO::from)
                .collect(Collectors.toList());
    }

    // ===== Invoices =====

    @Override
    public Long addInvoice(Long orderId, InvoiceCreateDTO dto) {
        Order order = requireOrder(orderId);
        Invoice i = new Invoice();
        i.setOrder(order);
        applyInvoice(dto, i);
        i.setCreatedAt(LocalDateTime.now());
        invoiceRepository.save(i);
        return i.getInvoiceId();
    }

    @Override
    public void updateInvoice(Long invoiceId, InvoiceCreateDTO dto) {
        Invoice i = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hoá đơn không tồn tại"));
        applyInvoice(dto, i);
        invoiceRepository.save(i);
    }

    @Override
    public void deleteInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Hoá đơn không tồn tại");
        }
        invoiceRepository.deleteById(invoiceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByOrder(Long orderId) {
        return invoiceRepository.findByOrderOrderIdOrderByIssuedDateDesc(orderId).stream()
                .map(InvoiceDTO::from)
                .collect(Collectors.toList());
    }

    // ===== helpers =====

    private Order requireOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
    }

    private void applyAdvance(AdvanceCreateDTO dto, Advance a) {
        if (dto.getAmount() != null) a.setAmount(dto.getAmount());
        a.setAdvanceDate(dto.getAdvanceDate());
        a.setBank(dto.getBank());
        a.setNote(dto.getNote());
    }

    private void applyPayment(PaymentCreateDTO dto, Payment p) {
        if (dto.getAmount() != null) p.setAmount(dto.getAmount());
        p.setScheduledDate(dto.getScheduledDate());
        p.setActualDate(dto.getActualDate());
        p.setBank(dto.getBank());
        if (dto.getStatus() != null) p.setStatus(dto.getStatus());
        p.setNote(dto.getNote());
    }

    private void applyInvoice(InvoiceCreateDTO dto, Invoice i) {
        if (dto.getStatus() != null) i.setStatus(dto.getStatus());
        i.setIssuedDate(dto.getIssuedDate());
        i.setInvoiceNumber(dto.getInvoiceNumber());
        i.setNote(dto.getNote());
    }
}
