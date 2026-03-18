package manage.store.inventory.service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import manage.store.inventory.dto.ExportItemDTO;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.RequestSetRepository;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {

    @Mock
    private InventoryRequestItemRepository itemRepository;

    @Mock
    private RequestSetRepository requestSetRepository;

    @InjectMocks
    private ExcelExportService excelExportService;

    private RequestSet createTestRequestSet() {
        RequestSet rs = new RequestSet();
        rs.setSetId(1L);
        rs.setSetName("Test Export");
        rs.setStatus(RequestSetStatus.APPROVED);
        rs.setCreatedAt(LocalDateTime.of(2026, 3, 18, 10, 0));
        return rs;
    }

    @Test
    @DisplayName("Export thành công - tạo file Excel hợp lệ")
    void exportRequestSet_success_returnsValidExcelBytes() throws Exception {
        RequestSet rs = createTestRequestSet();
        when(requestSetRepository.findById(1L)).thenReturn(Optional.of(rs));

        ExportItemDTO item = createMockExportItem("Sơ mi nam dài tay", "Kiểu 1", "38", "COC", new BigDecimal("10"));
        when(itemRepository.findExportDataBySetId(1L)).thenReturn(List.of(item));

        byte[] result = excelExportService.exportRequestSet(1L);

        assertNotNull(result);
        assertTrue(result.length > 0);

        // Parse back and verify
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertEquals(1, wb.getNumberOfSheets());
            assertEquals("Đề xuất", wb.getSheetName(0));
        }
    }

    @Test
    @DisplayName("Export - RequestSet không tồn tại")
    void exportRequestSet_notFound_throwsException() {
        when(requestSetRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> excelExportService.exportRequestSet(99L));
        assertTrue(ex.getMessage().contains("RequestSet not found"));
    }

    @Test
    @DisplayName("Export - không có items vẫn tạo file hợp lệ")
    void exportRequestSet_emptyItems_generatesFile() throws Exception {
        RequestSet rs = createTestRequestSet();
        when(requestSetRepository.findById(1L)).thenReturn(Optional.of(rs));
        when(itemRepository.findExportDataBySetId(1L)).thenReturn(Collections.emptyList());

        byte[] result = excelExportService.exportRequestSet(1L);

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertNotNull(wb.getSheet("Đề xuất"));
        }
    }

    @Test
    @DisplayName("Export - nhiều sản phẩm tạo nhiều block")
    void exportRequestSet_multipleProducts_createsMultipleBlocks() throws Exception {
        RequestSet rs = createTestRequestSet();
        when(requestSetRepository.findById(1L)).thenReturn(Optional.of(rs));

        ExportItemDTO item1 = createMockExportItem("Sơ mi nam dài tay", "Kiểu 1", "38", "COC", new BigDecimal("10"));
        ExportItemDTO item2 = createMockExportItem("Áo khoác nam", "Kiểu A", "40", "DAI", new BigDecimal("5"));

        when(itemRepository.findExportDataBySetId(1L)).thenReturn(List.of(item1, item2));

        byte[] result = excelExportService.exportRequestSet(1L);

        assertNotNull(result);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            // File should be valid and have data rows
            assertTrue(wb.getSheetAt(0).getLastRowNum() > 5);
        }
    }

    private ExportItemDTO createMockExportItem(String productName, String styleName,
                                                String sizeValue, String lengthCode,
                                                BigDecimal quantity) {
        return new ExportItemDTO() {
            @Override public Long getRequestId() { return 1L; }
            @Override public String getUnitName() { return "Bưu điện HN"; }
            @Override public String getPositionCode() { return "GD"; }
            @Override public String getProductName() { return productName; }
            @Override public String getStyleName() { return styleName; }
            @Override public String getSizeValue() { return sizeValue; }
            @Override public String getLengthCode() { return lengthCode; }
            @Override public String getGender() { return "NAM"; }
            @Override public String getItemCode() { return null; }
            @Override public String getItemName() { return null; }
            @Override public String getUnit() { return null; }
            @Override public BigDecimal getQuantity() { return quantity; }
        };
    }
}
