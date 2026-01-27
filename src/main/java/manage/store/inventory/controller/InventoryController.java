package manage.store.inventory.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.dto.InventoryBalanceDTO;
import manage.store.inventory.dto.InventoryBalanceViewDTO;
import manage.store.inventory.dto.InventoryRequestHistoryDTO;
import manage.store.inventory.dto.ProductInventoryViewDTO;
import manage.store.inventory.dto.RequestHistoryMatrixDTO;
import manage.store.inventory.dto.RequestHistoryRowDTO;
import manage.store.inventory.entity.Product;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.security.CurrentUser;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final List<Integer> SIZE_COLUMNS = List.of(35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45);

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final CurrentUser currentUser;

    public InventoryController(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            CurrentUser currentUser
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.currentUser = currentUser;
    }

    /**
     * Kiểm tra user có quyền xem expectedQuantity không
     * ADMIN và PURCHASER được xem cả actual và expected
     * USER và STOCKKEEPER chỉ xem actual
     */
    private boolean canViewExpectedQuantity() {
        return currentUser.isAdmin() || currentUser.isPurchaser();
    }

    /**
     * Convert InventoryBalanceDTO sang InventoryBalanceViewDTO
     * Ẩn expectedQuantity nếu user không có quyền
     */
    private List<InventoryBalanceViewDTO> convertToViewDTO(List<InventoryBalanceDTO> data, boolean canViewExpected) {
        return data.stream()
                .map(item -> new InventoryBalanceViewDTO(
                        item.getStyleName(),
                        item.getSizeValue(),
                        item.getLengthCode(),
                        item.getActualQuantity(),
                        canViewExpected ? item.getExpectedQuantity() : null
                ))
                .toList();
    }

    /**
     * Lấy tồn kho theo product ID
     * GET /api/inventory/{productId}
     *
     * Quyền xem dựa vào JWT token:
     * - ADMIN, PURCHASER: Xem cả actualQuantity và expectedQuantity
     * - USER, STOCKKEEPER: Chỉ xem actualQuantity
     */
    @GetMapping("/{productId}")
    public ProductInventoryViewDTO getInventoryByProduct(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        List<InventoryBalanceDTO> rawData = inventoryRepository.getInventoryByProductId(productId);
        boolean canViewExpected = canViewExpectedQuantity();
        List<InventoryBalanceViewDTO> data = convertToViewDTO(rawData, canViewExpected);

        return new ProductInventoryViewDTO(
                product.getProductId(),
                product.getProductName(),
                product.getNote(),
                product.getCreatedAt(),
                data,
                canViewExpected
        );
    }

    /**
     * Lấy danh sách tất cả products với tồn kho
     * GET /api/inventory
     *
     * Quyền xem dựa vào JWT token
     */
    @GetMapping
    public List<ProductInventoryViewDTO> getAllInventory() {
        List<Product> products = productRepository.findAll();
        boolean canViewExpected = canViewExpectedQuantity();

        return products.stream()
                .map(product -> {
                    List<InventoryBalanceDTO> rawData = inventoryRepository
                            .getInventoryByProductId(product.getProductId());
                    List<InventoryBalanceViewDTO> data = convertToViewDTO(rawData, canViewExpected);
                    return new ProductInventoryViewDTO(
                            product.getProductId(),
                            product.getProductName(),
                            product.getNote(),
                            product.getCreatedAt(),
                            data,
                            canViewExpected
                    );
                })
                .toList();
    }

    /**
     * Lấy lịch sử các requests theo product ID và style name (dạng matrix)
     * GET /api/inventory/{productId}/history?style=CỔ ĐIỂN
     *
     * Quyền xem dựa vào JWT token:
     * - ADMIN, PURCHASER: Xem cả APPROVED và EXECUTED
     * - USER, STOCKKEEPER: Chỉ xem EXECUTED
     */
    @GetMapping("/{productId}/history")
    public RequestHistoryMatrixDTO getRequestHistory(
            @PathVariable Long productId,
            @RequestParam("style") String styleName
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        boolean canViewApproved = canViewExpectedQuantity();

        List<InventoryRequestHistoryDTO> rawData;
        if (canViewApproved) {
            rawData = inventoryRepository.getRequestHistoryByProductAndStyle(productId, styleName);
        } else {
            rawData = inventoryRepository.getRequestHistoryByProductAndStyleExecutedOnly(productId, styleName);
        }

        List<RequestHistoryRowDTO> rows = transformToMatrixRows(rawData);

        return new RequestHistoryMatrixDTO(
                product.getProductId(),
                product.getProductName(),
                styleName,
                SIZE_COLUMNS,
                rows
        );
    }

    /**
     * Transform raw data thành matrix rows
     * Group by: requestId + lengthCode
     */
    private List<RequestHistoryRowDTO> transformToMatrixRows(List<InventoryRequestHistoryDTO> rawData) {
        // Key: "requestId-lengthCode" -> Row data
        Map<String, RequestHistoryRowDTO> rowMap = new LinkedHashMap<>();

        for (InventoryRequestHistoryDTO item : rawData) {
            String key = item.getRequestId() + "-" + item.getLengthCode();

            RequestHistoryRowDTO row = rowMap.get(key);
            if (row == null) {
                row = new RequestHistoryRowDTO();
                row.setRequestId(item.getRequestId());
                row.setSetId(item.getSetId());
                row.setSetName(item.getSetName());
                row.setSetStatus(item.getSetStatus());
                row.setUnitName(item.getUnitName());
                row.setRequestType(item.getRequestType());
                row.setLengthCode(item.getLengthCode());
                row.setNote(item.getNote());
                row.setCreatedAt(item.getCreatedAt());

                // Initialize sizes map with 0
                Map<Integer, Integer> sizes = new LinkedHashMap<>();
                for (Integer size : SIZE_COLUMNS) {
                    sizes.put(size, 0);
                }
                row.setSizes(sizes);

                rowMap.put(key, row);
            }

            // Set quantity for this size
            if (item.getSizeValue() != null && item.getQuantity() != null) {
                row.getSizes().put(item.getSizeValue(), item.getQuantity());
            }
        }

        return new ArrayList<>(rowMap.values());
    }
}
