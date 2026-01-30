package cn.cordys.crm.customer.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户标签查询请求
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户标签查询请求")
public class CustomerTagPageRequest extends BasePageRequest {

    @Schema(description = "标签名称")
    private String name;
    
    @Schema(description = "组织ID")
    private String organizationId;
}