package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerTagUpdateRequest {

    @Schema(description = "标签id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标签id不能为空")
    private String id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @Schema(description = "标签颜色")
    private String color;
}
