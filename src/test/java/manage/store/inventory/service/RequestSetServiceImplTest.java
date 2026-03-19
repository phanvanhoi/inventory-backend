package manage.store.inventory.service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.entity.ApprovalHistory;
import manage.store.inventory.entity.InventoryRequest;
import manage.store.inventory.entity.InventoryRequestItem;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.Role;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.repository.ApprovalHistoryRepository;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.InventoryRequestRepository;
import manage.store.inventory.repository.PositionRepository;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.repository.ProductVariantRepository;
import manage.store.inventory.repository.RequestSetRepository;
import manage.store.inventory.repository.UserRepository;
import manage.store.inventory.repository.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class RequestSetServiceImplTest {

    @Mock private RequestSetRepository requestSetRepository;
    @Mock private InventoryRequestRepository requestRepository;
    @Mock private InventoryRequestItemRepository itemRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventoryRequestService inventoryRequestService;
    @Mock private ApprovalHistoryRepository approvalHistoryRepository;
    @Mock private NotificationService notificationService;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private WarehouseRepository warehouseRepository;

    @InjectMocks
    private RequestSetServiceImpl requestSetService;

    private User adminUser;
    private User regularUser;
    private User stockkeeperUser;
    private User purchaserUser;
    private RequestSet pendingSet;
    private RequestSet approvedSet;
    private RequestSet rejectedSet;
    private RequestSet receivingSet;

    @BeforeEach
    void setUp() {
        // Admin user
        Role adminRole = new Role();
        adminRole.setRoleId(1L);
        adminRole.setRoleName("ADMIN");

        adminUser = new User();
        adminUser.setUserId(1L);
        adminUser.setUsername("admin");
        adminUser.setFullName("Admin User");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);

        // Regular user
        Role userRole = new Role();
        userRole.setRoleId(2L);
        userRole.setRoleName("USER");

        regularUser = new User();
        regularUser.setUserId(2L);
        regularUser.setUsername("user");
        regularUser.setFullName("Regular User");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        regularUser.setRoles(userRoles);

        // Stockkeeper user
        Role stockkeeperRole = new Role();
        stockkeeperRole.setRoleId(3L);
        stockkeeperRole.setRoleName("STOCKKEEPER");

        stockkeeperUser = new User();
        stockkeeperUser.setUserId(3L);
        stockkeeperUser.setUsername("stockkeeper");
        stockkeeperUser.setFullName("Stockkeeper User");
        Set<Role> stockkeeperRoles = new HashSet<>();
        stockkeeperRoles.add(stockkeeperRole);
        stockkeeperUser.setRoles(stockkeeperRoles);

        // Purchaser user
        Role purchaserRole = new Role();
        purchaserRole.setRoleId(4L);
        purchaserRole.setRoleName("PURCHASER");

        purchaserUser = new User();
        purchaserUser.setUserId(4L);
        purchaserUser.setUsername("purchaser");
        purchaserUser.setFullName("Purchaser User");
        Set<Role> purchaserRoles = new HashSet<>();
        purchaserRoles.add(purchaserRole);
        purchaserUser.setRoles(purchaserRoles);

        // Request sets
        pendingSet = new RequestSet();
        pendingSet.setSetId(1L);
        pendingSet.setSetName("Bộ phiếu test");
        pendingSet.setStatus(RequestSetStatus.PENDING);
        pendingSet.setCreatedByUser(regularUser);

        approvedSet = new RequestSet();
        approvedSet.setSetId(2L);
        approvedSet.setSetName("Bộ phiếu approved");
        approvedSet.setStatus(RequestSetStatus.APPROVED);
        approvedSet.setCreatedByUser(regularUser);

        rejectedSet = new RequestSet();
        rejectedSet.setSetId(3L);
        rejectedSet.setSetName("Bộ phiếu rejected");
        rejectedSet.setStatus(RequestSetStatus.REJECTED);
        rejectedSet.setCreatedByUser(regularUser);

        receivingSet = new RequestSet();
        receivingSet.setSetId(4L);
        receivingSet.setSetName("Bộ phiếu receiving");
        receivingSet.setStatus(RequestSetStatus.RECEIVING);
        receivingSet.setCreatedByUser(regularUser);
    }

    // ==================== APPROVE ====================

    @Nested
    @DisplayName("Approve Request Set")
    class ApproveTests {

        @Test
        @DisplayName("Admin duyệt bộ phiếu PENDING thành công")
        void approve_success() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            requestSetService.approve(1L, 1L);

            assertEquals(RequestSetStatus.APPROVED, pendingSet.getStatus());
            verify(requestSetRepository).save(pendingSet);
            verify(approvalHistoryRepository).save(any(ApprovalHistory.class));
            verify(notificationService).notifyCreatorOfApproval(pendingSet, adminUser);
            verify(notificationService).notifyStockkeeperOfApproval(pendingSet, adminUser);
        }

        @Test
        @DisplayName("Không phải ADMIN duyệt - thất bại")
        void approve_notAdmin_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.approve(1L, 2L));
            assertTrue(ex.getMessage().contains("Chỉ ADMIN mới có quyền duyệt"));
        }

        @Test
        @DisplayName("Tự duyệt phiếu của mình - thất bại")
        void approve_selfApproval_throwsException() {
            pendingSet.setCreatedByUser(adminUser); // Admin tự tạo
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.approve(1L, 1L));
            assertTrue(ex.getMessage().contains("Không thể tự duyệt"));
        }

        @Test
        @DisplayName("Duyệt phiếu không ở trạng thái PENDING - thất bại")
        void approve_notPending_throwsException() {
            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.approve(2L, 1L));
            assertTrue(ex.getMessage().contains("Chỉ có thể duyệt bộ phiếu đang chờ duyệt"));
        }
    }

    // ==================== REJECT ====================

    @Nested
    @DisplayName("Reject Request Set")
    class RejectTests {

        @Test
        @DisplayName("Admin từ chối bộ phiếu PENDING thành công")
        void reject_success() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            requestSetService.reject(1L, 1L, "Số lượng không hợp lý");

            assertEquals(RequestSetStatus.REJECTED, pendingSet.getStatus());
            verify(requestSetRepository).save(pendingSet);
            verify(approvalHistoryRepository).save(any(ApprovalHistory.class));
            verify(notificationService).notifyCreatorOfRejection(eq(pendingSet), eq(adminUser), eq("Số lượng không hợp lý"));
        }

        @Test
        @DisplayName("Không phải ADMIN từ chối - thất bại")
        void reject_notAdmin_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.reject(1L, 2L, "Reason"));
            assertTrue(ex.getMessage().contains("Chỉ ADMIN mới có quyền từ chối"));
        }

        @Test
        @DisplayName("Từ chối phiếu không PENDING - thất bại")
        void reject_notPending_throwsException() {
            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.reject(2L, 1L, "Reason"));
            assertTrue(ex.getMessage().contains("Chỉ có thể từ chối bộ phiếu đang chờ duyệt"));
        }

        @Test
        @DisplayName("Từ chối không có lý do - thất bại")
        void reject_noReason_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.reject(1L, 1L, ""));
            assertTrue(ex.getMessage().contains("Phải có lý do khi từ chối"));
        }

        @Test
        @DisplayName("Từ chối với reason null - thất bại")
        void reject_nullReason_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.reject(1L, 1L, null));
            assertTrue(ex.getMessage().contains("Phải có lý do khi từ chối"));
        }
    }

    // ==================== EXECUTE ====================

    @Nested
    @DisplayName("Execute Request Set")
    class ExecuteTests {

        @Test
        @DisplayName("STOCKKEEPER execute bộ phiếu APPROVED thành công (IN requests)")
        void execute_withInRequests_success() {
            InventoryRequest inRequest = new InventoryRequest();
            inRequest.setRequestId(10L);
            inRequest.setRequestType(InventoryRequest.RequestType.IN);
            inRequest.setProductId(1L);
            inRequest.setWarehouseId(1L);

            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(3L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(2L)).thenReturn(List.of(inRequest));

            requestSetService.execute(2L, 3L);

            assertEquals(RequestSetStatus.EXECUTED, approvedSet.getStatus());
            assertEquals(stockkeeperUser, approvedSet.getExecutedByUser());
            assertNotNull(approvedSet.getExecutedAt());
            verify(approvalHistoryRepository).save(any(ApprovalHistory.class));
            verify(notificationService).notifyCreatorOfExecution(approvedSet, stockkeeperUser);
        }

        @Test
        @DisplayName("STOCKKEEPER execute - ADJUST_IN chuyển thành IN")
        void execute_adjustInConvertsToIn() {
            InventoryRequest adjustIn = new InventoryRequest();
            adjustIn.setRequestId(10L);
            adjustIn.setRequestType(InventoryRequest.RequestType.ADJUST_IN);
            adjustIn.setProductId(1L);
            adjustIn.setWarehouseId(1L);

            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(3L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(2L)).thenReturn(List.of(adjustIn));

            requestSetService.execute(2L, 3L);

            assertEquals(InventoryRequest.RequestType.IN, adjustIn.getRequestType());
            verify(requestRepository).save(adjustIn);
        }

        @Test
        @DisplayName("STOCKKEEPER execute - ADJUST_OUT vượt tồn kho thất bại")
        void execute_adjustOutExceedsStock_throwsException() {
            InventoryRequest adjustOut = new InventoryRequest();
            adjustOut.setRequestId(10L);
            adjustOut.setRequestType(InventoryRequest.RequestType.ADJUST_OUT);
            adjustOut.setProductId(1L);
            adjustOut.setWarehouseId(1L);

            InventoryRequestItem item = new InventoryRequestItem();
            item.setItemId(1L);
            item.setVariantId(100L);
            item.setQuantity(new BigDecimal("50"));

            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(3L)).thenReturn(Optional.of(stockkeeperUser));
            when(requestRepository.findBySetId(2L)).thenReturn(List.of(adjustOut));
            when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
            when(inventoryRepository.getActualQuantityByVariantAndWarehouse(1L, 100L, 1L))
                    .thenReturn(new BigDecimal("30")); // Only 30 in stock

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.execute(2L, 3L));
            assertTrue(ex.getMessage().contains("Không thể xuất kho"));
            assertTrue(ex.getMessage().contains("vượt quá tồn kho thực tế"));
        }

        @Test
        @DisplayName("Không phải STOCKKEEPER execute - thất bại")
        void execute_notStockkeeper_throwsException() {
            when(requestSetRepository.findById(2L)).thenReturn(Optional.of(approvedSet));
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.execute(2L, 2L));
            assertTrue(ex.getMessage().contains("Chỉ STOCKKEEPER mới có quyền"));
        }

        @Test
        @DisplayName("Execute phiếu không APPROVED - thất bại")
        void execute_notApproved_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(3L)).thenReturn(Optional.of(stockkeeperUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.execute(1L, 3L));
            assertTrue(ex.getMessage().contains("Chỉ có thể xác nhận thực hiện bộ phiếu đã được duyệt"));
        }
    }

    // ==================== SUBMIT FOR APPROVAL ====================

    @Nested
    @DisplayName("Submit For Approval")
    class SubmitTests {

        @Test
        @DisplayName("Submit lại phiếu bị REJECTED thành công")
        void submitForApproval_fromRejected_success() {
            when(requestSetRepository.findById(3L)).thenReturn(Optional.of(rejectedSet));
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

            requestSetService.submitForApproval(3L, 2L);

            assertEquals(RequestSetStatus.PENDING, rejectedSet.getStatus());
            assertNotNull(rejectedSet.getSubmittedAt());
            verify(approvalHistoryRepository).save(any(ApprovalHistory.class));
            verify(notificationService).notifyAdminsOfPendingApproval(rejectedSet, regularUser);
        }

        @Test
        @DisplayName("Submit phiếu không phải REJECTED - thất bại")
        void submitForApproval_notRejected_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.submitForApproval(1L, 2L));
            assertTrue(ex.getMessage().contains("Chỉ có thể submit lại bộ phiếu đã bị từ chối"));
        }
    }

    // ==================== UPDATE REQUEST SET ====================

    @Nested
    @DisplayName("Update Request Set")
    class UpdateTests {

        @Test
        @DisplayName("Cập nhật phiếu REJECTED - không phải chủ phiếu thất bại")
        void updateRequestSet_notOwner_throwsException() {
            when(requestSetRepository.findById(3L)).thenReturn(Optional.of(rejectedSet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.updateRequestSet(3L, null, 1L));
            assertTrue(ex.getMessage().contains("Chỉ chủ phiếu mới có quyền cập nhật"));
        }

        @Test
        @DisplayName("Cập nhật phiếu không REJECTED - thất bại")
        void updateRequestSet_notRejected_throwsException() {
            when(requestSetRepository.findById(1L)).thenReturn(Optional.of(pendingSet));
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> requestSetService.updateRequestSet(1L, null, 2L));
            assertTrue(ex.getMessage().contains("Chỉ có thể cập nhật bộ phiếu đã bị từ chối"));
        }
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("Xóa bộ phiếu thành công")
    void deleteRequestSet_success() {
        InventoryRequest request = new InventoryRequest();
        request.setRequestId(10L);

        when(requestRepository.findBySetId(1L)).thenReturn(List.of(request));

        requestSetService.deleteRequestSet(1L, null);

        verify(itemRepository).deleteByRequestId(10L);
        verify(requestRepository).deleteAll(List.of(request));
        verify(requestSetRepository).deleteById(1L);
    }
}
