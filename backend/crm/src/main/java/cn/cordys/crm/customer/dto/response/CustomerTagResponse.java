package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户标签响应
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
@Data
@Schema(description = "客户标签响应")
public class CustomerTagResponse {

    @Schema(description = "标签ID")
    private String id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "标签描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private String createBy;
}