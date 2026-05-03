package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class CustomerMergePreviewResponse {

    @Schema(description = "主客户信息")
    private CustomerMergeInfo primaryCustomer;

    @Schema(description = "副客户信息列表")
    private List<CustomerMergeInfo> secondaryCustomers;

    @Schema(description = "合并统计信息")
    private MergeStatistics statistics;

    @Schema(description = "冲突字段列表")
    private List<MergeConflictField> conflictFields;

    @Data
    public static class CustomerMergeInfo {
        @Schema(description = "客户ID")
        private String id;

        @Schema(description = "客户名称")
        private String name;

        @Schema(description = "负责人ID")
        private String owner;

        @Schema(description = "负责人名称")
        private String ownerName;

        @Schema(description = "联系人数量")
        private Integer contactCount;

        @Schema(description = "商机数量")
        private Integer opportunityCount;

        @Schema(description = "合同数量")
        private Integer contractCount;

        @Schema(description = "回款计划数量")
        private Integer paymentPlanCount;

        @Schema(description = "跟进记录数量")
        private Integer followRecordCount;

        @Schema(description = "跟进计划数量")
        private Integer followPlanCount;
    }

    @Data
    public static class MergeStatistics {
        @Schema(description = "联系人总数")
        private Integer totalContacts;

        @Schema(description = "商机总数")
        private Integer totalOpportunities;

        @Schema(description = "合同总数")
        private Integer totalContracts;

        @Schema(description = "回款计划总数")
        private Integer totalPaymentPlans;

        @Schema(description = "跟进记录总数")
        private Integer totalFollowRecords;

        @Schema(description = "跟进计划总数")
        private Integer totalFollowPlans;
    }

    @Data
    public static class MergeConflictField {
        @Schema(description = "字段名称")
        private String fieldName;

        @Schema(description = "字段显示名称")
        private String fieldLabel;

        @Schema(description = "主客户值")
        private String primaryValue;

        @Schema(description = "副客户值列表")
        private List<SecondaryFieldValue> secondaryValues;

        @Schema(description = "是否需要用户选择")
        private Boolean needSelection;
    }

    @Data
    public static class SecondaryFieldValue {
        @Schema(description = "客户ID")
        private String customerId;

        @Schema(description = "客户名称")
        private String customerName;

        @Schema(description = "字段值")
        private String value;
    }
}
