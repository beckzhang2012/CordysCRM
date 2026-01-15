package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.response.CrmResponse;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.request.CustomerTagRelationRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagSaveRequest;
import cn.cordys.crm.customer.service.CustomerTagService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户标签控制器
 *
 * @author jianxing
 * @date 2025-01-15
 */
@Tag(name = "客户标签")
@RestController
@RequestMapping("/account/tag")
public class CustomerTagController {

    @Resource
    private CustomerTagService customerTagService;

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取标签列表")
    public CrmResponse<List<CustomerTag>> list() {
        return customerTagService.list(OrganizationContext.getOrganizationId());
    }

    @GetMapping("/list/{customerId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取客户的标签列表")
    public CrmResponse<List<CustomerTag>> listByCustomerId(@PathVariable String customerId) {
        return customerTagService.listByCustomerId(customerId);
    }

    @PostMapping("/save")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "保存标签")
    public CrmResponse<CustomerTag> save(@Valid @RequestBody CustomerTagSaveRequest request) {
        return customerTagService.save(request, OrganizationContext.getOrganizationId(), SessionUtils.getUserId());
    }

    @DeleteMapping("/delete/{tagId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "删除标签")
    public CrmResponse<Void> delete(@PathVariable String tagId) {
        return customerTagService.delete(tagId, OrganizationContext.getOrganizationId());
    }

    @PostMapping("/updateCustomerTags")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "更新客户标签")
    public CrmResponse<Void> updateCustomerTags(@Valid @RequestBody CustomerTagRelationRequest request) {
        return customerTagService.updateCustomerTags(request, OrganizationContext.getOrganizationId());
    }

    @GetMapping("/customers/{tagId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "根据标签ID获取客户ID列表")
    public CrmResponse<List<String>> getCustomerIdsByTagId(@PathVariable String tagId) {
        return customerTagService.getCustomerIdsByTagId(tagId);
    }
}
