package manage.store.inventory.delivery;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.delivery.DeliveryService.BookRequest;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.security.CurrentUser;

@RestController
@RequestMapping("/api/delivery")
@PreAuthorize("isAuthenticated()")
public class DeliveryController {

    private static final String DISPATCH = "hasAnyRole('ADMIN','DISPATCHER')";

    private final DeliveryService deliveryService;
    private final CurrentUser currentUser;

    public DeliveryController(DeliveryService deliveryService, CurrentUser currentUser) {
        this.deliveryService = deliveryService;
        this.currentUser = currentUser;
    }

    @GetMapping("/stops")
    public List<DeliveryStop> stops() {
        return deliveryService.stops();
    }

    @GetMapping("/owners")
    public List<String> owners() {
        return deliveryService.ownerNames();
    }

    @GetMapping("/geocode")
    public List<Map<String, Object>> geocode(@RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String focus) {
        return deliveryService.geocode(q, focus);
    }

    @GetMapping("/place")
    public Map<String, Object> place(@RequestParam String refId) {
        return deliveryService.place(refId);
    }

    @GetMapping("/reverse")
    public Map<String, Object> reverse(@RequestParam double lat, @RequestParam double lng) {
        return deliveryService.reverse(lat, lng);
    }

    @GetMapping(value = "/basemap/style.json", produces = "application/json")
    public String basemapStyle(HttpServletRequest request) {
        // Scheme-relative: origin behind Cloudflare is HTTP, so http:// tile URLs are blocked on the HTTPS page.
        return RoadDistance.pointAtProxy(deliveryService.mapStyle(), request.getHeader("Host"));
    }

