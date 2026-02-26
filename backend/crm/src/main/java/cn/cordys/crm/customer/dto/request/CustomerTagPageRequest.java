package cn.cordys.crm.customer.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerTagPageRequest extends BasePageRequest {

    @Schema(description = "标签名称")
    private String name;
}
