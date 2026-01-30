package cn.cordys.crm.system.service;

import cn.cordys.crm.system.dto.request.AsyncExportRequest;
import cn.cordys.crm.system.service.impl.ExportMessageQueueServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 异步导出任务处理器
 * 负责处理从消息队列中获取的导出任务
 */
@Service
public class AsyncExportTaskProcessor {
    
    @Resource
    private ExportMessageQueueServiceImpl exportMessageQueueService;
    
    @Resource
    private Map<String, StreamingExportService> streamingExportServices;
    
    @Resource
    private ExportTaskService exportTaskService;
    
    /**
     * 处理导出任务
     * 
     * @param request 导出请求
     */
    public void processExportTask(AsyncExportRequest request) {
        try {
            // 更新任务状态为处理中
            exportTaskService.update(request.getTaskId(), 
                    cn.cordys.crm.system.constants.ExportConstants.ExportStatus.PROCESSING.toString(), 
                    request.getUserId());
            
            // 根据导出类型获取对应的流式导出服务
            StreamingExportService exportService = getExportService(request.getExportType());
            if (exportService == null) {
                throw new RuntimeException("不支持的导出类型: " + request.getExportType());
            }
            
            // 构建文件路径
            String filePath = buildFilePath(request);
            
            // 创建进度回调
            StreamingExportService.ProgressCallback progressCallback = new StreamingExportService.ProgressCallback() {
                @Override
                public void onProgress(String taskId, long processed, long total, double percentage) {
                    // 更新任务进度
                    exportTaskService.updateProgress(taskId, processed, total, percentage, request.getUserId());
                }
                
                @Override
                public void onComplete(String taskId, boolean success, String message) {
                    // 更新任务状态
                    String status = success ? 
                            cn.cordys.crm.system.constants.ExportConstants.ExportStatus.SUCCESS.toString() : 
                            cn.cordys.crm.system.constants.ExportConstants.ExportStatus.ERROR.toString();
                    exportTaskService.update(taskId, status, request.getUserId());
                }
            };
            
            // 执行流式导出
            exportService.streamExportToFile(request, filePath, progressCallback);
            
        } catch (Exception e) {
            // 处理异常
            exportTaskService.update(request.getTaskId(), 
                    cn.cordys.crm.system.constants.ExportConstants.ExportStatus.ERROR.toString(), 
                    request.getUserId());
            throw new RuntimeException("处理导出任务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据导出类型获取对应的导出服务
     */
    private StreamingExportService getExportService(String exportType) {
        // 根据导出类型获取对应的服务
        switch (cn.cordys.crm.system.constants.ExportConstants.ExportType.valueOf(exportType)) {
            case CUSTOMER:
                return streamingExportServices.get("customerStreamingExportService");
            case CLUE:
                return streamingExportServices.get("clueStreamingExportService");
            case OPPORTUNITY:
                return streamingExportServices.get("opportunityStreamingExportService");
            // 添加其他类型的导出服务
            default:
                return null;
        }
    }
    
    /**
     * 构建文件路径
     */
    private String buildFilePath(AsyncExportRequest request) {
        // 构建导出目录路径
        String exportDir = cn.cordys.file.engine.DefaultRepositoryDir.getDefaultDir()
                + File.separator
                + cn.cordys.file.engine.DefaultRepositoryDir.getExportDir(request.getOrganizationId())
                + File.separator + request.getFileId();
        
        // 确保目录存在
        File dir = new File(exportDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("无法创建导出目录: " + dir.getAbsolutePath());
        }
        
        // 返回完整的文件路径
        return exportDir + File.separator + request.getFileName() + ".xlsx";
    }
}