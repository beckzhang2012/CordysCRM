package cn.cordys.crm.reminder.service;

import cn.cordys.crm.reminder.domain.Reminder;
import cn.cordys.crm.reminder.dto.ReminderDTO;
import cn.cordys.crm.reminder.mapper.ExtReminderMapper;
import cn.cordys.common.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 提醒Service接口
 *
 * @author jianxing
 * @date 2025-02-15
 */
public interface ReminderService {

    /**
     * 创建提醒
     *
     * @param reminderDTO 提醒DTO
     * @return 提醒ID
     */
    String createReminder(ReminderDTO reminderDTO);

    /**
     * 更新提醒
     *
     * @param reminderDTO 提醒DTO
     */
    void updateReminder(ReminderDTO reminderDTO);

    /**
     * 根据ID查询提醒
     *
     * @param id 提醒ID
     * @return 提醒DTO
     */
    ReminderDTO getReminderById(String id);

    /**
     * 查询所有提醒
     *
     * @return 提醒列表
     */
    List<ReminderDTO> getAllReminders();

    /**
     * 查询未读提醒
     *
     * @return 未读提醒列表
     */
    List<ReminderDTO> getUnreadReminders();

    /**
     * 查询即将到期的提醒
     *
     * @return 即将到期的提醒列表
     */
    List<ReminderDTO> getUpcomingReminders();

    /**
     * 标记为已读
     *
     * @param id 提醒ID
     */
    void markAsRead(String id);

    /**
     * 批量标记为已读
     *
     * @param ids 提醒ID列表
     */
    void batchMarkAsRead(List<String> ids);

    /**
     * 删除提醒
     *
     * @param id 提醒ID
     */
    void deleteReminder(String id);

    /**
     * 批量删除提醒
     *
     * @param ids 提醒ID列表
     */
    void batchDeleteReminders(List<String> ids);

    /**
     * 统计未读提醒数量
     *
     * @return 未读提醒数量
     */
    int countUnread();
}
