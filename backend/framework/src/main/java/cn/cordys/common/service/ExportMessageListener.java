package cn.cordys.common.service;

import cn.cordys.common.domain.ExportTask;
import cn.cordys.crm.system.service.ExportTaskService;
import cn.cordys.registry.ExportThreadRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 导出消息监听器
 * 监听Redis消息队列中的导出任务并异步执行
 * @author song-cc-rock
 */
@Service
public class ExportMessageListener implements MessageListener {

    @Resource
    private ExportTaskService exportTaskService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ExportExecutorResolver exportExecutorResolver;

    /**
     * 异步处理导出任务消息
     * @param message 消息体
     * @param pattern 消息模式
     */
    @Override
    @Async("exportTaskExecutor")
    public void onMessage(Message message, byte[] pattern) {
        try {
            String taskJson = new String(message.getBody());
            ExportTask task = objectMapper.readValue(taskJson, ExportTask.class);

            // 设置语言环境
            Locale locale = LocaleContextHolder.getLocale();
            LocaleContextHolder.setLocale(locale);

            // 注册线程用于中断控制
            ExportThreadRegistry.register(task.getId(), Thread.currentThread());

            // 获取对应的导出执行器并执行
            cn.cordys.common.service.ExportExecutor executor = exportExecutorResolver.resolve(task.getResourceType());
            if (executor != null) {
                executor.execute(task);
                exportTaskService.update(task.getId(), "SUCCESS", task.getCreateUser());
            } else {
                exportTaskService.update(task.getId(), "ERROR", task.getCreateUser());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // 处理异常并更新任务状态
        } finally {
            // 清理线程注册
        }
    }

    /**
     * 获取监听的主题
     * @return 主题
     */
    public ChannelTopic getTopic() {
        return new ChannelTopic(ExportMessagePublisher.EXPORT_TASK_TOPIC);
    }
}
