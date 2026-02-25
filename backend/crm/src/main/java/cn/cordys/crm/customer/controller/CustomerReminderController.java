package cn.cordys.crm.customer.controller;

import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.dto.request.CustomerReminderAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerReminderUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerReminderResponse;
import cn.cordys.crm.customer.service.CustomerReminderService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户提醒")
@RestController
@RequestMapping("/account/reminder")
public class CustomerReminderController {

    @Resource
    private CustomerReminderService customerReminderService;

    @PostMapping("/add")
    @Operation(summary = "添加客户提醒")
    public CustomerReminder add(@Valid @RequestBody CustomerReminderAddRequest request) {
        return customerReminderService.add(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新客户提醒")
    public CustomerReminder update(@Valid @RequestBody CustomerReminderUpdateRequest request) {
        return customerReminderService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除客户提醒")
    public void delete(@PathVariable String id) {
        customerReminderService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/list/{customerId}")
    @Operation(summary = "获取客户提醒列表")
    public List<CustomerReminderResponse> listByCustomerId(@PathVariable String customerId) {
        return customerReminderService.listByCustomerId(customerId, SessionUtils.getUserId());
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待提醒列表")
    public List<CustomerReminderResponse> listPending() {
        return customerReminderService.listPending(SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有提醒列表")
    public List<CustomerReminderResponse> listAll() {
        return customerReminderService.listAll(SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取提醒详情")
    public CustomerReminderResponse getById(@PathVariable String id) {
        return customerReminderService.getById(id, SessionUtils.getUserId());
    }

    @GetMapping("/count")
    @Operation(summary = "获取待提醒数量")
    public int countPending() {
        return customerReminderService.countPending(SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }
}
