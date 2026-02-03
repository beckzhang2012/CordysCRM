package cn.cordys.crm.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户标签关联
 *
 * @author cordys
 * @date 2025-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "customer_tag_relation")
public class CustomerTagRelation extends BaseModel {

    @Schema(description = "客户id")
    private String customerId;

    @Schema(description = "标签id")
    private String tagId;
}
