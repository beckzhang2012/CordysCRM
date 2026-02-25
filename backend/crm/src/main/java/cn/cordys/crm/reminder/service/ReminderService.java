package cn.cordys.crm.reminder.service;

import cn.cordys.common.dto.OptionDTO;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.service.BaseService;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.BeanUtils;
import cn.cordys.crm.reminder.constants.ReminderStatus;
import cn.cordys.crm.reminder.domain.Reminder;
import cn.cordys.crm.reminder.dto.request.ReminderAddRequest;
import cn.cordys.crm.reminder.dto.request.ReminderPageRequest;
import cn.cordys.crm.reminder.dto.response.ReminderListResponse;
import cn.cordys.crm.reminder.mapper.ExtReminderMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
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
    private BaseService baseService;

    public Reminder add(ReminderAddRequest request, String userId, String orgId) {
        Reminder reminder = BeanUtils.copyBean(new Reminder(), request);
        reminder.setId(IDGenerator.nextStr());
        reminder.setOrganizationId(orgId);
        reminder.setOwner(userId);
        reminder.setStatus(ReminderStatus.PENDING.name());
        reminder.setCreateTime(System.currentTimeMillis());
        reminder.setUpdateTime(System.currentTimeMillis());
        reminder.setCreateUser(userId);
        reminder.setUpdateUser(userId);
        reminderMapper.insert(reminder);
        return reminder;
    }

    public PagerWithOption<List<ReminderListResponse>> list(ReminderPageRequest request, String userId, String orgId) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<ReminderListResponse> list = extReminderMapper.selectList(request, userId, orgId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> userIds = list.stream().map(ReminderListResponse::getOwner).distinct().toList();
            Map<String, String> userNameMap = baseService.getUserNameMap(userIds);
            list.forEach(item -> item.setOwnerName(userNameMap.get(item.getOwner())));
        }
        return PageUtils.setPageInfo(page, list);
    }

    public List<ReminderListResponse> getPendingReminders(String userId, String orgId, Long currentTime) {
        LambdaQueryWrapper<Reminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reminder::getOwner, userId);
        queryWrapper.eq(Reminder::getOrganizationId, orgId);
        queryWrapper.eq(Reminder::getStatus, ReminderStatus.PENDING.name());
        queryWrapper.le(Reminder::getRemindTime, currentTime);
        queryWrapper.orderByAsc(Reminder::getRemindTime);
        List<Reminder> reminders = reminderMapper.selectListByLambda(queryWrapper);
        return reminders.stream().map(r -> {
            ReminderListResponse response = BeanUtils.copyBean(new ReminderListResponse(), r);
            if (response != null) {
                response.setBusinessName(getBusinessName(r.getBusinessType(), r.getBusinessId()));
            }
            return response;
        }).filter(r -> r != null).toList();
    }

    private String getBusinessName(String businessType, String businessId) {
        if (businessId == null) {
            return null;
        }
        switch (businessType) {
            case "CUSTOMER" -> {
                return baseService.getCustomerMap(List.of(businessId)).get(businessId);
            }
            case "OPPORTUNITY" -> {
                return baseService.getOpportunityMap(List.of(businessId)).get(businessId);
            }
            case "CLUE" -> {
                return baseService.getClueMap(List.of(businessId)).get(businessId);
            }
            default -> {
                return null;
            }
        }
    }

    public void complete(String id) {
        Reminder reminder = new Reminder();
        reminder.setId(id);
        reminder.setStatus(ReminderStatus.COMPLETED.name());
        reminder.setUpdateTime(System.currentTimeMillis());
        reminderMapper.updateById(reminder);
    }

    public void cancel(String id) {
        Reminder reminder = new Reminder();
        reminder.setId(id);
        reminder.setStatus(ReminderStatus.CANCELLED.name());
        reminder.setUpdateTime(System.currentTimeMillis());
        reminderMapper.updateById(reminder);
    }

    public void delete(String id) {
        reminderMapper.deleteById(id);
    }
}
