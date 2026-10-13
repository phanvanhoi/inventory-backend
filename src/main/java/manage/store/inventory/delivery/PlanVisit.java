package manage.store.inventory.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "plan_visits")
@Data
public class PlanVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "seq_no", nullable = false)
    private int seqNo;

    @Column(name = "stop_id", nullable = false)
    private Long stopId;
}
