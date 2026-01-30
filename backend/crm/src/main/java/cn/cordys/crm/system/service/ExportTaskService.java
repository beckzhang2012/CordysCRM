package cn.cordys.crm.system.service;

import cn.cordys.common.exception.GenericException;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.Translator;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.mapper.ExtExportTaskMapper;
import cn.cordys.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("ExportTaskService")
@Transactional(rollbackFor = Exception.class)
public class ExportTaskService {

    @Resource
    private BaseMapper<ExportTask> exportTaskMapper;
    @Resource
    private ExtExportTaskMapper extExportTaskMapper;

    public ExportTask saveTask(String orgId, String fileId, String userId, String resourceType, String fileName) {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(IDGenerator.nextStr());
        exportTask.setResourceType(resourceType);
        exportTask.setCreateUser(userId);
        exportTask.setCreateTime(System.currentTimeMillis());
        exportTask.setStatus(ExportConstants.ExportStatus.PREPARED.toString());
        exportTask.setUpdateUser(userId);
        exportTask.setFileName(fileName);
        exportTask.setUpdateTime(System.currentTimeMillis());
        exportTask.setOrganizationId(orgId);
        exportTask.setFileId(fileId);
        exportTask.setProgressPercentage(0.0);
        exportTask.setProcessedCount(0L);
        exportTask.setTotalCount(0L);
        exportTask.setPriority(5);
        exportTaskMapper.insert(exportTask);
        return exportTask;
    }

    public void update(String taskId, String status, String userId) {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(taskId);
        exportTask.setStatus(status);
        exportTask.setUpdateTime(System.currentTimeMillis());
        exportTask.setUpdateUser(userId);
        
        // 如果是开始处理，设置开始时间
        if (ExportConstants.ExportStatus.PROCESSING.toString().equals(status)) {
            exportTask.setStartTime(System.currentTimeMillis());
        }
        // 如果是完成或失败，设置结束时间
        else if (ExportConstants.ExportStatus.SUCCESS.toString().equals(status) || 
                 ExportConstants.ExportStatus.ERROR.toString().equals(status) ||
                 ExportConstants.ExportStatus.STOP.toString().equals(status)) {
            exportTask.setEndTime(System.currentTimeMillis());
        }
        
        exportTaskMapper.updateById(exportTask);
    }

    public void checkUserTaskLimit(String userId, String status) {
        int userTaskCount = extExportTaskMapper.getExportTaskCount(userId, status);
        if (userTaskCount >= 10) {
            throw new GenericException(Translator.get("user_export_task_limit"));
        }
    }
    
    /**
     * 根据ID获取导出任务
     * 
     * @param taskId 任务ID
     * @return 导出任务
     */
    public ExportTask getById(String taskId) {
        return exportTaskMapper.selectByPrimaryKey(taskId);
    }
    
    /**
     * 更新任务进度
     * 
     * @param taskId 任务ID
     * @param processed 已处理数量
     * @param total 总数量
     * @param percentage 百分比
     * @param userId 用户ID
     */
    public void updateProgress(String taskId, long processed, long total, double percentage, String userId) {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(taskId);
        exportTask.setProcessedCount(processed);
        exportTask.setTotalCount(total);
        exportTask.setProgressPercentage(percentage);
        exportTask.setUpdateTime(System.currentTimeMillis());
        exportTask.setUpdateUser(userId);
        exportTaskMapper.updateById(exportTask);
    }
    
    /**
     * 更新任务错误信息
     * 
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     * @param userId 用户ID
     */
    public void updateError(String taskId, String errorMessage, String userId) {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(taskId);
        exportTask.setStatus(ExportConstants.ExportStatus.ERROR.toString());
        exportTask.setErrorMessage(errorMessage);
        exportTask.setEndTime(System.currentTimeMillis());
        exportTask.setUpdateTime(System.currentTimeMillis());
        exportTask.setUpdateUser(userId);
        exportTaskMapper.updateById(exportTask);
    }
    
    /**
     * 获取用户的导出任务列表
     * 
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @return 任务列表
     */
    public List<ExportTask> getUserTasks(String userId, String organizationId) {
        // 这里需要实现根据用户ID和组织ID查询任务列表的方法
        // 暂时返回空列表，实际实现需要添加对应的Mapper方法
        return List.of();
    }
    
    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean cancelTask(String taskId, String userId) {
        try {
            ExportTask exportTask = new ExportTask();
            exportTask.setId(taskId);
            exportTask.setStatus(ExportConstants.ExportStatus.STOP.toString());
            exportTask.setEndTime(System.currentTimeMillis());
            exportTask.setUpdateTime(System.currentTimeMillis());
            exportTask.setUpdateUser(userId);
            exportTaskMapper.updateById(exportTask);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}