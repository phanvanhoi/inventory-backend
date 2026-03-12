package manage.store.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.dto.UnitEmployeeCreateDTO;
import manage.store.inventory.dto.UnitEmployeeDTO;
import manage.store.inventory.service.UnitEmployeeService;

@RestController
@RequestMapping("/api/units/{unitId}/employees")
public class UnitEmployeeController {

    private final UnitEmployeeService employeeService;

    public UnitEmployeeController(UnitEmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<UnitEmployeeDTO> getEmployees(@PathVariable Long unitId) {
        return employeeService.getEmployeesByUnit(unitId);
    }

    @PostMapping
    public UnitEmployeeDTO createEmployee(
            @PathVariable Long unitId,
            @RequestBody UnitEmployeeCreateDTO dto
    ) {
        return employeeService.createEmployee(unitId, dto);
    }

    @PutMapping("/{id}")
    public UnitEmployeeDTO updateEmployee(
            @PathVariable Long unitId,
            @PathVariable Long id,
            @RequestBody UnitEmployeeCreateDTO dto
    ) {
        return employeeService.updateEmployee(unitId, id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long unitId,
            @PathVariable Long id
    ) {
        employeeService.deleteEmployee(unitId, id);
        return ResponseEntity.noContent().build();
    }
}
