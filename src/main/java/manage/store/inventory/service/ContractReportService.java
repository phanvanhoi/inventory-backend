package manage.store.inventory.service;

import java.util.List;
import java.util.Set;

import manage.store.inventory.dto.ContractReportAlertDTO;
import manage.store.inventory.dto.ContractReportCreateDTO;
import manage.store.inventory.dto.ContractReportDashboardDTO;
import manage.store.inventory.dto.ContractReportHistoryDTO;
import manage.store.inventory.dto.ContractReportListDTO;
import manage.store.inventory.dto.ContractReportUpdateDTO;

public interface ContractReportService {

    Long createReport(ContractReportCreateDTO dto, Long userId);

    List<ContractReportListDTO> getAllReports();

    ContractReportListDTO getReportById(Long id);

    void updateReport(Long id, ContractReportUpdateDTO dto, Long userId, Set<String> userRoles);

    void deleteReport(Long id);

    void advancePhase(Long id, Long userId, Set<String> userRoles);

    void returnPhase(Long id, String reason, Long userId, Set<String> userRoles);

    List<ContractReportHistoryDTO> getHistory(Long id);

    List<ContractReportAlertDTO> getAlerts();

    ContractReportDashboardDTO getDashboard();
}
