package cn.cordys.crm.system.stream;

import cn.cordys.common.util.LogUtils;
import cn.cordys.common.util.SubListUtils;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.domain.ExportTask;
import cn.cordys.crm.system.excel.handler.CustomHeadColWidthStyleStrategy;
import cn.cordys.crm.system.excel.handler.SummaryMergeHandler;
import cn.cordys.crm.system.queue.ExportTaskQueueManager;
import cn.cordys.crm.system.queue.dto.ExportTaskMessage;
import cn.cordys.crm.system.service.ExportTaskService;
import cn.cordys.registry.ExportThreadRegistry;
import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.metadata.WriteSheet;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.function.Function;

@Service
public class StreamDataExportService {

    private static final int DEFAULT_BATCH_SIZE = 2000;

    @Resource
    private ExportTaskQueueManager queueManager;

    @Resource
    private ExportTaskService exportTaskService;

    @FunctionalInterface
    public interface BatchDataProvider<T> {
        List<?> getBatchData(T request, int pageNum, int pageSize) throws InterruptedException;
    }

    @FunctionalInterface
    public interface SelectDataProvider {
        List<?> getBatchDataByIds(List<String> ids) throws InterruptedException;
    }

    public <T> void streamExportAll(
            ExportTaskMessage message,
            T request,
            List<List<String>> headList,
            File outputFile,
            BatchDataProvider<T> dataProvider,
            int totalPages
    ) throws Exception {

        try (ExcelWriter writer = EasyExcel.write(outputFile)
                .head(headList)
                .excelType(ExcelTypeEnum.XLSX)
                .build()) {

            WriteSheet sheet = EasyExcel.writerSheet("导出数据").build();
            int currentPage = 1;

            while (true) {
                if (ExportThreadRegistry.isInterrupted(message.getTaskId())) {
                    throw new InterruptedException("导出任务被用户中断");
                }

                List<?> batchData = dataProvider.getBatchData(request, currentPage, message.getBatchSize());

                if (batchData == null || batchData.isEmpty()) {
                    break;
                }

                writer.write(batchData, sheet);

                queueManager.updateTaskProgress(message.getTaskId(), currentPage, totalPages);

                if (batchData.size() < message.getBatchSize()) {
                    break;
                }

                currentPage++;

                if (currentPage % 5 == 0) {
                    System.gc();
                }
            }

            LogUtils.info("流式导出完成: taskId={}, totalPages={}", message.getTaskId(), currentPage);
        }
    }

    public void streamExportSelect(
            ExportTaskMessage message,
            List<String> selectIds,
            List<List<String>> headList,
            File outputFile,
            SelectDataProvider dataProvider
    ) throws Exception {

        try (ExcelWriter writer = EasyExcel.write(outputFile)
                .head(headList)
                .excelType(ExcelTypeEnum.XLSX)
                .build()) {

            WriteSheet sheet = EasyExcel.writerSheet("导出数据").build();
            final int[] processedCount = {0};
            int totalBatches = (selectIds.size() + message.getBatchSize() - 1) / message.getBatchSize();

            SubListUtils.dealForSubList(selectIds, message.getBatchSize(), (batchIds) -> {
                try {
                    if (ExportThreadRegistry.isInterrupted(message.getTaskId())) {
                        throw new InterruptedException("导出任务被用户中断");
                    }

                    List<?> batchData = dataProvider.getBatchDataByIds(batchIds);

                    if (batchData != null && !batchData.isEmpty()) {
                        writer.write(batchData, sheet);
                    }

                    processedCount[0]++;
                    queueManager.updateTaskProgress(message.getTaskId(), processedCount[0], totalBatches);

                    if (processedCount[0] % 5 == 0) {
                        System.gc();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            LogUtils.info("选中数据流式导出完成: taskId={}, totalBatches={}", message.getTaskId(), processedCount[0]);
        }
    }

    public void streamExportWithMerge(
            ExportTaskMessage message,
            List<List<String>> headList,
            List<Integer> mergeColumns,
            File outputFile,
            Function<Integer, cn.cordys.crm.system.excel.domain.MergeResult> dataProvider,
            int totalPages
    ) throws Exception {

        try (ExcelWriter writer = EasyExcel.write(outputFile)
                .head(headList)
                .excelType(ExcelTypeEnum.XLSX)
                .registerWriteHandler(new CustomHeadColWidthStyleStrategy())
                .build()) {

            WriteSheet sheet = EasyExcel.writerSheet("导出数据").build();
            int offset = 2;
            int currentPage = 1;

            while (true) {
                if (ExportThreadRegistry.isInterrupted(message.getTaskId())) {
                    throw new InterruptedException("导出任务被用户中断");
                }

                cn.cordys.crm.system.excel.domain.MergeResult mergeResult = dataProvider.apply(currentPage);

                if (mergeResult == null || mergeResult.getDataList() == null || mergeResult.getDataList().isEmpty()) {
                    break;
                }

                writer.write(mergeResult.getDataList(), sheet);

                Sheet mergeSheet = writer.writeContext().writeWorkbookHolder().getWorkbook().getSheetAt(0);
                SummaryMergeHandler strategy = new SummaryMergeHandler(
                        mergeResult.getMergeRegions(), mergeColumns, offset);
                strategy.merge(mergeSheet);

                queueManager.updateTaskProgress(message.getTaskId(), currentPage, totalPages);

                if (mergeResult.getDataList().size() < message.getBatchSize()) {
                    break;
                }

                offset += mergeResult.getDataList().size();
                currentPage++;

                if (currentPage % 5 == 0) {
                    System.gc();
                }
            }

            LogUtils.info("带合并策略的流式导出完成: taskId={}, totalPages={}", message.getTaskId(), currentPage);
        }
    }

    public File prepareExportFile(String fileId, String fileName, String orgId) {
        String exportDirPath = cn.cordys.file.engine.DefaultRepositoryDir.getDefaultDir()
                + File.separator
                + cn.cordys.file.engine.DefaultRepositoryDir.getExportDir(orgId)
                + File.separator + fileId;

        File dir = new File(exportDirPath);

        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("无法创建导出目录: " + dir.getAbsolutePath());
        }

        return new File(dir, fileName + ".xlsx");
    }
}
