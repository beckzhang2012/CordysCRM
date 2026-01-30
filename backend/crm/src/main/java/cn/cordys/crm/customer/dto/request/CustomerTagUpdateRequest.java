package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新客户标签请求
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
@Data
@Schema(description = "更新客户标签请求")
public class CustomerTagUpdateRequest {

    @NotBlank(message = "标签ID不能为空")
    @Schema(description = "标签ID")
    private String id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "标签描述")
    private String description;
}