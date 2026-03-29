package manage.store.inventory.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.dto.InventoryRequestHeaderDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.dto.ReceiptCreateDTO;
import manage.store.inventory.dto.ReceiptEntryProjection;
import manage.store.inventory.entity.ApprovalHistory;
import manage.store.inventory.entity.InventoryRequest;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.ReceiptItem;
import manage.store.inventory.entity.ReceiptRecord;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.Role;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.repository.ApprovalHistoryRepository;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.InventoryRequestRepository;
import manage.store.inventory.repository.ReceiptItemRepository;
import manage.store.inventory.repository.ReceiptRecordRepository;
import manage.store.inventory.repository.RequestSetRepository;
import manage.store.inventory.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {

    @Mock private RequestSetRepository requestSetRepository;
    @Mock private InventoryRequestRepository requestRepository;
    @Mock private InventoryRequestItemRepository itemRepository;
    @Mock private ReceiptRecordRepository receiptRecordRepository;
    @Mock private ReceiptItemRepository receiptItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApprovalHistoryRepository approvalHistoryRepository;
    @Mock private NotificationService notificationService;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks
    private ReceiptServiceImpl receiptService;

    private User stockkeeperUser;
    private User regularUser;
    private RequestSet approvedSet;
    private RequestSet receivingSet;
    private RequestSet pendingSet;

    @BeforeEach
    void setUp() {
        Role stockkeeperRole = new Role();
        stockkeeperRole.setRoleId(1L);
        stockkeeperRole.setRoleName("STOCKKEEPER");

        stockkeeperUser = new User();
        stockkeeperUser.setUserId(1L);
        stockkeeperUser.setUsername("stockkeeper");
        stockkeeperUser.setFullName("Stockkeeper");
        Set<Role> roles = new HashSet<>();
        roles.add(stockkeeperRole);
        stockkeeperUser.setRoles(roles);

        Role userRole = new Role();
        userRole.setRoleId(2L);
        userRole.setRoleName("USER");

        regularUser = new User();
        regularUser.setUserId(2L);
        regularUser.setUsername("user");
        regularUser.setFullName("User");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        regularUser.setRoles(userRoles);

        approvedSet = new RequestSet();
        approvedSet.setSetId(1L);
        approvedSet.setSetName("Approved Set");
        approvedSet.setStatus(RequestSetStatus.APPROVED);
        approvedSet.setCreatedByUser(regularUser);

        receivingSet = new RequestSet();
        receivingSet.setSetId(2L);
        receivingSet.setSetName("Receiving Set");
        receivingSet.setStatus(RequestSetStatus.RECEIVING);
        receivingSet.setCreatedByUser(regularUser);

        pendingSet = new RequestSet();
        pendingSet.setSetId(3L);
        pendingSet.setSetName("Pending Set");
        pendingSet.setStatus(RequestSetStatus.PENDING);
        pendingSet.setCreatedByUser(regularUser);
    }

    // ==================== RECORD RECEIPT ====================

    @Nested
    @DisplayName("Record Receipt")
    class RecordReceiptTests {

        @Test
        @DisplayName("Ghi nhận nhận hàng lần đầu (APPROVED -> RECEIVING)")
        void recordReceipt_firstTime_success() {
            InventoryRequest request = new InventoryRequest();
            request.setRequestId(10L);
            request.setSetId(1L);

            InventoryRequestItem item = new InventoryRequestItem();
            item.setItemId(1L);
            item.setRequestId(10L);
            item.setVariantId(100L);
            item.setQuantity(new BigDecimal("50")); // proposed = 50

            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            dto.setNote("Lần nhận 1");
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(10L);
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("20")); // 20 <= 50 → OK
            dto.setItems(List.of(itemDTO));

            ReceiptRecord savedRecord = new ReceiptRecord();
            savedRecord.setReceiptId(1L);

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(request));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 10L, 100L))
                    .thenReturn(BigDecimal.ZERO);
            when(receiptRecordRepository.save(any(ReceiptRecord.class))).thenReturn(savedRecord);

            receiptService.recordReceipt(1L, dto, 1L);

            // APPROVED -> RECEIVING
            assertEquals(RequestSetStatus.RECEIVING, approvedSet.getStatus());
            verify(requestSetRepository).save(approvedSet);
            verify(receiptItemRepository).save(any(ReceiptItem.class));
            verify(approvalHistoryRepository).save(any(ApprovalHistory.class));
        }

        @Test
        @DisplayName("Ghi nhận nhận hàng tiếp theo (RECEIVING giữ nguyên)")
        void recordReceipt_subsequentTime_staysReceiving() {
            InventoryRequest request = new InventoryRequest();
            request.setRequestId(10L);
            request.setSetId(2L);

            InventoryRequestItem item = new InventoryRequestItem();
            item.setItemId(1L);
            item.setRequestId(10L);
            item.setVariantId(100L);
            item.setQuantity(new BigDecimal("50"));

            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            dto.setNote("Lần nhận 2");
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(10L);
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("10"));
            dto.setItems(List.of(itemDTO));

            ReceiptRecord savedRecord = new ReceiptRecord();
            savedRecord.setReceiptId(2L);

            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(receivingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(2L)).thenReturn(List.of(request));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(2L, 10L, 100L))
                    .thenReturn(new BigDecimal("20")); // đã nhận 20, nhận thêm 10 = 30 <= 50
            when(receiptRecordRepository.save(any(ReceiptRecord.class))).thenReturn(savedRecord);

            receiptService.recordReceipt(2L, dto, 1L);

            // RECEIVING stays RECEIVING
            assertEquals(RequestSetStatus.RECEIVING, receivingSet.getStatus());
            verify(requestSetRepository, never()).save(receivingSet);
        }

        @Test
        @DisplayName("Nhận vượt số lượng đề xuất - thất bại")
        void recordReceipt_exceedsProposed_throwsException() {
            InventoryRequest request = new InventoryRequest();
            request.setRequestId(10L);
            request.setSetId(1L);

            InventoryRequestItem item = new InventoryRequestItem();
            item.setItemId(1L);
            item.setRequestId(10L);
            item.setVariantId(100L);
            item.setQuantity(new BigDecimal("50")); // proposed = 50

            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(10L);
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("60")); // 60 > 50 → fail
            dto.setItems(List.of(itemDTO));

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(request));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 10L, 100L))
                    .thenReturn(BigDecimal.ZERO);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.recordReceipt(1L, dto, 1L));
            assertTrue(ex.getMessage().contains("Vượt quá số lượng đề xuất"));
        }

        @Test
        @DisplayName("Không phải STOCKKEEPER nhận hàng - thất bại")
        void recordReceipt_notStockkeeper_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

            ReceiptCreateDTO dto = new ReceiptCreateDTO();

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.recordReceipt(1L, dto, 2L));
            assertTrue(ex.getMessage().contains("Chỉ STOCKKEEPER mới có quyền ghi nhận nhận hàng"));
        }

        @Test
        @DisplayName("Nhận hàng khi trạng thái PENDING - thất bại")
        void recordReceipt_pendingStatus_throwsException() {
            when(requestSetRepository.findById(3L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));

            ReceiptCreateDTO dto = new ReceiptCreateDTO();

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.recordReceipt(3L, dto, 1L));
            assertTrue(ex.getMessage().contains("Chỉ có thể nhận hàng cho bộ phiếu đã duyệt"));
        }

        @Test
        @DisplayName("Request không thuộc bộ phiếu - thất bại")
        void recordReceipt_invalidRequest_throwsException() {
            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(999L); // Not in this set
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("10"));
            dto.setItems(List.of(itemDTO));

            InventoryRequest request = new InventoryRequest();
            request.setRequestId(10L);

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(request));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.recordReceipt(1L, dto, 1L));
            assertTrue(ex.getMessage().contains("không thuộc bộ phiếu"));
        }

        @Test
        @DisplayName("Variant không thuộc request - thất bại")
        void recordReceipt_invalidVariant_throwsException() {
            InventoryRequest request = new InventoryRequest();
            request.setRequestId(10L);

            InventoryRequestItem item = new InventoryRequestItem();
            item.setVariantId(100L);
            item.setRequestId(10L);
            item.setQuantity(new BigDecimal("50"));

            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(10L);
            itemDTO.setVariantId(999L); // Wrong variant
            itemDTO.setReceivedQuantity(new BigDecimal("10"));
            dto.setItems(List.of(itemDTO));

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(request));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.recordReceipt(1L, dto, 1L));
            assertTrue(ex.getMessage().contains("không thuộc request"));
        }
    }

    // ==================== VAI_GIAO_THO: MULTI-WORKER SAME VARIANT ====================

    @Nested
    @DisplayName("VAI_GIAO_THO — Multi-worker same variantId")
    class MultiWorkerReceiptTests {

        /**
         * Setup: request 44, 2 items cùng variantId=100
         *   item Hương: qty=5.8
         *   item Tú:    qty=30.6
         *   → proposed tổng = 36.4m
         */
        private InventoryRequest buildRequest() {
            InventoryRequest request = new InventoryRequest();
            request.setRequestId(44L);
            request.setSetId(1L);
            return request;
        }

        private List<InventoryRequestItem> buildTwoWorkerItems() {
            InventoryRequestItem huong = new InventoryRequestItem();
            huong.setItemId(573L);
            huong.setRequestId(44L);
            huong.setVariantId(100L);
            huong.setQuantity(new BigDecimal("5.8"));

            InventoryRequestItem tu = new InventoryRequestItem();
            tu.setItemId(574L);
            tu.setRequestId(44L);
            tu.setVariantId(100L);
            tu.setQuantity(new BigDecimal("30.6"));

            return List.of(huong, tu);
        }

        @Test
        @DisplayName("Nhận 23m — hợp lệ vì 23 ≤ tổng proposed 36.4")
        void recordReceipt_multiWorker_receiveLessThanTotal_success() {
            ReceiptRecord savedRecord = new ReceiptRecord();
            savedRecord.setReceiptId(6L);

            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(44L);
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("23"));
            dto.setItems(List.of(itemDTO));

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(buildRequest()));
            when(itemRepository.findByRequestId(44L)).thenReturn(buildTwoWorkerItems());
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 44L, 100L))
                    .thenReturn(BigDecimal.ZERO);
            when(receiptRecordRepository.save(any(ReceiptRecord.class))).thenReturn(savedRecord);

            assertDoesNotThrow(() -> receiptService.recordReceipt(1L, dto, 1L));
            verify(receiptItemRepository).save(any(ReceiptItem.class));
        }

        @Test
        @DisplayName("Nhận 35m lần 1 rồi nhận thêm 2m → tổng 37 > 36.4 → thất bại")
        void recordReceipt_multiWorker_exceedsTotalProposed_throwsException() {
            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(44L);
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("2")); // 35 + 2 = 37 > 36.4
            dto.setItems(List.of(itemDTO));

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(buildRequest()));
            when(itemRepository.findByRequestId(44L)).thenReturn(buildTwoWorkerItems());
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 44L, 100L))
                    .thenReturn(new BigDecimal("35")); // đã nhận 35

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.recordReceipt(1L, dto, 1L));
            assertTrue(ex.getMessage().contains("Vượt quá số lượng đề xuất"));
        }

        @Test
        @DisplayName("Nhận đúng số còn lại 1.4m → tổng 36.4 = proposed → thành công")
        void recordReceipt_multiWorker_receiveExactRemaining_success() {
            ReceiptRecord savedRecord = new ReceiptRecord();
            savedRecord.setReceiptId(7L);

            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(44L);
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("1.4")); // 35 + 1.4 = 36.4 = proposed
            dto.setItems(List.of(itemDTO));

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(buildRequest()));
            when(itemRepository.findByRequestId(44L)).thenReturn(buildTwoWorkerItems());
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 44L, 100L))
                    .thenReturn(new BigDecimal("35"));
            when(receiptRecordRepository.save(any(ReceiptRecord.class))).thenReturn(savedRecord);

            assertDoesNotThrow(() -> receiptService.recordReceipt(1L, dto, 1L));
        }

        @Test
        @DisplayName("Validation dùng SUM proposed — không chỉ item đầu tiên (findFirst bug)")
        void recordReceipt_multiWorker_usesSum_notFirstItem() {
            // Nếu code vẫn dùng findFirst(), proposed = 5.8 (Hương), nhận 10 > 5.8 → sẽ throw
            // Với fix sum-based: proposed = 36.4, nhận 10 ≤ 36.4 → OK
            ReceiptRecord savedRecord = new ReceiptRecord();
            savedRecord.setReceiptId(8L);

            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(44L);
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("10")); // 10 > 5.8 (Hương) nhưng ≤ 36.4
            dto.setItems(List.of(itemDTO));

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(buildRequest()));
            when(itemRepository.findByRequestId(44L)).thenReturn(buildTwoWorkerItems());
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 44L, 100L))
                    .thenReturn(BigDecimal.ZERO);
            when(receiptRecordRepository.save(any(ReceiptRecord.class))).thenReturn(savedRecord);

            // Phải PASS — nếu fail tức là code vẫn dùng findFirst()
            assertDoesNotThrow(() -> receiptService.recordReceipt(1L, dto, 1L));
        }
    }

    // ==================== COMPLETE RECEIPT ====================

    @Nested
    @DisplayName("Complete Receipt")
    class CompleteReceiptTests {

        @Test
        @DisplayName("Hoàn tất nhận hàng RECEIVING -> EXECUTED thành công")
        void completeReceipt_success() {
            InventoryRequest request = new InventoryRequest();
            request.setRequestId(10L);
            request.setRequestType(InventoryRequest.RequestType.ADJUST_IN);
            request.setProductId(1L);
            request.setWarehouseId(1L);

            InventoryRequestItem item = new InventoryRequestItem();
            item.setItemId(1L);
            item.setVariantId(100L);
            item.setQuantity(new BigDecimal("50"));

            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(receivingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(2L)).thenReturn(List.of(request));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(2L, 10L, 100L))
                    .thenReturn(new BigDecimal("30"));

            receiptService.completeReceipt(2L, 1L);

            assertEquals(RequestSetStatus.EXECUTED, receivingSet.getStatus());
            assertEquals(stockkeeperUser, receivingSet.getExecutedByUser());
            assertEquals(InventoryRequest.RequestType.IN, request.getRequestType());
            assertEquals(new BigDecimal("30"), item.getQuantity());
            verify(notificationService).notifyCreatorOfExecution(receivingSet, stockkeeperUser);
        }

        @Test
        @DisplayName("Hoàn tất nhận hàng - item không có receipt -> quantity = 0")
        void completeReceipt_noReceiptsForItem_quantityZero() {
            InventoryRequest request = new InventoryRequest();
            request.setRequestId(10L);
            request.setRequestType(InventoryRequest.RequestType.IN);
            request.setProductId(1L);

            InventoryRequestItem item = new InventoryRequestItem();
            item.setItemId(1L);
            item.setVariantId(100L);
            item.setQuantity(new BigDecimal("50"));

            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(receivingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(2L)).thenReturn(List.of(request));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(2L, 10L, 100L))
                    .thenReturn(null);

            receiptService.completeReceipt(2L, 1L);

            assertEquals(BigDecimal.ZERO, item.getQuantity());
        }

        @Test
        @DisplayName("Hoàn tất nhận hàng - ADJUST_OUT vượt tồn kho thất bại")
        void completeReceipt_adjustOutExceedsStock_throwsException() {
            InventoryRequest request = new InventoryRequest();
            request.setRequestId(10L);
            request.setRequestType(InventoryRequest.RequestType.ADJUST_OUT);
            request.setProductId(1L);
            request.setWarehouseId(1L);

            InventoryRequestItem item = new InventoryRequestItem();
            item.setItemId(1L);
            item.setVariantId(100L);
            item.setQuantity(new BigDecimal("50"));

            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(receivingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(2L)).thenReturn(List.of(request));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(2L, 10L, 100L))
                    .thenReturn(new BigDecimal("40"));
            when(inventoryRepository.getActualQuantityByVariantAndWarehouse(1L, 100L, 1L))
                    .thenReturn(new BigDecimal("20"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.completeReceipt(2L, 1L));
            assertTrue(ex.getMessage().contains("Không thể hoàn tất"));
        }

        @Test
        @DisplayName("Không phải STOCKKEEPER hoàn tất - thất bại")
        void completeReceipt_notStockkeeper_throwsException() {
            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(receivingSet));
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.completeReceipt(2L, 2L));
            assertTrue(ex.getMessage().contains("Chỉ STOCKKEEPER mới có quyền hoàn tất nhận hàng"));
        }

        @Test
        @DisplayName("Hoàn tất khi không phải RECEIVING - thất bại")
        void completeReceipt_notReceiving_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.completeReceipt(1L, 1L));
            assertTrue(ex.getMessage().contains("Chỉ có thể hoàn tất bộ phiếu đang trong trạng thái nhận hàng"));
        }
    }

    // ==================== GET PROGRESS ====================

    @Nested
    @DisplayName("Get Progress")
    class GetProgressTests {

        private InventoryRequest buildRequest(Long requestId, Long setId) {
            InventoryRequest r = new InventoryRequest();
            r.setRequestId(requestId);
            r.setSetId(setId);
            return r;
        }

        private ItemDetailDTO mockItem(Long variantId, String itemCode, String itemName, BigDecimal qty) {
            ItemDetailDTO d = mock(ItemDetailDTO.class);
            when(d.getVariantId()).thenReturn(variantId);
            when(d.getItemCode()).thenReturn(itemCode);
            when(d.getItemName()).thenReturn(itemName);
            when(d.getUnit()).thenReturn("mét");
            when(d.getQuantity()).thenReturn(qty);
            when(d.getStyleName()).thenReturn(null);
            when(d.getSizeValue()).thenReturn(null);
            when(d.getLengthCode()).thenReturn(null);
            when(d.getGender()).thenReturn(null);
            return d;
        }

        private InventoryRequestHeaderDTO mockHeader(Long requestId, String productName) {
            InventoryRequestHeaderDTO h = mock(InventoryRequestHeaderDTO.class);
            when(h.getUnitName()).thenReturn("Đơn vị test");
            when(h.getPositionCode()).thenReturn(null);
            when(h.getProductName()).thenReturn(productName);
            return h;
        }

        @Test
        @DisplayName("Set không tồn tại - thất bại")
        void getProgress_setNotFound_throwsException() {
            when(requestSetRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> receiptService.getProgress(99L));
            assertTrue(ex.getMessage().contains("không tồn tại"));
        }

        @Test
        @DisplayName("Chưa nhận hàng nào — tất cả zero, 1 ItemProgress per variant")
        void getProgress_noReceipts_allZero() {
            // 2 items cùng variantId=100 (Hương 5.8 + Tú 30.6 = 36.4)
            ItemDetailDTO item1 = mockItem(100L, "B21", "Cam LĐ Vnpost", new BigDecimal("5.8"));
            ItemDetailDTO item2 = mockItem(100L, "B21", "Cam LĐ Vnpost", new BigDecimal("30.6"));

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(buildRequest(44L, 1L)));
            when(requestRepository.findHeaderByRequestId(44L))
                    .thenReturn(Optional.of(mockHeader(44L, "VẢI 2026")));
            when(itemRepository.findItemDetailsByRequestId(44L)).thenReturn(List.of(item1, item2));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 44L, 100L))
                    .thenReturn(BigDecimal.ZERO);
            when(receiptItemRepository.findReceiptHistoryByVariant(1L, 44L, 100L))
                    .thenReturn(List.of());
            when(receiptRecordRepository.findTimelineBySetId(1L)).thenReturn(List.of());

            var result = receiptService.getProgress(1L);

            // 1 request, 1 ItemProgress (grouped by variantId)
            assertEquals(1, result.getRequests().size());
            var reqProgress = result.getRequests().get(0);
            assertEquals(1, reqProgress.getItems().size()); // KHÔNG phải 2

            var itemProgress = reqProgress.getItems().get(0);
            assertEquals(new BigDecimal("36.4"), itemProgress.getProposedQuantity());
            assertEquals(BigDecimal.ZERO, itemProgress.getTotalReceived());

            // Summary
            assertEquals(new BigDecimal("36.4"), result.getSummary().getTotalProposed());
            assertEquals(BigDecimal.ZERO, result.getSummary().getTotalReceived());
            assertEquals(0.0, result.getSummary().getOverallPercentage());
        }

        @Test
        @DisplayName("Đã nhận 35m — không double-count (core bug fix: 35 không thành 70)")
        void getProgress_afterReceipt_noDoubleCount() {
            // 2 items cùng variantId=100
            ItemDetailDTO item1 = mockItem(100L, "B21", "Cam LĐ Vnpost", new BigDecimal("5.8"));
            ItemDetailDTO item2 = mockItem(100L, "B21", "Cam LĐ Vnpost", new BigDecimal("30.6"));

            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(receivingSet));
            when(requestRepository.findBySetId(2L)).thenReturn(List.of(buildRequest(44L, 2L)));
            when(requestRepository.findHeaderByRequestId(44L))
                    .thenReturn(Optional.of(mockHeader(44L, "VẢI 2026")));
            when(itemRepository.findItemDetailsByRequestId(44L)).thenReturn(List.of(item1, item2));
            // Lookup chỉ gọi 1 lần cho variant 100 → trả về 35
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(2L, 44L, 100L))
                    .thenReturn(new BigDecimal("35"));
            when(receiptItemRepository.findReceiptHistoryByVariant(2L, 44L, 100L))
                    .thenReturn(List.of());
            when(receiptRecordRepository.findTimelineBySetId(2L)).thenReturn(List.of());

            var result = receiptService.getProgress(2L);

            // totalReceived PHẢI là 35, không phải 70 (double-count bug)
            assertEquals(new BigDecimal("35"), result.getSummary().getTotalReceived());

            // Remaining = 36.4 - 35 = 1.4
            var itemProgress = result.getRequests().get(0).getItems().get(0);
            assertEquals(new BigDecimal("35"), itemProgress.getTotalReceived());
            assertEquals(0, itemProgress.getRemainingQuantity()
                    .compareTo(new BigDecimal("1.4")));

            // Percentage ≈ 96.15%
            assertTrue(result.getSummary().getOverallPercentage() > 96.0);
            assertTrue(result.getSummary().getOverallPercentage() < 97.0);
        }

        @Test
        @DisplayName("Nhiều requests, nhiều variants — grand total tổng hợp đúng")
        void getProgress_multiRequestMultiVariant_correctGrandTotal() {
            InventoryRequest reqA = buildRequest(10L, 1L);
            InventoryRequest reqB = buildRequest(20L, 1L);

            ItemDetailDTO itemA = mockItem(1L, "V01", "Vải A", new BigDecimal("10"));
            ItemDetailDTO itemB = mockItem(2L, "V02", "Vải B", new BigDecimal("20"));

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(reqA, reqB));
            when(requestRepository.findHeaderByRequestId(10L))
                    .thenReturn(Optional.of(mockHeader(10L, "Sản phẩm A")));
            when(requestRepository.findHeaderByRequestId(20L))
                    .thenReturn(Optional.of(mockHeader(20L, "Sản phẩm B")));
            when(itemRepository.findItemDetailsByRequestId(10L)).thenReturn(List.of(itemA));
            when(itemRepository.findItemDetailsByRequestId(20L)).thenReturn(List.of(itemB));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 10L, 1L))
                    .thenReturn(new BigDecimal("8"));
            when(receiptItemRepository.getTotalReceivedByRequestAndVariant(1L, 20L, 2L))
                    .thenReturn(new BigDecimal("15"));
            when(receiptItemRepository.findReceiptHistoryByVariant(eq(1L), eq(10L), eq(1L)))
                    .thenReturn(List.of());
            when(receiptItemRepository.findReceiptHistoryByVariant(eq(1L), eq(20L), eq(2L)))
                    .thenReturn(List.of());
            when(receiptRecordRepository.findTimelineBySetId(1L)).thenReturn(List.of());

            var result = receiptService.getProgress(1L);

            assertEquals(0, result.getSummary().getTotalProposed().compareTo(new BigDecimal("30")));
            assertEquals(0, result.getSummary().getTotalReceived().compareTo(new BigDecimal("23")));
        }
    }

    // ==================== GET RECEIPTS ====================

    @Test
    @DisplayName("Lấy danh sách receipts - set không tồn tại")
    void getReceipts_setNotFound_throwsException() {
        when(requestSetRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> receiptService.getReceipts(99L));
        assertTrue(ex.getMessage().contains("RequestSet not found"));
    }
}
