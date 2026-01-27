package manage.store.inventory.security;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String username;
    private List<String> roles;

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isPurchaser() {
        return hasRole("PURCHASER");
    }

    public boolean isStockkeeper() {
        return hasRole("STOCKKEEPER");
    }

    public boolean isUser() {
        return hasRole("USER");
    }
}
