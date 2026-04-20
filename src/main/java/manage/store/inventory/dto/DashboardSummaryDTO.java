package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Data;
import manage.store.inventory.entity.enums.OrderStatus;

/**
 * Dashboard KPIs tiles (G11, W22).
 * Scope filtered by role (ADMIN=all, SALES=own, phase-roles=own phase).
 */
@Data
public class DashboardSummaryDTO {

    private Long totalOrders;
    private Map<OrderStatus, Long> ordersByStatus;
    private BigDecimal totalRevenueContract;       // SUM(total_after_vat)
    private BigDecimal totalRevenuePaid;            // SUM(payments.amount WHERE status=CONFIRMED)
    private BigDecimal totalOutstanding;            // contract - paid

    private Long lateDeliveryCount;                 // late packing batches
    private Long upcomingDeliveryCount;             // within 7 days
    private Long overdueTailorCount;                // tailor_assignments overdue
    private Long pendingQcCount;                    // quality_checks status PENDING/IN_PROGRESS
    private Long unresolvedMissingCount;            // missing_items.resolved=false
    private Long activeRepairCount;                 // repair_requests not SHIPPED_BACK
    private Long expiringGuaranteeCount;            // guarantees expiring within 30 days
    private Long expiredGuaranteeCount;             // guarantees past expiry
    private Long overduePaymentCount;               // payments PENDING past scheduled

    private Integer year;                           // filter applied
    private String scope;                           // "ADMIN" | "SALES" | role name
}
