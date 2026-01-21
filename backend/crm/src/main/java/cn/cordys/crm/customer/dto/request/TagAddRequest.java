package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


/**
 * 新增标签请求
 *
 * @author jianxing
 * @date 2026-01-21 10:00:00
 */
@Data
public class TagAddRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "标签名称")
    private String name;

    @Size(max = 255)
    @Schema(description = "标签描述")
    private String description;

    @Size(max = 20)
    @Schema(description = "标签颜色")
    private String color;

    @Size(max = 50)
    @Schema(description = "标签类型")
    private String type;
}
