package cn.cordys.crm.system.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Table(name = "export_task")
public class ExportTask extends BaseModel {

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "文件id")
    private String fileId;

    @Schema(description = "状态")
    private String status;
    
    @Schema(description = "进度百分比")
    private Double progressPercentage;
    
    @Schema(description = "已处理数量")
    private Long processedCount;
    
    @Schema(description = "总数量")
    private Long totalCount;
    
    @Schema(description = "优先级")
    private Integer priority;
    
    @Schema(description = "开始时间")
    private Long startTime;
    
    @Schema(description = "结束时间")
    private Long endTime;
    
    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "组织id")
    private String organizationId;
}
