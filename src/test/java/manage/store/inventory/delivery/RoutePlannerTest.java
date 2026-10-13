package manage.store.inventory.delivery;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import manage.store.inventory.delivery.RoutePlanner.Distance;
import manage.store.inventory.delivery.RoutePlanner.Route;
import manage.store.inventory.delivery.RoutePlanner.Trip;
import manage.store.inventory.delivery.RoutePlanner.Visit;

class RoutePlannerTest {

    private static Distance line() {
        Map<Long, Integer> at = Map.of(1L, 0, 2L, 10, 3L, 20, 4L, 100);
        return (from, to) -> Math.abs(at.get(from) - at.get(to));
    }

    @Test
    @DisplayName("Không có chuyến thì không tạo điểm dừng")
    void emptyDay() {
        List<Route> routes = new RoutePlanner().plan(List.of(), 1L, line(), 2, true);
        assertEquals(2, routes.size());
        assertTrue(routes.get(0).visits().isEmpty());
        assertEquals(0, routes.get(0).meters());
    }

    @Test
    @DisplayName("Hai chuyến cùng điểm lấy chỉ dừng một lần tại điểm đó")
    void sharedPickup() {
        List<Trip> trips = List.of(new Trip(10, 1, 2), new Trip(11, 1, 3));
        List<Route> routes = new RoutePlanner().plan(trips, 1L, line(), 2, true);
        Route used = routes.stream().filter(route -> !route.visits().isEmpty()).findFirst().orElseThrow();
        long pickupVisits = used.visits().stream().filter(visit -> visit.stopId() == 1L).count();
        assertEquals(1, pickupVisits);
        assertTrue(RoutePlanner.precedenceOk(used.visits()));
        assertTrue(used.visits().get(0).pickups().contains(10L));
        assertTrue(used.visits().get(0).pickups().contains(11L));
    }

    @Test
    @DisplayName("Chuyến lệch hướng sang bác còn lại")
    void splitsDrivers() {
        List<Trip> trips = List.of(new Trip(10, 1, 2), new Trip(11, 4, 4));
        Distance distance = (from, to) -> {
            if (from == to) {
                return 0;
            }
            if ((from == 4L || to == 4L) && (from == 1L || to == 1L || from == 2L || to == 2L)) {
                return 500;
            }
            return 10;
        };
        List<Route> routes = new RoutePlanner().plan(trips, 1L, distance, 2, false);
        long busy = routes.stream().filter(route -> !route.visits().isEmpty()).count();
        assertEquals(2, busy);
        for (Route route : routes) {
            assertTrue(RoutePlanner.precedenceOk(route.visits()));
        }
    }

    @Test
    @DisplayName("Một tài xế vẫn xếp được hai chuyến")
    void oneDriver() {
        List<Trip> trips = List.of(new Trip(10, 1, 2), new Trip(11, 2, 3));
        List<Route> routes = new RoutePlanner().plan(trips, 1L, line(), 1, true);
        assertEquals(1, routes.size());
        assertEquals(2, RoutePlanner.tripsOf(routes.get(0).visits()).size());
        assertTrue(RoutePlanner.precedenceOk(routes.get(0).visits()));
        assertTrue(routes.get(0).meters() > 0);
    }

    @Test
    @DisplayName("Xe gánh quá hai phần ba km thì nhường một chuyến ngắn")
    void balancesWhenOneDriverHoldsMostDistance() {
        Distance distance = (from, to) -> {
            if (from == to) {
                return 0;
            }
            if ((from == 1L && to == 2L) || (from == 2L && to == 1L)) {
                return 500;
            }
            if ((from == 2L && to == 3L) || (from == 3L && to == 2L)) {
                return 9000;
            }
            if ((from == 3L && to == 1L) || (from == 1L && to == 3L)) {
                return 500;
            }
            return 1000;
        };
        List<Trip> trips = List.of(new Trip(10, 2, 3), new Trip(11, 2, 2));
        List<Route> routes = new RoutePlanner().plan(trips, 1L, distance, 2, true);
        long busy = routes.stream().filter(route -> !route.visits().isEmpty()).count();
        assertEquals(2, busy);
        assertTrue(routes.stream().anyMatch(route -> route.reasons().stream().anyMatch(reason -> reason.contains("cân km"))));
        for (Route route : routes) {
            assertTrue(RoutePlanner.precedenceOk(route.visits()));
        }
    }

    @Test
    @DisplayName("Chèn tiện chuyến chỉ tính đoạn còn lại")
    void detourOnRemaining() {
        Visit first = new Visit(1L);
        first.pickups().add(1L);
        Visit second = new Visit(2L);
        second.dropoffs().add(1L);
        RoutePlanner.Insertion insertion = new RoutePlanner().insertIntoRemaining(
                List.of(first, second), 1, new Trip(9, 2, 3), 1L, line(), false);
        assertEquals(10, insertion.extraMeters());
        assertEquals(1L, insertion.visits().get(0).stopId());
    }
}
