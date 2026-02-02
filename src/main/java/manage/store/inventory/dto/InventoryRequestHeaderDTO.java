package manage.store.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface InventoryRequestHeaderDTO {

    Long getRequestId();
    String getUnitName();
    String getPositionCode(); // Chức danh: GDV, VHX, ...
    String getProductName();
    String getRequestType();
    LocalDate getExpectedDate();
    String getNote();
    LocalDateTime getCreatedAt();
}
