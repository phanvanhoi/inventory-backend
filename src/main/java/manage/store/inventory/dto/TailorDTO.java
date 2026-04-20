package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.Tailor;
import manage.store.inventory.entity.enums.TailorType;

@Data
public class TailorDTO {
    private Long tailorId;
    private String name;
    private TailorType type;
    private String phone;
    private String location;
    private Boolean active;
    private String note;
    private LocalDateTime createdAt;

    public static TailorDTO from(Tailor t) {
        if (t == null) return null;
        TailorDTO dto = new TailorDTO();
        dto.setTailorId(t.getTailorId());
        dto.setName(t.getName());
        dto.setType(t.getType());
        dto.setPhone(t.getPhone());
        dto.setLocation(t.getLocation());
        dto.setActive(t.getActive());
        dto.setNote(t.getNote());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }
}
