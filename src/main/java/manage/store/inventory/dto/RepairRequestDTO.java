package manage.store.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.RepairRequest;
import manage.store.inventory.entity.enums.LogisticsMethod;
import manage.store.inventory.entity.enums.RepairStatus;

@Data
public class RepairRequestDTO {
    private Long repairId;
    private Long packingBatchId;
    private Long orderItemId;
    private String productName;
    private Long orderId;
    private String batchNumber;
    private LocalDate receivedDate;
    private Long receiverUserId;
    private String receiverName;
    private LogisticsMethod receiveMethod;
    private LocalDate expectedCompletionDate;
    private Integer qtyRepair;
    private String repairDetails;
    private LocalDate returnDate;
    private LogisticsMethod returnMethod;
    private Long returnHandlerUserId;
    private String returnHandlerName;
    private String parentBatches;
    private String reasonForReturn;
    private RepairStatus status;
    private String note;
    private LocalDateTime createdAt;

    public static RepairRequestDTO from(RepairRequest r) {
        if (r == null) return null;
        RepairRequestDTO dto = new RepairRequestDTO();
        dto.setRepairId(r.getRepairId());
        if (r.getPackingBatch() != null) dto.setPackingBatchId(r.getPackingBatch().getPackingBatchId());
        if (r.getOrderItem() != null) {
            dto.setOrderItemId(r.getOrderItem().getOrderItemId());
            dto.setProductName(r.getOrderItem().getProductName());
            if (r.getOrderItem().getOrder() != null) {
                dto.setOrderId(r.getOrderItem().getOrder().getOrderId());
            }
        }
        dto.setBatchNumber(r.getBatchNumber());
        dto.setReceivedDate(r.getReceivedDate());
        if (r.getReceiverUser() != null) {
            dto.setReceiverUserId(r.getReceiverUser().getUserId());
            dto.setReceiverName(r.getReceiverUser().getFullName());
        }
        dto.setReceiveMethod(r.getReceiveMethod());
        dto.setExpectedCompletionDate(r.getExpectedCompletionDate());
        dto.setQtyRepair(r.getQtyRepair());
        dto.setRepairDetails(r.getRepairDetails());
        dto.setReturnDate(r.getReturnDate());
        dto.setReturnMethod(r.getReturnMethod());
        if (r.getReturnHandlerUser() != null) {
            dto.setReturnHandlerUserId(r.getReturnHandlerUser().getUserId());
            dto.setReturnHandlerName(r.getReturnHandlerUser().getFullName());
        }
        dto.setParentBatches(r.getParentBatches());
        dto.setReasonForReturn(r.getReasonForReturn());
        dto.setStatus(r.getStatus());
        dto.setNote(r.getNote());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
