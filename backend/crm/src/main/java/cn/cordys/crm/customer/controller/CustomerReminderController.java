package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.Pager;
import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.dto.request.CustomerReminderAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerReminderPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerReminderUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerReminderListResponse;
import cn.cordys.crm.customer.service.CustomerReminderService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户提醒")
@RestController
@RequestMapping("/account/customer/reminder")
public class CustomerReminderController {

    @Resource
    private CustomerReminderService customerReminderService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "创建客户提醒")
    public CustomerReminder add(@Validated @RequestBody CustomerReminderAddRequest request) {
        return customerReminderService.create(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "更新客户提醒")
    public CustomerReminder update(@Validated @RequestBody CustomerReminderUpdateRequest request) {
        return customerReminderService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "删除客户提醒")
    public void delete(@PathVariable String id) {
        customerReminderService.delete(id);
    }

    @GetMapping("/dismiss/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "取消客户提醒")
    public void dismiss(@PathVariable String id) {
        customerReminderService.dismiss(id, SessionUtils.getUserId());
    }

    @GetMapping("/list/customer/{customerId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "根据客户ID查询提醒列表")
    public List<CustomerReminderListResponse> getByCustomerId(@PathVariable String customerId) {
        return customerReminderService.getByCustomerId(customerId, SessionUtils.getUserId());
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "查询当前用户的提醒列表（分页）")
    public Pager<List<CustomerReminderListResponse>> page(@Validated @RequestBody CustomerReminderPageRequest request) {
        return customerReminderService.getByOwner(
                SessionUtils.getUserId(),
                request.getStatus(),
                request.getCurrent(),
                request.getPageSize()
        );
    }
}
