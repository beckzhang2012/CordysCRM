package cn.cordys.crm.customer.service;

import cn.cordys.common.constants.TopicConstants;
import cn.cordys.common.redis.MessagePublisher;
import cn.cordys.common.util.JSON;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.dto.request.CustomerReminderAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerReminderUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerReminderResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerReminderMapper;
import cn.cordys.crm.system.notice.dto.NoticeRedisMessage;
import cn.cordys.crm.system.notice.sse.SseService;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerReminderService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_NOTIFIED = "NOTIFIED";

    @Resource
    private BaseMapper<CustomerReminder> reminderMapper;

    @Resource
    private ExtCustomerReminderMapper extCustomerReminderMapper;

    @Resource
    private SseService sseService;

    @Resource
    private MessagePublisher messagePublisher;

    public CustomerReminder add(CustomerReminderAddRequest request, String userId, String orgId) {
        CustomerReminder reminder = new CustomerReminder();
        reminder.setCustomerId(request.getCustomerId());
        reminder.setCustomerName(request.getCustomerName());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setContent(request.getContent());
        reminder.setStatus(STATUS_PENDING);
        reminder.setOrganizationId(orgId);
        reminder.setReminderUserId(userId);
        reminder.setCreateUser(userId);
        reminder.setUpdateUser(userId);
        long now = System.currentTimeMillis();
        reminder.setCreateTime(now);
        reminder.setUpdateTime(now);
        reminderMapper.insert(reminder);
        return reminder;
    }

    public CustomerReminder update(CustomerReminderUpdateRequest request, String userId) {
        CustomerReminder existing = reminderMapper.selectByPrimaryKey(request.getId());
        if (existing == null || !existing.getReminderUserId().equals(userId)) {
            throw new RuntimeException("提醒不存在或无权限修改");
        }
        CustomerReminder reminder = new CustomerReminder();
        reminder.setId(request.getId());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setContent(request.getContent());
        reminder.setUpdateUser(userId);
        reminder.setUpdateTime(System.currentTimeMillis());
        reminderMapper.update(reminder);
        return reminder;
    }

    public void delete(String id, String userId) {
        extCustomerReminderMapper.deleteById(id, userId);
    }

    public List<CustomerReminderResponse> listByCustomerId(String customerId, String userId) {
        return extCustomerReminderMapper.listByCustomerId(customerId, userId);
    }

    public List<CustomerReminderResponse> listPending(String userId, String orgId) {
        return extCustomerReminderMapper.listPendingByUserId(userId, orgId);
    }

    public List<CustomerReminderResponse> listAll(String userId, String orgId) {
        return extCustomerReminderMapper.listAllByUserId(userId, orgId);
    }

    public CustomerReminderResponse getById(String id, String userId) {
        return extCustomerReminderMapper.getById(id, userId);
    }

    public int countPending(String userId, String orgId) {
        return extCustomerReminderMapper.countPendingByUserId(userId, orgId);
    }

    public void markAsNotified(String id) {
        extCustomerReminderMapper.updateStatus(id, STATUS_NOTIFIED);
    }

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        String orgId = OrganizationContext.getOrganizationId();
        if (orgId == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        List<CustomerReminder> dueReminders = extCustomerReminderMapper.getDueReminders(currentTime, orgId);
        for (CustomerReminder reminder : dueReminders) {
            sendReminderNotification(reminder);
            markAsNotified(reminder.getId());
        }
    }

    private void sendReminderNotification(CustomerReminder reminder) {
        NoticeRedisMessage noticeRedisMessage = new NoticeRedisMessage();
        noticeRedisMessage.setMessage(JSON.toJSONString(reminder));
        noticeRedisMessage.setNoticeType("REMINDER");
        messagePublisher.publish(TopicConstants.SSE_TOPIC, JSON.toJSONString(noticeRedisMessage));
        sseService.broadcastPeriodically(reminder.getReminderUserId(), "REMINDER");
    }
}
