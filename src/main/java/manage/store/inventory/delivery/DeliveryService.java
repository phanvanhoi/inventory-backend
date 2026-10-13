package manage.store.inventory.delivery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.delivery.RoutePlanner.Distance;
import manage.store.inventory.delivery.RoutePlanner.Insertion;
import manage.store.inventory.delivery.RoutePlanner.Route;
import manage.store.inventory.delivery.RoutePlanner.Trip;
import manage.store.inventory.delivery.RoutePlanner.Visit;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;

@Service
@Transactional
public class DeliveryService {

    private static final String WAITING = "WAITING";
    private static final String PLANNED = "PLANNED";
    private static final String SENDER_CONFIRMED = "SENDER_CONFIRMED";
    private static final String DELIVERED = "DELIVERED";
    private static final String CANCELLED = "CANCELLED";
    private static final String OPEN = "OPEN";
    private static final String ASSIGNED = "ASSIGNED";
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String LOCKED = "LOCKED";
    private static final String EN_ROUTE = "EN_ROUTE";
    private static final String DONE = "DONE";

    @PersistenceContext
    private EntityManager em;

    private final RoadDistance roadDistance;
    private final RoutePlanner routePlanner = new RoutePlanner();

    public DeliveryService(RoadDistance roadDistance) {
        this.roadDistance = roadDistance;
    }

    public List<DeliveryStop> stops() {
        return em.createQuery("select s from DeliveryStop s order by s.shop desc, s.stopName", DeliveryStop.class).getResultList();
    }

    public DeliveryStop saveStop(DeliveryStop input) {
        if (input.isShop()) {
            em.createQuery("update DeliveryStop s set s.shop = false").executeUpdate();
        }
        if (input.getStopId() == null) {
            input.setCreatedAt(LocalDateTime.now());
            em.persist(input);
            return input;
        }
        DeliveryStop stop = stop(input.getStopId());
        stop.setStopName(input.getStopName());
        stop.setAddress(input.getAddress());
        stop.setLatitude(input.getLatitude());
        stop.setLongitude(input.getLongitude());
        stop.setShop(input.isShop());
        stop.setUnitId(input.getUnitId());
        stop.setWarehouseId(input.getWarehouseId());
        return stop;
    }

    public List<DeliveryDriver> drivers() {
        return em.createQuery("select d from DeliveryDriver d order by d.driverId", DeliveryDriver.class).getResultList();
    }

    public DeliveryDriver saveDriver(DeliveryDriver input) {
        if (input.getDriverId() == null) {
            input.setCreatedAt(LocalDateTime.now());
            em.persist(input);
            return input;
        }
        DeliveryDriver driver = driver(input.getDriverId());
        driver.setDriverName(input.getDriverName());
        driver.setPhone(input.getPhone());
        driver.setActive(input.isActive());
        return driver;
    }

    public DeliverySetting settings() {
        DeliverySetting setting = em.find(DeliverySetting.class, (byte) 1);
        if (setting == null) {
            setting = new DeliverySetting();
            em.persist(setting);
        }
        return setting;
    }

    public DeliverySetting saveSettings(DeliverySetting input) {
        DeliverySetting setting = settings();
        setting.setDistanceBaseUrl(input.getDistanceBaseUrl());
        setting.setDetourLimitMeters(input.getDetourLimitMeters() > 0 ? input.getDetourLimitMeters() : 5000);
        setting.setCostPerKm(Math.max(0, input.getCostPerKm()));
        return setting;
    }

    public DeliveryBook createBook(BookRequest request, Long userId) {
        if (request.pickupStopId().equals(request.dropoffStopId())) {
            throw new BusinessException("Điểm lấy và điểm giao phải khác nhau.");
        }
        stop(request.pickupStopId());
        stop(request.dropoffStopId());
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessException("Book cần ít nhất một dòng hàng.");
        }
        DeliveryBook book = new DeliveryBook();
        book.setPickupStopId(request.pickupStopId());
        book.setDropoffStopId(request.dropoffStopId());
        book.setServiceDate(request.serviceDate());
        book.setNote(request.note());
        book.setRequestSetId(request.requestSetId());
        book.setCreatedBy(userId);
        book.setCreatedAt(LocalDateTime.now());
        book.setStatus(WAITING);

