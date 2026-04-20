package manage.store.inventory.service;

import java.util.List;
import java.util.Set;

import manage.store.inventory.dto.CustomerRollupDTO;
import manage.store.inventory.dto.DashboardRowDTO;
import manage.store.inventory.dto.DashboardSummaryDTO;

public interface DashboardService {

    /**
     * KPI tiles. Scope filter theo role của user.
     */
    DashboardSummaryDTO getSummary(Integer year, Long userId, Set<String> roles);

    /**
     * Full list 36-col. Filters + role-based visibility.
     */
    List<DashboardRowDTO> getReports(
            Integer year,
            String status,
            String province,
            Long customerId,
            Long userId,
            Set<String> roles);

    /**
     * Customer parent roll-up.
     */
    CustomerRollupDTO getCustomerRollup(Long customerId, Integer year);
}
