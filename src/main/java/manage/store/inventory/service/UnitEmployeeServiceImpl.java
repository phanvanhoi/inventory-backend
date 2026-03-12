package manage.store.inventory.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.UnitEmployeeCreateDTO;
import manage.store.inventory.dto.UnitEmployeeDTO;
import manage.store.inventory.entity.Position;
import manage.store.inventory.entity.Unit;
import manage.store.inventory.entity.UnitEmployee;
import manage.store.inventory.repository.PositionRepository;
import manage.store.inventory.repository.UnitEmployeeRepository;
import manage.store.inventory.repository.UnitRepository;

@Service
@Transactional
public class UnitEmployeeServiceImpl implements UnitEmployeeService {

    private final UnitEmployeeRepository employeeRepository;
    private final UnitRepository unitRepository;
    private final PositionRepository positionRepository;

    public UnitEmployeeServiceImpl(
            UnitEmployeeRepository employeeRepository,
            UnitRepository unitRepository,
            PositionRepository positionRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.unitRepository = unitRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitEmployeeDTO> getEmployeesByUnit(Long unitId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + unitId));

        List<UnitEmployee> employees = employeeRepository.findByUnitIdOrderByFullName(unitId);

        return employees.stream()
                .map(emp -> toDTO(emp, unit))
                .collect(Collectors.toList());
    }

    @Override
    public UnitEmployeeDTO createEmployee(Long unitId, UnitEmployeeCreateDTO dto) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + unitId));

        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }

        UnitEmployee employee = new UnitEmployee();
        employee.setUnitId(unitId);
        employee.setFullName(dto.getFullName().trim());
        employee.setPositionId(dto.getPositionId());

        employee = employeeRepository.save(employee);
        return toDTO(employee, unit);
    }

    @Override
    public UnitEmployeeDTO updateEmployee(Long unitId, Long employeeId, UnitEmployeeCreateDTO dto) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + unitId));

        UnitEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        if (!employee.getUnitId().equals(unitId)) {
            throw new IllegalArgumentException("Employee không thuộc đơn vị này");
        }

        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }

        employee.setFullName(dto.getFullName().trim());
        employee.setPositionId(dto.getPositionId());

        employee = employeeRepository.save(employee);
        return toDTO(employee, unit);
    }

    @Override
    public void deleteEmployee(Long unitId, Long employeeId) {
        UnitEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        if (!employee.getUnitId().equals(unitId)) {
            throw new IllegalArgumentException("Employee không thuộc đơn vị này");
        }

        employeeRepository.delete(employee);
    }

    private UnitEmployeeDTO toDTO(UnitEmployee employee, Unit unit) {
        String positionCode = null;
        String positionName = null;

        if (employee.getPositionId() != null) {
            Position position = positionRepository.findById(employee.getPositionId()).orElse(null);
            if (position != null) {
                positionCode = position.getPositionCode();
                positionName = position.getPositionName();
            }
        }

        return new UnitEmployeeDTO(
                employee.getEmployeeId(),
                unit.getUnitId(),
                unit.getUnitName(),
                employee.getFullName(),
                employee.getPositionId(),
                positionCode,
                positionName
        );
    }
}
