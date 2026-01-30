package cn.cordys.crm.system.service;

import cn.cordys.crm.config.ExportQueueConfig;
import cn.cordys.crm.system.dto.ExportTaskMessage;
import cn.cordys.common.util.JSON;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class ExportTaskProducerService {

    @Resource
    private RBlockingQueue<String> exportTaskQueue;

    @Resource
    private RedissonClient redissonClient;

    public void submitExportTask(ExportTaskMessage message) {
        String messageJson = JSON.toJSONString(message);
        exportTaskQueue.offer(messageJson);
    }

    public void cancelExportTask(String taskId) {
        RBucket<Boolean> cancelBucket = redissonClient.getBucket(
                ExportQueueConfig.EXPORT_TASK_CANCEL_KEY + ":" + taskId);
        cancelBucket.set(true, 24, TimeUnit.HOURS);
    }

    public boolean isTaskCancelled(String taskId) {
        RBucket<Boolean> cancelBucket = redissonClient.getBucket(
                ExportQueueConfig.EXPORT_TASK_CANCEL_KEY + ":" + taskId);
        Boolean cancelled = cancelBucket.get();
        return cancelled != null && cancelled;
    }

    public void clearCancelFlag(String taskId) {
        RBucket<Boolean> cancelBucket = redissonClient.getBucket(
                ExportQueueConfig.EXPORT_TASK_CANCEL_KEY + ":" + taskId);
        cancelBucket.delete();
    }

    public void updateTaskProgress(String taskId, int progress, int total) {
        RBucket<String> progressBucket = redissonClient.getBucket(
                ExportQueueConfig.EXPORT_TASK_PROGRESS_KEY + ":" + taskId);
        String progressInfo = progress + "/" + total;
        progressBucket.set(progressInfo, 24, TimeUnit.HOURS);
    }

    public String getTaskProgress(String taskId) {
        RBucket<String> progressBucket = redissonClient.getBucket(
                ExportQueueConfig.EXPORT_TASK_PROGRESS_KEY + ":" + taskId);
        return progressBucket.get();
    }

    public void clearTaskProgress(String taskId) {
        RBucket<String> progressBucket = redissonClient.getBucket(
                ExportQueueConfig.EXPORT_TASK_PROGRESS_KEY + ":" + taskId);
        progressBucket.delete();
    }
}
