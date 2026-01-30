package cn.cordys.crm.system.controller;

import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.ExportTaskProgressDTO;
import cn.cordys.crm.system.mapper.ExtExportTaskMapper;
import cn.cordys.crm.system.service.ExportTaskProducerService;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import cn.cordys.registry.ExportThreadRegistry;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "导出任务管理")
@RestController
@RequestMapping("/api/export-task")
public class ExportTaskController {

    @Resource
    private BaseMapper<ExportTask> exportTaskMapper;

    @Resource
    private ExtExportTaskMapper extExportTaskMapper;

    @Resource
    private ExportTaskProducerService exportTaskProducerService;

    @Operation(summary = "查询当前用户的导出任务列表")
    @GetMapping("/my-tasks")
    public List<ExportTask> getMyTasks() {
        String userId = SessionUtils.getUserId();
        String orgId = OrganizationContext.getOrganizationId();
        
        LambdaQueryWrapper<ExportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExportTask::getCreateUser, userId)
               .eq(ExportTask::getOrganizationId, orgId)
               .orderByDesc(ExportTask::getCreateTime);
        
        return exportTaskMapper.selectListByLambda(wrapper);
    }

    @Operation(summary = "查询任务进度")
    @GetMapping("/progress/{taskId}")
    public ExportTaskProgressDTO getTaskProgress(@PathVariable String taskId) {
        ExportTask task = exportTaskMapper.selectByPrimaryKey(taskId);
        if (task == null) {
            return null;
        }

        ExportTaskProgressDTO progress = new ExportTaskProgressDTO();
        progress.setTaskId(taskId);
        progress.setStatus(task.getStatus());

        String progressStr = exportTaskProducerService.getTaskProgress(taskId);
        if (progressStr != null && progressStr.contains("/")) {
            String[] parts = progressStr.split("/");
            progress.setCurrent(Integer.parseInt(parts[0]));
            progress.setTotal(Integer.parseInt(parts[1]));
        }

        return progress;
    }

    @Operation(summary = "取消导出任务")
    @PostMapping("/cancel/{taskId}")
    public void cancelTask(@PathVariable String taskId) {
        ExportTask task = exportTaskMapper.selectByPrimaryKey(taskId);
        if (task == null) {
            return;
        }

        String userId = SessionUtils.getUserId();
        if (!userId.equals(task.getCreateUser())) {
            return;
        }

        String status = task.getStatus();
        if (ExportConstants.ExportStatus.SUCCESS.toString().equals(status) ||
            ExportConstants.ExportStatus.ERROR.toString().equals(status) ||
            ExportConstants.ExportStatus.STOP.toString().equals(status)) {
            return;
        }

        exportTaskProducerService.cancelExportTask(taskId);
        ExportThreadRegistry.stop(taskId);
        
        task.setStatus(ExportConstants.ExportStatus.STOP.toString());
        exportTaskMapper.updateById(task);
    }

    @Operation(summary = "删除导出任务记录")
    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable String taskId) {
        ExportTask task = exportTaskMapper.selectByPrimaryKey(taskId);
        if (task == null) {
            return;
        }

        String userId = SessionUtils.getUserId();
        if (!userId.equals(task.getCreateUser())) {
            return;
        }

        exportTaskMapper.deleteByPrimaryKey(taskId);
        exportTaskProducerService.clearTaskProgress(taskId);
    }
}
