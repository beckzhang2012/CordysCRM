package cn.cordys.crm.config;

import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExportQueueConfig {

    public static final String EXPORT_TASK_QUEUE_NAME = "crm:export:task:queue";
    public static final String EXPORT_TASK_PROGRESS_KEY = "crm:export:task:progress";
    public static final String EXPORT_TASK_CANCEL_KEY = "crm:export:task:cancel";
    public static final String EXPORT_TASK_LOCK_KEY = "crm:export:task:lock";

    @Bean
    public RBlockingQueue<String> exportTaskQueue(RedissonClient redissonClient) {
        return redissonClient.getBlockingQueue(EXPORT_TASK_QUEUE_NAME);
    }
}
