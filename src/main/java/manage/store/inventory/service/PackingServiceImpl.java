package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.MissingItemCreateDTO;
import manage.store.inventory.dto.MissingItemDTO;
import manage.store.inventory.dto.PackingBatchCreateDTO;
import manage.store.inventory.dto.PackingBatchDTO;
import manage.store.inventory.entity.MissingItem;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.OrderItem;
import manage.store.inventory.entity.PackingBatch;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.PackingBatchStatus;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.MissingItemRepository;
import manage.store.inventory.repository.OrderItemRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.PackingBatchRepository;
import manage.store.inventory.repository.UserRepository;

@Service
@Transactional
public class PackingServiceImpl implements PackingService {

    private final PackingBatchRepository batchRepository;
    private final MissingItemRepository missingRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public PackingServiceImpl(
            PackingBatchRepository batchRepository,
            MissingItemRepository missingRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository) {
        this.batchRepository = batchRepository;
        this.missingRepository = missingRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    // ===== Packing batches =====

    @Override
    public Long createBatch(Long orderId, PackingBatchCreateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        PackingBatch pb = new PackingBatch();
        pb.setOrder(order);
        applyBatchFields(dto, pb);
        pb.setCreatedAt(LocalDateTime.now());
        batchRepository.save(pb);
        return pb.getPackingBatchId();
    }

    @Override
    public void updateBatch(Long batchId, PackingBatchCreateDTO dto) {
        PackingBatch pb = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Packing batch không tồn tại"));
        applyBatchFields(dto, pb);
        batchRepository.save(pb);
    }

    @Override
    public void changeStatus(Long batchId, PackingBatchStatus status) {
        if (status == null) {
            throw new BusinessException("Trạng thái không được trống");
        }
        PackingBatch pb = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Packing batch không tồn tại"));
        pb.setStatus(status);
        // Auto: SHIPPED → set actual_delivery_date hôm nay nếu chưa có
        if (status == PackingBatchStatus.SHIPPED && pb.getActualDeliveryDate() == null) {
            pb.setActualDeliveryDate(LocalDate.now());
        }
        batchRepository.save(pb);
    }

    @Override
    public void deleteBatch(Long batchId) {
        if (!batchRepository.existsById(batchId)) {
            throw new ResourceNotFoundException("Packing batch không tồn tại");
        }
        batchRepository.deleteById(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackingBatchDTO> getBatchesByOrder(Long orderId) {
        return batchRepository.findByOrderId(orderId).stream()
                .map(PackingBatchDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PackingBatchDTO getBatchById(Long batchId) {
        return batchRepository.findById(batchId)
                .map(PackingBatchDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Packing batch không tồn tại"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackingBatchDTO> getOverdueBatches(LocalDate today) {
        return batchRepository.findOverdue(today).stream()
                .map(PackingBatchDTO::from)
                .collect(Collectors.toList());
    }

    // ===== Missing items =====

    @Override
    public Long addMissingItem(Long batchId, MissingItemCreateDTO dto) {
        PackingBatch pb = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Packing batch không tồn tại"));
        OrderItem item = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));

        MissingItem m = new MissingItem();
        m.setPackingBatch(pb);
        m.setOrderItem(item);
        applyMissingFields(dto, m);
        m.setCreatedAt(LocalDateTime.now());
        missingRepository.save(m);
        return m.getMissingId();
    }

    @Override
    public void updateMissingItem(Long missingId, MissingItemCreateDTO dto) {
        MissingItem m = missingRepository.findById(missingId)
                .orElseThrow(() -> new ResourceNotFoundException("Missing item không tồn tại"));
        if (dto.getOrderItemId() != null
                && !dto.getOrderItemId().equals(m.getOrderItem().getOrderItemId())) {
            OrderItem item = orderItemRepository.findById(dto.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));
            m.setOrderItem(item);
        }
        applyMissingFields(dto, m);
        missingRepository.save(m);
    }

    @Override
    public void markResolved(Long missingId, boolean resolved) {
        MissingItem m = missingRepository.findById(missingId)
                .orElseThrow(() -> new ResourceNotFoundException("Missing item không tồn tại"));
        m.setResolved(resolved);
        missingRepository.save(m);
    }

    @Override
    public void deleteMissingItem(Long missingId) {
        if (!missingRepository.existsById(missingId)) {
            throw new ResourceNotFoundException("Missing item không tồn tại");
        }
        missingRepository.deleteById(missingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissingItemDTO> getMissingItems(Long batchId) {
        return missingRepository.findByPackingBatchId(batchId).stream()
                .map(MissingItemDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissingItemDTO> getUnresolvedByOrder(Long orderId) {
        return missingRepository.findUnresolvedByOrderId(orderId).stream()
                .map(MissingItemDTO::from)
                .collect(Collectors.toList());
    }

    // ===== helpers =====

    private void applyBatchFields(PackingBatchCreateDTO dto, PackingBatch pb) {
        if (dto.getPackerUserId() != null) {
            User packer = userRepository.findById(dto.getPackerUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Packer user không tồn tại"));
            pb.setPackerUser(packer);
        } else {
            pb.setPackerUser(null);
        }
        pb.setDocumentsReceivedDate(dto.getDocumentsReceivedDate());
        pb.setPackingStartedDate(dto.getPackingStartedDate());
        pb.setPackingCompletedDate(dto.getPackingCompletedDate());
        pb.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        pb.setContractDeliveryDate(dto.getContractDeliveryDate());
        pb.setActualDeliveryDate(dto.getActualDeliveryDate());
        if (dto.getDeliveryStatus() != null) pb.setDeliveryStatus(dto.getDeliveryStatus());
        pb.setTickFileUrl(dto.getTickFileUrl());
        if (dto.getStatus() != null) pb.setStatus(dto.getStatus());
        pb.setNote(dto.getNote());
    }

    private void applyMissingFields(MissingItemCreateDTO dto, MissingItem m) {
        if (dto.getMissingQuantity() != null) m.setMissingQuantity(dto.getMissingQuantity());
        m.setMissingListFileUrl(dto.getMissingListFileUrl());
        if (dto.getResolved() != null) m.setResolved(dto.getResolved());
        m.setNote(dto.getNote());
    }
}
