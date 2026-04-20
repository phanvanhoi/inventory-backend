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
import manage.store.inventory.dto.RepairRequestCreateDTO;
import manage.store.inventory.dto.RepairRequestDTO;
import manage.store.inventory.entity.enums.RepairStatus;
import manage.store.inventory.service.RepairService;

/**
 * Repair (hàng quay đầu) endpoints (G9, W20).
 * Role REPAIRER cho mutations.
 */
@RestController
@RequestMapping("/api")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping("/orders/{orderId}/repairs")
    public List<RepairRequestDTO> getByOrder(@PathVariable Long orderId) {
        return repairService.getByOrder(orderId);
    }

    @GetMapping("/order-items/{itemId}/repairs")
    public List<RepairRequestDTO> getByItem(@PathVariable Long itemId) {
        return repairService.getByOrderItem(itemId);
    }

    @PostMapping("/repairs")
    @PreAuthorize("hasAnyRole('REPAIRER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Long> create(@Valid @RequestBody RepairRequestCreateDTO dto) {
        return ResponseEntity.ok(repairService.createRepair(dto));
    }

    @PutMapping("/repairs/{repairId}")
    @PreAuthorize("hasAnyRole('REPAIRER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> update(
            @PathVariable Long repairId,
            @Valid @RequestBody RepairRequestCreateDTO dto) {
        repairService.updateRepair(repairId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/repairs/{repairId}/status")
    @PreAuthorize("hasAnyRole('REPAIRER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long repairId,
            @RequestParam RepairStatus status) {
        repairService.changeStatus(repairId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/repairs/{repairId}")
    @PreAuthorize("hasAnyRole('REPAIRER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long repairId) {
        repairService.deleteRepair(repairId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{orderId}/has-repair/recompute")
    @PreAuthorize("hasAnyRole('REPAIRER','STOCKKEEPER','ADMIN','SALES')")
    public ResponseEntity<Map<String, Boolean>> recompute(@PathVariable Long orderId) {
        boolean has = repairService.recomputeHasRepair(orderId);
        return ResponseEntity.ok(Map.of("hasRepair", has));
    }
}
