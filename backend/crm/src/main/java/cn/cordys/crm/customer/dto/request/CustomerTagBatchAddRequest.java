package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 客户批量添加标签请求
 *
 * @author cordys
 * @date 2025-02-03
 */
@Data
@Schema(description = "客户批量添加标签请求")
public class CustomerTagBatchAddRequest {

    @NotBlank(message = "{customer.id.not_blank}")
    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerId;

    @Schema(description = "标签ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> tagIds;
}
