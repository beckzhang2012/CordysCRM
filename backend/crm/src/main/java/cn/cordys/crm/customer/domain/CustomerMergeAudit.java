package cn.cordys.crm.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;

@Data
@Table(name = "customer_merge_audit")
public class CustomerMergeAudit extends BaseModel {

    @Schema(description = "组织ID")
    private String organizationId;

    @Schema(description = "主客户ID")
    private String primaryCustomerId;

    @Schema(description = "主客户名称")
    private String primaryCustomerName;

    @Schema(description = "副客户ID列表（JSON格式）")
    private String secondaryCustomerIds;

    @Schema(description = "副客户名称列表（JSON格式）")
    private String secondaryCustomerNames;

    @Schema(description = "合并后的负责人ID")
    private String ownerId;

    @Schema(description = "合并后的负责人名称")
    private String ownerName;

    @Schema(description = "迁移的联系人数量")
    private Integer contactCount;

    @Schema(description = "迁移的商机数量")
    private Integer opportunityCount;

    @Schema(description = "迁移的合同数量")
    private Integer contractCount;

    @Schema(description = "迁移的回款计划数量")
    private Integer paymentPlanCount;

    @Schema(description = "迁移的跟进记录数量")
    private Integer followRecordCount;

    @Schema(description = "迁移的跟进计划数量")
    private Integer followPlanCount;

    @Schema(description = "合并状态：SUCCESS/FAILED")
    private String status;

    @Schema(description = "失败原因（如果有）")
    private String failReason;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "操作时间")
    private Long operationTime;
}
