package cn.cordys.crm.customer.job;

import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.mapper.ExtCustomerReminderMapper;
import cn.cordys.crm.customer.service.CustomerReminderService;
import cn.cordys.crm.system.notice.sse.SseService;
import cn.cordys.quartz.anno.QuartzScheduled;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class CustomerReminderJob {
    @Resource
    private ExtCustomerReminderMapper extCustomerReminderMapper;
    @Resource
    private CustomerReminderService customerReminderService;
    @Resource
    private SseService sseService;

    @QuartzScheduled(cron = "0 0/1 * * * ?")
    public void onEvent() {
        try {
            this.processReminders();
        } catch (Exception e) {
            LogUtils.error("客户提醒任务执行异常: ", e.getMessage());
        }
    }

    public void processReminders() {
        LocalDateTime dateTime = LocalDateTime.now();
        long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.doProcessReminders(timestamp);
    }

    private void doProcessReminders(long timestamp) {
        LocalDateTime now = LocalDateTime.now();
        List<CustomerReminder> reminders = extCustomerReminderMapper.selectRemindersToProcess(now);
        
        if (CollectionUtils.isEmpty(reminders)) {
            return;
        }
        
        LogUtils.info("待处理客户提醒数量: {}", reminders.size());
        
        for (CustomerReminder reminder : reminders) {
            try {
                sendReminderNotification(reminder);
                
                CustomerReminder updateReminder = new CustomerReminder();
                updateReminder.setId(reminder.getId());
                updateReminder.setStatus(1);
                updateReminder.setUpdateUser(reminder.getUserId());
                customerReminderService.updateById(updateReminder);
            } catch (Exception e) {
                LogUtils.error("处理客户提醒失败, id: {}", reminder.getId(), e);
            }
        }
    }

    private void sendReminderNotification(CustomerReminder reminder) {
        try {
            java.util.Map<String, Object> messageData = new java.util.HashMap<>();
            messageData.put("type", "customer_reminder");
            messageData.put("id", reminder.getId());
            messageData.put("customerName", reminder.getCustomerName());
            messageData.put("content", reminder.getContent());
            messageData.put("reminderTime", reminder.getReminderTime());
            
            String message = JSON.toJSONString(messageData);
            
            sseService.sendNotice(reminder.getUserId(), message);
            LogUtils.info("发送客户提醒通知给用户: {}, 客户: {}", reminder.getUserId(), reminder.getCustomerName());
        } catch (Exception e) {
            LogUtils.error("发送客户提醒通知失败, id: {}", reminder.getId(), e);
        }
    }
}
