package manage.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DesignDocumentCreateDTO {

    @NotBlank(message = "URL file không được trống")
    private String fileUrl;

    private String fileName;
    private String note;
}
