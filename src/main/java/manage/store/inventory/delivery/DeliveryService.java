package manage.store.inventory.delivery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import manage.store.inventory.delivery.RoutePlanner.Distance;
import manage.store.inventory.delivery.RoutePlanner.Insertion;
import manage.store.inventory.delivery.RoutePlanner.Route;
import manage.store.inventory.delivery.RoutePlanner.Trip;
import manage.store.inventory.delivery.RoutePlanner.Visit;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;

@Service
public class DeliveryService {

    private static final String WAITING = "WAITING";
    private static final String PLANNED = "PLANNED";
    private static final String SENDER_CONFIRMED = "SENDER_CONFIRMED";
    private static final String DELIVERED = "DELIVERED";
    private static final String CANCELLED = "CANCELLED";
    private static final String OPEN = "OPEN";
    private static final String ASSIGNED = "ASSIGNED";
    private static final String CLOSED = "CLOSED";
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String LOCKED = "LOCKED";
    private static final String EN_ROUTE = "EN_ROUTE";
    private static final String DONE = "DONE";
    private static final String STRAIGHT = "Đang dùng đường thẳng vì không lấy được km Goong.";
    private static final double PASS_RADIUS_KM = 0.15;

    @PersistenceContext
    private EntityManager em;

    private final RoadDistance roadDistance;
    private final RoutePlanner routePlanner = new RoutePlanner();
    private final TransactionTemplate tx;
    private final Map<String, List<List<Double>>> routeCache = new ConcurrentHashMap<>();

    @Value("${goong.api-key:}")
    private String vietmapEnvKey;

    public DeliveryService(RoadDistance roadDistance, PlatformTransactionManager transactions) {
        this.roadDistance = roadDistance;
        this.tx = new TransactionTemplate(transactions);
    }

