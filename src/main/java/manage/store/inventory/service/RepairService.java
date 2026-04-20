package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.RepairRequestCreateDTO;
import manage.store.inventory.dto.RepairRequestDTO;
import manage.store.inventory.entity.enums.RepairStatus;

public interface RepairService {

    Long createRepair(RepairRequestCreateDTO dto);
    void updateRepair(Long repairId, RepairRequestCreateDTO dto);
    void changeStatus(Long repairId, RepairStatus status);
    void deleteRepair(Long repairId);

    List<RepairRequestDTO> getByOrder(Long orderId);
    List<RepairRequestDTO> getByOrderItem(Long orderItemId);

    /** Recompute orders.has_repair based on active (non SHIPPED_BACK) repairs. */
    boolean recomputeHasRepair(Long orderId);
}
