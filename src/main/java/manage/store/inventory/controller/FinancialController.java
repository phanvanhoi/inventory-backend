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
import manage.store.inventory.dto.AdvanceCreateDTO;
import manage.store.inventory.dto.AdvanceDTO;
import manage.store.inventory.dto.GuaranteeCreateDTO;
import manage.store.inventory.dto.GuaranteeDTO;
import manage.store.inventory.dto.InvoiceCreateDTO;
import manage.store.inventory.dto.InvoiceDTO;
import manage.store.inventory.dto.OrderFinancialDTO;
import manage.store.inventory.dto.PaymentCreateDTO;
import manage.store.inventory.dto.PaymentDTO;
import manage.store.inventory.service.FinancialService;

/**
 * Financial endpoints (G2a, W6): Advance + Payment + Invoice per order.
 * Guarantees hoãn đến G2b (W21).
 */
@RestController
@RequestMapping("/api")
public class FinancialController {

    private final FinancialService financialService;

    public FinancialController(FinancialService financialService) {
        this.financialService = financialService;
    }

    // ===== Aggregate =====

    @GetMapping("/orders/{orderId}/financial")
    public OrderFinancialDTO getOrderFinancial(@PathVariable Long orderId) {
        return financialService.getOrderFinancial(orderId);
    }

    // ===== Advances =====

    @GetMapping("/orders/{orderId}/advances")
    public List<AdvanceDTO> getAdvances(@PathVariable Long orderId) {
        return financialService.getAdvancesByOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/advances")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Long> addAdvance(
            @PathVariable Long orderId,
            @Valid @RequestBody AdvanceCreateDTO dto) {
        return ResponseEntity.ok(financialService.addAdvance(orderId, dto));
    }

    @PutMapping("/advances/{advanceId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> updateAdvance(
            @PathVariable Long advanceId,
            @Valid @RequestBody AdvanceCreateDTO dto) {
        financialService.updateAdvance(advanceId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/advances/{advanceId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> deleteAdvance(@PathVariable Long advanceId) {
        financialService.deleteAdvance(advanceId);
        return ResponseEntity.ok().build();
    }

    // ===== Payments =====

    @GetMapping("/orders/{orderId}/payments")
    public List<PaymentDTO> getPayments(@PathVariable Long orderId) {
        return financialService.getPaymentsByOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/payments")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Long> addPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentCreateDTO dto) {
        return ResponseEntity.ok(financialService.addPayment(orderId, dto));
    }

    @PutMapping("/payments/{paymentId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> updatePayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCreateDTO dto) {
        financialService.updatePayment(paymentId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/payments/{paymentId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        financialService.deletePayment(paymentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payments/overdue")
    public List<PaymentDTO> getOverduePayments() {
        return financialService.getOverduePayments(LocalDate.now());
    }

    // ===== Invoices =====

    @GetMapping("/orders/{orderId}/invoices")
    public List<InvoiceDTO> getInvoices(@PathVariable Long orderId) {
        return financialService.getInvoicesByOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/invoices")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Long> addInvoice(
            @PathVariable Long orderId,
            @Valid @RequestBody InvoiceCreateDTO dto) {
        return ResponseEntity.ok(financialService.addInvoice(orderId, dto));
    }

    @PutMapping("/invoices/{invoiceId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> updateInvoice(
            @PathVariable Long invoiceId,
            @Valid @RequestBody InvoiceCreateDTO dto) {
        financialService.updateInvoice(invoiceId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/invoices/{invoiceId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long invoiceId) {
        financialService.deleteInvoice(invoiceId);
        return ResponseEntity.ok().build();
    }

    // ===== Guarantees (G2b, V28) =====

    @GetMapping("/orders/{orderId}/guarantees")
    public List<GuaranteeDTO> getGuarantees(@PathVariable Long orderId) {
        return financialService.getGuaranteesByOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/guarantees")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Long> addGuarantee(
            @PathVariable Long orderId,
            @Valid @RequestBody GuaranteeCreateDTO dto) {
        return ResponseEntity.ok(financialService.addGuarantee(orderId, dto));
    }

    @PutMapping("/guarantees/{guaranteeId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> updateGuarantee(
            @PathVariable Long guaranteeId,
            @Valid @RequestBody GuaranteeCreateDTO dto) {
        financialService.updateGuarantee(guaranteeId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/guarantees/{guaranteeId}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> deleteGuarantee(@PathVariable Long guaranteeId) {
        financialService.deleteGuarantee(guaranteeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/guarantees/expiring")
    public List<GuaranteeDTO> getExpiring(
            @RequestParam(defaultValue = "30") int daysAhead) {
        return financialService.getExpiringGuarantees(daysAhead);
    }

    @GetMapping("/guarantees/expired")
    public List<GuaranteeDTO> getExpired() {
        return financialService.getExpiredGuarantees();
    }
}
