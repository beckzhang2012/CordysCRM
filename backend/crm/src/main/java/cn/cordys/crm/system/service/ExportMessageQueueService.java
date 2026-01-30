package cn.cordys.crm.system.service;

import cn.cordys.crm.system.dto.request.AsyncExportRequest;
import org.springframework.stereotype.Service;

/**
 * 导出任务消息队列服务
 * 负责将导出任务发送到消息队列，实现异步处理
 */
@Service
public interface ExportMessageQueueService {
    
    /**
     * 发送导出任务到消息队列
     * 
     * @param request 导出请求
     * @return 是否发送成功
     */
    boolean sendExportTask(AsyncExportRequest request);
    
    /**
     * 发送高优先级导出任务
     * 
     * @param request 导出请求
     * @return 是否发送成功
     */
    boolean sendHighPriorityExportTask(AsyncExportRequest request);
    
    /**
     * 取消导出任务
     * 
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancelExportTask(String taskId);
    
    /**
     * 获取队列中的任务数量
     * 
     * @return 任务数量
     */
    long getQueueSize();
    
    /**
     * 初始化消息队列
     */
    void initializeQueue();
    
    /**
     * 从队列中接收导出任务
     * 
     * @return 导出请求，如果队列为空则返回null
     */
    AsyncExportRequest receiveExportTask();
}