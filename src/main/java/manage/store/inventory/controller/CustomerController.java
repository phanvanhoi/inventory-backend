package manage.store.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.dto.CustomerCreateDTO;
import manage.store.inventory.dto.CustomerDTO;
import manage.store.inventory.dto.CustomerRollupDTO;
import manage.store.inventory.service.CustomerService;
import manage.store.inventory.service.DashboardService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final DashboardService dashboardService;

    public CustomerController(CustomerService customerService, DashboardService dashboardService) {
        this.customerService = customerService;
        this.dashboardService = dashboardService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Long> createCustomer(@Valid @RequestBody CustomerCreateDTO dto) {
        Long id = customerService.createCustomer(dto);
        return ResponseEntity.ok(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALES','ADMIN')")
    public ResponseEntity<Void> updateCustomer(@PathVariable Long id,
                                                @Valid @RequestBody CustomerCreateDTO dto) {
        customerService.updateCustomer(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<CustomerDTO> getAllCustomers(@RequestParam(required = false) Long unitId,
                                              @RequestParam(required = false) Boolean rootOnly) {
        if (unitId != null) return customerService.getCustomersByUnitId(unitId);
        if (Boolean.TRUE.equals(rootOnly)) return customerService.getRootCustomers();
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public CustomerDTO getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @GetMapping("/{id}/children")
    public List<CustomerDTO> getChildren(@PathVariable Long id) {
        return customerService.getChildrenOf(id);
    }

    // G11, W22 — Roll-up parent customer metrics (A4 decision)
    @GetMapping("/{id}/rollup")
    public CustomerRollupDTO getRollup(
            @PathVariable Long id,
            @RequestParam(required = false) Integer year) {
        return dashboardService.getCustomerRollup(id, year);
    }
}
