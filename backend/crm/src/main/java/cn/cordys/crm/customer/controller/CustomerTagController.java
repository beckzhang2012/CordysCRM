package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.dto.OptionDTO;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.request.TagAddRequest;
import cn.cordys.crm.customer.dto.request.TagRelationRequest;
import cn.cordys.crm.customer.dto.request.TagUpdateRequest;
import cn.cordys.crm.customer.service.CustomerTagService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户标签控制器
 *
 * @author jianxing
 * @date 2026-01-21 10:00:00
 */
@Tag(name = "客户标签")
@RestController
@RequestMapping("/account/tag")
public class CustomerTagController {

    @Resource
    private CustomerTagService customerTagService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_ADD)
    @Operation(summary = "新增标签")
    public CustomerTag addTag(@Valid @RequestBody TagAddRequest request) {
        return customerTagService.addTag(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "更新标签")
    public CustomerTag updateTag(@Valid @RequestBody TagUpdateRequest request) {
        return customerTagService.updateTag(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_DELETE)
    @Operation(summary = "删除标签")
    public void deleteTag(@PathVariable String id) {
        customerTagService.deleteTag(id, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "标签详情")
    public CustomerTag getTag(@PathVariable String id) {
        return customerTagService.getTag(id);
    }

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "标签列表")
    public List<CustomerTag> getTagList() {
        return customerTagService.getTagList(OrganizationContext.getOrganizationId());
    }

    @PostMapping("/customer/add")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "为客户添加标签")
    public void addCustomerTags(@Valid @RequestBody TagRelationRequest request) {
        customerTagService.addCustomerTags(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/customer/remove")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "为客户移除标签")
    public void removeCustomerTags(@Valid @RequestBody TagRelationRequest request) {
        customerTagService.removeCustomerTags(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/customer/list/{customerId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取客户的标签列表")
    public List<CustomerTag> getCustomerTags(@PathVariable String customerId) {
        return customerTagService.getCustomerTags(customerId);
    }
}
