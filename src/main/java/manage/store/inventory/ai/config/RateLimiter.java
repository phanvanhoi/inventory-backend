package manage.store.inventory.ai.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {

    private final ConcurrentHashMap<Long, Deque<Instant>> userRequests = new ConcurrentHashMap<>();

    @Value("${ai.rate-limit.max-requests-per-minute:20}")
    private int maxPerMinute;

    public boolean isAllowed(Long userId) {
        Deque<Instant> timestamps = userRequests.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);

        // Remove expired entries
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(oneMinuteAgo)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxPerMinute) {
            return false;
        }

        timestamps.addLast(Instant.now());
        return true;
    }
}
