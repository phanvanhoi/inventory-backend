package manage.store.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.ContractReportAlertDTO;
import manage.store.inventory.dto.ContractReportCreateDTO;
import manage.store.inventory.dto.ContractReportDashboardDTO;
import manage.store.inventory.dto.ContractReportHistoryDTO;
import manage.store.inventory.dto.ContractReportListDTO;
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

@Service
@Transactional
public class ContractReportServiceImpl implements ContractReportService {

    private final ContractReportRepository reportRepository;
    private final ContractReportHistoryRepository historyRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;

    public ContractReportServiceImpl(
            ContractReportRepository reportRepository,
            ContractReportHistoryRepository historyRepository,
            UnitRepository unitRepository,
            UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.historyRepository = historyRepository;
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Long createReport(ContractReportCreateDTO dto, Long userId) {
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new RuntimeException("Đơn vị không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        ContractReport report = new ContractReport();
        report.setCurrentPhase(ReportPhase.SALES_INPUT);
        report.setUnit(unit);
        report.setCreatedByUser(user);
        report.setCreatedAt(LocalDateTime.now());
        applySalesFields(dto, report);

        reportRepository.save(report);
        return report.getReportId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractReportListDTO> getAllReports() {
        return reportRepository.findAllWithRelations().stream()
                .map(this::toListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContractReportListDTO getReportById(Long id) {
        ContractReport report = reportRepository.findByIdWithRelations(id);
        if (report == null) {
            throw new RuntimeException("Báo cáo không tồn tại");
        }
        return toListDTO(report);
    }

    @Override
    public void updateReport(Long id, ContractReportUpdateDTO dto, Long userId, Set<String> userRoles) {
        ContractReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Báo cáo không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        boolean isAdmin = userRoles.contains("ADMIN");
        ReportPhase phase = report.getCurrentPhase();

        if (isAdmin) {
            // ADMIN can edit all fields at any time
            updateAllFields(report, dto, user);
        } else if (userRoles.contains("SALES") && phase == ReportPhase.SALES_INPUT) {
            updateSalesFields(report, dto, user);
        } else if (userRoles.contains("MEASUREMENT") && phase == ReportPhase.MEASUREMENT_INPUT) {
            updateMeasurementFields(report, dto, user);
        } else if (userRoles.contains("PRODUCTION") && phase == ReportPhase.PRODUCTION_INPUT) {
            updateProductionFields(report, dto, user);
        } else if (userRoles.contains("STOCKKEEPER") && phase == ReportPhase.STOCKKEEPER_INPUT) {
            updateStockkeeperFields(report, dto, user);
        } else {
            throw new RuntimeException("Bạn không có quyền sửa báo cáo ở giai đoạn này");
        }

        reportRepository.save(report);
    }

    @Override
    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new RuntimeException("Báo cáo không tồn tại");
        }
        reportRepository.deleteById(id);
    }

    @Override
    public void advancePhase(Long id, Long userId, Set<String> userRoles) {
        ContractReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Báo cáo không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        ReportPhase currentPhase = report.getCurrentPhase();
        if (currentPhase == ReportPhase.COMPLETED) {
            throw new RuntimeException("Báo cáo đã hoàn tất");
        }

        String ownerRole = currentPhase.ownerRole();
        if (!userRoles.contains(ownerRole)) {
            throw new RuntimeException("Chỉ " + ownerRole + " mới được chuyển giai đoạn này");
        }

        ReportPhase nextPhase = currentPhase.next();
        report.setCurrentPhase(nextPhase);
        reportRepository.save(report);

        logHistory(report.getReportId(), user, "ADVANCE",
                "currentPhase", currentPhase.name(), nextPhase.name(), null);
    }

    @Override
    public void returnPhase(Long id, String reason, Long userId, Set<String> userRoles) {
        ContractReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Báo cáo không tồn tại"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        ReportPhase currentPhase = report.getCurrentPhase();
        if (currentPhase == ReportPhase.SALES_INPUT) {
            throw new RuntimeException("Không thể trả lại từ giai đoạn đầu tiên");
        }
        if (currentPhase == ReportPhase.COMPLETED) {
            throw new RuntimeException("Báo cáo đã hoàn tất, không thể trả lại");
        }

        String ownerRole = currentPhase.ownerRole();
        if (!userRoles.contains(ownerRole)) {
            throw new RuntimeException("Chỉ " + ownerRole + " mới được trả lại giai đoạn này");
        }

        ReportPhase prevPhase = currentPhase.previous();
        report.setCurrentPhase(prevPhase);
        reportRepository.save(report);

        logHistory(report.getReportId(), user, "RETURN",
                "currentPhase", currentPhase.name(), prevPhase.name(), reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractReportHistoryDTO> getHistory(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new RuntimeException("Báo cáo không tồn tại");
        }
        return historyRepository.findByReportIdWithUser(id).stream()
                .map(h -> new ContractReportHistoryDTO(
                        h.getHistoryId(),
                        h.getAction(),
                        h.getFieldName(),
                        h.getOldValue(),
                        h.getNewValue(),
                        h.getReason(),
                        h.getChangedByUser() != null ? h.getChangedByUser().getFullName() : null,
                        h.getChangedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractReportAlertDTO> getAlerts() {
        LocalDate today = LocalDate.now();
        List<ContractReportAlertDTO> alerts = new ArrayList<>();

        reportRepository.findLateDeliveries(today).forEach(cr -> {
            long days = ChronoUnit.DAYS.between(cr.getExpectedDeliveryDate(), today);
            alerts.add(new ContractReportAlertDTO(
                    cr.getReportId(), cr.getUnit().getUnitName(), cr.getSalesPerson(),
                    "LATE_DELIVERY", cr.getExpectedDeliveryDate(), (int) days));
        });

        reportRepository.findUpcomingDeliveries(today, today.plusDays(14)).forEach(cr -> {
            long days = ChronoUnit.DAYS.between(today, cr.getExpectedDeliveryDate());
            alerts.add(new ContractReportAlertDTO(
                    cr.getReportId(), cr.getUnit().getUnitName(), cr.getSalesPerson(),
                    "UPCOMING_DELIVERY", cr.getExpectedDeliveryDate(), (int) days));
        });

        reportRepository.findLateTailorReturns(today).forEach(cr -> {
            long days = ChronoUnit.DAYS.between(cr.getTailorExpectedReturn(), today);
            alerts.add(new ContractReportAlertDTO(
                    cr.getReportId(), cr.getUnit().getUnitName(), cr.getSalesPerson(),
                    "LATE_TAILOR_RETURN", cr.getTailorExpectedReturn(), (int) days));
        });

        return alerts;
    }

    @Override
    @Transactional(readOnly = true)
    public ContractReportDashboardDTO getDashboard() {
        LocalDate today = LocalDate.now();

        Object[] counts = reportRepository.getDashboardCounts();
        Object[] row = (Object[]) counts[0];

        long total = toLong(row[0]);
        long completed = toLong(row[1]);
        long salesInput = toLong(row[2]);
        long measurementInput = toLong(row[3]);
        long productionInput = toLong(row[4]);
        long stockkeeperInput = toLong(row[5]);

        List<ContractReportAlertDTO> lateDeliveries = reportRepository.findLateDeliveries(today).stream()
                .map(cr -> new ContractReportAlertDTO(
                        cr.getReportId(), cr.getUnit().getUnitName(), cr.getSalesPerson(),
                        "LATE_DELIVERY", cr.getExpectedDeliveryDate(),
                        (int) ChronoUnit.DAYS.between(cr.getExpectedDeliveryDate(), today)))
                .collect(Collectors.toList());

        List<ContractReportAlertDTO> upcomingDeliveries = reportRepository.findUpcomingDeliveries(today, today.plusDays(14)).stream()
                .map(cr -> new ContractReportAlertDTO(
                        cr.getReportId(), cr.getUnit().getUnitName(), cr.getSalesPerson(),
                        "UPCOMING_DELIVERY", cr.getExpectedDeliveryDate(),
                        (int) ChronoUnit.DAYS.between(today, cr.getExpectedDeliveryDate())))
                .collect(Collectors.toList());

        List<ContractReportAlertDTO> lateTailorReturns = reportRepository.findLateTailorReturns(today).stream()
                .map(cr -> new ContractReportAlertDTO(
                        cr.getReportId(), cr.getUnit().getUnitName(), cr.getSalesPerson(),
                        "LATE_TAILOR_RETURN", cr.getTailorExpectedReturn(),
                        (int) ChronoUnit.DAYS.between(cr.getTailorExpectedReturn(), today)))
                .collect(Collectors.toList());

        return new ContractReportDashboardDTO(
                total, completed, salesInput, measurementInput, productionInput, stockkeeperInput,
                lateDeliveries, upcomingDeliveries, lateTailorReturns);
    }

    // ========== Field update by role ==========

    private void updateSalesFields(ContractReport report, ContractReportUpdateDTO dto, User user) {
        logFieldChange(report, user, "unitId",
                String.valueOf(report.getUnit().getUnitId()), String.valueOf(dto.getUnitId()));
        if (!report.getUnit().getUnitId().equals(dto.getUnitId())) {
            Unit unit = unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Đơn vị không tồn tại"));
            report.setUnit(unit);
        }
        logAndSet(report, user, "salesPerson", report.getSalesPerson(), dto.getSalesPerson(),
                () -> report.setSalesPerson(dto.getSalesPerson()));
        logAndSet(report, user, "expectedDeliveryDate", str(report.getExpectedDeliveryDate()), str(dto.getExpectedDeliveryDate()),
                () -> report.setExpectedDeliveryDate(dto.getExpectedDeliveryDate()));
        logAndSet(report, user, "finalizedListSentDate", str(report.getFinalizedListSentDate()), str(dto.getFinalizedListSentDate()),
                () -> report.setFinalizedListSentDate(dto.getFinalizedListSentDate()));
        logAndSet(report, user, "finalizedListReceivedDate", str(report.getFinalizedListReceivedDate()), str(dto.getFinalizedListReceivedDate()),
                () -> report.setFinalizedListReceivedDate(dto.getFinalizedListReceivedDate()));
        logAndSet(report, user, "deliveryMethod", report.getDeliveryMethod(), dto.getDeliveryMethod(),
                () -> report.setDeliveryMethod(dto.getDeliveryMethod()));
        logAndSet(report, user, "extraPaymentDate", str(report.getExtraPaymentDate()), str(dto.getExtraPaymentDate()),
                () -> report.setExtraPaymentDate(dto.getExtraPaymentDate()));
        logAndSet(report, user, "extraPaymentAmount", str(report.getExtraPaymentAmount()), str(dto.getExtraPaymentAmount()),
                () -> report.setExtraPaymentAmount(dto.getExtraPaymentAmount()));
        logAndSet(report, user, "note", report.getNote(), dto.getNote(),
                () -> report.setNote(dto.getNote()));
    }

    private void updateMeasurementFields(ContractReport report, ContractReportUpdateDTO dto, User user) {
        logAndSet(report, user, "measurementStart", str(report.getMeasurementStart()), str(dto.getMeasurementStart()),
                () -> report.setMeasurementStart(dto.getMeasurementStart()));
        logAndSet(report, user, "measurementEnd", str(report.getMeasurementEnd()), str(dto.getMeasurementEnd()),
                () -> report.setMeasurementEnd(dto.getMeasurementEnd()));
        logAndSet(report, user, "technicianName", report.getTechnicianName(), dto.getTechnicianName(),
                () -> report.setTechnicianName(dto.getTechnicianName()));
        logAndSet(report, user, "measurementReceivedDate", str(report.getMeasurementReceivedDate()), str(dto.getMeasurementReceivedDate()),
                () -> report.setMeasurementReceivedDate(dto.getMeasurementReceivedDate()));
        logAndSet(report, user, "measurementHandler", report.getMeasurementHandler(), dto.getMeasurementHandler(),
                () -> report.setMeasurementHandler(dto.getMeasurementHandler()));
        logAndSet(report, user, "skipMeasurement", str(report.getSkipMeasurement()), str(dto.getSkipMeasurement()),
                () -> report.setSkipMeasurement(dto.getSkipMeasurement() != null ? dto.getSkipMeasurement() : false));
        logAndSet(report, user, "productionHandoverDate", str(report.getProductionHandoverDate()), str(dto.getProductionHandoverDate()),
                () -> report.setProductionHandoverDate(dto.getProductionHandoverDate()));
    }

    private void updateProductionFields(ContractReport report, ContractReportUpdateDTO dto, User user) {
        logAndSet(report, user, "packingReturnDate", str(report.getPackingReturnDate()), str(dto.getPackingReturnDate()),
                () -> report.setPackingReturnDate(dto.getPackingReturnDate()));
        logAndSet(report, user, "tailorStartDate", str(report.getTailorStartDate()), str(dto.getTailorStartDate()),
                () -> report.setTailorStartDate(dto.getTailorStartDate()));
        logAndSet(report, user, "tailorExpectedReturn", str(report.getTailorExpectedReturn()), str(dto.getTailorExpectedReturn()),
                () -> report.setTailorExpectedReturn(dto.getTailorExpectedReturn()));
        logAndSet(report, user, "tailorActualReturn", str(report.getTailorActualReturn()), str(dto.getTailorActualReturn()),
                () -> report.setTailorActualReturn(dto.getTailorActualReturn()));
    }

    private void updateStockkeeperFields(ContractReport report, ContractReportUpdateDTO dto, User user) {
        logAndSet(report, user, "actualShippingDate", str(report.getActualShippingDate()), str(dto.getActualShippingDate()),
                () -> report.setActualShippingDate(dto.getActualShippingDate()));
    }

    private void updateAllFields(ContractReport report, ContractReportUpdateDTO dto, User user) {
        updateSalesFields(report, dto, user);
        updateMeasurementFields(report, dto, user);
        updateProductionFields(report, dto, user);
        updateStockkeeperFields(report, dto, user);
    }

    // ========== History helpers ==========

    private void logAndSet(ContractReport report, User user, String fieldName,
                           String oldValue, String newValue, Runnable setter) {
        if (!Objects.equals(oldValue, newValue)) {
            logHistory(report.getReportId(), user, "EDIT", fieldName, oldValue, newValue, null);
            setter.run();
        }
    }

    private void logFieldChange(ContractReport report, User user, String fieldName,
                                String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            logHistory(report.getReportId(), user, "EDIT", fieldName, oldValue, newValue, null);
        }
    }

    private void logHistory(Long reportId, User user, String action,
                            String fieldName, String oldValue, String newValue, String reason) {
        ContractReportHistory history = new ContractReportHistory();
        history.setReportId(reportId);
        history.setChangedByUser(user);
        history.setChangedAt(LocalDateTime.now());
        history.setAction(action);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setReason(reason);
        historyRepository.save(history);
    }

    // ========== Create helper ==========

    private void applySalesFields(ContractReportCreateDTO dto, ContractReport entity) {
        entity.setSalesPerson(dto.getSalesPerson());
        entity.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        entity.setFinalizedListSentDate(dto.getFinalizedListSentDate());
        entity.setFinalizedListReceivedDate(dto.getFinalizedListReceivedDate());
        entity.setDeliveryMethod(dto.getDeliveryMethod());
        entity.setExtraPaymentDate(dto.getExtraPaymentDate());
        entity.setExtraPaymentAmount(dto.getExtraPaymentAmount());
        entity.setNote(dto.getNote());
    }

    // ========== DTO mapping ==========

    private ContractReportListDTO toListDTO(ContractReport cr) {
        ContractReportListDTO dto = new ContractReportListDTO();
        dto.setReportId(cr.getReportId());
        dto.setCurrentPhase(cr.getCurrentPhase().name());
        dto.setUnitId(cr.getUnit().getUnitId());
        dto.setUnitName(cr.getUnit().getUnitName());
        dto.setSalesPerson(cr.getSalesPerson());
        dto.setExpectedDeliveryDate(cr.getExpectedDeliveryDate());
        dto.setFinalizedListSentDate(cr.getFinalizedListSentDate());
        dto.setFinalizedListReceivedDate(cr.getFinalizedListReceivedDate());
        dto.setDeliveryMethod(cr.getDeliveryMethod());
        dto.setExtraPaymentDate(cr.getExtraPaymentDate());
        dto.setExtraPaymentAmount(cr.getExtraPaymentAmount());
        dto.setNote(cr.getNote());
        dto.setMeasurementStart(cr.getMeasurementStart());
        dto.setMeasurementEnd(cr.getMeasurementEnd());
        dto.setTechnicianName(cr.getTechnicianName());
        dto.setMeasurementReceivedDate(cr.getMeasurementReceivedDate());
        dto.setMeasurementHandler(cr.getMeasurementHandler());
        dto.setSkipMeasurement(cr.getSkipMeasurement());
        dto.setProductionHandoverDate(cr.getProductionHandoverDate());
        dto.setPackingReturnDate(cr.getPackingReturnDate());
        dto.setTailorStartDate(cr.getTailorStartDate());
        dto.setTailorExpectedReturn(cr.getTailorExpectedReturn());
        dto.setTailorActualReturn(cr.getTailorActualReturn());
        dto.setActualShippingDate(cr.getActualShippingDate());
        dto.setCreatedByName(cr.getCreatedByUser() != null ? cr.getCreatedByUser().getFullName() : null);
        dto.setCreatedAt(cr.getCreatedAt());
        dto.setUpdatedAt(cr.getUpdatedAt());
        dto.setDaysLate(calculateDaysLate(cr));
        return dto;
    }

    private Integer calculateDaysLate(ContractReport cr) {
        if (cr.getExpectedDeliveryDate() == null || cr.getActualShippingDate() != null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(cr.getExpectedDeliveryDate())) {
            return (int) ChronoUnit.DAYS.between(cr.getExpectedDeliveryDate(), today);
        }
        return null;
    }

    private String str(Object value) {
        return value == null ? null : value.toString();
    }

    private long toLong(Object value) {
        if (value == null) return 0;
        return ((Number) value).longValue();
    }
}
