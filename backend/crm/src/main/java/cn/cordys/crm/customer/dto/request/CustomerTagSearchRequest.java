package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CustomerTagSearchRequest {

    @Schema(description = "组织ID")
    private String organizationId;

    @Schema(description = "搜索关键词")
    private String keyword;
}
