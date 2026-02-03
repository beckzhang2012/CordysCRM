package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户标签响应
 *
 * @author cordys
 * @date 2025-02-03
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

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "客户数量")
    private Integer customerCount;
}
