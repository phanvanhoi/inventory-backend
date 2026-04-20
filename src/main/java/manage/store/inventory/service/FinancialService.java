package manage.store.inventory.service;

import java.time.LocalDate;
import java.util.List;

import manage.store.inventory.dto.AdvanceCreateDTO;
import manage.store.inventory.dto.AdvanceDTO;
import manage.store.inventory.dto.InvoiceCreateDTO;
import manage.store.inventory.dto.InvoiceDTO;
import manage.store.inventory.dto.OrderFinancialDTO;
import manage.store.inventory.dto.PaymentCreateDTO;
import manage.store.inventory.dto.PaymentDTO;

public interface FinancialService {

    // Aggregate
    OrderFinancialDTO getOrderFinancial(Long orderId);

    // Advances
    Long addAdvance(Long orderId, AdvanceCreateDTO dto);
    void updateAdvance(Long advanceId, AdvanceCreateDTO dto);
    void deleteAdvance(Long advanceId);
    List<AdvanceDTO> getAdvancesByOrder(Long orderId);

    // Payments
    Long addPayment(Long orderId, PaymentCreateDTO dto);
    void updatePayment(Long paymentId, PaymentCreateDTO dto);
    void deletePayment(Long paymentId);
    List<PaymentDTO> getPaymentsByOrder(Long orderId);
    List<PaymentDTO> getOverduePayments(LocalDate today);

    // Invoices
    Long addInvoice(Long orderId, InvoiceCreateDTO dto);
    void updateInvoice(Long invoiceId, InvoiceCreateDTO dto);
    void deleteInvoice(Long invoiceId);
    List<InvoiceDTO> getInvoicesByOrder(Long orderId);
}
