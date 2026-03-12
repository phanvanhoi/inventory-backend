package manage.store.inventory.dto;

public class UnitEmployeeCreateDTO {

    private String fullName;
    private Long positionId;

    public UnitEmployeeCreateDTO() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }
}
