package cn.cordys.crm.system.consumer;

import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.ApplicationContextProvider;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.request.ExportQueueRequest;
import cn.cordys.crm.system.service.ExportQueueService;
import cn.cordys.crm.system.service.ExportTaskService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ExportQueueConsumer {

    private static final int CONSUMER_THREAD_COUNT = 5;
    private static final int MAX_RETRY_COUNT = 3;

    @Resource
    private ExportQueueService exportQueueService;

    @Resource
    private ExportTaskService exportTaskService;

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    private ExecutorService consumerExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @PostConstruct
    public void start() {
        running.set(true);
        consumerExecutor = Executors.newFixedThreadPool(CONSUMER_THREAD_COUNT);
        
        for (int i = 0; i < CONSUMER_THREAD_COUNT; i++) {
            consumerExecutor.submit(this::consumeTasks);
        }
        
        LogUtils.info("ExportQueueConsumer started with " + CONSUMER_THREAD_COUNT + " threads");
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (consumerExecutor != null) {
            consumerExecutor.shutdown();
            try {
                if (!consumerExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    consumerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                consumerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        LogUtils.info("ExportQueueConsumer stopped");
    }

    private void consumeTasks() {
        while (running.get()) {
            try {
                String taskId = exportQueueService.dequeueTask();
                if (taskId != null) {
                    taskExecutor.submit(() -> processTask(taskId));
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LogUtils.error("Error consuming export task", e);
            }
        }
    }

    @Async
    private void processTask(String taskId) {
        ExportQueueRequest queueRequest = null;
        try {
            queueRequest = exportQueueService.getTask(taskId);
            if (queueRequest == null) {
                LogUtils.error("Export task not found: " + taskId);
                return;
            }

            Locale locale = queueRequest.getLocale() != null ? Locale.forLanguageTag(queueRequest.getLocale()) : LocaleContextHolder.getLocale();
            LocaleContextHolder.setLocale(locale);

            ExportTask exportTask = exportTaskService.saveTask(
                queueRequest.getOrgId(),
                taskId,
                queueRequest.getUserId(),
                queueRequest.getResourceType(),
                queueRequest.getFileName()
            );

            executeExportTask(exportTask, queueRequest, locale);

            exportQueueService.removeTask(taskId);

        } catch (InterruptedException e) {
            LogUtils.error("Export task interrupted: " + taskId, e);
            exportTaskService.update(taskId, ExportConstants.ExportStatus.STOP.toString(), queueRequest.getUserId());
            exportQueueService.removeTask(taskId);
        } catch (Exception e) {
            LogUtils.error("Export task failed: " + taskId, e);
            handleTaskFailure(taskId, queueRequest, e);
        }
    }

    private void executeExportTask(ExportTask exportTask, ExportQueueRequest queueRequest, Locale locale) throws Exception {
        String exportType = queueRequest.getExportType();
        String resourceType = queueRequest.getResourceType();

        if (ExportConstants.ExportType.CUSTOMER.toString().equals(resourceType)) {
            cn.cordys.crm.customer.service.CustomerExportService customerExportService = 
                ApplicationContextProvider.getBean(cn.cordys.crm.customer.service.CustomerExportService.class);
            customerExportService.exportCustomerData(exportTask, queueRequest);
        } else {
            LogUtils.error("Unsupported export type: " + exportType);
            throw new IllegalArgumentException("Unsupported export type: " + exportType);
        }
    }

    private void handleTaskFailure(String taskId, ExportQueueRequest queueRequest, Exception e) {
        if (queueRequest == null) {
            return;
        }

        int retryCount = queueRequest.getRetryCount() != null ? queueRequest.getRetryCount() : 0;
        int maxRetryCount = queueRequest.getMaxRetryCount() != null ? queueRequest.getMaxRetryCount() : MAX_RETRY_COUNT;

        if (retryCount < maxRetryCount) {
            queueRequest.setRetryCount(retryCount + 1);
            exportQueueService.retryTask(taskId);
            LogUtils.info("Export task will be retried: " + taskId + ", retry count: " + queueRequest.getRetryCount());
        } else {
            exportTaskService.update(taskId, ExportConstants.ExportStatus.ERROR.toString(), queueRequest.getUserId());
            exportQueueService.removeTask(taskId);
            LogUtils.error("Export task failed after max retries: " + taskId);
        }
    }
}
