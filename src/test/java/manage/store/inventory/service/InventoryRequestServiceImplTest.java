package manage.store.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.entity.InventoryRequest;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.enums.RequestSetStatus;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.InventoryRequestItemRepository;
import manage.store.inventory.repository.InventoryRequestRepository;
import manage.store.inventory.repository.PositionRepository;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.repository.ProductVariantRepository;
import manage.store.inventory.repository.RequestSetRepository;

@ExtendWith(MockitoExtension.class)
class InventoryRequestServiceImplTest {

    @Mock
    private InventoryRequestRepository requestRepository;

    @Mock
    private InventoryRequestItemRepository itemRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private RequestSetRepository requestSetRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryRequestServiceImpl requestService;

    private InventoryRequest adjustInRequest;
    private InventoryRequest adjustOutRequest;
    private InventoryRequest inRequest;

    @BeforeEach
    void setUp() {
        adjustInRequest = new InventoryRequest();
        adjustInRequest.setRequestId(1L);
        adjustInRequest.setRequestType(InventoryRequest.RequestType.ADJUST_IN);
        adjustInRequest.setProductId(1L);
        adjustInRequest.setExpectedDate(LocalDate.now().plusDays(7));
        adjustInRequest.setSetId(10L);

        adjustOutRequest = new InventoryRequest();
        adjustOutRequest.setRequestId(2L);
        adjustOutRequest.setRequestType(InventoryRequest.RequestType.ADJUST_OUT);
        adjustOutRequest.setProductId(1L);
        adjustOutRequest.setExpectedDate(LocalDate.now().plusDays(14));

        inRequest = new InventoryRequest();
        inRequest.setRequestId(3L);
        inRequest.setRequestType(InventoryRequest.RequestType.IN);
        inRequest.setProductId(1L);
    }

    // ==================== UPDATE REQUEST TYPE ====================

