package manage.store.inventory.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "plan_visit_actions")
@Data
public class PlanVisitAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "action_type", nullable = false)
    private String actionType;
}
