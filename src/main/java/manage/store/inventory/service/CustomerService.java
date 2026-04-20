package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.CustomerCreateDTO;
import manage.store.inventory.dto.CustomerDTO;

public interface CustomerService {

    Long createCustomer(CustomerCreateDTO dto);

    void updateCustomer(Long customerId, CustomerCreateDTO dto);

    void deleteCustomer(Long customerId);

    List<CustomerDTO> getAllCustomers();

    CustomerDTO getCustomerById(Long customerId);

    List<CustomerDTO> getCustomersByUnitId(Long unitId);

    List<CustomerDTO> getRootCustomers();

    List<CustomerDTO> getChildrenOf(Long parentCustomerId);
}
