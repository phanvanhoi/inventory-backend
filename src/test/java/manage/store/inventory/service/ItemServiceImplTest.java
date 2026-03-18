package manage.store.inventory.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.dto.ItemCreateDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.dto.ItemUpdateDTO;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.enums.Gender;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.ProductVariantRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private InventoryRequestItemRepository itemRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private ProductVariant structuredVariant;
    private ProductVariant itemBasedVariant;

    @BeforeEach
    void setUp() {
        structuredVariant = new ProductVariant();
        structuredVariant.setVariantId(10L);
        structuredVariant.setProductId(1L);
        structuredVariant.setStyleId(1L);
        structuredVariant.setSizeId(1L);
        structuredVariant.setGender(Gender.NAM);

        itemBasedVariant = new ProductVariant();
        itemBasedVariant.setVariantId(20L);
        itemBasedVariant.setProductId(2L);
        itemBasedVariant.setItemCode("PK001");
        itemBasedVariant.setItemName("Cúc áo");
        itemBasedVariant.setUnit("Cái");
    }

    // ==================== CREATE ITEM ====================

    @Test
    @DisplayName("Tạo item với variantId trực tiếp (ITEM_BASED)")
    void createItem_withVariantId_success() {
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setRequestId(1L);
        dto.setVariantId(20L);
        dto.setQuantity(new BigDecimal("100"));

        when(variantRepository.findById(20L)).thenReturn(Optional.of(itemBasedVariant));

        InventoryRequestItem savedItem = new InventoryRequestItem();
        savedItem.setItemId(1L);
        savedItem.setRequestId(1L);
        savedItem.setVariantId(20L);
        savedItem.setQuantity(new BigDecimal("100"));
        when(itemRepository.save(any(InventoryRequestItem.class))).thenReturn(savedItem);

        Long itemId = itemService.createItem(dto);

        assertEquals(1L, itemId);
        verify(variantRepository).findById(20L);
    }

    @Test
    @DisplayName("Tạo item với STRUCTURED variant (style + size + length)")
    void createItem_withStyle_success() {
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setRequestId(1L);
        dto.setStyleId(1L);
        dto.setSizeValue("38");
        dto.setLengthCode("COC");
        dto.setQuantity(new BigDecimal("50"));

        when(variantRepository.findVariant(1L, "38", "COC"))
                .thenReturn(Optional.of(structuredVariant));

        InventoryRequestItem savedItem = new InventoryRequestItem();
        savedItem.setItemId(2L);
        when(itemRepository.save(any(InventoryRequestItem.class))).thenReturn(savedItem);

        Long itemId = itemService.createItem(dto);

        assertEquals(2L, itemId);
    }

    @Test
    @DisplayName("Tạo item với gender + size (không có style)")
    void createItem_withGender_success() {
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setRequestId(1L);
        dto.setGender("NAM");
        dto.setSizeValue("XL");
        dto.setQuantity(new BigDecimal("30"));

        when(variantRepository.findStructuredVariantWithGender(null, "XL", Gender.NAM))
                .thenReturn(Optional.of(structuredVariant));

        InventoryRequestItem savedItem = new InventoryRequestItem();
        savedItem.setItemId(3L);
        when(itemRepository.save(any(InventoryRequestItem.class))).thenReturn(savedItem);

        Long itemId = itemService.createItem(dto);

        assertEquals(3L, itemId);
    }

    @Test
    @DisplayName("Tạo item với gender + size + length")
    void createItem_withGenderAndLength_success() {
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setRequestId(1L);
        dto.setGender("NAM");
        dto.setSizeValue("M");
        dto.setLengthCode("DAI");
        dto.setQuantity(new BigDecimal("20"));

        when(variantRepository.findStructuredVariantWithGenderAndLength(null, "M", "DAI", Gender.NAM))
                .thenReturn(Optional.of(structuredVariant));

        InventoryRequestItem savedItem = new InventoryRequestItem();
        savedItem.setItemId(4L);
        when(itemRepository.save(any(InventoryRequestItem.class))).thenReturn(savedItem);

        Long itemId = itemService.createItem(dto);

        assertEquals(4L, itemId);
    }

    @Test
    @DisplayName("Tạo item thất bại - không đủ thông tin variant")
    void createItem_noVariantInfo_throwsException() {
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setRequestId(1L);
        dto.setQuantity(new BigDecimal("10"));
        // No variantId, no styleId, no gender

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> itemService.createItem(dto));
        assertEquals("Không đủ thông tin để xác định variant", ex.getMessage());
    }

    @Test
    @DisplayName("Tạo item thất bại - variant không tìm thấy")
    void createItem_variantNotFound_throwsException() {
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setRequestId(1L);
        dto.setVariantId(999L);
        dto.setQuantity(new BigDecimal("10"));

        when(variantRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> itemService.createItem(dto));
        assertTrue(ex.getMessage().contains("Variant not found"));
    }

    // ==================== UPDATE ITEM ====================

    @Test
    @DisplayName("Cập nhật số lượng item thành công")
    void updateItem_success() {
        InventoryRequestItem existingItem = new InventoryRequestItem();
        existingItem.setItemId(1L);
        existingItem.setRequestId(1L);
        existingItem.setVariantId(10L);
        existingItem.setQuantity(new BigDecimal("50"));

        ItemUpdateDTO dto = new ItemUpdateDTO();
        dto.setQuantity(new BigDecimal("100"));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));

        // Mock the detail return
        ItemDetailDTO mockDetail = createMockItemDetail(1L, 1L, 10L, new BigDecimal("100"));
        when(itemRepository.findItemDetailById(1L)).thenReturn(Optional.of(mockDetail));

        ItemDetailDTO result = itemService.updateItem(1L, dto);

        assertEquals(new BigDecimal("100"), result.getQuantity());
        verify(itemRepository).save(existingItem);
    }

    @Test
    @DisplayName("Cập nhật item - quantity <= 0 thì không thay đổi")
    void updateItem_zeroQuantity_noChange() {
        InventoryRequestItem existingItem = new InventoryRequestItem();
        existingItem.setItemId(1L);
        existingItem.setQuantity(new BigDecimal("50"));

        ItemUpdateDTO dto = new ItemUpdateDTO();
        dto.setQuantity(BigDecimal.ZERO);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));

        ItemDetailDTO mockDetail = createMockItemDetail(1L, 1L, 10L, new BigDecimal("50"));
        when(itemRepository.findItemDetailById(1L)).thenReturn(Optional.of(mockDetail));

        itemService.updateItem(1L, dto);

        // quantity should remain unchanged
        assertEquals(new BigDecimal("50"), existingItem.getQuantity());
    }

    @Test
    @DisplayName("Cập nhật item - item không tìm thấy")
    void updateItem_notFound_throwsException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        ItemUpdateDTO dto = new ItemUpdateDTO();
        dto.setQuantity(new BigDecimal("10"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> itemService.updateItem(99L, dto));
        assertTrue(ex.getMessage().contains("Item not found"));
    }

    // ==================== DELETE ITEM ====================

    @Test
    @DisplayName("Xóa item thành công")
    void deleteItem_success() {
        when(itemRepository.existsById(1L)).thenReturn(true);

        itemService.deleteItem(1L);

        verify(itemRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Xóa item - không tìm thấy")
    void deleteItem_notFound_throwsException() {
        when(itemRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> itemService.deleteItem(99L));
        assertTrue(ex.getMessage().contains("Item not found"));
        verify(itemRepository, never()).deleteById(any());
    }

    // ==================== GET ITEMS ====================

    @Test
    @DisplayName("Lấy item theo ID thành công")
    void getItemById_success() {
        ItemDetailDTO mockDetail = createMockItemDetail(1L, 1L, 10L, new BigDecimal("50"));
        when(itemRepository.findItemDetailById(1L)).thenReturn(Optional.of(mockDetail));

        ItemDetailDTO result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getItemId());
    }

    @Test
    @DisplayName("Lấy item theo ID - không tìm thấy")
    void getItemById_notFound_throwsException() {
        when(itemRepository.findItemDetailById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> itemService.getItemById(99L));
        assertTrue(ex.getMessage().contains("Item not found"));
    }

    @Test
    @DisplayName("Lấy danh sách items theo request ID")
    void getItemsByRequestId_returnsList() {
        ItemDetailDTO d1 = createMockItemDetail(1L, 1L, 10L, new BigDecimal("50"));
        ItemDetailDTO d2 = createMockItemDetail(2L, 1L, 11L, new BigDecimal("30"));
        when(itemRepository.findItemDetailsByRequestId(1L)).thenReturn(List.of(d1, d2));

        List<ItemDetailDTO> result = itemService.getItemsByRequestId(1L);

        assertEquals(2, result.size());
    }

    // ==================== HELPER ====================

    private ItemDetailDTO createMockItemDetail(Long itemId, Long requestId, Long variantId, BigDecimal quantity) {
        return new ItemDetailDTO() {
            @Override public Long getItemId() { return itemId; }
            @Override public Long getRequestId() { return requestId; }
            @Override public Long getVariantId() { return variantId; }
            @Override public String getStyleName() { return null; }
            @Override public String getSizeValue() { return "38"; }
            @Override public String getLengthCode() { return null; }
            @Override public String getGender() { return "NAM"; }
            @Override public String getItemCode() { return null; }
            @Override public String getItemName() { return null; }
            @Override public String getUnit() { return null; }
            @Override public BigDecimal getQuantity() { return quantity; }
        };
    }
}
