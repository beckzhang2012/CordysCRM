package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.Pager;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagRelationRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagListResponse;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
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
 * @date 2025-01-30 16:24:22
 */
@Tag(name = "客户标签")
@RestController
@RequestMapping("/account/tag")
public class CustomerTagController {

    @Resource
    private CustomerTagService customerTagService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "添加标签")
    public String add(@Valid @RequestBody CustomerTagAddRequest request) {
        return customerTagService.addTag(request, OrganizationContext.getOrganizationId(), SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "更新标签")
    public boolean update(@Valid @RequestBody CustomerTagUpdateRequest request) {
        return customerTagService.updateTag(request);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "删除标签")
    public boolean delete(@PathVariable String id) {
        return customerTagService.deleteTag(id);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "标签列表")
    public Pager<List<CustomerTagResponse>> list(@Valid @RequestBody CustomerTagPageRequest request) {
        return customerTagService.getTagList(request, OrganizationContext.getOrganizationId());
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "标签详情")
    public CustomerTagResponse get(@PathVariable String id) {
        return customerTagService.getTag(id);
    }

    @PostMapping("/relate")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "关联客户标签")
    public boolean relateCustomerTags(@Valid @RequestBody CustomerTagRelationRequest request) {
        return customerTagService.relateCustomerTags(request, OrganizationContext.getOrganizationId(), SessionUtils.getUserId());
    }

    @GetMapping("/customer/{customerId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取客户的标签列表")
    public CustomerTagListResponse getCustomerTags(@PathVariable String customerId) {
        return customerTagService.getCustomerTags(customerId);
    }

    @GetMapping("/customers/{tagId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "根据标签ID获取客户ID列表")
    public List<String> getCustomerIdsByTagId(@PathVariable String tagId) {
        return customerTagService.getCustomerIdsByTagId(tagId, OrganizationContext.getOrganizationId());
    }
}