    @GetMapping("/basemap/proxy/**")
    public ResponseEntity<byte[]> basemap(HttpServletRequest request) {
        String uri = request.getRequestURI();
        int at = uri.indexOf("/basemap/proxy");
        String path = at >= 0 ? uri.substring(at + "/basemap/proxy".length()) : "";
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            path = path + "?" + query;
        }
        var response = deliveryService.mapAsset(path);
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new BusinessException("Goong từ chối khóa bản đồ. Dán khóa Map tiles từ account.goong.io.");
        }
        if (response.statusCode() >= 300) {
            throw new BusinessException("Không tải được mảnh bản đồ Goong (" + response.statusCode() + ").");
        }
        byte[] body = response.body();
        String type = response.contentType() == null || response.contentType().isBlank() ? tileType(path) : response.contentType();
        if (type.contains("json")) {
            body = RoadDistance.pointAtProxy(new String(body, StandardCharsets.UTF_8), request.getHeader("Host"))
                    .getBytes(StandardCharsets.UTF_8);
        }
        return ResponseEntity.ok()
                .header("Content-Type", type)
                .header("Cache-Control", "public, max-age=86400")
                .body(body);
    }

    private static String tileType(String path) {
        String file = path.contains("?") ? path.substring(0, path.indexOf('?')) : path;
        if (file.endsWith(".png")) {
            return "image/png";
        }
        if (file.endsWith(".json")) {
            return "application/json";
        }
        if (file.endsWith(".pbf")) {
            return "application/x-protobuf";
        }
        return "application/octet-stream";
    }

    @PostMapping("/stops")
    public DeliveryStop saveStop(@RequestBody DeliveryStop stop) {
        if (stop.isShop() && !currentUser.isCoordinator()) {
            throw new AccessDeniedException("Chỉ điều phối được đặt cửa hàng.");
        }
        return deliveryService.saveStop(stop);
    }

    @GetMapping("/drivers")
    public List<DeliveryDriver> drivers() {
        return deliveryService.drivers();
    }

    @PostMapping("/drivers")
    @PreAuthorize(DISPATCH)
    public DeliveryDriver saveDriver(@RequestBody DeliveryDriver driver) {
        return deliveryService.saveDriver(driver);
    }

    @GetMapping("/settings")
    public DeliverySetting settings() {
        return deliveryService.settings();
    }

    @PutMapping("/settings")
    @PreAuthorize(DISPATCH)
    public DeliverySetting saveSettings(@RequestBody DeliverySetting setting) {
        return deliveryService.saveSettings(setting);
    }

    @PostMapping("/books")
    public DeliveryBook createBook(@RequestBody BookRequest request) {
        return deliveryService.createBook(request, currentUser.getUserId());
    }

    @PostMapping("/books/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        deliveryService.cancelBook(id);
    }

    @PostMapping("/books/{id}/sender-confirm")
    public void senderConfirm(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean onBehalf) {
        if (onBehalf && !currentUser.isCoordinator()) {
            throw new AccessDeniedException("Chỉ điều phối được xác nhận hộ.");
        }
        deliveryService.confirmSender(id, onBehalf, currentUser.getUserId());
    }

    @PostMapping("/books/{id}/receiver-confirm")
    public void receiverConfirm(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean onBehalf) {
        if (onBehalf && !currentUser.isCoordinator()) {
            throw new AccessDeniedException("Chỉ điều phối được xác nhận hộ.");
        }
        deliveryService.confirmReceiver(id, onBehalf, currentUser.getUserId());
    }

    @PostMapping("/books/{id}/accept-detour")
    @PreAuthorize(DISPATCH)
    public String acceptDetour(@PathVariable Long id) {
        return deliveryService.acceptDetour(id);
    }

    @PostMapping("/books/{id}/assign")
    @PreAuthorize(DISPATCH)
    public void assign(@PathVariable Long id, @RequestParam(required = false) Long driverId) {
        deliveryService.assign(id, driverId);
    }

    @PostMapping("/books/{id}/confirm-sudden")
    @PreAuthorize(DISPATCH)
    public void confirmSudden(@PathVariable Long id) {
        deliveryService.confirmSudden(id);
    }

    @PostMapping("/books/{id}/reject-sudden")
    @PreAuthorize(DISPATCH)
    public void rejectSudden(@PathVariable Long id) {
        deliveryService.deferSudden(id);
    }

    @GetMapping("/days/{date}")
    public Map<String, Object> day(@PathVariable LocalDate date) {
        return deliveryService.day(date);
    }

    @PostMapping("/days/{date}/plan")
    @PreAuthorize(DISPATCH)
    public Map<String, Object> plan(@PathVariable LocalDate date) {
        return deliveryService.plan(date);
    }

    @PostMapping("/days/{date}/publish")
    @PreAuthorize(DISPATCH)
    public Map<String, Object> publish(@PathVariable LocalDate date) {
        return deliveryService.publish(date);
    }

    @PostMapping("/days/{date}/lock")
    @PreAuthorize(DISPATCH)
    public Map<String, Object> lock(@PathVariable LocalDate date, @RequestParam(required = false) Long startStopId) {
        return deliveryService.lock(date, startStopId);
    }

    @PostMapping("/plans/{id}/depart")
    @PreAuthorize(DISPATCH)
    public void depart(@PathVariable Long id) {
        deliveryService.depart(id);
    }

    @PostMapping("/plans/{id}/finish")
    @PreAuthorize(DISPATCH)
    public void finish(@PathVariable Long id) {
        deliveryService.finish(id);
    }

    @PostMapping("/plans/{id}/drop-return")
    @PreAuthorize(DISPATCH)
    public void dropReturn(@PathVariable Long id) {
        deliveryService.dropReturn(id);
    }

    @GetMapping("/notices")
    public List<DispatchNotice> notices() {
        return deliveryService.notices();
    }

    @PostMapping("/notices/{id}/ack")
    public void ack(@PathVariable Long id) {
        deliveryService.ack(id);
    }

    @PostMapping("/drivers/{id}/location")
    public void ping(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        if (body == null || body.get("latitude") == null || body.get("longitude") == null) {
            throw new BusinessException("Thiếu tọa độ.");
        }
        deliveryService.ping(id, body.get("latitude"), body.get("longitude"), currentUser.getUserId(), currentUser.isCoordinator());
    }
}
