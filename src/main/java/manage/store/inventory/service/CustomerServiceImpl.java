package manage.store.inventory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.CustomerCreateDTO;
import manage.store.inventory.dto.CustomerDTO;
import manage.store.inventory.entity.Customer;
import manage.store.inventory.entity.Unit;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.CustomerRepository;
import manage.store.inventory.repository.UnitRepository;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UnitRepository unitRepository;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            UnitRepository unitRepository) {
        this.customerRepository = customerRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    public Long createCustomer(CustomerCreateDTO dto) {
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vị không tồn tại"));

        Customer customer = new Customer();
        customer.setUnit(unit);
        applyFields(dto, customer);
        customer.setCreatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        return customer.getCustomerId();
    }

    @Override
    public void updateCustomer(Long customerId, CustomerCreateDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));

        if (dto.getUnitId() != null && !dto.getUnitId().equals(customer.getUnit().getUnitId())) {
            Unit unit = unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Đơn vị không tồn tại"));
            customer.setUnit(unit);
        }
        applyFields(dto, customer);
        customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAllWithRelations().stream()
                .map(CustomerDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerId) {
        return customerRepository.findByIdWithRelations(customerId)
                .map(CustomerDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersByUnitId(Long unitId) {
        return customerRepository.findByUnitUnitId(unitId).stream()
                .map(CustomerDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getRootCustomers() {
        return customerRepository.findAllRootCustomers().stream()
                .map(CustomerDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getChildrenOf(Long parentCustomerId) {
        return customerRepository.findByParentCustomerCustomerId(parentCustomerId).stream()
                .map(CustomerDTO::from)
                .collect(Collectors.toList());
    }

    private void applyFields(CustomerCreateDTO dto, Customer customer) {
        if (dto.getParentCustomerId() != null) {
            if (customer.getCustomerId() != null &&
                    dto.getParentCustomerId().equals(customer.getCustomerId())) {
                throw new BusinessException("Khách hàng không thể là cha của chính mình");
            }
            Customer parent = customerRepository.findById(dto.getParentCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khách hàng cha không tồn tại"));
            customer.setParentCustomer(parent);
        } else {
            customer.setParentCustomer(null);
        }
        customer.setTaxCode(dto.getTaxCode());
        customer.setSignerName(dto.getSignerName());
        if (dto.getCustomerType() != null) customer.setCustomerType(dto.getCustomerType());
        customer.setProvince(dto.getProvince());
        customer.setContractYear(dto.getContractYear());
        customer.setNote(dto.getNote());
    }
}
