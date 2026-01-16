package cn.cordys.crm.system.dto;

import cn.cordys.common.dto.DeptDataPermissionDTO;
import cn.cordys.common.dto.ExportSelectRequest;
import cn.cordys.crm.customer.dto.request.CustomerExportRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskMessage {

    private String taskId;
    private String fileId;
    private String userId;
    private String orgId;
    private String fileName;
    private String exportType;
    private String locale;
    private String logModule;

    private CustomerExportRequest customerExportRequest;
    private ExportSelectRequest exportSelectRequest;
    private DeptDataPermissionDTO deptDataPermission;
}
