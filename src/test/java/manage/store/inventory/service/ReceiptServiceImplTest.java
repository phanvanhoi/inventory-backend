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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.dto.ReceiptCreateDTO;
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
        // Stockkeeper user
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

        // Regular user
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

        // Request sets
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

            ReceiptCreateDTO dto = new ReceiptCreateDTO();
            dto.setNote("Lần nhận 1");
            ReceiptCreateDTO.ReceiptItemDTO itemDTO = new ReceiptCreateDTO.ReceiptItemDTO();
            itemDTO.setRequestId(10L);
            itemDTO.setVariantId(100L);
            itemDTO.setReceivedQuantity(new BigDecimal("20"));
            dto.setItems(List.of(itemDTO));

            ReceiptRecord savedRecord = new ReceiptRecord();
            savedRecord.setReceiptId(1L);

            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(1L)).thenReturn(List.of(request));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
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
            when(receiptRecordRepository.save(any(ReceiptRecord.class))).thenReturn(savedRecord);

            receiptService.recordReceipt(2L, dto, 1L);

            // RECEIVING stays RECEIVING
            assertEquals(RequestSetStatus.RECEIVING, receivingSet.getStatus());
            verify(requestSetRepository, never()).save(receivingSet);
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
            // ADJUST_IN -> IN
            assertEquals(InventoryRequest.RequestType.IN, request.getRequestType());
            // Quantity updated to total received
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
                    .thenReturn(null); // No receipts

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
                    .thenReturn(new BigDecimal("20")); // Only 20 in stock, but received 40

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

    // ==================== GET RECEIPTS ====================

    @Test
    @DisplayName("Lấy danh sách receipts - set không tồn tại")
    void getReceipts_setNotFound_throwsException() {
        when(requestSetRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> receiptService.getReceipts(99L));
        assertTrue(ex.getMessage().contains("RequestSet not found"));
    }

    @Test
    @DisplayName("Lấy progress - set không tồn tại")
    void getProgress_setNotFound_throwsException() {
        when(requestSetRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> receiptService.getProgress(99L));
        assertTrue(ex.getMessage().contains("RequestSet not found"));
    }
}
