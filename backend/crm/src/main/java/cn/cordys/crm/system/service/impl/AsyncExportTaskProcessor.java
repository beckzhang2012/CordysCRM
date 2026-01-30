package cn.cordys.crm.system.service.impl;

import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.request.AsyncExportRequest;
import cn.cordys.crm.system.service.ExportMessageQueueService;
import cn.cordys.crm.system.service.ExportTaskService;
import cn.cordys.crm.system.service.StreamingExportService;
import cn.cordys.file.engine.DefaultRepositoryDir;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步导出任务处理器
 * 处理从消息队列中获取的导出任务
 */
@Service
public class AsyncExportTaskProcessor {

    @Resource
    private ExportTaskService exportTaskService;

    @Resource
    private Map<String, StreamingExportService> streamingExportServices;

    @Resource
    private ExportMessageQueueService exportMessageQueueService;
    
    // 用于存储正在处理的任务，支持任务取消
    private final ConcurrentHashMap<String, Boolean> processingTasks = new ConcurrentHashMap<>();

    /**
     * 初始化处理器，启动队列监听线程
     */
    @PostConstruct
    public void initialize() {
        // 启动一个线程监听消息队列
        Thread queueListener = new Thread(() -> {
            while (true) {
                try {
                    // 从队列中获取任务
                    AsyncExportRequest request = exportMessageQueueService.receiveExportTask();
                    if (request != null) {
                        // 异步处理任务
                        processExportTaskAsync(request);
                    }
                    Thread.sleep(100); // 避免CPU占用过高
                } catch (Exception e) {
                    LogUtils.error("监听导出任务队列异常", e);
                    try {
                        Thread.sleep(1000); // 出错后等待1秒再继续
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        queueListener.setDaemon(true);
        queueListener.setName("ExportQueueListener");
        queueListener.start();
    }

    /**
     * 异步处理导出任务
     */
    @Async("exportTaskExecutor")
    public void processExportTaskAsync(AsyncExportRequest request) {
        processExportTask(request);
    }

    /**
     * 处理导出任务
     */
    public void processExportTask(AsyncExportRequest request) {
        String taskId = request.getTaskId();
        processingTasks.put(taskId, true);
        
        try {
            // 更新任务状态为处理中
            exportTaskService.update(taskId, 
                    ExportConstants.ExportStatus.PROCESSING.toString(), 
                    request.getUserId());
            
            // 检查任务是否已被取消
            if (isTaskCancelled(taskId)) {
                exportTaskService.update(taskId, 
                        ExportConstants.ExportStatus.STOP.toString(), 
                        request.getUserId());
                return;
            }
            
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
                    exportTaskService.updateProgress(taskId, processed, total, percentage, request.getUserId());
                }

                @Override
                public void onComplete(String taskId, boolean success, String message) {
                    if (!success) {
                        exportTaskService.updateError(taskId, message, request.getUserId());
                    } else {
                        exportTaskService.update(taskId,
                                ExportConstants.ExportStatus.SUCCESS.toString(),
                                request.getUserId());
                    }
                    processingTasks.remove(taskId);
                }
            };

            // 执行流式导出
            exportService.streamExportToFile(request, filePath, progressCallback);
            
        } catch (Exception e) {
            LogUtils.error("处理导出任务失败: " + taskId, e);
            exportTaskService.updateError(taskId, "导出失败: " + e.getMessage(), request.getUserId());
            processingTasks.remove(taskId);
        }
    }

    /**
     * 根据导出类型获取对应的导出服务
     */
    private StreamingExportService getExportService(String exportType) {
        try {
            ExportConstants.ExportType type = ExportConstants.ExportType.valueOf(exportType);
            return switch (type) {
                case CUSTOMER -> streamingExportServices.get("customerStreamingExportService");
                case CLUE -> streamingExportServices.get("clueStreamingExportService");
                case OPPORTUNITY -> streamingExportServices.get("opportunityStreamingExportService");
                default -> null;
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 构建导出文件路径
     */
    private String buildFilePath(AsyncExportRequest request) {
        String exportDir = DefaultRepositoryDir.getDefaultDir()
                + File.separator
                + DefaultRepositoryDir.getExportDir(request.getOrganizationId())
                + File.separator + request.getFileId();
        File dir = new File(exportDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("无法创建导出目录: " + dir.getAbsolutePath());
        }
        return exportDir + File.separator + request.getFileName() + ".xlsx";
    }

    /**
     * 检查任务是否已被取消
     */
    public boolean isTaskCancelled(String taskId) {
        ExportTask task = exportTaskService.getById(taskId);
        return task != null && ExportConstants.ExportStatus.STOP.toString().equals(task.getStatus());
    }
}