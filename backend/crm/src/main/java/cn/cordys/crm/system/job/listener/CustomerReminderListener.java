package cn.cordys.crm.system.job.listener;

import cn.cordys.common.dto.OptionDTO;
import cn.cordys.common.util.LogUtils;
import cn.cordys.crm.customer.constants.CustomerReminderStatus;
import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.mapper.ExtCustomerMapper;
import cn.cordys.crm.customer.service.CustomerReminderService;
import cn.cordys.crm.system.constants.NotificationConstants;
import cn.cordys.crm.system.notice.CommonNoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户提醒监听器
 * <p>
 * 该监听器负责监听执行事件，当触发时检查并提醒到期的客户提醒。
 * </p>
 */
@Component
public class CustomerReminderListener implements ApplicationListener<ExecuteEvent> {

    @Resource
    private CustomerReminderService customerReminderService;

    @Resource
    private CommonNoticeSendService commonNoticeSendService;

    @Resource
    private ExtCustomerMapper extCustomerMapper;

    @Override
    public void onApplicationEvent(ExecuteEvent event) {
        try {
            this.checkAndTriggerReminders();
        } catch (Exception e) {
            LogUtils.error("客户提醒触发异常: ", e);
        }
    }

    /**
     * 检查并触发到期的客户提醒
     */
    public void checkAndTriggerReminders() {
        LogUtils.info("开始检查到期的客户提醒");
        
        long currentTime = System.currentTimeMillis();
        List<CustomerReminder> pendingReminders = customerReminderService.getPendingReminders(currentTime);

        if (CollectionUtils.isEmpty(pendingReminders)) {
            LogUtils.info("没有到期的客户提醒");
            return;
        }

        LogUtils.info("发现 {} 条到期的客户提醒", pendingReminders.size());

        // 提取客户ID列表
        List<String> customerIds = pendingReminders.stream()
                .map(CustomerReminder::getCustomerId)
                .distinct()
                .collect(Collectors.toList());

        // 查询客户名称
        Map<String, String> customerNameMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(customerIds)) {
            customerNameMap = extCustomerMapper.selectOptionByIds(customerIds)
                    .stream()
                    .collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
        }

        // 提取需要更新状态的提醒ID
        List<String> reminderIds = pendingReminders.stream()
                .map(CustomerReminder::getId)
                .collect(Collectors.toList());

        // 批量更新状态为已提醒
        customerReminderService.batchUpdateStatus(reminderIds, CustomerReminderStatus.TRIGGERED.name(), "system");

        // 发送通知
        for (CustomerReminder reminder : pendingReminders) {
            sendReminderNotification(reminder, customerNameMap);
        }

        LogUtils.info("客户提醒触发完成，共处理 {} 条提醒", pendingReminders.size());
    }

    /**
     * 发送提醒通知
     */
    private void sendReminderNotification(CustomerReminder reminder, Map<String, String> customerNameMap) {
        String customerName = customerNameMap.getOrDefault(reminder.getCustomerId(), "未知客户");
        
        Map<String, Object> resource = new HashMap<>();
        resource.put("name", customerName);
        resource.put("content", reminder.getContent());
        resource.put("customerName", customerName);
        resource.put("reminderContent", reminder.getContent());

        commonNoticeSendService.sendNotice(
                NotificationConstants.Module.CUSTOMER,
                NotificationConstants.Event.CUSTOMER_REMINDER,
                resource,
                reminder.getCreateUser(),
                reminder.getOrganizationId(),
                List.of(reminder.getOwner()),
                false
        );
    }
}
