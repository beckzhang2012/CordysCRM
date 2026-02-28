package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 更新客户标签请求
 *
 * @author jianxing
 * @date 2026-01-21 10:24:22
 */
@Data
public class CustomerTagUpdateRequest {

    @Schema(description = "标签ID", required = true)
    @NotBlank(message = "标签ID不能为空")
    private String id;

    @Schema(description = "标签名称", required = true)
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @Schema(description = "标签描述")
    private String description;

    @Schema(description = "标签颜色", required = true)
    @NotBlank(message = "标签颜色不能为空")
    private String color;

    @Schema(description = "标签分类")
    private String category;
}