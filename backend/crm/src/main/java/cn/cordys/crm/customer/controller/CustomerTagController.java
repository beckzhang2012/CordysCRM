package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;

import cn.cordys.crm.customer.dto.request.CustomerTagRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagSearchRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.service.CustomerTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户标签")
@RestController
@RequestMapping("/customer-tag")
public class CustomerTagController {

    @Resource
    private CustomerTagService customerTagService;

    @GetMapping("/get/{customerId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取客户标签")
    public CustomerTagResponse getCustomerTags(@PathVariable String customerId) {
        return customerTagService.getCustomerTags(customerId);
    }

    @PostMapping("/save")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "保存客户标签")
    public void saveCustomerTags(@Validated @RequestBody CustomerTagRequest request) {
        customerTagService.saveCustomerTags(request);
    }

    @PostMapping("/delete")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "删除客户标签")
    public void deleteCustomerTags(@Validated @RequestBody CustomerTagRequest request) {
        customerTagService.deleteCustomerTags(request);
    }

    @PostMapping("/search")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "搜索标签")
    public List<String> searchTags(@Valid @RequestBody CustomerTagSearchRequest request) {
        return customerTagService.searchTags(request);
    }

    @GetMapping("/all")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取所有标签")
    public List<String> getAllTags() {
        return customerTagService.getAllTags();
    }

    @PostMapping("/get-customer-ids")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "根据标签获取客户ID列表")
    public List<String> getCustomerIdsByTags(@RequestBody List<String> tags) {
        return customerTagService.getCustomerIdsByTags(tags);
    }
}
