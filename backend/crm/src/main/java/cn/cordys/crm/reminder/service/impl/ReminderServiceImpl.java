package cn.cordys.crm.reminder.service.impl;

import cn.cordys.crm.reminder.domain.Reminder;
import cn.cordys.crm.reminder.dto.ReminderDTO;
import cn.cordys.crm.reminder.mapper.ExtReminderMapper;
import cn.cordys.crm.reminder.service.ReminderService;
import cn.cordys.common.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 提醒Service实现类
 *
 * @author jianxing
 * @date 2025-02-15
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ReminderServiceImpl implements ReminderService {

    @Resource
    private ExtReminderMapper extReminderMapper;

    @Resource
    private IDGenerator idGenerator;

    @Override
    public String createReminder(ReminderDTO reminderDTO) {
        Reminder reminder = new Reminder();
        reminder.setId(idGenerator.generate());
        reminder.setSourceId(reminderDTO.getSourceId());
        reminder.setSourceName(reminderDTO.getSourceName());
        reminder.setRemindTime(reminderDTO.getRemindTime());
        reminder.setRemindContent(reminderDTO.getRemindContent());
        reminder.setIsRead(false);
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());
        
        extReminderMapper.insert(reminder);
        return reminder.getId();
    }

    @Override
    public void updateReminder(ReminderDTO reminderDTO) {
        Reminder reminder = extReminderMapper.selectById(reminderDTO.getId());
        if (reminder != null) {
            reminder.setSourceId(reminderDTO.getSourceId());
            reminder.setSourceName(reminderDTO.getSourceName());
            reminder.setRemindTime(reminderDTO.getRemindTime());
            reminder.setRemindContent(reminderDTO.getRemindContent());
            reminder.setUpdatedAt(LocalDateTime.now());
            
            extReminderMapper.update(reminder);
        }
    }

    @Override
    public ReminderDTO getReminderById(String id) {
        Reminder reminder = extReminderMapper.selectById(id);
        if (reminder != null) {
            return convertToDTO(reminder);
        }
        return null;
    }

    @Override
    public List<ReminderDTO> getAllReminders() {
        List<Reminder> reminders = extReminderMapper.selectAll();
        return reminders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReminderDTO> getUnreadReminders() {
        List<Reminder> reminders = extReminderMapper.selectUnread();
        return reminders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReminderDTO> getUpcomingReminders() {
        List<Reminder> reminders = extReminderMapper.selectUpcoming(LocalDateTime.now());
        return reminders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(String id) {
        extReminderMapper.markAsRead(id);
    }

    @Override
    public void batchMarkAsRead(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            extReminderMapper.batchMarkAsRead(ids);
        }
    }

    @Override
    public void deleteReminder(String id) {
        extReminderMapper.deleteById(id);
    }

    @Override
    public void batchDeleteReminders(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            extReminderMapper.batchDeleteByIds(ids);
        }
    }

    @Override
    public int countUnread() {
        return extReminderMapper.countUnread();
    }

    private ReminderDTO convertToDTO(Reminder reminder) {
        ReminderDTO dto = new ReminderDTO();
        dto.setId(reminder.getId());
        dto.setSourceId(reminder.getSourceId());
        dto.setSourceName(reminder.getSourceName());
        dto.setRemindTime(reminder.getRemindTime());
        dto.setRemindContent(reminder.getRemindContent());
        dto.setIsRead(reminder.getIsRead());
        dto.setCreatedAt(reminder.getCreatedAt());
        dto.setUpdatedAt(reminder.getUpdatedAt());
        return dto;
    }
}
