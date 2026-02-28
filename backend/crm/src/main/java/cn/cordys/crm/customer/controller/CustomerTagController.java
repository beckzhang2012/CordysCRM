package cn.cordys.crm.customer.controller;

import cn.cordys.common.dto.BasePageRequest;
import cn.cordys.common.dto.CommonResult;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.service.CustomerTagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 客户标签控制器
 *
 * @author jianxing
 * @date 2026-01-21 10:24:22
 */
@Slf4j
@RestController
@RequestMapping("/api/customer/tag")
@Tag(name = "客户标签", description = "客户标签管理接口")
public class CustomerTagController {

    private final CustomerTagService customerTagService;

    public CustomerTagController(CustomerTagService customerTagService) {
        this.customerTagService = customerTagService;
    }

    @Operation(summary = "标签分页查询", description = "根据条件分页查询客户标签")
    @PostMapping("/page")
    public CommonResult<Page<CustomerTagResponse>> getTagPage(@RequestBody CustomerTagPageRequest request, HttpServletRequest httpRequest) {
        String organizationId = (String) httpRequest.getAttribute("organizationId");
        request.setOrganizationId(organizationId);

        Page<CustomerTagResponse> page = customerTagService.getTagPage(request);
        return CommonResult.success(page);
    }

    @Operation(summary = "获取标签列表", description = "获取客户标签列表")
    @GetMapping("/list")
    public CommonResult<List<CustomerTagResponse>> getTagList(
            @RequestParam(required = false) String category,
            HttpServletRequest httpRequest) {
        String organizationId = (String) httpRequest.getAttribute("organizationId");

        List<CustomerTagResponse> tagList = customerTagService.getTagList(organizationId, category);
        return CommonResult.success(tagList);
    }

    @Operation(summary = "获取标签详情", description = "根据ID获取客户标签详情")
    @GetMapping("/{id}")
    public CommonResult<CustomerTagResponse> getTagById(@PathVariable String id) {
        CustomerTagResponse tag = customerTagService.getTagById(id);
        return CommonResult.success(tag);
    }

    @Operation(summary = "创建标签", description = "创建新的客户标签")
    @PostMapping
    public CommonResult<String> createTag(@RequestBody CustomerTagAddRequest request, HttpServletRequest httpRequest) {
        String organizationId = (String) httpRequest.getAttribute("organizationId");
        String creator = (String) httpRequest.getAttribute("userId");

        String tagId = customerTagService.createTag(request, organizationId, creator);
        return CommonResult.success(tagId);
    }

    @Operation(summary = "更新标签", description = "更新客户标签信息")
    @PutMapping
    public CommonResult<Boolean> updateTag(@RequestBody CustomerTagUpdateRequest request) {
        boolean success = customerTagService.updateTag(request);
        return CommonResult.success(success);
    }

    @Operation(summary = "删除标签", description = "删除客户标签")
    @DeleteMapping("/{id}")
    public CommonResult<Boolean> deleteTag(@PathVariable String id) {
        boolean success = customerTagService.deleteTag(id);
        return CommonResult.success(success);
    }

    @Operation(summary = "为客户添加标签", description = "为指定客户添加标签")
    @PostMapping("/customer/{customerId}/add")
    public CommonResult<Boolean> addTagsToCustomer(
            @PathVariable String customerId,
            @RequestBody List<String> tagIds,
            HttpServletRequest httpRequest) {
        String organizationId = (String) httpRequest.getAttribute("organizationId");
        String adder = (String) httpRequest.getAttribute("userId");

        boolean success = customerTagService.addTagsToCustomer(customerId, tagIds, organizationId, adder);
        return CommonResult.success(success);
    }

    @Operation(summary = "从客户移除标签", description = "从指定客户移除标签")
    @PostMapping("/customer/{customerId}/remove")
    public CommonResult<Boolean> removeTagsFromCustomer(
            @PathVariable String customerId,
            @RequestBody List<String> tagIds) {
        boolean success = customerTagService.removeTagsFromCustomer(customerId, tagIds);
        return CommonResult.success(success);
    }

    @Operation(summary = "获取客户标签", description = "获取指定客户的标签列表")
    @GetMapping("/customer/{customerId}")
    public CommonResult<List<CustomerTagResponse>> getCustomerTags(@PathVariable String customerId) {
        List<CustomerTagResponse> tagList = customerTagService.getCustomerTags(customerId);
        return CommonResult.success(tagList);
    }

    @Operation(summary = "批量为客户添加标签", description = "批量为多个客户添加标签")
    @PostMapping("/batch/add")
    public CommonResult<Integer> batchAddTagsToCustomers(
            @RequestBody BatchTagRequest request,
            HttpServletRequest httpRequest) {
        String organizationId = (String) httpRequest.getAttribute("organizationId");
        String adder = (String) httpRequest.getAttribute("userId");

        int successCount = customerTagService.batchAddTagsToCustomers(
                request.getCustomerIds(), request.getTagIds(), organizationId, adder);
        return CommonResult.success(successCount);
    }

    @Operation(summary = "批量从客户移除标签", description = "批量从多个客户移除标签")
    @PostMapping("/batch/remove")
    public CommonResult<Integer> batchRemoveTagsFromCustomers(@RequestBody BatchTagRequest request) {
        int successCount = customerTagService.batchRemoveTagsFromCustomers(request.getCustomerIds(), request.getTagIds());
        return CommonResult.success(successCount);
    }

    @Operation(summary = "获取标签使用统计", description = "获取标签关联的客户数量")
    @GetMapping("/usage/{tagId}")
    public CommonResult<Integer> countCustomersByTagId(@PathVariable String tagId) {
        int count = customerTagService.countCustomersByTagId(tagId);
        return CommonResult.success(count);
    }

    /**
     * 批量标签请求参数
     */
    public static class BatchTagRequest {
        private List<String> customerIds;
        private List<String> tagIds;

        public List<String> getCustomerIds() {
            return customerIds;
        }

        public void setCustomerIds(List<String> customerIds) {
            this.customerIds = customerIds;
        }

        public List<String> getTagIds() {
            return tagIds;
        }

        public void setTagIds(List<String> tagIds) {
            this.tagIds = tagIds;
        }
    }
}