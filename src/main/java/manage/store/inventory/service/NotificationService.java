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

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
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

                notificationRepository.save(notification);
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

        notificationRepository.save(notification);
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

        notificationRepository.save(notification);
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

        notificationRepository.save(notification);
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

            notificationRepository.save(notification);
        }
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
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(n -> {
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
                n.getRelatedSet() != null ? n.getRelatedSet().getSetId() : null,
                n.getCreatedAt()
        );
    }
}
