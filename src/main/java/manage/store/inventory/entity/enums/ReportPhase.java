package manage.store.inventory.entity.enums;

public enum ReportPhase {
    SALES_INPUT,
    MEASUREMENT_INPUT,
    PRODUCTION_INPUT,
    STOCKKEEPER_INPUT,
    COMPLETED;

    public ReportPhase next() {
        return switch (this) {
            case SALES_INPUT -> MEASUREMENT_INPUT;
            case MEASUREMENT_INPUT -> PRODUCTION_INPUT;
            case PRODUCTION_INPUT -> STOCKKEEPER_INPUT;
            case STOCKKEEPER_INPUT -> COMPLETED;
            case COMPLETED -> throw new IllegalStateException("Không thể chuyển tiếp từ COMPLETED");
        };
    }

    public ReportPhase previous() {
        return switch (this) {
            case MEASUREMENT_INPUT -> SALES_INPUT;
            case PRODUCTION_INPUT -> MEASUREMENT_INPUT;
            case STOCKKEEPER_INPUT -> PRODUCTION_INPUT;
            case COMPLETED -> STOCKKEEPER_INPUT;
            case SALES_INPUT -> throw new IllegalStateException("Không thể trả lại từ SALES_INPUT");
        };
    }

    public String ownerRole() {
        return switch (this) {
            case SALES_INPUT -> "SALES";
            case MEASUREMENT_INPUT -> "MEASUREMENT";
            case PRODUCTION_INPUT -> "PRODUCTION";
            case STOCKKEEPER_INPUT -> "STOCKKEEPER";
            case COMPLETED -> null;
        };
    }
}
