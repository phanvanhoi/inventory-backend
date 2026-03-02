package manage.store.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho API GET /{setId}/progress
 * Cấu trúc phân cấp: Set → Requests → Items → ReceiptHistory
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetReceiptProgressDTO {

    // ── Thông tin bộ phiếu ──
    private Long setId;
    private String setName;
    private String status;

    // ── Tổng quan toàn bộ ──
    private OverallSummary summary;

    // ── Chi tiết theo từng phiếu ──
    private List<RequestProgress> requests;

    // ── Dòng thời gian nhận hàng ──
    private List<ReceiptTimeline> timeline;

    // =========================================================
    // Tổng quan toàn bộ bộ phiếu
    // =========================================================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OverallSummary {
        private int totalProposed;
        private int totalReceived;
        private int totalRemaining;
        private double overallPercentage;
        private int receiptCount;        // Số lần nhận hàng
        private LocalDateTime firstReceivedAt;
        private LocalDateTime lastReceivedAt;
    }

    // =========================================================
    // Tiến độ từng phiếu (request)
    // =========================================================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequestProgress {
        private Long requestId;
        private String unitName;
        private String positionCode;
        private String productName;
        private String requestType;     // IN, OUT, ADJUST_IN, ADJUST_OUT

        // Tổng hợp phiếu
        private int totalProposed;
        private int totalReceived;
        private int totalRemaining;
        private double percentage;

        // Chi tiết từng biến thể
        private List<ItemProgress> items;
    }

    // =========================================================
    // Tiến độ từng biến thể (variant) trong phiếu
    // =========================================================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemProgress {
        private Long variantId;
        private String styleName;
        private Integer sizeValue;
        private String lengthCode;

        private int proposedQuantity;
        private int totalReceived;
        private int remainingQuantity;
        private double percentage;

        // Lịch sử các lần nhận cho variant này
        private List<ReceiptEntry> receiptHistory;
    }

    // =========================================================
    // Một lần nhận hàng cho 1 variant cụ thể
    // =========================================================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReceiptEntry {
        private Long receiptId;
        private LocalDateTime receivedAt;
        private String receivedByName;
        private int receivedQuantity;
    }

    // =========================================================
    // Dòng thời gian: tổng hợp mỗi lần nhận hàng
    // =========================================================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReceiptTimeline {
        private Long receiptId;
        private LocalDateTime receivedAt;
        private String receivedByName;
        private String note;
        private int totalItems;        // Số biến thể nhận trong lần này
        private int totalQuantity;     // Tổng số lượng nhận trong lần này
    }
}
