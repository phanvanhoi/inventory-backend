package manage.store.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.PackingBatch;
import manage.store.inventory.entity.enums.DeliveryStatus;
import manage.store.inventory.entity.enums.PackingBatchStatus;

@Data
public class PackingBatchDTO {

    private Long packingBatchId;
    private Long orderId;
    private Long packerUserId;
    private String packerName;

    private LocalDate documentsReceivedDate;
    private LocalDate packingStartedDate;
    private LocalDate packingCompletedDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate contractDeliveryDate;
    private LocalDate actualDeliveryDate;

    private DeliveryStatus deliveryStatus;
    private String tickFileUrl;
    private PackingBatchStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed
    private Long daysLate;

    public static PackingBatchDTO from(PackingBatch pb) {
        if (pb == null) return null;
        PackingBatchDTO dto = new PackingBatchDTO();
        dto.setPackingBatchId(pb.getPackingBatchId());
        if (pb.getOrder() != null) dto.setOrderId(pb.getOrder().getOrderId());
        if (pb.getPackerUser() != null) {
            dto.setPackerUserId(pb.getPackerUser().getUserId());
            dto.setPackerName(pb.getPackerUser().getFullName());
        }
        dto.setDocumentsReceivedDate(pb.getDocumentsReceivedDate());
        dto.setPackingStartedDate(pb.getPackingStartedDate());
        dto.setPackingCompletedDate(pb.getPackingCompletedDate());
        dto.setExpectedDeliveryDate(pb.getExpectedDeliveryDate());
        dto.setContractDeliveryDate(pb.getContractDeliveryDate());
        dto.setActualDeliveryDate(pb.getActualDeliveryDate());
        dto.setDeliveryStatus(pb.getDeliveryStatus());
        dto.setTickFileUrl(pb.getTickFileUrl());
        dto.setStatus(pb.getStatus());
        dto.setNote(pb.getNote());
        dto.setCreatedAt(pb.getCreatedAt());
        dto.setUpdatedAt(pb.getUpdatedAt());

        // daysLate: actual_delivery_date null AND contract_delivery_date past
        if (pb.getContractDeliveryDate() != null && pb.getActualDeliveryDate() == null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    pb.getContractDeliveryDate(), java.time.LocalDate.now());
            if (days > 0) dto.setDaysLate(days);
        }
        return dto;
    }
}
