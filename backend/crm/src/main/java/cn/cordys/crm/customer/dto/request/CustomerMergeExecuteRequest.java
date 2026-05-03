package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CustomerMergeExecuteRequest {

    @NotNull
    @Schema(description = "被合并的客户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> mergeIds;

    @NotEmpty
    @Schema(description = "合并目标客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toMergeId;

    @NotEmpty
    @Schema(description = "合并后的负责人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerId;

    @Schema(description = "字段冲突选择列表")
    private List<MergeFieldSelection> fieldSelections;

    @Data
    public static class MergeFieldSelection {
        @Schema(description = "字段名称")
        private String fieldName;

        @Schema(description = "选择保留的客户ID（主客户或某个副客户）")
        private String selectedCustomerId;
    }
}
