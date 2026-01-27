package manage.store.inventory.controller;

import java.util.Arrays;
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
import manage.store.inventory.dto.RejectReasonDTO;
import manage.store.inventory.dto.RequestSetCreateDTO;
import manage.store.inventory.dto.RequestSetDetailDTO;
import manage.store.inventory.dto.RequestSetListDTO;
import manage.store.inventory.dto.RequestSetUpdateDTO;
import manage.store.inventory.entity.User;
import manage.store.inventory.repository.RequestSetRepository;
import manage.store.inventory.repository.UserRepository;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.RequestSetService;

@RestController
@RequestMapping("/api/request-sets")
public class RequestSetController {

    private final RequestSetService requestSetService;
    private final RequestSetRepository requestSetRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public RequestSetController(
            RequestSetService requestSetService,
            RequestSetRepository requestSetRepository,
            UserRepository userRepository,
            CurrentUser currentUser
    ) {
        this.requestSetService = requestSetService;
        this.requestSetRepository = requestSetRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    // Lấy tên gợi ý cho bộ phiếu mới
    // Format: "ĐX {số thứ tự} - {tên user}"
    @GetMapping("/suggested-name")
    @PreAuthorize("hasAnyRole('USER', 'PURCHASER')")
    public Map<String, String> getSuggestedName() {
        Long userId = currentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Long count = requestSetRepository.countByCreatedByUserId(userId);
        String suggestedName = "ĐX " + (count + 1) + " - " + user.getFullName();

        return Map.of("suggestedName", suggestedName);
    }

    // Tạo bộ phiếu mới (USER, PURCHASER)
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'PURCHASER')")
    public ResponseEntity<Long> createRequestSet(@Valid @RequestBody RequestSetCreateDTO dto) {
        Long setId = requestSetService.createRequestSet(dto, currentUser.getUserId());
        return ResponseEntity.ok(setId);
    }

    // Lấy danh sách bộ phiếu (phân quyền theo role)
    // - USER/PURCHASER: chỉ xem của mình
    // - ADMIN: xem tất cả, sắp xếp theo tên người tạo
    // - STOCKKEEPER: xem tất cả
    // Hỗ trợ: ?status=APPROVED hoặc ?status=APPROVED,EXECUTED
    @GetMapping
    public List<RequestSetListDTO> getAllRequestSets(
            @RequestParam(required = false) String status
    ) {
        Long userId = currentUser.getUserId();
        if (status != null && !status.isEmpty()) {
            // Kiểm tra nếu có nhiều status (phân cách bằng dấu phẩy)
            if (status.contains(",")) {
                List<String> statuses = Arrays.asList(status.split(","));
                return requestSetService.getRequestSetsByStatuses(statuses, userId);
            }
            return requestSetService.getRequestSetsByStatus(status, userId);
        }
        return requestSetService.getAllRequestSets(userId);
    }

    // Lấy chi tiết bộ phiếu
    @GetMapping("/{setId:\\d+}")
    public RequestSetDetailDTO getRequestSetDetail(@PathVariable Long setId) {
        return requestSetService.getRequestSetDetail(setId);
    }

    // Cập nhật bộ phiếu đã bị từ chối và tự động gửi duyệt lại
    // - Chỉ chủ phiếu (USER, PURCHASER) mới được cập nhật
    // - Chỉ cập nhật được khi status = REJECTED
    // - Sau khi cập nhật, status tự động chuyển sang PENDING
    @PutMapping("/{setId:\\d+}")
    @PreAuthorize("hasAnyRole('USER', 'PURCHASER')")
    public ResponseEntity<Void> updateRequestSet(
            @PathVariable Long setId,
            @Valid @RequestBody RequestSetUpdateDTO dto
    ) {
        requestSetService.updateRequestSet(setId, dto, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    // Submit bộ phiếu để chờ duyệt (USER, PURCHASER - chủ phiếu)
    @PostMapping("/{setId:\\d+}/submit")
    @PreAuthorize("hasAnyRole('USER', 'PURCHASER')")
    public ResponseEntity<Void> submitForApproval(@PathVariable Long setId) {
        requestSetService.submitForApproval(setId, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    // Duyệt bộ phiếu (chỉ ADMIN)
    @PostMapping("/{setId:\\d+}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approve(@PathVariable Long setId) {
        requestSetService.approve(setId, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    // Từ chối bộ phiếu (chỉ ADMIN)
    @PostMapping("/{setId:\\d+}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reject(
            @PathVariable Long setId,
            @Valid @RequestBody RejectReasonDTO dto
    ) {
        requestSetService.reject(setId, currentUser.getUserId(), dto.getReason());
        return ResponseEntity.ok().build();
    }

    // Xóa bộ phiếu (chủ phiếu)
    @DeleteMapping("/{setId:\\d+}")
    public ResponseEntity<Void> deleteRequestSet(@PathVariable Long setId) {
        requestSetService.deleteRequestSet(setId);
        return ResponseEntity.noContent().build();
    }

    // Xóa tất cả bộ phiếu (ADMIN only)
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllRequestSets() {
        requestSetService.deleteAllRequestSets();
        return ResponseEntity.noContent().build();
    }

    // Xác nhận đã thực hiện nhập/xuất kho (chỉ STOCKKEEPER)
    @PostMapping("/{setId:\\d+}/execute")
    @PreAuthorize("hasRole('STOCKKEEPER')")
    public ResponseEntity<Void> execute(@PathVariable Long setId) {
        requestSetService.execute(setId, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    // Lấy danh sách bộ phiếu đã duyệt (chờ STOCKKEEPER thực hiện)
    @GetMapping("/approved")
    @PreAuthorize("hasRole('STOCKKEEPER')")
    public List<RequestSetListDTO> getApprovedRequestSets() {
        return requestSetService.getApprovedRequestSets();
    }
}
