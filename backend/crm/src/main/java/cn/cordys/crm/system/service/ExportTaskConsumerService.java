package cn.cordys.crm.system.service;

import cn.cordys.aspectj.constants.LogModule;
import cn.cordys.common.dto.DeptDataPermissionDTO;
import cn.cordys.common.dto.ExportSelectRequest;
import cn.cordys.common.util.JSON;
import cn.cordys.common.util.LogUtils;

import cn.cordys.crm.config.ExportQueueConfig;
import cn.cordys.crm.customer.dto.request.CustomerExportRequest;
import cn.cordys.crm.customer.service.CustomerExportService;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.ExportTaskMessage;
import cn.cordys.registry.ExportThreadRegistry;
import java.util.Map;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ExportTaskConsumerService {

    @Resource
    private RBlockingQueue<String> exportTaskQueue;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ExportTaskService exportTaskService;

    @Resource
    private ExportTaskProducerService exportTaskProducerService;

    @Value("${export.consumer.threads:5}")
    private int consumerThreads;

    private ExecutorService consumerExecutor;
    private volatile boolean running = true;

    @PostConstruct
    public void startConsumers() {
        consumerExecutor = Executors.newFixedThreadPool(consumerThreads, r -> {
            Thread t = new Thread(r, "export-task-consumer");
            t.setDaemon(true);
            return t;
        });

        for (int i = 0; i < consumerThreads; i++) {
            consumerExecutor.submit(this::consumeTasks);
        }
        LogUtils.info("Export task consumers started with {} threads", consumerThreads);
    }

    @PreDestroy
    public void stopConsumers() {
        running = false;
        if (consumerExecutor != null) {
            consumerExecutor.shutdown();
            try {
                if (!consumerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    consumerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                consumerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        LogUtils.info("Export task consumers stopped");
    }

    private void consumeTasks() {
        while (running) {
            try {
                String messageJson = exportTaskQueue.poll(1, TimeUnit.SECONDS);
                if (messageJson == null) {
                    continue;
                }

                ExportTaskMessage message = JSON.parseObject(messageJson, ExportTaskMessage.class);
                if (message != null) {
                    processTask(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LogUtils.error("Error consuming export task", e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processTask(ExportTaskMessage message) {
        String taskId = message.getTaskId();
        try {
            ExportThreadRegistry.register(taskId, Thread.currentThread());
            
            if (exportTaskProducerService.isTaskCancelled(taskId)) {
                LogUtils.info("Task {} has been cancelled, skipping", taskId);
                exportTaskProducerService.clearCancelFlag(taskId);
                return;
            }

            exportTaskService.update(taskId, ExportConstants.ExportStatus.PROCESSING.toString(), message.getUserId());

            LocaleContextHolder.setLocale(message.getLocale());

            ExportConstants.ExportType exportType = ExportConstants.ExportType.valueOf(message.getExportType());
            dispatchExportTask(exportType, message);

            exportTaskService.update(taskId, ExportConstants.ExportStatus.SUCCESS.toString(), message.getUserId());
            LogUtils.info("Export task {} completed successfully", taskId);

        } catch (InterruptedException e) {
            LogUtils.info("Export task {} was interrupted", taskId);
            exportTaskService.update(taskId, ExportConstants.ExportStatus.STOP.toString(), message.getUserId());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LogUtils.error("Export task " + taskId + " failed: " + e.getMessage(), e);
            exportTaskService.update(taskId, ExportConstants.ExportStatus.ERROR.toString(), message.getUserId());
        } finally {
            ExportThreadRegistry.remove(taskId);
            LocaleContextHolder.resetLocaleContext();
            exportTaskProducerService.clearCancelFlag(taskId);
            exportTaskProducerService.clearTaskProgress(taskId);
        }
    }

    private void dispatchExportTask(ExportConstants.ExportType exportType, ExportTaskMessage message) throws Exception {
        switch (exportType) {
            case CUSTOMER:
                handleCustomerExport(message);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export type: " + exportType);
        }
    }

    @Autowired
    private CustomerExportService customerExportService;

    private void handleCustomerExport(ExportTaskMessage message) throws Exception {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(message.getTaskId());
        exportTask.setFileId(message.getFileId());
        exportTask.setOrganizationId(message.getOrgId());

        String requestJson = message.getRequestJson();
        Map<String, Object> requestObj = JSON.parseObject(requestJson, Map.class);
        
        if (requestObj.containsKey("ids") && requestObj.get("ids") != null) {
            ExportSelectRequest selectRequest = JSON.parseObject(requestJson, ExportSelectRequest.class);
            customerExportService.exportSelectData(exportTask, message.getUserId(), selectRequest, message.getOrgId(), message.getLocale());
        } else {
            CustomerExportRequest exportRequest = JSON.parseObject(requestJson, CustomerExportRequest.class);
            DeptDataPermissionDTO deptDataPermission = null;
            if (requestObj.containsKey("deptDataPermission")) {
                deptDataPermission = JSON.parseObject(
                    JSON.toJSONString(requestObj.get("deptDataPermission")), 
                    DeptDataPermissionDTO.class
                );
            }
            customerExportService.exportCustomerData(exportTask, message.getUserId(), exportRequest, 
                message.getOrgId(), deptDataPermission, message.getLocale());
        }
    }
}
