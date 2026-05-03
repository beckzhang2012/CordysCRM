package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CustomerMergePreviewRequest {

    @NotNull
    @Schema(description = "被合并的客户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> mergeIds;

    @NotEmpty
    @Schema(description = "合并目标客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toMergeId;
}
