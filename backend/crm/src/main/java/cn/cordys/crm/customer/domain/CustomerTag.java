package cn.cordys.crm.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 客户标签
 *
 * @author jianxing
 */
@Data
@Table(name = "customer_tag")
public class CustomerTag extends BaseModel {

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "标签内容")
    private String tagName;

    @Schema(description = "组织ID")
    private String organizationId;
}
