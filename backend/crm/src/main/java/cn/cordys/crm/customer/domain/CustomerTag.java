package cn.cordys.crm.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;


/**
 * 客户标签
 *
 * @author jianxing
 * @date 2025-01-15
 */
@Data
@Table(name = "customer_tag")
public class CustomerTag extends BaseModel {

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "使用次数")
    private Integer usageCount;
}
