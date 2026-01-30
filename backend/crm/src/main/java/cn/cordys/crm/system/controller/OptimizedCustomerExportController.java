package cn.cordys.crm.system.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.dto.DeptDataPermissionDTO;
import cn.cordys.common.dto.ExportSelectRequest;
import cn.cordys.common.utils.ConditionFilterUtils;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.dto.request.CustomerExportRequest;
import cn.cordys.crm.customer.service.OptimizedCustomerExportService;
import cn.cordys.security.SessionUtils;
import cn.cordys.common.service.DataScopeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 优化后的客户导出控制器
 * 使用异步导出和流式处理，避免内存溢出和阻塞主线程
 */
@Tag(name = "客户导出(优化版)")
@RestController
@RequestMapping("/account/export-optimized")
public class OptimizedCustomerExportController {

    @Resource
    private OptimizedCustomerExportService optimizedCustomerExportService;
    @Resource
    private DataScopeService dataScopeService;

    @PostMapping("/async-export-all")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_EXPORT)
    @Operation(summary = "异步导出全部客户数据")
    public String asyncExportAll(@Validated @RequestBody CustomerExportRequest request) {
        ConditionFilterUtils.parseCondition(request);
        DeptDataPermissionDTO deptDataPermission = dataScopeService.getDeptDataPermission(SessionUtils.getUserId(),
                OrganizationContext.getOrganizationId(), request.getViewId(), PermissionConstants.CUSTOMER_MANAGEMENT_READ);
        
        return optimizedCustomerExportService.asyncExport(
                SessionUtils.getUserId(), 
                request, 
                OrganizationContext.getOrganizationId(), 
                deptDataPermission, 
                LocaleContextHolder.getLocale()
        );
    }

    @PostMapping("/async-export-select")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_EXPORT)
    @Operation(summary = "异步导出选中的客户数据")
    public String asyncExportSelect(@Validated @RequestBody ExportSelectRequest request) {
        return optimizedCustomerExportService.asyncExportSelect(
                SessionUtils.getUserId(), 
                request, 
                OrganizationContext.getOrganizationId(), 
                LocaleContextHolder.getLocale()
        );
    }

    @PostMapping("/high-priority-export")
    @RequiresPermissions(PermissionConstants.CUSTOMER_MANAGEMENT_EXPORT)
    @Operation(summary = "高优先级异步导出客户数据")
    public String highPriorityExport(@Validated @RequestBody CustomerExportRequest request) {
        ConditionFilterUtils.parseCondition(request);
        DeptDataPermissionDTO deptDataPermission = dataScopeService.getDeptDataPermission(SessionUtils.getUserId(),
                OrganizationContext.getOrganizationId(), request.getViewId(), PermissionConstants.CUSTOMER_MANAGEMENT_READ);
        
        return optimizedCustomerExportService.highPriorityAsyncExport(
                SessionUtils.getUserId(), 
                request, 
                OrganizationContext.getOrganizationId(), 
                deptDataPermission, 
                LocaleContextHolder.getLocale()
        );
    }
}