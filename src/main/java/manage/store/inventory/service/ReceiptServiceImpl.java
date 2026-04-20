package manage.store.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.ReceiptCreateDTO;
import manage.store.inventory.dto.ReceiptDetailDTO;
import manage.store.inventory.dto.ReceiptEntryProjection;
import manage.store.inventory.dto.ReceiptTimelineProjection;
import manage.store.inventory.dto.SetReceiptProgressDTO;
import manage.store.inventory.dto.InventoryRequestHeaderDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.entity.ApprovalHistory;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.entity.InventoryRequest;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.OrderItem;
import manage.store.inventory.entity.ReceiptItem;
import manage.store.inventory.entity.ReceiptRecord;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.TailorAssignment;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.ApprovalAction;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.repository.ApprovalHistoryRepository;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.InventoryRequestRepository;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.OrderItemRepository;
import manage.store.inventory.repository.ReceiptItemRepository;
import manage.store.inventory.repository.ReceiptRecordRepository;
import manage.store.inventory.repository.RequestSetRepository;
import manage.store.inventory.repository.TailorAssignmentRepository;
import manage.store.inventory.repository.UserRepository;

@Service
@Transactional
public class ReceiptServiceImpl implements ReceiptService {

    private final RequestSetRepository requestSetRepository;
    private final InventoryRequestRepository requestRepository;
    private final InventoryRequestItemRepository itemRepository;
    private final ReceiptRecordRepository receiptRecordRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final UserRepository userRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final NotificationService notificationService;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final TailorAssignmentRepository tailorAssignmentRepository;

    public ReceiptServiceImpl(
            RequestSetRepository requestSetRepository,
            InventoryRequestRepository requestRepository,
            InventoryRequestItemRepository itemRepository,
            ReceiptRecordRepository receiptRecordRepository,
            ReceiptItemRepository receiptItemRepository,
            UserRepository userRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            NotificationService notificationService,
            InventoryRepository inventoryRepository,
            OrderItemRepository orderItemRepository,
            TailorAssignmentRepository tailorAssignmentRepository
    ) {
        this.requestSetRepository = requestSetRepository;
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.receiptRecordRepository = receiptRecordRepository;
        this.receiptItemRepository = receiptItemRepository;
        this.userRepository = userRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.notificationService = notificationService;
        this.inventoryRepository = inventoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.tailorAssignmentRepository = tailorAssignmentRepository;
    }