    @Transactional(readOnly = true)
    public List<DeliveryStop> stops() {
        return em.createQuery("select s from DeliveryStop s order by s.shop desc, s.stopName", DeliveryStop.class).getResultList();
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public List<DeliveryDriver> drivers() {
        return em.createQuery("select d from DeliveryDriver d order by d.driverId", DeliveryDriver.class).getResultList();
    }

    @Transactional
    public DeliveryDriver saveDriver(DeliveryDriver input) {
        if (input.getUserId() != null) {
            List<DeliveryDriver> taken = em.createQuery("select d from DeliveryDriver d where d.userId = :userId", DeliveryDriver.class)
                    .setParameter("userId", input.getUserId())
                    .getResultList();
            boolean used = taken.stream().anyMatch(driver -> !driver.getDriverId().equals(input.getDriverId()));
            if (used) {
                throw new BusinessException("User này đã gắn với tài xế khác.");
            }
        }
        if (input.getDriverId() == null) {
            input.setCreatedAt(LocalDateTime.now());
            if (!input.isActive()) {
                input.setActive(true);
            }
            em.persist(input);
            return input;
        }
        DeliveryDriver driver = driver(input.getDriverId());
        driver.setDriverName(input.getDriverName());
        driver.setPhone(input.getPhone());
        driver.setActive(input.isActive());
        driver.setUserId(input.getUserId());
        return driver;
    }

    @Transactional
    public DeliverySetting settings() {
        DeliverySetting setting = em.find(DeliverySetting.class, (byte) 1);
        if (setting == null) {
            setting = new DeliverySetting();
            em.persist(setting);
        }
        markVietmap(setting);
        return setting;
    }

    @Transactional
    public DeliverySetting saveSettings(DeliverySetting input) {
        DeliverySetting setting = settings();
        if (input.getDistanceBaseUrl() != null && !input.getDistanceBaseUrl().isBlank()) {
            setting.setDistanceBaseUrl(input.getDistanceBaseUrl());
        }
        setting.setDetourLimitMeters(input.getDetourLimitMeters() > 0 ? input.getDetourLimitMeters() : 5000);
        setting.setCostPerKm(Math.max(0, input.getCostPerKm()));
        if (input.getVietmapApiKey() != null && !input.getVietmapApiKey().isBlank()) {
            setting.setVietmapApiKey(input.getVietmapApiKey().trim());
            if (input.getVietmapTileKey() == null || input.getVietmapTileKey().isBlank()) {
                setting.setVietmapTileKey(null);
            }
        }
        if (input.getVietmapTileKey() != null && !input.getVietmapTileKey().isBlank()) {
            setting.setVietmapTileKey(input.getVietmapTileKey().trim());
        }
        markVietmap(setting);
        return setting;
    }

    public DeliveryBook createBook(BookRequest request, Long userId) {
        NewBook created = tx.execute(status -> persistBook(request, userId));
        if (created.place()) {
            placeOnRoute(created.bookId(), false, true);
        }
        return tx.execute(status -> book(created.bookId()));
    }

    public void cancelBook(Long bookId) {
        CancelPreview preview = tx.execute(status -> previewCancel(bookId));
        MatrixDistance matrix = preview.recompute()
                ? matrixOf(preview.stops(), preview.apiKey(), preview.root())
                : null;
        int meters = preview.recompute()
                ? RoutePlanner.pathMeters(preview.originId(), preview.next(), matrix, preview.returnTo())
                : 0;
        tx.executeWithoutResult(status -> commitCancel(bookId, preview, meters));
    }

    @Transactional
    public void confirmSender(Long bookId, boolean onBehalf, Long userId) {
        DeliveryBook book = book(bookId);
        if (CANCELLED.equals(book.getStatus())) {
            throw new BusinessException("Book đã hủy.");
        }
        if (DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Book đã giao.");
        }
        if (book.getSenderConfirmedAt() != null) {
            return;
        }
        if (!WAITING.equals(book.getStatus()) && !PLANNED.equals(book.getStatus())) {
            throw new BusinessException("Book không còn ở bước người gửi xác nhận.");
        }
        book.setSenderConfirmedAt(LocalDateTime.now());
        book.setSenderConfirmedBy(userId);
        book.setSenderOnBehalf(onBehalf);
        book.setStatus(SENDER_CONFIRMED);
    }

    @Transactional
    public void confirmReceiver(Long bookId, boolean onBehalf, Long userId) {
        DeliveryBook book = book(bookId);
        if (CANCELLED.equals(book.getStatus())) {
            throw new BusinessException("Book đã hủy.");
        }
        if (DELIVERED.equals(book.getStatus()) || book.getReceiverConfirmedAt() != null) {
            return;
        }
        if (book.getSenderConfirmedAt() == null) {
            throw new BusinessException("Người gửi chưa xác nhận đã đưa hàng.");
        }
        book.setReceiverConfirmedAt(LocalDateTime.now());
        book.setReceiverConfirmedBy(userId);
        book.setReceiverOnBehalf(onBehalf);
        book.setStatus(DELIVERED);
    }

    public String acceptDetour(Long bookId) {
        AcceptPrep prep = tx.execute(status -> prepAccept(bookId));
        if (prep.message() != null) {
            return prep.message();
        }
        placeOnRoute(prep.bookId(), true, true);
        return tx.execute(status -> acceptMessage(prep.bookId(), prep.originalDate()));
    }

    public Map<String, Object> plan(LocalDate date) {
        PlanInput input = tx.execute(status -> readPlan(date));
        MatrixDistance matrix = input.batches().isEmpty() ? null : matrixOf(input.stops(), input.apiKey(), input.root());
        tx.executeWithoutResult(status -> writePlan(date, input, matrix));
        return day(date);
    }

    @Transactional
    public void assign(Long bookId, Long driverId) {
        DeliveryBook book = book(bookId);
        if (book.getSenderConfirmedAt() != null || CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Việc này không còn giao được cho bác tài.");
        }
        if (driverId != null && !driver(driverId).isActive()) {
            throw new BusinessException("Tài xế đang nghỉ.");
        }
        DeliveryTrip trip = book.getTripId() == null ? null : em.find(DeliveryTrip.class, book.getTripId());
        if (trip == null) {
            throw new BusinessException("Việc chưa gắn chuyến.");
        }
        DriverDayPlan owner = planOfTrip(trip.getTripId());
        if (owner != null && !DRAFT.equals(owner.getStatus())) {
            throw new BusinessException("Lịch đã chốt. Hãy xác nhận hoặc không nhận việc đột xuất.");
        }
        for (DriverDayPlan plan : plans(book.getServiceDate())) {
            if (DRAFT.equals(plan.getStatus())) {
                deletePlan(plan);
            }
        }
        trip.setAssignedDriverId(driverId);
        trip.setStatus(OPEN);
        if (PLANNED.equals(book.getStatus())) {
            book.setStatus(WAITING);
        }
        book.setSudden(false);
        book.setSuddenConfirmed(false);
    }

    @Transactional
    public void confirmSudden(Long bookId) {
        DeliveryBook book = book(bookId);
        if (!book.isSudden() || CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Việc này không đang chờ xác nhận.");
        }
        book.setSuddenConfirmed(true);
    }

    public void deferSudden(Long bookId) {
        CancelPreview preview = tx.execute(status -> previewDefer(bookId));
        MatrixDistance matrix = preview.recompute()
                ? matrixOf(preview.stops(), preview.apiKey(), preview.root())
                : null;
        int meters = preview.recompute()
                ? RoutePlanner.pathMeters(preview.originId(), preview.next(), matrix, preview.returnTo())
                : 0;
        tx.executeWithoutResult(status -> commitDefer(bookId, preview, meters));
    }

    public Map<String, Object> publish(LocalDate date) {
        tx.executeWithoutResult(status -> {
            List<DriverDayPlan> list = requirePlans(date, DRAFT);
            for (DriverDayPlan plan : list) {
                plan.setStatus(PUBLISHED);
                notice(plan.getDriverId(), date, slipText(plan));
            }
        });
        return day(date);
    }

    public Map<String, Object> lock(LocalDate date, Long startStopId) {
        List<Long> pending = tx.execute(status -> pendingSudden(date));
        for (Long bookId : pending) {
            deferSudden(bookId);
        }
        LockRead read = tx.execute(status -> readLock(date, startStopId));
        List<LockResult> results = new ArrayList<>();
        for (LockJob job : read.jobs()) {
            if (job.own().isEmpty()) {
                results.add(new LockResult(job.planId(), null, null, 0));
                continue;
            }
            MatrixDistance matrix = matrixOf(job.stops(), read.apiKey(), read.root());
            Long returnTo = job.returnToShop() ? read.shopId() : null;
            Route route = routePlanner.plan(job.own(), read.startId(), matrix, 1, returnTo).get(0);
            if (!route.visits().isEmpty() && route.visits().get(0).pickups().isEmpty()) {
                throw new BusinessException("Điểm đầu phải là điểm lấy vì xe đang trống.");
            }
            results.add(new LockResult(job.planId(), route.visits(), explain(route.reasons(), matrix.straight()), route.meters()));
        }
        tx.executeWithoutResult(status -> applyLock(read.startId(), results));
        return day(date);
    }

    @Transactional
    public void depart(Long planId) {
        DriverDayPlan plan = plan(planId);
        if (!LOCKED.equals(plan.getStatus())) {
            throw new BusinessException("Chỉ xuất phát lịch đã khóa.");
        }
        plan.setStatus(EN_ROUTE);
    }

    public void finish(Long planId) {
        List<Long> moved = tx.execute(status -> closeRoute(planId));
        for (Long bookId : moved) {
            placeOnRoute(bookId, false, false);
        }
    }

    public void dropReturn(Long planId) {
        DropRead read = tx.execute(status -> readDrop(planId));
        MatrixDistance matrix = matrixOf(read.stops(), read.apiKey(), read.root());
        int meters = RoutePlanner.pathMeters(read.originId(), read.visits(), matrix, null);
        tx.executeWithoutResult(status -> {
            DriverDayPlan plan = plan(planId);
            if (EN_ROUTE.equals(plan.getStatus()) || DONE.equals(plan.getStatus())) {
                throw new BusinessException("Lịch đã chạy thì không bỏ đoạn về.");
            }
            plan.setReturnToShop(false);
            plan.setTotalMeters(meters);
        });
    }

    @Transactional
    public void ping(Long driverId, double latitude, double longitude, Long userId, boolean coordinator) {
        DeliveryDriver person = driver(driverId);
        if (!coordinator && (person.getUserId() == null || !person.getUserId().equals(userId))) {
            throw new AccessDeniedException("Chỉ tài xế của xe này mới gửi được vị trí.");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new BusinessException("Tọa độ không hợp lệ.");
        }
        DriverLocation location = em.find(DriverLocation.class, driverId);
        if (location == null) {
            location = new DriverLocation();
            location.setDriverId(driverId);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setRecordedAt(LocalDateTime.now());
            em.persist(location);
        } else {
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setRecordedAt(LocalDateTime.now());
        }
        syncEnRoute(driverId, latitude, longitude);
    }

    @Transactional
    public void ack(Long noticeId) {
        DispatchNotice notice = em.find(DispatchNotice.class, noticeId);
        if (notice == null) {
            throw new ResourceNotFoundException("Không thấy phiếu cần báo");
        }
        notice.setAcknowledgedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<DispatchNotice> notices() {
        return em.createQuery("select n from DispatchNotice n where n.acknowledgedAt is null order by n.createdAt desc", DispatchNotice.class)
                .getResultList();
    }

    public Map<String, Object> day(LocalDate date) {
        DayLoad load = tx.execute(status -> loadDay(date));
        attachRoutes(load.body(), load.apiKey());
        return load.body();
    }

    public List<Map<String, Object>> geocode(String query, String focus) {
        String key = tx.execute(status -> vietmapKey());
        return roadDistance.geocode(query, focus, key);
    }

    public Map<String, Object> place(String refId) {
        String key = tx.execute(status -> vietmapKey());
        return roadDistance.place(refId, key);
    }

    public Map<String, Object> reverse(double latitude, double longitude) {
        String key = tx.execute(status -> vietmapKey());
        return roadDistance.reverse(latitude, longitude, key);
    }

    public String mapStyle() {
        String key = tx.execute(status -> tileKey());
        return roadDistance.style(key);
    }

    public RoadDistance.AssetFile mapAsset(String pathAndQuery) {
        String key = tx.execute(status -> tileKey());
        return roadDistance.asset(pathAndQuery, key);
    }

    private NewBook persistBook(BookRequest request, Long userId) {
        if (request.pickupStopId().equals(request.dropoffStopId())) {
            throw new BusinessException("Điểm lấy và điểm giao phải khác nhau.");
        }
        stop(request.pickupStopId());
        stop(request.dropoffStopId());
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessException("Book cần ít nhất một dòng hàng.");
        }
        for (LineRequest line : request.lines()) {
            if (line.description() == null || line.description().isBlank()) {
                throw new BusinessException("Dòng hàng cần mô tả.");
            }
            if (line.quantity() == null || line.quantity().signum() <= 0) {
                throw new BusinessException("Số lượng phải lớn hơn 0.");
            }
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
            trip = newTrip(request.serviceDate(), request.pickupStopId(), request.dropoffStopId());
        }
        book.setTripId(trip.getTripId());
        boolean live = merged && ASSIGNED.equals(trip.getStatus());
        if (live) {
            book.setStatus(PLANNED);
        }
        em.persist(book);
        em.flush();
        for (LineRequest line : request.lines()) {
            DeliveryBookLine entity = new DeliveryBookLine();
            entity.setBookId(book.getBookId());
            entity.setDescription(line.description().trim());
            entity.setQuantity(line.quantity());
            entity.setUnitLabel(line.unitLabel());
            em.persist(entity);
        }
        if (live) {
            noticeForTrip(trip, "Ghép thêm hàng vào chuyến đã có, km không tăng.");
        }
        return new NewBook(book.getBookId(), !merged);
    }

    private DeliveryTrip newTrip(LocalDate date, Long pickup, Long dropoff) {
        DeliveryTrip trip = new DeliveryTrip();
        trip.setServiceDate(date);
        trip.setPickupStopId(pickup);
        trip.setDropoffStopId(dropoff);
        trip.setStatus(OPEN);
        em.persist(trip);
        em.flush();
        return trip;
    }

    private CancelPreview previewCancel(Long bookId) {
        DeliveryBook book = book(bookId);
        if (book.getSenderConfirmedAt() != null || CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Book đã xác nhận đưa hàng thì không hủy.");
        }
        DriverDayPlan plan = planOfTrip(book.getTripId());
        if (plan == null) {
            return CancelPreview.simple(book.getTripId());
        }
        boolean others = booksOfTrip(book.getTripId()).stream()
                .anyMatch(other -> !other.getBookId().equals(book.getBookId()) && !CANCELLED.equals(other.getStatus()));
        List<Visit> visits = toVisits(plan.getPlanId());
        String seen = sequence(visits);
        if (others || alreadyPassed(plan, visits, book.getTripId())) {
            return CancelPreview.keep(book.getTripId(), plan.getPlanId(), seen, !DRAFT.equals(plan.getStatus()));
        }
        List<Visit> next = withoutTrip(visits, book.getTripId());
        boolean deleteDraft = next.isEmpty() && DRAFT.equals(plan.getStatus());
        long origin = originId(plan);
        Long returnTo = plan.isReturnToShop() ? shop().getStopId() : null;
        List<DeliveryStop> stops = deleteDraft ? List.of() : snaps(ids(origin, next, returnTo));
        return new CancelPreview(book.getTripId(), plan.getPlanId(), seen, next, true, deleteDraft,
                !DRAFT.equals(plan.getStatus()), origin, returnTo, stops, vietmapKey(), matrixRoot());
    }

    private void commitCancel(Long bookId, CancelPreview preview, int meters) {
        DeliveryBook book = book(bookId);
        if (book.getSenderConfirmedAt() != null || CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Book đã xác nhận đưa hàng thì không hủy.");
        }
        book.setStatus(CANCELLED);
        if (preview.planId() != null) {
            DriverDayPlan plan = plan(preview.planId());
            List<Visit> visits = toVisits(plan.getPlanId());
            if (!preview.seen().equals(sequence(visits))) {
                throw new BusinessException("Lịch vừa đổi, hủy lại.");
            }
            if (preview.strip()) {
                if (preview.deleteDraft()) {
                    deletePlan(plan);
                } else {
                    replaceVisits(plan.getPlanId(), preview.next());
                    plan.setTotalMeters(meters);
                }
            }
            if (preview.tell() && !preview.deleteDraft()) {
                notice(plan.getDriverId(), plan.getServiceDate(), "Hủy book " + bookId + ". Gỡ hàng khỏi phiếu nếu bác chưa lấy. " + slipText(plan));
            } else if (preview.tell() && preview.deleteDraft()) {
                notice(plan.getDriverId(), plan.getServiceDate(), "Hủy book " + bookId + ". Lịch nháp không còn điểm dừng.");
            }
        }
        closeTripIfEmpty(book.getTripId());
    }

    private List<Long> pendingSudden(LocalDate date) {
        List<Long> ids = new ArrayList<>();
        for (DeliveryBook book : books(date)) {
            if (!book.isSudden() || book.isSuddenConfirmed() || book.getSenderConfirmedAt() != null) {
                continue;
            }
            if (CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
                continue;
            }
            DriverDayPlan owner = planOfTrip(book.getTripId());
            if (owner != null && (EN_ROUTE.equals(owner.getStatus()) || DONE.equals(owner.getStatus()))) {
                continue;
            }
            ids.add(book.getBookId());
        }
        return ids;
    }

    private CancelPreview previewDefer(Long bookId) {
        DeliveryBook book = book(bookId);
        if (!book.isSudden() || book.isSuddenConfirmed() || book.getSenderConfirmedAt() != null
                || CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Việc này không còn chờ xác nhận.");
        }
        DriverDayPlan plan = planOfTrip(book.getTripId());
        if (plan == null) {
            return CancelPreview.simple(book.getTripId());
        }
        if (EN_ROUTE.equals(plan.getStatus()) || DONE.equals(plan.getStatus())) {
            throw new BusinessException("Bác tài đã xuất phát, không chuyển việc này sang ngày mai.");
        }
        boolean others = booksOfTrip(book.getTripId()).stream()
                .anyMatch(other -> !other.getBookId().equals(book.getBookId()) && !CANCELLED.equals(other.getStatus()));
        List<Visit> visits = toVisits(plan.getPlanId());
        if (others) {
            return CancelPreview.keep(book.getTripId(), plan.getPlanId(), sequence(visits), true);
        }
        List<Visit> next = withoutTrip(visits, book.getTripId());
        boolean deleteDraft = next.isEmpty() && DRAFT.equals(plan.getStatus());
        long origin = originId(plan);
        Long returnTo = plan.isReturnToShop() ? shop().getStopId() : null;
        List<DeliveryStop> stops = deleteDraft ? List.of() : snaps(ids(origin, next, returnTo));
        return new CancelPreview(book.getTripId(), plan.getPlanId(), sequence(visits), next, true, deleteDraft,
                true, origin, returnTo, stops, vietmapKey(), matrixRoot());
    }

    private void commitDefer(Long bookId, CancelPreview preview, int meters) {
        DeliveryBook book = book(bookId);
        if (!book.isSudden() || book.isSuddenConfirmed() || book.getSenderConfirmedAt() != null
                || CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Việc này không còn chờ xác nhận.");
        }
        if (preview.planId() != null && preview.strip()) {
            DriverDayPlan plan = plan(preview.planId());
            List<Visit> visits = toVisits(plan.getPlanId());
            if (!preview.seen().equals(sequence(visits))) {
                throw new BusinessException("Lịch vừa đổi, thử lại.");
            }
            if (preview.deleteDraft()) {
                deletePlan(plan);
            } else {
                replaceVisits(plan.getPlanId(), preview.next());
                plan.setTotalMeters(meters);
            }
            notice(plan.getDriverId(), plan.getServiceDate(),
                    "Việc " + bookId + " chưa xác nhận, chuyển sang ngày mai.");
        }
        LocalDate next = book.getServiceDate().plusDays(1);
        DeliveryTrip trip = book.getTripId() == null ? null : em.find(DeliveryTrip.class, book.getTripId());
        if (preview.planId() != null && !preview.strip()) {
            DeliveryTrip fresh = newTrip(next, book.getPickupStopId(), book.getDropoffStopId());
            book.setTripId(fresh.getTripId());
        } else if (trip != null) {
            trip.setServiceDate(next);
            trip.setStatus(OPEN);
            trip.setAssignedDriverId(null);
        }
        book.setServiceDate(next);
        book.setStatus(WAITING);
        book.setSudden(false);
        book.setSuddenConfirmed(false);
    }

    private AcceptPrep prepAccept(Long bookId) {
        DeliveryBook book = book(bookId);
        if (CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus())) {
            throw new BusinessException("Book không còn chờ gắn.");
        }
        DeliveryTrip trip = book.getTripId() == null ? null : em.find(DeliveryTrip.class, book.getTripId());
        if (trip == null) {
            throw new BusinessException("Book chưa gắn chuyến.");
        }
        DriverDayPlan owner = planOfTrip(trip.getTripId());
        if (ASSIGNED.equals(trip.getStatus()) && owner != null && !DONE.equals(owner.getStatus())) {
            return new AcceptPrep(bookId, book.getServiceDate(), "Chuyến đã nằm trong lịch.");
        }
        if (ASSIGNED.equals(trip.getStatus()) || CLOSED.equals(trip.getStatus())) {
            DeliveryTrip fresh = newTrip(book.getServiceDate(), book.getPickupStopId(), book.getDropoffStopId());
            book.setTripId(fresh.getTripId());
            book.setStatus(WAITING);
            closeTripIfEmpty(trip.getTripId());
        }
        return new AcceptPrep(bookId, book.getServiceDate(), null);
    }

