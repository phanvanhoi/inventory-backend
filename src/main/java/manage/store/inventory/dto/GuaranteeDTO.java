package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.Guarantee;
import manage.store.inventory.entity.enums.GuaranteeForm;
import manage.store.inventory.entity.enums.GuaranteeType;

@Data
public class GuaranteeDTO {
    private Long guaranteeId;
    private Long orderId;
    private GuaranteeType type;
    private GuaranteeForm form;
    private BigDecimal amount;
    private LocalDate expiryDate;
    private String bank;
    private String note;
    private LocalDateTime createdAt;

    // Computed — số ngày đến khi hết hạn (âm = đã hết hạn)
    private Long daysUntilExpiry;

    public static GuaranteeDTO from(Guarantee g) {
        if (g == null) return null;
        GuaranteeDTO dto = new GuaranteeDTO();
        dto.setGuaranteeId(g.getGuaranteeId());
        if (g.getOrder() != null) dto.setOrderId(g.getOrder().getOrderId());
        dto.setType(g.getType());
        dto.setForm(g.getForm());
        dto.setAmount(g.getAmount());
        dto.setExpiryDate(g.getExpiryDate());
        dto.setBank(g.getBank());
        dto.setNote(g.getNote());
        dto.setCreatedAt(g.getCreatedAt());

        if (g.getExpiryDate() != null) {
            dto.setDaysUntilExpiry(
                    java.time.temporal.ChronoUnit.DAYS.between(
                            java.time.LocalDate.now(), g.getExpiryDate()));
        }
        return dto;
    }
}
