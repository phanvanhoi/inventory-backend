package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * Tổng hợp tài chính 1 đơn: advance + payment + invoice + computed totals.
 * Endpoint: GET /api/orders/{id}/financial
 */
@Data
public class OrderFinancialDTO {
    private Long orderId;
    private BigDecimal totalAfterVat;        // Tổng giá trị HĐ
    private BigDecimal totalAdvance;          // Tổng tạm ứng
    private BigDecimal totalPaid;             // Tổng đã thanh toán (status IN PAID,CONFIRMED)
    private BigDecimal remaining;             // Còn lại = totalAfterVat - totalAdvance - totalPaid
    private List<AdvanceDTO> advances;
    private List<PaymentDTO> payments;
    private List<InvoiceDTO> invoices;
}
