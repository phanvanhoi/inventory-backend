package manage.store.inventory.dto;

public class RequestCompleteResponseDTO {

    private Long requestId;
    private String requestStatus;
    private String setStatus;
    private int completedCount;
    private int totalCount;

    public RequestCompleteResponseDTO(Long requestId, String requestStatus,
            String setStatus, int completedCount, int totalCount) {
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        this.setStatus = setStatus;
        this.completedCount = completedCount;
        this.totalCount = totalCount;
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }

    public String getSetStatus() { return setStatus; }
    public void setSetStatus(String setStatus) { this.setStatus = setStatus; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}
