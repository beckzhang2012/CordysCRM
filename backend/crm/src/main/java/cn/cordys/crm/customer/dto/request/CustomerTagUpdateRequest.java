package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客户标签更新请求
 *
 * @author cordys
 * @date 2025-02-03
 */
@Data
@Schema(description = "客户标签更新请求")
public class CustomerTagUpdateRequest {

    @NotBlank(message = "{customer.tag.id.not_blank}")
    @Schema(description = "标签ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @NotBlank(message = "{customer.tag.name.not_blank}")
    @Size(max = 100, message = "{customer.tag.name.length}")
    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "标签颜色")
    private String color;
}
