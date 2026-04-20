package manage.store.inventory.controller;

import java.util.List;
import java.util.Map;

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
import manage.store.inventory.dto.QualityCheckCreateDTO;
import manage.store.inventory.dto.QualityCheckDTO;
import manage.store.inventory.entity.enums.QualityCheckStatus;
import manage.store.inventory.service.KcsService;

/**
 * KCS (Quality Check) endpoints (G7, W16-17).
 * Role KCS cho mutations; SALES/ADMIN có thể đổi status cuối cùng.
 * Auto sets orders.qc_passed when all items have PASSED qc.
 */
@RestController
@RequestMapping("/api")
public class KcsController {

    private final KcsService kcsService;

    public KcsController(KcsService kcsService) {
        this.kcsService = kcsService;
    }

    @GetMapping("/orders/{orderId}/quality-checks")
    public List<QualityCheckDTO> getByOrder(@PathVariable Long orderId) {
        return kcsService.getChecksByOrder(orderId);
    }

    @GetMapping("/order-items/{itemId}/quality-checks")
    public List<QualityCheckDTO> getByItem(@PathVariable Long itemId) {
        return kcsService.getChecksByOrderItem(itemId);
    }

    @PostMapping("/order-items/{itemId}/quality-checks")
    @PreAuthorize("hasAnyRole('KCS','ADMIN')")
    public ResponseEntity<Long> addCheck(
            @PathVariable Long itemId,
            @Valid @RequestBody QualityCheckCreateDTO dto) {
        return ResponseEntity.ok(kcsService.addCheck(itemId, dto));
    }

    @PutMapping("/quality-checks/{qcId}")
    @PreAuthorize("hasAnyRole('KCS','ADMIN')")
    public ResponseEntity<Void> updateCheck(
            @PathVariable Long qcId,
            @Valid @RequestBody QualityCheckCreateDTO dto) {
        kcsService.updateCheck(qcId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quality-checks/{qcId}/status")
    @PreAuthorize("hasAnyRole('KCS','ADMIN','SALES')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long qcId,
            @RequestParam QualityCheckStatus status) {
        kcsService.changeStatus(qcId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/quality-checks/{qcId}")
    @PreAuthorize("hasAnyRole('KCS','ADMIN')")
    public ResponseEntity<Void> deleteCheck(@PathVariable Long qcId) {
        kcsService.deleteCheck(qcId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{orderId}/qc-passed/recompute")
    @PreAuthorize("hasAnyRole('KCS','ADMIN','SALES')")
    public ResponseEntity<Map<String, Boolean>> recompute(@PathVariable Long orderId) {
        boolean passed = kcsService.recomputeQcPassed(orderId);
        return ResponseEntity.ok(Map.of("qcPassed", passed));
    }
}
