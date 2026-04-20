package manage.store.inventory.dto;

import java.time.LocalDate;

import lombok.Data;
import manage.store.inventory.entity.enums.QualityCheckStatus;

@Data
public class QualityCheckCreateDTO {
    private Long tailorAssignmentId;
    private Long kcsUserId;
    private LocalDate receivedDate;
    private LocalDate completedDate;
    private Boolean fullDocumentsReceived;
    private Boolean fullVariantsReceived;
    // Null = giữ nguyên status cũ khi update; Create sẽ dùng entity default (PENDING)
    private QualityCheckStatus status;
    private String notes;
}
