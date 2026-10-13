package manage.store.inventory.delivery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chèn từng chuyến vào lịch của vài tài xế sao cho điểm lấy đứng trước điểm giao
 * và tổng km nhỏ nhất. Cửa hàng chỉ là chỗ đứng ban đầu, không tự thành điểm lấy.
 */
public final class RoutePlanner {

    public record Trip(long id, long pickup, long dropoff) {}

    public interface Distance {
        int meters(long from, long to);
    }

    public static final class Visit {
        private final long stopId;
        private final List<Long> pickups = new ArrayList<>();
        private final List<Long> dropoffs = new ArrayList<>();

        public Visit(long stopId) {
            this.stopId = stopId;
        }

        public long stopId() {
            return stopId;
        }

        public List<Long> pickups() {
            return pickups;
        }

        public List<Long> dropoffs() {
            return dropoffs;
        }

        Visit copy() {
            Visit copy = new Visit(stopId);
            copy.pickups.addAll(pickups);
            copy.dropoffs.addAll(dropoffs);
            return copy;
        }
    }

    public record Route(List<Visit> visits, int meters, List<String> reasons) {}

    public List<Route> plan(List<Trip> trips, long startId, Distance distance, int driverCount, boolean returnToStart) {
        return plan(trips, startId, distance, driverCount, returnToStart ? Long.valueOf(startId) : null);
    }

    public List<Route> plan(List<Trip> trips, long startId, Distance distance, int driverCount, Long returnStopId) {
        if (driverCount < 1) {
            throw new IllegalArgumentException("Cần ít nhất một tài xế");
        }
        List<List<Visit>> routes = new ArrayList<>();
        List<List<String>> reasons = new ArrayList<>();
        for (int i = 0; i < driverCount; i++) {
            routes.add(new ArrayList<>());
            reasons.add(new ArrayList<>());
        }
        if (trips.isEmpty()) {
            return toResult(routes, reasons, startId, distance, returnStopId);
        }

        List<Trip> ordered = new ArrayList<>(trips);
        ordered.sort(Comparator.comparingInt(trip -> distance.meters(startId, trip.pickup())));

        for (Trip trip : ordered) {
            int bestCost = Integer.MAX_VALUE;
            int bestDriver = 0;
            List<Visit> bestVisits = null;
            int[] extras = new int[driverCount];
            for (int driver = 0; driver < driverCount; driver++) {
                List<Visit> inserted = bestInsertion(routes.get(driver), trip, startId, distance, returnStopId);
                int before = pathMeters(startId, routes.get(driver), distance, returnStopId);
                int after = pathMeters(startId, inserted, distance, returnStopId);
                extras[driver] = after - before;
                int current = pathMeters(startId, routes.get(driver), distance, null);
                int tie = current;
                if (extras[driver] < bestCost || (extras[driver] == bestCost && tie < pathMeters(startId, routes.get(bestDriver), distance, null))) {
                    bestCost = extras[driver];
                    bestDriver = driver;
                    bestVisits = inserted;
                }
            }
            routes.set(bestDriver, bestVisits);
            reasons.get(bestDriver).add(reason(trip, extras, bestDriver));
        }

        for (int driver = 0; driver < driverCount; driver++) {
            routes.set(driver, improve(routes.get(driver), startId, distance, returnStopId));
        }
        balance(routes, reasons, startId, distance, returnStopId);
        for (int driver = 0; driver < driverCount; driver++) {
            routes.set(driver, improve(routes.get(driver), startId, distance, returnStopId));
        }
        return toResult(routes, reasons, startId, distance, returnStopId);
    }

    public Insertion insertIntoRemaining(List<Visit> visits, int fromIndex, Trip trip, long startId, Distance distance, boolean returnToStart) {
        List<Visit> remaining = new ArrayList<>();
        for (int i = Math.max(0, fromIndex); i < visits.size(); i++) {
            remaining.add(visits.get(i).copy());
        }
        List<Visit> head = new ArrayList<>();
        for (int i = 0; i < Math.min(fromIndex, visits.size()); i++) {
            head.add(visits.get(i).copy());
        }
        long anchor = head.isEmpty() ? startId : head.get(head.size() - 1).stopId();
        Long returnTo = returnToStart ? startId : null;
        int before = pathMeters(anchor, remaining, distance, returnTo);
        List<Visit> inserted = bestInsertion(remaining, trip, anchor, distance, returnTo);
        int after = pathMeters(anchor, inserted, distance, returnTo);
        List<Visit> combined = new ArrayList<>(head);
        combined.addAll(inserted);
        return new Insertion(combined, after - before);
    }

    public record Insertion(List<Visit> visits, int extraMeters) {}

