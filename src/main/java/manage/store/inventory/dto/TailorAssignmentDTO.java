package manage.store.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.TailorAssignment;
import manage.store.inventory.entity.enums.TailorAssignmentStatus;
import manage.store.inventory.entity.enums.TailorType;

@Data
public class TailorAssignmentDTO {
    private Long assignmentId;
    private Long orderItemId;
    private String productName;
    private Long orderId;
    private Long tailorId;
    private String tailorName;
    private Integer qtyAssigned;
    private Integer qtyFromStock;
    private Integer qtyReturned;
    private LocalDate appointmentDate;
    private LocalDate returnedDate;
    private TailorType tailorType;
    private String nplProposalUrl;
    private TailorAssignmentStatus status;
    private String note;
    private LocalDateTime createdAt;

    public static TailorAssignmentDTO from(TailorAssignment ta) {
        if (ta == null) return null;
        TailorAssignmentDTO dto = new TailorAssignmentDTO();
        dto.setAssignmentId(ta.getAssignmentId());
        if (ta.getOrderItem() != null) {
            dto.setOrderItemId(ta.getOrderItem().getOrderItemId());
            dto.setProductName(ta.getOrderItem().getProductName());
            if (ta.getOrderItem().getOrder() != null) {
                dto.setOrderId(ta.getOrderItem().getOrder().getOrderId());
            }
        }
        if (ta.getTailor() != null) {
            dto.setTailorId(ta.getTailor().getTailorId());
            dto.setTailorName(ta.getTailor().getName());
        }
        dto.setQtyAssigned(ta.getQtyAssigned());
        dto.setQtyFromStock(ta.getQtyFromStock());
        dto.setQtyReturned(ta.getQtyReturned());
        dto.setAppointmentDate(ta.getAppointmentDate());
        dto.setReturnedDate(ta.getReturnedDate());
        dto.setTailorType(ta.getTailorType());
        dto.setNplProposalUrl(ta.getNplProposalUrl());
        dto.setStatus(ta.getStatus());
        dto.setNote(ta.getNote());
        dto.setCreatedAt(ta.getCreatedAt());
        return dto;
    }
}
