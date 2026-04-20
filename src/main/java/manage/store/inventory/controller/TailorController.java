package manage.store.inventory.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.dto.TailorAssignmentCreateDTO;
import manage.store.inventory.dto.TailorAssignmentDTO;
import manage.store.inventory.dto.TailorCreateDTO;
import manage.store.inventory.dto.TailorDTO;
import manage.store.inventory.entity.enums.TailorAssignmentStatus;
import manage.store.inventory.service.TailorService;

/**
 * Tailor + TailorAssignment endpoints (G5, W13-14).
 * Role PRODUCTION cho mutations.
 */
@RestController
@RequestMapping("/api")
public class TailorController {

    private final TailorService tailorService;

    public TailorController(TailorService tailorService) {
        this.tailorService = tailorService;
    }

    // ===== Tailors (master) =====

    @GetMapping("/tailors")
    public List<TailorDTO> getAllTailors(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        return tailorService.getAllTailors(activeOnly);
    }

    @GetMapping("/tailors/{tailorId}")
    public TailorDTO getTailorById(@PathVariable Long tailorId) {
        return tailorService.getTailorById(tailorId);
    }

    @PostMapping("/tailors")
    @PreAuthorize("hasAnyRole('PRODUCTION','ADMIN')")
    public ResponseEntity<Long> createTailor(@Valid @RequestBody TailorCreateDTO dto) {
        return ResponseEntity.ok(tailorService.createTailor(dto));
    }

    @PutMapping("/tailors/{tailorId}")
    @PreAuthorize("hasAnyRole('PRODUCTION','ADMIN')")
    public ResponseEntity<Void> updateTailor(
            @PathVariable Long tailorId,
            @Valid @RequestBody TailorCreateDTO dto) {
        tailorService.updateTailor(tailorId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tailors/{tailorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTailor(@PathVariable Long tailorId) {
        tailorService.deleteTailor(tailorId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tailors/{tailorId}/assignments")
    public List<TailorAssignmentDTO> getAssignmentsByTailor(@PathVariable Long tailorId) {
        return tailorService.getAssignmentsByTailor(tailorId);
    }

    // ===== Assignments (per OrderItem) =====

    @GetMapping("/orders/{orderId}/tailor-assignments")
    public List<TailorAssignmentDTO> getByOrder(@PathVariable Long orderId) {
        return tailorService.getAssignmentsByOrder(orderId);
    }

    @GetMapping("/order-items/{itemId}/tailor-assignments")
    public List<TailorAssignmentDTO> getByItem(@PathVariable Long itemId) {
        return tailorService.getAssignmentsByOrderItem(itemId);
    }

    @PostMapping("/order-items/{itemId}/tailor-assignments")
    @PreAuthorize("hasAnyRole('PRODUCTION','ADMIN')")
    public ResponseEntity<Long> addAssignment(
            @PathVariable Long itemId,
            @Valid @RequestBody TailorAssignmentCreateDTO dto) {
        return ResponseEntity.ok(tailorService.addAssignment(itemId, dto));
    }

    @PutMapping("/tailor-assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('PRODUCTION','ADMIN')")
    public ResponseEntity<Void> updateAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody TailorAssignmentCreateDTO dto) {
        tailorService.updateAssignment(assignmentId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tailor-assignments/{assignmentId}/status")
    @PreAuthorize("hasAnyRole('PRODUCTION','ADMIN','STOCKKEEPER')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long assignmentId,
            @RequestParam TailorAssignmentStatus status) {
        tailorService.changeAssignmentStatus(assignmentId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tailor-assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('PRODUCTION','ADMIN')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
        tailorService.deleteAssignment(assignmentId);
        return ResponseEntity.ok().build();
    }

    // ===== Alerts =====

    @GetMapping("/tailor-assignments/overdue")
    public List<TailorAssignmentDTO> getOverdue() {
        return tailorService.getOverdueAssignments(LocalDate.now());
    }
}
