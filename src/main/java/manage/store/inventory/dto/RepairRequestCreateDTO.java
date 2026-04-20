package manage.store.inventory.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import manage.store.inventory.entity.enums.LogisticsMethod;
import manage.store.inventory.entity.enums.RepairStatus;

@Data
public class RepairRequestCreateDTO {

    @NotNull(message = "Mặt hàng không được trống")
    private Long orderItemId;

    private Long packingBatchId;
    private String batchNumber;
    private LocalDate receivedDate;
    private Long receiverUserId;
    private LogisticsMethod receiveMethod;
    private LocalDate expectedCompletionDate;

    @Min(value = 0, message = "Số lượng không âm")
    private Integer qtyRepair;

    private String repairDetails;
    private LocalDate returnDate;
    private LogisticsMethod returnMethod;
    private Long returnHandlerUserId;
    private String parentBatches;
    private String reasonForReturn;
    private RepairStatus status;
    private String note;
}
