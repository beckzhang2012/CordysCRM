package cn.cordys.crm.system.service;

import cn.cordys.common.constants.TopicConstants;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.redis.MessagePublisher;
import cn.cordys.common.util.LogUtils;
import cn.cordys.common.util.Translator;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.ExportTaskMessage;
import cn.cordys.registry.ExportThreadRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 导出任务队列服务
 * 使用有界队列管理导出任务，控制并发数量，避免内存溢出
 */
@Service
public class ExportTaskQueueService {

    /**
     * 导出任务队列最大容量
     */
    private static final int QUEUE_CAPACITY = 100;

    /**
     * 最大并发导出任务数
     */
    private static final int MAX_CONCURRENT_EXPORTS = 5;

    /**
     * 任务队列Redis Key前缀
     */
    private static final String EXPORT_QUEUE_KEY = "export:task:queue";

    /**
     * 正在处理的任务Redis Key前缀
     */
    private static final String EXPORT_PROCESSING_KEY = "export:task:processing";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ExportTaskService exportTaskService;

    @Resource
    private ExportProgressService exportProgressService;

    @Resource
    private MessagePublisher messagePublisher;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 本地有界阻塞队列，用于控制并发
     */
    private LinkedBlockingQueue<ExportTaskMessage> taskQueue;

    /**
     * 线程池执行器
     */
    private ThreadPoolExecutor executor;

    /**
     * 任务处理器映射
     */
    private final ConcurrentHashMap<String, Consumer<ExportTaskMessage>> taskHandlers = new ConcurrentHashMap<>();

    /**
     * 初始化队列和线程池
     */
    @PostConstruct
    public void init() {
        // 创建有界队列，避免无限堆积导致内存溢出
        taskQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        // 创建线程池，控制并发导出任务数
        executor = new ThreadPoolExecutor(
                MAX_CONCURRENT_EXPORTS,
                MAX_CONCURRENT_EXPORTS,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(MAX_CONCURRENT_EXPORTS),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "export-worker-" + counter.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 启动队列处理器
        startQueueProcessor();

        LogUtils.info("导出任务队列服务初始化完成，最大并发数：{}，队列容量：{}", MAX_CONCURRENT_EXPORTS, QUEUE_CAPACITY);
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        LogUtils.info("导出任务队列服务已关闭");
    }

    /**
     * 提交导出任务到队列
     *
     * @param taskMessage 任务消息
     * @return 是否提交成功
     */
    public boolean submitTask(ExportTaskMessage taskMessage) {
        try {
            // 检查用户任务数量限制
            exportTaskService.checkUserTaskLimit(
                    taskMessage.getUserId(),
                    ExportConstants.ExportStatus.PREPARED.toString()
            );

            // 将任务序列化并存入Redis队列（持久化）
            String taskJson = objectMapper.writeValueAsString(taskMessage);
            Long queueSize = stringRedisTemplate.opsForList().leftPush(EXPORT_QUEUE_KEY, taskJson);

            // 设置队列过期时间（7天）
            stringRedisTemplate.expire(EXPORT_QUEUE_KEY, Duration.ofDays(7));

            LogUtils.info("导出任务已提交到队列，任务ID：{}，当前队列大小：{}", taskMessage.getTaskId(), queueSize);
            return true;
        } catch (Exception e) {
            LogUtils.error("提交导出任务到队列失败", e);
            throw new GenericException(Translator.get("export_task_submit_failed"));
        }
    }

    /**
     * 注册任务处理器
     *
     * @param exportType  导出类型
     * @param taskHandler 任务处理器
     */
    public void registerTaskHandler(String exportType, Consumer<ExportTaskMessage> taskHandler) {
        taskHandlers.put(exportType, taskHandler);
        LogUtils.info("注册导出任务处理器，类型：{}", exportType);
    }

    /**
     * 启动队列处理器
     */
    private void startQueueProcessor() {
        // 使用虚拟线程启动队列消费器
        Thread.startVirtualThread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 从Redis队列中获取任务（阻塞式）
                    String taskJson = stringRedisTemplate.opsForList().rightPop(EXPORT_QUEUE_KEY, Duration.ofSeconds(5));

                    if (taskJson != null) {
                        ExportTaskMessage taskMessage = objectMapper.readValue(taskJson, ExportTaskMessage.class);

                        // 检查任务是否已被取消
                        if (isTaskCancelled(taskMessage.getTaskId())) {
                            LogUtils.info("任务已被取消，跳过执行，任务ID：{}", taskMessage.getTaskId());
                            continue;
                        }

                        // 将任务放入本地队列等待处理
                        boolean offered = taskQueue.offer(taskMessage, 5, TimeUnit.SECONDS);
                        if (offered) {
                            processTask(taskMessage);
                        } else {
                            // 队列已满，重新放回Redis队列
                            stringRedisTemplate.opsForList().rightPush(EXPORT_QUEUE_KEY, taskJson);
                            LogUtils.warn("本地队列已满，任务重新放回Redis队列，任务ID：{}", taskMessage.getTaskId());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    LogUtils.error("处理导出任务队列异常", e);
                }
            }
        });

