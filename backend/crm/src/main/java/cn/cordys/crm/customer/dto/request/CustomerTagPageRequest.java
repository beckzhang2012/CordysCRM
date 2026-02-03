package cn.cordys.crm.customer.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户标签分页查询请求
 *
 * @author cordys
 * @date 2025-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户标签分页查询请求")
public class CustomerTagPageRequest extends BasePageRequest {

    @Schema(description = "标签名称")
    private String name;
}
