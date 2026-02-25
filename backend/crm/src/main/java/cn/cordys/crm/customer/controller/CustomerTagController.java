package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.Pager;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagRelationRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.service.CustomerTagService;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户标签管理")
@RestController
@RequestMapping("/account/tag")
public class CustomerTagController {
    @Resource
    private CustomerTagService customerTagService;

    @GetMapping("/page")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "标签分页列表")
    public Pager<CustomerTagResponse> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return customerTagService.list(keyword, current, pageSize, OrganizationContext.getOrganizationId());
    }

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "所有标签列表")
    public List<CustomerTagResponse> listAll() {
        return customerTagService.listAll(OrganizationContext.getOrganizationId());
    }

    @GetMapping("/customer/{customerId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取客户的标签")
    public List<CustomerTagResponse> getByCustomerId(@PathVariable String customerId) {
        return customerTagService.getByCustomerId(customerId, OrganizationContext.getOrganizationId());
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "添加标签")
    public CustomerTag add(@Validated @RequestBody CustomerTagAddRequest request) {
        return customerTagService.add(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "更新标签")
    public CustomerTag update(@Validated @RequestBody CustomerTagUpdateRequest request) {
        return customerTagService.update(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "删除标签")
    public void delete(@PathVariable String id) {
        customerTagService.delete(id, OrganizationContext.getOrganizationId());
    }

    @PostMapping("/customer/set")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "设置客户标签")
    public void setCustomerTags(@Validated @RequestBody CustomerTagRelationRequest request) {
        customerTagService.setCustomerTags(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }
}
