package manage.store.inventory.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public UserPrincipal get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new RuntimeException("User chưa đăng nhập");
    }

    public Long getUserId() {
        return get().getUserId();
    }

    public String getUsername() {
        return get().getUsername();
    }

    public boolean isAdmin() {
        return get().isAdmin();
    }

    public boolean isPurchaser() {
        return get().isPurchaser();
    }

    public boolean isStockkeeper() {
        return get().isStockkeeper();
    }

    public boolean isUser() {
        return get().isUser();
    }
}
