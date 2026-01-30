package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 客户标签列表响应
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
@Data
@Schema(description = "客户标签列表响应")
public class CustomerTagListResponse {

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "标签列表")
    private List<CustomerTagResponse> tags;
}