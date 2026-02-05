package cn.cordys.crm.reminder.controller;

import cn.cordys.crm.reminder.dto.ReminderDTO;
import cn.cordys.crm.reminder.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提醒Controller
 *
 * @author jianxing
 * @date 2025-02-15
 */
@Tag(name = "提醒")
@RestController
@RequestMapping("/reminder")
public class ReminderController {

    @Resource
    private ReminderService reminderService;

    @PostMapping("/create")
    @Operation(summary = "创建提醒")
    public String createReminder(@Valid @RequestBody ReminderDTO reminderDTO) {
        return reminderService.createReminder(reminderDTO);
    }

    @PutMapping("/update")
    @Operation(summary = "更新提醒")
    public void updateReminder(@Valid @RequestBody ReminderDTO reminderDTO) {
        reminderService.updateReminder(reminderDTO);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询提醒")
    public ReminderDTO getReminderById(@PathVariable String id) {
        return reminderService.getReminderById(id);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有提醒")
    public List<ReminderDTO> getAllReminders() {
        return reminderService.getAllReminders();
    }

    @GetMapping("/unread")
    @Operation(summary = "查询未读提醒")
    public List<ReminderDTO> getUnreadReminders() {
        return reminderService.getUnreadReminders();
    }

    @GetMapping("/upcoming")
    @Operation(summary = "查询即将到期的提醒")
    public List<ReminderDTO> getUpcomingReminders() {
        return reminderService.getUpcomingReminders();
    }

    @PutMapping("/read/{id}")
    @Operation(summary = "标记为已读")
    public void markAsRead(@PathVariable String id) {
        reminderService.markAsRead(id);
    }

    @PutMapping("/batchRead")
    @Operation(summary = "批量标记为已读")
    public void batchMarkAsRead(@RequestBody List<String> ids) {
        reminderService.batchMarkAsRead(ids);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除提醒")
    public void deleteReminder(@PathVariable String id) {
        reminderService.deleteReminder(id);
    }

    @DeleteMapping("/batchDelete")
    @Operation(summary = "批量删除提醒")
    public void batchDeleteReminders(@RequestBody List<String> ids) {
        reminderService.batchDeleteReminders(ids);
    }

    @GetMapping("/countUnread")
    @Operation(summary = "统计未读提醒数量")
    public int countUnread() {
        return reminderService.countUnread();
    }
}
