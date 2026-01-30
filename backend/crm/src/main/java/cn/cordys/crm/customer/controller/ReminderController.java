package cn.cordys.crm.customer.controller;

import cn.cordys.common.pager.Pager;
import cn.cordys.crm.customer.dto.request.ReminderAddRequest;
import cn.cordys.crm.customer.dto.request.ReminderPageRequest;
import cn.cordys.crm.customer.dto.response.ReminderListResponse;
import cn.cordys.crm.customer.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "客户跟进提醒")
@RestController
@RequestMapping(value = "/reminder")
public class ReminderController {

    @Resource
    private ReminderService reminderService;

    @PostMapping(value = "/add")
    @Operation(summary = "添加提醒")
    public void add(@Validated @RequestBody ReminderAddRequest request) {
        reminderService.add(request);
    }

    @PostMapping(value = "/list/page")
    @Operation(summary = "分页查询提醒列表")
    public Pager<List<ReminderListResponse>> list(@Validated @RequestBody ReminderPageRequest request) {
        return reminderService.list(request);
    }

    @DeleteMapping(value = "/delete/{id}")
    @Operation(summary = "删除提醒")
    public void delete(@PathVariable String id) {
        reminderService.delete(id);
    }

    @PutMapping(value = "/cancel/{id}")
    @Operation(summary = "取消提醒")
    public void cancel(@PathVariable String id) {
        reminderService.cancel(id);
    }

    @GetMapping(value = "/pending/count")
    @Operation(summary = "获取待提醒数量")
    public Map<String, Integer> countPending() {
        Map<String, Integer> result = new HashMap<>();
        result.put("count", reminderService.countPending());
        return result;
    }

    @PostMapping(value = "/process")
    @Operation(summary = "处理到期提醒（定时任务调用）")
    public void processDueReminders() {
        reminderService.processDueReminders();
    }
}
