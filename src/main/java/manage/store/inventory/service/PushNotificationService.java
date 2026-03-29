package manage.store.inventory.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import manage.store.inventory.entity.DeviceToken;
import manage.store.inventory.repository.DeviceTokenRepository;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final DeviceTokenRepository deviceTokenRepository;
    private final RestTemplate restTemplate;

    public PushNotificationService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send push notification to all devices of a specific user.
     */
    @Async
    public void sendToUser(Long userId, String title, String body, Long relatedSetId) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) return;

        List<Map<String, Object>> messages = new ArrayList<>();
        for (DeviceToken dt : tokens) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("to", dt.getPushToken());
            msg.put("title", title);
            msg.put("body", body);
            msg.put("sound", "default");
            msg.put("priority", "high");
            if (relatedSetId != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("setId", relatedSetId);
                msg.put("data", data);
            }
            messages.add(msg);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(messages, headers);
            restTemplate.postForEntity(EXPO_PUSH_URL, request, String.class);
        } catch (Exception e) {
            log.warn("Failed to send push notification to user {}: {}", userId, e.getMessage());
        }
    }
}
