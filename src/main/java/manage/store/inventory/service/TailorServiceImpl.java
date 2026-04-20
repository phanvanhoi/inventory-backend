package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.TailorAssignmentCreateDTO;
import manage.store.inventory.dto.TailorAssignmentDTO;
import manage.store.inventory.dto.TailorCreateDTO;
import manage.store.inventory.dto.TailorDTO;
import manage.store.inventory.entity.OrderItem;
import manage.store.inventory.entity.Tailor;
import manage.store.inventory.entity.TailorAssignment;
import manage.store.inventory.entity.enums.TailorAssignmentStatus;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.OrderItemRepository;
import manage.store.inventory.repository.TailorAssignmentRepository;
import manage.store.inventory.repository.TailorRepository;

@Service
@Transactional
public class TailorServiceImpl implements TailorService {

    private final TailorRepository tailorRepository;
    private final TailorAssignmentRepository assignmentRepository;
    private final OrderItemRepository itemRepository;

    public TailorServiceImpl(
            TailorRepository tailorRepository,
            TailorAssignmentRepository assignmentRepository,
            OrderItemRepository itemRepository) {
        this.tailorRepository = tailorRepository;
        this.assignmentRepository = assignmentRepository;
        this.itemRepository = itemRepository;
    }

    // ===== Tailor master =====

    @Override
    public Long createTailor(TailorCreateDTO dto) {
        Tailor t = new Tailor();
        applyTailor(dto, t);
        t.setCreatedAt(LocalDateTime.now());
        tailorRepository.save(t);
        return t.getTailorId();
    }

    @Override
    public void updateTailor(Long tailorId, TailorCreateDTO dto) {
        Tailor t = tailorRepository.findById(tailorId)
                .orElseThrow(() -> new ResourceNotFoundException("Thợ không tồn tại"));
        applyTailor(dto, t);
        tailorRepository.save(t);
    }

    @Override
    public void deleteTailor(Long tailorId) {
        Tailor t = tailorRepository.findById(tailorId)
                .orElseThrow(() -> new ResourceNotFoundException("Thợ không tồn tại"));
        t.setDeletedAt(LocalDateTime.now());
        tailorRepository.save(t);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TailorDTO> getAllTailors(Boolean activeOnly) {
        List<Tailor> list = Boolean.TRUE.equals(activeOnly)
                ? tailorRepository.findByActiveTrue()
                : tailorRepository.findAll();
        return list.stream().map(TailorDTO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TailorDTO getTailorById(Long tailorId) {
        return tailorRepository.findById(tailorId)
                .map(TailorDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Thợ không tồn tại"));
    }

    // ===== Assignment =====

    @Override
    public Long addAssignment(Long orderItemId, TailorAssignmentCreateDTO dto) {
        OrderItem item = itemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));
        Tailor tailor = tailorRepository.findById(dto.getTailorId())
                .orElseThrow(() -> new ResourceNotFoundException("Thợ không tồn tại"));

        validateQtyAssignment(orderItemId, dto.getQtyAssigned(), null, item.getQtyContract());

        TailorAssignment a = new TailorAssignment();
        a.setOrderItem(item);
        a.setTailor(tailor);
        applyAssignment(dto, a);
        a.setCreatedAt(LocalDateTime.now());
        assignmentRepository.save(a);
        return a.getAssignmentId();
    }

    @Override
    public void updateAssignment(Long assignmentId, TailorAssignmentCreateDTO dto) {
        TailorAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment không tồn tại"));

        if (dto.getTailorId() != null
                && !dto.getTailorId().equals(a.getTailor().getTailorId())) {
            Tailor tailor = tailorRepository.findById(dto.getTailorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Thợ không tồn tại"));
            a.setTailor(tailor);
        }

        Integer newQty = dto.getQtyAssigned() != null ? dto.getQtyAssigned() : a.getQtyAssigned();
        validateQtyAssignment(a.getOrderItem().getOrderItemId(), newQty, assignmentId,
                a.getOrderItem().getQtyContract());

        applyAssignment(dto, a);
        assignmentRepository.save(a);
    }

    @Override
    public void changeAssignmentStatus(Long assignmentId, TailorAssignmentStatus status) {
        TailorAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment không tồn tại"));
        a.setStatus(status);
        // Khi COMPLETED và chưa có returned_date → set hôm nay
        if (status == TailorAssignmentStatus.COMPLETED && a.getReturnedDate() == null) {
            a.setReturnedDate(LocalDate.now());
        }
        assignmentRepository.save(a);
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment không tồn tại");
        }
        assignmentRepository.deleteById(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TailorAssignmentDTO> getAssignmentsByOrderItem(Long orderItemId) {
        return assignmentRepository.findByOrderItemId(orderItemId).stream()
                .map(TailorAssignmentDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TailorAssignmentDTO> getAssignmentsByOrder(Long orderId) {
        return assignmentRepository.findByOrderId(orderId).stream()
                .map(TailorAssignmentDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TailorAssignmentDTO> getAssignmentsByTailor(Long tailorId) {
        return assignmentRepository.findByTailorId(tailorId).stream()
                .map(TailorAssignmentDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TailorAssignmentDTO> getOverdueAssignments(LocalDate today) {
        return assignmentRepository.findOverdue(today).stream()
                .map(TailorAssignmentDTO::from)
                .collect(Collectors.toList());
    }

    // ===== helpers =====

    private void applyTailor(TailorCreateDTO dto, Tailor t) {
        if (dto.getName() != null) t.setName(dto.getName());
        t.setType(dto.getType());
        t.setPhone(dto.getPhone());
        t.setLocation(dto.getLocation());
        if (dto.getActive() != null) t.setActive(dto.getActive());
        t.setNote(dto.getNote());
    }

    private void applyAssignment(TailorAssignmentCreateDTO dto, TailorAssignment a) {
        if (dto.getQtyAssigned() != null) a.setQtyAssigned(dto.getQtyAssigned());
        if (dto.getQtyFromStock() != null) a.setQtyFromStock(dto.getQtyFromStock());
        if (dto.getQtyReturned() != null) a.setQtyReturned(dto.getQtyReturned());
        a.setAppointmentDate(dto.getAppointmentDate());
        a.setReturnedDate(dto.getReturnedDate());
        a.setTailorType(dto.getTailorType());
        a.setNplProposalUrl(dto.getNplProposalUrl());
        if (dto.getStatus() != null) a.setStatus(dto.getStatus());
        a.setNote(dto.getNote());
    }

    /**
     * Validate: sum(qty_assigned) of all assignments for this item &lt;= qty_contract.
     * Excludes current assignment for update scenario.
     */
    private void validateQtyAssignment(Long orderItemId, Integer newQty, Long excludeId, Integer qtyContract) {
        if (newQty == null || newQty <= 0) return;
        if (qtyContract == null || qtyContract <= 0) return;
        long existing = assignmentRepository.sumQtyAssignedExcluding(
                orderItemId, excludeId == null ? -1L : excludeId);
        if (existing + newQty > qtyContract) {
            throw new BusinessException(
                    "Tổng slg giao thợ (" + (existing + newQty) +
                    ") vượt slg HĐ (" + qtyContract + ")");
        }
    }
}