    // =====================================================
    // RECORD RECEIPT - Ghi nhận nhận hàng từng phần (Case 3)
    // APPROVED → RECEIVING (lần đầu) hoặc giữ RECEIVING
    // =====================================================
    @Override
    public void recordReceipt(Long setId, ReceiptCreateDTO dto, Long userId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Bộ phiếu không tồn tại"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        // Kiểm tra quyền STOCKKEEPER
        if (!user.isStockkeeper()) {
            throw new BusinessException("Chỉ STOCKKEEPER mới có quyền ghi nhận nhận hàng");
        }

        // Kiểm tra trạng thái: chỉ APPROVED hoặc RECEIVING
        if (requestSet.getStatus() != RequestSetStatus.APPROVED
                && requestSet.getStatus() != RequestSetStatus.RECEIVING) {
            throw new BusinessException("Chỉ có thể nhận hàng cho bộ phiếu đã duyệt (APPROVED) hoặc đang nhận (RECEIVING)");
        }

        // Validate: các item phải thuộc request set này
        List<InventoryRequest> requests = requestRepository.findBySetId(setId);
        for (ReceiptCreateDTO.ReceiptItemDTO itemDTO : dto.getItems()) {
            boolean validRequest = requests.stream()
                    .anyMatch(r -> r.getRequestId().equals(itemDTO.getRequestId()));
            if (!validRequest) {
                throw new BusinessException("Request không thuộc bộ phiếu này");
            }

            // Validate variant thuộc request này — sum tất cả items cùng variantId
            // (VAI_GIAO_THO có thể có nhiều thợ cùng dùng 1 loại vải → nhiều rows cùng variantId)
            List<InventoryRequestItem> requestItems = itemRepository.findByRequestId(itemDTO.getRequestId());
            BigDecimal proposedQty = requestItems.stream()
                    .filter(ri -> ri.getVariantId().equals(itemDTO.getVariantId()))
                    .map(InventoryRequestItem::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (proposedQty.compareTo(BigDecimal.ZERO) == 0) {
                throw new BusinessException("Variant không thuộc request này");
            }

            // Validate: tổng đã nhận + lần này không vượt quá SL đề xuất
            BigDecimal alreadyReceived = receiptItemRepository
                    .getTotalReceivedByRequestAndVariant(setId, itemDTO.getRequestId(), itemDTO.getVariantId());
            BigDecimal totalAfter = alreadyReceived.add(itemDTO.getReceivedQuantity());
            if (totalAfter.compareTo(proposedQty) > 0) {
                throw new BusinessException("Vượt quá số lượng đề xuất");
            }
        }

        // 1. Tạo receipt record
        ReceiptRecord record = new ReceiptRecord();
        record.setSetId(setId);
        record.setReceivedBy(user);
        record.setReceivedAt(LocalDateTime.now());
        record.setNote(dto.getNote());
        // G6, V24 — optional link to OrderItem (Lark integration)
        if (dto.getOrderItemId() != null) {
            OrderItem oi = orderItemRepository.findById(dto.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrderItem không tồn tại"));
            record.setOrderItem(oi);
        }
        record = receiptRecordRepository.save(record);

        // 2. Tạo receipt items
        for (ReceiptCreateDTO.ReceiptItemDTO itemDTO : dto.getItems()) {
            ReceiptItem item = new ReceiptItem();
            item.setReceiptId(record.getReceiptId());
            item.setRequestId(itemDTO.getRequestId());
            item.setVariantId(itemDTO.getVariantId());
            item.setReceivedQuantity(itemDTO.getReceivedQuantity());
            // G6, V24 — optional link to TailorAssignment
            if (itemDTO.getTailorAssignmentId() != null) {
                TailorAssignment ta = tailorAssignmentRepository.findById(itemDTO.getTailorAssignmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("TailorAssignment không tồn tại"));
                item.setTailorAssignment(ta);
            }
            receiptItemRepository.save(item);
        }

        // 3. Chuyển status APPROVED → RECEIVING (lần đầu)
        if (requestSet.getStatus() == RequestSetStatus.APPROVED) {
            requestSet.setStatus(RequestSetStatus.RECEIVING);
            requestSetRepository.save(requestSet);
        }

        // 4. Lưu lịch sử
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.RECEIVE);
        history.setPerformedBy(user);
        history.setReason(dto.getNote());
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);
    }

