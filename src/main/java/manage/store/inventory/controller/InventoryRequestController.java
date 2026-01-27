package manage.store.inventory.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.dto.InventoryRequestCreateDTO;
import manage.store.inventory.dto.InventoryRequestDetailDTO;
import manage.store.inventory.dto.InventoryRequestItemDTO;
import manage.store.inventory.dto.InventoryRequestListDTO;
import manage.store.inventory.dto.UpdateExpectedDateDTO;
import manage.store.inventory.dto.UpdateRequestTypeDTO;
import manage.store.inventory.service.InventoryRequestService;

@RestController
@RequestMapping("/api/requests")
public class InventoryRequestController {

    private final InventoryRequestService service;

    public InventoryRequestController(InventoryRequestService service) {
        this.service = service;
    }

    // ===== POST: TẠO REQUEST =====
    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestBody InventoryRequestCreateDTO dto
    ) {
        Long requestId = service.createRequest(dto);
        return ResponseEntity.ok(Map.of("requestId", requestId));
    }

    // ===== GET: XEM REQUEST =====
    @GetMapping("/{id}")
    public ResponseEntity<List<InventoryRequestItemDTO>> getRequest(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(service.getRequestItems(id));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<InventoryRequestDetailDTO> getRequestDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getRequestDetail(id));
    }

    @GetMapping
    public List<InventoryRequestListDTO> getAllRequests() {
        return service.getAllRequests();
    }

    // ===== DELETE: XÓA REQUEST =====
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    // ===== DELETE: XÓA TẤT CẢ REQUESTS =====
    @DeleteMapping
    public ResponseEntity<Void> deleteAllRequests() {
        service.deleteAllRequests();
        return ResponseEntity.noContent().build();
    }

    // ===== PATCH: CẬP NHẬT REQUEST TYPE =====
    // ADJUST_IN -> IN, ADJUST_OUT -> OUT
    @PatchMapping("/{id}/request-type")
    public ResponseEntity<Void> updateRequestType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestTypeDTO dto
    ) {
        service.updateRequestType(id, dto.getRequestType());
        return ResponseEntity.ok().build();
    }

    // ===== PATCH: CẬP NHẬT NGÀY DỰ KIẾN =====
    // Chỉ áp dụng cho ADJUST_IN và ADJUST_OUT
    // Quy tắc: Muốn dời ADJUST_IN thì phải dời hết ADJUST_OUT phụ thuộc trước
    @PatchMapping("/{id}/expected-date")
    public ResponseEntity<?> updateExpectedDate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpectedDateDTO dto
    ) {
        service.updateExpectedDate(id, dto.getNewExpectedDate(), dto.getUserId());
        return ResponseEntity.ok(Map.of(
                "message", "Cập nhật ngày dự kiến thành công",
                "requestId", id,
                "newExpectedDate", dto.getNewExpectedDate().toString()
        ));
    }

    // ===== GET: KIỂM TRA CÓ ADJUST_OUT PHỤ THUỘC =====
    // Dùng để kiểm tra trước khi dời ADJUST_IN
    @GetMapping("/{id}/dependent-adjust-out")
    public ResponseEntity<?> getDependentAdjustOutCount(@PathVariable Long id) {
        int count = service.countDependentAdjustOut(id);
        return ResponseEntity.ok(Map.of(
                "requestId", id,
                "dependentAdjustOutCount", count,
                "canMoveDate", count == 0
        ));
    }
}
