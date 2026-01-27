package manage.store.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSetDetailDTO {

    private Long setId;
    private String setName;
    private String description;
    private String status;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private List<InventoryRequestDetailDTO> requests;
    private List<ApprovalHistoryDTO> approvalHistory;
}
