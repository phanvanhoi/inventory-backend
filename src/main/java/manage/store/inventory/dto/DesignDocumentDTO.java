package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.DesignDocument;

@Data
public class DesignDocumentDTO {
    private Long designDocId;
    private Long orderId;
    private String fileUrl;
    private String fileName;
    private Long uploadedByUserId;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
    private String note;

    public static DesignDocumentDTO from(DesignDocument dd) {
        if (dd == null) return null;
        DesignDocumentDTO dto = new DesignDocumentDTO();
        dto.setDesignDocId(dd.getDesignDocId());
        if (dd.getOrder() != null) dto.setOrderId(dd.getOrder().getOrderId());
        dto.setFileUrl(dd.getFileUrl());
        dto.setFileName(dd.getFileName());
        if (dd.getUploadedByUser() != null) {
            dto.setUploadedByUserId(dd.getUploadedByUser().getUserId());
            dto.setUploadedByName(dd.getUploadedByUser().getFullName());
        }
        dto.setUploadedAt(dd.getUploadedAt());
        dto.setNote(dd.getNote());
        return dto;
    }
}
