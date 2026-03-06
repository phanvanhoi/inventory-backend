package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractReportHistoryDTO {

    private Long historyId;
    private String action;       // EDIT, ADVANCE, RETURN
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String reason;
    private String changedByName;
    private LocalDateTime changedAt;
}
