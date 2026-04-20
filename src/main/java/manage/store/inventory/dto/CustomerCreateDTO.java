package manage.store.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import manage.store.inventory.entity.enums.CustomerType;

@Data
public class CustomerCreateDTO {

    @NotNull(message = "Đơn vị không được để trống")
    private Long unitId;

    private Long parentCustomerId;
    private String taxCode;
    private String signerName;
    private CustomerType customerType = CustomerType.NEW;
    private String province;
    private Integer contractYear;
    private String note;
}
