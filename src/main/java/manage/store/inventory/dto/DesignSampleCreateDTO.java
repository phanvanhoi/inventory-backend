package manage.store.inventory.dto;

import lombok.Data;
import manage.store.inventory.entity.enums.DesignSampleStatus;

@Data
public class DesignSampleCreateDTO {
    private String sampleImageUrl;
    private String fabricCode;
    private Long designerUserId;
    private DesignSampleStatus status = DesignSampleStatus.DRAFT;
    private String note;
}
