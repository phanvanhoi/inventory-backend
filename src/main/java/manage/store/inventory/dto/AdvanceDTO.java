package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.Advance;

@Data
public class AdvanceDTO {
    private Long advanceId;
    private Long orderId;
    private BigDecimal amount;
    private LocalDate advanceDate;
    private String bank;
    private String note;
    private LocalDateTime createdAt;

    public static AdvanceDTO from(Advance a) {
        if (a == null) return null;
        AdvanceDTO dto = new AdvanceDTO();
        dto.setAdvanceId(a.getAdvanceId());
        if (a.getOrder() != null) dto.setOrderId(a.getOrder().getOrderId());
        dto.setAmount(a.getAmount());
        dto.setAdvanceDate(a.getAdvanceDate());
        dto.setBank(a.getBank());
        dto.setNote(a.getNote());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
