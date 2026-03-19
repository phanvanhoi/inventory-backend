package manage.store.inventory.dto.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String fullName;
    private List<String> roles;
    private Long warehouseId;

    public AuthResponseDTO(String token, Long userId, String username, String fullName, List<String> roles, Long warehouseId) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.roles = roles;
        this.warehouseId = warehouseId;
    }
}
