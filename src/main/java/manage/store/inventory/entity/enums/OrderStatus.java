package manage.store.inventory.entity.enums;

public enum OrderStatus {
    NEW,
    NEGOTIATION,
    CONTRACT_SIGNED,
    DESIGNING,
    MEASURING,
    PRODUCING,
    QC,
    PACKING,
    DELIVERED,
    SUCCESS,
    LIQUIDATED,
    CANCELLED;

    public boolean isTerminal() {
        return this == SUCCESS || this == LIQUIDATED || this == CANCELLED;
    }
}
