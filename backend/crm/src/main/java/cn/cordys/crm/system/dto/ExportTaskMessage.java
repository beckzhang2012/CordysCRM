package cn.cordys.crm.system.dto;

import java.io.Serializable;
import java.util.Locale;

public class ExportTaskMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;
    private String fileId;
    private String userId;
    private String orgId;
    private String exportType;
    private String fileName;
    private Locale locale;
    private String requestJson;
    private long createTime;

    public ExportTaskMessage() {
    }

    public ExportTaskMessage(String taskId, String fileId, String userId, String orgId,
                             String exportType, String fileName, Locale locale, String requestJson) {
        this.taskId = taskId;
        this.fileId = fileId;
        this.userId = userId;
        this.orgId = orgId;
        this.exportType = exportType;
        this.fileName = fileName;
        this.locale = locale;
        this.requestJson = requestJson;
        this.createTime = System.currentTimeMillis();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
