package manage.store.inventory.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.ai.entity.AiQueryLog;

public interface AiQueryLogRepository extends JpaRepository<AiQueryLog, Long> {
}
