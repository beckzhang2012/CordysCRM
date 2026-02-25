package cn.cordys.crm.reminder.controller;

import cn.cordys.common.dto.ApiResponse;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.reminder.domain.Reminder;
import cn.cordys.crm.reminder.dto.request.ReminderAddRequest;
import cn.cordys.crm.reminder.dto.request.ReminderPageRequest;
import cn.cordys.crm.reminder.dto.response.ReminderListResponse;
import cn.cordys.crm.reminder.service.ReminderService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "提醒管理")
@RestController
@RequestMapping("/reminder")
public class ReminderController {

    @Resource
    private ReminderService reminderService;

    @PostMapping("/add")
    @Operation(summary = "创建提醒")
    public Reminder add(@Validated @RequestBody ReminderAddRequest request) {
        return reminderService.add(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/page")
    @Operation(summary = "提醒列表")
    public PagerWithOption<List<ReminderListResponse>> list(@Validated @RequestBody ReminderPageRequest request) {
        return reminderService.list(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待处理提醒（用于通知）")
    public List<ReminderListResponse> getPendingReminders() {
        return reminderService.getPendingReminders(SessionUtils.getUserId(), OrganizationContext.getOrganizationId(), System.currentTimeMillis());
    }

    @GetMapping("/complete/{id}")
    @Operation(summary = "标记为已完成")
    public void complete(@PathVariable String id) {
        reminderService.complete(id);
    }

    @GetMapping("/cancel/{id}")
    @Operation(summary = "取消提醒")
    public void cancel(@PathVariable String id) {
        reminderService.cancel(id);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除提醒")
    public void delete(@PathVariable String id) {
        reminderService.delete(id);
    }
}
