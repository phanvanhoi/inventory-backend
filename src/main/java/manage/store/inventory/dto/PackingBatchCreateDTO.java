package manage.store.inventory.dto;

import java.time.LocalDate;

import lombok.Data;
import manage.store.inventory.entity.enums.DeliveryStatus;
import manage.store.inventory.entity.enums.PackingBatchStatus;

@Data
public class PackingBatchCreateDTO {
    private Long packerUserId;
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
}
