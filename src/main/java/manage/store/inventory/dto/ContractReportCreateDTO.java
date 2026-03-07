package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContractReportCreateDTO {

    @NotNull(message = "Đơn vị không được để trống")
    private Long unitId;

    // SALES fields only
    private String unitType;       // BUU_DIEN, VIEN_THONG, KHAC
    private Integer contractYear;  // Nam hop dong
    private String salesPerson;
    private LocalDate expectedDeliveryDate;
    private LocalDate finalizedListSentDate;
    private LocalDate finalizedListReceivedDate;
    private String deliveryMethod;
    private LocalDate extraPaymentDate;
    private BigDecimal extraPaymentAmount;
    private String note;
}
