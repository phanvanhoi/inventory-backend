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
import manage.store.inventory.dto.MissingItemCreateDTO;
import manage.store.inventory.dto.MissingItemDTO;
import manage.store.inventory.dto.PackingBatchCreateDTO;
import manage.store.inventory.dto.PackingBatchDTO;
import manage.store.inventory.entity.enums.PackingBatchStatus;
import manage.store.inventory.service.PackingService;

/**
 * Packing endpoints (G8, W19).
 * Role PACKER cho mutations.
 */
@RestController
@RequestMapping("/api")
public class PackingController {

    private final PackingService packingService;

    public PackingController(PackingService packingService) {
        this.packingService = packingService;
    }

    // ===== Packing batches =====

    @GetMapping("/orders/{orderId}/packing-batches")
    public List<PackingBatchDTO> getBatchesByOrder(@PathVariable Long orderId) {
        return packingService.getBatchesByOrder(orderId);
    }

    @GetMapping("/packing-batches/{batchId}")
    public PackingBatchDTO getBatch(@PathVariable Long batchId) {
        return packingService.getBatchById(batchId);
    }

    @PostMapping("/orders/{orderId}/packing-batches")
    @PreAuthorize("hasAnyRole('PACKER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Long> createBatch(
            @PathVariable Long orderId,
            @Valid @RequestBody PackingBatchCreateDTO dto) {
        return ResponseEntity.ok(packingService.createBatch(orderId, dto));
    }

    @PutMapping("/packing-batches/{batchId}")
    @PreAuthorize("hasAnyRole('PACKER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> updateBatch(
            @PathVariable Long batchId,
            @Valid @RequestBody PackingBatchCreateDTO dto) {
        packingService.updateBatch(batchId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/packing-batches/{batchId}/status")
    @PreAuthorize("hasAnyRole('PACKER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long batchId,
            @RequestParam PackingBatchStatus status) {
        packingService.changeStatus(batchId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/packing-batches/{batchId}")
    @PreAuthorize("hasAnyRole('PACKER','ADMIN')")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long batchId) {
        packingService.deleteBatch(batchId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/packing-batches/overdue")
    public List<PackingBatchDTO> getOverdue() {
        return packingService.getOverdueBatches(LocalDate.now());
    }

    // ===== Missing items =====

    @GetMapping("/packing-batches/{batchId}/missing-items")
    public List<MissingItemDTO> getMissingItems(@PathVariable Long batchId) {
        return packingService.getMissingItems(batchId);
    }

    @GetMapping("/orders/{orderId}/missing-items/unresolved")
    public List<MissingItemDTO> getUnresolvedByOrder(@PathVariable Long orderId) {
        return packingService.getUnresolvedByOrder(orderId);
    }

    @PostMapping("/packing-batches/{batchId}/missing-items")
    @PreAuthorize("hasAnyRole('PACKER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Long> addMissing(
            @PathVariable Long batchId,
            @Valid @RequestBody MissingItemCreateDTO dto) {
        return ResponseEntity.ok(packingService.addMissingItem(batchId, dto));
    }

    @PutMapping("/missing-items/{missingId}")
    @PreAuthorize("hasAnyRole('PACKER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> updateMissing(
            @PathVariable Long missingId,
            @Valid @RequestBody MissingItemCreateDTO dto) {
        packingService.updateMissingItem(missingId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/missing-items/{missingId}/resolve")
    @PreAuthorize("hasAnyRole('PACKER','STOCKKEEPER','ADMIN')")
    public ResponseEntity<Void> markResolved(
            @PathVariable Long missingId,
            @RequestParam(defaultValue = "true") Boolean resolved) {
        packingService.markResolved(missingId, resolved);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/missing-items/{missingId}")
    @PreAuthorize("hasAnyRole('PACKER','ADMIN')")
    public ResponseEntity<Void> deleteMissing(@PathVariable Long missingId) {
        packingService.deleteMissingItem(missingId);
        return ResponseEntity.ok().build();
    }
}