        LogUtils.info("导出任务队列处理器已启动");
    }

    /**
     * 处理单个任务
     *
     * @param taskMessage 任务消息
     */
    private void processTask(ExportTaskMessage taskMessage) {
        executor.submit(() -> {
            String taskId = taskMessage.getTaskId();
            try {
                // 标记任务为处理中
                markTaskProcessing(taskId);

                // 初始化进度跟踪
                exportProgressService.initProgress(taskId, taskMessage.getUserId(), null);

                // 注册线程到注册表（支持中断）
                ExportThreadRegistry.register(taskId, Thread.currentThread());

                // 获取任务处理器
                Consumer<ExportTaskMessage> handler = taskHandlers.get(taskMessage.getExportType());
                if (handler == null) {
                    throw new GenericException("未找到导出任务处理器，类型：" + taskMessage.getExportType());
                }

                // 执行任务
                handler.accept(taskMessage);

                // 更新任务状态为成功
                exportTaskService.update(taskId, ExportConstants.ExportStatus.SUCCESS.toString(), taskMessage.getUserId());

                // 标记进度完成
                exportProgressService.completeProgress(taskId);

                // 发送完成通知
                sendTaskNotification(taskMessage, ExportConstants.ExportStatus.SUCCESS);

            } catch (InterruptedException e) {
                LogUtils.info("导出任务被中断，任务ID：{}", taskId);
                exportTaskService.update(taskId, ExportConstants.ExportStatus.STOP.toString(), taskMessage.getUserId());
                exportProgressService.cancelProgress(taskId);
                sendTaskNotification(taskMessage, ExportConstants.ExportStatus.STOP);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LogUtils.error("导出任务执行异常，任务ID：" + taskId, e);
                exportTaskService.update(taskId, ExportConstants.ExportStatus.ERROR.toString(), taskMessage.getUserId());
                exportProgressService.failProgress(taskId, e.getMessage());
                sendTaskNotification(taskMessage, ExportConstants.ExportStatus.ERROR);
            } finally {
                ExportThreadRegistry.remove(taskId);
                removeTaskProcessing(taskId);
                taskQueue.remove(taskMessage);
            }
        });
    }

    /**
     * 检查任务是否已取消
     *
     * @param taskId 任务ID
     * @return 是否已取消
     */
    private boolean isTaskCancelled(String taskId) {
        String status = stringRedisTemplate.opsForValue().get(EXPORT_PROCESSING_KEY + ":" + taskId);
        return ExportConstants.ExportStatus.STOP.name().equals(status);
    }

    /**
     * 标记任务为处理中
     *
     * @param taskId 任务ID
     */
    private void markTaskProcessing(String taskId) {
        stringRedisTemplate.opsForValue().set(
                EXPORT_PROCESSING_KEY + ":" + taskId,
                ExportConstants.ExportStatus.PREPARED.name(),
                Duration.ofHours(1)
        );
    }

    /**
     * 移除处理中标记
     *
     * @param taskId 任务ID
     */
    private void removeTaskProcessing(String taskId) {
        stringRedisTemplate.delete(EXPORT_PROCESSING_KEY + ":" + taskId);
    }

    /**
     * 发送任务状态通知
     *
     * @param taskMessage 任务消息
     * @param status      状态
     */
    private void sendTaskNotification(ExportTaskMessage taskMessage, ExportConstants.ExportStatus status) {
        try {
            // 通过Redis发布订阅发送通知
            String notification = objectMapper.writeValueAsString(
                    new ExportTaskNotification(taskMessage.getTaskId(), status.name(), taskMessage.getUserId())
            );
            messagePublisher.publish(TopicConstants.SSE_TOPIC, notification);
        } catch (Exception e) {
            LogUtils.error("发送导出任务通知失败", e);
        }
    }

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     */
    public void cancelTask(String taskId) {
        // 标记任务为取消状态
        stringRedisTemplate.opsForValue().set(
                EXPORT_PROCESSING_KEY + ":" + taskId,
                ExportConstants.ExportStatus.STOP.name(),
                Duration.ofMinutes(5)
        );

        // 发送中断信号
        messagePublisher.publish(TopicConstants.DOWNLOAD_TOPIC, taskId);

        LogUtils.info("导出任务已标记为取消，任务ID：{}", taskId);
    }

    /**
     * 获取队列状态
     *
     * @return 队列状态信息
     */
    public QueueStatus getQueueStatus() {
        Long redisQueueSize = stringRedisTemplate.opsForList().size(EXPORT_QUEUE_KEY);
        int localQueueSize = taskQueue.size();
        int activeCount = executor.getActiveCount();
        int poolSize = executor.getPoolSize();

        return new QueueStatus(
                redisQueueSize != null ? redisQueueSize.intValue() : 0,
                localQueueSize,
                activeCount,
                poolSize,
                MAX_CONCURRENT_EXPORTS,
                QUEUE_CAPACITY
        );
    }

    /**
     * 队列状态记录
     */
    public record QueueStatus(
            int redisQueueSize,
            int localQueueSize,
            int activeCount,
            int poolSize,
            int maxConcurrent,
            int queueCapacity
    ) {
    }

    /**
     * 任务通知记录
     */
    private record ExportTaskNotification(String taskId, String status, String userId) {
    }
}
