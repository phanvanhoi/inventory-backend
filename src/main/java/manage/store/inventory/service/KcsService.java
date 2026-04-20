package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.QualityCheckCreateDTO;
import manage.store.inventory.dto.QualityCheckDTO;
import manage.store.inventory.entity.enums.QualityCheckStatus;

public interface KcsService {

    Long addCheck(Long orderItemId, QualityCheckCreateDTO dto);
    void updateCheck(Long qcId, QualityCheckCreateDTO dto);
    void changeStatus(Long qcId, QualityCheckStatus status);
    void deleteCheck(Long qcId);

    List<QualityCheckDTO> getChecksByOrderItem(Long orderItemId);
    List<QualityCheckDTO> getChecksByOrder(Long orderId);

    // Computed
    boolean recomputeQcPassed(Long orderId);
}