        DeliveryTrip trip = openTrip(request.serviceDate(), request.pickupStopId(), request.dropoffStopId());
        boolean merged = trip != null && !passed(trip);
        if (!merged) {
            trip = new DeliveryTrip();
            trip.setServiceDate(request.serviceDate());
            trip.setPickupStopId(request.pickupStopId());
            trip.setDropoffStopId(request.dropoffStopId());
            trip.setStatus(OPEN);
            em.persist(trip);
            em.flush();
        }
        book.setTripId(trip.getTripId());
        if (ASSIGNED.equals(trip.getStatus())) {
            book.setStatus(PLANNED);
        }
        em.persist(book);
        em.flush();
        for (LineRequest line : request.lines()) {
            DeliveryBookLine entity = new DeliveryBookLine();
            entity.setBookId(book.getBookId());
            entity.setDescription(line.description());
            entity.setQuantity(line.quantity());
            entity.setUnitLabel(line.unitLabel());
            em.persist(entity);
        }
        if (merged && ASSIGNED.equals(trip.getStatus())) {
            noticeForTrip(trip, "Ghép thêm hàng vào chuyến đã có, km không tăng.");
        } else if (!merged) {
            trySuddenInsert(book, trip, false);
        }
        return book;
    }

    public void cancelBook(Long bookId) {
        DeliveryBook book = book(bookId);
        if (book.getSenderConfirmedAt() != null || CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Book đã xác nhận đưa hàng thì không hủy.");
        }
        book.setStatus(CANCELLED);
        DriverDayPlan plan = planOfTrip(book.getTripId());
        if (plan != null && !DRAFT.equals(plan.getStatus())) {
            stripCancelledTrip(plan, book);
            notice(plan.getDriverId(), plan.getServiceDate(), "Hủy book " + bookId + ". Gỡ hàng khỏi phiếu nếu bác chưa lấy. " + slipText(plan));
        }
    }

    public void confirmSender(Long bookId, boolean onBehalf, Long userId) {
        DeliveryBook book = book(bookId);
        if (CANCELLED.equals(book.getStatus())) {
            throw new BusinessException("Book đã hủy.");
        }
        book.setSenderConfirmedAt(LocalDateTime.now());
        book.setSenderConfirmedBy(userId);
        book.setSenderOnBehalf(onBehalf);
        book.setStatus(SENDER_CONFIRMED);
    }

    public void confirmReceiver(Long bookId, boolean onBehalf, Long userId) {
        DeliveryBook book = book(bookId);
        if (book.getSenderConfirmedAt() == null) {
            throw new BusinessException("Người gửi chưa xác nhận đã đưa hàng.");
        }
        book.setReceiverConfirmedAt(LocalDateTime.now());
        book.setReceiverConfirmedBy(userId);
        book.setReceiverOnBehalf(onBehalf);
        book.setStatus(DELIVERED);
    }

    public String acceptDetour(Long bookId) {
        DeliveryBook book = book(bookId);
        LocalDate original = book.getServiceDate();
        DeliveryTrip trip = book.getTripId() == null ? null : em.find(DeliveryTrip.class, book.getTripId());
        if (trip == null) {
            throw new BusinessException("Book chưa gắn chuyến.");
        }
        if (ASSIGNED.equals(trip.getStatus())) {
            return "Chuyến đã nằm trong lịch.";
        }
        boolean attached = trySuddenInsert(book, trip, true);
        if (attached) {
            return "Đã gắn tiện chuyến.";
        }
        if (!original.equals(book.getServiceDate())) {
            return "Không còn tiện chuyến. Book chuyển sang " + book.getServiceDate() + ".";
        }
        throw new BusinessException("Không gắn được. Kiểm tra lịch trong ngày và nguồn km.");
    }

    public Map<String, Object> plan(LocalDate date) {
        List<DriverDayPlan> existing = plans(date);
        if (existing.stream().anyMatch(plan -> !DRAFT.equals(plan.getStatus()))) {
            throw new BusinessException("Ngày này đã chốt lịch. Không xếp lại.");
        }
        for (DriverDayPlan plan : existing) {
            deletePlan(plan);
        }
        em.flush();

        DeliveryStop shop = shop();
        List<DeliveryDriver> fleet = drivers().stream().filter(DeliveryDriver::isActive).limit(2).toList();
        if (fleet.isEmpty()) {
            throw new BusinessException("Chưa có tài xế.");
        }
        List<DeliveryTrip> trips = trips(date).stream().filter(this::hasActiveBook).toList();
        if (trips.isEmpty()) {
            return day(date);
        }
        List<DeliveryStop> stops = stopsFor(trips, shop);
        int[][] matrix = roadDistance.meters(stops, settings().getDistanceBaseUrl());
        Map<Long, Integer> index = new HashMap<>();
        for (int i = 0; i < stops.size(); i++) {
            index.put(stops.get(i).getStopId(), i);
        }
        Distance distance = (from, to) -> matrix[index.get(from)][index.get(to)];
        List<Trip> inputs = trips.stream().map(trip -> new Trip(trip.getTripId(), trip.getPickupStopId(), trip.getDropoffStopId())).toList();
        List<Route> routes = routePlanner.plan(inputs, shop.getStopId(), distance, fleet.size(), true);
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            if (route.visits().isEmpty()) {
                continue;
            }
            DriverDayPlan plan = new DriverDayPlan();
            plan.setDriverId(fleet.get(i).getDriverId());
            plan.setServiceDate(date);
            plan.setStatus(DRAFT);
            plan.setReturnToShop(true);
            plan.setTotalMeters(route.meters());
            plan.setExplanation(String.join("\n", route.reasons()));
            em.persist(plan);
            em.flush();
            saveVisits(plan.getPlanId(), route.visits());
            for (Visit visit : route.visits()) {
                for (Long tripId : visit.pickups()) {
                    markAssigned(tripId);
                }
            }
        }
        return day(date);
    }

    public Map<String, Object> publish(LocalDate date) {
        List<DriverDayPlan> list = requirePlans(date, DRAFT);
        for (DriverDayPlan plan : list) {
            plan.setStatus(PUBLISHED);
            notice(plan.getDriverId(), date, slipText(plan));
        }
        return day(date);
    }

    public Map<String, Object> lock(LocalDate date, Long startStopId) {
        List<DriverDayPlan> list = requirePlans(date, PUBLISHED);
        DeliveryStop shop = shop();
        DeliveryStop start = startStopId == null ? shop : stop(startStopId);
        for (DriverDayPlan plan : list) {
            List<Visit> current = toVisits(plan.getPlanId());
            String before = sequence(current);
            List<Trip> own = RoutePlanner.tripsOf(current);
            if (!own.isEmpty()) {
                Long returnTo = plan.isReturnToShop() ? shop.getStopId() : null;
                Route route = routePlanner.plan(own, start.getStopId(), cachedDistance(date, shop), 1, returnTo).get(0);
                if (!route.visits().isEmpty() && route.visits().get(0).pickups().isEmpty()) {
                    throw new BusinessException("Điểm đầu phải là điểm lấy vì xe đang trống.");
                }
                replaceVisits(plan.getPlanId(), route.visits());
                plan.setTotalMeters(route.meters());
                plan.setExplanation(String.join("\n", route.reasons()));
                String after = sequence(route.visits());
                plan.setStatus(LOCKED);
                String lead = before.equals(after)
                        ? "Khóa thứ tự, không đổi. "
                        : "Thứ tự buổi sáng đổi. In lại phiếu hoặc nói lại. ";
                notice(plan.getDriverId(), date, lead + slipText(plan));
            } else {
                plan.setStatus(LOCKED);
            }
        }
        return day(date);
    }

    public void depart(Long planId) {
        DriverDayPlan plan = plan(planId);
        if (!LOCKED.equals(plan.getStatus())) {
            throw new BusinessException("Chỉ xuất phát lịch đã khóa.");
        }
        plan.setStatus(EN_ROUTE);
    }

    public void finish(Long planId) {
        DriverDayPlan plan = plan(planId);
        plan.setStatus(DONE);
        for (DeliveryBook book : books(plan.getServiceDate())) {
            if (CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus()) || book.getSenderConfirmedAt() != null) {
                continue;
            }
            DriverDayPlan owner = planOfTrip(book.getTripId());
            if (owner == null || !owner.getPlanId().equals(plan.getPlanId())) {
                continue;
            }
            DeliveryTrip fresh = new DeliveryTrip();
            fresh.setServiceDate(plan.getServiceDate().plusDays(1));
            fresh.setPickupStopId(book.getPickupStopId());
            fresh.setDropoffStopId(book.getDropoffStopId());
            fresh.setStatus(OPEN);
            em.persist(fresh);
            em.flush();
            book.setTripId(fresh.getTripId());
            book.setServiceDate(fresh.getServiceDate());
            book.setStatus(WAITING);
        }
    }

    public void dropReturn(Long planId) {
        DriverDayPlan plan = plan(planId);
        if (EN_ROUTE.equals(plan.getStatus()) || DONE.equals(plan.getStatus())) {
            throw new BusinessException("Lịch đã chạy thì không bỏ đoạn về.");
        }
        plan.setReturnToShop(false);
        List<Visit> visits = toVisits(plan.getPlanId());
        DeliveryStop shop = shop();
        plan.setTotalMeters(RoutePlanner.pathMeters(shop.getStopId(), visits, cachedDistance(plan.getServiceDate(), shop), null));
    }

    public void ping(Long driverId, double latitude, double longitude) {
        driver(driverId);
        DriverLocation location = em.find(DriverLocation.class, driverId);
        if (location == null) {
            location = new DriverLocation();
            location.setDriverId(driverId);
            em.persist(location);
        }
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setRecordedAt(LocalDateTime.now());
    }

    public void ack(Long noticeId) {
        DispatchNotice notice = em.find(DispatchNotice.class, noticeId);
        if (notice == null) {
            throw new ResourceNotFoundException("Không thấy phiếu cần báo");
        }
        notice.setAcknowledgedAt(LocalDateTime.now());
    }

    public List<DispatchNotice> notices() {
        return em.createQuery("select n from DispatchNotice n where n.acknowledgedAt is null order by n.createdAt desc", DispatchNotice.class)
                .getResultList();
    }

    public Map<String, Object> day(LocalDate date) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("date", date);
        body.put("shop", stops().stream().filter(DeliveryStop::isShop).findFirst().orElse(null));
        body.put("books", books(date).stream().map(this::bookView).toList());
        body.put("plans", plans(date).stream().map(this::planView).toList());
        body.put("notices", notices().stream().filter(notice -> date.equals(notice.getServiceDate())).toList());
        body.put("locations", em.createQuery("select l from DriverLocation l", DriverLocation.class).getResultList());
        DeliverySetting setting = settings();
        body.put("costPerKm", setting.getCostPerKm());
        body.put("detourLimitMeters", setting.getDetourLimitMeters());
        return body;
    }

    private Map<String, Object> bookView(DeliveryBook book) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("book", book);
        view.put("lines", lines(book.getBookId()));
        view.put("pickup", stop(book.getPickupStopId()));
        view.put("dropoff", stop(book.getDropoffStopId()));
        return view;
    }

    private Map<String, Object> planView(DriverDayPlan plan) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("plan", plan);
        view.put("driver", driver(plan.getDriverId()));
        List<Map<String, Object>> visits = new ArrayList<>();
        for (PlanVisit visit : visits(plan.getPlanId())) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("visit", visit);
            row.put("stop", stop(visit.getStopId()));
            row.put("actions", actions(visit.getVisitId()));
            visits.add(row);
        }
        view.put("visits", visits);
        long cost = Math.round(plan.getTotalMeters() / 1000.0 * settings().getCostPerKm());
        view.put("estimatedCost", cost);
        return view;
    }

    private boolean trySuddenInsert(DeliveryBook book, DeliveryTrip trip, boolean allowEnRoute) {
        List<DriverDayPlan> dayPlans = plans(book.getServiceDate()).stream()
                .filter(plan -> !DRAFT.equals(plan.getStatus()) && !DONE.equals(plan.getStatus()))
                .toList();
        if (dayPlans.isEmpty()) {
            return false;
        }
        boolean enRoute = dayPlans.stream().anyMatch(plan -> EN_ROUTE.equals(plan.getStatus()));
        if (enRoute && !allowEnRoute) {
            StringBuilder text = new StringBuilder();
            text.append("Có book mới ").append(book.getBookId()).append(" khi bác đang đi. Gọi điện, rồi bấm gắn nếu còn tiện. ");
            for (DriverDayPlan plan : dayPlans) {
                if (!EN_ROUTE.equals(plan.getStatus())) {
                    continue;
                }
                DeliveryDriver person = driver(plan.getDriverId());
                text.append("Gọi ").append(person.getDriverName()).append(" ").append(person.getPhone());
                text.append(": có hàng tiện đường, chỉ nhận nếu lệch thêm không quá ");
                text.append(settings().getDetourLimitMeters()).append(" mét. ");
            }
            notice(dayPlans.get(0).getDriverId(), book.getServiceDate(), text.toString());
            return false;
        }
        DeliveryStop shop = shop();
        int bestExtra = Integer.MAX_VALUE;
        DriverDayPlan bestPlan = null;
        Insertion best = null;
        for (DriverDayPlan plan : dayPlans) {
            List<Visit> visits = toVisits(plan.getPlanId());
            int from = firstOpenIndex(plan, visits);
            Insertion insertion = routePlanner.insertIntoRemaining(
                    visits, from, new Trip(trip.getTripId(), trip.getPickupStopId(), trip.getDropoffStopId()),
                    shop.getStopId(), cachedDistance(book.getServiceDate(), shop), plan.isReturnToShop());
            if (insertion.extraMeters() < bestExtra) {
                bestExtra = insertion.extraMeters();
                bestPlan = plan;
                best = insertion;
            }
        }
        if (bestPlan == null || bestExtra > settings().getDetourLimitMeters()) {
            book.setServiceDate(book.getServiceDate().plusDays(1));
            trip.setServiceDate(book.getServiceDate());
            return false;
        }
        replaceVisits(bestPlan.getPlanId(), best.visits());
        bestPlan.setTotalMeters(bestPlan.getTotalMeters() + bestExtra);
        trip.setStatus(ASSIGNED);
        book.setStatus(PLANNED);
        book.setSudden(true);
        DeliveryDriver person = driver(bestPlan.getDriverId());
        String lead = EN_ROUTE.equals(bestPlan.getStatus())
                ? "Tiện chuyến khi đang đi, thêm " + bestExtra + " m. Gọi " + person.getDriverName() + " " + person.getPhone() + " rồi in lại phiếu. "
                : "Tiện chuyến book " + book.getBookId() + ", thêm " + bestExtra + " m. Nói với bác trước khi đi. ";
        notice(bestPlan.getDriverId(), bestPlan.getServiceDate(), lead + slipText(bestPlan));
        return true;
    }

    private void stripCancelledTrip(DriverDayPlan plan, DeliveryBook book) {
        if (book.getTripId() == null) {
            return;
        }
        boolean others = booksOfTrip(book.getTripId()).stream()
                .anyMatch(other -> !other.getBookId().equals(book.getBookId()) && !CANCELLED.equals(other.getStatus()));
        if (others) {
            return;
        }
        List<Visit> visits = toVisits(plan.getPlanId());
        if (EN_ROUTE.equals(plan.getStatus())) {
            int open = firstOpenIndex(plan, visits);
            for (int i = 0; i < open; i++) {
                Visit visit = visits.get(i);
                if (visit.pickups().contains(book.getTripId()) || visit.dropoffs().contains(book.getTripId())) {
                    return;
                }
            }
        }
        List<Visit> next = new ArrayList<>();
        for (Visit visit : visits) {
            Visit copy = visit.copy();
            copy.pickups().remove(book.getTripId());
            copy.dropoffs().remove(book.getTripId());
            if (!copy.pickups().isEmpty() || !copy.dropoffs().isEmpty()) {
                next.add(copy);
            }
        }
        replaceVisits(plan.getPlanId(), next);
        DeliveryStop shop = shop();
        Long returnTo = plan.isReturnToShop() ? shop.getStopId() : null;
        plan.setTotalMeters(RoutePlanner.pathMeters(shop.getStopId(), next, cachedDistance(plan.getServiceDate(), shop), returnTo));
    }

    private String sequence(List<Visit> visits) {
        StringBuilder text = new StringBuilder();
        for (Visit visit : visits) {
            text.append(visit.stopId()).append(':').append(visit.pickups()).append(visit.dropoffs()).append('|');
        }
        return text.toString();
    }

    private Distance cachedDistance(LocalDate date, DeliveryStop shop) {
        List<DeliveryStop> stops = new ArrayList<>(stops());
        if (stops.stream().noneMatch(stop -> stop.getStopId().equals(shop.getStopId()))) {
            stops.add(shop);
        }
        List<DeliveryStop> used = stops.stream().filter(stop -> stop.getLatitude() != null).toList();
        int[][] matrix = roadDistance.meters(used, settings().getDistanceBaseUrl());
        Map<Long, Integer> index = new HashMap<>();
        for (int i = 0; i < used.size(); i++) {
            index.put(used.get(i).getStopId(), i);
        }
        return (from, to) -> matrix[index.get(from)][index.get(to)];
    }

    private int firstOpenIndex(DriverDayPlan plan, List<Visit> visits) {
        if (!EN_ROUTE.equals(plan.getStatus())) {
            return 0;
        }
        DriverLocation location = em.find(DriverLocation.class, plan.getDriverId());
        if (location == null) {
            return 0;
        }
        int closest = 0;
        double best = Double.MAX_VALUE;
        for (int i = 0; i < visits.size(); i++) {
            DeliveryStop stop = stop(visits.get(i).stopId());
            if (stop.getLatitude() == null) {
                continue;
            }
            double km = Geo.km(location.getLatitude(), location.getLongitude(), stop.getLatitude(), stop.getLongitude());
            if (km < best) {
                best = km;
                closest = i;
            }
        }
        return closest;
    }

    private boolean passed(DeliveryTrip trip) {
        for (DriverDayPlan plan : plans(trip.getServiceDate())) {
            if (!EN_ROUTE.equals(plan.getStatus())) {
                continue;
            }
            List<Visit> visits = toVisits(plan.getPlanId());
            int open = firstOpenIndex(plan, visits);
            for (int i = 0; i < open; i++) {
                Visit visit = visits.get(i);
                if (visit.pickups().contains(trip.getTripId()) || visit.dropoffs().contains(trip.getTripId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void saveVisits(Long planId, List<Visit> visits) {
        int seq = 0;
        for (Visit visit : visits) {
            PlanVisit entity = new PlanVisit();
            entity.setPlanId(planId);
            entity.setSeqNo(seq++);
            entity.setStopId(visit.stopId());
            em.persist(entity);
            em.flush();
            for (Long tripId : visit.pickups()) {
                saveAction(entity.getVisitId(), tripId, "PICKUP");
            }
            for (Long tripId : visit.dropoffs()) {
                saveAction(entity.getVisitId(), tripId, "DROPOFF");
            }
        }
    }

    private void replaceVisits(Long planId, List<Visit> visits) {
        for (PlanVisit visit : visits(planId)) {
            for (PlanVisitAction action : actions(visit.getVisitId())) {
                em.remove(action);
            }
            em.remove(visit);
        }
        em.flush();
        saveVisits(planId, visits);
    }

    private void deletePlan(DriverDayPlan plan) {
        for (PlanVisit visit : visits(plan.getPlanId())) {
            for (PlanVisitAction action : actions(visit.getVisitId())) {
                em.remove(action);
            }
            em.remove(visit);
        }
        em.remove(plan);
    }

    private void saveAction(Long visitId, Long tripId, String type) {
        PlanVisitAction action = new PlanVisitAction();
        action.setVisitId(visitId);
        action.setTripId(tripId);
        action.setActionType(type);
        em.persist(action);
    }

    private List<Visit> toVisits(Long planId) {
        List<Visit> visits = new ArrayList<>();
        for (PlanVisit entity : visits(planId)) {
            Visit visit = new Visit(entity.getStopId());
            for (PlanVisitAction action : actions(entity.getVisitId())) {
                if ("PICKUP".equals(action.getActionType())) {
                    visit.pickups().add(action.getTripId());
                } else {
                    visit.dropoffs().add(action.getTripId());
                }
            }
            visits.add(visit);
        }
        return visits;
    }

    private void markAssigned(Long tripId) {
        DeliveryTrip trip = em.find(DeliveryTrip.class, tripId);
        trip.setStatus(ASSIGNED);
        for (DeliveryBook book : booksOfTrip(tripId)) {
            if (WAITING.equals(book.getStatus())) {
                book.setStatus(PLANNED);
            }
        }
    }

    private String slipText(DriverDayPlan plan) {
        DeliveryDriver person = driver(plan.getDriverId());
        StringBuilder text = new StringBuilder();
        text.append(person.getDriverName()).append(" ").append(person.getPhone()).append(". ");
        text.append(plan.getServiceDate()).append(": ");
        for (PlanVisit visit : visits(plan.getPlanId())) {
            DeliveryStop stop = stop(visit.getStopId());
            text.append(visit.getSeqNo() + 1).append(". ").append(stop.getStopName());
            if (stop.getAddress() != null && !stop.getAddress().isBlank()) {
                text.append(" — ").append(stop.getAddress());
            }
            for (PlanVisitAction action : actions(visit.getVisitId())) {
                boolean pickup = "PICKUP".equals(action.getActionType());
                text.append(pickup ? " Lấy:" : " Giao:");
                for (DeliveryBook book : booksOfTrip(action.getTripId())) {
                    if (CANCELLED.equals(book.getStatus())) {
                        continue;
                    }
                    Long expected = pickup ? book.getPickupStopId() : book.getDropoffStopId();
                    if (!stop.getStopId().equals(expected)) {
                        continue;
                    }
                    for (DeliveryBookLine line : lines(book.getBookId())) {
                        text.append(" ").append(line.getDescription()).append(" ").append(line.getQuantity());
                        if (line.getUnitLabel() != null) {
                            text.append(" ").append(line.getUnitLabel());
                        }
                    }
                    if (book.isSudden()) {
                        text.append(" (đột xuất)");
                    }
                }
            }
            text.append(". ");
        }
        if (plan.isReturnToShop()) {
            text.append("Về cửa hàng.");
        }
        return text.toString();
    }

    private void noticeForTrip(DeliveryTrip trip, String message) {
        DriverDayPlan plan = planOfTrip(trip.getTripId());
        if (plan != null) {
            notice(plan.getDriverId(), trip.getServiceDate(), message);
        }
    }

    private void notice(Long driverId, LocalDate date, String message) {
        DispatchNotice notice = new DispatchNotice();
        notice.setDriverId(driverId);
        notice.setServiceDate(date);
        notice.setMessage(message);
        notice.setCreatedAt(LocalDateTime.now());
        em.persist(notice);
    }

    private DeliveryTrip openTrip(LocalDate date, Long pickup, Long dropoff) {
        List<DeliveryTrip> found = em.createQuery(
                        "select t from DeliveryTrip t where t.serviceDate = :date and t.pickupStopId = :pickup and t.dropoffStopId = :dropoff",
                        DeliveryTrip.class)
                .setParameter("date", date)
                .setParameter("pickup", pickup)
                .setParameter("dropoff", dropoff)
                .getResultList();
        return found.stream().filter(this::hasActiveBook).min(Comparator.comparing(DeliveryTrip::getTripId)).orElse(found.isEmpty() ? null : found.get(0));
    }

    private boolean hasActiveBook(DeliveryTrip trip) {
        return booksOfTrip(trip.getTripId()).stream().anyMatch(book -> !CANCELLED.equals(book.getStatus()));
    }

    private List<DeliveryStop> stopsFor(List<DeliveryTrip> trips, DeliveryStop shop) {
        Map<Long, DeliveryStop> map = new HashMap<>();
        map.put(shop.getStopId(), shop);
        for (DeliveryTrip trip : trips) {
            map.put(trip.getPickupStopId(), stop(trip.getPickupStopId()));
            map.put(trip.getDropoffStopId(), stop(trip.getDropoffStopId()));
        }
        return new ArrayList<>(map.values());
    }

    private List<DriverDayPlan> requirePlans(LocalDate date, String status) {
        List<DriverDayPlan> list = plans(date).stream().filter(plan -> status.equals(plan.getStatus())).toList();
        if (list.isEmpty()) {
            throw new BusinessException("Không có lịch trạng thái " + status + " cho ngày " + date);
        }
        return list;
    }

    private DeliveryStop shop() {
        return stops().stream().filter(DeliveryStop::isShop).findFirst()
                .orElseThrow(() -> new BusinessException("Chưa đặt điểm cửa hàng."));
    }

    private DeliveryStop stop(Long id) {
        DeliveryStop stop = em.find(DeliveryStop.class, id);
        if (stop == null) {
            throw new ResourceNotFoundException("Không thấy điểm " + id);
        }
        return stop;
    }

    private DeliveryDriver driver(Long id) {
        DeliveryDriver driver = em.find(DeliveryDriver.class, id);
        if (driver == null) {
            throw new ResourceNotFoundException("Không thấy tài xế " + id);
        }
        return driver;
    }

    private DeliveryBook book(Long id) {
        DeliveryBook book = em.find(DeliveryBook.class, id);
        if (book == null) {
            throw new ResourceNotFoundException("Không thấy book " + id);
        }
        return book;
    }

    private DriverDayPlan plan(Long id) {
        DriverDayPlan plan = em.find(DriverDayPlan.class, id);
        if (plan == null) {
            throw new ResourceNotFoundException("Không thấy lịch " + id);
        }
        return plan;
    }

    private List<DriverDayPlan> plans(LocalDate date) {
        return em.createQuery("select p from DriverDayPlan p where p.serviceDate = :date order by p.planId", DriverDayPlan.class)
                .setParameter("date", date)
                .getResultList();
    }

    private List<DeliveryTrip> trips(LocalDate date) {
        return em.createQuery("select t from DeliveryTrip t where t.serviceDate = :date", DeliveryTrip.class)
                .setParameter("date", date)
                .getResultList();
    }

    private List<DeliveryBook> books(LocalDate date) {
        return em.createQuery("select b from DeliveryBook b where b.serviceDate = :date order by b.bookId", DeliveryBook.class)
                .setParameter("date", date)
                .getResultList();
    }

    private List<DeliveryBook> booksOfTrip(Long tripId) {
        return em.createQuery("select b from DeliveryBook b where b.tripId = :tripId", DeliveryBook.class)
                .setParameter("tripId", tripId)
                .getResultList();
    }

    private List<DeliveryBookLine> lines(Long bookId) {
        return em.createQuery("select l from DeliveryBookLine l where l.bookId = :bookId", DeliveryBookLine.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    private List<PlanVisit> visits(Long planId) {
        return em.createQuery("select v from PlanVisit v where v.planId = :planId order by v.seqNo", PlanVisit.class)
                .setParameter("planId", planId)
                .getResultList();
    }

    private List<PlanVisitAction> actions(Long visitId) {
        return em.createQuery("select a from PlanVisitAction a where a.visitId = :visitId", PlanVisitAction.class)
                .setParameter("visitId", visitId)
                .getResultList();
    }

    private DriverDayPlan planOfTrip(Long tripId) {
        if (tripId == null) {
            return null;
        }
        List<PlanVisitAction> actions = em.createQuery("select a from PlanVisitAction a where a.tripId = :tripId", PlanVisitAction.class)
                .setParameter("tripId", tripId)
                .setMaxResults(1)
                .getResultList();
        if (actions.isEmpty()) {
            return null;
        }
        PlanVisit visit = em.find(PlanVisit.class, actions.get(0).getVisitId());
        return visit == null ? null : em.find(DriverDayPlan.class, visit.getPlanId());
    }

    public record LineRequest(String description, BigDecimal quantity, String unitLabel) {}

    public record BookRequest(Long pickupStopId, Long dropoffStopId, LocalDate serviceDate, String note, Long requestSetId, List<LineRequest> lines) {
        public BookRequest {
            Objects.requireNonNull(pickupStopId);
            Objects.requireNonNull(dropoffStopId);
            Objects.requireNonNull(serviceDate);
        }
    }
}
