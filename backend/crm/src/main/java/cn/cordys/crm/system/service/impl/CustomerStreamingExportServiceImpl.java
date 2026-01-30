package cn.cordys.crm.system.service.impl;

import cn.cordys.common.util.LogUtils;
import cn.cordys.common.util.SubListUtils;
import cn.cordys.crm.customer.dto.request.CustomerExportRequest;
import cn.cordys.crm.customer.dto.response.CustomerListResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerMapper;
import cn.cordys.crm.customer.service.CustomerService;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.request.AsyncExportRequest;
import cn.cordys.crm.system.service.ExportTaskService;
import cn.cordys.crm.system.service.StreamingExportService;
import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户数据流式导出服务实现
 * 支持大数据量的流式导出，避免内存溢出
 */
@Service("customerStreamingExportService")
public class CustomerStreamingExportServiceImpl implements StreamingExportService {
    
    @Resource
    private ExtCustomerMapper extCustomerMapper;
    
    @Resource
    private CustomerService customerService;
    
    @Resource
    private ExportTaskService exportTaskService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void streamExport(AsyncExportRequest request, OutputStream outputStream, ProgressCallback progressCallback) throws Exception {
        try {
            // 解析导出参数
            CustomerExportRequest exportRequest = parseExportRequest(request.getExportParams());
            
            // 获取表头信息
            List<List<String>> headList = exportRequest.getHeadList().stream()
                    .map(head -> List.of(head.getTitle()))
                    .toList();
            
            // 获取总数据量
            long totalCount = getTotalCount(request);
            progressCallback.onProgress(request.getTaskId(), 0, totalCount, 0.0);
            
            // 创建Excel写入器
            try (ExcelWriter writer = EasyExcel.write(outputStream)
                    .head(headList)
                    .excelType(ExcelTypeEnum.XLSX)
                    .build()) {
                
                WriteSheet sheet = EasyExcel.writerSheet("导出数据").build();
                
                // 分批查询和写入数据
                AtomicLong processedCount = new AtomicLong(0);
                int batchSize = request.getBatchSize() != null ? request.getBatchSize() : 1000;
                
                // 使用流式处理，避免一次性加载所有数据
                int currentPage = 1;
                boolean hasMoreData = true;
                
                while (hasMoreData) {
                    // 检查任务是否已取消
                    if (isTaskCancelled(request.getTaskId())) {
                        progressCallback.onComplete(request.getTaskId(), false, "任务已取消");
                        return;
                    }
                    
                    // 分页查询数据
                    PageHelper.startPage(currentPage, batchSize);
                    List<CustomerListResponse> rawData = extCustomerMapper.list(
                            exportRequest, 
                            request.getOrganizationId(), 
                            request.getUserId(), 
                            null // 部门权限数据需要从请求中解析
                    );
                    
                    if (rawData == null || rawData.isEmpty()) {
                        hasMoreData = false;
                        break;
                    }
                    
                    // 处理数据
                    List<List<Object>> processedData = processCustomerData(rawData, request.getOrganizationId());
                    
                    // 写入Excel
                    writer.write(processedData, sheet);
                    
                    // 更新进度
                    long currentProcessed = processedCount.addAndGet(processedData.size());
                    double percentage = (double) currentProcessed / totalCount * 100;
                    progressCallback.onProgress(request.getTaskId(), currentProcessed, totalCount, percentage);
                    
                    // 检查是否还有更多数据
                    if (rawData.size() < batchSize) {
                        hasMoreData = false;
                    } else {
                        currentPage++;
                    }
                    
                    // 刷新输出流，确保数据及时写入
                    outputStream.flush();
                }
            }
            
            progressCallback.onComplete(request.getTaskId(), true, "导出完成");
        } catch (Exception e) {
            LogUtils.error("流式导出客户数据失败", e);
            progressCallback.onComplete(request.getTaskId(), false, "导出失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void streamExportToFile(AsyncExportRequest request, String filePath, ProgressCallback progressCallback) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            streamExport(request, fos, progressCallback);
        }
    }
    
    @Override
    public Object queryDataBatch(AsyncExportRequest request, int batchIndex, int batchSize) throws Exception {
        try {
            // 解析导出参数
            CustomerExportRequest exportRequest = parseExportRequest(request.getExportParams());
            
            // 分页查询数据
            PageHelper.startPage(batchIndex + 1, batchSize);
            List<CustomerListResponse> rawData = extCustomerMapper.list(
                    exportRequest, 
                    request.getOrganizationId(), 
                    request.getUserId(), 
                    null // 部门权限数据需要从请求中解析
            );
            
            // 处理数据
            return processCustomerData(rawData, request.getOrganizationId());
        } catch (Exception e) {
            LogUtils.error("查询客户数据批次失败", e);
            throw e;
        }
    }
    
    @Override
    public long getTotalCount(AsyncExportRequest request) throws Exception {
        try {
            // 解析导出参数
            CustomerExportRequest exportRequest = parseExportRequest(request.getExportParams());
            
            // 查询总数量
            return extCustomerMapper.countList(exportRequest, request.getOrganizationId(), request.getUserId(), null);
        } catch (Exception e) {
            LogUtils.error("获取客户数据总量失败", e);
            throw e;
        }
    }
    
    /**
     * 解析导出请求参数
     */
    private CustomerExportRequest parseExportRequest(String exportParams) throws Exception {
        if (exportParams == null || exportParams.isEmpty()) {
            return new CustomerExportRequest();
        }
        
        try {
            return objectMapper.readValue(exportParams, CustomerExportRequest.class);
        } catch (Exception e) {
            LogUtils.error("解析导出请求参数失败", e);
            throw new Exception("导出参数格式错误");
        }
    }
    
    /**
     * 处理客户数据
     */
    private List<List<Object>> processCustomerData(List<CustomerListResponse> rawData, String orgId) {
        if (rawData == null || rawData.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 构建数据
        List<CustomerListResponse> dataList = customerService.buildListData(rawData, orgId);
        
        // 转换为导出格式
        List<List<Object>> result = new ArrayList<>(dataList.size());
        for (CustomerListResponse data : dataList) {
            // 这里需要根据实际的表头信息来构建数据行
            // 简化实现，实际应该根据表头信息提取对应字段
            List<Object> row = new ArrayList<>();
            // 示例：添加几个字段
            row.add(data.getId());
            row.add(data.getName());
            row.add(data.getPhone());
            row.add(data.getEmail());
            // 根据实际表头信息添加更多字段...
            
            result.add(row);
        }
        
        return result;
    }
    
    /**
     * 检查任务是否已取消
     */
    private boolean isTaskCancelled(String taskId) {
        try {
            ExportTask task = exportTaskService.getById(taskId);
            return task != null && ExportConstants.ExportStatus.STOP.toString().equals(task.getStatus());
        } catch (Exception e) {
            LogUtils.error("检查任务取消状态失败: " + taskId, e);
            return false;
        }
    }
}