package cn.cordys.crm.customer.service;

import cn.cordys.aspectj.constants.LogModule;
import cn.cordys.common.constants.FormKey;
import cn.cordys.common.dto.DeptDataPermissionDTO;
import cn.cordys.common.dto.ExportHeadDTO;
import cn.cordys.common.dto.ExportSelectRequest;
import cn.cordys.common.service.StreamingExportService;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.customer.dto.request.CustomerExportRequest;
import cn.cordys.crm.customer.dto.response.CustomerListResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerMapper;
import cn.cordys.crm.customer.utils.PoolCustomerFieldUtils;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.dto.ExportTaskMessage;
import cn.cordys.crm.system.dto.field.base.BaseField;
import cn.cordys.crm.system.service.ExportTaskQueueService;
import cn.cordys.crm.system.service.ExportTaskService;
import cn.cordys.crm.system.service.ModuleFormService;
import cn.cordys.registry.ExportThreadRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户导出队列服务
 * 基于消息队列的异步导出实现，支持流式处理大数据量
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerExportQueueService {

    @Resource
    private ExportTaskQueueService exportTaskQueueService;

    @Resource
    private ExportTaskService exportTaskService;

    @Resource
    private StreamingExportService streamingExportService;

    @Resource
    private CustomerService customerService;

    @Resource
    private ExtCustomerMapper extCustomerMapper;

    @Resource
    private ModuleFormService moduleFormService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 导出类型标识
     */
    private static final String EXPORT_TYPE = ExportConstants.ExportType.CUSTOMER.toString();

    /**
     * 初始化时注册任务处理器
     */
    @PostConstruct
    public void init() {
        exportTaskQueueService.registerTaskHandler(EXPORT_TYPE, this::handleExportTask);
        LogUtils.info("客户导出任务处理器已注册");
    }

    /**
     * 提交客户导出任务到队列
     *
     * @param userId             用户ID
     * @param request            导出请求
     * @param orgId              组织ID
     * @param deptDataPermission 部门数据权限
     * @param locale             语言环境
     * @return 任务ID
     */
    public String submitExportTask(String userId, CustomerExportRequest request,
                                   String orgId, DeptDataPermissionDTO deptDataPermission, Locale locale) {
        // 生成文件ID
        String fileId = IDGenerator.nextStr();

        // 创建导出任务
        ExportTask exportTask = exportTaskService.saveTask(
                orgId, fileId, userId, EXPORT_TYPE, request.getFileName()
        );

        try {
            // 构建任务消息
            ExportTaskMessage taskMessage = ExportTaskMessage.builder()
                    .taskId(exportTask.getId())
                    .exportType(EXPORT_TYPE)
                    .userId(userId)
                    .orgId(orgId)
                    .fileName(request.getFileName())
                    .fileId(fileId)
                    .locale(locale)
                    .logModule(LogModule.CUSTOMER_INDEX)
                    .exportParams(objectMapper.writeValueAsString(request))
                    .extraParams(Map.of(
                            "deptDataPermission", deptDataPermission,
                            "headList", request.getHeadList()
                    ))
                    .createTime(System.currentTimeMillis())
                    .priority(0)
                    .isSelectExport(false)
                    .build();

            // 提交到队列
            exportTaskQueueService.submitTask(taskMessage);

            LogUtils.info("客户导出任务已提交到队列，任务ID：{}，用户ID：{}", exportTask.getId(), userId);
            return exportTask.getId();

        } catch (Exception e) {
            LogUtils.error("提交客户导出任务失败", e);
            exportTaskService.update(exportTask.getId(), ExportConstants.ExportStatus.ERROR.toString(), userId);
            throw new RuntimeException("提交导出任务失败", e);
        }
    }

    /**
     * 提交选中客户导出任务到队列
     *
     * @param userId 用户ID
     * @param request 选中导出请求
     * @param orgId 组织ID
     * @param locale 语言环境
     * @return 任务ID
     */
    public String submitSelectExportTask(String userId, ExportSelectRequest request,
                                         String orgId, Locale locale) {
        // 生成文件ID
        String fileId = IDGenerator.nextStr();

        // 创建导出任务
        ExportTask exportTask = exportTaskService.saveTask(
                orgId, fileId, userId, EXPORT_TYPE, request.getFileName()
        );

        try {
            // 构建任务消息
            ExportTaskMessage taskMessage = ExportTaskMessage.builder()
                    .taskId(exportTask.getId())
                    .exportType(EXPORT_TYPE)
                    .userId(userId)
                    .orgId(orgId)
                    .fileName(request.getFileName())
                    .fileId(fileId)
                    .locale(locale)
                    .logModule(LogModule.CUSTOMER_INDEX)
                    .exportParams(objectMapper.writeValueAsString(request))
                    .extraParams(Map.of("headList", request.getHeadList()))
                    .createTime(System.currentTimeMillis())
                    .priority(0)
                    .isSelectExport(true)
                    .selectIds(objectMapper.writeValueAsString(request.getIds()))
                    .build();

            // 提交到队列
            exportTaskQueueService.submitTask(taskMessage);

            LogUtils.info("客户选中导出任务已提交到队列，任务ID：{}，用户ID：{}，选中数量：{}",
                    exportTask.getId(), userId, request.getIds().size());
            return exportTask.getId();

        } catch (Exception e) {
            LogUtils.error("提交客户选中导出任务失败", e);
            exportTaskService.update(exportTask.getId(), ExportConstants.ExportStatus.ERROR.toString(), userId);
            throw new RuntimeException("提交导出任务失败", e);
        }
    }

    /**
     * 处理导出任务
     *
     * @param taskMessage 任务消息
     */
    private void handleExportTask(ExportTaskMessage taskMessage) {
        LocaleContextHolder.setLocale(taskMessage.getLocale());
        ExportThreadRegistry.register(taskMessage.getTaskId(), Thread.currentThread());

        try {
            ExportTask exportTask = new ExportTask();
            exportTask.setId(taskMessage.getTaskId());
            exportTask.setFileId(taskMessage.getFileId());
            exportTask.setFileName(taskMessage.getFileName());
            exportTask.setOrganizationId(taskMessage.getOrgId());

            if (Boolean.TRUE.equals(taskMessage.getIsSelectExport())) {
                // 处理选中导出
                handleSelectExport(taskMessage, exportTask);
            } else {
                // 处理条件导出
                handleConditionExport(taskMessage, exportTask);
            }

        } catch (InterruptedException e) {
            LogUtils.info("客户导出任务被中断，任务ID：{}", taskMessage.getTaskId());
            exportTaskService.update(taskMessage.getTaskId(), ExportConstants.ExportStatus.STOP.toString(), taskMessage.getUserId());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LogUtils.error("客户导出任务执行异常，任务ID：" + taskMessage.getTaskId(), e);
            exportTaskService.update(taskMessage.getTaskId(), ExportConstants.ExportStatus.ERROR.toString(), taskMessage.getUserId());
        } finally {
            ExportThreadRegistry.remove(taskMessage.getTaskId());
        }
    }

    /**
     * 处理条件导出
     *
     * @param taskMessage 任务消息
     * @param exportTask  导出任务
     * @throws Exception 异常
     */
    private void handleConditionExport(ExportTaskMessage taskMessage, ExportTask exportTask) throws Exception {
        CustomerExportRequest request = objectMapper.readValue(
                taskMessage.getExportParams(), CustomerExportRequest.class
        );

        @SuppressWarnings("unchecked")
        DeptDataPermissionDTO deptDataPermission = objectMapper.convertValue(
                taskMessage.getExtraParams().get("deptDataPermission"), DeptDataPermissionDTO.class
        );

        @SuppressWarnings("unchecked")
        List<ExportHeadDTO> headList = (List<ExportHeadDTO>) taskMessage.getExtraParams().get("headList");

        // 转换表头
        List<List<String>> headers = streamingExportService.convertHeaders(headList);

        // 创建导出上下文
        StreamingExportService.ExportContext<CustomerExportRequest> context =
                streamingExportService.createContext(exportTask, headers);

        // 执行流式导出
        streamingExportService.streamExport(context, ctx -> {
            // 检查中断
            if (ExportThreadRegistry.isInterrupted(taskMessage.getTaskId())) {
                throw new RuntimeException(new InterruptedException("任务被中断"));
            }

            // 分页查询数据
            PageHelper.startPage(ctx.getCurrentPage(), ctx.getBatchSize());
            List<CustomerListResponse> rawList = extCustomerMapper.list(
                    request, taskMessage.getOrgId(), taskMessage.getUserId(), deptDataPermission
            );

            // 构建导出数据
            return buildExportData(headList, rawList, taskMessage.getOrgId(), taskMessage.getTaskId());
        });

        // 更新任务状态为成功
        exportTaskService.update(taskMessage.getTaskId(), ExportConstants.ExportStatus.SUCCESS.toString(), taskMessage.getUserId());
    }

    /**
     * 处理选中导出
     *
     * @param taskMessage 任务消息
     * @param exportTask  导出任务
     * @throws Exception 异常
     */
    private void handleSelectExport(ExportTaskMessage taskMessage, ExportTask exportTask) throws Exception {
        ExportSelectRequest request = objectMapper.readValue(
                taskMessage.getExportParams(), ExportSelectRequest.class
        );

        @SuppressWarnings("unchecked")
        List<ExportHeadDTO> headList = (List<ExportHeadDTO>) taskMessage.getExtraParams().get("headList");

        List<String> ids = objectMapper.readValue(
                taskMessage.getSelectIds(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        // 转换表头
        List<List<String>> headers = streamingExportService.convertHeaders(headList);

        // 创建导出上下文
        StreamingExportService.ExportContext<ExportSelectRequest> context =
                streamingExportService.createContext(exportTask, headers);

        // 执行流式选中导出
        streamingExportService.streamSelectExport(context, ids, batchIds -> {
            // 检查中断
            if (ExportThreadRegistry.isInterrupted(taskMessage.getTaskId())) {
                throw new RuntimeException(new InterruptedException("任务被中断"));
            }

            // 查询数据
            List<CustomerListResponse> rawList = extCustomerMapper.getListByIds(batchIds);

            // 构建导出数据
            return buildExportData(headList, rawList, taskMessage.getOrgId(), taskMessage.getTaskId());
        });

        // 更新任务状态为成功
        exportTaskService.update(taskMessage.getTaskId(), ExportConstants.ExportStatus.SUCCESS.toString(), taskMessage.getUserId());
    }

    /**
     * 构建导出数据
     *
     * @param headList   表头列表
     * @param rawList    原始数据列表
     * @param orgId      组织ID
     * @param taskId     任务ID
     * @return 导出数据
     */
    private List<List<Object>> buildExportData(List<ExportHeadDTO> headList,
                                               List<CustomerListResponse> rawList,
                                               String orgId, String taskId) {
        if (rawList.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建列表数据
        List<CustomerListResponse> dataList = customerService.buildListData(rawList, orgId);

        // 获取字段配置
        Map<String, BaseField> fieldConfigMap = moduleFormService.getAllFields(FormKey.CUSTOMER.getKey(), orgId)
                .stream()
                .collect(Collectors.toMap(BaseField::getId, Function.identity()));

        List<List<Object>> result = new ArrayList<>(dataList.size());
        for (CustomerListResponse data : dataList) {
            // 检查中断
            if (ExportThreadRegistry.isInterrupted(taskId)) {
                throw new RuntimeException(new InterruptedException("任务被中断"));
            }
            result.add(buildRowData(headList, data, fieldConfigMap));
        }
        return result;
    }

    /**
     * 构建单行数据
     *
     * @param headList       表头列表
     * @param data           客户数据
     * @param fieldConfigMap 字段配置映射
     * @return 行数据
     */
    private List<Object> buildRowData(List<ExportHeadDTO> headList,
                                      CustomerListResponse data,
                                      Map<String, BaseField> fieldConfigMap) {
        List<Object> rowData = new ArrayList<>();

        // 获取系统字段映射
        LinkedHashMap<String, Object> systemFieldMap = PoolCustomerFieldUtils.getSystemFieldMap(data);

        // 获取自定义字段映射
        AtomicReference<Map<String, Object>> moduleFieldMap = new AtomicReference<>(new LinkedHashMap<>());
        Optional.ofNullable(data.getModuleFields()).ifPresent(moduleFields -> {
            moduleFieldMap.set(moduleFields.stream()
                    .collect(Collectors.toMap(
                            fv -> fv.getFieldId(),
                            fv -> fv.getFieldValue(),
                            (v1, v2) -> v1
                    )));
        });

        // 根据表头顺序构建数据
        for (ExportHeadDTO head : headList) {
            String key = head.getKey();
            if (systemFieldMap.containsKey(key)) {
                rowData.add(systemFieldMap.get(key));
            } else if (moduleFieldMap.get().containsKey(key)) {
                // 处理自定义字段
                Object value = moduleFieldMap.get().get(key);
                BaseField fieldConfig = fieldConfigMap.get(key);
                if (fieldConfig != null && value != null) {
                    rowData.add(transformFieldValue(fieldConfig, value));
                } else {
                    rowData.add(value);
                }
            } else {
                rowData.add(null);
            }
        }

        return rowData;
    }

    /**
     * 转换字段值
     *
     * @param field 字段配置
     * @param value 原始值
     * @return 转换后的值
     */
    private Object transformFieldValue(BaseField field, Object value) {
        if (value == null) {
            return null;
        }
        // 这里可以根据字段类型进行转换
        // 简化处理，直接返回字符串表示
        return value.toString();
    }
}
