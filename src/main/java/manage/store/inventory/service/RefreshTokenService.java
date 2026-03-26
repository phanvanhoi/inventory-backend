package manage.store.inventory.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.entity.RefreshToken;
import manage.store.inventory.entity.User;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:2592000000}") // 30 ngày mặc định
    private long refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        return refreshTokenRepository.save(token);
    }

    public RefreshToken validateRefreshToken(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new BusinessException("Refresh token không hợp lệ"));

        if (token.isRevoked()) {
            throw new BusinessException("Refresh token đã bị thu hồi");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }

        return token;
    }

    @Transactional
    public void revokeToken(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
