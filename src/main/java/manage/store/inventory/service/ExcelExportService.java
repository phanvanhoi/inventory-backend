package manage.store.inventory.service;

import manage.store.inventory.dto.ExportItemDTO;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.RequestSetRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelExportService {

    private final InventoryRequestItemRepository itemRepository;
    private final RequestSetRepository requestSetRepository;

    private static final int[] SIZES = {35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45};
    // Column mapping: E=4(size35 coc), F=5(size35 dai), G=6(size36 coc), ...
    // Formula: col = 4 + (sizeIndex * 2) + (lengthOffset)
    // lengthOffset: COC=0, DAI=1
    private static final int FIRST_SIZE_COL = 4; // column E
    private static final int TOTAL_COC_COL = 26; // column AA
    private static final int TOTAL_DAI_COL = 27; // column AB

    public ExcelExportService(InventoryRequestItemRepository itemRepository,
                              RequestSetRepository requestSetRepository) {
        this.itemRepository = itemRepository;
        this.requestSetRepository = requestSetRepository;
    }

    public byte[] exportRequestSet(Long setId) throws IOException {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("RequestSet not found: " + setId));

        List<ExportItemDTO> items = itemRepository.findExportDataBySetId(setId);

        // Group: productName -> list of items
        Map<String, List<ExportItemDTO>> byProduct = new LinkedHashMap<>();
        for (ExportItemDTO item : items) {
            byProduct.computeIfAbsent(item.getProductName(), k -> new ArrayList<>()).add(item);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Đề xuất");
            sheet.setDefaultColumnWidth(5);
            sheet.setColumnWidth(0, 4 * 256);  // STT
            sheet.setColumnWidth(1, 30 * 256); // Đơn vị
            sheet.setColumnWidth(2, 12 * 256); // Chức danh
            sheet.setColumnWidth(3, 16 * 256); // Kiểu dáng

            // Create styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);
            CellStyle productNameStyle = createProductNameStyle(workbook);
            CellStyle infoStyle = createInfoStyle(workbook);

            int rowIdx = 0;

            // Row 0: Title
            Row titleRow = sheet.createRow(rowIdx);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("ĐỀ XUẤT LẤY ÁO SƠ MI SẴN Ở KHO ĐÓNG HÀNG");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, TOTAL_DAI_COL));
            rowIdx += 2;

            // Row 2: Set name
            Row nameRow = sheet.createRow(rowIdx);
            Cell nameCell = nameRow.createCell(0);
            nameCell.setCellValue("ĐX: " + requestSet.getSetName());
            nameCell.setCellStyle(infoStyle);
            rowIdx++;

            // Row 3: Date
            Row dateRow = sheet.createRow(rowIdx);
            Cell dateCell = dateRow.createCell(0);
            String dateStr = requestSet.getCreatedAt() != null
                    ? requestSet.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "";
            dateCell.setCellValue("Ngày đề xuất: " + dateStr);
            dateCell.setCellStyle(infoStyle);
            rowIdx += 2;

            // Each product block
            for (Map.Entry<String, List<ExportItemDTO>> productEntry : byProduct.entrySet()) {
                String productName = productEntry.getKey();
                List<ExportItemDTO> productItems = productEntry.getValue();

                rowIdx = writeProductBlock(sheet, rowIdx, productName, productItems,
                        headerStyle, subHeaderStyle, dataStyle, totalStyle, productNameStyle);
                rowIdx += 2; // gap between products
            }

            // Footer: Người lập / Người duyệt
            Row footerRow = sheet.createRow(rowIdx);
            Cell creatorCell = footerRow.createCell(2);
            creatorCell.setCellValue("Người lập");
            creatorCell.setCellStyle(infoStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 2, 3));

            Cell approverCell = footerRow.createCell(18);
            approverCell.setCellValue("Người duyệt");
            approverCell.setCellStyle(infoStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private int writeProductBlock(Sheet sheet, int startRow, String productName,
                                  List<ExportItemDTO> items,
                                  CellStyle headerStyle, CellStyle subHeaderStyle,
                                  CellStyle dataStyle, CellStyle totalStyle,
                                  CellStyle productNameStyle) {
        int rowIdx = startRow;

        // Product name row
        Row prodRow = sheet.createRow(rowIdx);
        Cell prodCell = prodRow.createCell(0);
        prodCell.setCellValue("(" + productName + ")");
        prodCell.setCellStyle(productNameStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, TOTAL_DAI_COL));
        rowIdx++;

        // Header row 1: STT, Đơn vị, Chức danh, Kiểu dáng, Size35...Size45, Tổng
        int headerRow1 = rowIdx;
        Row hRow1 = sheet.createRow(rowIdx);
        String[] headers = {"STT", "Đơn vị", "Chức danh", "Kiểu dáng"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow1.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }
        for (int i = 0; i < SIZES.length; i++) {
            int col = FIRST_SIZE_COL + i * 2;
            Cell c = hRow1.createCell(col);
            c.setCellValue("Size " + SIZES[i]);
            c.setCellStyle(headerStyle);
            // Merge size header across Cộc+Dài
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, col, col + 1));
        }
        Cell totalHeaderCell = hRow1.createCell(TOTAL_COC_COL);
        totalHeaderCell.setCellValue("Tổng");
        totalHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, TOTAL_COC_COL, TOTAL_DAI_COL));
        rowIdx++;

        // Header row 2: Cộc/Dài for each size
        Row hRow2 = sheet.createRow(rowIdx);
        // Merge STT, Đơn vị, Chức danh, Kiểu dáng across 2 header rows
        for (int i = 0; i < 4; i++) {
            sheet.addMergedRegion(new CellRangeAddress(headerRow1, rowIdx, i, i));
            Cell c = hRow2.createCell(i);
            c.setCellStyle(headerStyle);
        }
        for (int i = 0; i < SIZES.length; i++) {
            int col = FIRST_SIZE_COL + i * 2;
            Cell cocCell = hRow2.createCell(col);
            cocCell.setCellValue("Cộc");
            cocCell.setCellStyle(subHeaderStyle);
            Cell daiCell = hRow2.createCell(col + 1);
            daiCell.setCellValue("Dài");
            daiCell.setCellStyle(subHeaderStyle);
        }
        Cell totalCocH = hRow2.createCell(TOTAL_COC_COL);
        totalCocH.setCellValue("Cộc");
        totalCocH.setCellStyle(subHeaderStyle);
        Cell totalDaiH = hRow2.createCell(TOTAL_DAI_COL);
        totalDaiH.setCellValue("Dài");
        totalDaiH.setCellStyle(subHeaderStyle);
        rowIdx++;

        // Group items by unit+position -> style -> size/length -> quantity
        // LinkedHashMap to keep order
        Map<String, Map<String, Map<String, Integer>>> grouped = new LinkedHashMap<>();
        // key: "unitName|positionCode", value: {styleName: {sizeValue_lengthCode: quantity}}
        Map<String, String> unitPositionMap = new LinkedHashMap<>();

        for (ExportItemDTO item : items) {
            String unitKey = item.getUnitName() + "|" + (item.getPositionCode() != null ? item.getPositionCode() : "");
            unitPositionMap.put(unitKey, unitKey);

            Map<String, Map<String, Integer>> styleMap = grouped.computeIfAbsent(unitKey, k -> new LinkedHashMap<>());
            Map<String, Integer> sizeMap = styleMap.computeIfAbsent(item.getStyleName(), k -> new LinkedHashMap<>());
            String sizeKey = item.getSizeValue() + "_" + item.getLengthCode();
            sizeMap.merge(sizeKey, item.getQuantity(), Integer::sum);
        }

        int dataStartRow = rowIdx;
        int stt = 1;

        for (Map.Entry<String, Map<String, Map<String, Integer>>> unitEntry : grouped.entrySet()) {
            String unitKey = unitEntry.getKey();
            String[] parts = unitKey.split("\\|", -1);
            String unitName = parts[0];
            String positionCode = parts.length > 1 ? parts[1] : "";

            Map<String, Map<String, Integer>> styles = unitEntry.getValue();
            int unitStartRow = rowIdx;
            boolean firstStyle = true;

            for (Map.Entry<String, Map<String, Integer>> styleEntry : styles.entrySet()) {
                String styleName = styleEntry.getKey();
                Map<String, Integer> sizeData = styleEntry.getValue();

                Row dataRow = sheet.createRow(rowIdx);

                if (firstStyle) {
                    Cell sttCell = dataRow.createCell(0);
                    sttCell.setCellValue(stt);
                    sttCell.setCellStyle(dataStyle);

                    Cell unitCell = dataRow.createCell(1);
                    unitCell.setCellValue(unitName);
                    unitCell.setCellStyle(dataStyle);

                    Cell posCell = dataRow.createCell(2);
                    posCell.setCellValue(positionCode);
                    posCell.setCellStyle(dataStyle);

                    firstStyle = false;
                } else {
                    // Empty cells with style for border
                    dataRow.createCell(0).setCellStyle(dataStyle);
                    dataRow.createCell(1).setCellStyle(dataStyle);
                    dataRow.createCell(2).setCellStyle(dataStyle);
                }

                Cell styleCell = dataRow.createCell(3);
                styleCell.setCellValue(styleName);
                styleCell.setCellStyle(dataStyle);

                // Fill size data
                for (int i = 0; i < SIZES.length; i++) {
                    int colCoc = FIRST_SIZE_COL + i * 2;
                    int colDai = colCoc + 1;

                    Integer cocQty = sizeData.get(SIZES[i] + "_COC");
                    Integer daiQty = sizeData.get(SIZES[i] + "_DAI");

                    Cell coc = dataRow.createCell(colCoc);
                    coc.setCellStyle(dataStyle);
                    if (cocQty != null && cocQty > 0) {
                        coc.setCellValue(cocQty);
                    }

                    Cell dai = dataRow.createCell(colDai);
                    dai.setCellStyle(dataStyle);
                    if (daiQty != null && daiQty > 0) {
                        dai.setCellValue(daiQty);
                    }
                }

                // Total formulas (Cộc = sum of odd cols, Dài = total - Cộc)
                String rowNum = String.valueOf(rowIdx + 1);
                Cell totalCoc = dataRow.createCell(TOTAL_COC_COL);
                totalCoc.setCellStyle(dataStyle);
                totalCoc.setCellFormula(buildCocSumFormula(rowIdx));

                Cell totalDai = dataRow.createCell(TOTAL_DAI_COL);
                totalDai.setCellStyle(dataStyle);
                totalDai.setCellFormula("SUM(E" + rowNum + ":Z" + rowNum + ")-AA" + rowNum);

                rowIdx++;
            }

            // Merge STT, Đơn vị, Chức danh if multiple styles
            int unitEndRow = rowIdx - 1;
            if (unitEndRow > unitStartRow) {
                sheet.addMergedRegion(new CellRangeAddress(unitStartRow, unitEndRow, 0, 0));
                sheet.addMergedRegion(new CellRangeAddress(unitStartRow, unitEndRow, 1, 1));
                sheet.addMergedRegion(new CellRangeAddress(unitStartRow, unitEndRow, 2, 2));
            }
            stt++;
        }

        int dataEndRow = rowIdx - 1;

        // TỔNG CỘNG row
        Row totalRow = sheet.createRow(rowIdx);
        Cell totalLabel = totalRow.createCell(0);
        totalLabel.setCellValue("TỔNG CỘNG");
        totalLabel.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 3));
        for (int i = 1; i <= 3; i++) {
            totalRow.createCell(i).setCellStyle(totalStyle);
        }

        // SUM formulas for each column
        for (int col = FIRST_SIZE_COL; col <= TOTAL_DAI_COL; col++) {
            Cell sumCell = totalRow.createCell(col);
            sumCell.setCellStyle(totalStyle);
            String colLetter = getColumnLetter(col);
            sumCell.setCellFormula("SUM(" + colLetter + (dataStartRow + 1) + ":" + colLetter + (dataEndRow + 1) + ")");
        }
        rowIdx++;

        return rowIdx;
    }

    private String buildCocSumFormula(int rowIdx) {
        StringBuilder sb = new StringBuilder();
        String rowNum = String.valueOf(rowIdx + 1);
        for (int i = 0; i < SIZES.length; i++) {
            if (i > 0) sb.append("+");
            String col = getColumnLetter(FIRST_SIZE_COL + i * 2);
            sb.append(col).append(rowNum);
        }
        return sb.toString();
    }

    private String getColumnLetter(int colIndex) {
        if (colIndex < 26) {
            return String.valueOf((char) ('A' + colIndex));
        }
        return String.valueOf((char) ('A' + colIndex / 26 - 1)) + (char) ('A' + colIndex % 26);
    }

    // ========== Cell Styles ==========

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createInfoStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createProductNameStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        font.setItalic(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTotalStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
