package cn.cordys.crm.system.job;

import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.system.job.listener.ExecuteEvent;
import cn.cordys.quartz.anno.QuartzScheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 客户提醒定时任务
 * <p>
 * 每分钟执行一次，检查并触发到期的客户提醒
 * </p>
 */
@Component
public class CustomerReminderJob {

    private final ApplicationEventPublisher publisher;

    @Autowired
    public CustomerReminderJob(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 每分钟执行一次，检查到期的客户提醒
     */
    @QuartzScheduled(cron = "0 * * * * ?")
    public void execute() {
        run();
    }

    public void run() {
        LogUtils.info("开始执行客户提醒检查任务");
        publisher.publishEvent(new ExecuteEvent(this));
        LogUtils.info("客户提醒检查任务执行完成");
    }
}
