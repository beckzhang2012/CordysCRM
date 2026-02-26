package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 客户标签绑定请求
 *
 * @author jianxing
 * @date 2025-02-26
 */
@Data
public class CustomerTagBindRequest {

    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{customer.id.not_blank}")
    private String customerId;

    @Schema(description = "标签ID列表")
    private List<String> tagIds;
}