    // =====================================================
    // COMPLETE RECEIPT - Hoàn tất nhận hàng (Case 3)
    // RECEIVING → EXECUTED
    // Chuyển ADJUST_IN → IN, ADJUST_OUT → OUT
    // =====================================================
    @Override
    public void completeReceipt(Long setId, Long userId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Bộ phiếu không tồn tại"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        // Kiểm tra quyền STOCKKEEPER
        if (!user.isStockkeeper()) {
            throw new BusinessException("Chỉ STOCKKEEPER mới có quyền hoàn tất nhận hàng");
        }

        // Kiểm tra trạng thái: chỉ RECEIVING
        if (requestSet.getStatus() != RequestSetStatus.RECEIVING) {
            throw new BusinessException("Chỉ có thể hoàn tất bộ phiếu đang trong trạng thái nhận hàng (RECEIVING)");
        }

        // Giữ nguyên items.quantity — không ghi đè bằng receipt totals.
        // Quantity đã chính xác: gốc từ APPROVED, hoặc đã sửa bởi editAndReceive.
        // Receipt records chỉ dùng cho tracking tiến độ.
        List<InventoryRequest> requests = requestRepository.findBySetId(setId);
        for (InventoryRequest request : requests) {
            // Chuyển ADJUST_IN → IN, ADJUST_OUT → OUT
            InventoryRequest.RequestType currentType = request.getRequestType();
            if (currentType == InventoryRequest.RequestType.ADJUST_IN) {
                request.setRequestType(InventoryRequest.RequestType.IN);
                requestRepository.save(request);
            } else if (currentType == InventoryRequest.RequestType.ADJUST_OUT) {
                // Validate tồn kho thực tế trước khi chuyển ADJUST_OUT → OUT
                List<InventoryRequestItem> outItems = itemRepository.findByRequestId(request.getRequestId());
                for (InventoryRequestItem outItem : outItems) {
                    if (outItem.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal actualQty = inventoryRepository.getActualQuantityByVariantAndWarehouse(
                                request.getProductId(), outItem.getVariantId(), request.getWarehouseId());
                        if (actualQty == null) actualQty = BigDecimal.ZERO;
                        if (outItem.getQuantity().compareTo(actualQty) > 0) {
                            throw new BusinessException("Không thể hoàn tất: số lượng xuất (" + outItem.getQuantity() +
                                    ") vượt quá tồn kho thực tế (" + actualQty + "). " +
                                    "Hãy chờ hàng nhập kho thực tế trước khi hoàn tất.");
                        }
                    }
                }
                request.setRequestType(InventoryRequest.RequestType.OUT);
                requestRepository.save(request);
            }
        }

        // Cập nhật trạng thái → EXECUTED
        requestSet.setStatus(RequestSetStatus.EXECUTED);
        requestSet.setExecutedByUser(user);
        requestSet.setExecutedAt(LocalDateTime.now());
        requestSetRepository.save(requestSet);

        // Lưu lịch sử
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestSet(requestSet);
        history.setAction(ApprovalAction.COMPLETE);
        history.setPerformedBy(user);
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        // Thông báo cho người tạo
        notificationService.notifyCreatorOfExecution(requestSet, user);
    }

    // =====================================================
    // GET RECEIPTS - Lấy danh sách các lần nhận hàng
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<ReceiptDetailDTO> getReceipts(Long setId) {
        // Verify set exists
        requestSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Bộ phiếu không tồn tại"));

        List<ReceiptRecord> records = receiptRecordRepository.findBySetIdOrderByReceivedAtDesc(setId);
        List<ReceiptDetailDTO> result = new ArrayList<>();

        for (ReceiptRecord record : records) {
            ReceiptDetailDTO dto = new ReceiptDetailDTO();
            dto.setReceiptId(record.getReceiptId());
            dto.setSetId(record.getSetId());
            dto.setReceivedBy(record.getReceivedBy().getUserId());
            dto.setReceivedByName(record.getReceivedBy().getFullName());
            dto.setReceivedAt(record.getReceivedAt());
            dto.setNote(record.getNote());
            // G6, V24 — Lark link (LAZY: proxy access only if FK populated)
            if (record.getOrderItem() != null) {
                dto.setOrderItemId(record.getOrderItem().getOrderItemId());
            }

            List<ReceiptItem> items = receiptItemRepository.findByReceiptId(record.getReceiptId());
            List<ReceiptDetailDTO.ReceiptItemDetailDTO> itemDetails = new ArrayList<>();

            for (ReceiptItem item : items) {
                ReceiptDetailDTO.ReceiptItemDetailDTO itemDetail = new ReceiptDetailDTO.ReceiptItemDetailDTO();
                itemDetail.setReceiptItemId(item.getReceiptItemId());
                itemDetail.setRequestId(item.getRequestId());
                itemDetail.setVariantId(item.getVariantId());
                itemDetail.setReceivedQuantity(item.getReceivedQuantity());
                // G6, V24 — Lark link
                if (item.getTailorAssignment() != null) {
                    itemDetail.setTailorAssignmentId(item.getTailorAssignment().getAssignmentId());
                }
                itemDetails.add(itemDetail);
            }

            dto.setItems(itemDetails);
            result.add(dto);
        }

        return result;
    }

