package cn.cordys.common.service;

import cn.cordys.common.dto.ExportHeadDTO;
import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.service.ExportTaskService;
import cn.cordys.file.engine.DefaultRepositoryDir;
import cn.cordys.registry.ExportThreadRegistry;
import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.metadata.WriteSheet;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 流式导出服务
 * 支持大数据量的分批流式导出，避免内存溢出
 */
@Service
public class StreamingExportService {

    /**
     * 默认批次大小
     */
    public static final int DEFAULT_BATCH_SIZE = 2000;

    /**
     * 最小批次大小
     */
    public static final int MIN_BATCH_SIZE = 500;

    /**
     * 最大批次大小
     */
    public static final int MAX_BATCH_SIZE = 5000;

    @Resource
    private ExportTaskService exportTaskService;

    /**
     * 流式导出上下文
     */
    public static class ExportContext<T> {
        private final String taskId;
        private final String fileId;
        private final String fileName;
        private final String orgId;
        private final List<List<String>> headers;
        private final int batchSize;
        private final AtomicInteger processedCount;
        private final AtomicInteger currentPage;
        private volatile boolean interrupted;

        public ExportContext(String taskId, String fileId, String fileName, String orgId,
                             List<List<String>> headers, int batchSize) {
            this.taskId = taskId;
            this.fileId = fileId;
            this.fileName = fileName;
            this.orgId = orgId;
            this.headers = headers;
            this.batchSize = Math.clamp(batchSize, MIN_BATCH_SIZE, MAX_BATCH_SIZE);
            this.processedCount = new AtomicInteger(0);
            this.currentPage = new AtomicInteger(1);
            this.interrupted = false;
        }

        public String getTaskId() {
            return taskId;
        }

        public List<List<String>> getHeaders() {
            return headers;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public int getProcessedCount() {
            return processedCount.get();
        }

        public int getCurrentPage() {
            return currentPage.get();
        }

        public void incrementPage() {
            currentPage.incrementAndGet();
        }

        public void addProcessedCount(int count) {
            processedCount.addAndGet(count);
        }

        public boolean isInterrupted() {
            return interrupted || ExportThreadRegistry.isInterrupted(taskId);
        }

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
        }

        public String getFileId() {
            return fileId;
        }

        public String getFileName() {
            return fileName;
        }

        public String getOrgId() {
            return orgId;
        }
    }

    /**
     * 执行流式分页导出
     *
     * @param context   导出上下文
     * @param fetchData 数据获取函数（分页获取数据）
     * @param <T>       数据类型
     * @throws InterruptedException 当任务被中断时抛出
     * @throws IOException          当文件操作异常时抛出
     */
    public <T> void streamExport(ExportContext<T> context,
                                 Function<ExportContext<T>, List<List<Object>>> fetchData) throws InterruptedException, IOException {

        // 准备导出文件
        File file = prepareExportFile(context.getFileId(), context.getFileName(), context.getOrgId());

        try (ExcelWriter writer = EasyExcel.write(file)
                .head(context.getHeaders())
                .excelType(ExcelTypeEnum.XLSX)
                .build()) {

            WriteSheet sheet = EasyExcel.writerSheet("导出数据").build();

            while (!context.isInterrupted()) {
                // 检查中断状态
                if (ExportThreadRegistry.isInterrupted(context.getTaskId())) {
                    throw new InterruptedException("导出任务被中断，任务ID：" + context.getTaskId());
                }

                // 获取当前批次数据
                List<List<Object>> batchData = fetchData.apply(context);

                if (CollectionUtils.isEmpty(batchData)) {
                    // 没有更多数据，结束导出
                    break;
                }

                // 写入数据
                writer.write(batchData, sheet);

                // 更新处理计数
                context.addProcessedCount(batchData.size());

                // 更新任务进度（每处理10000条更新一次）
                if (context.getProcessedCount() % 10000 == 0) {
                    updateTaskProgress(context.getTaskId(), context.getProcessedCount());
                }

                // 如果当前批次数据少于批次大小，说明是最后一批
                if (batchData.size() < context.getBatchSize()) {
                    break;
                }

                // 页码递增
                context.incrementPage();

                // 主动让出CPU，避免长时间占用
                Thread.yield();
            }
        }

        LogUtils.info("流式导出完成，任务ID：{}，总处理记录数：{}",
                context.getTaskId(), context.getProcessedCount());
    }

