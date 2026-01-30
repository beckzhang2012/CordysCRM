package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 客户标签关联请求
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
@Data
@Schema(description = "客户标签关联请求")
public class CustomerTagRelationRequest {

    @NotBlank(message = "客户ID不能为空")
    @Schema(description = "客户ID")
    private String customerId;

    @NotEmpty(message = "标签ID列表不能为空")
    @Schema(description = "标签ID列表")
    private List<String> tagIds;
}