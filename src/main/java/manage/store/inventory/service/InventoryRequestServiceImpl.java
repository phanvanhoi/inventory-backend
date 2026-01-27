package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.InventoryRequestCreateDTO;
import manage.store.inventory.dto.InventoryRequestDetailDTO;
import manage.store.inventory.dto.InventoryRequestHeaderDTO;
import manage.store.inventory.dto.InventoryRequestItemDTO;
import manage.store.inventory.dto.InventoryRequestListDTO;
import manage.store.inventory.entity.InventoryRequest;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.InventoryRequestRepository;
import manage.store.inventory.repository.ProductVariantRepository;
import manage.store.inventory.repository.RequestSetRepository;

@Service
@Transactional
public class InventoryRequestServiceImpl implements InventoryRequestService {

    private final InventoryRequestRepository requestRepository;
    private final InventoryRequestItemRepository itemRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final RequestSetRepository requestSetRepository;

    public InventoryRequestServiceImpl(
            InventoryRequestRepository requestRepository,
            InventoryRequestItemRepository itemRepository,
            ProductVariantRepository variantRepository,
            InventoryRepository inventoryRepository,
            RequestSetRepository requestSetRepository
    ) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
        this.requestSetRepository = requestSetRepository;
    }

    // =====================================================
    // CREATE REQUEST
    // =====================================================
    @Override
    public Long createRequest(InventoryRequestCreateDTO dto) {

        InventoryRequest request = new InventoryRequest();
        request.setUnitId(dto.getUnitId());
        request.setProductId(dto.getProductId());
        request.setRequestType(
                InventoryRequest.RequestType.valueOf(dto.getRequestType())
        );
        request.setNote(dto.getNote());
        request.setCreatedAt(LocalDateTime.now());

        request = requestRepository.save(request);

        if (dto.getItems() == null) {
            return request.getRequestId();
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
                    .orElseThrow(()
                            -> new RuntimeException(
                            "Variant not found: styleId="
                            + item.getStyleId()
                            + ", size=" + item.getSizeValue()
                            + ", length=" + item.getLengthCode()
                    )
                    );

            InventoryRequestItem requestItem = new InventoryRequestItem();
            requestItem.setRequestId(request.getRequestId());
            requestItem.setVariantId(variant.getVariantId());
            requestItem.setQuantity(item.getQuantity());

            itemRepository.save(requestItem);
        }

        return request.getRequestId();
    }

    // =====================================================
    // GET ITEMS ONLY
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<InventoryRequestItemDTO> getRequestItems(Long requestId) {
        return itemRepository.findItemsByRequestId(requestId);
    }

    // =====================================================
    // GET HEADER + ITEMS (CHO UI)
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public InventoryRequestDetailDTO getRequestDetail(Long requestId) {

        InventoryRequestHeaderDTO header
                = requestRepository.findHeaderByRequestId(requestId)
                        .orElseThrow(()
                                -> new RuntimeException("Request not found: " + requestId)
                        );

        List<InventoryRequestItemDTO> items
                = itemRepository.findItemsByRequestId(requestId);

        return new InventoryRequestDetailDTO(header, items);
    }

    // =====================================================
    // GET ALL REQUESTS
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<InventoryRequestListDTO> getAllRequests() {
        return requestRepository.findAllRequests();
    }

    // =====================================================
    // DELETE REQUEST
    // =====================================================
    @Override
    public void deleteRequest(Long requestId) {
        if (!requestRepository.existsById(requestId)) {
            throw new RuntimeException("Request not found: " + requestId);
        }
        // Xóa items trước
        itemRepository.deleteByRequestId(requestId);
        // Xóa request
        requestRepository.deleteById(requestId);
    }

    // =====================================================
    // DELETE ALL REQUESTS
    // =====================================================
    @Override
    public void deleteAllRequests() {
        // Xóa tất cả items
        itemRepository.deleteAll();
        // Xóa tất cả requests
        requestRepository.deleteAll();
    }

    // =====================================================
    // UPDATE REQUEST TYPE
    // ADJUST_IN -> IN, ADJUST_OUT -> OUT
    // =====================================================
    @Override
    public void updateRequestType(Long requestId, String newRequestType) {
        InventoryRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        InventoryRequest.RequestType currentType = request.getRequestType();
        InventoryRequest.RequestType targetType;

        // Validate newRequestType
        try {
            targetType = InventoryRequest.RequestType.valueOf(newRequestType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Request type không hợp lệ: '" + newRequestType + "'. " +
                    "Chỉ chấp nhận IN hoặc OUT."
            );
        }

        // Validate transition rules:
        // - ADJUST_IN chỉ có thể chuyển thành IN
        // - ADJUST_OUT chỉ có thể chuyển thành OUT
        if (currentType == InventoryRequest.RequestType.ADJUST_IN) {
            if (targetType != InventoryRequest.RequestType.IN) {
                throw new IllegalArgumentException(
                        "Phiếu ADJUST_IN chỉ có thể chuyển thành IN, không thể chuyển thành " + newRequestType
                );
            }
        } else if (currentType == InventoryRequest.RequestType.ADJUST_OUT) {
            if (targetType != InventoryRequest.RequestType.OUT) {
                throw new IllegalArgumentException(
                        "Phiếu ADJUST_OUT chỉ có thể chuyển thành OUT, không thể chuyển thành " + newRequestType
                );
            }
        } else {
            // IN hoặc OUT không được phép update
            throw new IllegalArgumentException(
                    "Phiếu đã ở trạng thái " + currentType.name() + ", không thể cập nhật request type nữa."
            );
        }

        // Update request type
        request.setRequestType(targetType);
        requestRepository.save(request);
    }

    // =====================================================
    // COUNT DEPENDENT ADJUST_OUT
    // Đếm số ADJUST_OUT phụ thuộc vào ADJUST_IN này
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public int countDependentAdjustOut(Long requestId) {
        InventoryRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // Chỉ áp dụng cho ADJUST_IN
        if (request.getRequestType() != InventoryRequest.RequestType.ADJUST_IN) {
            return 0;
        }

        if (request.getExpectedDate() == null) {
            return 0;
        }

        Integer count = inventoryRepository.countDependentAdjustOut(
                request.getProductId(),
                request.getExpectedDate(),
                requestId
        );
        return count != null ? count : 0;
    }

    // =====================================================
    // UPDATE EXPECTED DATE
    // Cập nhật ngày dự kiến cho ADJUST_IN hoặc ADJUST_OUT
    // =====================================================
    @Override
    public void updateExpectedDate(Long requestId, LocalDate newExpectedDate, Long userId) {
        InventoryRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // 1. Validate: Chỉ ADJUST_IN và ADJUST_OUT mới có expected_date
        InventoryRequest.RequestType requestType = request.getRequestType();
        if (requestType != InventoryRequest.RequestType.ADJUST_IN &&
            requestType != InventoryRequest.RequestType.ADJUST_OUT) {
            throw new IllegalArgumentException(
                    "Chỉ có thể cập nhật ngày dự kiến cho phiếu ADJUST_IN hoặc ADJUST_OUT"
            );
        }

        // 2. Validate: Ngày mới phải >= hôm nay
        if (newExpectedDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày dự kiến mới phải >= hôm nay (" + LocalDate.now() + ")"
            );
        }

        // 3. Validate: Request set phải ở trạng thái PENDING hoặc APPROVED
        if (request.getSetId() != null) {
            RequestSet requestSet = requestSetRepository.findById(request.getSetId())
                    .orElseThrow(() -> new RuntimeException("Request set not found: " + request.getSetId()));

            if (requestSet.getStatus() == RequestSetStatus.REJECTED) {
                throw new IllegalArgumentException(
                        "Không thể cập nhật ngày dự kiến cho phiếu đã bị từ chối"
                );
            }
        }

        // 4. Validate đặc biệt cho ADJUST_IN:
        // Nếu dời sang ngày MỚI (xa hơn), phải kiểm tra không có ADJUST_OUT phụ thuộc
        if (requestType == InventoryRequest.RequestType.ADJUST_IN) {
            LocalDate currentExpectedDate = request.getExpectedDate();

            // Nếu dời sang ngày xa hơn (newDate > currentDate)
            if (currentExpectedDate != null && newExpectedDate.isAfter(currentExpectedDate)) {
                // Kiểm tra có ADJUST_OUT nào có expected_date trong khoảng [currentDate, newDate)
                Integer dependentCount = inventoryRepository.countDependentAdjustOut(
                        request.getProductId(),
                        currentExpectedDate,
                        requestId
                );

                if (dependentCount != null && dependentCount > 0) {
                    throw new IllegalArgumentException(
                            "Không thể dời ADJUST_IN sang ngày " + newExpectedDate +
                            " vì có " + dependentCount + " phiếu ADJUST_OUT phụ thuộc. " +
                            "Vui lòng dời các phiếu ADJUST_OUT trước."
                    );
                }
            }
        }

        // 5. Cập nhật expected_date
        request.setExpectedDate(newExpectedDate);
        requestRepository.save(request);

        // 6. Nếu request set đã được APPROVED, chuyển về PENDING để duyệt lại
        if (request.getSetId() != null) {
            RequestSet requestSet = requestSetRepository.findById(request.getSetId()).orElse(null);
            if (requestSet != null && requestSet.getStatus() == RequestSetStatus.APPROVED) {
                requestSet.setStatus(RequestSetStatus.PENDING);
                requestSetRepository.save(requestSet);
            }
        }
    }
}
