package manage.store.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.dto.RepairRequestCreateDTO;
import manage.store.inventory.dto.RepairRequestDTO;
import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.OrderItem;
import manage.store.inventory.entity.PackingBatch;
import manage.store.inventory.entity.RepairRequest;
import manage.store.inventory.entity.User;
import manage.store.inventory.entity.enums.RepairStatus;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.OrderItemRepository;
import manage.store.inventory.repository.OrderRepository;
import manage.store.inventory.repository.PackingBatchRepository;
import manage.store.inventory.repository.RepairRequestRepository;
import manage.store.inventory.repository.UserRepository;

@Service
@Transactional
public class RepairServiceImpl implements RepairService {

    private final RepairRequestRepository repairRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final PackingBatchRepository packingBatchRepository;
    private final UserRepository userRepository;

    public RepairServiceImpl(
            RepairRequestRepository repairRepository,
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            PackingBatchRepository packingBatchRepository,
            UserRepository userRepository) {
        this.repairRepository = repairRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.packingBatchRepository = packingBatchRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Long createRepair(RepairRequestCreateDTO dto) {
        OrderItem item = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));

        RepairRequest r = new RepairRequest();
        r.setOrderItem(item);
        applyFields(dto, r);
        r.setCreatedAt(LocalDateTime.now());
        repairRepository.save(r);
        recomputeHasRepair(item.getOrder().getOrderId());
        return r.getRepairId();
    }

    @Override
    public void updateRepair(Long repairId, RepairRequestCreateDTO dto) {
        RepairRequest r = repairRepository.findById(repairId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair request không tồn tại"));
        if (dto.getOrderItemId() != null
                && !dto.getOrderItemId().equals(r.getOrderItem().getOrderItemId())) {
            OrderItem item = orderItemRepository.findById(dto.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng không tồn tại"));
            r.setOrderItem(item);
        }
        applyFields(dto, r);
        repairRepository.save(r);
        recomputeHasRepair(r.getOrderItem().getOrder().getOrderId());
    }

    @Override
    public void changeStatus(Long repairId, RepairStatus status) {
        RepairRequest r = repairRepository.findById(repairId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair request không tồn tại"));
        r.setStatus(status);
        // Auto set returnDate khi SHIPPED_BACK nếu chưa có
        if (status == RepairStatus.SHIPPED_BACK && r.getReturnDate() == null) {
            r.setReturnDate(LocalDate.now());
        }
        repairRepository.save(r);
        recomputeHasRepair(r.getOrderItem().getOrder().getOrderId());
    }

    @Override
    public void deleteRepair(Long repairId) {
        RepairRequest r = repairRepository.findById(repairId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair request không tồn tại"));
        Long orderId = r.getOrderItem().getOrder().getOrderId();
        repairRepository.delete(r);
        recomputeHasRepair(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairRequestDTO> getByOrder(Long orderId) {
        return repairRepository.findByOrderId(orderId).stream()
                .map(RepairRequestDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairRequestDTO> getByOrderItem(Long orderItemId) {
        return repairRepository.findByOrderItemId(orderItemId).stream()
                .map(RepairRequestDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public boolean recomputeHasRepair(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        long active = repairRepository.countActiveByOrderId(orderId);
        boolean hasRepair = active > 0;
        if (hasRepair != Boolean.TRUE.equals(order.getHasRepair())) {
            order.setHasRepair(hasRepair);
            orderRepository.save(order);
        }
        return hasRepair;
    }

    private void applyFields(RepairRequestCreateDTO dto, RepairRequest r) {
        if (dto.getPackingBatchId() != null) {
            PackingBatch pb = packingBatchRepository.findById(dto.getPackingBatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Packing batch không tồn tại"));
            r.setPackingBatch(pb);
        } else {
            r.setPackingBatch(null);
        }
        r.setBatchNumber(dto.getBatchNumber());
        r.setReceivedDate(dto.getReceivedDate());
        if (dto.getReceiverUserId() != null) {
            User u = userRepository.findById(dto.getReceiverUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người nhận không tồn tại"));
            r.setReceiverUser(u);
        } else {
            r.setReceiverUser(null);
        }
        r.setReceiveMethod(dto.getReceiveMethod());
        r.setExpectedCompletionDate(dto.getExpectedCompletionDate());
        if (dto.getQtyRepair() != null) r.setQtyRepair(dto.getQtyRepair());
        r.setRepairDetails(dto.getRepairDetails());
        r.setReturnDate(dto.getReturnDate());
        r.setReturnMethod(dto.getReturnMethod());
        if (dto.getReturnHandlerUserId() != null) {
            User u = userRepository.findById(dto.getReturnHandlerUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người trả không tồn tại"));
            r.setReturnHandlerUser(u);
        } else {
            r.setReturnHandlerUser(null);
        }
        r.setParentBatches(dto.getParentBatches());
        r.setReasonForReturn(dto.getReasonForReturn());
        if (dto.getStatus() != null) r.setStatus(dto.getStatus());
        r.setNote(dto.getNote());
    }
}
