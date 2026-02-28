package cn.cordys.crm.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;


/**
 * 客户标签关联
 *
 * @author jianxing
 * @date 2026-01-21 10:24:22
 */
@Data
@Table(name = "customer_tag_relation")
public class CustomerTagRelation extends BaseModel {

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "标签ID")
    private String tagId;

    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "添加人")
    private String adder;

    @Schema(description = "添加时间")
    private Long addTime;
}