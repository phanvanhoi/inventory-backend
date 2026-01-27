package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long notificationId;
    private String title;
    private String message;
    private Boolean isRead;
    private Long relatedSetId;
    private LocalDateTime createdAt;
}