    @Test
    @DisplayName("Chuyển ADJUST_IN -> IN thành công")
    void updateRequestType_adjustInToIn_success() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));

        requestService.updateRequestType(1L, "IN");

        assertEquals(InventoryRequest.RequestType.IN, adjustInRequest.getRequestType());
        verify(requestRepository).save(adjustInRequest);
    }

    @Test
    @DisplayName("Chuyển ADJUST_OUT -> OUT thành công")
    void updateRequestType_adjustOutToOut_success() {
        when(requestRepository.findById(2L)).thenReturn(Optional.of(adjustOutRequest));

        requestService.updateRequestType(2L, "OUT");

        assertEquals(InventoryRequest.RequestType.OUT, adjustOutRequest.getRequestType());
        verify(requestRepository).save(adjustOutRequest);
    }

    @Test
    @DisplayName("Chuyển ADJUST_IN -> OUT thất bại")
    void updateRequestType_adjustInToOut_throwsException() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> requestService.updateRequestType(1L, "OUT"));
        assertTrue(ex.getMessage().contains("ADJUST_IN chỉ có thể chuyển thành IN"));
    }

    @Test
    @DisplayName("Chuyển ADJUST_OUT -> IN thất bại")
    void updateRequestType_adjustOutToIn_throwsException() {
        when(requestRepository.findById(2L)).thenReturn(Optional.of(adjustOutRequest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> requestService.updateRequestType(2L, "IN"));
        assertTrue(ex.getMessage().contains("ADJUST_OUT chỉ có thể chuyển thành OUT"));
    }

    @Test
    @DisplayName("Cập nhật request type cho IN/OUT (đã final) thất bại")
    void updateRequestType_alreadyFinal_throwsException() {
        when(requestRepository.findById(3L)).thenReturn(Optional.of(inRequest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> requestService.updateRequestType(3L, "OUT"));
        assertTrue(ex.getMessage().contains("không thể cập nhật request type nữa"));
    }

    @Test
    @DisplayName("Cập nhật request type không hợp lệ")
    void updateRequestType_invalidType_throwsException() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> requestService.updateRequestType(1L, "INVALID"));
        assertTrue(ex.getMessage().contains("Request type không hợp lệ"));
    }

    // ==================== DELETE REQUEST ====================

    @Test
    @DisplayName("Xóa request thành công")
    void deleteRequest_success() {
        when(requestRepository.existsById(1L)).thenReturn(true);

        requestService.deleteRequest(1L);

        verify(itemRepository).deleteByRequestId(1L);
        verify(requestRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Xóa request - không tìm thấy")
    void deleteRequest_notFound_throwsException() {
        when(requestRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> requestService.deleteRequest(99L));
        assertTrue(ex.getMessage().contains("Request not found"));
        verify(requestRepository, never()).deleteById(any());
    }

    // ==================== COUNT DEPENDENT ADJUST_OUT ====================

    @Test
    @DisplayName("Đếm ADJUST_OUT phụ thuộc - có phụ thuộc")
    void countDependentAdjustOut_hasDependents() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));
        when(inventoryRepository.countDependentAdjustOut(1L, adjustInRequest.getExpectedDate(), 1L))
                .thenReturn(3);

        int count = requestService.countDependentAdjustOut(1L);

        assertEquals(3, count);
    }

    @Test
    @DisplayName("Đếm ADJUST_OUT phụ thuộc - không phải ADJUST_IN trả về 0")
    void countDependentAdjustOut_notAdjustIn_returnsZero() {
        when(requestRepository.findById(3L)).thenReturn(Optional.of(inRequest));

        int count = requestService.countDependentAdjustOut(3L);

        assertEquals(0, count);
    }

    @Test
    @DisplayName("Đếm ADJUST_OUT phụ thuộc - không có expected_date trả về 0")
    void countDependentAdjustOut_noExpectedDate_returnsZero() {
        adjustInRequest.setExpectedDate(null);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));

        int count = requestService.countDependentAdjustOut(1L);

        assertEquals(0, count);
    }

    // ==================== UPDATE EXPECTED DATE ====================

    @Test
    @DisplayName("Cập nhật expected date thành công")
    void updateExpectedDate_success() {
        LocalDate newDate = LocalDate.now().plusDays(14);
        adjustInRequest.setSetId(null); // No set

        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));

        requestService.updateExpectedDate(1L, newDate, 1L);

        assertEquals(newDate, adjustInRequest.getExpectedDate());
        verify(requestRepository).save(adjustInRequest);
    }

    @Test
    @DisplayName("Cập nhật expected date - không phải ADJUST type thất bại")
    void updateExpectedDate_notAdjustType_throwsException() {
        when(requestRepository.findById(3L)).thenReturn(Optional.of(inRequest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> requestService.updateExpectedDate(3L, LocalDate.now().plusDays(7), 1L));
        assertTrue(ex.getMessage().contains("Chỉ có thể cập nhật ngày dự kiến cho phiếu ADJUST_IN hoặc ADJUST_OUT"));
    }

    @Test
    @DisplayName("Cập nhật expected date - ngày trong quá khứ thất bại")
    void updateExpectedDate_pastDate_throwsException() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> requestService.updateExpectedDate(1L, LocalDate.now().minusDays(1), 1L));
        assertTrue(ex.getMessage().contains("Ngày dự kiến mới phải >= hôm nay"));
    }

    @Test
    @DisplayName("Cập nhật expected date - request set bị REJECTED thất bại")
    void updateExpectedDate_rejectedSet_throwsException() {
        adjustInRequest.setSetId(10L);
        RequestSet rejectedSet = new RequestSet();
        rejectedSet.setSetId(10L);
        rejectedSet.setStatus(RequestSetStatus.REJECTED);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));
        when(requestSetRepository.findById(10L)).thenReturn(Optional.of(rejectedSet));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> requestService.updateExpectedDate(1L, LocalDate.now().plusDays(14), 1L));
        assertTrue(ex.getMessage().contains("Không thể cập nhật ngày dự kiến cho phiếu đã bị từ chối"));
    }

    @Test
    @DisplayName("Dời ADJUST_IN xa hơn - có ADJUST_OUT phụ thuộc thất bại")
    void updateExpectedDate_adjustInWithDependents_throwsException() {
        LocalDate currentDate = LocalDate.now().plusDays(7);
        LocalDate newDate = LocalDate.now().plusDays(14);
        adjustInRequest.setExpectedDate(currentDate);
        adjustInRequest.setSetId(null);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));
        when(inventoryRepository.countDependentAdjustOut(1L, currentDate, 1L)).thenReturn(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> requestService.updateExpectedDate(1L, newDate, 1L));
        assertTrue(ex.getMessage().contains("có 2 phiếu ADJUST_OUT phụ thuộc"));
    }

    @Test
    @DisplayName("Cập nhật expected date - set APPROVED chuyển về PENDING")
    void updateExpectedDate_approvedSet_resetsToPending() {
        LocalDate newDate = LocalDate.now().plusDays(5);
        // Set date earlier (not later) to avoid dependent check
        adjustInRequest.setExpectedDate(LocalDate.now().plusDays(14));
        adjustInRequest.setSetId(10L);

        RequestSet approvedSet = new RequestSet();
        approvedSet.setSetId(10L);
        approvedSet.setStatus(RequestSetStatus.APPROVED);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(adjustInRequest));
        when(requestSetRepository.findById(10L)).thenReturn(Optional.of(approvedSet));

        requestService.updateExpectedDate(1L, newDate, 1L);

        assertEquals(RequestSetStatus.PENDING, approvedSet.getStatus());
        verify(requestSetRepository).save(approvedSet);
    }
}
