package manage.store.inventory.dto;

import java.math.BigDecimal;

import lombok.Data;
import manage.store.inventory.entity.OrderItem;

@Data
public class OrderItemDTO {

    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer qtyContract;
    private Integer qtySettlement;
    private BigDecimal unitPrice;
    private BigDecimal amountContract;
    private BigDecimal amountSettlement;
    private String note;
    private String seedSource;
    private String larkLegacyId;

    public static OrderItemDTO from(OrderItem oi) {
        if (oi == null) return null;
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(oi.getOrderItemId());
        if (oi.getOrder() != null) dto.setOrderId(oi.getOrder().getOrderId());
        if (oi.getProduct() != null) dto.setProductId(oi.getProduct().getProductId());
        dto.setProductName(oi.getProductName());
        dto.setQtyContract(oi.getQtyContract());
        dto.setQtySettlement(oi.getQtySettlement());
        dto.setUnitPrice(oi.getUnitPrice());
        dto.setAmountContract(oi.getAmountContract());
        dto.setAmountSettlement(oi.getAmountSettlement());
        dto.setNote(oi.getNote());
        dto.setSeedSource(oi.getSeedSource());
        dto.setLarkLegacyId(oi.getLarkLegacyId());
        return dto;
    }
}
