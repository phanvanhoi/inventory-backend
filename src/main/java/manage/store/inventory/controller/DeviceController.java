package manage.store.inventory.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.dto.DeviceRegisterDTO;
import manage.store.inventory.entity.DeviceToken;
import manage.store.inventory.repository.DeviceTokenRepository;
import manage.store.inventory.security.UserPrincipal;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceController(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Void> register(
            @Valid @RequestBody DeviceRegisterDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Upsert: delete old entry if exists, then insert with current user
        deviceTokenRepository.deleteByPushToken(dto.getPushToken());

        DeviceToken token = new DeviceToken();
        token.setUserId(principal.getUserId());
        token.setPushToken(dto.getPushToken());
        token.setPlatform(dto.getPlatform() != null ? dto.getPlatform() : "UNKNOWN");
        token.setCreatedAt(LocalDateTime.now());
        deviceTokenRepository.save(token);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unregister")
    @Transactional
    public ResponseEntity<Void> unregister(
            @Valid @RequestBody DeviceRegisterDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Only delete tokens owned by the authenticated user
        deviceTokenRepository.deleteByPushTokenAndUserId(dto.getPushToken(), principal.getUserId());
        return ResponseEntity.ok().build();
    }
}
