package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 客户标签请求
 *
 * @author jianxing
 */
@Data
public class CustomerTagRequest {

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String customerId;

    @Schema(description = "标签列表")
    @NotNull(message = "标签列表不能为空")
    private List<@NotBlank(message = "标签名称不能为空") String> tagNames;
}
