package cn.cordys.common.service;

import cn.cordys.common.domain.ExportTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

/**
 * 导出消息发布者
 * 用于将导出任务发送到Redis消息队列
 * @author song-cc-rock
 */
@Service
public class ExportMessagePublisher {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 导出任务主题
     */
    public static final String EXPORT_TASK_TOPIC = "export:task:topic";

    /**
     * 发布导出任务到消息队列
     * @param task 导出任务
     */
    public void publish(ExportTask task) {
        try {
            String taskJson = objectMapper.writeValueAsString(task);
            redisTemplate.convertAndSend(EXPORT_TASK_TOPIC, taskJson);
        } catch (Exception e) {
            throw new RuntimeException("发布导出任务失败", e);
        }
    }

    /**
     * 获取导出任务主题
     * @return 主题
     */
    public ChannelTopic getTopic() {
        return new ChannelTopic(EXPORT_TASK_TOPIC);
    }
}
