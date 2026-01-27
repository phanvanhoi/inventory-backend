package manage.store.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
    private List<String> roles;
}
