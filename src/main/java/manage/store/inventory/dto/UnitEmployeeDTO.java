package manage.store.inventory.dto;

public class UnitEmployeeDTO {

    private Long employeeId;
    private Long unitId;
    private String unitName;
    private String fullName;
    private Long positionId;
    private String positionCode;
    private String positionName;

    public UnitEmployeeDTO() {}

    public UnitEmployeeDTO(Long employeeId, Long unitId, String unitName,
                           String fullName, Long positionId,
                           String positionCode, String positionName) {
        this.employeeId = employeeId;
        this.unitId = unitId;
        this.unitName = unitName;
        this.fullName = fullName;
        this.positionId = positionId;
        this.positionCode = positionCode;
        this.positionName = positionName;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    public String getPositionCode() { return positionCode; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
}
