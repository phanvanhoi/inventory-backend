package manage.store.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface RequestSetListDTO {

    Long getSetId();
    String getSetName();
    String getDescription();
    String getStatus();
    Long getCreatedBy();
    String getCreatedByName();
    LocalDateTime getCreatedAt();
    LocalDateTime getSubmittedAt();
    Integer getRequestCount();
    String getRequestTypes(); // Comma-separated list: "ADJUST_IN,IN,OUT"
    String getProductNames(); // Comma-separated list of product names
    String getPositionCodes(); // Comma-separated list: "GDV,VHX"
    LocalDate getEarliestExpectedDate(); // Ngày dự kiến sớm nhất trong các request
}
