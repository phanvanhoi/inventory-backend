package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.UnitEmployeeCreateDTO;
import manage.store.inventory.dto.UnitEmployeeDTO;

public interface UnitEmployeeService {

    List<UnitEmployeeDTO> getEmployeesByUnit(Long unitId);

    UnitEmployeeDTO createEmployee(Long unitId, UnitEmployeeCreateDTO dto);

    UnitEmployeeDTO updateEmployee(Long unitId, Long employeeId, UnitEmployeeCreateDTO dto);

    void deleteEmployee(Long unitId, Long employeeId);
}
