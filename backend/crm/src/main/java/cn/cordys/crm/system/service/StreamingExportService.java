package cn.cordys.crm.system.service;

import cn.cordys.crm.system.dto.request.AsyncExportRequest;

import java.io.OutputStream;

/**
 * 流式导出服务接口
 * 支持大数据量的流式导出，避免内存溢出
 */
public interface StreamingExportService {
    
    /**
     * 流式导出数据到输出流
     * 
     * @param request 导出请求
     * @param outputStream 输出流
     * @param progressCallback 进度回调
     * @throws Exception 导出异常
     */
    void streamExport(AsyncExportRequest request, OutputStream outputStream, ProgressCallback progressCallback) throws Exception;
    
    /**
     * 流式导出数据到文件
     * 
     * @param request 导出请求
     * @param filePath 文件路径
     * @param progressCallback 进度回调
     * @throws Exception 导出异常
     */
    void streamExportToFile(AsyncExportRequest request, String filePath, ProgressCallback progressCallback) throws Exception;
    
    /**
     * 分批查询数据
     * 
     * @param request 导出请求
     * @param batchIndex 批次索引(从0开始)
     * @param batchSize 批次大小
     * @return 数据批次
     * @throws Exception 查询异常
     */
    Object queryDataBatch(AsyncExportRequest request, int batchIndex, int batchSize) throws Exception;
    
    /**
     * 获取总数据量
     * 
     * @param request 导出请求
     * @return 总数据量
     * @throws Exception 查询异常
     */
    long getTotalCount(AsyncExportRequest request) throws Exception;
    
    /**
     * 进度回调接口
     */
    interface ProgressCallback {
        /**
         * 更新进度
         * 
         * @param taskId 任务ID
         * @param processed 已处理数量
         * @param total 总数量
         * @param percentage 百分比
         */
        void onProgress(String taskId, long processed, long total, double percentage);
        
        /**
         * 任务完成
         * 
         * @param taskId 任务ID
         * @param success 是否成功
         * @param message 消息
         */
        void onComplete(String taskId, boolean success, String message);
    }
}