package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户跟进提醒列表响应
 */
@Data
public class CustomerReminderListResponse {

    @Schema(description = "提醒ID")
    private String id;

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "提醒接收人ID")
    private String userId;

    @Schema(description = "提醒接收人名称")
    private String userName;

    @Schema(description = "提醒时间")
    private LocalDateTime reminderTime;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "状态：0-未提醒 1-已提醒 2-已关闭")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
