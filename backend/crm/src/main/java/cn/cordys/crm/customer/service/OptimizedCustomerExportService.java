package cn.cordys.crm.customer.service;

import cn.cordys.aspectj.constants.LogModule;
import cn.cordys.common.constants.FormKey;
import cn.cordys.common.dto.DeptDataPermissionDTO;
import cn.cordys.common.dto.ExportSelectRequest;
import cn.cordys.common.service.BaseExportService;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.customer.dto.request.CustomerExportRequest;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.request.AsyncExportRequest;
import cn.cordys.crm.system.service.ExportMessageQueueService;
import cn.cordys.crm.system.service.ExportTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * 优化后的客户导出服务
 * 使用消息队列和流式处理，避免内存溢出和阻塞主线程
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OptimizedCustomerExportService extends BaseExportService {

    @Resource
    private CustomerService customerService;
    @Resource
    private ExportTaskService exportTaskService;
    @Resource
    private ExportMessageQueueService exportMessageQueueService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 异步导出客户数据
     * 
     * @param userId 用户ID
     * @param request 导出请求
     * @param orgId 组织ID
     * @param deptDataPermission 部门数据权限
     * @param locale 本地化
     * @return 导出任务ID
     */
    public String asyncExport(String userId, CustomerExportRequest request, String orgId, DeptDataPermissionDTO deptDataPermission, Locale locale) {
        checkFileName(request.getFileName());
        // 用户导出数量限制
        exportTaskService.checkUserTaskLimit(userId, ExportConstants.ExportStatus.PREPARED.toString());

        String fileId = IDGenerator.nextStr();
        ExportTask exportTask = exportTaskService.saveTask(orgId, fileId, userId, ExportConstants.ExportType.CUSTOMER.toString(), request.getFileName());

        // 构建异步导出请求
        AsyncExportRequest asyncRequest = new AsyncExportRequest();
        asyncRequest.setTaskId(exportTask.getId());
        asyncRequest.setExportType(ExportConstants.ExportType.CUSTOMER.toString());
        asyncRequest.setOrganizationId(orgId);
        asyncRequest.setUserId(userId);
        asyncRequest.setFileName(request.getFileName());
        asyncRequest.setFileId(fileId);
        
        try {
            // 将导出参数序列化为JSON
            String exportParams = objectMapper.writeValueAsString(request);
            asyncRequest.setExportParams(exportParams);
            
            // 设置优先级和批次大小
            asyncRequest.setPriority(5); // 默认优先级
            asyncRequest.setBatchSize(1000); // 默认批次大小
            asyncRequest.setUseStream(true); // 使用流式处理
            
            // 发送到消息队列
            boolean success = exportMessageQueueService.sendExportTask(asyncRequest);
            if (!success) {
                throw new RuntimeException("发送导出任务到消息队列失败");
            }
            
            LogUtils.info("客户导出任务已提交到消息队列: " + exportTask.getId());
        } catch (Exception e) {
            LogUtils.error("创建异步导出任务失败", e);
            // 更新任务状态为失败
            exportTaskService.update(exportTask.getId(), ExportConstants.ExportStatus.ERROR.toString(), userId);
            throw new RuntimeException("创建异步导出任务失败", e);
        }

        return exportTask.getId();
    }

    /**
     * 异步导出选中的客户数据
     * 
     * @param userId 用户ID
     * @param request 导出选择请求
     * @param orgId 组织ID
     * @param locale 本地化
     * @return 导出任务ID
     */
    public String asyncExportSelect(String userId, ExportSelectRequest request, String orgId, Locale locale) {
        checkFileName(request.getFileName());
        // 用户导出数量限制
        exportTaskService.checkUserTaskLimit(userId, ExportConstants.ExportStatus.PREPARED.toString());

        String fileId = IDGenerator.nextStr();
        ExportTask exportTask = exportTaskService.saveTask(orgId, fileId, userId, ExportConstants.ExportType.CUSTOMER.toString(), request.getFileName());

        // 构建异步导出请求
        AsyncExportRequest asyncRequest = new AsyncExportRequest();
        asyncRequest.setTaskId(exportTask.getId());
        asyncRequest.setExportType(ExportConstants.ExportType.CUSTOMER.toString());
        asyncRequest.setOrganizationId(orgId);
        asyncRequest.setUserId(userId);
        asyncRequest.setFileName(request.getFileName());
        asyncRequest.setFileId(fileId);
        
        try {
            // 将导出参数序列化为JSON
            String exportParams = objectMapper.writeValueAsString(request);
            asyncRequest.setExportParams(exportParams);
            
            // 设置优先级和批次大小
            asyncRequest.setPriority(5); // 默认优先级
            asyncRequest.setBatchSize(1000); // 默认批次大小
            asyncRequest.setUseStream(true); // 使用流式处理
            
            // 发送到消息队列
            boolean success = exportMessageQueueService.sendExportTask(asyncRequest);
            if (!success) {
                throw new RuntimeException("发送导出任务到消息队列失败");
            }
            
            LogUtils.info("客户选择导出任务已提交到消息队列: " + exportTask.getId());
        } catch (Exception e) {
            LogUtils.error("创建异步选择导出任务失败", e);
            // 更新任务状态为失败
            exportTaskService.update(exportTask.getId(), ExportConstants.ExportStatus.ERROR.toString(), userId);
            throw new RuntimeException("创建异步选择导出任务失败", e);
        }

        return exportTask.getId();
    }
    
    /**
     * 高优先级异步导出客户数据
     * 用于重要数据的快速导出
     * 
     * @param userId 用户ID
     * @param request 导出请求
     * @param orgId 组织ID
     * @param deptDataPermission 部门数据权限
     * @param locale 本地化
     * @return 导出任务ID
     */
    public String highPriorityAsyncExport(String userId, CustomerExportRequest request, String orgId, DeptDataPermissionDTO deptDataPermission, Locale locale) {
        checkFileName(request.getFileName());
        // 用户导出数量限制
        exportTaskService.checkUserTaskLimit(userId, ExportConstants.ExportStatus.PREPARED.toString());

        String fileId = IDGenerator.nextStr();
        ExportTask exportTask = exportTaskService.saveTask(orgId, fileId, userId, ExportConstants.ExportType.CUSTOMER.toString(), request.getFileName());

        // 构建异步导出请求
        AsyncExportRequest asyncRequest = new AsyncExportRequest();
        asyncRequest.setTaskId(exportTask.getId());
        asyncRequest.setExportType(ExportConstants.ExportType.CUSTOMER.toString());
        asyncRequest.setOrganizationId(orgId);
        asyncRequest.setUserId(userId);
        asyncRequest.setFileName(request.getFileName());
        asyncRequest.setFileId(fileId);
        
        try {
            // 将导出参数序列化为JSON
            String exportParams = objectMapper.writeValueAsString(request);
            asyncRequest.setExportParams(exportParams);
            
            // 设置高优先级和批次大小
            asyncRequest.setPriority(9); // 高优先级
            asyncRequest.setBatchSize(500); // 较小的批次大小，减少内存使用
            asyncRequest.setUseStream(true); // 使用流式处理
            
            // 发送到高优先级消息队列
            boolean success = exportMessageQueueService.sendHighPriorityExportTask(asyncRequest);
            if (!success) {
                throw new RuntimeException("发送高优先级导出任务到消息队列失败");
            }
            
            LogUtils.info("高优先级客户导出任务已提交到消息队列: " + exportTask.getId());
        } catch (Exception e) {
            LogUtils.error("创建高优先级异步导出任务失败", e);
            // 更新任务状态为失败
            exportTaskService.update(exportTask.getId(), ExportConstants.ExportStatus.ERROR.toString(), userId);
            throw new RuntimeException("创建高优先级异步导出任务失败", e);
        }

        return exportTask.getId();
    }
}