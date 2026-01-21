package cn.cordys.crm.customer.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * 客户标签分页查询请求
 *
 * @author jianxing
 * @date 2026-01-21 10:24:22
 */
@Data
public class CustomerTagPageRequest extends BasePageRequest {

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签分类")
    private String category;

    @Schema(description = "是否系统内置")
    private Boolean isSystem;
}