package cn.cordys.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 导出任务实体
 * @author song-cc-rock
 */
@Data
public class ExportTask implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "文件id")
    private String fileId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "组织id")
    private String organizationId;
}