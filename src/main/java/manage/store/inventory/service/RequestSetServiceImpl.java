package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.ApprovalHistoryDTO;
import manage.store.inventory.dto.InventoryRequestCreateDTO;
import manage.store.inventory.dto.InventoryRequestDetailDTO;
import manage.store.inventory.dto.RequestSetCreateDTO;
import manage.store.inventory.dto.RequestSetDetailDTO;
import manage.store.inventory.dto.RequestSetListDTO;
import manage.store.inventory.dto.RequestSetUpdateDTO;
import manage.store.inventory.entity.ApprovalHistory;
import manage.store.inventory.entity.InventoryRequest;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.ApprovalAction;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.repository.ApprovalHistoryRepository;
import manage.store.inventory.entity.Position;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.InventoryRequestRepository;
import manage.store.inventory.repository.PositionRepository;
import manage.store.inventory.repository.ProductVariantRepository;
import manage.store.inventory.repository.RequestSetRepository;
import manage.store.inventory.repository.UserRepository;

@Service
@Transactional
public class RequestSetServiceImpl implements RequestSetService {

    private final RequestSetRepository requestSetRepository;
    private final InventoryRequestRepository requestRepository;
    private final InventoryRequestItemRepository itemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final InventoryRequestService inventoryRequestService;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final NotificationService notificationService;
    private final InventoryRepository inventoryRepository;
    private final PositionRepository positionRepository;

    public RequestSetServiceImpl(
            RequestSetRepository requestSetRepository,
            InventoryRequestRepository requestRepository,
            InventoryRequestItemRepository itemRepository,
            ProductVariantRepository variantRepository,
            UserRepository userRepository,
            InventoryRequestService inventoryRequestService,
            ApprovalHistoryRepository approvalHistoryRepository,
            NotificationService notificationService,
            InventoryRepository inventoryRepository,
            PositionRepository positionRepository
    ) {
        this.requestSetRepository = requestSetRepository;
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.inventoryRequestService = inventoryRequestService;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.notificationService = notificationService;
        this.inventoryRepository = inventoryRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    public Long createRequestSet(RequestSetCreateDTO dto, Long createdByUserId) {
        // Validate user và quyền tạo
        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + createdByUserId));

        // ADMIN không được tạo request set (trừ khi có role khác như PURCHASER hoặc USER)
        if (creator.isAdmin() && !creator.isPurchaser() && !creator.isUser()) {
            throw new IllegalArgumentException("ADMIN không có quyền tạo bộ phiếu. Chỉ có quyền duyệt/từ chối.");
        }

        // STOCKKEEPER không được tạo
        if (creator.isStockkeeper() && !creator.isPurchaser() && !creator.isUser()) {
            throw new IllegalArgumentException("STOCKKEEPER không có quyền tạo bộ phiếu. Chỉ có quyền xem.");
        }

        // Validate request types theo role
        if (dto.getRequests() != null && !dto.getRequests().isEmpty()) {
            validateRequestTypesForRole(dto.getRequests(), creator);
        }

        // 1. Tạo RequestSet - bắt đầu với PENDING (bỏ DRAFT)
        RequestSet requestSet = new RequestSet();
        requestSet.setSetName(dto.getSetName());
        requestSet.setDescription(dto.getDescription());
        requestSet.setStatus(RequestSetStatus.PENDING);
        requestSet.setCreatedAt(LocalDateTime.now());
        requestSet.setSubmittedAt(LocalDateTime.now());
        requestSet.setCreatedByUser(creator);

        requestSet = requestSetRepository.save(requestSet);

        // 2. Tạo các InventoryRequest trong bộ
        if (dto.getRequests() != null && !dto.getRequests().isEmpty()) {
            for (InventoryRequestCreateDTO requestDTO : dto.getRequests()) {
                createRequestInSet(requestDTO, requestSet.getSetId());
            }
        }

        // Thông báo cho ADMIN về phiếu mới cần duyệt
        notificationService.notifyAdminsOfPendingApproval(requestSet, creator);

