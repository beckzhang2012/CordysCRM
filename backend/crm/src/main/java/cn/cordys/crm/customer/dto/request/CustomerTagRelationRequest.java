package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class CustomerTagRelationRequest {

    @NotBlank(message = "客户ID不能为空")
    @Schema(description = "客户ID")
    private String customerId;

    @NotBlank(message = "标签ID不能为空")
    @Schema(description = "标签ID")
    private String tagId;
}
