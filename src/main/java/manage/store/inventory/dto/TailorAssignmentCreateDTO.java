package manage.store.inventory.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import manage.store.inventory.entity.enums.TailorAssignmentStatus;
import manage.store.inventory.entity.enums.TailorType;

@Data
public class TailorAssignmentCreateDTO {

    @NotNull(message = "Thợ không được trống")
    private Long tailorId;

    @Min(value = 0, message = "Số lượng không âm")
    private Integer qtyAssigned = 0;

    @Min(value = 0, message = "Số lượng bốc tồn kho không âm")
    private Integer qtyFromStock = 0;

    @Min(value = 0, message = "Số lượng đã trả không âm")
    private Integer qtyReturned = 0;

    private LocalDate appointmentDate;
    private LocalDate returnedDate;
    private TailorType tailorType;
    private String nplProposalUrl;
    private TailorAssignmentStatus status = TailorAssignmentStatus.PLANNED;
    private String note;
}
