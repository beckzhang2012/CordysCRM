package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 保存客户标签请求
 *
 * @author jianxing
 * @date 2025-01-15
 */
@Data
@Schema(description = "保存客户标签请求")
public class CustomerTagSaveRequest {

    @Schema(description = "标签名称")
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @Schema(description = "标签颜色")
    private String color;
}