    /**
     * 执行流式选中导出（基于ID列表）
     *
     * @param context     导出上下文
     * @param ids         ID列表
     * @param fetchByIds  根据ID获取数据的函数
     * @param <T>         数据类型
     * @throws InterruptedException 当任务被中断时抛出
     * @throws IOException          当文件操作异常时抛出
     */
    public <T> void streamSelectExport(ExportContext<T> context,
                                       List<String> ids,
                                       Function<List<String>, List<List<Object>>> fetchByIds) throws InterruptedException, IOException {

        // 准备导出文件
        File file = prepareExportFile(context.getFileId(), context.getFileName(), context.getOrgId());

        try (ExcelWriter writer = EasyExcel.write(file)
                .head(context.getHeaders())
                .excelType(ExcelTypeEnum.XLSX)
                .build()) {

            WriteSheet sheet = EasyExcel.writerSheet("导出数据").build();

            // 分批处理ID列表
            int totalIds = ids.size();
            int processedIds = 0;

            while (processedIds < totalIds && !context.isInterrupted()) {
                // 检查中断状态
                if (ExportThreadRegistry.isInterrupted(context.getTaskId())) {
                    throw new InterruptedException("导出任务被中断，任务ID：" + context.getTaskId());
                }

                // 计算当前批次的ID范围
                int endIndex = Math.min(processedIds + context.getBatchSize(), totalIds);
                List<String> batchIds = ids.subList(processedIds, endIndex);

                // 获取当前批次数据
                List<List<Object>> batchData = fetchByIds.apply(batchIds);

                if (CollectionUtils.isNotEmpty(batchData)) {
                    // 写入数据
                    writer.write(batchData, sheet);
                    context.addProcessedCount(batchData.size());
                }

                processedIds = endIndex;

                // 更新任务进度（每处理10000条更新一次）
                if (context.getProcessedCount() % 10000 == 0) {
                    updateTaskProgress(context.getTaskId(), context.getProcessedCount());
                }

                // 主动让出CPU
                Thread.yield();
            }
        }

        LogUtils.info("流式选中导出完成，任务ID：{}，总处理记录数：{}",
                context.getTaskId(), context.getProcessedCount());
    }

    /**
     * 准备导出文件
     *
     * @param fileId   文件ID
     * @param fileName 文件名
     * @param orgId    组织ID
     * @return 导出文件
     */
    private File prepareExportFile(String fileId, String fileName, String orgId) {
        if (fileId == null || fileName == null || orgId == null) {
            throw new IllegalArgumentException("文件ID、文件名和组织ID不能为空");
        }

        // 构建导出目录路径
        String exportDirPath = DefaultRepositoryDir.getDefaultDir()
                + File.separator
                + DefaultRepositoryDir.getExportDir(orgId)
                + File.separator + fileId;

        File dir = new File(exportDirPath);

        // 检查目录创建结果
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("无法创建导出目录: " + dir.getAbsolutePath());
        }

        // 返回完整的文件路径
        return new File(dir, fileName + ".xlsx");
    }

    /**
     * 更新任务进度
     *
     * @param taskId    任务ID
     * @param processed 已处理数量
     */
    private void updateTaskProgress(String taskId, int processed) {
        try {
            // 可以在这里添加进度更新逻辑，如写入Redis或数据库
            LogUtils.debug("导出任务进度更新，任务ID：{}，已处理：{}条", taskId, processed);
        } catch (Exception e) {
            LogUtils.error("更新导出任务进度失败", e);
        }
    }

    /**
     * 创建导出上下文
     *
     * @param task    导出任务
     * @param headers 表头
     * @param <T>     数据类型
     * @return 导出上下文
     */
    public <T> ExportContext<T> createContext(ExportTask task, List<List<String>> headers) {
        return new ExportContext<>(
                task.getId(),
                task.getFileId(),
                task.getFileName(),
                task.getOrganizationId(),
                headers,
                DEFAULT_BATCH_SIZE
        );
    }

    /**
     * 创建导出上下文（自定义批次大小）
     *
     * @param task      导出任务
     * @param headers   表头
     * @param batchSize 批次大小
     * @param <T>       数据类型
     * @return 导出上下文
     */
    public <T> ExportContext<T> createContext(ExportTask task, List<List<String>> headers, int batchSize) {
        return new ExportContext<>(
                task.getId(),
                task.getFileId(),
                task.getFileName(),
                task.getOrganizationId(),
                headers,
                batchSize
        );
    }

    /**
     * 转换表头DTO为列表格式
     *
     * @param headList 表头DTO列表
     * @return 表头列表
     */
    public List<List<String>> convertHeaders(List<ExportHeadDTO> headList) {
        return headList.stream()
                .map(head -> List.of(head.getTitle()))
                .toList();
    }
}