    private void balance(List<List<Visit>> routes, List<List<String>> reasons, long startId, Distance distance, Long returnStopId) {
        int[] meters = new int[routes.size()];
        int total = 0;
        for (int i = 0; i < routes.size(); i++) {
            meters[i] = pathMeters(startId, routes.get(i), distance, returnStopId);
            total += meters[i];
        }
        if (total == 0) {
            return;
        }
        int heavy = 0;
        for (int i = 1; i < meters.length; i++) {
            if (meters[i] > meters[heavy]) {
                heavy = i;
            }
        }
        if (meters[heavy] * 3 <= total * 2) {
            return;
        }
        int light = heavy == 0 ? 1 : 0;
        for (int i = 0; i < meters.length; i++) {
            if (i != heavy && meters[i] < meters[light]) {
                light = i;
            }
        }
        List<Trip> movable = tripsOf(routes.get(heavy));
        int bestTotalGrowth = Integer.MAX_VALUE;
        List<Visit> bestHeavy = null;
        List<Visit> bestLight = null;
        for (Trip trip : movable) {
            List<Visit> without = removeTrip(routes.get(heavy), trip.id());
            List<Visit> with = bestInsertion(routes.get(light), trip, startId, distance, returnStopId);
            int newHeavy = pathMeters(startId, without, distance, returnStopId);
            int newLight = pathMeters(startId, with, distance, returnStopId);
            int old = meters[heavy] + meters[light];
            int grown = (newHeavy + newLight) - old;
            int oldGap = Math.abs(meters[heavy] - meters[light]);
            int newGap = Math.abs(newHeavy - newLight);
            if (newGap < oldGap && grown <= Math.max(1, old * 15 / 100) && grown < bestTotalGrowth) {
                bestTotalGrowth = grown;
                bestHeavy = without;
                bestLight = with;
            }
        }
        if (bestHeavy != null) {
            routes.set(heavy, bestHeavy);
            routes.set(light, bestLight);
            reasons.get(light).add("Nhận thêm một chuyến để cân km giữa hai bác.");
        }
    }

    static List<Visit> bestInsertion(List<Visit> route, Trip trip, long startId, Distance distance, Long returnStopId) {
        List<Visit> best = null;
        int bestCost = Integer.MAX_VALUE;
        int stops = route.size();
        for (int pickupAt = 0; pickupAt <= stops; pickupAt++) {
            boolean attachPickup = pickupAt < stops && route.get(pickupAt).stopId() == trip.pickup();
            int dropoffStart = pickupAt;
            int dropoffEnd = stops + (attachPickup ? 0 : 1);
            for (int dropoffAt = dropoffStart; dropoffAt <= dropoffEnd; dropoffAt++) {
                List<Visit> candidate = apply(route, trip, pickupAt, attachPickup, dropoffAt);
                if (!precedenceOk(candidate)) {
                    continue;
                }
                int cost = pathMeters(startId, candidate, distance, returnStopId);
                if (cost < bestCost) {
                    bestCost = cost;
                    best = candidate;
                }
            }
            if (!attachPickup && trip.pickup() == trip.dropoff()) {
                break;
            }
        }
        if (best == null) {
            List<Visit> fallback = copy(route);
            Visit visit = new Visit(trip.pickup());
            visit.pickups.add(trip.id());
            if (trip.pickup() == trip.dropoff()) {
                visit.dropoffs.add(trip.id());
            }
            fallback.add(visit);
            if (trip.pickup() != trip.dropoff()) {
                Visit drop = new Visit(trip.dropoff());
                drop.dropoffs.add(trip.id());
                fallback.add(drop);
            }
            return fallback;
        }
        return best;
    }

    private static List<Visit> apply(List<Visit> route, Trip trip, int pickupAt, boolean attachPickup, int dropoffAt) {
        List<Visit> next = copy(route);
        int pickupIndex;
        if (attachPickup) {
            next.get(pickupAt).pickups.add(trip.id());
            pickupIndex = pickupAt;
        } else {
            Visit created = new Visit(trip.pickup());
            created.pickups.add(trip.id());
            next.add(pickupAt, created);
            pickupIndex = pickupAt;
        }
        if (trip.pickup() == trip.dropoff()) {
            next.get(pickupIndex).dropoffs.add(trip.id());
            return next;
        }
        int adjustedDrop = dropoffAt;
        if (!attachPickup && dropoffAt >= pickupAt) {
            adjustedDrop = dropoffAt + 1;
        }
        if (adjustedDrop < next.size() && next.get(adjustedDrop).stopId() == trip.dropoff() && adjustedDrop >= pickupIndex) {
            next.get(adjustedDrop).dropoffs.add(trip.id());
        } else {
            Visit drop = new Visit(trip.dropoff());
            drop.dropoffs.add(trip.id());
            int insertAt = Math.max(pickupIndex + 1, Math.min(adjustedDrop, next.size()));
            next.add(insertAt, drop);
        }
        return next;
    }

