package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistoryDTO {

    private Long historyId;
    private String action;
    private Long performedBy;
    private String performedByName;
    private String reason;
    private LocalDateTime createdAt;
}
