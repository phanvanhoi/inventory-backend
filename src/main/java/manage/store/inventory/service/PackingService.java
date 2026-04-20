package manage.store.inventory.service;

import java.time.LocalDate;
import java.util.List;

import manage.store.inventory.dto.MissingItemCreateDTO;
import manage.store.inventory.dto.MissingItemDTO;
import manage.store.inventory.dto.PackingBatchCreateDTO;
import manage.store.inventory.dto.PackingBatchDTO;
import manage.store.inventory.entity.enums.PackingBatchStatus;

public interface PackingService {

    // Packing batches
    Long createBatch(Long orderId, PackingBatchCreateDTO dto);
    void updateBatch(Long batchId, PackingBatchCreateDTO dto);
    void changeStatus(Long batchId, PackingBatchStatus status);
    void deleteBatch(Long batchId);
    List<PackingBatchDTO> getBatchesByOrder(Long orderId);
    PackingBatchDTO getBatchById(Long batchId);
    List<PackingBatchDTO> getOverdueBatches(LocalDate today);

    // Missing items
    Long addMissingItem(Long batchId, MissingItemCreateDTO dto);
    void updateMissingItem(Long missingId, MissingItemCreateDTO dto);
    void markResolved(Long missingId, boolean resolved);
    void deleteMissingItem(Long missingId);
    List<MissingItemDTO> getMissingItems(Long batchId);
    List<MissingItemDTO> getUnresolvedByOrder(Long orderId);
}
