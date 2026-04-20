package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.DesignDocumentCreateDTO;
import manage.store.inventory.dto.DesignDocumentDTO;
import manage.store.inventory.dto.DesignSampleCreateDTO;
import manage.store.inventory.dto.DesignSampleDTO;
import manage.store.inventory.entity.enums.DesignSampleStatus;

public interface DesignService {

    // Design samples per OrderItem
    Long addSample(Long orderItemId, DesignSampleCreateDTO dto);
    void updateSample(Long sampleId, DesignSampleCreateDTO dto);
    void changeSampleStatus(Long sampleId, DesignSampleStatus status);
    void deleteSample(Long sampleId);
    List<DesignSampleDTO> getSamplesByOrderItem(Long orderItemId);
    List<DesignSampleDTO> getSamplesByOrder(Long orderId);

    // Design documents per Order
    Long addDocument(Long orderId, DesignDocumentCreateDTO dto, Long userId);
    void deleteDocument(Long docId);
    List<DesignDocumentDTO> getDocumentsByOrder(Long orderId);

    // Computed: check design ready across all items
    boolean recomputeDesignReady(Long orderId);
}
