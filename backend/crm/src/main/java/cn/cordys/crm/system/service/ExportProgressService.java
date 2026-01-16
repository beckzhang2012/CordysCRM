package cn.cordys.crm.system.service;

import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.dto.request.ExportQueueRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ExportProgressService {

    @Resource
    private ExportQueueService exportQueueService;

    public void updateProgress(String taskId, int current, int total) {
        try {
            exportQueueService.updateTaskProgress(taskId, current, total);
            LogUtils.info("Export task progress updated: " + taskId + " - " + current + "/" + total);
        } catch (Exception e) {
            LogUtils.error("Failed to update export progress: " + taskId, e);
        }
    }

    public ExportQueueRequest getTaskProgress(String taskId) {
        try {
            return exportQueueService.getTask(taskId);
        } catch (Exception e) {
            LogUtils.error("Failed to get export progress: " + taskId, e);
            return null;
        }
    }

    public int getProgressPercentage(String taskId) {
        ExportQueueRequest task = getTaskProgress(taskId);
        if (task == null || task.getProgressTotal() == null || task.getProgressTotal() == 0) {
            return 0;
        }
        int current = task.getProgressCurrent() != null ? task.getProgressCurrent() : 0;
        int total = task.getProgressTotal();
        return (current * 100) / total;
    }
}
