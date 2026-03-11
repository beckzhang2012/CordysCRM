package cn.cordys.crm.customer.service;

import cn.cordys.common.dto.OptionDTO;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.BeanUtils;
import cn.cordys.common.util.Translator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.constants.CustomerReminderStatus;
import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.dto.request.CustomerReminderAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerReminderUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerReminderListResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerMapper;
import cn.cordys.crm.customer.mapper.ExtCustomerReminderMapper;
import cn.cordys.crm.system.mapper.ExtUserMapper;
import cn.cordys.mybatis.BaseMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 客户提醒服务
 *
 * @author system
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerReminderService {

    @Resource
    private BaseMapper<CustomerReminder> customerReminderMapper;

    @Resource
    private ExtCustomerReminderMapper extCustomerReminderMapper;

    @Resource
    private ExtCustomerMapper extCustomerMapper;

    @Resource
    private ExtUserMapper extUserMapper;

    /**
     * 创建提醒
     *
     * @param request 请求
     * @param userId  当前用户ID
     * @return 提醒实体
     */
    public CustomerReminder create(CustomerReminderAddRequest request, String userId) {
        CustomerReminder reminder = BeanUtils.copyBean(new CustomerReminder(), request);
        reminder.setId(IDGenerator.nextStr());
        reminder.setCreateTime(System.currentTimeMillis());
        reminder.setUpdateTime(System.currentTimeMillis());
        reminder.setCreateUser(userId);
        reminder.setUpdateUser(userId);
        reminder.setOrganizationId(OrganizationContext.getOrganizationId());
        reminder.setStatus(CustomerReminderStatus.PENDING.name());

        if (StringUtils.isBlank(reminder.getOwner())) {
            reminder.setOwner(userId);
        }

        customerReminderMapper.insert(reminder);
        return reminder;
    }

    /**
     * 更新提醒
     *
     * @param request 请求
     * @param userId  当前用户ID
     * @return 提醒实体
     */
    public CustomerReminder update(CustomerReminderUpdateRequest request, String userId) {
        CustomerReminder existing = customerReminderMapper.selectByPrimaryKey(request.getId());
        if (existing == null) {
            throw new GenericException(Translator.get("reminder_not_found"));
        }

        // 只有待提醒的才能更新
        if (!CustomerReminderStatus.PENDING.name().equals(existing.getStatus())) {
            throw new GenericException(Translator.get("reminder_cannot_update"));
        }

        CustomerReminder reminder = BeanUtils.copyBean(new CustomerReminder(), request);
        reminder.setUpdateTime(System.currentTimeMillis());
        reminder.setUpdateUser(userId);

        customerReminderMapper.update(reminder);
        return customerReminderMapper.selectByPrimaryKey(request.getId());
    }

    /**
     * 删除提醒
     *
     * @param id 提醒ID
     */
    public void delete(String id) {
        CustomerReminder reminder = customerReminderMapper.selectByPrimaryKey(id);
        if (reminder == null) {
            throw new GenericException(Translator.get("reminder_not_found"));
        }
        customerReminderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 取消提醒
     *
     * @param id     提醒ID
     * @param userId 当前用户ID
     */
    public void dismiss(String id, String userId) {
        CustomerReminder reminder = customerReminderMapper.selectByPrimaryKey(id);
        if (reminder == null) {
            throw new GenericException(Translator.get("reminder_not_found"));
        }

        if (!CustomerReminderStatus.PENDING.name().equals(reminder.getStatus())) {
            throw new GenericException(Translator.get("reminder_cannot_dismiss"));
        }

        CustomerReminder update = new CustomerReminder();
        update.setId(id);
        update.setStatus(CustomerReminderStatus.DISMISSED.name());
        update.setUpdateTime(System.currentTimeMillis());
        update.setUpdateUser(userId);
        customerReminderMapper.update(update);
    }

    /**
     * 根据客户ID查询提醒列表
     *
     * @param customerId 客户ID
     * @param owner      负责人（可选）
     * @return 提醒列表
     */
    public List<CustomerReminderListResponse> getByCustomerId(String customerId, String owner) {
        List<CustomerReminder> reminders = extCustomerReminderMapper.selectByCustomerId(customerId, owner);
        return convertToResponseList(reminders);
    }

    /**
     * 查询用户的提醒列表
     *
     * @param owner    负责人
     * @param status   状态（可选）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Pager<List<CustomerReminderListResponse>> getByOwner(String owner, String status, int pageNum, int pageSize) {
        var page = PageHelper.startPage(pageNum, pageSize);
        List<CustomerReminder> reminders = extCustomerReminderMapper.selectByOwner(owner, status);
        List<CustomerReminderListResponse> responseList = convertToResponseList(reminders);
        return PageUtils.setPageInfo(page, responseList);
    }

    /**
     * 查询待触发的提醒
     *
     * @param currentTime 当前时间戳
     * @return 待触发的提醒列表
     */
    public List<CustomerReminder> getPendingReminders(Long currentTime) {
        return extCustomerReminderMapper.selectPendingReminders(currentTime);
    }

    /**
     * 批量更新提醒状态
     *
     * @param ids    ID列表
     * @param status 新状态
     * @param userId 更新人
     * @return 更新数量
     */
    public int batchUpdateStatus(List<String> ids, String status, String userId) {
        return extCustomerReminderMapper.batchUpdateStatus(ids, status, System.currentTimeMillis(), userId);
    }

    /**
     * 转换为响应列表
     */
    private List<CustomerReminderListResponse> convertToResponseList(List<CustomerReminder> reminders) {
        if (CollectionUtils.isEmpty(reminders)) {
            return Collections.emptyList();
        }

        List<String> customerIds = reminders.stream()
                .map(CustomerReminder::getCustomerId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        List<String> userIds = reminders.stream()
                .flatMap(r -> Stream.of(r.getOwner(), r.getCreateUser()))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        final Map<String, String> customerNameMap;
        if (CollectionUtils.isNotEmpty(customerIds)) {
            customerNameMap = extCustomerMapper.selectOptionByIds(customerIds)
                    .stream()
                    .collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
        } else {
            customerNameMap = Collections.emptyMap();
        }

        final Map<String, String> userNameMap;
        if (CollectionUtils.isNotEmpty(userIds)) {
            userNameMap = extUserMapper.selectUserOptionByIds(userIds)
                    .stream()
                    .collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
        } else {
            userNameMap = Collections.emptyMap();
        }

        return reminders.stream()
                .map(r -> {
                    CustomerReminderListResponse resp = BeanUtils.copyBean(new CustomerReminderListResponse(), r);
                    resp.setCustomerName(customerNameMap.get(r.getCustomerId()));
                    resp.setOwnerName(userNameMap.get(r.getOwner()));
                    resp.setCreateUserName(userNameMap.get(r.getCreateUser()));
                    return resp;
                })
                .collect(Collectors.toList());
    }
}
