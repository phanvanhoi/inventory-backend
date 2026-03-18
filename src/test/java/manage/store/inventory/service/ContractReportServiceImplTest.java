package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.dto.ContractReportCreateDTO;
import manage.store.inventory.dto.ContractReportUpdateDTO;
import manage.store.inventory.entity.ContractReport;
import manage.store.inventory.entity.ContractReportHistory;
import manage.store.inventory.entity.Unit;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.ReportPhase;
import manage.store.inventory.repository.ContractReportHistoryRepository;
import manage.store.inventory.repository.ContractReportRepository;
import manage.store.inventory.repository.UnitRepository;
import manage.store.inventory.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ContractReportServiceImplTest {

    @Mock private ContractReportRepository reportRepository;
    @Mock private ContractReportHistoryRepository historyRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ContractReportServiceImpl service;

    private Unit unit;
    private User salesUser;
    private User measurementUser;
    private User adminUser;
    private ContractReport report;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId(1L);
        unit.setUnitName("Bưu điện HN");

        salesUser = new User();
        salesUser.setUserId(1L);
        salesUser.setFullName("Sales User");

        measurementUser = new User();
        measurementUser.setUserId(2L);
        measurementUser.setFullName("Measurement User");

        adminUser = new User();
        adminUser.setUserId(3L);
        adminUser.setFullName("Admin User");

        report = new ContractReport();
        report.setReportId(1L);
        report.setCurrentPhase(ReportPhase.SALES_INPUT);
        report.setUnit(unit);
        report.setCreatedByUser(salesUser);
        report.setSalesPerson("Nguyễn Văn A");
    }

    // ==================== CREATE ====================

    @Nested
    @DisplayName("Create Report")
    class CreateTests {

        @Test
        @DisplayName("Tạo báo cáo thành công - phase mặc định SALES_INPUT")
        void createReport_success() {
            ContractReportCreateDTO dto = new ContractReportCreateDTO();
            dto.setUnitId(1L);
            dto.setSalesPerson("Nguyễn Văn A");

            when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
            when(userRepository.findById(1L)).thenReturn(Optional.of(salesUser));

            service.createReport(dto, 1L);

            verify(reportRepository).save(any(ContractReport.class));
        }

        @Test
        @DisplayName("Tạo báo cáo - unit không tồn tại")
        void createReport_unitNotFound_throwsException() {
            ContractReportCreateDTO dto = new ContractReportCreateDTO();
            dto.setUnitId(99L);

            when(unitRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.createReport(dto, 1L));
            assertEquals("Đơn vị không tồn tại", ex.getMessage());
        }

        @Test
        @DisplayName("Tạo báo cáo - user không tồn tại")
        void createReport_userNotFound_throwsException() {
            ContractReportCreateDTO dto = new ContractReportCreateDTO();
            dto.setUnitId(1L);

            when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.createReport(dto, 99L));
            assertEquals("User không tồn tại", ex.getMessage());
        }
    }

    // ==================== GET ====================

    @Test
    @DisplayName("Lấy báo cáo theo ID - không tìm thấy")
    void getReportById_notFound_throwsException() {
        when(reportRepository.findByIdWithRelations(99L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getReportById(99L));
        assertEquals("Báo cáo không tồn tại", ex.getMessage());
    }

    // ==================== UPDATE ====================

    @Nested
    @DisplayName("Update Report")
    class UpdateTests {

        @Test
        @DisplayName("ADMIN sửa tất cả fields ở mọi phase")
        void updateReport_adminCanEditAtAnyPhase() {
            report.setCurrentPhase(ReportPhase.PRODUCTION_INPUT);
            ContractReportUpdateDTO dto = new ContractReportUpdateDTO();
            dto.setUnitId(1L);

            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(3L)).thenReturn(Optional.of(adminUser));

            // ADMIN can edit even at PRODUCTION_INPUT
            assertDoesNotThrow(() ->
                    service.updateReport(1L, dto, 3L, Set.of("ADMIN")));
            verify(reportRepository).save(report);
        }

        @Test
        @DisplayName("SALES sửa ở phase SALES_INPUT")
        void updateReport_salesAtSalesPhase_success() {
            ContractReportUpdateDTO dto = new ContractReportUpdateDTO();
            dto.setUnitId(1L);
            dto.setSalesPerson("Trần Văn B");

            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(1L)).thenReturn(Optional.of(salesUser));

            assertDoesNotThrow(() ->
                    service.updateReport(1L, dto, 1L, Set.of("SALES")));
        }

        @Test
        @DisplayName("SALES sửa ở phase MEASUREMENT_INPUT - thất bại")
        void updateReport_salesAtWrongPhase_throwsException() {
            report.setCurrentPhase(ReportPhase.MEASUREMENT_INPUT);
            ContractReportUpdateDTO dto = new ContractReportUpdateDTO();

            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(1L)).thenReturn(Optional.of(salesUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.updateReport(1L, dto, 1L, Set.of("SALES")));
            assertTrue(ex.getMessage().contains("không có quyền sửa"));
        }

        @Test
        @DisplayName("MEASUREMENT sửa ở phase MEASUREMENT_INPUT")
        void updateReport_measurementAtCorrectPhase_success() {
            report.setCurrentPhase(ReportPhase.MEASUREMENT_INPUT);
            ContractReportUpdateDTO dto = new ContractReportUpdateDTO();

            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(2L)).thenReturn(Optional.of(measurementUser));

            assertDoesNotThrow(() ->
                    service.updateReport(1L, dto, 2L, Set.of("MEASUREMENT")));
        }

        @Test
        @DisplayName("Report không tồn tại")
        void updateReport_notFound_throwsException() {
            when(reportRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.updateReport(99L, new ContractReportUpdateDTO(), 1L, Set.of("ADMIN")));
            assertEquals("Báo cáo không tồn tại", ex.getMessage());
        }
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("Xóa báo cáo thành công")
    void deleteReport_success() {
        when(reportRepository.existsById(1L)).thenReturn(true);

        service.deleteReport(1L);

        verify(reportRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Xóa báo cáo - không tồn tại")
    void deleteReport_notFound_throwsException() {
        when(reportRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.deleteReport(99L));
        assertEquals("Báo cáo không tồn tại", ex.getMessage());
    }

    // ==================== ADVANCE PHASE ====================

    @Nested
    @DisplayName("Advance Phase")
    class AdvancePhaseTests {

        @Test
        @DisplayName("Chuyển SALES_INPUT -> MEASUREMENT_INPUT thành công")
        void advancePhase_success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(1L)).thenReturn(Optional.of(salesUser));

            service.advancePhase(1L, 1L, Set.of("SALES"));

            assertEquals(ReportPhase.MEASUREMENT_INPUT, report.getCurrentPhase());
            verify(reportRepository).save(report);
            verify(historyRepository).save(any(ContractReportHistory.class));
        }

        @Test
        @DisplayName("Chuyển từ COMPLETED - thất bại")
        void advancePhase_fromCompleted_throwsException() {
            report.setCurrentPhase(ReportPhase.COMPLETED);
            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(1L)).thenReturn(Optional.of(salesUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.advancePhase(1L, 1L, Set.of("SALES")));
            assertEquals("Báo cáo đã hoàn tất", ex.getMessage());
        }

        @Test
        @DisplayName("Role sai không được chuyển phase")
        void advancePhase_wrongRole_throwsException() {
            // SALES_INPUT requires SALES role, not MEASUREMENT
            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(2L)).thenReturn(Optional.of(measurementUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.advancePhase(1L, 2L, Set.of("MEASUREMENT")));
            assertTrue(ex.getMessage().contains("Chỉ SALES mới được chuyển"));
        }
    }

    // ==================== RETURN PHASE ====================

    @Nested
    @DisplayName("Return Phase")
    class ReturnPhaseTests {

        @Test
        @DisplayName("Trả MEASUREMENT_INPUT -> SALES_INPUT thành công")
        void returnPhase_success() {
            report.setCurrentPhase(ReportPhase.MEASUREMENT_INPUT);
            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(2L)).thenReturn(Optional.of(measurementUser));

            service.returnPhase(1L, "Thiếu thông tin", 2L, Set.of("MEASUREMENT"));

            assertEquals(ReportPhase.SALES_INPUT, report.getCurrentPhase());
            verify(historyRepository).save(any(ContractReportHistory.class));
        }

        @Test
        @DisplayName("Trả từ SALES_INPUT - thất bại")
        void returnPhase_fromSalesInput_throwsException() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(1L)).thenReturn(Optional.of(salesUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.returnPhase(1L, "Reason", 1L, Set.of("SALES")));
            assertEquals("Không thể trả lại từ giai đoạn đầu tiên", ex.getMessage());
        }

        @Test
        @DisplayName("Trả từ COMPLETED - thất bại")
        void returnPhase_fromCompleted_throwsException() {
            report.setCurrentPhase(ReportPhase.COMPLETED);
            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(1L)).thenReturn(Optional.of(salesUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.returnPhase(1L, "Reason", 1L, Set.of("SALES")));
            assertEquals("Báo cáo đã hoàn tất, không thể trả lại", ex.getMessage());
        }

        @Test
        @DisplayName("Role sai không được trả phase")
        void returnPhase_wrongRole_throwsException() {
            report.setCurrentPhase(ReportPhase.MEASUREMENT_INPUT);
            when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(userRepository.findById(1L)).thenReturn(Optional.of(salesUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.returnPhase(1L, "Reason", 1L, Set.of("SALES")));
            assertTrue(ex.getMessage().contains("Chỉ MEASUREMENT mới được trả lại"));
        }
    }

    // ==================== GET HISTORY ====================

    @Test
    @DisplayName("Lấy history - report không tồn tại")
    void getHistory_reportNotFound_throwsException() {
        when(reportRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getHistory(99L));
        assertEquals("Báo cáo không tồn tại", ex.getMessage());
    }

    @Test
    @DisplayName("Lấy history thành công")
    void getHistory_success() {
        ContractReportHistory h = new ContractReportHistory();
        h.setHistoryId(1L);
        h.setAction("ADVANCE");
        h.setFieldName("currentPhase");
        h.setOldValue("SALES_INPUT");
        h.setNewValue("MEASUREMENT_INPUT");
        h.setChangedByUser(salesUser);
        h.setChangedAt(LocalDateTime.now());

        when(reportRepository.existsById(1L)).thenReturn(true);
        when(historyRepository.findByReportIdWithUser(1L)).thenReturn(List.of(h));

        var result = service.getHistory(1L);

        assertEquals(1, result.size());
        assertEquals("ADVANCE", result.get(0).getAction());
    }

    // ==================== GET ALERTS ====================

    @Test
    @DisplayName("Lấy alerts kết hợp late + upcoming + tailor")
    void getAlerts_combinesAlerts() {
        ContractReport lateReport = new ContractReport();
        lateReport.setReportId(1L);
        lateReport.setUnit(unit);
        lateReport.setSalesPerson("A");
        lateReport.setExpectedDeliveryDate(LocalDate.now().minusDays(5));

        when(reportRepository.findLateDeliveries(any())).thenReturn(List.of(lateReport));
        when(reportRepository.findUpcomingDeliveries(any(), any())).thenReturn(List.of());
        when(reportRepository.findLateTailorReturns(any())).thenReturn(List.of());

        var alerts = service.getAlerts();

        assertEquals(1, alerts.size());
        assertEquals("LATE_DELIVERY", alerts.get(0).getAlertType());
    }
}
