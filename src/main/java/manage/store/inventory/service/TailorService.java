package manage.store.inventory.service;

import java.time.LocalDate;
import java.util.List;

import manage.store.inventory.dto.TailorAssignmentCreateDTO;
import manage.store.inventory.dto.TailorAssignmentDTO;
import manage.store.inventory.dto.TailorCreateDTO;
import manage.store.inventory.dto.TailorDTO;
import manage.store.inventory.entity.enums.TailorAssignmentStatus;

public interface TailorService {

    // Tailor master CRUD
    Long createTailor(TailorCreateDTO dto);
    void updateTailor(Long tailorId, TailorCreateDTO dto);
    void deleteTailor(Long tailorId);
    List<TailorDTO> getAllTailors(Boolean activeOnly);
    TailorDTO getTailorById(Long tailorId);

    // Assignment CRUD per OrderItem
    Long addAssignment(Long orderItemId, TailorAssignmentCreateDTO dto);
    void updateAssignment(Long assignmentId, TailorAssignmentCreateDTO dto);
    void changeAssignmentStatus(Long assignmentId, TailorAssignmentStatus status);
    void deleteAssignment(Long assignmentId);

    List<TailorAssignmentDTO> getAssignmentsByOrderItem(Long orderItemId);
    List<TailorAssignmentDTO> getAssignmentsByOrder(Long orderId);
    List<TailorAssignmentDTO> getAssignmentsByTailor(Long tailorId);
    List<TailorAssignmentDTO> getOverdueAssignments(LocalDate today);
}
