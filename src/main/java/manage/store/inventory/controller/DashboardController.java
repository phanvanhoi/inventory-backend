package manage.store.inventory.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.dto.DashboardRowDTO;
import manage.store.inventory.dto.DashboardSummaryDTO;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.DashboardService;

/**
 * Dashboard aggregate endpoints (G11, W22).
 * Role-based scope filter applied in service.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    public DashboardController(DashboardService dashboardService, CurrentUser currentUser) {
        this.dashboardService = dashboardService;
        this.currentUser = currentUser;
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public DashboardSummaryDTO getSummary(@RequestParam(required = false) Integer year) {
        return dashboardService.getSummary(year, currentUser.getUserId(), getUserRoles());
    }

    @GetMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public List<DashboardRowDTO> getReports(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) Long customerId) {
        return dashboardService.getReports(year, status, province, customerId,
                currentUser.getUserId(), getUserRoles());
    }

    private Set<String> getUserRoles() {
        return new HashSet<>(currentUser.get().getRoles());
    }
}
