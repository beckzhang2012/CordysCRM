package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class CustomerTagResponse {

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "标签列表")
    private List<String> tags;
}
