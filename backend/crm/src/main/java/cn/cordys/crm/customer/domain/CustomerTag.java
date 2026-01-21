package cn.cordys.crm.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;


/**
 * 客户标签
 *
 * @author jianxing
 * @date 2026-01-21 10:00:00
 */
@Data
@Table(name = "customer_tag")
public class CustomerTag extends BaseModel {

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签描述")
    private String description;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "标签类型")
    private String type;

    @Schema(description = "是否系统标签")
    private Boolean isSystem;

    @Schema(description = "组织id")
    private String organizationId;
}
