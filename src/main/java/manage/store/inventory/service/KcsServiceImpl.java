package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.QualityCheckCreateDTO;
import manage.store.inventory.dto.QualityCheckDTO;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.OrderItem;
import manage.store.inventory.entity.QualityCheck;
import manage.store.inventory.entity.TailorAssignment;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.QualityCheckStatus;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.OrderItemRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.QualityCheckRepository;
import manage.store.inventory.repository.TailorAssignmentRepository;
import manage.store.inventory.repository.UserRepository;

@Service
@Transactional
public class KcsServiceImpl implements KcsService {

    private final QualityCheckRepository qcRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final TailorAssignmentRepository tailorAssignmentRepository;
    private final UserRepository userRepository;

    public KcsServiceImpl(
            QualityCheckRepository qcRepository,
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            TailorAssignmentRepository tailorAssignmentRepository,
            UserRepository userRepository) {
        this.qcRepository = qcRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.tailorAssignmentRepository = tailorAssignmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Long addCheck(Long orderItemId, QualityCheckCreateDTO dto) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));

        QualityCheck qc = new QualityCheck();
        qc.setOrderItem(item);
        applyFields(dto, qc);
        qc.setCreatedAt(LocalDateTime.now());
        qcRepository.save(qc);

        // Consistency với updateCheck/deleteCheck/changeStatus: always recompute
        recomputeQcPassed(item.getOrder().getOrderId());
        return qc.getQcId();
    }

    @Override
    public void updateCheck(Long qcId, QualityCheckCreateDTO dto) {
        QualityCheck qc = qcRepository.findById(qcId)
                .orElseThrow(() -> new ResourceNotFoundException("QC không tồn tại"));
        applyFields(dto, qc);
        qcRepository.save(qc);
        recomputeQcPassed(qc.getOrderItem().getOrder().getOrderId());
    }

    @Override
    public void changeStatus(Long qcId, QualityCheckStatus status) {
        QualityCheck qc = qcRepository.findById(qcId)
                .orElseThrow(() -> new ResourceNotFoundException("QC không tồn tại"));
        qc.setStatus(status);
        // Auto-set completed_date khi PASSED/FAILED/RETURNED
        if (qc.getCompletedDate() == null &&
                (status == QualityCheckStatus.PASSED
                        || status == QualityCheckStatus.FAILED
                        || status == QualityCheckStatus.RETURNED)) {
            qc.setCompletedDate(LocalDate.now());
        }
        qcRepository.save(qc);
        recomputeQcPassed(qc.getOrderItem().getOrder().getOrderId());
    }

    @Override
    public void deleteCheck(Long qcId) {
        QualityCheck qc = qcRepository.findById(qcId)
                .orElseThrow(() -> new ResourceNotFoundException("QC không tồn tại"));
        Long orderId = qc.getOrderItem().getOrder().getOrderId();
        qcRepository.delete(qc);
        recomputeQcPassed(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityCheckDTO> getChecksByOrderItem(Long orderItemId) {
        return qcRepository.findByOrderItemId(orderItemId).stream()
                .map(QualityCheckDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityCheckDTO> getChecksByOrder(Long orderId) {
        return qcRepository.findByOrderId(orderId).stream()
                .map(QualityCheckDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * Auto qc_passed: set TRUE khi tất cả order_items có ≥1 QC PASSED.
     * Nếu order có skip_kcs=TRUE → flag không áp dụng (giữ nguyên).
     */
    @Override
    public boolean recomputeQcPassed(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        if (Boolean.TRUE.equals(order.getSkipKcs())) {
            return Boolean.TRUE.equals(order.getQcPassed());
        }

        long totalItems = orderItemRepository.countActiveByOrderId(orderId);
        long passedItems = qcRepository.countPassedItems(orderId);
        boolean passed = totalItems > 0 && passedItems >= totalItems;

        if (passed != Boolean.TRUE.equals(order.getQcPassed())) {
            order.setQcPassed(passed);
            orderRepository.save(order);
        }
        return passed;
    }

    private void applyFields(QualityCheckCreateDTO dto, QualityCheck qc) {
        if (dto.getTailorAssignmentId() != null) {
            TailorAssignment ta = tailorAssignmentRepository.findById(dto.getTailorAssignmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tailor assignment không tồn tại"));
            qc.setTailorAssignment(ta);
        } else {
            qc.setTailorAssignment(null);
        }
        if (dto.getKcsUserId() != null) {
            User kcs = userRepository.findById(dto.getKcsUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("KCS user không tồn tại"));
            qc.setKcsUser(kcs);
        } else {
            qc.setKcsUser(null);
        }
        qc.setReceivedDate(dto.getReceivedDate());
        qc.setCompletedDate(dto.getCompletedDate());
        if (dto.getFullDocumentsReceived() != null) {
            qc.setFullDocumentsReceived(dto.getFullDocumentsReceived());
        }
        if (dto.getFullVariantsReceived() != null) {
            qc.setFullVariantsReceived(dto.getFullVariantsReceived());
        }
        if (dto.getStatus() != null) qc.setStatus(dto.getStatus());
        qc.setNotes(dto.getNotes());
    }
}
