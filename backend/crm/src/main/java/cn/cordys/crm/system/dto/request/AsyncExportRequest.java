package cn.cordys.crm.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class AsyncExportRequest implements Serializable {
    
    @Schema(description = "导出任务ID", required = true)
    @NotNull(message = "导出任务ID不能为空")
    private String taskId;
    
    @Schema(description = "导出类型", required = true)
    @NotNull(message = "导出类型不能为空")
    private String exportType;
    
    @Schema(description = "组织ID", required = true)
    @NotNull(message = "组织ID不能为空")
    private String organizationId;
    
    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private String userId;
    
    @Schema(description = "文件名", required = true)
    @NotNull(message = "文件名不能为空")
    private String fileName;
    
    @Schema(description = "文件ID", required = true)
    @NotNull(message = "文件ID不能为空")
    private String fileId;
    
    @Schema(description = "导出参数(JSON格式)")
    private String exportParams;
    
    @Schema(description = "任务优先级(1-10，数字越大优先级越高)")
    private Integer priority = 5;
    
    @Schema(description = "分批大小(默认1000)")
    private Integer batchSize = 1000;
    
    @Schema(description = "是否使用流式处理")
    private Boolean useStream = true;
    
    @Schema(description = "任务超时时间(秒)")
    private Integer timeoutSeconds = 3600;
}