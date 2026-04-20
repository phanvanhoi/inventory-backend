package manage.store.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.QualityCheck;
import manage.store.inventory.entity.enums.QualityCheckStatus;

@Data
public class QualityCheckDTO {

    private Long qcId;
    private Long orderItemId;
    private String productName;
    private Long tailorAssignmentId;
    private String tailorName;
    private Long kcsUserId;
    private String kcsUserName;
    private LocalDate receivedDate;
    private LocalDate completedDate;
    private Boolean fullDocumentsReceived;
    private Boolean fullVariantsReceived;
    private QualityCheckStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QualityCheckDTO from(QualityCheck qc) {
        if (qc == null) return null;
        QualityCheckDTO dto = new QualityCheckDTO();
        dto.setQcId(qc.getQcId());
        if (qc.getOrderItem() != null) {
            dto.setOrderItemId(qc.getOrderItem().getOrderItemId());
            dto.setProductName(qc.getOrderItem().getProductName());
        }
        if (qc.getTailorAssignment() != null) {
            dto.setTailorAssignmentId(qc.getTailorAssignment().getAssignmentId());
            if (qc.getTailorAssignment().getTailor() != null) {
                dto.setTailorName(qc.getTailorAssignment().getTailor().getName());
            }
        }
        if (qc.getKcsUser() != null) {
            dto.setKcsUserId(qc.getKcsUser().getUserId());
            dto.setKcsUserName(qc.getKcsUser().getFullName());
        }
        dto.setReceivedDate(qc.getReceivedDate());
        dto.setCompletedDate(qc.getCompletedDate());
        dto.setFullDocumentsReceived(qc.getFullDocumentsReceived());
        dto.setFullVariantsReceived(qc.getFullVariantsReceived());
        dto.setStatus(qc.getStatus());
        dto.setNotes(qc.getNotes());
        dto.setCreatedAt(qc.getCreatedAt());
        dto.setUpdatedAt(qc.getUpdatedAt());
        return dto;
    }
}
