package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.Pager;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagBatchAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.service.CustomerTagService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户标签控制器
 *
 * @author cordys
 * @date 2025-02-03
 */
@Tag(name = "客户标签")
@RestController
@RequestMapping("/account/tag")
@Validated
public class CustomerTagController {

    @Resource
    private CustomerTagService customerTagService;

    @PostMapping("/add")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "添加标签")
    public String add(@Valid @RequestBody CustomerTagAddRequest request) {
        return customerTagService.add(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/update")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "更新标签")
    public void update(@Valid @RequestBody CustomerTagUpdateRequest request) {
        customerTagService.update(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "删除标签")
    public void delete(@PathVariable String id) {
        customerTagService.delete(id, OrganizationContext.getOrganizationId());
    }

    @PostMapping("/page")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "标签分页列表")
    public Pager<List<CustomerTagResponse>> list(@RequestBody CustomerTagPageRequest request) {
        return customerTagService.list(request, OrganizationContext.getOrganizationId());
    }

    @GetMapping("/list-all")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "查询所有标签")
    public List<CustomerTagResponse> listAll(
            @RequestParam(required = false) String name) {
        return customerTagService.listAll(OrganizationContext.getOrganizationId(), name);
    }

    @GetMapping("/get-by-customer/{customerId}")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "获取客户的标签列表")
    public List<CustomerTagResponse> getTagsByCustomerId(
            @PathVariable @NotBlank(message = "{customer.id.not_blank}") String customerId) {
        return customerTagService.getTagsByCustomerId(customerId);
    }

    @PostMapping("/batch-add-to-customer")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "批量添加标签到客户")
    public void batchAddTagsToCustomer(@Valid @RequestBody CustomerTagBatchAddRequest request) {
        customerTagService.batchAddTagsToCustomer(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch-remove-from-customer")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "批量删除客户的标签")
    public void batchRemoveTagsFromCustomer(
            @RequestParam @NotBlank(message = "{customer.id.not_blank}") String customerId,
            @RequestParam List<String> tagIds) {
        customerTagService.batchRemoveTagsFromCustomer(customerId, tagIds);
    }

    @PostMapping("/set-customer-tags")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "设置客户的标签")
    public void setCustomerTags(
            @RequestParam @NotBlank(message = "{customer.id.not_blank}") String customerId,
            @RequestParam List<String> tagIds) {
        customerTagService.setCustomerTags(customerId, tagIds, SessionUtils.getUserId());
    }
}
