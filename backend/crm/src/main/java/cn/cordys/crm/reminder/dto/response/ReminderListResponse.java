package cn.cordys.crm.reminder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ReminderListResponse {

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

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人名称")
    private String creatorName;
}
