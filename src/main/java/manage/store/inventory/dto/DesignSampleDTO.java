package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.DesignSample;
import manage.store.inventory.entity.enums.DesignSampleStatus;

@Data
public class DesignSampleDTO {

    private Long designSampleId;
    private Long orderItemId;
    private String productName;
    private String sampleImageUrl;
    private String fabricCode;
    private Long designerUserId;
    private String designerName;
    private DesignSampleStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DesignSampleDTO from(DesignSample ds) {
        if (ds == null) return null;
        DesignSampleDTO dto = new DesignSampleDTO();
        dto.setDesignSampleId(ds.getDesignSampleId());
        if (ds.getOrderItem() != null) {
            dto.setOrderItemId(ds.getOrderItem().getOrderItemId());
            dto.setProductName(ds.getOrderItem().getProductName());
        }
        dto.setSampleImageUrl(ds.getSampleImageUrl());
        dto.setFabricCode(ds.getFabricCode());
        if (ds.getDesignerUser() != null) {
            dto.setDesignerUserId(ds.getDesignerUser().getUserId());
            dto.setDesignerName(ds.getDesignerUser().getFullName());
        }
        dto.setStatus(ds.getStatus());
        dto.setNote(ds.getNote());
        dto.setCreatedAt(ds.getCreatedAt());
        dto.setUpdatedAt(ds.getUpdatedAt());
        return dto;
    }
}
