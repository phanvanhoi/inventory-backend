package manage.store.inventory.service;

import java.time.LocalDateTime;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.dto.NotificationDTO;
import manage.store.inventory.entity.Notification;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.Role;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.repository.NotificationRepository;
import manage.store.inventory.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User adminUser1;
    private User adminUser2;
    private User regularUser;
    private User stockkeeperUser;
    private RequestSet requestSet;

    @BeforeEach
    void setUp() {
        Role adminRole = new Role();
        adminRole.setRoleId(1L);
        adminRole.setRoleName("ADMIN");

        adminUser1 = new User();
        adminUser1.setUserId(1L);
        adminUser1.setUsername("admin1");
        adminUser1.setFullName("Admin 1");
        Set<Role> roles1 = new HashSet<>();
        roles1.add(adminRole);
        adminUser1.setRoles(roles1);

        adminUser2 = new User();
        adminUser2.setUserId(2L);
        adminUser2.setUsername("admin2");
        adminUser2.setFullName("Admin 2");
        Set<Role> roles2 = new HashSet<>();
        roles2.add(adminRole);
        adminUser2.setRoles(roles2);

        Role userRole = new Role();
        userRole.setRoleId(2L);
        userRole.setRoleName("USER");

        regularUser = new User();
        regularUser.setUserId(3L);
        regularUser.setUsername("user");
        regularUser.setFullName("Regular User");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        regularUser.setRoles(userRoles);

        Role skRole = new Role();
        skRole.setRoleId(3L);
        skRole.setRoleName("STOCKKEEPER");

        stockkeeperUser = new User();
        stockkeeperUser.setUserId(4L);
        stockkeeperUser.setUsername("stockkeeper");
        stockkeeperUser.setFullName("Stockkeeper");
        Set<Role> skRoles = new HashSet<>();
        skRoles.add(skRole);
        stockkeeperUser.setRoles(skRoles);

        requestSet = new RequestSet();
        requestSet.setSetId(1L);
        requestSet.setSetName("Bộ phiếu test");
        requestSet.setStatus(RequestSetStatus.PENDING);
        requestSet.setCreatedByUser(regularUser);
    }

    // ==================== NOTIFY ADMINS ====================

    @Nested
    @DisplayName("Notify Admins of Pending Approval")
    class NotifyAdminsTests {

        @Test
        @DisplayName("Thông báo tất cả admin trừ người submit")
        void notifyAdmins_excludesSubmitter() {
            when(userRepository.findByRoleName("ADMIN")).thenReturn(List.of(adminUser1, adminUser2));

            notificationService.notifyAdminsOfPendingApproval(requestSet, regularUser);

            // Both admins get notified (neither is submitter)
            verify(notificationRepository, times(2)).save(any(Notification.class));
        }

        @Test
        @DisplayName("Bỏ qua admin là chính người submit")
        void notifyAdmins_skipsSelfNotification() {
            // Admin1 is the submitter
            when(userRepository.findByRoleName("ADMIN")).thenReturn(List.of(adminUser1, adminUser2));

            notificationService.notifyAdminsOfPendingApproval(requestSet, adminUser1);

            // Only admin2 gets notified
            verify(notificationRepository, times(1)).save(any(Notification.class));
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertEquals(2L, captor.getValue().getUser().getUserId());
        }

        @Test
        @DisplayName("Không có admin nào - không lưu thông báo")
        void notifyAdmins_noAdmins_savesNothing() {
            when(userRepository.findByRoleName("ADMIN")).thenReturn(List.of());

            notificationService.notifyAdminsOfPendingApproval(requestSet, regularUser);

            verify(notificationRepository, never()).save(any());
        }
    }

    // ==================== NOTIFY CREATOR ====================

    @Nested
    @DisplayName("Notify Creator")
    class NotifyCreatorTests {

        @Test
        @DisplayName("Thông báo creator khi phiếu được duyệt")
        void notifyCreatorOfApproval_savesNotification() {
            notificationService.notifyCreatorOfApproval(requestSet, adminUser1);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertEquals(regularUser, saved.getUser());
            assertEquals("Bộ phiếu đã được duyệt", saved.getTitle());
            assertTrue(saved.getMessage().contains("Admin 1"));
        }

        @Test
        @DisplayName("Creator null - không làm gì")
        void notifyCreatorOfApproval_nullCreator_doesNothing() {
            requestSet.setCreatedByUser(null);

            notificationService.notifyCreatorOfApproval(requestSet, adminUser1);

            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Thông báo từ chối có lý do")
        void notifyCreatorOfRejection_includesReason() {
            notificationService.notifyCreatorOfRejection(requestSet, adminUser1, "Số lượng sai");

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertTrue(captor.getValue().getMessage().contains("Số lượng sai"));
            assertEquals("Bộ phiếu bị từ chối", captor.getValue().getTitle());
        }

        @Test
        @DisplayName("Thông báo khi phiếu được thực hiện")
        void notifyCreatorOfExecution_savesNotification() {
            notificationService.notifyCreatorOfExecution(requestSet, stockkeeperUser);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertEquals("Bộ phiếu đã được thực hiện", captor.getValue().getTitle());
            assertTrue(captor.getValue().getMessage().contains("Stockkeeper"));
        }
    }

    // ==================== NOTIFY STOCKKEEPER ====================

    @Test
    @DisplayName("Thông báo tất cả stockkeeper khi phiếu được duyệt")
    void notifyStockkeeperOfApproval_notifiesAllStockkeepers() {
        when(userRepository.findByRoleName("STOCKKEEPER")).thenReturn(List.of(stockkeeperUser));

        notificationService.notifyStockkeeperOfApproval(requestSet, adminUser1);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ==================== EDIT AND RECEIVE ====================

    @Nested
    @DisplayName("Notify Edit and Receive")
    class EditAndReceiveTests {

        @Test
        @DisplayName("Thông báo creator và admins khi sửa và nhận hàng")
        void notifyOfEditAndReceive_notifiesCreatorAndAdmins() {
            when(userRepository.findByRoleName("ADMIN")).thenReturn(List.of(adminUser1));

            notificationService.notifyOfEditAndReceive(requestSet, stockkeeperUser, "Sửa số lượng");

            // Creator + 1 admin = 2 notifications
            verify(notificationRepository, times(2)).save(any(Notification.class));
        }

        @Test
        @DisplayName("Bỏ qua stockkeeper nếu là creator")
        void notifyOfEditAndReceive_skipsStockkeeperIfCreator() {
            requestSet.setCreatedByUser(stockkeeperUser); // stockkeeper tự tạo
            when(userRepository.findByRoleName("ADMIN")).thenReturn(List.of(adminUser1));

            notificationService.notifyOfEditAndReceive(requestSet, stockkeeperUser, "Sửa");

            // Only admin gets notified (creator = stockkeeper, skip self)
            verify(notificationRepository, times(1)).save(any(Notification.class));
        }
    }

    // ==================== READ OPERATIONS ====================

    @Nested
    @DisplayName("Read Operations")
    class ReadOperationsTests {

        @Test
        @DisplayName("Lấy danh sách notifications")
        void getNotifications_returnsMappedDTOs() {
            Notification n = new Notification();
            n.setNotificationId(1L);
            n.setTitle("Test");
            n.setMessage("Message");
            n.setIsRead(false);
            n.setRelatedSet(requestSet);
            n.setCreatedAt(LocalDateTime.now());

            when(notificationRepository.findByUserUserIdOrderByCreatedAtDesc(3L))
                    .thenReturn(List.of(n));

            List<NotificationDTO> result = notificationService.getNotifications(3L);

            assertEquals(1, result.size());
            assertEquals("Test", result.get(0).getTitle());
            assertEquals(1L, result.get(0).getRelatedSetId());
        }

        @Test
        @DisplayName("Đếm số notification chưa đọc")
        void countUnread_delegatesToRepository() {
            when(notificationRepository.countByUserUserIdAndIsReadFalse(3L)).thenReturn(5L);

            long count = notificationService.countUnread(3L);

            assertEquals(5L, count);
        }

        @Test
        @DisplayName("Đánh dấu đã đọc 1 notification")
        void markAsRead_setsIsReadTrue() {
            Notification n = new Notification();
            n.setNotificationId(1L);
            n.setIsRead(false);

            when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

            notificationService.markAsRead(1L);

            assertTrue(n.getIsRead());
            verify(notificationRepository).save(n);
        }

        @Test
        @DisplayName("Đánh dấu đã đọc tất cả")
        void markAllAsRead_updatesAllUnread() {
            Notification n1 = new Notification();
            n1.setIsRead(false);
            Notification n2 = new Notification();
            n2.setIsRead(false);

            when(notificationRepository.findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(3L))
                    .thenReturn(List.of(n1, n2));

            notificationService.markAllAsRead(3L);

            assertTrue(n1.getIsRead());
            assertTrue(n2.getIsRead());
            verify(notificationRepository).saveAll(List.of(n1, n2));
        }
    }
}
