package manage.store.inventory.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractReportAlertDTO {

    private Long reportId;
    private String unitName;
    private String salesPerson;
    private String alertType;       // LATE_DELIVERY, UPCOMING_DELIVERY, LATE_TAILOR_RETURN
    private LocalDate expectedDate;
    private Integer daysOverdue;    // So ngay qua han (null neu chua qua)
}
