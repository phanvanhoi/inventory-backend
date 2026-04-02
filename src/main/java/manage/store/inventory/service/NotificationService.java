package manage.store.inventory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.NotificationDTO;
import manage.store.inventory.entity.Notification;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.User;
import manage.store.inventory.repository.NotificationRepository;
import manage.store.inventory.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            PushNotificationService pushService
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.pushService = pushService;
    }

    @Transactional
    public void notifyAdminsOfPendingApproval(RequestSet requestSet, User submitter) {
        List<User> admins = userRepository.findByRoleName("ADMIN");

        for (User admin : admins) {
            if (!admin.getUserId().equals(submitter.getUserId())) {
                Notification notification = new Notification();
                notification.setUser(admin);
                notification.setTitle("Bộ phiếu chờ duyệt");
                notification.setMessage(String.format(
                        "Bộ phiếu '%s' đã được submit bởi %s và đang chờ duyệt.",
                        requestSet.getSetName(),
                        submitter.getFullName()
                ));
                notification.setRelatedSet(requestSet);
                notification.setCreatedAt(LocalDateTime.now());
                notification.setIsRead(false);

                saveAndPush(notification);
            }
        }
    }

    @Transactional
    public void notifyCreatorOfApproval(RequestSet requestSet, User approver) {
        User creator = requestSet.getCreatedByUser();
        if (creator == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(creator);
        notification.setTitle("Bộ phiếu đã được duyệt");
        notification.setMessage(String.format(
                "Bộ phiếu '%s' đã được duyệt bởi %s.",
                requestSet.getSetName(),
                approver.getFullName()
        ));
        notification.setRelatedSet(requestSet);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        saveAndPush(notification);
    }

    @Transactional
    public void notifyCreatorOfRejection(RequestSet requestSet, User rejecter, String reason) {
        User creator = requestSet.getCreatedByUser();
        if (creator == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(creator);
        notification.setTitle("Bộ phiếu bị từ chối");
        notification.setMessage(String.format(
                "Bộ phiếu '%s' đã bị từ chối bởi %s. Lý do: %s",
                requestSet.getSetName(),
                rejecter.getFullName(),
                reason
        ));
        notification.setRelatedSet(requestSet);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        saveAndPush(notification);
    }

    @Transactional
    public void notifyCreatorOfExecution(RequestSet requestSet, User executor) {
        User creator = requestSet.getCreatedByUser();
        if (creator == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(creator);
        notification.setTitle("Bộ phiếu đã được thực hiện");
        notification.setMessage(String.format(
                "Bộ phiếu '%s' đã được thực hiện nhập/xuất kho bởi %s.",
                requestSet.getSetName(),
                executor.getFullName()
        ));
        notification.setRelatedSet(requestSet);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        saveAndPush(notification);
    }

    @Transactional
    public void notifyStockkeeperOfApproval(RequestSet requestSet, User approver) {
        List<User> stockkeepers = userRepository.findByRoleName("STOCKKEEPER");

        for (User stockkeeper : stockkeepers) {
            Notification notification = new Notification();
            notification.setUser(stockkeeper);
            notification.setTitle("Bộ phiếu chờ thực hiện");
            notification.setMessage(String.format(
                    "Bộ phiếu '%s' đã được duyệt bởi %s và đang chờ thực hiện nhập/xuất kho.",
                    requestSet.getSetName(),
                    approver.getFullName()
            ));
            notification.setRelatedSet(requestSet);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);

            saveAndPush(notification);
        }
    }

    @Transactional
    public void notifyOfEditAndReceive(RequestSet requestSet, User stockkeeper, String reason) {
        String message = String.format(
                "Thủ kho %s đã sửa số lượng bộ phiếu '%s' và gửi lại để duyệt. Lý do: %s",
                stockkeeper.getFullName(),
                requestSet.getSetName(),
                reason
        );

        // Thông báo cho Creator
        User creator = requestSet.getCreatedByUser();
        if (creator != null && !creator.getUserId().equals(stockkeeper.getUserId())) {
            Notification notiCreator = new Notification();
            notiCreator.setUser(creator);
            notiCreator.setTitle("Bộ phiếu đã bị sửa SL — cần duyệt lại");
            notiCreator.setMessage(message);
            notiCreator.setRelatedSet(requestSet);
            notiCreator.setCreatedAt(LocalDateTime.now());
            notiCreator.setIsRead(false);
            notiCreator.setIsUrgent(true);
            saveAndPush(notiCreator);
        }

        // Thông báo cho tất cả ADMIN (cần duyệt lại)
        List<User> admins = userRepository.findByRoleName("ADMIN");
        for (User admin : admins) {
            if (!admin.getUserId().equals(stockkeeper.getUserId())) {
                Notification notiAdmin = new Notification();
                notiAdmin.setUser(admin);
                notiAdmin.setTitle("Bộ phiếu đã bị sửa SL — cần duyệt lại");
                notiAdmin.setMessage(message);
                notiAdmin.setRelatedSet(requestSet);
                notiAdmin.setCreatedAt(LocalDateTime.now());
                notiAdmin.setIsRead(false);
                notiAdmin.setIsUrgent(true);
                saveAndPush(notiAdmin);
            }
        }
    }

    @Transactional
    public void notifyAdminsOfRateChange(RequestSet requestSet, User submitter, List<String> modifiedItems) {
        String itemList = String.join("; ", modifiedItems);
        notifyAdminsExcluding(submitter,
                "[KHẨN] Định mức phụ liệu bị thay đổi",
                String.format("Bộ phiếu '%s' do %s tạo có định mức phụ liệu bị thay đổi so với template: %s",
                        requestSet.getSetName(), submitter.getFullName(), itemList),
                requestSet, true);
    }

    private Notification buildNotification(User recipient, String title, String message,
                                           RequestSet relatedSet, boolean urgent) {
        Notification n = new Notification();
        n.setUser(recipient);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedSet(relatedSet);
        n.setCreatedAt(LocalDateTime.now());
        n.setIsRead(false);
        n.setIsUrgent(urgent);
        return n;
    }

    private void notifyAdminsExcluding(User excluded, String title, String message,
                                        RequestSet relatedSet, boolean urgent) {
        List<User> admins = userRepository.findByRoleName("ADMIN");
        for (User admin : admins) {
            if (!admin.getUserId().equals(excluded.getUserId())) {
                saveAndPush(buildNotification(admin, title, message, relatedSet, urgent));
            }
        }
    }

    private void saveAndPush(Notification notification) {
        notificationRepository.save(notification);
        Long userId = notification.getUser().getUserId();
        String title = notification.getTitle();
        String message = notification.getMessage();
        Long setId = notification.getRelatedSet() != null ? notification.getRelatedSet().getSetId() : null;
        // Send push after transaction commits to avoid notifying for rolled-back data
        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        pushService.sendToUser(userId, title, message, setId);
                    }
                });
    }

    public List<NotificationDTO> getNotifications(Long userId) {
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId)
                .ifPresent(n -> {
                    // Chỉ owner mới được đánh dấu đã đọc
                    if (!n.getUser().getUserId().equals(userId)) return;
                    n.setIsRead(true);
                    notificationRepository.save(n);
                });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository
                .findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    private NotificationDTO toDTO(Notification n) {
        return new NotificationDTO(
                n.getNotificationId(),
                n.getTitle(),
                n.getMessage(),
                n.getIsRead(),
                n.getIsUrgent(),
                n.getRelatedSet() != null ? n.getRelatedSet().getSetId() : null,
                n.getCreatedAt()
        );
    }
}
