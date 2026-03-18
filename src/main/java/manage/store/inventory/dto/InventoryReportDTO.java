package manage.store.inventory.dto;

import java.math.BigDecimal;

public class InventoryReportDTO {

    private Long productId;
    private String productName;
    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal netQuantity;
    private Long transactionCount;

    public InventoryReportDTO(Long productId, String productName,
                              BigDecimal totalIn, BigDecimal totalOut,
                              BigDecimal netQuantity, Long transactionCount) {
        this.productId = productId;
        this.productName = productName;
        this.totalIn = totalIn != null ? totalIn : BigDecimal.ZERO;
        this.totalOut = totalOut != null ? totalOut : BigDecimal.ZERO;
        this.netQuantity = netQuantity != null ? netQuantity : BigDecimal.ZERO;
        this.transactionCount = transactionCount != null ? transactionCount : 0L;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getTotalIn() { return totalIn; }
    public BigDecimal getTotalOut() { return totalOut; }
    public BigDecimal getNetQuantity() { return netQuantity; }
    public Long getTransactionCount() { return transactionCount; }
}
