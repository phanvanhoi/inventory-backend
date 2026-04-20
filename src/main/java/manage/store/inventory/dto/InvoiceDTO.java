package manage.store.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.Invoice;
import manage.store.inventory.entity.enums.InvoiceStatus;

@Data
public class InvoiceDTO {
    private Long invoiceId;
    private Long orderId;
    private InvoiceStatus status;
    private LocalDate issuedDate;
    private String invoiceNumber;
    private String note;
    private LocalDateTime createdAt;

    public static InvoiceDTO from(Invoice i) {
        if (i == null) return null;
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(i.getInvoiceId());
        if (i.getOrder() != null) dto.setOrderId(i.getOrder().getOrderId());
        dto.setStatus(i.getStatus());
        dto.setIssuedDate(i.getIssuedDate());
        dto.setInvoiceNumber(i.getInvoiceNumber());
        dto.setNote(i.getNote());
        dto.setCreatedAt(i.getCreatedAt());
        return dto;
    }
}
