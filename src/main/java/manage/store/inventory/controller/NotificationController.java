package manage.store.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.dto.NotificationDTO;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService notificationService, CurrentUser currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    // Lấy tất cả thông báo của user hiện tại
    @GetMapping
    public List<NotificationDTO> getNotifications() {
        return notificationService.getNotifications(currentUser.getUserId());
    }

    // Lấy thông báo chưa đọc của user hiện tại
    @GetMapping("/unread")
    public List<NotificationDTO> getUnreadNotifications() {
        return notificationService.getUnreadNotifications(currentUser.getUserId());
    }

    // Đếm số thông báo chưa đọc
    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread() {
        return ResponseEntity.ok(notificationService.countUnread(currentUser.getUserId()));
    }

    // Đánh dấu thông báo đã đọc
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // Đánh dấu tất cả đã đọc
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(currentUser.getUserId());
        return ResponseEntity.ok().build();
    }
}
