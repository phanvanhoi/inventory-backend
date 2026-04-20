package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * Roll-up metrics cho customer parent (A4 decision).
 * GET /api/customers/{id}/rollup?year=
 */
@Data
public class CustomerRollupDTO {
    private Long parentCustomerId;
    private String parentName;
    private Integer year;

    // Aggregated metrics across all children + self
    private List<CustomerDTO> children;
    private Long totalOrders;
    private BigDecimal totalRevenueContract;
    private BigDecimal totalRevenuePaid;
    private BigDecimal totalOutstanding;
    private Long successOrders;
    private Long activeOrders;
    private Long cancelledOrders;
}
