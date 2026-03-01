package cn.cordys.crm.reminder.controller;

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
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Tag(name = "提醒")
@RequestMapping("/reminder")
public class ReminderController {

    @Resource
    private ReminderService reminderService;

    @PostMapping("/page")
    @Operation(summary = "提醒列表")
    public PagerWithOption<List<ReminderListResponse>> list(@Validated @RequestBody ReminderPageRequest request) {
        return reminderService.list(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "提醒详情")
    public Reminder get(@PathVariable String id) {
        return reminderService.get(id, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/add")
    @Operation(summary = "添加提醒")
    public Reminder add(@Validated @RequestBody ReminderAddRequest request) {
        return reminderService.add(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "删除提醒")
    public void delete(@PathVariable String id) {
        reminderService.delete(id, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/markAsRead/{id}")
    @Operation(summary = "标记为已读")
    public void markAsRead(@PathVariable String id) {
        reminderService.markAsRead(id, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/markAllAsRead")
    @Operation(summary = "全部标记为已读")
    public void markAllAsRead() {
        reminderService.markAllAsRead(SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/unreadCount")
    @Operation(summary = "未读提醒数量")
    public int getUnreadCount() {
        return reminderService.getUnreadCount(SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }
}
