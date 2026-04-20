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
import manage.store.inventory.dto.DesignDocumentCreateDTO;
import manage.store.inventory.dto.DesignDocumentDTO;
import manage.store.inventory.dto.DesignSampleCreateDTO;
import manage.store.inventory.dto.DesignSampleDTO;
import manage.store.inventory.entity.enums.DesignSampleStatus;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.DesignService;

/**
 * Design endpoints (G4, W11-12).
 * Samples per OrderItem, documents per Order.
 * Auto sets orders.design_ready when all items have APPROVED sample.
 */
@RestController
@RequestMapping("/api")
public class DesignController {

    private final DesignService designService;
    private final CurrentUser currentUser;

    public DesignController(DesignService designService, CurrentUser currentUser) {
        this.designService = designService;
        this.currentUser = currentUser;
    }

    // ===== Samples =====

    @GetMapping("/orders/{orderId}/design-samples")
    public List<DesignSampleDTO> getSamplesByOrder(@PathVariable Long orderId) {
        return designService.getSamplesByOrder(orderId);
    }

    @GetMapping("/order-items/{itemId}/design-samples")
    public List<DesignSampleDTO> getSamplesByItem(@PathVariable Long itemId) {
        return designService.getSamplesByOrderItem(itemId);
    }

    @PostMapping("/order-items/{itemId}/design-samples")
    @PreAuthorize("hasAnyRole('DESIGNER','ADMIN')")
    public ResponseEntity<Long> addSample(
            @PathVariable Long itemId,
            @Valid @RequestBody DesignSampleCreateDTO dto) {
        return ResponseEntity.ok(designService.addSample(itemId, dto));
    }

    @PutMapping("/design-samples/{sampleId}")
    @PreAuthorize("hasAnyRole('DESIGNER','ADMIN')")
    public ResponseEntity<Void> updateSample(
            @PathVariable Long sampleId,
            @Valid @RequestBody DesignSampleCreateDTO dto) {
        designService.updateSample(sampleId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/design-samples/{sampleId}/status")
    @PreAuthorize("hasAnyRole('DESIGNER','ADMIN','SALES')")
    public ResponseEntity<Void> changeSampleStatus(
            @PathVariable Long sampleId,
            @RequestParam DesignSampleStatus status) {
        designService.changeSampleStatus(sampleId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/design-samples/{sampleId}")
    @PreAuthorize("hasAnyRole('DESIGNER','ADMIN')")
    public ResponseEntity<Void> deleteSample(@PathVariable Long sampleId) {
        designService.deleteSample(sampleId);
        return ResponseEntity.ok().build();
    }

    // ===== Documents =====

    @GetMapping("/orders/{orderId}/design-documents")
    public List<DesignDocumentDTO> getDocumentsByOrder(@PathVariable Long orderId) {
        return designService.getDocumentsByOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/design-documents")
    @PreAuthorize("hasAnyRole('DESIGNER','SALES','ADMIN')")
    public ResponseEntity<Long> addDocument(
            @PathVariable Long orderId,
            @Valid @RequestBody DesignDocumentCreateDTO dto) {
        return ResponseEntity.ok(designService.addDocument(orderId, dto, currentUser.getUserId()));
    }

    @DeleteMapping("/design-documents/{docId}")
    @PreAuthorize("hasAnyRole('DESIGNER','ADMIN')")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long docId) {
        designService.deleteDocument(docId);
        return ResponseEntity.ok().build();
    }

    // ===== Recompute design_ready =====

    @PostMapping("/orders/{orderId}/design-ready/recompute")
    @PreAuthorize("hasAnyRole('DESIGNER','ADMIN','SALES')")
    public ResponseEntity<Map<String, Boolean>> recomputeDesignReady(@PathVariable Long orderId) {
        boolean ready = designService.recomputeDesignReady(orderId);
        return ResponseEntity.ok(Map.of("designReady", ready));
    }
}
