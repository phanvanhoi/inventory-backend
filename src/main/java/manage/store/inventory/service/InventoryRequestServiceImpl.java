package manage.store.inventory.service;

import java.math.BigDecimal;
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
import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.enums.Gender;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.entity.enums.VariantType;
import manage.store.inventory.entity.Position;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.InventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.InventoryRequestRepository;
import manage.store.inventory.repository.PositionRepository;
import manage.store.inventory.repository.ProductRepository;
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
    private final PositionRepository positionRepository;
    private final ProductRepository productRepository;

    public InventoryRequestServiceImpl(
            InventoryRequestRepository requestRepository,
            InventoryRequestItemRepository itemRepository,
            ProductVariantRepository variantRepository,
            InventoryRepository inventoryRepository,
            RequestSetRepository requestSetRepository,
            PositionRepository positionRepository,
            ProductRepository productRepository
    ) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
        this.requestSetRepository = requestSetRepository;
        this.positionRepository = positionRepository;
        this.productRepository = productRepository;
    }

    // =====================================================
    // CREATE REQUEST
    // =====================================================
    @Override
    public Long createRequest(InventoryRequestCreateDTO dto) {

        InventoryRequest request = new InventoryRequest();
        request.setUnitId(dto.getUnitId());
        if (dto.getPositionId() != null) {
            request.setPositionId(dto.getPositionId());
        }
        request.setProductId(dto.getProductId());
        request.setRequestType(
                InventoryRequest.RequestType.valueOf(dto.getRequestType())
        );
        request.setNote(dto.getNote());
        request.setRequestStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());

        request = requestRepository.save(request);

        if (dto.getItems() == null) {
            return request.getRequestId();
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

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

        return request.getRequestId();
    }

    /**
     * Resolve variant dựa trên variant_type của product
     * - ITEM_BASED: dùng variantId trực tiếp
     * - STRUCTURED: lookup theo style/size/length/gender
     */
    private ProductVariant resolveVariant(Product product, InventoryRequestCreateDTO.ItemDTO item) {
        // ITEM_BASED hoặc có variantId trực tiếp
        if (product.getVariantType() == VariantType.ITEM_BASED || item.getVariantId() != null) {
            Long variantId = item.getVariantId();
            if (variantId == null) {
                throw new BusinessException("ITEM_BASED product yêu cầu variantId");
            }
            return variantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm không tồn tại"));
        }

        // STRUCTURED: có style (Sơ mi nam) — by ID
        if (item.getStyleId() != null) {
            return variantRepository
                    .findStructuredVariantWithStyle(
                            product.getProductId(),
                            item.getStyleId(),
                            item.getSizeValue(),
                            item.getLengthCode()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm không tồn tại"));
        }

        // STRUCTURED: có style (Sơ mi nam) — by name
        if (item.getStyleName() != null) {
            return variantRepository
                    .findStructuredVariantWithStyleName(
                            product.getProductId(),
                            item.getStyleName(),
                            item.getSizeValue(),
                            item.getLengthCode()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm không tồn tại"));
        }

        // STRUCTURED: có gender + length (Áo phông)
        Gender gender = item.getGender() != null ? Gender.valueOf(item.getGender()) : null;
        if (item.getLengthCode() != null && gender != null) {
            return variantRepository
                    .findStructuredVariantWithGenderAndLength(
                            product.getProductId(),
                            item.getSizeValue(),
                            item.getLengthCode(),
                            gender
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm không tồn tại"));
        }

        // STRUCTURED: chỉ size + gender (Áo khoác, Áo len, Gile BH)
        if (gender != null) {
            return variantRepository
                    .findStructuredVariantWithGender(
                            product.getProductId(),
                            item.getSizeValue(),
                            gender
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm không tồn tại"));
        }

        // STRUCTURED: chỉ size (Giày BH, Bộ áo mưa — no gender, no length, no style)
        if (item.getSizeValue() != null) {
            return variantRepository
                    .findStructuredVariantWithSizeOnly(
                            product.getProductId(),
                            item.getSizeValue()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm không tồn tại"));
        }

        throw new ResourceNotFoundException("Không thể xác định variant cho sản phẩm");
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
                                -> new ResourceNotFoundException("Phiếu nhập xuất không tồn tại")
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

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryRequestListDTO> getAllRequests(Pageable pageable) {
        return requestRepository.findAllRequestsPageable(pageable);
    }

    // =====================================================
    // DELETE REQUEST
    // =====================================================
    @Override
    public void deleteRequest(Long requestId) {
        if (!requestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Phiếu nhập xuất không tồn tại");
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
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu nhập xuất không tồn tại"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu nhập xuất không tồn tại"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu nhập xuất không tồn tại"));

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
                    .orElseThrow(() -> new ResourceNotFoundException("Bộ phiếu không tồn tại"));

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
