package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.crm.customer.dto.request.CustomerReminderAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerReminderUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerReminderListResponse;
import cn.cordys.crm.customer.service.CustomerReminderService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户跟进提醒Controller
 */
@Tag(name = "客户跟进提醒")
@RestController
@RequestMapping("/account/reminder")
public class CustomerReminderController {

    @Resource
    private CustomerReminderService customerReminderService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "添加提醒")
    public void addReminder(@Valid @RequestBody CustomerReminderAddRequest request) {
        customerReminderService.addReminder(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "更新提醒状态")
    public void updateReminder(@Valid @RequestBody CustomerReminderUpdateRequest request) {
        customerReminderService.updateReminder(request, SessionUtils.getUserId());
    }

    @DeleteMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "删除提醒")
    public void deleteReminder(@PathVariable String id) {
        customerReminderService.deleteReminder(id);
    }

    @GetMapping("/my-list")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取我的提醒列表")
    public List<CustomerReminderListResponse> getMyReminders() {
        return customerReminderService.getMyReminders(SessionUtils.getUserId());
    }

    @GetMapping("/customer-list/{customerId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取客户的提醒列表")
    public List<CustomerReminderListResponse> getCustomerReminders(@PathVariable String customerId) {
        return customerReminderService.getCustomerReminders(customerId);
    }

    @GetMapping("/count-pending")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "统计待处理提醒数量")
    public Integer countPendingReminders() {
        return customerReminderService.countPendingReminders(SessionUtils.getUserId());
    }

    @GetMapping("/pending")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取待处理的提醒（用于推送）")
    public List<CustomerReminderListResponse> getPendingReminders() {
        return customerReminderService.getPendingReminders();
    }
}