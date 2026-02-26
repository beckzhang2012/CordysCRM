package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户标签查询请求
 *
 * @author jianxing
 * @date 2025-02-26
 */
@Data
public class CustomerTagQueryRequest {

    @Schema(description = "搜索关键词")
    private String keyword;
}
