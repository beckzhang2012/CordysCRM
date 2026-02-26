package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CustomerTagBindRequest {

    @Schema(description = "客户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "客户id不能为空")
    private String customerId;

    @Schema(description = "标签id列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标签id列表不能为空")
    private List<String> tagIds;
}
