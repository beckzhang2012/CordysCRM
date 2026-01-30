package cn.cordys.crm.customer.service;

import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.constants.ReminderConstants;
import cn.cordys.crm.customer.domain.Customer;
import cn.cordys.crm.customer.domain.Reminder;
import cn.cordys.crm.customer.dto.request.ReminderAddRequest;
import cn.cordys.crm.customer.dto.request.ReminderPageRequest;
import cn.cordys.crm.customer.dto.response.ReminderListResponse;
import cn.cordys.crm.customer.mapper.ExtReminderMapper;
import cn.cordys.crm.system.constants.NotificationConstants;
import cn.cordys.crm.system.notice.CommonNoticeSendService;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReminderService {

    @Resource
    private BaseMapper<Reminder> reminderMapper;
    @Resource
    private ExtReminderMapper extReminderMapper;
    @Resource
    private BaseMapper<Customer> customerMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;

    public void add(ReminderAddRequest request) {
        Customer customer = customerMapper.selectByPrimaryKey(request.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("客户不存在");
        }
        
        Reminder reminder = new Reminder();
        reminder.setId(IDGenerator.nextStr());
        reminder.setCustomerId(request.getCustomerId());
        reminder.setCustomerName(customer.getName());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setContent(request.getContent());
        reminder.setStatus(ReminderConstants.Status.PENDING.name());
        reminder.setReceiver(SessionUtils.getUserId());
        reminder.setOrganizationId(OrganizationContext.getOrganizationId());
        reminder.setCreateUser(SessionUtils.getUserId());
        reminder.setUpdateUser(SessionUtils.getUserId());
        reminder.setCreateTime(System.currentTimeMillis());
        reminder.setUpdateTime(System.currentTimeMillis());
        
        reminderMapper.insert(reminder);
    }

    public Pager<List<ReminderListResponse>> list(ReminderPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, extReminderMapper.list(request, OrganizationContext.getOrganizationId(), SessionUtils.getUserId()));
    }

    public void delete(String id) {
        Reminder reminder = reminderMapper.selectByPrimaryKey(id);
        if (reminder != null && 
            reminder.getReceiver().equals(SessionUtils.getUserId()) &&
            reminder.getOrganizationId().equals(OrganizationContext.getOrganizationId())) {
            reminderMapper.deleteByPrimaryKey(id);
        }
    }

    public void cancel(String id) {
        Reminder reminder = new Reminder();
        reminder.setId(id);
        reminder.setStatus(ReminderConstants.Status.CANCELLED.name());
        reminder.setUpdateUser(SessionUtils.getUserId());
        reminder.setUpdateTime(System.currentTimeMillis());
        reminderMapper.updateById(reminder);
    }

    public int countPending() {
        return extReminderMapper.countPendingByUserId(SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    public void processDueReminders() {
        long currentTime = System.currentTimeMillis();
        List<Reminder> dueReminders = extReminderMapper.selectPendingReminders(currentTime, OrganizationContext.getOrganizationId());
        
        if (CollectionUtils.isEmpty(dueReminders)) {
            return;
        }
        
        for (Reminder reminder : dueReminders) {
            commonNoticeSendService.sendNotice(
                NotificationConstants.Module.CUSTOMER,
                "REMINDER_NOTICE",
                Map.of(
                    "name", reminder.getCustomerName(),
                    "customerName", reminder.getCustomerName(),
                    "reminderContent", reminder.getContent()
                ),
                SessionUtils.getUserId(),
                reminder.getOrganizationId(),
                List.of(reminder.getReceiver()),
                false
            );
            
            Reminder updateReminder = new Reminder();
            updateReminder.setId(reminder.getId());
            updateReminder.setStatus(ReminderConstants.Status.COMPLETED.name());
            updateReminder.setUpdateTime(System.currentTimeMillis());
            reminderMapper.updateById(updateReminder);
        }
    }
}