    private String acceptMessage(Long bookId, LocalDate original) {
        DeliveryBook book = book(bookId);
        if (PLANNED.equals(book.getStatus()) || book.isSudden()) {
            return "Đã gắn tiện chuyến.";
        }
        if (!original.equals(book.getServiceDate())) {
            return "Không còn tiện chuyến. Book chuyển sang " + book.getServiceDate() + ".";
        }
        throw new BusinessException("Không gắn được. Kiểm tra lịch trong ngày và nguồn km.");
    }

    private void placeOnRoute(long bookId, boolean allowEnRoute, boolean shiftWhenBlocked) {
        SuddenView view = tx.execute(status -> suddenView(bookId, allowEnRoute));
        if (view.kind() == SuddenKind.SKIP) {
            return;
        }
        if (view.kind() == SuddenKind.IDLE) {
            tx.executeWithoutResult(status -> noticeIfSealed(bookId));
            return;
        }
        if (view.kind() == SuddenKind.NOTICE) {
            tx.executeWithoutResult(status -> notice(view.noticeDriverId(), view.serviceDate(), view.notice()));
            return;
        }
        MatrixDistance matrix = matrixOf(view.stops(), view.apiKey(), view.root());
        InsertDecision decision = decide(view, matrix, shiftWhenBlocked);
        tx.executeWithoutResult(status -> applySudden(bookId, decision));
    }

