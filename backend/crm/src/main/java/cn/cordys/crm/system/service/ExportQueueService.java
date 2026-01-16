package cn.cordys.crm.system.service;

import cn.cordys.common.redis.MessagePublisher;
import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.request.ExportQueueRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ExportQueueService {

    private static final String EXPORT_QUEUE_KEY = "export:task:queue";
    private static final String EXPORT_PRIORITY_QUEUE_KEY = "export:task:priority:queue";
    private static final String EXPORT_RETRY_QUEUE_KEY = "export:task:retry:queue";
    private static final String EXPORT_TASK_KEY_PREFIX = "export:task:";
    private static final long TASK_TTL_HOURS = 24;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MessagePublisher messagePublisher;

    @Resource
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        LogUtils.info("ExportQueueService initialized");
    }

    public String enqueueTask(ExportQueueRequest request) {
        try {
            String taskId = request.getTaskId();
            String taskKey = EXPORT_TASK_KEY_PREFIX + taskId;

            redisTemplate.opsForValue().set(taskKey, request, TASK_TTL_HOURS, TimeUnit.HOURS);

            if (request.getPriority() != null && request.getPriority() > 0) {
                redisTemplate.opsForList().leftPush(EXPORT_PRIORITY_QUEUE_KEY, taskId);
            } else {
                redisTemplate.opsForList().rightPush(EXPORT_QUEUE_KEY, taskId);
            }

            messagePublisher.publish("export_task_enqueued:" + taskId);
            LogUtils.info("Export task enqueued: " + taskId);
            return taskId;
        } catch (Exception e) {
            LogUtils.error("Failed to enqueue export task", e);
            throw new RuntimeException("Failed to enqueue export task", e);
        }
    }

    public String dequeueTask() {
        try {
            String taskId = (String) redisTemplate.opsForList().leftPop(EXPORT_PRIORITY_QUEUE_KEY, 5, TimeUnit.SECONDS);
            if (taskId == null) {
                taskId = (String) redisTemplate.opsForList().leftPop(EXPORT_QUEUE_KEY, 5, TimeUnit.SECONDS);
            }
            if (taskId == null) {
                taskId = (String) redisTemplate.opsForList().leftPop(EXPORT_RETRY_QUEUE_KEY, 5, TimeUnit.SECONDS);
            }
            return taskId;
        } catch (Exception e) {
            LogUtils.error("Failed to dequeue export task", e);
            return null;
        }
    }

    public ExportQueueRequest getTask(String taskId) {
        try {
            String taskKey = EXPORT_TASK_KEY_PREFIX + taskId;
            return (ExportQueueRequest) redisTemplate.opsForValue().get(taskKey);
        } catch (Exception e) {
            LogUtils.error("Failed to get export task: " + taskId, e);
            return null;
        }
    }

    public void removeTask(String taskId) {
        try {
            String taskKey = EXPORT_TASK_KEY_PREFIX + taskId;
            redisTemplate.delete(taskKey);
            redisTemplate.opsForList().remove(EXPORT_QUEUE_KEY, 1, taskId);
            redisTemplate.opsForList().remove(EXPORT_PRIORITY_QUEUE_KEY, 1, taskId);
            redisTemplate.opsForList().remove(EXPORT_RETRY_QUEUE_KEY, 1, taskId);
        } catch (Exception e) {
            LogUtils.error("Failed to remove export task: " + taskId, e);
        }
    }

    public void retryTask(String taskId) {
        try {
            redisTemplate.opsForList().rightPush(EXPORT_RETRY_QUEUE_KEY, taskId);
            messagePublisher.publish("export_task_retry:" + taskId);
            LogUtils.info("Export task retried: " + taskId);
        } catch (Exception e) {
            LogUtils.error("Failed to retry export task: " + taskId, e);
        }
    }

    public long getQueueSize() {
        try {
            long normalSize = redisTemplate.opsForList().size(EXPORT_QUEUE_KEY) != null ? redisTemplate.opsForList().size(EXPORT_QUEUE_KEY) : 0;
            long prioritySize = redisTemplate.opsForList().size(EXPORT_PRIORITY_QUEUE_KEY) != null ? redisTemplate.opsForList().size(EXPORT_PRIORITY_QUEUE_KEY) : 0;
            long retrySize = redisTemplate.opsForList().size(EXPORT_RETRY_QUEUE_KEY) != null ? redisTemplate.opsForList().size(EXPORT_RETRY_QUEUE_KEY) : 0;
            return normalSize + prioritySize + retrySize;
        } catch (Exception e) {
            LogUtils.error("Failed to get queue size", e);
            return 0;
        }
    }

    public void updateTaskProgress(String taskId, int current, int total) {
        try {
            ExportQueueRequest task = getTask(taskId);
            if (task != null) {
                task.setProgressCurrent(current);
                task.setProgressTotal(total);
                String taskKey = EXPORT_TASK_KEY_PREFIX + taskId;
                redisTemplate.opsForValue().set(taskKey, task, TASK_TTL_HOURS, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            LogUtils.error("Failed to update task progress: " + taskId, e);
        }
    }
}
