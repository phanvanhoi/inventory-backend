package manage.store.inventory.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import manage.store.inventory.dto.InventoryBalanceDTO;
import manage.store.inventory.dto.InventoryBalanceViewDTO;
import manage.store.inventory.dto.InventoryRequestHistoryDTO;
import manage.store.inventory.dto.ProductInventoryViewDTO;
import manage.store.inventory.dto.RequestHistoryMatrixDTO;
import manage.store.inventory.dto.RequestHistoryRowDTO;
import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.enums.VariantType;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.security.CurrentUser;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

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
                        item.getVariantId(),
                        item.getStyleName(),
                        item.getSizeValue(),
                        item.getLengthCode(),
                        item.getGender(),
                        item.getItemCode(),
                        item.getItemName(),
                        item.getUnit(),
                        item.getActualQuantity(),
                        canViewExpected ? item.getExpectedQuantity() : null
                ))
                .toList();
    }

    /**
     * Lấy tồn kho theo product ID, optional filter theo kho
     * GET /api/inventory/{productId}
     * GET /api/inventory/{productId}?warehouseId=1
     */
    @GetMapping("/{productId}")
    public ProductInventoryViewDTO getInventoryByProduct(
            @PathVariable Long productId,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        log.info("[getInventoryByProduct] productId={}, warehouseId={}", productId, warehouseId);

        List<InventoryBalanceDTO> rawData;
        if (warehouseId != null) {
            rawData = inventoryRepository.getInventoryByProductIdAndWarehouse(productId, warehouseId);
        } else {
            rawData = inventoryRepository.getInventoryByProductId(productId);
        }
        boolean canViewExpected = canViewExpectedQuantity();
        List<InventoryBalanceViewDTO> data = convertToViewDTO(rawData, canViewExpected);

        // Debug: log first few items with non-zero quantities
        data.stream()
            .filter(d -> d.getActualQuantity() != null && d.getActualQuantity().doubleValue() != 0)
            .limit(5)
            .forEach(d -> log.info("[getInventoryByProduct]   variant={} actual={} expected={}",
                d.getVariantId(), d.getActualQuantity(), d.getExpectedQuantity()));

        return new ProductInventoryViewDTO(
                product.getProductId(),
                product.getProductName(),
                product.getVariantType().name(),
                product.getNote(),
                product.getCreatedAt(),
                data,
                canViewExpected
        );
    }

    /**
     * Lấy danh sách tất cả products với tồn kho
     * GET /api/inventory
     * GET /api/inventory?warehouseId=1
     */
    @GetMapping
    public List<ProductInventoryViewDTO> getAllInventory(
            @RequestParam(value = "warehouseId", required = false) Long warehouseId
    ) {
        log.info("[getAllInventory] warehouseId={}", warehouseId);
        // Bỏ parent products (chỉ lấy products không có children = leaf products)
        List<Product> products = productRepository.findLeafProducts();
        boolean canViewExpected = canViewExpectedQuantity();

        return products.stream()
                .map(product -> {
                    List<InventoryBalanceDTO> rawData;
                    if (warehouseId != null) {
                        rawData = inventoryRepository.getInventoryByProductIdAndWarehouse(
                                product.getProductId(), warehouseId);
                    } else {
                        rawData = inventoryRepository.getInventoryByProductId(product.getProductId());
                    }
                    List<InventoryBalanceViewDTO> data = convertToViewDTO(rawData, canViewExpected);
                    return new ProductInventoryViewDTO(
                            product.getProductId(),
                            product.getProductName(),
                            product.getVariantType().name(),
                            product.getNote(),
                            product.getCreatedAt(),
                            data,
                            canViewExpected
                    );
                })
                .toList();
    }

    /**
     * Lấy lịch sử các requests theo product ID và filter
     * GET /api/inventory/{productId}/history?filter=CỔ ĐIỂN
     * GET /api/inventory/{productId}/history?filter=CỔ ĐIỂN&warehouseId=1
     * filter = styleName (STRUCTURED with style) hoặc gender (STRUCTURED with gender) hoặc null (ITEM_BASED)
     */
    @GetMapping("/{productId}/history")
    public RequestHistoryMatrixDTO getRequestHistory(
            @PathVariable Long productId,
            @RequestParam(value = "filter", required = false) String filterValue,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        boolean canViewApproved = canViewExpectedQuantity();

        List<InventoryRequestHistoryDTO> rawData;
        if (canViewApproved) {
            rawData = inventoryRepository.getRequestHistoryByProductAndStyle(productId, filterValue, warehouseId);
        } else {
            rawData = inventoryRepository.getRequestHistoryByProductAndStyleExecutedOnly(productId, filterValue, warehouseId);
        }

        String variantType = product.getVariantType().name();

        if (product.getVariantType() == VariantType.ITEM_BASED) {
            // ITEM_BASED: mỗi row là 1 record riêng lẻ, không group theo size
            List<RequestHistoryRowDTO> rows = transformToItemBasedRows(rawData);
            return new RequestHistoryMatrixDTO(
                    product.getProductId(),
                    product.getProductName(),
                    variantType,
                    filterValue,
                    List.of(),
                    rows
            );
        }

        // STRUCTURED: group by requestId + lengthCode, pivot sizes
        List<String> sizeColumns = extractSizeColumns(rawData);
        List<RequestHistoryRowDTO> rows = transformToMatrixRows(rawData, sizeColumns);

        return new RequestHistoryMatrixDTO(
                product.getProductId(),
                product.getProductName(),
                variantType,
                filterValue,
                sizeColumns,
                rows
        );
    }

    /**
     * Extract unique size values from history data (sorted by appearance)
     */
    private List<String> extractSizeColumns(List<InventoryRequestHistoryDTO> rawData) {
        return rawData.stream()
                .map(InventoryRequestHistoryDTO::getSizeValue)
                .filter(sv -> sv != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Transform raw data thành matrix rows (STRUCTURED products)
     * Group by: requestId + lengthCode
     */
    private List<RequestHistoryRowDTO> transformToMatrixRows(
            List<InventoryRequestHistoryDTO> rawData,
            List<String> sizeColumns
    ) {
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
                row.setCreatedBy(item.getCreatedBy());
                row.setCreatedByName(item.getCreatedByName());

                // Initialize sizes map with 0
                Map<String, BigDecimal> sizes = new LinkedHashMap<>();
                for (String size : sizeColumns) {
                    sizes.put(size, BigDecimal.ZERO);
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

    /**
     * Transform raw data thành flat rows (ITEM_BASED products)
     * Mỗi record = 1 row
     */
    private List<RequestHistoryRowDTO> transformToItemBasedRows(List<InventoryRequestHistoryDTO> rawData) {
        List<RequestHistoryRowDTO> rows = new ArrayList<>();

        for (InventoryRequestHistoryDTO item : rawData) {
            RequestHistoryRowDTO row = new RequestHistoryRowDTO();
            row.setRequestId(item.getRequestId());
            row.setSetId(item.getSetId());
            row.setSetName(item.getSetName());
            row.setSetStatus(item.getSetStatus());
            row.setUnitName(item.getUnitName());
            row.setRequestType(item.getRequestType());
            row.setNote(item.getNote());
            row.setCreatedAt(item.getCreatedAt());
            row.setCreatedBy(item.getCreatedBy());
            row.setCreatedByName(item.getCreatedByName());
            row.setVariantId(item.getVariantId());
            row.setItemCode(item.getItemCode());
            row.setItemName(item.getItemName());
            row.setUnit(item.getUnit());
            row.setQuantity(item.getQuantity());
            rows.add(row);
        }

        return rows;
    }
}
