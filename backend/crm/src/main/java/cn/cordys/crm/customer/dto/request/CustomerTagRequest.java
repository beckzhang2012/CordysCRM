package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CustomerTagRequest {

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String customerId;

    @Schema(description = "标签列表")
    private List<String> tags;
}
