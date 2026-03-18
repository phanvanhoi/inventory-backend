package manage.store.inventory.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.dto.UnitEmployeeCreateDTO;
import manage.store.inventory.dto.UnitEmployeeDTO;
import manage.store.inventory.entity.Position;
import manage.store.inventory.entity.Unit;
import manage.store.inventory.entity.UnitEmployee;
import manage.store.inventory.repository.PositionRepository;
import manage.store.inventory.repository.UnitEmployeeRepository;
import manage.store.inventory.repository.UnitRepository;

@ExtendWith(MockitoExtension.class)
class UnitEmployeeServiceImplTest {

    @Mock private UnitEmployeeRepository employeeRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private PositionRepository positionRepository;

    @InjectMocks
    private UnitEmployeeServiceImpl service;

    private Unit unit;
    private Position position;
    private UnitEmployee employee;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId(1L);
        unit.setUnitName("Bưu điện HN");

        position = new Position();
        position.setPositionId(1L);
        position.setPositionCode("GD");
        position.setPositionName("Giám đốc");

        employee = new UnitEmployee();
        employee.setEmployeeId(1L);
        employee.setUnitId(1L);
        employee.setFullName("Nguyễn Văn A");
        employee.setPositionId(1L);
    }

    // ==================== GET EMPLOYEES ====================

    @Test
    @DisplayName("Lấy danh sách nhân viên theo đơn vị")
    void getEmployeesByUnit_success() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(employeeRepository.findByUnitIdOrderByFullName(1L)).thenReturn(List.of(employee));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));

        List<UnitEmployeeDTO> result = service.getEmployeesByUnit(1L);

        assertEquals(1, result.size());
        assertEquals("Nguyễn Văn A", result.get(0).getFullName());
        assertEquals("GD", result.get(0).getPositionCode());
        assertEquals("Giám đốc", result.get(0).getPositionName());
    }

    @Test
    @DisplayName("Lấy nhân viên - unit không tồn tại")
    void getEmployeesByUnit_unitNotFound_throwsException() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getEmployeesByUnit(99L));
        assertTrue(ex.getMessage().contains("Unit not found"));
    }

    @Test
    @DisplayName("Nhân viên không có position")
    void getEmployeesByUnit_noPosition_nullPositionFields() {
        employee.setPositionId(null);
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(employeeRepository.findByUnitIdOrderByFullName(1L)).thenReturn(List.of(employee));

        List<UnitEmployeeDTO> result = service.getEmployeesByUnit(1L);

        assertNull(result.get(0).getPositionCode());
        assertNull(result.get(0).getPositionName());
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("Tạo nhân viên thành công")
    void createEmployee_success() {
        UnitEmployeeCreateDTO dto = new UnitEmployeeCreateDTO();
        dto.setFullName("Trần Văn B");
        dto.setPositionId(1L);

        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(employeeRepository.save(any(UnitEmployee.class))).thenReturn(employee);
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));

        UnitEmployeeDTO result = service.createEmployee(1L, dto);

        assertNotNull(result);
        verify(employeeRepository).save(any(UnitEmployee.class));
    }

    @Test
    @DisplayName("Tạo nhân viên - tên trống")
    void createEmployee_blankName_throwsException() {
        UnitEmployeeCreateDTO dto = new UnitEmployeeCreateDTO();
        dto.setFullName("   ");

        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createEmployee(1L, dto));
        assertEquals("Họ tên không được để trống", ex.getMessage());
    }

    @Test
    @DisplayName("Tạo nhân viên - tên null")
    void createEmployee_nullName_throwsException() {
        UnitEmployeeCreateDTO dto = new UnitEmployeeCreateDTO();
        dto.setFullName(null);

        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createEmployee(1L, dto));
        assertEquals("Họ tên không được để trống", ex.getMessage());
    }

    @Test
    @DisplayName("Tạo nhân viên - unit không tồn tại")
    void createEmployee_unitNotFound_throwsException() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        UnitEmployeeCreateDTO dto = new UnitEmployeeCreateDTO();
        dto.setFullName("Test");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createEmployee(99L, dto));
        assertTrue(ex.getMessage().contains("Unit not found"));
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("Cập nhật nhân viên thành công")
    void updateEmployee_success() {
        UnitEmployeeCreateDTO dto = new UnitEmployeeCreateDTO();
        dto.setFullName("Nguyễn Văn B");
        dto.setPositionId(1L);

        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(UnitEmployee.class))).thenReturn(employee);
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));

        UnitEmployeeDTO result = service.updateEmployee(1L, 1L, dto);

        assertNotNull(result);
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Cập nhật nhân viên - không thuộc đơn vị")
    void updateEmployee_wrongUnit_throwsException() {
        employee.setUnitId(2L); // Thuộc unit khác

        UnitEmployeeCreateDTO dto = new UnitEmployeeCreateDTO();
        dto.setFullName("Test");

        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateEmployee(1L, 1L, dto));
        assertEquals("Employee không thuộc đơn vị này", ex.getMessage());
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("Xóa nhân viên thành công")
    void deleteEmployee_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        service.deleteEmployee(1L, 1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    @DisplayName("Xóa nhân viên - không thuộc đơn vị")
    void deleteEmployee_wrongUnit_throwsException() {
        employee.setUnitId(2L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.deleteEmployee(1L, 1L));
        assertEquals("Employee không thuộc đơn vị này", ex.getMessage());
    }
}
