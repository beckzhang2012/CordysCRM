package cn.cordys.crm.customer.service;

import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.BeanUtils;
import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.dto.request.CustomerReminderAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerReminderUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerReminderListResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerReminderMapper;
import cn.cordys.crm.system.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户跟进提醒Service
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerReminderService {

    @Resource
    private ExtCustomerReminderMapper extCustomerReminderMapper;

    @Resource
    private UserService userService;

    @Resource
    private IDGenerator idGenerator;

    public void addReminder(CustomerReminderAddRequest request, String userId) {
        CustomerReminder reminder = new CustomerReminder();
        reminder.setId(idGenerator.nextId());
        reminder.setCustomerId(request.getCustomerId());
        reminder.setCustomerName(request.getCustomerName());
        reminder.setUserId(userId);
        reminder.setUserName(userService.getUserName(userId));
        reminder.setReminderTime(request.getReminderTime());
        reminder.setContent(StringUtils.trimToNull(request.getContent()));
        reminder.setStatus(0);
        reminder.setCreateUser(userId);
        extCustomerReminderMapper.insert(reminder);
    }

    public void updateReminder(CustomerReminderUpdateRequest request, String userId) {
        CustomerReminder reminder = new CustomerReminder();
        reminder.setId(request.getId());
        reminder.setStatus(request.getStatus());
        reminder.setUpdateUser(userId);
        extCustomerReminderMapper.update(reminder);
    }

    public void deleteReminder(String id) {
        extCustomerReminderMapper.delete(id);
    }

    public List<CustomerReminderListResponse> getMyReminders(String userId) {
        return extCustomerReminderMapper.selectByUserId(userId);
    }

    public List<CustomerReminderListResponse> getCustomerReminders(String customerId) {
        return extCustomerReminderMapper.selectByCustomerId(customerId);
    }

    public List<CustomerReminderListResponse> getPendingReminders() {
        return extCustomerReminderMapper.selectPendingReminders(LocalDateTime.now());
    }

    public int countPendingReminders(String userId) {
        return extCustomerReminderMapper.countPendingByUserId(userId);
    }
}