    static boolean precedenceOk(List<Visit> visits) {
        Set<Long> ids = new HashSet<>();
        for (Visit visit : visits) {
            ids.addAll(visit.pickups);
            ids.addAll(visit.dropoffs);
        }
        for (Long id : ids) {
            int pickup = indexOf(visits, id, true);
            int dropoff = indexOf(visits, id, false);
            if (pickup < 0 || dropoff < 0 || pickup > dropoff) {
                return false;
            }
        }
        return true;
    }

    private List<Visit> improve(List<Visit> visits, long startId, Distance distance, Long returnStopId) {
        List<Visit> current = copy(visits);
        boolean changed = true;
        while (changed) {
            changed = false;
            int base = pathMeters(startId, current, distance, returnStopId);
            for (int i = 0; i < current.size(); i++) {
                for (int j = i + 1; j < current.size(); j++) {
                    List<Visit> swapped = copy(current);
                    Visit tmp = swapped.get(i);
                    swapped.set(i, swapped.get(j));
                    swapped.set(j, tmp);
                    if (!precedenceOk(swapped)) {
                        continue;
                    }
                    int cost = pathMeters(startId, swapped, distance, returnStopId);
                    if (cost < base) {
                        current = swapped;
                        base = cost;
                        changed = true;
                    }
                }
            }
        }
        return current;
    }

    static int pathMeters(long startId, List<Visit> visits, Distance distance, Long returnStopId) {
        if (visits.isEmpty()) {
            if (returnStopId != null && startId != returnStopId) {
                return distance.meters(startId, returnStopId);
            }
            return 0;
        }
        int sum = distance.meters(startId, visits.get(0).stopId());
        for (int i = 1; i < visits.size(); i++) {
            sum += distance.meters(visits.get(i - 1).stopId(), visits.get(i).stopId());
        }
        long last = visits.get(visits.size() - 1).stopId();
        if (returnStopId != null && last != returnStopId) {
            sum += distance.meters(last, returnStopId);
        }
        return sum;
    }

    static List<Trip> tripsOf(List<Visit> visits) {
        Set<Long> seen = new HashSet<>();
        List<Trip> trips = new ArrayList<>();
        for (Visit visit : visits) {
            for (Long id : visit.pickups) {
                if (seen.add(id)) {
                    int drop = indexOf(visits, id, false);
                    trips.add(new Trip(id, visit.stopId(), visits.get(drop).stopId()));
                }
            }
        }
        return trips;
    }

    private static List<Visit> removeTrip(List<Visit> visits, long tripId) {
        List<Visit> next = new ArrayList<>();
        for (Visit visit : visits) {
            Visit copy = visit.copy();
            copy.pickups.remove(tripId);
            copy.dropoffs.remove(tripId);
            if (!copy.pickups.isEmpty() || !copy.dropoffs.isEmpty()) {
                next.add(copy);
            }
        }
        return next;
    }

    private static int indexOf(List<Visit> visits, long tripId, boolean pickup) {
        for (int i = 0; i < visits.size(); i++) {
            List<Long> ids = pickup ? visits.get(i).pickups : visits.get(i).dropoffs;
            if (ids.contains(tripId)) {
                return i;
            }
        }
        return -1;
    }

    private static List<Visit> copy(List<Visit> visits) {
        List<Visit> copy = new ArrayList<>();
        for (Visit visit : visits) {
            copy.add(visit.copy());
        }
        return copy;
    }

    private static String reason(Trip trip, int[] extras, int chosen) {
        StringBuilder text = new StringBuilder();
        text.append("Chuyến ").append(trip.pickup()).append("→").append(trip.dropoff()).append(" vào bác ").append(chosen + 1);
        text.append(" vì");
        for (int i = 0; i < extras.length; i++) {
            text.append(" bác ").append(i + 1).append(" thêm ").append(extras[i]).append(" m");
            if (i < extras.length - 1) {
                text.append(",");
            }
        }
        return text.toString();
    }

    private List<Route> toResult(List<List<Visit>> routes, List<List<String>> reasons, long startId, Distance distance, Long returnStopId) {
        List<Route> result = new ArrayList<>();
        for (int i = 0; i < routes.size(); i++) {
            List<Visit> visits = routes.get(i);
            result.add(new Route(visits, pathMeters(startId, visits, distance, returnStopId), reasons.get(i)));
        }
        return result;
    }
}
