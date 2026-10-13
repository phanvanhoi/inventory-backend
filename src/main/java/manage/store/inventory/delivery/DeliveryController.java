package manage.store.inventory.delivery;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
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
public class DeliveryController {

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
        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto == null || proto.isBlank()) {
            proto = request.getScheme();
        }
        String host = request.getHeader("Host");
        String style = deliveryService.mapStyle();
        if (host == null || host.isBlank()) {
            return style;
        }
        return style.replace("/api/delivery/basemap/proxy", proto + "://" + host + "/api/delivery/basemap/proxy");
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
        if (response.statusCode() == 423) {
            throw new BusinessException("Vietmap chặn bản đồ nền. Dán khóa Tilemap, khóa thứ hai trong email đăng ký.");
        }
        if (response.statusCode() >= 300) {
            throw new BusinessException("Không tải được mảnh bản đồ Vietmap (" + response.statusCode() + ").");
        }
        String type = response.headers().firstValue("content-type").orElse(tileType(path));
        return ResponseEntity.ok()
                .header("Content-Type", type)
                .header("Cache-Control", "public, max-age=86400")
                .body(response.body());
    }

    private static String tileType(String path) {
        String file = path.contains("?") ? path.substring(0, path.indexOf('?')) : path;
        if (file.endsWith(".png")) {
            return "image/png";
        }
        if (file.endsWith(".json")) {
            return "application/json";
        }
        return "application/octet-stream";
    }

    @PostMapping("/stops")
    public DeliveryStop saveStop(@RequestBody DeliveryStop stop) {
        return deliveryService.saveStop(stop);
    }

    @GetMapping("/drivers")
    public List<DeliveryDriver> drivers() {
        return deliveryService.drivers();
    }

    @PostMapping("/drivers")
    public DeliveryDriver saveDriver(@RequestBody DeliveryDriver driver) {
        return deliveryService.saveDriver(driver);
    }

    @GetMapping("/settings")
    public DeliverySetting settings() {
        return deliveryService.settings();
    }

    @PutMapping("/settings")
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
        deliveryService.confirmSender(id, onBehalf, currentUser.getUserId());
    }

    @PostMapping("/books/{id}/receiver-confirm")
    public void receiverConfirm(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean onBehalf) {
        deliveryService.confirmReceiver(id, onBehalf, currentUser.getUserId());
    }

    @PostMapping("/books/{id}/accept-detour")
    public String acceptDetour(@PathVariable Long id) {
        return deliveryService.acceptDetour(id);
    }

    @GetMapping("/days/{date}")
    public Map<String, Object> day(@PathVariable LocalDate date) {
        return deliveryService.day(date);
    }

    @PostMapping("/days/{date}/plan")
    public Map<String, Object> plan(@PathVariable LocalDate date) {
        return deliveryService.plan(date);
    }

    @PostMapping("/days/{date}/publish")
    public Map<String, Object> publish(@PathVariable LocalDate date) {
        return deliveryService.publish(date);
    }

    @PostMapping("/days/{date}/lock")
    public Map<String, Object> lock(@PathVariable LocalDate date, @RequestParam(required = false) Long startStopId) {
        return deliveryService.lock(date, startStopId);
    }

    @PostMapping("/plans/{id}/depart")
    public void depart(@PathVariable Long id) {
        deliveryService.depart(id);
    }

    @PostMapping("/plans/{id}/finish")
    public void finish(@PathVariable Long id) {
        deliveryService.finish(id);
    }

    @PostMapping("/plans/{id}/drop-return")
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
        deliveryService.ping(id, body.get("latitude"), body.get("longitude"));
    }
}
