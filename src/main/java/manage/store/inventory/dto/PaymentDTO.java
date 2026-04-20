package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.Payment;
import manage.store.inventory.entity.enums.PaymentStatus;

@Data
public class PaymentDTO {
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private LocalDate scheduledDate;
    private LocalDate actualDate;
    private String bank;
    private PaymentStatus status;
    private String note;
    private LocalDateTime createdAt;

    public static PaymentDTO from(Payment p) {
        if (p == null) return null;
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(p.getPaymentId());
        if (p.getOrder() != null) dto.setOrderId(p.getOrder().getOrderId());
        dto.setAmount(p.getAmount());
        dto.setScheduledDate(p.getScheduledDate());
        dto.setActualDate(p.getActualDate());
        dto.setBank(p.getBank());
        dto.setStatus(p.getStatus());
        dto.setNote(p.getNote());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
