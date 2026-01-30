package cn.cordys.crm.system.dto;

public class ExportTaskProgressDTO {

    private String taskId;
    private int current;
    private int total;
    private String status;
    private String message;

    public ExportTaskProgressDTO() {
    }

    public ExportTaskProgressDTO(String taskId, int current, int total, String status) {
        this.taskId = taskId;
        this.current = current;
        this.total = total;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPercentage() {
        if (total == 0) {
            return 0;
        }
        return (int) ((current * 100.0) / total);
    }
}
