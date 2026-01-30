package cn.cordys.crm.customer.controller;

import cn.cordys.common.dto.OptionDTO;
import cn.cordys.context.OrganizationContext;
import cn.cordys.security.SessionUtils;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.service.CustomerTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户标签管理")
@RestController
@RequestMapping("/customerTag")
public class CustomerTagController {

    @Resource
    private CustomerTagService customerTagService;

    @Operation(summary = "查询标签列表")
    @GetMapping("/list")
    public List<CustomerTag> list(@RequestParam(required = false) String keyword) {
        return customerTagService.list(keyword, OrganizationContext.getOrganizationId());
    }

    @Operation(summary = "查询标签详情")
    @GetMapping("/{id}")
    public CustomerTag getById(@PathVariable String id) {
        return customerTagService.getById(id);
    }

    @Operation(summary = "新增标签")
    @PostMapping
    public void add(@RequestBody CustomerTag customerTag) {
        customerTagService.add(customerTag, OrganizationContext.getOrganizationId());
    }

    @Operation(summary = "更新标签")
    @PutMapping
    public void update(@RequestBody CustomerTag customerTag) {
        customerTagService.update(customerTag, OrganizationContext.getOrganizationId());
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        customerTagService.delete(id);
    }

    @Operation(summary = "为客户添加标签")
    @PostMapping("/customer/{customerId}")
    public void addTagsToCustomer(@PathVariable String customerId, @RequestBody List<String> tagIds) {
        customerTagService.addTagsToCustomer(customerId, tagIds, OrganizationContext.getOrganizationId());
    }

    @Operation(summary = "移除客户的标签")
    @DeleteMapping("/customer/{customerId}")
    public void removeTagsFromCustomer(@PathVariable String customerId, @RequestBody List<String> tagIds) {
        customerTagService.removeTagsFromCustomer(customerId, tagIds);
    }

    @Operation(summary = "查询客户的标签列表")
    @GetMapping("/customer/{customerId}")
    public List<CustomerTag> getTagsByCustomerId(@PathVariable String customerId) {
        return customerTagService.getTagsByCustomerId(customerId);
    }

    @Operation(summary = "获取标签选项列表")
    @GetMapping("/options")
    public List<OptionDTO> getTagOptions(@RequestParam(required = false) String keyword) {
        return customerTagService.getTagOptions(keyword, OrganizationContext.getOrganizationId());
    }
}
