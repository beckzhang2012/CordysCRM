package cn.cordys.crm.system.queue.consumer;

import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.queue.ExportTaskQueueManager;
import cn.cordys.crm.system.queue.dto.ExportTaskMessage;
import cn.cordys.crm.system.service.ExportTaskService;
import cn.cordys.registry.ExportThreadRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ExportTaskMessageConsumer {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final long POLL_TIMEOUT_SECONDS = 5;

    @Resource
    private ExportTaskQueueManager queueManager;

    @Resource
    private ExportTaskService exportTaskService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;
    private Thread consumerThread;

    @PostConstruct
    public void start() {
        if (running.compareAndSet(false, true)) {
            this.executorService = new ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAX_POOL_SIZE,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                    new ThreadFactory() {
                        private int count = 0;
                        @Override
                        public Thread newThread(Runnable r) {
                            Thread thread = new Thread(r, "export-worker-" + count++);
                            thread.setDaemon(true);
                            return thread;
                        }
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

            consumerThread = new Thread(this::consumeLoop, "export-task-consumer");
            consumerThread.setDaemon(true);
            consumerThread.start();

            LogUtils.info("导出任务消息消费者已启动");
        }
    }

    @PreDestroy
    public void stop() {
        if (running.compareAndSet(true, false)) {
            LogUtils.info("正在停止导出任务消息消费者...");

            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            if (consumerThread != null) {
                consumerThread.interrupt();
                try {
                    consumerThread.join(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            LogUtils.info("导出任务消息消费者已停止");
        }
    }

    private void consumeLoop() {
        while (running.get()) {
            try {
                ExportTaskMessage message = queueManager.blockingDequeueTask(POLL_TIMEOUT_SECONDS);

                if (message != null) {
                    if (!queueManager.tryLockTask(message.getTaskId())) {
                        LogUtils.warn("任务已被其他实例处理，跳过: taskId={}", message.getTaskId());
                        continue;
                    }

                    CompletableFuture.runAsync(() -> processTask(message), executorService)
                            .whenComplete((result, ex) -> queueManager.unlockTask(message.getTaskId()));
                }
            } catch (Exception e) {
                if (!running.get()) {
                    break;
                }
                LogUtils.error("消费导出任务异常", e);
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
        try {
            LocaleContextHolder.setLocale(message.getLocale());
            ExportThreadRegistry.register(message.getTaskId(), Thread.currentThread());

            LogUtils.info("开始处理导出任务: taskId={}, type={}", message.getTaskId(), message.getExportType());

            exportTaskService.update(message.getTaskId(),
                    ExportConstants.ExportStatus.PREPARED.toString(), message.getUserId());

            executeExport(message);

            exportTaskService.update(message.getTaskId(),
                    ExportConstants.ExportStatus.SUCCESS.toString(), message.getUserId());

            LogUtils.info("导出任务完成: taskId={}", message.getTaskId());

        } catch (InterruptedException e) {
            LogUtils.error("导出任务被中断: taskId=" + message.getTaskId(), e);
            exportTaskService.update(message.getTaskId(),
                    ExportConstants.ExportStatus.STOP.toString(), message.getUserId());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LogUtils.error("导出任务执行异常: taskId=" + message.getTaskId(), e);
            exportTaskService.update(message.getTaskId(),
                    ExportConstants.ExportStatus.ERROR.toString(), message.getUserId());
        } finally {
            ExportThreadRegistry.remove(message.getTaskId());
            LocaleContextHolder.resetLocaleContext();
        }
    }

    protected void executeExport(ExportTaskMessage message) throws Exception {
        LogUtils.warn("未实现的导出任务执行方法，子类应重写此方法: taskId={}", message.getTaskId());
    }
}
