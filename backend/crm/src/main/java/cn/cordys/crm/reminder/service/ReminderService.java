package cn.cordys.crm.reminder.service;

import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.service.BaseService;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.BeanUtils;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.Customer;
import cn.cordys.crm.customer.mapper.ExtCustomerMapper;
import cn.cordys.crm.reminder.domain.Reminder;
import cn.cordys.crm.reminder.dto.request.ReminderAddRequest;
import cn.cordys.crm.reminder.dto.request.ReminderPageRequest;
import cn.cordys.crm.reminder.dto.response.ReminderListResponse;
import cn.cordys.crm.reminder.mapper.ExtReminderMapper;
import cn.cordys.crm.system.domain.User;
import cn.cordys.crm.system.mapper.ExtUserMapper;
import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ReminderService extends BaseService {

    @Resource
    private ExtReminderMapper extReminderMapper;

    @Resource
    private ExtCustomerMapper extCustomerMapper;

    @Resource
    private ExtUserMapper extUserMapper;

    public PagerWithOption<List<ReminderListResponse>> list(ReminderPageRequest request, String userId, String orgId) {
        int total = extReminderMapper.countByUserId(userId, orgId);
        List<ReminderListResponse> list = extReminderMapper.list(request, orgId, userId);
        return new PagerWithOption<>(list, total);
    }

    public Reminder get(String id, String userId, String orgId) {
        Reminder reminder = extReminderMapper.selectById(id);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        if (!reminder.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权查看此提醒");
        }
        return reminder;
    }

    public Reminder add(ReminderAddRequest request, String userId, String orgId) {
        Customer customer = extCustomerMapper.selectById(request.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("客户不存在");
        }

        User user = extUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Reminder reminder = new Reminder();
        BeanUtils.copyProperties(request, reminder);
        reminder.setId(IDGenerator.generateId());
        reminder.setCustomerName(customer.getName());
        reminder.setIsRead(false);
        reminder.setOrganizationId(orgId);
        reminder.setCreatorId(userId);
        reminder.setCreatorName(user.getName());
        reminder.setCreateTime(System.currentTimeMillis());
        reminder.setCreateUser(userId);
        reminder.setUpdateTime(System.currentTimeMillis());
        reminder.setUpdateUser(userId);

        extReminderMapper.insert(reminder);
        return reminder;
    }

    public void delete(String id, String userId, String orgId) {
        Reminder reminder = extReminderMapper.selectById(id);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        if (!reminder.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权删除此提醒");
        }
        extReminderMapper.deleteById(id);
    }

    public void markAsRead(String id, String userId, String orgId) {
        Reminder reminder = extReminderMapper.selectById(id);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        if (!reminder.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权操作此提醒");
        }
        reminder.setIsRead(true);
        reminder.setUpdateTime(System.currentTimeMillis());
        reminder.setUpdateUser(userId);
        extReminderMapper.updateById(reminder);
    }

    public void markAllAsRead(String userId, String orgId) {
        List<ReminderListResponse> unreadReminders = extReminderMapper.list(
            new ReminderPageRequest().setIsRead(false), orgId, userId
        );
        for (ReminderListResponse reminder : unreadReminders) {
            Reminder reminderEntity = new Reminder();
            reminderEntity.setId(reminder.getId());
            reminderEntity.setIsRead(true);
            reminderEntity.setUpdateTime(System.currentTimeMillis());
            reminderEntity.setUpdateUser(userId);
            extReminderMapper.updateById(reminderEntity);
        }
    }

    public int getUnreadCount(String userId, String orgId) {
        return extReminderMapper.countByUserId(userId, orgId);
    }
}
