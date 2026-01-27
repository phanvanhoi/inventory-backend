package manage.store.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "length_types")
public class LengthType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lengthTypeId;

    private String code;

    public Long getLengthTypeId() { return lengthTypeId; }
    public void setLengthTypeId(Long lengthTypeId) { this.lengthTypeId = lengthTypeId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
