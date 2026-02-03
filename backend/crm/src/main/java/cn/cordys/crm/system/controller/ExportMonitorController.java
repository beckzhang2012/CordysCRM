package cn.cordys.crm.system.controller;

import cn.cordys.crm.system.service.ExportProgressService;
import cn.cordys.crm.system.service.ExportTaskQueueService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 导出监控控制器
 * 提供导出任务队列状态监控和进度查询接口
 */
@RestController
@RequestMapping("/export/monitor")
@Tag(name = "导出监控")
public class ExportMonitorController {

    @Resource
    private ExportTaskQueueService exportTaskQueueService;

    @Resource
    private ExportProgressService exportProgressService;

    /**
     * 获取导出队列状态
     *
     * @return 队列状态信息
     */
    @GetMapping("/queue/status")
    @Operation(summary = "获取导出队列状态")
    public Map<String, Object> getQueueStatus() {
        ExportTaskQueueService.QueueStatus status = exportTaskQueueService.getQueueStatus();

        Map<String, Object> result = new HashMap<>();
        result.put("redisQueueSize", status.redisQueueSize());
        result.put("localQueueSize", status.localQueueSize());
        result.put("activeCount", status.activeCount());
        result.put("poolSize", status.poolSize());
        result.put("maxConcurrent", status.maxConcurrent());
        result.put("queueCapacity", status.queueCapacity());
        result.put("usageRate", String.format("%.2f%%",
                (status.activeCount() * 100.0) / status.maxConcurrent()));

        return result;
    }

    /**
     * 获取任务进度
     *
     * @param taskId 任务ID
     * @return 进度信息
     */
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "获取导出任务进度")
    public Map<String, Object> getTaskProgress(@PathVariable String taskId) {
        ExportProgressService.ExportProgress progress = exportProgressService.getProgress(taskId);

        Map<String, Object> result = new HashMap<>();
        if (progress != null) {
            result.put("taskId", progress.getTaskId());
            result.put("status", progress.getStatus());
            result.put("processedCount", progress.getProcessedCount());
            result.put("totalCount", progress.getTotalCount());
            result.put("progressPercentage", progress.getProgressPercentage());
            result.put("errorMessage", progress.getErrorMessage());
            result.put("startTime", progress.getStartTime());
            result.put("endTime", progress.getEndTime());

            // 计算耗时
            if (progress.getEndTime() > 0) {
                result.put("duration", progress.getEndTime() - progress.getStartTime());
            } else {
                result.put("duration", System.currentTimeMillis() - progress.getStartTime());
            }
        } else {
            result.put("error", "任务不存在或已过期");
        }

        return result;
    }

    /**
     * 取消导出任务
     *
     * @param taskId 任务ID
     */
    @GetMapping("/cancel/{taskId}")
    @Operation(summary = "取消导出任务")
    public void cancelTask(@PathVariable String taskId) {
        exportTaskQueueService.cancelTask(taskId);
    }

    /**
     * 获取当前用户的导出统计
     *
     * @return 统计信息
     */
    @GetMapping("/user/stats")
    @Operation(summary = "获取当前用户导出统计")
    public Map<String, Object> getUserExportStats() {
        String userId = SessionUtils.getUserId();

        // TODO: 可以实现更详细的用户导出统计
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("message", "导出统计功能待实现");

        return result;
    }
}
