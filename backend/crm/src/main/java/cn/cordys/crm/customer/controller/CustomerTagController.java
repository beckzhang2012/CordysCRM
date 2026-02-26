package cn.cordys.crm.customer.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagBindRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.service.CustomerTagService;
import cn.cordys.security.SessionUtils;
import com.github.pagehelper.Page;
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
@RequestMapping("/account/tag")
public class CustomerTagController {

    @Resource
    private CustomerTagService customerTagService;

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

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "标签列表分页")
    public Page<CustomerTagResponse> page(@Validated @RequestBody CustomerTagPageRequest request) {
        return customerTagService.list(request, OrganizationContext.getOrganizationId());
    }

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取所有标签")
    public List<CustomerTagResponse> listAll() {
        return customerTagService.listAll(OrganizationContext.getOrganizationId());
    }

    @GetMapping("/customer/{customerId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_READ)
    @Operation(summary = "获取客户的标签列表")
    public List<CustomerTagResponse> getCustomerTags(@PathVariable String customerId) {
        return customerTagService.getTagsByCustomerId(customerId);
    }

    @PostMapping("/bind")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "绑定标签到客户")
    public void bindTags(@Validated @RequestBody CustomerTagBindRequest request) {
        customerTagService.bindTags(request, SessionUtils.getUserId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/unbind/{customerId}/{tagId}")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_UPDATE)
    @Operation(summary = "解绑客户标签")
    public void unbindTag(@PathVariable String customerId, @PathVariable String tagId) {
        customerTagService.unbindTag(customerId, tagId, OrganizationContext.getOrganizationId());
    }
}
