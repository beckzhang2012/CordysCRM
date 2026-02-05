package cn.cordys.crm.system.producer;

import cn.cordys.crm.system.config.RabbitMQConfig;
import cn.cordys.crm.system.dto.ExportTaskMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ExportTaskProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendExportTask(ExportTaskMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXPORT_EXCHANGE,
                    RabbitMQConfig.EXPORT_ROUTING_KEY,
                    message
            );

            log.info("导出任务已发送到消息队列, taskId: {}", message.getTaskId());
        } catch (Exception e) {
            log.error("发送导出任务到消息队列失败, taskId: {}", message.getTaskId(), e);
            throw new RuntimeException("导出任务提交失败", e);
        }
    }

    public void sendExportTaskWithDelay(ExportTaskMessage message, long delay, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("延迟任务功能暂不支持");
    }
}