    // =====================================================
    // GET PROGRESS - Tiến độ nhận hàng (phân cấp)
    // Set → Requests → Items → ReceiptHistory + Timeline
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public SetReceiptProgressDTO getProgress(Long setId) {
        RequestSet requestSet = requestSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Bộ phiếu không tồn tại"));

        SetReceiptProgressDTO result = new SetReceiptProgressDTO();
        result.setSetId(setId);
        result.setSetName(requestSet.getSetName());
        result.setStatus(requestSet.getStatus().name());

        // ── Build per-request progress ──
        List<InventoryRequest> requests = requestRepository.findBySetId(setId);
        List<SetReceiptProgressDTO.RequestProgress> requestProgressList = new ArrayList<>();

        BigDecimal grandTotalProposed = BigDecimal.ZERO;
        BigDecimal grandTotalReceived = BigDecimal.ZERO;

        for (InventoryRequest request : requests) {
            SetReceiptProgressDTO.RequestProgress rp = new SetReceiptProgressDTO.RequestProgress();
            rp.setRequestId(request.getRequestId());
            rp.setRequestType(request.getRequestType().name());

            // Lấy header info (unitName, positionCode, productName)
            InventoryRequestHeaderDTO header = requestRepository
                    .findHeaderByRequestId(request.getRequestId()).orElse(null);
            if (header != null) {
                rp.setUnitName(header.getUnitName());
                rp.setPositionCode(header.getPositionCode());
                rp.setProductName(header.getProductName());
            }

            // Lấy items với variant details
            List<ItemDetailDTO> itemDetails = itemRepository
                    .findItemDetailsByRequestId(request.getRequestId());

            // Group by variantId để tránh double-count totalReceived khi cùng variantId
            // xuất hiện nhiều lần trong 1 request (VD: VAI_GIAO_THO có nhiều thợ cùng loại vải)
            Map<Long, List<ItemDetailDTO>> byVariant = new LinkedHashMap<>();
            for (ItemDetailDTO d : itemDetails) {
                byVariant.computeIfAbsent(d.getVariantId(), k -> new ArrayList<>()).add(d);
            }

            List<SetReceiptProgressDTO.ItemProgress> itemProgressList = new ArrayList<>();
            BigDecimal requestTotalProposed = BigDecimal.ZERO;
            BigDecimal requestTotalReceived = BigDecimal.ZERO;

            for (Map.Entry<Long, List<ItemDetailDTO>> entry : byVariant.entrySet()) {
                Long variantId = entry.getKey();
                List<ItemDetailDTO> variantItems = entry.getValue();
                ItemDetailDTO first = variantItems.get(0); // metadata từ item đầu tiên

                // Tổng proposed = cộng tất cả items cùng variantId (mỗi thợ một dòng)
                BigDecimal proposed = variantItems.stream()
                        .map(d -> d.getQuantity() != null ? d.getQuantity() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Chỉ gọi 1 lần cho (setId, requestId, variantId) — không nhân đôi
                BigDecimal totalReceived = receiptItemRepository.getTotalReceivedByRequestAndVariant(
                        setId, request.getRequestId(), variantId);
                if (totalReceived == null) totalReceived = BigDecimal.ZERO;

                BigDecimal remaining = proposed.subtract(totalReceived).max(BigDecimal.ZERO);
                double pct = proposed.compareTo(BigDecimal.ZERO) > 0
                        ? Math.round(totalReceived.doubleValue() / proposed.doubleValue() * 100.0 * 100.0) / 100.0
                        : (totalReceived.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);

                // Lịch sử nhận cho variant này
                List<ReceiptEntryProjection> entries = receiptItemRepository
                        .findReceiptHistoryByVariant(setId, request.getRequestId(), variantId);
                List<SetReceiptProgressDTO.ReceiptEntry> receiptHistory = new ArrayList<>();
                for (ReceiptEntryProjection e : entries) {
                    SetReceiptProgressDTO.ReceiptEntry re = new SetReceiptProgressDTO.ReceiptEntry();
                    re.setReceiptId(e.getReceiptId());
                    re.setReceivedAt(e.getReceivedAt());
                    re.setReceivedByName(e.getReceivedByName());
                    re.setReceivedQuantity(e.getReceivedQuantity());
                    receiptHistory.add(re);
                }

                SetReceiptProgressDTO.ItemProgress ip = new SetReceiptProgressDTO.ItemProgress();
                ip.setVariantId(variantId);
                ip.setStyleName(first.getStyleName());
                ip.setSizeValue(first.getSizeValue());
                ip.setLengthCode(first.getLengthCode());
                ip.setGender(first.getGender());
                ip.setItemCode(first.getItemCode());
                ip.setItemName(first.getItemName());
                ip.setUnit(first.getUnit());
                ip.setProposedQuantity(proposed);
                ip.setTotalReceived(totalReceived);
                ip.setRemainingQuantity(remaining);
                ip.setPercentage(pct);
                ip.setReceiptHistory(receiptHistory);
                itemProgressList.add(ip);

                requestTotalProposed = requestTotalProposed.add(proposed);
                requestTotalReceived = requestTotalReceived.add(totalReceived);
            }

            BigDecimal requestRemaining = requestTotalProposed.subtract(requestTotalReceived).max(BigDecimal.ZERO);
            double requestPct = requestTotalProposed.compareTo(BigDecimal.ZERO) > 0
                    ? Math.round(requestTotalReceived.doubleValue() / requestTotalProposed.doubleValue() * 100.0 * 100.0) / 100.0
                    : (requestTotalReceived.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);

            rp.setTotalProposed(requestTotalProposed);
            rp.setTotalReceived(requestTotalReceived);
            rp.setTotalRemaining(requestRemaining);
            rp.setPercentage(requestPct);
            rp.setItems(itemProgressList);
            requestProgressList.add(rp);

            grandTotalProposed = grandTotalProposed.add(requestTotalProposed);
            grandTotalReceived = grandTotalReceived.add(requestTotalReceived);
        }

        result.setRequests(requestProgressList);

        // ── Build timeline ──
        List<ReceiptTimelineProjection> timelineRows = receiptRecordRepository.findTimelineBySetId(setId);
        List<SetReceiptProgressDTO.ReceiptTimeline> timeline = new ArrayList<>();
        for (ReceiptTimelineProjection row : timelineRows) {
            SetReceiptProgressDTO.ReceiptTimeline tl = new SetReceiptProgressDTO.ReceiptTimeline();
            tl.setReceiptId(row.getReceiptId());
            tl.setReceivedAt(row.getReceivedAt());
            tl.setReceivedByName(row.getReceivedByName());
            tl.setNote(row.getNote());
            tl.setTotalItems(row.getTotalItems() != null ? row.getTotalItems() : 0);
            tl.setTotalQuantity(row.getTotalQuantity() != null ? row.getTotalQuantity() : BigDecimal.ZERO);
            timeline.add(tl);
        }
        result.setTimeline(timeline);

        // ── Build overall summary ──
        BigDecimal grandRemaining = grandTotalProposed.subtract(grandTotalReceived).max(BigDecimal.ZERO);
        double overallPct = grandTotalProposed.compareTo(BigDecimal.ZERO) > 0
                ? Math.round(grandTotalReceived.doubleValue() / grandTotalProposed.doubleValue() * 100.0 * 100.0) / 100.0
                : (grandTotalReceived.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);

        SetReceiptProgressDTO.OverallSummary summary = new SetReceiptProgressDTO.OverallSummary();
        summary.setTotalProposed(grandTotalProposed);
        summary.setTotalReceived(grandTotalReceived);
        summary.setTotalRemaining(grandRemaining);
        summary.setOverallPercentage(overallPct);
        summary.setReceiptCount(timelineRows.size());

        if (!timelineRows.isEmpty()) {
            // Timeline đã ORDER BY received_at DESC → last = cuối list, first = đầu list
            summary.setLastReceivedAt(timelineRows.get(0).getReceivedAt());
            summary.setFirstReceivedAt(timelineRows.get(timelineRows.size() - 1).getReceivedAt());
        }

        result.setSummary(summary);

        return result;
    }
}
