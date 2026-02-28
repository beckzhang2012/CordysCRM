package cn.cordys.crm.customer.controller;

import cn.cordys.crm.customer.dto.request.CustomerTagRequest;
import cn.cordys.crm.customer.dto.response.TagResponse;
import cn.cordys.crm.customer.service.CustomerService;
import cn.cordys.framework.core.web.Result;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/tag/save")
    @RequiresPermissions("customer:tag:save")
    public Result<Void> saveTags(@RequestBody CustomerTagRequest request) {
        customerService.saveCustomerTags(request.getCustomerId(), request.getTagIds());
        return Result.success();
    }

    @GetMapping("/tag/{id}")
    @RequiresPermissions("customer:tag:view")
    public Result<List<Long>> getTags(@PathVariable Long id) {
        return Result.success(customerService.getCustomerTagIds(id));
    }

    @GetMapping("/tag/list")
    @RequiresPermissions("customer:tag:view")
    public Result<List<TagResponse>> getAllTags() {
        return Result.success(customerService.getAllTags());
    }
}