    private SuddenView suddenView(long bookId, boolean allowEnRoute) {
        DeliveryBook book = book(bookId);
        if (CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus()) || book.getTripId() == null) {
            return SuddenView.skip();
        }
        DeliveryTrip trip = em.find(DeliveryTrip.class, book.getTripId());
        if (trip == null) {
            return SuddenView.skip();
        }
        List<DriverDayPlan> dayPlans = plans(book.getServiceDate()).stream()
                .filter(plan -> !DONE.equals(plan.getStatus()))
                .toList();
        if (dayPlans.isEmpty()) {
            return SuddenView.idle();
        }
        boolean enRoute = dayPlans.stream().anyMatch(plan -> EN_ROUTE.equals(plan.getStatus()));
        if (enRoute && !allowEnRoute) {
            return SuddenView.notice(dayPlans.get(0).getDriverId(), book.getServiceDate(), callNotice(book, dayPlans));
        }
        DeliveryStop shop = shop();
        List<Candidate> candidates = new ArrayList<>();
        LinkedHashSet<Long> stopIds = new LinkedHashSet<>();
        for (DriverDayPlan plan : dayPlans) {
            List<Visit> visits = toVisits(plan.getPlanId());
            int from = firstOpenIndex(plan, visits);
            long origin = originId(plan);
            Long returnTo = plan.isReturnToShop() ? shop.getStopId() : null;
            candidates.add(new Candidate(plan.getPlanId(), plan.getDriverId(), plan.getStatus(), origin, returnTo, from, visits, sequence(visits)));
            stopIds.add(origin);
            if (returnTo != null) {
                stopIds.add(returnTo);
            }
            for (Visit visit : visits) {
                stopIds.add(visit.stopId());
            }
        }
        stopIds.add(trip.getPickupStopId());
        stopIds.add(trip.getDropoffStopId());
        return SuddenView.ready(book.getBookId(), book.getServiceDate(), trip.getTripId(), trip.getPickupStopId(), trip.getDropoffStopId(),
                candidates, snaps(stopIds), settings().getDetourLimitMeters(), vietmapKey(), matrixRoot());
    }

    private InsertDecision decide(SuddenView view, MatrixDistance matrix, boolean shiftWhenBlocked) {
        int bestExtra = Integer.MAX_VALUE;
        Candidate best = null;
        Insertion bestInsert = null;
        for (Candidate candidate : view.candidates()) {
            Insertion insertion = routePlanner.insertIntoRemaining(
                    candidate.visits(), candidate.from(),
                    new Trip(view.tripId(), view.pickup(), view.dropoff()),
                    candidate.origin(), matrix, candidate.returnTo());
            if (insertion.extraMeters() < bestExtra) {
                bestExtra = insertion.extraMeters();
                best = candidate;
                bestInsert = insertion;
            }
        }
        if (best == null || bestExtra > view.limit()) {
            if (!shiftWhenBlocked) {
                Long driverId = best != null ? best.driverId() : view.candidates().get(0).driverId();
                String text = "Book " + view.bookId() + " vượt ngưỡng vòng, để chờ trên ngày " + view.serviceDate() + ". Điều phối cần xếp hoặc gọi.";
                return InsertDecision.notice(driverId, view.serviceDate(), text);
            }
            return InsertDecision.shift();
        }
        int total = RoutePlanner.pathMeters(best.origin(), bestInsert.visits(), matrix, best.returnTo());
        DeliveryDriver person = driver(best.driverId());
        String lead = EN_ROUTE.equals(best.status())
                ? "Tiện chuyến khi đang đi, thêm " + bestExtra + " m. Gọi " + person.getDriverName() + " " + person.getPhone() + " rồi in lại phiếu. "
                : "Tiện chuyến book " + view.bookId() + ", thêm " + bestExtra + " m. Nói với bác trước khi đi. ";
        if (matrix.straight()) {
            lead = STRAIGHT + " " + lead;
        }
        return InsertDecision.attach(best.planId(), best.seen(), bestInsert.visits(), total, best.driverId(), view.serviceDate(), lead);
    }

    private void applySudden(long bookId, InsertDecision decision) {
        if (decision.kind() == SuddenKind.NOTICE) {
            notice(decision.driverId(), decision.serviceDate(), decision.notice());
            return;
        }
        DeliveryBook book = book(bookId);
        if (CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus()) || book.getTripId() == null) {
            return;
        }
        DeliveryTrip trip = em.find(DeliveryTrip.class, book.getTripId());
        if (trip == null) {
            return;
        }
        if (decision.kind() == SuddenKind.SHIFT) {
            book.setServiceDate(book.getServiceDate().plusDays(1));
            trip.setServiceDate(book.getServiceDate());
            return;
        }
        DriverDayPlan plan = plan(decision.planId());
        if (DONE.equals(plan.getStatus())) {
            return;
        }
        if (!decision.seen().equals(sequence(toVisits(plan.getPlanId())))) {
            throw new BusinessException("Lịch vừa đổi, thử lại.");
        }
        replaceVisits(plan.getPlanId(), decision.visits());
        plan.setTotalMeters(decision.totalMeters());
        trip.setStatus(ASSIGNED);
        trip.setAssignedDriverId(plan.getDriverId());
        if (WAITING.equals(book.getStatus())) {
            book.setStatus(PLANNED);
        }
        book.setSudden(true);
        book.setSuddenConfirmed(false);
        notice(plan.getDriverId(), plan.getServiceDate(), decision.notice() + slipText(plan));
    }

    private void noticeIfSealed(long bookId) {
        DeliveryBook book = book(bookId);
        List<DriverDayPlan> all = plans(book.getServiceDate());
        boolean sealed = !all.isEmpty() && all.stream().anyMatch(plan -> !DRAFT.equals(plan.getStatus()));
        if (!sealed || !WAITING.equals(book.getStatus())) {
            return;
        }
        notice(all.get(0).getDriverId(), book.getServiceDate(),
                "Book " + bookId + " chờ trên ngày đã chốt " + book.getServiceDate() + ". Cần xếp lại hoặc gọi.");
    }

    private String callNotice(DeliveryBook book, List<DriverDayPlan> dayPlans) {
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
        return text.toString();
    }

    private PlanInput readPlan(LocalDate date) {
        List<DriverDayPlan> existing = plans(date);
        if (existing.stream().anyMatch(plan -> !DRAFT.equals(plan.getStatus()))) {
            throw new BusinessException("Ngày này đã chốt lịch. Không xếp lại.");
        }
        if (drivers().stream().noneMatch(DeliveryDriver::isActive)) {
            throw new BusinessException("Chưa có tài xế.");
        }
        DeliveryStop shop = shop();
        List<DeliveryTrip> active = trips(date).stream().filter(this::hasActiveBook).toList();
        if (active.isEmpty()) {
            return PlanInput.empty();
        }
        List<DeliveryTrip> assigned = active.stream().filter(trip -> trip.getAssignedDriverId() != null).toList();
        if (assigned.isEmpty()) {
            throw new BusinessException("Hãy giao việc cho bác tài trước khi xếp lộ trình.");
        }
        Map<Long, List<Trip>> grouped = new LinkedHashMap<>();
        for (DeliveryTrip trip : assigned) {
            grouped.computeIfAbsent(trip.getAssignedDriverId(), id -> new ArrayList<>())
                    .add(new Trip(trip.getTripId(), trip.getPickupStopId(), trip.getDropoffStopId()));
        }
        List<AssignedBatch> batches = new ArrayList<>();
        for (Map.Entry<Long, List<Trip>> entry : grouped.entrySet()) {
            batches.add(new AssignedBatch(entry.getKey(), List.copyOf(entry.getValue())));
        }
        return new PlanInput(batches, snaps(stopsFor(assigned, shop)), vietmapKey(), matrixRoot());
    }

    private void writePlan(LocalDate date, PlanInput input, MatrixDistance matrix) {
        List<DriverDayPlan> existing = plans(date);
        if (existing.stream().anyMatch(plan -> !DRAFT.equals(plan.getStatus()))) {
            throw new BusinessException("Ngày này đã chốt lịch. Không xếp lại.");
        }
        for (DriverDayPlan plan : existing) {
            deletePlan(plan);
        }
        em.flush();
        if (input.batches().isEmpty()) {
            return;
        }
        List<Long> live = trips(date).stream()
                .filter(this::hasActiveBook)
                .filter(trip -> trip.getAssignedDriverId() != null)
                .map(DeliveryTrip::getTripId)
                .sorted()
                .toList();
        List<Long> expected = new ArrayList<>();
        for (AssignedBatch batch : input.batches()) {
            for (Trip trip : batch.trips()) {
                expected.add(trip.id());
            }
        }
        expected.sort(Long::compareTo);
        if (!live.equals(expected)) {
            throw new BusinessException("Chuyến trong ngày vừa đổi, xếp lại.");
        }
        DeliveryStop shop = shop();
        for (AssignedBatch batch : input.batches()) {
            Route route = routePlanner.plan(batch.trips(), shop.getStopId(), matrix, 1, true).get(0);
            if (route.visits().isEmpty()) {
                continue;
            }
            DriverDayPlan plan = new DriverDayPlan();
            plan.setDriverId(batch.driverId());
            plan.setServiceDate(date);
            plan.setStatus(DRAFT);
            plan.setReturnToShop(true);
            plan.setStartStopId(shop.getStopId());
            plan.setTotalMeters(route.meters());
            plan.setExplanation(explain(route.reasons(), matrix.straight()));
            em.persist(plan);
            try {
                em.flush();
            } catch (DataIntegrityViolationException ex) {
                throw new BusinessException("Tài xế đã có lịch trong ngày này.");
            }
            saveVisits(plan.getPlanId(), route.visits());
            for (Visit visit : route.visits()) {
                for (Long tripId : visit.pickups()) {
                    markAssigned(tripId);
                }
            }
        }
    }

    private LockRead readLock(LocalDate date, Long startStopId) {
        List<DriverDayPlan> list = requirePlans(date, PUBLISHED);
        DeliveryStop shop = shop();
        DeliveryStop start = startStopId == null ? shop : stop(startStopId);
        List<LockJob> jobs = new ArrayList<>();
        for (DriverDayPlan plan : list) {
            List<Visit> current = toVisits(plan.getPlanId());
            List<Trip> own = RoutePlanner.tripsOf(current);
            LinkedHashSet<Long> ids = new LinkedHashSet<>();
            ids.add(start.getStopId());
            if (plan.isReturnToShop()) {
                ids.add(shop.getStopId());
            }
            for (Visit visit : current) {
                ids.add(visit.stopId());
            }
            for (Trip trip : own) {
                ids.add(trip.pickup());
                ids.add(trip.dropoff());
            }
            jobs.add(new LockJob(plan.getPlanId(), own, plan.isReturnToShop(), snaps(ids)));
        }
        return new LockRead(start.getStopId(), shop.getStopId(), jobs, vietmapKey(), matrixRoot());
    }

    private void applyLock(long startId, List<LockResult> results) {
        for (LockResult result : results) {
            DriverDayPlan plan = plan(result.planId());
            if (!PUBLISHED.equals(plan.getStatus())) {
                throw new BusinessException("Lịch vừa đổi, khóa lại.");
            }
            plan.setStartStopId(startId);
            if (result.visits() == null) {
                plan.setStatus(LOCKED);
                continue;
            }
            String before = sequence(toVisits(plan.getPlanId()));
            replaceVisits(plan.getPlanId(), result.visits());
            plan.setTotalMeters(result.meters());
            plan.setExplanation(result.explanation());
            plan.setStatus(LOCKED);
            String after = sequence(result.visits());
            String lead = before.equals(after)
                    ? "Khóa thứ tự, không đổi. "
                    : "Thứ tự buổi sáng đổi. In lại phiếu hoặc nói lại. ";
            notice(plan.getDriverId(), plan.getServiceDate(), lead + slipText(plan));
        }
    }

    private List<Long> closeRoute(Long planId) {
        DriverDayPlan plan = plan(planId);
        if (!EN_ROUTE.equals(plan.getStatus())) {
            throw new BusinessException("Chỉ kết thúc lịch đang đi.");
        }
        plan.setStatus(DONE);
        List<Long> moved = new ArrayList<>();
        for (DeliveryBook book : books(plan.getServiceDate())) {
            if (CANCELLED.equals(book.getStatus()) || DELIVERED.equals(book.getStatus()) || book.getSenderConfirmedAt() != null) {
                continue;
            }
            DriverDayPlan owner = planOfTrip(book.getTripId());
            if (owner == null || !owner.getPlanId().equals(plan.getPlanId())) {
                continue;
            }
            Long previous = book.getTripId();
            DeliveryTrip fresh = newTrip(plan.getServiceDate().plusDays(1), book.getPickupStopId(), book.getDropoffStopId());
            book.setTripId(fresh.getTripId());
            book.setServiceDate(fresh.getServiceDate());
            book.setStatus(WAITING);
            closeTripIfEmpty(previous);
            moved.add(book.getBookId());
        }
        return moved;
    }

    private DropRead readDrop(Long planId) {
        DriverDayPlan plan = plan(planId);
        if (EN_ROUTE.equals(plan.getStatus()) || DONE.equals(plan.getStatus())) {
            throw new BusinessException("Lịch đã chạy thì không bỏ đoạn về.");
        }
        List<Visit> visits = toVisits(plan.getPlanId());
        long origin = originId(plan);
        return new DropRead(origin, visits, snaps(ids(origin, visits, null)), vietmapKey(), matrixRoot());
    }

    private DayLoad loadDay(LocalDate date) {
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
        return new DayLoad(body, vietmapKey());
    }

    private Map<String, Object> bookView(DeliveryBook book) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("book", book);
        view.put("lines", lines(book.getBookId()));
        view.put("pickup", stop(book.getPickupStopId()));
        view.put("dropoff", stop(book.getDropoffStopId()));
        DeliveryTrip trip = book.getTripId() == null ? null : em.find(DeliveryTrip.class, book.getTripId());
        Long assigned = trip == null ? null : trip.getAssignedDriverId();
        if (assigned == null) {
            DriverDayPlan owner = planOfTrip(book.getTripId());
            if (owner != null) {
                assigned = owner.getDriverId();
            }
        }
        view.put("assignedDriverId", assigned);
        return view;
    }

    private Map<String, Object> planView(DriverDayPlan plan) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("plan", plan);
        view.put("driver", driver(plan.getDriverId()));
        if (plan.getStartStopId() != null) {
            view.put("start", stop(plan.getStartStopId()));
        }
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

    private void attachRoutes(Map<String, Object> body, String apiKey) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> plans = (List<Map<String, Object>>) body.get("plans");
        DeliveryStop shop = (DeliveryStop) body.get("shop");
        for (Map<String, Object> view : plans) {
            DriverDayPlan plan = (DriverDayPlan) view.get("plan");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> visits = (List<Map<String, Object>>) view.get("visits");
            view.put("route", routeGeometry(plan, visits, shop, (DeliveryStop) view.get("start"), apiKey));
        }
    }

    private List<List<Double>> routeGeometry(DriverDayPlan plan, List<Map<String, Object>> visitRows, DeliveryStop shop, DeliveryStop start, String apiKey) {
        List<DeliveryStop> line = new ArrayList<>();
        if (start != null && start.getLatitude() != null && start.getLongitude() != null) {
            line.add(start);
        }
        for (Map<String, Object> row : visitRows) {
            DeliveryStop stop = (DeliveryStop) row.get("stop");
            if (stop.getLatitude() == null || stop.getLongitude() == null) {
                continue;
            }
            if (!line.isEmpty() && stop.getStopId().equals(line.get(line.size() - 1).getStopId())) {
                continue;
            }
            line.add(stop);
        }
        if (plan.isReturnToShop() && shop != null && shop.getLatitude() != null && shop.getLongitude() != null
                && (line.isEmpty() || !shop.getStopId().equals(line.get(line.size() - 1).getStopId()))) {
            line.add(shop);
        }
        if (line.size() < 2) {
            return List.of();
        }
        String key = plan.getPlanId() + ":" + line.stream().map(stop -> String.valueOf(stop.getStopId())).reduce((a, b) -> a + "," + b).orElse("");
        List<List<Double>> cached = routeCache.get(key);
        if (cached != null) {
            return cached;
        }
        List<List<Double>> fresh = roadDistance.line(line, apiKey);
        if (!fresh.isEmpty()) {
            routeCache.put(key, fresh);
        }
        return fresh;
    }

    private String vietmapKey() {
        return vietmapKey(settings());
    }

    private String vietmapKey(DeliverySetting setting) {
        if (setting.getVietmapApiKey() != null && !setting.getVietmapApiKey().isBlank()) {
            return setting.getVietmapApiKey().trim();
        }
        return vietmapEnvKey == null ? "" : vietmapEnvKey.trim();
    }

    private String tileKey() {
        DeliverySetting setting = settings();
        if (setting.getVietmapTileKey() != null && !setting.getVietmapTileKey().isBlank()) {
            return setting.getVietmapTileKey().trim();
        }
        return vietmapKey(setting);
    }

    private String matrixRoot() {
        return "https://rsapi.goong.io";
    }

    private void markVietmap(DeliverySetting setting) {
        setting.setVietmapReady(!vietmapKey(setting).isBlank());
        setting.setVietmapTileReady(setting.getVietmapTileKey() != null && !setting.getVietmapTileKey().isBlank());
    }

    private MatrixDistance matrixOf(List<DeliveryStop> stops, String apiKey, String root) {
        for (DeliveryStop stop : stops) {
            if (stop.getLatitude() == null || stop.getLongitude() == null) {
                throw new BusinessException("Điểm " + stop.getStopName() + " chưa có tọa độ.");
            }
        }
        try {
            return MatrixDistance.of(stops, roadDistance.meters(stops, apiKey, root), false);
        } catch (RuntimeException ex) {
            if (ex instanceof BusinessException business && business.getMessage() != null && business.getMessage().contains("chưa có tọa độ")) {
                throw business;
            }
            return MatrixDistance.of(stops, straight(stops), true);
        }
    }

    private int[][] straight(List<DeliveryStop> stops) {
        int n = stops.size();
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                matrix[i][j] = Geo.meters(stops.get(i).getLatitude(), stops.get(i).getLongitude(),
                        stops.get(j).getLatitude(), stops.get(j).getLongitude());
            }
        }
        return matrix;
    }

    private String explain(List<String> reasons, boolean straight) {
        String text = String.join("\n", reasons);
        if (!straight) {
            return text;
        }
        return text.isBlank() ? STRAIGHT : STRAIGHT + "\n" + text;
    }

    private boolean alreadyPassed(DriverDayPlan plan, List<Visit> visits, Long tripId) {
        if (!EN_ROUTE.equals(plan.getStatus()) || tripId == null) {
            return false;
        }
        int open = firstOpenIndex(plan, visits);
        for (int i = 0; i < open; i++) {
            Visit visit = visits.get(i);
            if (visit.pickups().contains(tripId) || visit.dropoffs().contains(tripId)) {
                return true;
            }
        }
        return false;
    }

    private int firstOpenIndex(DriverDayPlan plan, List<Visit> visits) {
        if (!EN_ROUTE.equals(plan.getStatus())) {
            return 0;
        }
        DriverLocation location = em.find(DriverLocation.class, plan.getDriverId());
        if (location == null) {
            return Math.min(plan.getPassedSeq(), visits.size());
        }
        return advance(plan, visits, location.getLatitude(), location.getLongitude());
    }

    private void syncEnRoute(Long driverId, double latitude, double longitude) {
        List<DriverDayPlan> moving = em.createQuery(
                        "select p from DriverDayPlan p where p.driverId = :driverId and p.status = :status", DriverDayPlan.class)
                .setParameter("driverId", driverId)
                .setParameter("status", EN_ROUTE)
                .getResultList();
        for (DriverDayPlan plan : moving) {
            advance(plan, toVisits(plan.getPlanId()), latitude, longitude);
        }
    }

    private int advance(DriverDayPlan plan, List<Visit> visits, double latitude, double longitude) {
        int open = Math.min(Math.max(plan.getPassedSeq(), 0), visits.size());
        if (open >= visits.size()) {
            plan.setPassedSeq(open);
            return open;
        }
        DeliveryStop here = stop(visits.get(open).stopId());
        if (here.getLatitude() == null || here.getLongitude() == null) {
            return open;
        }
        double km = Geo.km(latitude, longitude, here.getLatitude(), here.getLongitude());
        if (km <= PASS_RADIUS_KM) {
            plan.setTouchedOpenStop(true);
            return open;
        }
        if (plan.isTouchedOpenStop()) {
            open += 1;
            plan.setPassedSeq(open);
            plan.setTouchedOpenStop(false);
            if (open < visits.size()) {
                return advance(plan, visits, latitude, longitude);
            }
        }
        return open;
    }

    private boolean passed(DeliveryTrip trip) {
        for (DriverDayPlan plan : plans(trip.getServiceDate())) {
            if (!EN_ROUTE.equals(plan.getStatus())) {
                continue;
            }
            List<Visit> visits = toVisits(plan.getPlanId());
            if (alreadyPassed(plan, visits, trip.getTripId())) {
                return true;
            }
        }
        return false;
    }

    private long originId(DriverDayPlan plan) {
        if (plan.getStartStopId() != null) {
            return plan.getStartStopId();
        }
        return shop().getStopId();
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

    private List<Visit> withoutTrip(List<Visit> visits, Long tripId) {
        List<Visit> next = new ArrayList<>();
        for (Visit visit : visits) {
            Visit copy = visit.copy();
            copy.pickups().remove(tripId);
            copy.dropoffs().remove(tripId);
            if (!copy.pickups().isEmpty() || !copy.dropoffs().isEmpty()) {
                next.add(copy);
            }
        }
        return next;
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

    private void closeTripIfEmpty(Long tripId) {
        if (tripId == null) {
            return;
        }
        boolean live = booksOfTrip(tripId).stream().anyMatch(book -> !CANCELLED.equals(book.getStatus()));
        if (live) {
            return;
        }
        DeliveryTrip trip = em.find(DeliveryTrip.class, tripId);
        if (trip != null) {
            trip.setStatus(CLOSED);
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
        return found.stream()
                .filter(trip -> !CLOSED.equals(trip.getStatus()))
                .filter(this::hasActiveBook)
                .filter(this::stillOnRoute)
                .min(Comparator.comparing(DeliveryTrip::getTripId))
                .orElse(null);
    }

    private boolean stillOnRoute(DeliveryTrip trip) {
        if (!ASSIGNED.equals(trip.getStatus())) {
            return true;
        }
        DriverDayPlan plan = planOfTrip(trip.getTripId());
        return plan != null && !DONE.equals(plan.getStatus());
    }

    private boolean hasActiveBook(DeliveryTrip trip) {
        return booksOfTrip(trip.getTripId()).stream().anyMatch(book -> !CANCELLED.equals(book.getStatus()));
    }

    private List<DeliveryStop> stopsFor(List<DeliveryTrip> trips, DeliveryStop shop) {
        Map<Long, DeliveryStop> map = new LinkedHashMap<>();
        map.put(shop.getStopId(), shop);
        for (DeliveryTrip trip : trips) {
            map.put(trip.getPickupStopId(), stop(trip.getPickupStopId()));
            map.put(trip.getDropoffStopId(), stop(trip.getDropoffStopId()));
        }
        return new ArrayList<>(map.values());
    }

    private List<DeliveryStop> snaps(Iterable<Long> ids) {
        List<DeliveryStop> stops = new ArrayList<>();
        for (Long id : ids) {
            stops.add(snap(stop(id)));
        }
        return stops;
    }

    private List<DeliveryStop> snaps(List<DeliveryStop> stops) {
        List<DeliveryStop> copies = new ArrayList<>();
        for (DeliveryStop stop : stops) {
            copies.add(snap(stop));
        }
        return copies;
    }

    private DeliveryStop snap(DeliveryStop stop) {
        DeliveryStop copy = new DeliveryStop();
        copy.setStopId(stop.getStopId());
        copy.setStopName(stop.getStopName());
        copy.setLatitude(stop.getLatitude());
        copy.setLongitude(stop.getLongitude());
        copy.setShop(stop.isShop());
        return copy;
    }

    private LinkedHashSet<Long> ids(long origin, List<Visit> visits, Long returnTo) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        ids.add(origin);
        for (Visit visit : visits) {
            ids.add(visit.stopId());
        }
        if (returnTo != null) {
            ids.add(returnTo);
        }
        return ids;
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

    private String sequence(List<Visit> visits) {
        StringBuilder text = new StringBuilder();
        for (Visit visit : visits) {
            text.append(visit.stopId()).append(':').append(visit.pickups()).append(visit.dropoffs()).append('|');
        }
        return text.toString();
    }

    public record LineRequest(String description, BigDecimal quantity, String unitLabel) {}

    public record BookRequest(Long pickupStopId, Long dropoffStopId, LocalDate serviceDate, String note, Long requestSetId, List<LineRequest> lines) {
        public BookRequest {
            Objects.requireNonNull(pickupStopId);
            Objects.requireNonNull(dropoffStopId);
            Objects.requireNonNull(serviceDate);
        }
    }

    private record NewBook(long bookId, boolean place) {}

    private record DayLoad(Map<String, Object> body, String apiKey) {}

    private record AssignedBatch(Long driverId, List<Trip> trips) {}

    private record PlanInput(List<AssignedBatch> batches, List<DeliveryStop> stops, String apiKey, String root) {
        static PlanInput empty() {
            return new PlanInput(List.of(), List.of(), "", "");
        }
    }

    private record LockJob(Long planId, List<Trip> own, boolean returnToShop, List<DeliveryStop> stops) {}

    private record LockRead(long startId, long shopId, List<LockJob> jobs, String apiKey, String root) {}

    private record LockResult(Long planId, List<Visit> visits, String explanation, int meters) {}

    private record DropRead(long originId, List<Visit> visits, List<DeliveryStop> stops, String apiKey, String root) {}

    private record AcceptPrep(long bookId, LocalDate originalDate, String message) {}

    private record CancelPreview(Long tripId, Long planId, String seen, List<Visit> next, boolean strip, boolean deleteDraft,
                                 boolean tell, long originId, Long returnTo, List<DeliveryStop> stops, String apiKey, String root) {
        boolean recompute() {
            return strip && !deleteDraft;
        }

        static CancelPreview simple(Long tripId) {
            return new CancelPreview(tripId, null, "", List.of(), false, false, false, 0, null, List.of(), "", "");
        }

        static CancelPreview keep(Long tripId, Long planId, String seen, boolean notify) {
            return new CancelPreview(tripId, planId, seen, List.of(), false, false, notify, 0, null, List.of(), "", "");
        }
    }

    private enum SuddenKind { SKIP, IDLE, NOTICE, READY, SHIFT, ATTACH }

    private record Candidate(Long planId, Long driverId, String status, long origin, Long returnTo, int from, List<Visit> visits, String seen) {}

    private record SuddenView(SuddenKind kind, long bookId, LocalDate serviceDate, long tripId, long pickup, long dropoff,
                              List<Candidate> candidates, List<DeliveryStop> stops, int limit, String apiKey, String root,
                              Long noticeDriverId, String notice) {
        static SuddenView skip() {
            return new SuddenView(SuddenKind.SKIP, 0, null, 0, 0, 0, List.of(), List.of(), 0, "", "", null, null);
        }

        static SuddenView idle() {
            return new SuddenView(SuddenKind.IDLE, 0, null, 0, 0, 0, List.of(), List.of(), 0, "", "", null, null);
        }

        static SuddenView notice(Long driverId, LocalDate date, String text) {
            return new SuddenView(SuddenKind.NOTICE, 0, date, 0, 0, 0, List.of(), List.of(), 0, "", "", driverId, text);
        }

        static SuddenView ready(long bookId, LocalDate date, long tripId, long pickup, long dropoff, List<Candidate> candidates,
                                List<DeliveryStop> stops, int limit, String apiKey, String root) {
            return new SuddenView(SuddenKind.READY, bookId, date, tripId, pickup, dropoff, candidates, stops, limit, apiKey, root, null, null);
        }
    }

    private record InsertDecision(SuddenKind kind, Long planId, String seen, List<Visit> visits, int totalMeters,
                                  Long driverId, LocalDate serviceDate, String notice) {
        static InsertDecision shift() {
            return new InsertDecision(SuddenKind.SHIFT, null, null, List.of(), 0, null, null, null);
        }

        static InsertDecision notice(Long driverId, LocalDate date, String text) {
            return new InsertDecision(SuddenKind.NOTICE, null, null, List.of(), 0, driverId, date, text);
        }

        static InsertDecision attach(Long planId, String seen, List<Visit> visits, int totalMeters, Long driverId, LocalDate date, String text) {
            return new InsertDecision(SuddenKind.ATTACH, planId, seen, visits, totalMeters, driverId, date, text);
        }
    }

    private record MatrixDistance(Map<Long, Integer> index, int[][] matrix, boolean straight) implements Distance {
        static MatrixDistance of(List<DeliveryStop> stops, int[][] matrix, boolean straight) {
            Map<Long, Integer> index = new LinkedHashMap<>();
            for (int i = 0; i < stops.size(); i++) {
                index.put(stops.get(i).getStopId(), i);
            }
            return new MatrixDistance(index, matrix, straight);
        }

        @Override
        public int meters(long from, long to) {
            Integer left = index.get(from);
            Integer right = index.get(to);
            if (left == null || right == null) {
                throw new BusinessException("Thiếu tọa độ của một điểm trên tuyến.");
            }
            return matrix[left][right];
        }
    }
}
