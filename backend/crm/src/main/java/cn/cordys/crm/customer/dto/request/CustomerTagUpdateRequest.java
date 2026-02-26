package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 客户标签更新请求
 *
 * @author jianxing
 * @date 2025-02-26
 */
@Data
public class CustomerTagUpdateRequest {

    @Schema(description = "标签ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{customer.tag.id.not_blank}")
    private String id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{customer.tag.name.not_blank}")
    private String name;

    @Schema(description = "标签颜色")
    private String color;
}
