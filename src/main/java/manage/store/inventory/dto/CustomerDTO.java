package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.Customer;
import manage.store.inventory.entity.enums.CustomerType;

@Data
public class CustomerDTO {

    private Long customerId;
    private Long unitId;
    private String unitName;
    private Long parentCustomerId;
    private String parentCustomerName;
    private String taxCode;
    private String signerName;
    private CustomerType customerType;
    private String province;
    private Integer contractYear;
    private String note;
    private String seedSource;
    private String larkLegacyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CustomerDTO from(Customer c) {
        if (c == null) return null;
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(c.getCustomerId());
        if (c.getUnit() != null) {
            dto.setUnitId(c.getUnit().getUnitId());
            dto.setUnitName(c.getUnit().getUnitName());
        }
        if (c.getParentCustomer() != null) {
            dto.setParentCustomerId(c.getParentCustomer().getCustomerId());
            if (c.getParentCustomer().getUnit() != null) {
                dto.setParentCustomerName(c.getParentCustomer().getUnit().getUnitName());
            }
        }
        dto.setTaxCode(c.getTaxCode());
        dto.setSignerName(c.getSignerName());
        dto.setCustomerType(c.getCustomerType());
        dto.setProvince(c.getProvince());
        dto.setContractYear(c.getContractYear());
        dto.setNote(c.getNote());
        dto.setSeedSource(c.getSeedSource());
        dto.setLarkLegacyId(c.getLarkLegacyId());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}
