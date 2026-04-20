package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.enums.OrderStatus;
import manage.store.inventory.entity.enums.ReportPhase;

@Data
public class OrderListDTO {

    private Long orderId;
    private String orderCode;

    private Long customerId;
    private Long unitId;
    private String unitName;

    private OrderStatus status;
    private ReportPhase currentPhase;

    private Long salesPersonUserId;
    private String salesPersonName;

    private String unitType;
    private Integer contractYear;

    private BigDecimal totalBeforeVat;
    private BigDecimal vatAmount;
    private BigDecimal totalAfterVat;

    // SALES dates
    private LocalDate expectedDeliveryDate;
    private LocalDate finalizedListSentDate;
    private LocalDate finalizedListReceivedDate;
    private String deliveryMethod;

    // MEASUREMENT
    private LocalDate measurementStart;
    private LocalDate measurementEnd;
    private String technicianName;
    private LocalDate measurementReceivedDate;
    private String measurementHandler;
    private Boolean skipMeasurement;
    private LocalDate productionHandoverDate;

    // PRODUCTION
    private LocalDate tailorStartDate;
    private LocalDate tailorExpectedReturn;
    private LocalDate tailorActualReturn;
    private LocalDate packingReturnDate;

    // STOCKKEEPER
    private LocalDate actualShippingDate;

    // Flags
    private Boolean skipDesign;
    private Boolean designReady;
    private Boolean skipKcs;
    private Boolean qcPassed;
    private Boolean hasRepair;
    private Boolean cancelled;

    private String note;

    // Migration tracking
    private Long legacyReportId;
    private String seedSource;
    private String larkLegacyId;

    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed field
    private Long daysLate;

    public static OrderListDTO from(Order o) {
        if (o == null) return null;
        OrderListDTO dto = new OrderListDTO();
        dto.setOrderId(o.getOrderId());
        dto.setOrderCode(o.getOrderCode());

        if (o.getCustomer() != null) {
            dto.setCustomerId(o.getCustomer().getCustomerId());
            if (o.getCustomer().getUnit() != null) {
                dto.setUnitId(o.getCustomer().getUnit().getUnitId());
                dto.setUnitName(o.getCustomer().getUnit().getUnitName());
            }
        }

        dto.setStatus(o.getStatus());
        dto.setCurrentPhase(o.getCurrentPhase());

        if (o.getSalesPersonUser() != null) {
            dto.setSalesPersonUserId(o.getSalesPersonUser().getUserId());
        }
        dto.setSalesPersonName(o.getSalesPersonName());

        dto.setUnitType(o.getUnitType());
        dto.setContractYear(o.getContractYear());
        dto.setTotalBeforeVat(o.getTotalBeforeVat());
        dto.setVatAmount(o.getVatAmount());
        dto.setTotalAfterVat(o.getTotalAfterVat());

        dto.setExpectedDeliveryDate(o.getExpectedDeliveryDate());
        dto.setFinalizedListSentDate(o.getFinalizedListSentDate());
        dto.setFinalizedListReceivedDate(o.getFinalizedListReceivedDate());
        dto.setDeliveryMethod(o.getDeliveryMethod());

        dto.setMeasurementStart(o.getMeasurementStart());
        dto.setMeasurementEnd(o.getMeasurementEnd());
        dto.setTechnicianName(o.getTechnicianName());
        dto.setMeasurementReceivedDate(o.getMeasurementReceivedDate());
        dto.setMeasurementHandler(o.getMeasurementHandler());
        dto.setSkipMeasurement(o.getSkipMeasurement());
        dto.setProductionHandoverDate(o.getProductionHandoverDate());

        dto.setTailorStartDate(o.getTailorStartDate());
        dto.setTailorExpectedReturn(o.getTailorExpectedReturn());
        dto.setTailorActualReturn(o.getTailorActualReturn());
        dto.setPackingReturnDate(o.getPackingReturnDate());

        dto.setActualShippingDate(o.getActualShippingDate());

        dto.setSkipDesign(o.getSkipDesign());
        dto.setDesignReady(o.getDesignReady());
        dto.setSkipKcs(o.getSkipKcs());
        dto.setQcPassed(o.getQcPassed());
        dto.setHasRepair(o.getHasRepair());
        dto.setCancelled(o.getCancelled());

        dto.setNote(o.getNote());
        dto.setLegacyReportId(o.getLegacyReportId());
        dto.setSeedSource(o.getSeedSource());
        dto.setLarkLegacyId(o.getLarkLegacyId());

        if (o.getCreatedByUser() != null) {
            dto.setCreatedBy(o.getCreatedByUser().getUserId());
            dto.setCreatedByName(o.getCreatedByUser().getFullName());
        }
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUpdatedAt(o.getUpdatedAt());

        // Compute daysLate
        if (o.getExpectedDeliveryDate() != null && o.getActualShippingDate() == null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    o.getExpectedDeliveryDate(), java.time.LocalDate.now());
            if (days > 0) dto.setDaysLate(days);
        }

        return dto;
    }
}
