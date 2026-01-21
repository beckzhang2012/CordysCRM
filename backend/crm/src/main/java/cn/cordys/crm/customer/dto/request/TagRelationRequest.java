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
 * @date 2026-01-21 10:00:00
 */
@Data
public class TagRelationRequest {

    @NotBlank
    @Schema(description = "客户ID")
    private String customerId;

    @NotEmpty
    @Schema(description = "标签ID列表")
    private List<String> tagIds;
}
