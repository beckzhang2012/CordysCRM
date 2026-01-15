package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 客户标签关联请求
 *
 * @author jianxing
 * @date 2025-01-15
 */
@Data
@Schema(description = "客户标签关联请求")
public class CustomerTagRelationRequest {

    @Schema(description = "客户ID")
    @NotEmpty(message = "客户ID不能为空")
    private String customerId;

    @Schema(description = "标签ID列表")
    @NotEmpty(message = "标签ID不能为空")
    private List<String> tagIds;
}
