package cn.cordys.crm.system.queue;

import cn.cordys.common.constants.TopicConstants;
import cn.cordys.common.util.JSON;
import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.queue.dto.ExportTaskMessage;
import cn.cordys.crm.system.service.ExportTaskService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class ExportTaskQueueManager {

    private static final String EXPORT_TASK_QUEUE_KEY = "export:task:queue";
    private static final String EXPORT_TASK_PROGRESS_KEY = "export:task:progress:";
    private static final String EXPORT_TASK_LOCK_KEY = "export:task:lock:";
    private static final long LOCK_EXPIRE_SECONDS = 300;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ExportTaskService exportTaskService;

    public void enqueueTask(ExportTaskMessage message) {
        try {
            String messageJson = JSON.toJSONString(message);
            stringRedisTemplate.opsForList().rightPush(EXPORT_TASK_QUEUE_KEY, messageJson);
            LogUtils.info("导出任务已入队: taskId={}, type={}", message.getTaskId(), message.getExportType());
        } catch (Exception e) {
            LogUtils.error("入队导出任务失败: taskId=" + message.getTaskId(), e);
            throw new RuntimeException("入队导出任务失败", e);
        }
    }

    public ExportTaskMessage dequeueTask() {
        try {
            String messageJson = stringRedisTemplate.opsForList().leftPop(EXPORT_TASK_QUEUE_KEY);
            if (messageJson == null) {
                return null;
            }
            return JSON.parseObject(messageJson, ExportTaskMessage.class);
        } catch (Exception e) {
            LogUtils.error("出队导出任务失败", e);
            return null;
        }
    }

    public ExportTaskMessage blockingDequeueTask(long timeoutSeconds) {
        try {
            var result = stringRedisTemplate.opsForList()
                    .leftPop(EXPORT_TASK_QUEUE_KEY, timeoutSeconds, TimeUnit.SECONDS);
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, ExportTaskMessage.class);
        } catch (Exception e) {
            LogUtils.error("阻塞出队导出任务失败", e);
            return null;
        }
    }

    public long getQueueSize() {
        try {
            Long size = stringRedisTemplate.opsForList().size(EXPORT_TASK_QUEUE_KEY);
            return size != null ? size : 0;
        } catch (Exception e) {
            LogUtils.error("获取队列大小失败", e);
            return 0;
        }
    }

    public void updateTaskProgress(String taskId, int currentPage, int totalPages) {
        try {
            String progressKey = EXPORT_TASK_PROGRESS_KEY + taskId;
            String progressJson = JSON.toJSONString(new TaskProgress(currentPage, totalPages));
            stringRedisTemplate.opsForValue().set(progressKey, progressJson, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            LogUtils.error("更新任务进度失败: taskId=" + taskId, e);
        }
    }

    public TaskProgress getTaskProgress(String taskId) {
        try {
            String progressKey = EXPORT_TASK_PROGRESS_KEY + taskId;
            String progressJson = stringRedisTemplate.opsForValue().get(progressKey);
            if (progressJson == null) {
                return null;
            }
            return JSON.parseObject(progressJson, TaskProgress.class);
        } catch (Exception e) {
            LogUtils.error("获取任务进度失败: taskId=" + taskId, e);
            return null;
        }
    }

    public boolean tryLockTask(String taskId) {
        try {
            String lockKey = EXPORT_TASK_LOCK_KEY + taskId;
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "locked", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            LogUtils.error("尝试锁定任务失败: taskId=" + taskId, e);
            return false;
        }
    }

    public void unlockTask(String taskId) {
        try {
            String lockKey = EXPORT_TASK_LOCK_KEY + taskId;
            stringRedisTemplate.delete(lockKey);
        } catch (Exception e) {
            LogUtils.error("解锁任务失败: taskId=" + taskId, e);
        }
    }

    public void clearTaskProgress(String taskId) {
        try {
            String progressKey = EXPORT_TASK_PROGRESS_KEY + taskId;
            stringRedisTemplate.delete(progressKey);
        } catch (Exception e) {
            LogUtils.error("清除任务进度失败: taskId=" + taskId, e);
        }
    }

    public static class TaskProgress {
        private int currentPage;
        private int totalPages;
        private long timestamp;

        public TaskProgress() {
            this.timestamp = System.currentTimeMillis();
        }

        public TaskProgress(int currentPage, int totalPages) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.timestamp = System.currentTimeMillis();
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getProgressPercent() {
            if (totalPages <= 0) {
                return 0;
            }
            return (int) ((currentPage * 100.0) / totalPages);
        }
    }
}
