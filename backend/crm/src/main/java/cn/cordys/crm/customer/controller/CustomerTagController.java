package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.response.ListResult;
import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagBindRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagQueryRequest;
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
 * @author jianxing
 * @date 2025-02-26
 */
@Tag(name = "客户标签")
@RestController
@RequestMapping("/account/tag")
@Validated
public class CustomerTagController {

    @Resource
    private CustomerTagService customerTagService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "添加标签")
    public String add(@Valid @RequestBody CustomerTagAddRequest request) {
        return customerTagService.add(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "更新标签")
    public void update(@Valid @RequestBody CustomerTagUpdateRequest request) {
        customerTagService.update(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "删除标签")
    public void delete(@PathVariable @NotBlank(message = "{customer.tag.id.not_blank}") String id) {
        customerTagService.delete(id, SessionUtils.getUserId());
    }

    @PostMapping("/list")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "获取标签列表")
    public ListResult<List<CustomerTagResponse>> list(@RequestBody CustomerTagQueryRequest request) {
        return ListResult.ok(customerTagService.list(request, OrganizationContext.getOrganizationId()));
    }

    @GetMapping("/list/{customerId}")
    @RequiresPermissions(value = {PermissionConstants.CUSTOMER_MANAGEMENT_READ, PermissionConstants.CUSTOMER_MANAGEMENT_POOL_READ}, logical = Logical.OR)
    @Operation(summary = "获取客户的标签列表")
    public ListResult<List<CustomerTagResponse>> listByCustomerId(
            @PathVariable @NotBlank(message = "{customer.id.not_blank}") String customerId) {
        return ListResult.ok(customerTagService.listByCustomerId(customerId));
    }

    @PostMapping("/bind")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "绑定标签到客户")
    public void bindTags(@Valid @RequestBody CustomerTagBindRequest request) {
        customerTagService.bindTags(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/add-to-customer")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "添加标签到客户")
    public void addTagToCustomer(
            @RequestParam @NotBlank(message = "{customer.id.not_blank}") String customerId,
            @RequestParam @NotBlank(message = "{customer.tag.id.not_blank}") String tagId) {
        customerTagService.addTagToCustomer(customerId, tagId, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/remove-from-customer")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "从客户移除标签")
    public void removeTagFromCustomer(
            @RequestParam @NotBlank(message = "{customer.id.not_blank}") String customerId,
            @RequestParam @NotBlank(message = "{customer.tag.id.not_blank}") String tagId) {
        customerTagService.removeTagFromCustomer(customerId, tagId);
    }
}
