package manage.store.inventory.service;

import java.math.BigDecimal;
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
import manage.store.inventory.dto.RequestCompleteResponseDTO;
import manage.store.inventory.dto.RequestSetCreateDTO;
import manage.store.inventory.dto.RequestSetDetailDTO;
import manage.store.inventory.dto.RequestSetListDTO;
import manage.store.inventory.dto.EditAndReceiveDTO;
import manage.store.inventory.dto.RequestSetUpdateDTO;
import manage.store.inventory.entity.ApprovalHistory;
import manage.store.inventory.entity.InventoryRequest;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.ApprovalAction;
import manage.store.inventory.entity.enums.Gender;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.entity.enums.VariantType;
import manage.store.inventory.repository.ApprovalHistoryRepository;
import manage.store.inventory.entity.Position;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.InventoryRequestRepository;
import manage.store.inventory.repository.PositionRepository;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.repository.ProductVariantRepository;
import manage.store.inventory.repository.RequestSetRepository;
import manage.store.inventory.repository.UserRepository;
import manage.store.inventory.repository.WarehouseRepository;

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
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

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
            PositionRepository positionRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository
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
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
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
        if (dto.getPositionId() != null) {
            request.setPositionId(dto.getPositionId());
        }
        request.setProductId(dto.getProductId());
        request.setRequestType(
                InventoryRequest.RequestType.valueOf(requestType)
        );
        request.setExpectedDate(dto.getExpectedDate());
        request.setNote(dto.getNote());
        request.setFabricMetadata(dto.getFabricMetadata());
        request.setRequestStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        request.setSetId(setId);

        // Set warehouse (null = default warehouse)
        if (dto.getWarehouseId() != null) {
            request.setWarehouseId(dto.getWarehouseId());
        } else {
            request.setWarehouseId(warehouseRepository.findByIsDefaultTrue()
                    .orElseThrow(() -> new RuntimeException("Default warehouse not found"))
                    .getWarehouseId());
        }

        request = requestRepository.save(request);

        if (dto.getItems() == null) {
            return;
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductId()));

        for (InventoryRequestCreateDTO.ItemDTO item : dto.getItems()) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            ProductVariant variant = resolveVariant(product, item);

            InventoryRequestItem requestItem = new InventoryRequestItem();
            requestItem.setRequestId(request.getRequestId());
            requestItem.setVariantId(variant.getVariantId());
            requestItem.setQuantity(item.getQuantity());
            requestItem.setWorkerNote(item.getWorkerNote());
            requestItem.setFabricNote(item.getFabricNote());
            requestItem.setEmployeeId(item.getEmployeeId());
            requestItem.setGarmentQuantity(item.getGarmentQuantity());

            itemRepository.save(requestItem);
        }
    }

    /**
     * Resolve variant dựa trên variant_type của product
     */
    private ProductVariant resolveVariant(Product product, InventoryRequestCreateDTO.ItemDTO item) {
        if (product.getVariantType() == VariantType.ITEM_BASED || item.getVariantId() != null) {
            Long variantId = item.getVariantId();
            if (variantId == null) {
                throw new RuntimeException("ITEM_BASED product yêu cầu variantId");
            }
            return variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Variant not found: id=" + variantId));
        }
        if (item.getStyleId() != null) {
            return variantRepository
                    .findStructuredVariantWithStyle(product.getProductId(), item.getStyleId(), item.getSizeValue(), item.getLengthCode())
                    .orElseThrow(() -> new RuntimeException("Variant not found: productId=" + product.getProductId()
                            + ", styleId=" + item.getStyleId() + ", size=" + item.getSizeValue() + ", length=" + item.getLengthCode()));
        }
        Gender gender = item.getGender() != null ? Gender.valueOf(item.getGender()) : null;
        if (item.getLengthCode() != null && gender != null) {
            return variantRepository
                    .findStructuredVariantWithGenderAndLength(product.getProductId(), item.getSizeValue(), item.getLengthCode(), gender)
                    .orElseThrow(() -> new RuntimeException("Variant not found: productId=" + product.getProductId()
                            + ", size=" + item.getSizeValue() + ", length=" + item.getLengthCode() + ", gender=" + item.getGender()));
        }
        if (gender != null) {
            return variantRepository
                    .findStructuredVariantWithGender(product.getProductId(), item.getSizeValue(), gender)
                    .orElseThrow(() -> new RuntimeException("Variant not found: productId=" + product.getProductId()
                            + ", size=" + item.getSizeValue() + ", gender=" + item.getGender()));
        }
        // STRUCTURED: chỉ size (Giày BH, Bộ áo mưa)
        if (item.getSizeValue() != null) {
            return variantRepository
                    .findStructuredVariantWithSizeOnly(product.getProductId(), item.getSizeValue())
                    .orElseThrow(() -> new RuntimeException("Variant not found: productId=" + product.getProductId()
                            + ", size=" + item.getSizeValue()));
        }
        throw new RuntimeException("Không thể xác định variant cho product: " + product.getProductId());
    }

    /**
     * Validate số lượng cho từng item trong request
     * - OUT: Số lượng <= Tồn thực tế
     * - ADJUST_OUT: Số lượng <= Tồn dự kiến tại expected_date
     */
    private void validateQuantityForItems(InventoryRequestCreateDTO dto) {
        String requestType = dto.getRequestType();
        Long productId = dto.getProductId();

        // Resolve warehouse cho validate
        Long warehouseId = dto.getWarehouseId();
        if (warehouseId == null) {
            warehouseId = warehouseRepository.findByIsDefaultTrue()
                    .orElseThrow(() -> new RuntimeException("Default warehouse not found"))
                    .getWarehouseId();
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        for (InventoryRequestCreateDTO.ItemDTO item : dto.getItems()) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            ProductVariant variant = resolveVariant(product, item);

            if ("OUT".equals(requestType)) {
                // OUT: Validate với tồn thực tế của kho cụ thể
                BigDecimal actualQty = inventoryRepository.getActualQuantityByVariantAndWarehouse(
                        productId, variant.getVariantId(), warehouseId);
                if (actualQty == null) actualQty = BigDecimal.ZERO;

                if (item.getQuantity().compareTo(actualQty) > 0) {
                    throw new IllegalArgumentException(
                            "Số lượng xuất (" + item.getQuantity() + ") vượt quá tồn kho thực tế (" + actualQty + ") " +
                            "cho biến thể: size=" + item.getSizeValue() + ", length=" + item.getLengthCode()
                    );
                }
            } else if ("ADJUST_OUT".equals(requestType)) {
                // ADJUST_OUT: Validate với tồn dự kiến tại expected_date của kho cụ thể
                BigDecimal expectedQty = inventoryRepository.getExpectedQuantityByVariantAtDateAndWarehouse(
                        productId, variant.getVariantId(), dto.getExpectedDate(), warehouseId
                );
                if (expectedQty == null) expectedQty = BigDecimal.ZERO;

                if (item.getQuantity().compareTo(expectedQty) > 0) {
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

        // Validate tồn kho thực tế trước khi chuyển ADJUST_OUT → OUT
        List<InventoryRequest> requests = requestRepository.findBySetId(setId);
        for (InventoryRequest request : requests) {
            if (request.getRequestType() == InventoryRequest.RequestType.ADJUST_OUT) {
                List<InventoryRequestItem> items = itemRepository.findByRequestId(request.getRequestId());
                for (InventoryRequestItem item : items) {
                    BigDecimal actualQty = inventoryRepository.getActualQuantityByVariantAndWarehouse(
                            request.getProductId(), item.getVariantId(), request.getWarehouseId());
                    if (actualQty == null) actualQty = BigDecimal.ZERO;
                    if (item.getQuantity().compareTo(actualQty) > 0) {
                        throw new RuntimeException(
                                "Không thể xuất kho: số lượng xuất (" + item.getQuantity() +
                                ") vượt quá tồn kho thực tế (" + actualQty + "). " +
                                "Hãy chờ hàng nhập kho thực tế trước khi xuất.");
                    }
                }
            }
        }

        // Chuyển đổi ADJUST_IN → IN, ADJUST_OUT → OUT
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
    // Bao gồm cả APPROVED và RECEIVING
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<RequestSetListDTO> getApprovedRequestSets() {
        return requestSetRepository.findAllByStatuses(List.of("APPROVED", "RECEIVING"));
    }

    // =====================================================
    // EDIT APPROVED REQUEST SET - Sửa phiếu đã duyệt (Case 2)
    // STOCKKEEPER hoặc chủ phiếu sửa → PENDING (cần Admin duyệt lại)
    // Chỉ khi APPROVED (chưa có receipt nào)
    // =====================================================
    @Override
    public void editApprovedRequestSet(Long setId, RequestSetUpdateDTO dto, Long userId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Kiểm tra quyền: STOCKKEEPER hoặc chủ phiếu
        boolean isOwner = requestSet.getCreatedByUser() != null
                && requestSet.getCreatedByUser().getUserId().equals(userId);
        boolean isStockkeeper = user.isStockkeeper();

        if (!isOwner && !isStockkeeper) {
            throw new RuntimeException("Chỉ STOCKKEEPER hoặc chủ phiếu mới có quyền sửa bộ phiếu đã duyệt");
        }

        // Kiểm tra trạng thái: chỉ APPROVED
        if (requestSet.getStatus() != RequestSetStatus.APPROVED) {
            throw new RuntimeException(
                    "Chỉ có thể sửa bộ phiếu đã duyệt (APPROVED). " +
                    "Bộ phiếu đang nhận hàng (RECEIVING) không thể sửa.");
        }

        // Validate request types theo role (nếu người sửa là chủ phiếu)
        if (isOwner && dto.getRequests() != null && !dto.getRequests().isEmpty()) {
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

        // 4. Lưu lịch sử (EDIT)
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.EDIT);
        history.setPerformedBy(user);
        history.setReason("Sửa bộ phiếu đã duyệt và gửi duyệt lại");
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        // 5. Thông báo cho ADMIN duyệt lại
        notificationService.notifyAdminsOfPendingApproval(requestSet, user);
    }

    // =====================================================
    // EDIT AND RECEIVE - Sửa số lượng và chuyển RECEIVING (Case 4)
    // STOCKKEEPER sửa quantity items → APPROVED → RECEIVING
    // Không quay về PENDING, thông báo Creator + ADMIN
    // =====================================================
    @Override
    public void editAndReceiveRequestSet(Long setId, EditAndReceiveDTO dto, Long userId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 1. Chỉ STOCKKEEPER
        if (!user.isStockkeeper()) {
            throw new RuntimeException("Chỉ STOCKKEEPER mới có quyền sửa số lượng và nhận hàng");
        }

        // 2. Chỉ khi APPROVED
        if (requestSet.getStatus() != RequestSetStatus.APPROVED) {
            throw new RuntimeException(
                    "Chỉ có thể sửa số lượng khi bộ phiếu đã duyệt (APPROVED). " +
                    "Trạng thái hiện tại: " + requestSet.getStatus());
        }

        // 3. Cập nhật quantity cho từng item
        for (EditAndReceiveDTO.ItemQuantityUpdate itemUpdate : dto.getItems()) {
            InventoryRequestItem item = itemRepository.findById(itemUpdate.getItemId())
                    .orElseThrow(() -> new RuntimeException(
                            "Item not found: " + itemUpdate.getItemId()));

            // Kiểm tra item thuộc request set này
            InventoryRequest request = requestRepository.findById(item.getRequestId())
                    .orElseThrow(() -> new RuntimeException(
                            "Request not found: " + item.getRequestId()));
            if (!request.getSetId().equals(setId)) {
                throw new RuntimeException(
                        "Item " + itemUpdate.getItemId() + " không thuộc bộ phiếu này");
            }

            if (itemUpdate.getQuantity() == null || itemUpdate.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Số lượng không hợp lệ cho item " + itemUpdate.getItemId());
            }

            item.setQuantity(itemUpdate.getQuantity());
            itemRepository.save(item);
        }

        // 4. Chuyển status → RECEIVING
        requestSet.setStatus(RequestSetStatus.RECEIVING);
        requestSetRepository.save(requestSet);

        // 5. Lưu lịch sử (EDIT_AND_RECEIVE)
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.EDIT_AND_RECEIVE);
        history.setPerformedBy(user);
        history.setReason(dto.getReason());
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        // 6. Thông báo cho Creator + tất cả ADMIN
        notificationService.notifyOfEditAndReceive(requestSet, user, dto.getReason());
    }

    // =====================================================
    // COMPLETE REQUEST (Multi-warehouse)
    // Thủ kho đánh dấu kho của mình đã hoàn thành
    // =====================================================
    @Override
    public RequestCompleteResponseDTO completeRequest(Long requestId, Long userId) {
        InventoryRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (!user.isStockkeeper()) {
            throw new RuntimeException("Chỉ STOCKKEEPER mới có quyền đánh dấu hoàn thành");
        }

        if (request.getSetId() == null) {
            throw new RuntimeException("Request không thuộc bộ phiếu nào");
        }

        RequestSet requestSet = requestSetRepository.findById(request.getSetId())
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + request.getSetId()));

        // Set phải APPROVED hoặc RECEIVING
        if (requestSet.getStatus() != RequestSetStatus.APPROVED
                && requestSet.getStatus() != RequestSetStatus.RECEIVING) {
            throw new RuntimeException("Bộ phiếu phải ở trạng thái APPROVED hoặc RECEIVING");
        }

        // Request chưa COMPLETED
        if ("COMPLETED".equals(request.getRequestStatus())) {
            throw new RuntimeException("Request này đã hoàn thành rồi");
        }

        // Validate tồn kho nếu là ADJUST_OUT
        if (request.getRequestType() == InventoryRequest.RequestType.ADJUST_OUT) {
            List<InventoryRequestItem> items = itemRepository.findByRequestId(requestId);
            for (InventoryRequestItem item : items) {
                BigDecimal actualQty = inventoryRepository.getActualQuantityByVariantAndWarehouse(
                        request.getProductId(), item.getVariantId(), request.getWarehouseId());
                if (actualQty == null) actualQty = BigDecimal.ZERO;
                if (item.getQuantity().compareTo(actualQty) > 0) {
                    throw new RuntimeException(
                            "Không thể xuất kho: số lượng xuất (" + item.getQuantity() +
                            ") vượt quá tồn kho thực tế (" + actualQty + ")");
                }
            }
        }

        // Chuyển ADJUST_IN → IN, ADJUST_OUT → OUT cho request này
        if (request.getRequestType() == InventoryRequest.RequestType.ADJUST_IN) {
            request.setRequestType(InventoryRequest.RequestType.IN);
        } else if (request.getRequestType() == InventoryRequest.RequestType.ADJUST_OUT) {
            request.setRequestType(InventoryRequest.RequestType.OUT);
        }

        // Đánh dấu request COMPLETED
        request.setRequestStatus("COMPLETED");
        requestRepository.save(request);

        // Kiểm tra tất cả requests trong set
        List<InventoryRequest> allRequests = requestRepository.findBySetId(request.getSetId());
        int totalCount = allRequests.size();
        int completedCount = (int) allRequests.stream()
                .filter(r -> "COMPLETED".equals(r.getRequestStatus()))
                .count();

        // Cập nhật set status
        if (completedCount == totalCount) {
            // Tất cả hoàn thành → EXECUTED
            requestSet.setStatus(RequestSetStatus.EXECUTED);
            requestSet.setExecutedByUser(user);
            requestSet.setExecutedAt(LocalDateTime.now());

            // Log history
            ApprovalHistory history = new ApprovalHistory();
            history.setRequestSet(requestSet);
            history.setAction(ApprovalAction.EXECUTE);
            history.setPerformedBy(user);
            history.setCreatedAt(LocalDateTime.now());
            approvalHistoryRepository.save(history);

            notificationService.notifyCreatorOfExecution(requestSet, user);
        } else if (requestSet.getStatus() == RequestSetStatus.APPROVED) {
            // Kho đầu tiên hoàn thành → RECEIVING
            requestSet.setStatus(RequestSetStatus.RECEIVING);

            ApprovalHistory history = new ApprovalHistory();
            history.setRequestSet(requestSet);
            history.setAction(ApprovalAction.RECEIVE);
            history.setPerformedBy(user);
            history.setCreatedAt(LocalDateTime.now());
            approvalHistoryRepository.save(history);
        }
        requestSetRepository.save(requestSet);

        return new RequestCompleteResponseDTO(
                requestId,
                request.getRequestStatus(),
                requestSet.getStatus().name(),
                completedCount,
                totalCount
        );
    }
}
