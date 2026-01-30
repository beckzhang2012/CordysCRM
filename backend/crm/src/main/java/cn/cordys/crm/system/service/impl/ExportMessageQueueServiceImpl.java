package cn.cordys.crm.system.service.impl;

import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.request.AsyncExportRequest;
import cn.cordys.crm.system.service.ExportMessageQueueService;
import cn.cordys.crm.system.service.ExportTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis和内存队列的导出任务消息队列服务实现
 */
@Service
public class ExportMessageQueueServiceImpl implements ExportMessageQueueService {
    
    // 高优先级队列
    private static final String HIGH_PRIORITY_QUEUE_KEY = "export:queue:high";
    // 普通优先级队列
    private static final String NORMAL_PRIORITY_QUEUE_KEY = "export:queue:normal";
    // 任务取消集合
    private static final String CANCELLED_TASKS_KEY = "export:cancelled";
    
    // 内存中的优先级队列，用于快速处理
    private final PriorityBlockingQueue<AsyncExportRequest> memoryQueue = new PriorityBlockingQueue<>(100, 
            (r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private ExportTaskService exportTaskService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void initializeQueue() {
        // 启动队列处理线程
        Thread queueProcessor = new Thread(this::processQueue, "export-queue-processor");
        queueProcessor.setDaemon(true);
        queueProcessor.start();
        
        LogUtils.info("导出任务消息队列已初始化");
    }
    
    @Override
    public boolean sendExportTask(AsyncExportRequest request) {
        try {
            // 根据优先级决定发送到哪个队列
            if (request.getPriority() >= 8) {
                return sendHighPriorityExportTask(request);
            } else {
                // 序列化请求对象
                String requestJson = objectMapper.writeValueAsString(request);
                // 添加到普通优先级队列
                redisTemplate.opsForList().rightPush(NORMAL_PRIORITY_QUEUE_KEY, requestJson);
                // 设置过期时间，防止队列无限增长
                redisTemplate.expire(NORMAL_PRIORITY_QUEUE_KEY, 7, TimeUnit.DAYS);
                return true;
            }
        } catch (Exception e) {
            LogUtils.error("发送导出任务到队列失败", e);
            return false;
        }
    }
    
    @Override
    public boolean sendHighPriorityExportTask(AsyncExportRequest request) {
        try {
            // 序列化请求对象
            String requestJson = objectMapper.writeValueAsString(request);
            // 添加到高优先级队列
            redisTemplate.opsForList().rightPush(HIGH_PRIORITY_QUEUE_KEY, requestJson);
            // 设置过期时间
            redisTemplate.expire(HIGH_PRIORITY_QUEUE_KEY, 7, TimeUnit.DAYS);
            return true;
        } catch (Exception e) {
            LogUtils.error("发送高优先级导出任务到队列失败", e);
            return false;
        }
    }
    
    @Override
    public boolean cancelExportTask(String taskId) {
        try {
            // 添加到取消任务集合
            redisTemplate.opsForSet().add(CANCELLED_TASKS_KEY, taskId);
            // 设置过期时间
            redisTemplate.expire(CANCELLED_TASKS_KEY, 1, TimeUnit.DAYS);
            
            // 更新任务状态
            exportTaskService.update(taskId, ExportConstants.ExportStatus.STOP.toString(), null);
            
            return true;
        } catch (Exception e) {
            LogUtils.error("取消导出任务失败: " + taskId, e);
            return false;
        }
    }
    
    @Override
    public long getQueueSize() {
        try {
            long highPrioritySize = redisTemplate.opsForList().size(HIGH_PRIORITY_QUEUE_KEY) != null ? 
                    redisTemplate.opsForList().size(HIGH_PRIORITY_QUEUE_KEY) : 0;
            long normalPrioritySize = redisTemplate.opsForList().size(NORMAL_PRIORITY_QUEUE_KEY) != null ? 
                    redisTemplate.opsForList().size(NORMAL_PRIORITY_QUEUE_KEY) : 0;
            return highPrioritySize + normalPrioritySize + memoryQueue.size();
        } catch (Exception e) {
            LogUtils.error("获取队列大小失败", e);
            return 0;
        }
    }
    
    @Override
    public AsyncExportRequest receiveExportTask() {
        try {
            // 首先检查内存队列
            AsyncExportRequest memoryRequest = memoryQueue.poll();
            if (memoryRequest != null) {
                return memoryRequest;
            }
            
            // 然后检查Redis中的高优先级队列
            Object highPriorityJson = redisTemplate.opsForList().leftPop(HIGH_PRIORITY_QUEUE_KEY);
            if (highPriorityJson != null) {
                return objectMapper.readValue(highPriorityJson.toString(), AsyncExportRequest.class);
            }
            
            // 最后检查Redis中的普通优先级队列
            Object normalPriorityJson = redisTemplate.opsForList().leftPop(NORMAL_PRIORITY_QUEUE_KEY);
            if (normalPriorityJson != null) {
                return objectMapper.readValue(normalPriorityJson.toString(), AsyncExportRequest.class);
            }
            
            return null;
        } catch (Exception e) {
            LogUtils.error("从队列接收导出任务失败", e);
            return null;
        }
    }
    
    /**
     * 检查任务是否已被取消
     */
    public boolean isTaskCancelled(String taskId) {
        try {
            return redisTemplate.opsForSet().isMember(CANCELLED_TASKS_KEY, taskId);
        } catch (Exception e) {
            LogUtils.error("检查任务取消状态失败: " + taskId, e);
            return false;
        }
    }
    
    /**
     * 处理队列中的任务
     */
    private void processQueue() {
        while (true) {
            try {
                // 优先处理高优先级队列
                processHighPriorityQueue();
                // 处理普通优先级队列
                processNormalPriorityQueue();
                // 处理内存队列
                processMemoryQueue();
                
                // 短暂休眠，避免CPU占用过高
                Thread.sleep(100);
            } catch (Exception e) {
                LogUtils.error("处理导出任务队列异常", e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    /**
     * 处理高优先级队列
     */
    private void processHighPriorityQueue() {
        try {
            String requestJson = (String) redisTemplate.opsForList().leftPop(HIGH_PRIORITY_QUEUE_KEY, 1, TimeUnit.SECONDS);
            if (requestJson != null) {
                AsyncExportRequest request = objectMapper.readValue(requestJson, AsyncExportRequest.class);
                if (!isTaskCancelled(request.getTaskId())) {
                    processExportTask(request);
                }
            }
        } catch (Exception e) {
            LogUtils.error("处理高优先级导出任务失败", e);
        }
    }
    
    /**
     * 处理普通优先级队列
     */
    private void processNormalPriorityQueue() {
        try {
            String requestJson = (String) redisTemplate.opsForList().leftPop(NORMAL_PRIORITY_QUEUE_KEY, 1, TimeUnit.SECONDS);
            if (requestJson != null) {
                AsyncExportRequest request = objectMapper.readValue(requestJson, AsyncExportRequest.class);
                if (!isTaskCancelled(request.getTaskId())) {
                    processExportTask(request);
                }
            }
        } catch (Exception e) {
            LogUtils.error("处理普通优先级导出任务失败", e);
        }
    }
    
    /**
     * 处理内存队列
     */
    private void processMemoryQueue() {
        try {
            AsyncExportRequest request = memoryQueue.poll(1, TimeUnit.SECONDS);
            if (request != null && !isTaskCancelled(request.getTaskId())) {
                processExportTask(request);
            }
        } catch (Exception e) {
            LogUtils.error("处理内存队列导出任务失败", e);
        }
    }
    
    /**
     * 检查任务是否已取消
     */
    private boolean isTaskCancelled(String taskId) {
        try {
            return redisTemplate.opsForSet().isMember(CANCELLED_TASKS_KEY, taskId);
        } catch (Exception e) {
            LogUtils.error("检查任务取消状态失败: " + taskId, e);
            return false;
        }
    }
    
    /**
     * 处理导出任务
     */
    private void processExportTask(AsyncExportRequest request) {
        try {
            // 更新任务状态为处理中
            exportTaskService.update(request.getTaskId(), ExportConstants.ExportStatus.PROCESSING.toString(), request.getUserId());
            
            // 这里可以调用具体的导出服务处理任务
            // 实际实现中，可以根据exportType调用不同的导出服务
            LogUtils.info("开始处理导出任务: " + request.getTaskId() + ", 类型: " + request.getExportType());
            
            // 模拟任务处理
            // 在实际实现中，这里应该调用相应的导出服务
            // exportService.processExport(request);
            
            // 更新任务状态为成功
            exportTaskService.update(request.getTaskId(), ExportConstants.ExportStatus.SUCCESS.toString(), request.getUserId());
            
            LogUtils.info("导出任务处理完成: " + request.getTaskId());
        } catch (Exception e) {
            LogUtils.error("处理导出任务失败: " + request.getTaskId(), e);
            // 更新任务状态为失败
            exportTaskService.update(request.getTaskId(), ExportConstants.ExportStatus.ERROR.toString(), request.getUserId());
        }
    }
}