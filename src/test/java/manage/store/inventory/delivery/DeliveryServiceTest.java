package manage.store.inventory.delivery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import manage.store.inventory.delivery.DeliveryService.BookRequest;
import manage.store.inventory.delivery.DeliveryService.LineRequest;
import manage.store.inventory.exception.BusinessException;

@DataJpaTest
@Import(DeliveryService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop",
        "spring.sql.init.mode=never"
})
class DeliveryServiceTest {

    @Autowired
    private DeliveryService service;

    @Autowired
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transactions;

    @MockitoBean
    private RoadDistance roadDistance;

    private int hop = 1000;

    @BeforeEach
    void clean() {
        hop = 1000;
        when(roadDistance.meters(any(), any(), any())).thenAnswer(invocation -> {
            List<DeliveryStop> stops = invocation.getArgument(0);
            int n = stops.size();
            int[][] matrix = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = i == j ? 0 : hop;
                }
            }
            return matrix;
        });
        when(roadDistance.line(any(), any())).thenReturn(List.of());
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            em.createQuery("delete from PlanVisitAction").executeUpdate();
            em.createQuery("delete from PlanVisit").executeUpdate();
            em.createQuery("delete from DispatchNotice").executeUpdate();
            em.createQuery("delete from DriverLocation").executeUpdate();
            em.createQuery("delete from DriverDayPlan").executeUpdate();
            em.createQuery("delete from DeliveryBookLine").executeUpdate();
            em.createQuery("delete from DeliveryBook").executeUpdate();
            em.createQuery("delete from DeliveryTrip").executeUpdate();
            em.createQuery("delete from DeliveryDriver").executeUpdate();
            em.createQuery("delete from DeliveryStop").executeUpdate();
            em.createQuery("delete from DeliverySetting").executeUpdate();
        });
    }

    @Test
    void oneDriverPlansBothTrips() {
        LocalDate date = LocalDate.of(2026, 10, 20);
        DeliveryStop shop = stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        DeliveryStop drop = stop("Giao", 10.02, 106.02, false);
        Long driverId = driver();
        hand(book(pickup, drop, date), driverId);
        hand(book(pickup, stop("Giao 2", 10.03, 106.03, false), date), driverId);
        Map<String, Object> day = service.plan(date);
        List<Map<String, Object>> plans = plans(day);
        assertEquals(1, plans.size());
        DriverDayPlan plan = (DriverDayPlan) plans.get(0).get("plan");
        assertEquals(shop.getStopId(), plan.getStartStopId());
        assertTrue(plan.getTotalMeters() > 0);
        assertTrue(visitsOf(plans.get(0)).size() >= 2);
    }

    @Test
    void rebookAfterCancelDoesNotReuseEmptyTrip() {
        LocalDate date = LocalDate.of(2026, 10, 21);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        DeliveryStop drop = stop("Giao", 10.02, 106.02, false);
        Long driverId = driver();
        DeliveryBook first = book(pickup, drop, date);
        hand(first, driverId);
        service.plan(date);
        service.publish(date);
        Long closedId = first.getTripId();
        service.cancelBook(first.getBookId());
        assertEquals("CLOSED", find(DeliveryTrip.class, closedId).getStatus());

        DeliveryBook again = book(pickup, drop, date);
        assertNotEquals(closedId, again.getTripId());
        assertEquals("CLOSED", find(DeliveryTrip.class, closedId).getStatus());
        assertEquals("PLANNED", again.getStatus());
        assertTrue(onRoute(service.day(date), again.getTripId()));
    }

    @Test
    void rebookSameStopsOnFinishedDayStaysOffTheOldTrip() {
        LocalDate date = LocalDate.of(2026, 10, 22);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        DeliveryStop drop = stop("Giao", 10.02, 106.02, false);
        Long driverId = driver();
        DeliveryBook first = book(pickup, drop, date);
        hand(first, driverId);
        service.plan(date);
        service.publish(date);
        service.lock(date, null);
        Long planId = ((DriverDayPlan) plans(service.day(date)).get(0).get("plan")).getPlanId();
        service.depart(planId);
        service.finish(planId);
        assertEquals("CLOSED", find(DeliveryTrip.class, first.getTripId()).getStatus());
        assertEquals(date.plusDays(1), find(DeliveryBook.class, first.getBookId()).getServiceDate());

        DeliveryBook again = book(pickup, drop, date);
        assertNotEquals(first.getTripId(), again.getTripId());
        assertEquals("WAITING", again.getStatus());
        assertNull(owner(again.getTripId()));
        assertNotEquals("PLANNED", again.getStatus());
    }

    @Test
    void finishRejectsDraft() {
        LocalDate date = LocalDate.of(2026, 10, 23);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        DeliveryStop drop = stop("Giao", 10.02, 106.02, false);
        Long driverId = driver();
        hand(book(pickup, drop, date), driverId);
        service.plan(date);
        Long planId = ((DriverDayPlan) plans(service.day(date)).get(0).get("plan")).getPlanId();
        BusinessException error = assertThrows(BusinessException.class, () -> service.finish(planId));
        assertTrue(error.getMessage().contains("đang đi"));
        assertEquals("DRAFT", find(DriverDayPlan.class, planId).getStatus());
    }

    @Test
    void detourOverLimitMovesTheBook() {
        LocalDate date = LocalDate.of(2026, 10, 24);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        DeliveryStop drop = stop("Giao", 10.02, 106.02, false);
        Long driverId = driver();
        hand(book(pickup, drop, date), driverId);
        service.plan(date);
        service.publish(date);
        hop = 100000;
        DeliveryBook extra = book(stop("Xa", 11, 107, false), stop("Xa hơn", 11.2, 107.2, false), date);
        assertEquals(date.plusDays(1), extra.getServiceDate());
        assertEquals("WAITING", extra.getStatus());
    }

    @Test
    void confirmDoesNotRewriteAFinishedBook() {
        LocalDate date = LocalDate.of(2026, 10, 25);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        DeliveryStop drop = stop("Giao", 10.02, 106.02, false);
        DeliveryBook book = book(pickup, drop, date);
        service.confirmSender(book.getBookId(), false, 7L);
        service.confirmSender(book.getBookId(), true, 8L);
        DeliveryBook sent = find(DeliveryBook.class, book.getBookId());
        assertEquals(7L, sent.getSenderConfirmedBy());
        assertEquals("SENDER_CONFIRMED", sent.getStatus());
        service.confirmReceiver(book.getBookId(), false, 9L);
        assertThrows(BusinessException.class, () -> service.confirmSender(book.getBookId(), false, 10L));
        assertEquals("DELIVERED", find(DeliveryBook.class, book.getBookId()).getStatus());

        DeliveryBook cancelled = book(pickup, drop, date);
        service.cancelBook(cancelled.getBookId());
        assertThrows(BusinessException.class, () -> service.confirmReceiver(cancelled.getBookId(), false, 9L));
        assertThrows(BusinessException.class, () -> service.acceptDetour(cancelled.getBookId()));
    }

    @Test
    void straightLineFallbackIsExplained() {
        doThrow(new BusinessException("Không kết nối được Goong.")).when(roadDistance).meters(any(), any(), any());
        LocalDate date = LocalDate.of(2026, 10, 26);
        stop("Cửa hàng", 10, 106, true);
        Long driverId = driver();
        hand(book(stop("Lấy", 10.01, 106.01, false), stop("Giao", 10.02, 106.02, false), date), driverId);
        Map<String, Object> day = service.plan(date);
        DriverDayPlan plan = (DriverDayPlan) plans(day).get(0).get("plan");
        assertTrue(plan.getExplanation().contains("đường thẳng"));
        assertTrue(plan.getTotalMeters() > 0);
    }

    @Test
    void gpsNearALaterStopDoesNotFreezeTheStart() {
        LocalDate date = LocalDate.of(2026, 10, 28);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        DeliveryStop drop = stop("Giao", 11, 107, false);
        Long driverId = driver();
        DeliveryBook book = book(pickup, drop, date);
        hand(book, driverId);
        service.plan(date);
        service.publish(date);
        service.lock(date, null);
        Long planId = ((DriverDayPlan) plans(service.day(date)).get(0).get("plan")).getPlanId();
        service.depart(planId);
        service.ping(driverId, 11, 107, 42L, false);
        service.cancelBook(book.getBookId());
        assertFalse(onRoute(service.day(date), book.getTripId()));
    }

    @Test
    void missingCoordinateStopsPlanning() {
        LocalDate date = LocalDate.of(2026, 10, 27);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = place("Lấy", null, null, false);
        Long driverId = driver();
        hand(book(pickup, stop("Giao", 10.02, 106.02, false), date), driverId);
        BusinessException error = assertThrows(BusinessException.class, () -> service.plan(date));
        assertTrue(error.getMessage().contains("tọa độ"));
    }

    @Test
    void planRoutesOnlyAssignedDrivers() {
        LocalDate date = LocalDate.of(2026, 10, 29);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        Long first = driver("Bác An", 42L);
        Long second = driver("Bác Hai", 43L);
        DeliveryBook kept = book(pickup, stop("Giao", 10.02, 106.02, false), date);
        DeliveryBook other = book(pickup, stop("Giao 2", 10.03, 106.03, false), date);
        DeliveryBook loose = book(pickup, stop("Giao 3", 10.04, 106.04, false), date);
        hand(kept, first);
        hand(other, second);
        Map<String, Object> day = service.plan(date);
        assertEquals(2, plans(day).size());
        assertTrue(onRoute(day, kept.getTripId()));
        assertTrue(onRoute(day, other.getTripId()));
        assertFalse(onRoute(day, loose.getTripId()));
    }

    @Test
    void unconfirmedSuddenLeavesTodayWhenLocked() {
        LocalDate date = LocalDate.of(2026, 10, 30);
        stop("Cửa hàng", 10, 106, true);
        DeliveryStop pickup = stop("Lấy", 10.01, 106.01, false);
        Long driverId = driver();
        hand(book(pickup, stop("Giao", 10.02, 106.02, false), date), driverId);
        service.plan(date);
        service.publish(date);
        DeliveryBook extra = book(stop("Gần", 10.015, 106.015, false), stop("Gần hơn", 10.016, 106.016, false), date);
        assertTrue(extra.isSudden());
        assertFalse(extra.isSuddenConfirmed());
        assertEquals(date, extra.getServiceDate());
        service.lock(date, null);
        DeliveryBook moved = find(DeliveryBook.class, extra.getBookId());
        assertEquals(date.plusDays(1), moved.getServiceDate());
        assertEquals("WAITING", moved.getStatus());
        assertFalse(moved.isSudden());
        assertNull(find(DeliveryTrip.class, moved.getTripId()).getAssignedDriverId());
    }

    @Test
    void confirmedSuddenStaysWhenLocked() {
        LocalDate date = LocalDate.of(2026, 10, 31);
        stop("Cửa hàng", 10, 106, true);
        Long driverId = driver();
        hand(book(stop("Lấy", 10.01, 106.01, false), stop("Giao", 10.02, 106.02, false), date), driverId);
        service.plan(date);
        service.publish(date);
        DeliveryBook extra = book(stop("Gần", 10.015, 106.015, false), stop("Gần hơn", 10.016, 106.016, false), date);
        service.confirmSudden(extra.getBookId());
        service.lock(date, null);
        DeliveryBook stayed = find(DeliveryBook.class, extra.getBookId());
        assertEquals(date, stayed.getServiceDate());
        assertTrue(stayed.isSuddenConfirmed());
        assertTrue(onRoute(service.day(date), stayed.getTripId()));
    }

    private void hand(DeliveryBook book, Long driverId) {
        service.assign(book.getBookId(), driverId);
    }

    private DeliveryBook book(DeliveryStop pickup, DeliveryStop drop, LocalDate date) {
        return service.createBook(new BookRequest(pickup.getStopId(), drop.getStopId(), date, null, null,
                List.of(new LineRequest("Vải", new BigDecimal("2"), "Cây"))), 1L);
    }

    private DeliveryStop stop(String name, double latitude, double longitude, boolean shop) {
        return place(name, latitude, longitude, shop);
    }

    private DeliveryStop place(String name, Double latitude, Double longitude, boolean shop) {
        return new TransactionTemplate(transactions).execute(status -> {
            DeliveryStop stop = new DeliveryStop();
            stop.setStopName(name);
            stop.setLatitude(latitude);
            stop.setLongitude(longitude);
            stop.setShop(shop);
            stop.setCreatedAt(LocalDateTime.now());
            em.persist(stop);
            em.flush();
            return stop;
        });
    }

    private Long driver() {
        return driver("Bác An", 42L);
    }

    private Long driver(String name, Long userId) {
        return new TransactionTemplate(transactions).execute(status -> {
            DeliveryDriver driver = new DeliveryDriver();
            driver.setDriverName(name);
            driver.setPhone("0900000000");
            driver.setActive(true);
            driver.setUserId(userId);
            driver.setCreatedAt(LocalDateTime.now());
            em.persist(driver);
            em.flush();
            return driver.getDriverId();
        });
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> plans(Map<String, Object> day) {
        return (List<Map<String, Object>>) day.get("plans");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> visitsOf(Map<String, Object> planView) {
        return (List<Map<String, Object>>) planView.get("visits");
    }

    @SuppressWarnings("unchecked")
    private boolean onRoute(Map<String, Object> day, Long tripId) {
        for (Map<String, Object> planView : plans(day)) {
            for (Map<String, Object> visit : visitsOf(planView)) {
                List<PlanVisitAction> actions = (List<PlanVisitAction>) visit.get("actions");
                if (actions.stream().anyMatch(action -> tripId.equals(action.getTripId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private DriverDayPlan owner(Long tripId) {
        return new TransactionTemplate(transactions).execute(status -> {
            List<PlanVisitAction> actions = em.createQuery("select a from PlanVisitAction a where a.tripId = :tripId", PlanVisitAction.class)
                    .setParameter("tripId", tripId)
                    .getResultList();
            if (actions.isEmpty()) {
                return null;
            }
            PlanVisit visit = em.find(PlanVisit.class, actions.get(0).getVisitId());
            return visit == null ? null : em.find(DriverDayPlan.class, visit.getPlanId());
        });
    }

    private <T> T find(Class<T> type, Object id) {
        return new TransactionTemplate(transactions).execute(status -> em.find(type, id));
    }
}
