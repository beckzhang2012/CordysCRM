package cn.cordys.crm.system.consumer;

import cn.cordys.aspectj.constants.LogModule;
import cn.cordys.crm.customer.service.CustomerExportService;
import cn.cordys.crm.system.config.RabbitMQConfig;
import cn.cordys.crm.system.constants.ExportConstants;
import cn.cordys.crm.system.dto.ExportTaskMessage;
import cn.cordys.crm.system.service.ExportTaskService;
import cn.cordys.registry.ExportThreadRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExportTaskConsumer {

    private final CustomerExportService customerExportService;
    private final ExportTaskService exportTaskService;

    public ExportTaskConsumer(CustomerExportService customerExportService,
                             ExportTaskService exportTaskService) {
        this.customerExportService = customerExportService;
        this.exportTaskService = exportTaskService;
    }

    @RabbitListener(queues = RabbitMQConfig.EXPORT_QUEUE)
    public void handleExportTask(ExportTaskMessage message) {
        log.info("开始处理导出任务, taskId: {}, exportType: {}", message.getTaskId(), message.getExportType());

        try {
            if (ExportConstants.ExportType.CUSTOMER.toString().equals(message.getExportType())) {
                if (message.getCustomerExportRequest() != null) {
                    customerExportService.doExportCustomerData(message);
                } else if (message.getExportSelectRequest() != null) {
                    customerExportService.doExportSelectData(message);
                }
            }

            log.info("导出任务处理完成, taskId: {}", message.getTaskId());

        } catch (Exception e) {
            log.error("导出任务处理失败, taskId: {}", message.getTaskId(), e);
            try {
                exportTaskService.update(message.getTaskId(), ExportConstants.ExportStatus.ERROR.toString(), message.getUserId());
            } catch (Exception ex) {
                log.error("更新失败状态失败: {}", message.getTaskId(), ex);
            }
        } finally {
            ExportThreadRegistry.remove(message.getTaskId());
            LocaleContextHolder.resetLocaleContext();
        }
    }

    @RabbitListener(queues = RabbitMQConfig.EXPORT_DLX_QUEUE)
    public void handleDlxExportTask(ExportTaskMessage message) {
        log.warn("处理死信队列中的导出任务, taskId: {}, exportType: {}", message.getTaskId(), message.getExportType());
        exportTaskService.update(message.getTaskId(), ExportConstants.ExportStatus.ERROR.toString(), message.getUserId());
    }
}
