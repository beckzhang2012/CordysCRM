package cn.cordys.crm.system.service;

import cn.cordys.common.constants.TopicConstants;
import cn.cordys.common.redis.MessagePublisher;
import cn.cordys.common.util.JSON;
import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.notice.sse.SseService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导出进度服务
 * 管理导出任务的进度跟踪和实时通知
 */
@Service
public class ExportProgressService {

    /**
     * 导出进度Redis Key前缀
     */
    private static final String EXPORT_PROGRESS_KEY = "export:progress:";

    /**
     * 导出进度过期时间（小时）
     */
    private static final int PROGRESS_EXPIRE_HOURS = 24;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SseService sseService;

    @Resource
    private MessagePublisher messagePublisher;

    /**
     * 本地进度缓存（减少Redis访问）
     */
    private final ConcurrentHashMap<String, ExportProgress> localProgressCache = new ConcurrentHashMap<>();

    /**
     * 初始化导出任务进度
     *
     * @param taskId     任务ID
     * @param userId     用户ID
     * @param totalCount 预计总记录数（如果已知）
     */
    public void initProgress(String taskId, String userId, Integer totalCount) {
        ExportProgress progress = new ExportProgress();
        progress.setTaskId(taskId);
        progress.setUserId(userId);
        progress.setStatus(ExportConstants.ExportStatus.PREPARED.name());
        progress.setProcessedCount(0);
        progress.setTotalCount(totalCount != null ? totalCount : -1);
        progress.setStartTime(System.currentTimeMillis());
        progress.setProgressPercentage(0);

        // 保存到Redis
        saveProgress(progress);

        // 本地缓存
        localProgressCache.put(taskId, progress);

        // 发送初始化通知
        sendProgressNotification(progress);

        LogUtils.info("导出任务进度初始化，任务ID：{}，用户ID：{}", taskId, userId);
    }

    /**
     * 更新处理进度
     *
     * @param taskId        任务ID
     * @param processedCount 已处理数量
     */
    public void updateProgress(String taskId, int processedCount) {
        ExportProgress progress = getProgress(taskId);
        if (progress == null) {
            return;
        }

        progress.setProcessedCount(processedCount);

        // 计算进度百分比
        if (progress.getTotalCount() > 0) {
            int percentage = (int) ((processedCount * 100.0) / progress.getTotalCount());
            progress.setProgressPercentage(Math.min(percentage, 99)); // 最大99%，完成时设为100%
        }

        // 每处理1000条更新一次Redis（减少Redis压力）
        if (processedCount % 1000 == 0) {
            saveProgress(progress);
            sendProgressNotification(progress);
        }

        // 更新本地缓存
        localProgressCache.put(taskId, progress);
    }

    /**
     * 标记任务完成
     *
     * @param taskId 任务ID
     */
    public void completeProgress(String taskId) {
        ExportProgress progress = getProgress(taskId);
        if (progress == null) {
            return;
        }

        progress.setStatus(ExportConstants.ExportStatus.SUCCESS.name());
        progress.setProgressPercentage(100);
        progress.setEndTime(System.currentTimeMillis());

        saveProgress(progress);
        sendProgressNotification(progress);
        localProgressCache.remove(taskId);

        LogUtils.info("导出任务完成，任务ID：{}，总处理：{}条，耗时：{}ms",
                taskId, progress.getProcessedCount(),
                progress.getEndTime() - progress.getStartTime());
    }

    /**
     * 标记任务失败
     *
     * @param taskId    任务ID
     * @param errorMsg  错误信息
     */
    public void failProgress(String taskId, String errorMsg) {
        ExportProgress progress = getProgress(taskId);
        if (progress == null) {
            return;
        }

        progress.setStatus(ExportConstants.ExportStatus.ERROR.name());
        progress.setErrorMessage(errorMsg);
        progress.setEndTime(System.currentTimeMillis());

        saveProgress(progress);
        sendProgressNotification(progress);
        localProgressCache.remove(taskId);

        LogUtils.error("导出任务失败，任务ID：{}，错误：{}", taskId, errorMsg);
    }

    /**
     * 标记任务取消
     *
     * @param taskId 任务ID
     */
    public void cancelProgress(String taskId) {
        ExportProgress progress = getProgress(taskId);
        if (progress == null) {
            return;
        }

        progress.setStatus(ExportConstants.ExportStatus.STOP.name());
        progress.setEndTime(System.currentTimeMillis());

        saveProgress(progress);
        sendProgressNotification(progress);
        localProgressCache.remove(taskId);

        LogUtils.info("导出任务取消，任务ID：{}", taskId);
    }

    /**
     * 获取任务进度
     *
     * @param taskId 任务ID
     * @return 进度信息
     */
    public ExportProgress getProgress(String taskId) {
        // 先从本地缓存获取
        ExportProgress progress = localProgressCache.get(taskId);
        if (progress != null) {
            return progress;
        }

        // 从Redis获取
        String progressJson = stringRedisTemplate.opsForValue().get(EXPORT_PROGRESS_KEY + taskId);
        if (progressJson != null) {
            return JSON.parseObject(progressJson, ExportProgress.class);
        }

        return null;
    }

    /**
     * 保存进度到Redis
     *
     * @param progress 进度信息
     */
    private void saveProgress(ExportProgress progress) {
        try {
            String progressJson = JSON.toJSONString(progress);
            stringRedisTemplate.opsForValue().set(
                    EXPORT_PROGRESS_KEY + progress.getTaskId(),
                    progressJson,
                    Duration.ofHours(PROGRESS_EXPIRE_HOURS)
            );
        } catch (Exception e) {
            LogUtils.error("保存导出进度到Redis失败", e);
        }
    }

    /**
     * 发送进度通知
     *
     * @param progress 进度信息
     */
    private void sendProgressNotification(ExportProgress progress) {
        try {
            // 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "EXPORT_PROGRESS");
            notification.put("taskId", progress.getTaskId());
            notification.put("status", progress.getStatus());
            notification.put("processedCount", progress.getProcessedCount());
            notification.put("totalCount", progress.getTotalCount());
            notification.put("progressPercentage", progress.getProgressPercentage());
            notification.put("errorMessage", progress.getErrorMessage());

            // 通过SSE发送给用户
            sseService.sendToUser(progress.getUserId(), notification);

            // 同时通过Redis发布订阅（支持集群环境）
            messagePublisher.publish(TopicConstants.SSE_TOPIC, JSON.toJSONString(notification));

        } catch (Exception e) {
            LogUtils.error("发送导出进度通知失败", e);
        }
    }

    /**
     * 清理过期进度
     *
     * @param taskId 任务ID
     */
    public void clearProgress(String taskId) {
        localProgressCache.remove(taskId);
        stringRedisTemplate.delete(EXPORT_PROGRESS_KEY + taskId);
    }

    /**
     * 导出进度信息
     */
    public static class ExportProgress {
        private String taskId;
        private String userId;
        private String status;
        private int processedCount;
        private int totalCount;
        private int progressPercentage;
        private String errorMessage;
        private long startTime;
        private long endTime;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public void setProcessedCount(int processedCount) {
            this.processedCount = processedCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getProgressPercentage() {
            return progressPercentage;
        }

        public void setProgressPercentage(int progressPercentage) {
            this.progressPercentage = progressPercentage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
    }
}
