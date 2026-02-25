package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "客户提醒响应")
public class CustomerReminderResponse {

    @Schema(description = "提醒ID")
    private String id;

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "提醒时间")
    private Long reminderTime;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒状态")
    private String status;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
