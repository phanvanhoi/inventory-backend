package manage.store.inventory.dto;

import java.time.LocalDate;

import lombok.Data;
import manage.store.inventory.entity.enums.InvoiceStatus;

@Data
public class InvoiceCreateDTO {
    private InvoiceStatus status = InvoiceStatus.NOT_ISSUED;
    private LocalDate issuedDate;
    private String invoiceNumber;
    private String note;
}
