package manage.store.inventory.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractReportDashboardDTO {

    private long total;
    private long completed;          // COMPLETED
    private long salesInput;         // SALES_INPUT
    private long measurementInput;   // MEASUREMENT_INPUT
    private long productionInput;    // PRODUCTION_INPUT
    private long stockkeeperInput;   // STOCKKEEPER_INPUT

    private List<ContractReportAlertDTO> lateDeliveries;
    private List<ContractReportAlertDTO> upcomingDeliveries;
    private List<ContractReportAlertDTO> lateTailorReturns;
}
