package cn.cordys.crm.system.queue.dto;

import cn.cordys.crm.system.constants.ExportConstants;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class ExportTaskMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;

    private String fileId;

    private String fileName;

    private String orgId;

    private String userId;

    private ExportConstants.ExportType exportType;

    private ExportConstants.ExportStatus status;

    private List<String> selectIds;

    private String conditionJson;

    private String headListJson;

    private Locale locale;

    private String logModule;

    private long createTime;

    private int batchSize;

    private int retryCount;

    public ExportTaskMessage() {
        this.createTime = System.currentTimeMillis();
        this.batchSize = 2000;
        this.retryCount = 0;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ExportConstants.ExportType getExportType() {
        return exportType;
    }

    public void setExportType(ExportConstants.ExportType exportType) {
        this.exportType = exportType;
    }

    public ExportConstants.ExportStatus getStatus() {
        return status;
    }

    public void setStatus(ExportConstants.ExportStatus status) {
        this.status = status;
    }

    public List<String> getSelectIds() {
        return selectIds;
    }

    public void setSelectIds(List<String> selectIds) {
        this.selectIds = selectIds;
    }

    public String getConditionJson() {
        return conditionJson;
    }

    public void setConditionJson(String conditionJson) {
        this.conditionJson = conditionJson;
    }

    public String getHeadListJson() {
        return headListJson;
    }

    public void setHeadListJson(String headListJson) {
        this.headListJson = headListJson;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getLogModule() {
        return logModule;
    }

    public void setLogModule(String logModule) {
        this.logModule = logModule;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
