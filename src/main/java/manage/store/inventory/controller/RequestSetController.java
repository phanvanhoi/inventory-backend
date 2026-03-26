package manage.store.inventory.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import manage.store.inventory.dto.ReceiptCreateDTO;
import manage.store.inventory.dto.ReceiptDetailDTO;
import manage.store.inventory.dto.SetReceiptProgressDTO;
import manage.store.inventory.dto.EditAndReceiveDTO;
import manage.store.inventory.dto.RejectReasonDTO;
import manage.store.inventory.dto.RequestSetCreateDTO;
import manage.store.inventory.dto.RequestSetDetailDTO;
import manage.store.inventory.dto.RequestSetListDTO;
import manage.store.inventory.dto.RequestSetUpdateDTO;
import manage.store.inventory.entity.User;
import manage.store.inventory.repository.RequestSetRepository;
import manage.store.inventory.repository.UserRepository;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.ExcelExportService;
import manage.store.inventory.service.ReceiptService;
import manage.store.inventory.service.RequestSetService;

@RestController
@RequestMapping("/api/request-sets")
public class RequestSetController {

    private final RequestSetService requestSetService;
    private final ReceiptService receiptService;
    private final RequestSetRepository requestSetRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final ExcelExportService excelExportService;

    public RequestSetController(
            RequestSetService requestSetService,
            ReceiptService receiptService,
            RequestSetRepository requestSetRepository,
            UserRepository userRepository,
            CurrentUser currentUser,
            ExcelExportService excelExportService
    ) {
        this.requestSetService = requestSetService;
        this.receiptService = receiptService;
        this.requestSetRepository = requestSetRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.excelExportService = excelExportService;
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
        return ResponseEntity.status(201).body(setId);
    }

    // Lấy danh sách bộ phiếu (phân quyền theo role)
    // - USER/PURCHASER: chỉ xem của mình
    // - ADMIN: xem tất cả, sắp xếp theo tên người tạo
    // - STOCKKEEPER: xem tất cả
    // Hỗ trợ: ?status=APPROVED hoặc ?status=APPROVED,EXECUTED
    @GetMapping
    public Page<RequestSetListDTO> getAllRequestSets(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = currentUser.getUserId();
        if (status != null && !status.isEmpty()) {
            if (status.contains(",")) {
                List<String> statuses = Arrays.asList(status.split(","));
                return requestSetService.getRequestSetsByStatuses(statuses, userId, pageable);
            }
            return requestSetService.getRequestSetsByStatus(status, userId, pageable);
        }
        return requestSetService.getAllRequestSets(userId, pageable);
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

    // Xóa bộ phiếu (chủ phiếu hoặc ADMIN)
    @DeleteMapping("/{setId:\\d+}")
    @PreAuthorize("hasAnyRole('USER', 'PURCHASER', 'ADMIN')")
    public ResponseEntity<Void> deleteRequestSet(@PathVariable Long setId) {
        requestSetService.deleteRequestSet(setId, currentUser.getUserId());
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

    // Xuất Excel cho bộ phiếu
    @GetMapping("/{setId:\\d+}/export")
    public ResponseEntity<byte[]> exportRequestSet(@PathVariable Long setId) throws IOException {
        byte[] excelData = excelExportService.exportRequestSet(setId);

        // Lấy tên request set thật từ DB
        String setName = requestSetRepository.findById(setId)
                .map(rs -> rs.getSetName())
                .orElse("de-xuat-" + setId);
        // Loại bỏ ký tự không hợp lệ cho filename
        String safeName = setName.replaceAll("[/\\\\?%*:|\"<>]", "-");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(safeName + ".xlsx", java.nio.charset.StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    // =====================================================
    // PARTIAL RECEIPT ENDPOINTS (Case 2 + Case 3)
    // =====================================================

    // Ghi nhận nhận hàng từng phần (STOCKKEEPER)
    // APPROVED → RECEIVING (lần đầu) hoặc giữ RECEIVING
    @PostMapping("/{setId:\\d+}/receive")
    @PreAuthorize("hasRole('STOCKKEEPER')")
    public ResponseEntity<Void> recordReceipt(
            @PathVariable Long setId,
            @Valid @RequestBody ReceiptCreateDTO dto
    ) {
        receiptService.recordReceipt(setId, dto, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    // Hoàn tất nhận hàng (STOCKKEEPER)
    // RECEIVING → EXECUTED
    @PostMapping("/{setId:\\d+}/complete")
    @PreAuthorize("hasRole('STOCKKEEPER')")
    public ResponseEntity<Void> completeReceipt(@PathVariable Long setId) {
        receiptService.completeReceipt(setId, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    // Lấy danh sách các lần nhận hàng của bộ phiếu
    @GetMapping("/{setId:\\d+}/receipts")
    public List<ReceiptDetailDTO> getReceipts(@PathVariable Long setId) {
        return receiptService.getReceipts(setId);
    }

    // Lấy tiến độ nhận hàng (phân cấp: set → requests → items → history)
    @GetMapping("/{setId:\\d+}/progress")
    public SetReceiptProgressDTO getProgress(@PathVariable Long setId) {
        return receiptService.getProgress(setId);
    }

    // Sửa bộ phiếu đã duyệt (chỉ chủ phiếu — STOCKKEEPER dùng "Sửa SL & Nhận hàng")
    // Chỉ khi APPROVED (chưa có receipt)
    // Status → PENDING (cần Admin duyệt lại)
    @PutMapping("/{setId:\\d+}/edit")
    @PreAuthorize("hasAnyRole('USER', 'PURCHASER')")
    public ResponseEntity<Void> editApprovedRequestSet(
            @PathVariable Long setId,
            @Valid @RequestBody RequestSetUpdateDTO dto
    ) {
        requestSetService.editApprovedRequestSet(setId, dto, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    // Sửa số lượng items và chuyển RECEIVING luôn (STOCKKEEPER only)
    // Dùng khi hàng thực tế khác phiếu (nhiều hơn/ít hơn)
    // APPROVED → RECEIVING (không quay về PENDING)
    // Bắt buộc nêu lý do, thông báo Creator + ADMIN
    @PutMapping("/{setId:\\d+}/edit-and-receive")
    @PreAuthorize("hasRole('STOCKKEEPER')")
    public ResponseEntity<Void> editAndReceiveRequestSet(
            @PathVariable Long setId,
            @Valid @RequestBody EditAndReceiveDTO dto
    ) {
        requestSetService.editAndReceiveRequestSet(setId, dto, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }
}
