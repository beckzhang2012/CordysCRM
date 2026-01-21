package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 客户标签响应
 *
 * @author jianxing
 * @date 2026-01-21 10:24:22
 */
@Data
public class CustomerTagResponse {

    @Schema(description = "标签ID")
    private String id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签描述")
    private String description;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "标签分类")
    private String category;

    @Schema(description = "使用次数")
    private Integer usageCount;

    @Schema(description = "是否系统内置")
    private Boolean isSystem;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}