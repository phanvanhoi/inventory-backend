package manage.store.inventory.delivery;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.delivery.DeliveryService.BookRequest;
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