        return requestSet.getSetId();
    }

    /**
     * Validate request types theo role của user
     * - USER: Chỉ được IN, OUT (ảnh hưởng tồn kho thực tế)
     * - PURCHASER: Được cả 4 loại (ADJUST_IN/OUT ảnh hưởng dự kiến, IN/OUT ảnh hưởng thực tế)
     *
     * Validate expected_date:
     * - ADJUST_IN, ADJUST_OUT: Bắt buộc có expected_date >= today
     */
    private void validateRequestTypesForRole(List<InventoryRequestCreateDTO> requests, User user) {
        boolean isPurchaser = user.isPurchaser();
        boolean isUser = user.isUser();
        LocalDate today = LocalDate.now();

        for (InventoryRequestCreateDTO request : requests) {
            String requestType = request.getRequestType();
            if (requestType == null) {
                throw new IllegalArgumentException("Request type không được để trống");
            }

            if (isUser && !isPurchaser) {
                // USER chỉ được tạo IN hoặc OUT
                if ("ADJUST_IN".equals(requestType) || "ADJUST_OUT".equals(requestType)) {
                    throw new IllegalArgumentException(
                            "USER chỉ được tạo phiếu loại IN hoặc OUT. " +
                            "Không được phép tạo " + requestType + "."
                    );
                }
                if (!"IN".equals(requestType) && !"OUT".equals(requestType)) {
                    throw new IllegalArgumentException(
                            "Request type không hợp lệ: '" + requestType + "'. " +
                            "USER chỉ được tạo IN hoặc OUT."
                    );
                }
            } else if (isPurchaser) {
                // PURCHASER được tạo cả 4 loại
                if (!"IN".equals(requestType) && !"OUT".equals(requestType)
                        && !"ADJUST_IN".equals(requestType) && !"ADJUST_OUT".equals(requestType)) {
                    throw new IllegalArgumentException(
                            "Request type không hợp lệ: '" + requestType + "'. " +
                            "Chỉ chấp nhận IN, OUT, ADJUST_IN hoặc ADJUST_OUT."
                    );
                }

                // Validate expected_date cho ADJUST_IN, ADJUST_OUT
                if ("ADJUST_IN".equals(requestType) || "ADJUST_OUT".equals(requestType)) {
                    if (request.getExpectedDate() == null) {
                        throw new IllegalArgumentException(
                                "Phiếu " + requestType + " bắt buộc phải có ngày dự kiến (expectedDate)."
                        );
                    }
                    if (request.getExpectedDate().isBefore(today)) {
                        throw new IllegalArgumentException(
                                "Ngày dự kiến phải >= ngày hôm nay (" + today + "). " +
                                "Ngày nhập: " + request.getExpectedDate()
                        );
                    }
                }
            }
        }
    }

    private void createRequestInSet(InventoryRequestCreateDTO dto, Long setId) {
        // Validation đã được thực hiện trong validateRequestTypesForRole
        String requestType = dto.getRequestType();

        // Validate số lượng trước khi tạo request
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            validateQuantityForItems(dto);
        }

        InventoryRequest request = new InventoryRequest();
        request.setUnitId(dto.getUnitId());
        if (dto.getPositionCode() != null && !dto.getPositionCode().isBlank()) {
            Position position = positionRepository.findByPositionCode(dto.getPositionCode())
                    .orElseThrow(() -> new RuntimeException("Position not found: " + dto.getPositionCode()));
            request.setPositionId(position.getPositionId());
        }
        request.setProductId(dto.getProductId());
        request.setRequestType(
                InventoryRequest.RequestType.valueOf(requestType)
        );
        request.setExpectedDate(dto.getExpectedDate());
        request.setNote(dto.getNote());
        request.setCreatedAt(LocalDateTime.now());
        request.setSetId(setId);

        request = requestRepository.save(request);

        if (dto.getItems() == null) {
            return;
        }

        for (InventoryRequestCreateDTO.ItemDTO item : dto.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }

            ProductVariant variant = variantRepository
                    .findVariant(
                            item.getStyleId(),
                            item.getSizeValue(),
                            item.getLengthCode()
                    )
                    .orElseThrow(() -> new RuntimeException(
                            "Variant not found: styleId=" + item.getStyleId()
                                    + ", size=" + item.getSizeValue()
                                    + ", length=" + item.getLengthCode()
                    ));

            InventoryRequestItem requestItem = new InventoryRequestItem();
            requestItem.setRequestId(request.getRequestId());
            requestItem.setVariantId(variant.getVariantId());
            requestItem.setQuantity(item.getQuantity());

            itemRepository.save(requestItem);
        }
    }

    /**
     * Validate số lượng cho từng item trong request
     * - OUT: Số lượng <= Tồn thực tế
     * - ADJUST_OUT: Số lượng <= Tồn dự kiến tại expected_date
     */
    private void validateQuantityForItems(InventoryRequestCreateDTO dto) {
        String requestType = dto.getRequestType();
        Long productId = dto.getProductId();

        for (InventoryRequestCreateDTO.ItemDTO item : dto.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }

            ProductVariant variant = variantRepository
                    .findVariant(
                            item.getStyleId(),
                            item.getSizeValue(),
                            item.getLengthCode()
                    )
                    .orElseThrow(() -> new RuntimeException(
                            "Variant not found: styleId=" + item.getStyleId()
                                    + ", size=" + item.getSizeValue()
                                    + ", length=" + item.getLengthCode()
                    ));

            if ("OUT".equals(requestType)) {
                // OUT: Validate với tồn thực tế
                Integer actualQty = inventoryRepository.getActualQuantityByVariant(productId, variant.getVariantId());
                if (actualQty == null) actualQty = 0;

                if (item.getQuantity() > actualQty) {
                    throw new IllegalArgumentException(
                            "Số lượng xuất (" + item.getQuantity() + ") vượt quá tồn kho thực tế (" + actualQty + ") " +
                            "cho biến thể: size=" + item.getSizeValue() + ", length=" + item.getLengthCode()
                    );
                }
            } else if ("ADJUST_OUT".equals(requestType)) {
                // ADJUST_OUT: Validate với tồn dự kiến tại expected_date
                Integer expectedQty = inventoryRepository.getExpectedQuantityByVariantAtDate(
                        productId, variant.getVariantId(), dto.getExpectedDate()
                );
                if (expectedQty == null) expectedQty = 0;

                if (item.getQuantity() > expectedQty) {
                    throw new IllegalArgumentException(
                            "Số lượng dự kiến xuất (" + item.getQuantity() + ") vượt quá tồn kho dự kiến (" + expectedQty + ") " +
                            "tại ngày " + dto.getExpectedDate() + " " +
                            "cho biến thể: size=" + item.getSizeValue() + ", length=" + item.getLengthCode()
                    );
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestSetListDTO> getAllRequestSets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // USER hoặc PURCHASER (không phải ADMIN/STOCKKEEPER thuần túy): chỉ xem của mình
        if ((user.isUser() || user.isPurchaser()) && !user.isAdmin() && !user.isStockkeeper()) {
            return requestSetRepository.findAllSetsByCreatedBy(userId);
        }

        // ADMIN: xem tất cả, sắp xếp theo tên người tạo
        if (user.isAdmin()) {
            return requestSetRepository.findAllSetsOrderByCreatorName();
        }

        // STOCKKEEPER: xem tất cả (giữ nguyên logic cũ)
        return requestSetRepository.findAllSets();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestSetListDTO> getRequestSetsByStatus(String status, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // USER hoặc PURCHASER: chỉ xem của mình
        if ((user.isUser() || user.isPurchaser()) && !user.isAdmin() && !user.isStockkeeper()) {
            return requestSetRepository.findAllByCreatedByAndStatus(userId, status);
        }

        // ADMIN: sắp xếp theo tên người tạo
        if (user.isAdmin()) {
            return requestSetRepository.findAllByStatusOrderByCreatorName(status);
        }

        // STOCKKEEPER: giữ nguyên
        return requestSetRepository.findAllByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestSetListDTO> getRequestSetsByStatuses(List<String> statuses, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // USER hoặc PURCHASER: chỉ xem của mình
        if ((user.isUser() || user.isPurchaser()) && !user.isAdmin() && !user.isStockkeeper()) {
            return requestSetRepository.findAllByCreatedByAndStatuses(userId, statuses);
        }

        // ADMIN: sắp xếp theo tên người tạo
        if (user.isAdmin()) {
            return requestSetRepository.findAllByStatusesOrderByCreatorName(statuses);
        }

        // STOCKKEEPER: giữ nguyên
        return requestSetRepository.findAllByStatuses(statuses);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestSetDetailDTO getRequestSetDetail(Long setId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        // Lấy danh sách request IDs thuộc set này
        List<InventoryRequest> requests = requestRepository.findBySetId(setId);

        List<InventoryRequestDetailDTO> requestDetails = new ArrayList<>();
        for (InventoryRequest request : requests) {
            InventoryRequestDetailDTO detail = inventoryRequestService
                    .getRequestDetail(request.getRequestId());
            requestDetails.add(detail);
        }

        // Lấy thông tin người tạo
        Long createdBy = null;
        String createdByName = null;
        if (requestSet.getCreatedByUser() != null) {
            createdBy = requestSet.getCreatedByUser().getUserId();
            createdByName = requestSet.getCreatedByUser().getFullName();
        }

        // Lấy lịch sử duyệt
        List<ApprovalHistoryDTO> historyDTOs = approvalHistoryRepository
                .findByRequestSetSetIdOrderByCreatedAtDesc(setId)
                .stream()
                .map(h -> new ApprovalHistoryDTO(
                        h.getHistoryId(),
                        h.getAction().name(),
                        h.getPerformedBy().getUserId(),
                        h.getPerformedBy().getFullName(),
                        h.getReason(),
                        h.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new RequestSetDetailDTO(
                requestSet.getSetId(),
                requestSet.getSetName(),
                requestSet.getDescription(),
                requestSet.getStatus().name(),
                createdBy,
                createdByName,
                requestSet.getCreatedAt(),
                requestSet.getSubmittedAt(),
                requestDetails,
                historyDTOs
        );
    }

    // =====================================================
    // UPDATE REQUEST SET - Cập nhật bộ phiếu bị từ chối
    // Sau khi cập nhật tự động gửi duyệt lại (PENDING)
    // =====================================================
    @Override
    public void updateRequestSet(Long setId, RequestSetUpdateDTO dto, Long userId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Kiểm tra quyền: chỉ chủ phiếu mới được cập nhật
        if (requestSet.getCreatedByUser() == null ||
                !requestSet.getCreatedByUser().getUserId().equals(userId)) {
            throw new RuntimeException("Chỉ chủ phiếu mới có quyền cập nhật bộ phiếu");
        }

        // Kiểm tra trạng thái: chỉ REJECTED mới được cập nhật
        if (requestSet.getStatus() != RequestSetStatus.REJECTED) {
            throw new RuntimeException("Chỉ có thể cập nhật bộ phiếu đã bị từ chối (REJECTED)");
        }

        // Validate request types theo role
        if (dto.getRequests() != null && !dto.getRequests().isEmpty()) {
            validateRequestTypesForRole(dto.getRequests(), user);
        }

        // 1. Xóa tất cả requests cũ và items của chúng
        List<InventoryRequest> oldRequests = requestRepository.findBySetId(setId);
        for (InventoryRequest oldRequest : oldRequests) {
            itemRepository.deleteByRequestId(oldRequest.getRequestId());
        }
        requestRepository.deleteAll(oldRequests);

        // 2. Cập nhật thông tin bộ phiếu
        requestSet.setSetName(dto.getSetName());
        requestSet.setDescription(dto.getDescription());
        requestSet.setStatus(RequestSetStatus.PENDING);
        requestSet.setSubmittedAt(LocalDateTime.now());
        requestSetRepository.save(requestSet);

        // 3. Tạo các requests mới
        if (dto.getRequests() != null && !dto.getRequests().isEmpty()) {
            for (InventoryRequestCreateDTO requestDTO : dto.getRequests()) {
                createRequestInSet(requestDTO, setId);
            }
        }

        // 4. Lưu lịch sử (SUBMIT sau khi UPDATE)
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.SUBMIT);
        history.setPerformedBy(user);
        history.setReason("Cập nhật và gửi duyệt lại");
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        // 5. Thông báo cho ADMIN
        notificationService.notifyAdminsOfPendingApproval(requestSet, user);
    }

    @Override
    public void deleteRequestSet(Long setId) {
        // Lấy tất cả requests trong set
        List<InventoryRequest> requests = requestRepository.findBySetId(setId);

        // Xóa items của từng request
        for (InventoryRequest request : requests) {
            itemRepository.deleteByRequestId(request.getRequestId());
        }

        // Xóa các requests
        requestRepository.deleteAll(requests);

        // Xóa set
        requestSetRepository.deleteById(setId);
    }

    @Override
    public void deleteAllRequestSets() {
        // Lấy tất cả sets
        List<RequestSet> allSets = requestSetRepository.findAll();

        for (RequestSet set : allSets) {
            // Lấy tất cả requests trong set
            List<InventoryRequest> requests = requestRepository.findBySetId(set.getSetId());

            // Xóa items của từng request
            for (InventoryRequest request : requests) {
                itemRepository.deleteByRequestId(request.getRequestId());
            }

            // Xóa các requests
            requestRepository.deleteAll(requests);
        }

        // Xóa tất cả sets
        requestSetRepository.deleteAll();
    }

    @Override
    public void submitForApproval(Long setId, Long userId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Chỉ REJECTED mới được submit lại (vì tạo mới đã là PENDING)
        if (requestSet.getStatus() != RequestSetStatus.REJECTED) {
            throw new RuntimeException("Chỉ có thể submit lại bộ phiếu đã bị từ chối (REJECTED)");
        }

        // Cập nhật trạng thái
        requestSet.setStatus(RequestSetStatus.PENDING);
        requestSet.setSubmittedAt(LocalDateTime.now());
        requestSetRepository.save(requestSet);

        // Lưu lịch sử
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.SUBMIT);
        history.setPerformedBy(user);
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        // Thông báo cho ADMIN
        notificationService.notifyAdminsOfPendingApproval(requestSet, user);
    }

    @Override
    public void approve(Long setId, Long userId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Kiểm tra quyền ADMIN
        if (!user.isAdmin()) {
            throw new RuntimeException("Chỉ ADMIN mới có quyền duyệt bộ phiếu");
        }

        // Kiểm tra người duyệt không phải người tạo
        if (requestSet.getCreatedByUser() != null
                && requestSet.getCreatedByUser().getUserId().equals(userId)) {
            throw new RuntimeException("Không thể tự duyệt bộ phiếu của chính mình");
        }

        // Kiểm tra trạng thái
        if (requestSet.getStatus() != RequestSetStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt bộ phiếu đang chờ duyệt (PENDING)");
        }

        // Cập nhật trạng thái
        requestSet.setStatus(RequestSetStatus.APPROVED);
        requestSetRepository.save(requestSet);

        // Lưu lịch sử
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.APPROVE);
        history.setPerformedBy(user);
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        // Thông báo cho người tạo
        notificationService.notifyCreatorOfApproval(requestSet, user);

        // Thông báo cho STOCKKEEPER để thực hiện nhập/xuất kho
        notificationService.notifyStockkeeperOfApproval(requestSet, user);
    }

    @Override
    public void reject(Long setId, Long userId, String reason) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Kiểm tra quyền ADMIN
        if (!user.isAdmin()) {
            throw new RuntimeException("Chỉ ADMIN mới có quyền từ chối bộ phiếu");
        }

        // Kiểm tra trạng thái
        if (requestSet.getStatus() != RequestSetStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể từ chối bộ phiếu đang chờ duyệt (PENDING)");
        }

        // Kiểm tra lý do từ chối
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Phải có lý do khi từ chối bộ phiếu");
        }

        // Cập nhật trạng thái
        requestSet.setStatus(RequestSetStatus.REJECTED);
        requestSetRepository.save(requestSet);

        // Lưu lịch sử
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.REJECT);
        history.setPerformedBy(user);
        history.setReason(reason);
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        // Thông báo cho người tạo
        notificationService.notifyCreatorOfRejection(requestSet, user, reason);
    }

    // =====================================================
    // EXECUTE - STOCKKEEPER xác nhận đã thực hiện nhập/xuất kho
    // Tự động chuyển đổi: ADJUST_IN → IN, ADJUST_OUT → OUT
    // =====================================================
    @Override
    public void execute(Long setId, Long userId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Kiểm tra quyền STOCKKEEPER
        if (!user.isStockkeeper()) {
            throw new RuntimeException("Chỉ STOCKKEEPER mới có quyền xác nhận thực hiện bộ phiếu");
        }

        // Kiểm tra trạng thái - chỉ APPROVED mới được execute
        if (requestSet.getStatus() != RequestSetStatus.APPROVED) {
            throw new RuntimeException("Chỉ có thể xác nhận thực hiện bộ phiếu đã được duyệt (APPROVED)");
        }

        // Chuyển đổi ADJUST_IN → IN, ADJUST_OUT → OUT cho tất cả requests trong set
        List<InventoryRequest> requests = requestRepository.findBySetId(setId);
        for (InventoryRequest request : requests) {
            InventoryRequest.RequestType currentType = request.getRequestType();
            if (currentType == InventoryRequest.RequestType.ADJUST_IN) {
                request.setRequestType(InventoryRequest.RequestType.IN);
                requestRepository.save(request);
            } else if (currentType == InventoryRequest.RequestType.ADJUST_OUT) {
                request.setRequestType(InventoryRequest.RequestType.OUT);
                requestRepository.save(request);
            }
        }

        // Cập nhật trạng thái
        requestSet.setStatus(RequestSetStatus.EXECUTED);
        requestSet.setExecutedByUser(user);
        requestSet.setExecutedAt(LocalDateTime.now());
        requestSetRepository.save(requestSet);

        // Lưu lịch sử
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.EXECUTE);
        history.setPerformedBy(user);
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        // Thông báo cho người tạo rằng phiếu đã được thực hiện
        notificationService.notifyCreatorOfExecution(requestSet, user);
    }

    // =====================================================
    // GET APPROVED REQUEST SETS - Danh sách chờ STOCKKEEPER thực hiện
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<RequestSetListDTO> getApprovedRequestSets() {
        return requestSetRepository.findByStatus("APPROVED");
    }
}
