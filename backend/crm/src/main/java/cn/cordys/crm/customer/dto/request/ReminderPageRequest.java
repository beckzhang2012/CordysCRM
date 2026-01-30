package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReminderPageRequest {

    @Schema(description = "当前页")
    private Integer current;

    @Schema(description = "每页数量")
    private Integer pageSize;

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "提醒状态")
    private String status;

    @Schema(description = "搜索内容")
    private String content;
}
