package cn.cordys.crm.system.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.service.ExportMessageQueueService;
import cn.cordys.crm.system.service.ExportTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 导出任务管理控制器
 * 提供导出任务的查询、取消等功能
 */
@Tag(name = "导出任务管理")
@RestController
@RequestMapping("/export/task")
public class ExportTaskController {

    @Resource
    private ExportTaskService exportTaskService;
    
    @Resource
    private ExportMessageQueueService exportMessageQueueService;

    @GetMapping("/{taskId}")
    @RequiresPermissions(PermissionConstants.EXPORT_TASK_READ)
    @Operation(summary = "获取导出任务详情")
    public ExportTask getTask(@PathVariable String taskId) {
        return exportTaskService.getById(taskId);
    }

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.EXPORT_TASK_READ)
    @Operation(summary = "获取用户导出任务列表")
    public List<ExportTask> getUserTasks(@RequestParam String userId, @RequestParam String organizationId) {
        return exportTaskService.getUserTasks(userId, organizationId);
    }

    @PostMapping("/{taskId}/cancel")
    @RequiresPermissions(PermissionConstants.EXPORT_TASK_CANCEL)
    @Operation(summary = "取消导出任务")
    public String cancelTask(@PathVariable String taskId, @RequestParam String userId) {
        // 取消消息队列中的任务
        boolean queueResult = exportMessageQueueService.cancelExportTask(taskId);
        
        // 更新数据库中的任务状态
        boolean dbResult = exportTaskService.cancelTask(taskId, userId);
        
        if (queueResult && dbResult) {
            return "任务已成功取消";
        } else {
            return "取消任务时出现问题，请检查任务状态";
        }
    }
    
    @GetMapping("/queue/size")
    @RequiresPermissions(PermissionConstants.EXPORT_TASK_READ)
    @Operation(summary = "获取导出队列大小")
    public long getQueueSize() {
        return exportMessageQueueService.getQueueSize();
    }
}