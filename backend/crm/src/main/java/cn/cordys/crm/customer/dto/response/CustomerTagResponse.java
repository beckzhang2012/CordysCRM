package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CustomerTagResponse {

    @Schema(description = "标签id")
    private String id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "创建时间")
    private Long createTime;
}
