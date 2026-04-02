package manage.store.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.entity.ApprovalHistory;
import manage.store.inventory.entity.enums.ApprovalAction;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {

    List<ApprovalHistory> findByRequestSetSetIdOrderByCreatedAtDesc(Long setId);

    Optional<ApprovalHistory> findTopByRequestSetSetIdAndActionOrderByCreatedAtDesc(Long setId, ApprovalAction action);

    void deleteByRequestSetSetId(Long setId);
}
