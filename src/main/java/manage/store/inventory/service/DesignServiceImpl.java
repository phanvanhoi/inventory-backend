package manage.store.inventory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.DesignDocumentCreateDTO;
import manage.store.inventory.dto.DesignDocumentDTO;
import manage.store.inventory.dto.DesignSampleCreateDTO;
import manage.store.inventory.dto.DesignSampleDTO;
import manage.store.inventory.entity.DesignDocument;
import manage.store.inventory.entity.DesignSample;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.OrderItem;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.DesignSampleStatus;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.DesignDocumentRepository;
import manage.store.inventory.repository.DesignSampleRepository;
import manage.store.inventory.repository.OrderItemRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.UserRepository;

@Service
@Transactional
public class DesignServiceImpl implements DesignService {

    private final DesignSampleRepository sampleRepository;
    private final DesignDocumentRepository docRepository;
    private final OrderItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public DesignServiceImpl(
            DesignSampleRepository sampleRepository,
            DesignDocumentRepository docRepository,
            OrderItemRepository itemRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.sampleRepository = sampleRepository;
        this.docRepository = docRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // ===== Samples =====

    @Override
    public Long addSample(Long orderItemId, DesignSampleCreateDTO dto) {
        OrderItem item = itemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));

        DesignSample s = new DesignSample();
        s.setOrderItem(item);
        applySampleFields(dto, s);
        s.setCreatedAt(LocalDateTime.now());
        sampleRepository.save(s);

        if (s.getStatus() == DesignSampleStatus.APPROVED) {
            recomputeDesignReady(item.getOrder().getOrderId());
        }
        return s.getDesignSampleId();
    }

    @Override
    public void updateSample(Long sampleId, DesignSampleCreateDTO dto) {
        DesignSample s = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sample không tồn tại"));
        applySampleFields(dto, s);
        sampleRepository.save(s);
        recomputeDesignReady(s.getOrderItem().getOrder().getOrderId());
    }

    @Override
    public void changeSampleStatus(Long sampleId, DesignSampleStatus status) {
        DesignSample s = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sample không tồn tại"));
        s.setStatus(status);
        sampleRepository.save(s);
        recomputeDesignReady(s.getOrderItem().getOrder().getOrderId());
    }

    @Override
    public void deleteSample(Long sampleId) {
        DesignSample s = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sample không tồn tại"));
        Long orderId = s.getOrderItem().getOrder().getOrderId();
        sampleRepository.delete(s);
        recomputeDesignReady(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignSampleDTO> getSamplesByOrderItem(Long orderItemId) {
        return sampleRepository.findByOrderItemId(orderItemId).stream()
                .map(DesignSampleDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignSampleDTO> getSamplesByOrder(Long orderId) {
        return sampleRepository.findByOrderId(orderId).stream()
                .map(DesignSampleDTO::from)
                .collect(Collectors.toList());
    }

    // ===== Documents =====

    @Override
    public Long addDocument(Long orderId, DesignDocumentCreateDTO dto, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        User user = userRepository.findById(userId).orElse(null);

        DesignDocument d = new DesignDocument();
        d.setOrder(order);
        d.setFileUrl(dto.getFileUrl());
        d.setFileName(dto.getFileName());
        d.setUploadedByUser(user);
        d.setUploadedAt(LocalDateTime.now());
        d.setNote(dto.getNote());
        docRepository.save(d);
        return d.getDesignDocId();
    }

    @Override
    public void deleteDocument(Long docId) {
        if (!docRepository.existsById(docId)) {
            throw new ResourceNotFoundException("Document không tồn tại");
        }
        docRepository.deleteById(docId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignDocumentDTO> getDocumentsByOrder(Long orderId) {
        return docRepository.findByOrderId(orderId).stream()
                .map(DesignDocumentDTO::from)
                .collect(Collectors.toList());
    }

    // ===== Auto design_ready logic =====

    /**
     * Set orders.design_ready = TRUE iff every OrderItem has at least 1 APPROVED sample.
     * Returns the resulting design_ready value.
     */
    @Override
    public boolean recomputeDesignReady(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        if (Boolean.TRUE.equals(order.getSkipDesign())) {
            // Nếu đơn skip design, flag không áp dụng
            return Boolean.TRUE.equals(order.getDesignReady());
        }

        long totalItems = sampleRepository.countOrderItems(orderId);
        long approvedItems = sampleRepository.countApprovedItems(orderId);
        boolean ready = totalItems > 0 && approvedItems >= totalItems;

        if (!ready == Boolean.TRUE.equals(order.getDesignReady())) {
            order.setDesignReady(ready);
            orderRepository.save(order);
        }
        return ready;
    }

    private void applySampleFields(DesignSampleCreateDTO dto, DesignSample s) {
        s.setSampleImageUrl(dto.getSampleImageUrl());
        s.setFabricCode(dto.getFabricCode());
        if (dto.getDesignerUserId() != null) {
            User designer = userRepository.findById(dto.getDesignerUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designer không tồn tại"));
            s.setDesignerUser(designer);
        } else {
            s.setDesignerUser(null);
        }
        if (dto.getStatus() != null) s.setStatus(dto.getStatus());
        s.setNote(dto.getNote());
    }
}
