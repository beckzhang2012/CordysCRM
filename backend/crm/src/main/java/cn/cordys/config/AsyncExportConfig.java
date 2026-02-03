package cn.cordys.config;

import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.service.ExportTaskQueueService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步导出配置类
 * 启用异步导出功能和任务队列管理
 */
@Configuration
@EnableAsync
public class AsyncExportConfig {

    @Resource
    private ExportTaskQueueService exportTaskQueueService;

    /**
     * 初始化导出任务队列服务
     */
    @PostConstruct
    public void init() {
        // 服务会在PostConstruct中自动初始化
        LogUtils.info("异步导出配置已加载，导出任务队列服务已就绪");
    }
}
