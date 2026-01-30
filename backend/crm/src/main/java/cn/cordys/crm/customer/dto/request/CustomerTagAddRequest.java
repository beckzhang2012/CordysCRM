package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 添加客户标签请求
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
@Data
@Schema(description = "添加客户标签请求")
public class CustomerTagAddRequest {

    @NotBlank(message = "标签名称不能为空")
    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "标签描述")
    private String description;
}