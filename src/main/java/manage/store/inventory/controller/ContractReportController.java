package manage.store.inventory.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.dto.ContractReportAlertDTO;
import manage.store.inventory.dto.ContractReportCreateDTO;
import manage.store.inventory.dto.ContractReportDashboardDTO;
import manage.store.inventory.dto.ContractReportHistoryDTO;
import manage.store.inventory.dto.ContractReportListDTO;
import manage.store.inventory.dto.ContractReportUpdateDTO;
import manage.store.inventory.dto.ReportReturnReasonDTO;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.ContractReportService;

@RestController
@RequestMapping("/api/reports")
public class ContractReportController {

    private final ContractReportService reportService;
    private final CurrentUser currentUser;

    public ContractReportController(ContractReportService reportService, CurrentUser currentUser) {
        this.reportService = reportService;
        this.currentUser = currentUser;
    }

    // Tạo báo cáo (chỉ SALES)
    @PostMapping
    @PreAuthorize("hasRole('SALES')")
    public ResponseEntity<Long> createReport(@Valid @RequestBody ContractReportCreateDTO dto) {
        Long reportId = reportService.createReport(dto, currentUser.getUserId());
        return ResponseEntity.ok(reportId);
    }

    // Danh sách báo cáo
    @GetMapping
    public List<ContractReportListDTO> getAllReports() {
        return reportService.getAllReports();
    }

    // Chi tiết báo cáo
    @GetMapping("/{id}")
    public ContractReportListDTO getReportById(@PathVariable Long id) {
        return reportService.getReportById(id);
    }

    // Cập nhật báo cáo (validate role + phase trong service)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALES', 'MEASUREMENT', 'PRODUCTION', 'STOCKKEEPER', 'ADMIN')")
    public ResponseEntity<Void> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ContractReportUpdateDTO dto) {
        reportService.updateReport(id, dto, currentUser.getUserId(), getUserRoles());
        return ResponseEntity.ok().build();
    }

    // Xóa báo cáo (chỉ ADMIN hoặc SALES)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALES', 'ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    // Chuyển sang giai đoạn tiếp theo (role sở hữu phase hiện tại)
    @PostMapping("/{id}/advance")
    @PreAuthorize("hasAnyRole('SALES', 'MEASUREMENT', 'PRODUCTION', 'STOCKKEEPER')")
    public ResponseEntity<Void> advancePhase(@PathVariable Long id) {
        reportService.advancePhase(id, currentUser.getUserId(), getUserRoles());
        return ResponseEntity.ok().build();
    }

    // Trả lại giai đoạn trước (role sở hữu phase hiện tại, cần lý do)
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('MEASUREMENT', 'PRODUCTION', 'STOCKKEEPER')")
    public ResponseEntity<Void> returnPhase(
            @PathVariable Long id,
            @Valid @RequestBody ReportReturnReasonDTO dto) {
        reportService.returnPhase(id, dto.getReason(), currentUser.getUserId(), getUserRoles());
        return ResponseEntity.ok().build();
    }

    // Lịch sử chỉnh sửa
    @GetMapping("/{id}/history")
    public List<ContractReportHistoryDTO> getHistory(@PathVariable Long id) {
        return reportService.getHistory(id);
    }

    // Cảnh báo trễ hạn
    @GetMapping("/alerts")
    public List<ContractReportAlertDTO> getAlerts() {
        return reportService.getAlerts();
    }

    // Dashboard tổng hợp
    @GetMapping("/dashboard")
    public ContractReportDashboardDTO getDashboard() {
        return reportService.getDashboard();
    }

    private Set<String> getUserRoles() {
        return new HashSet<>(currentUser.get().getRoles());
    }
}
