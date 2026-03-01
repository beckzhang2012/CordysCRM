package cn.cordys.crm.reminder.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Table(name = "reminder")
public class Reminder extends BaseModel {

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

    @Schema(description = "组织ID")
    private String organizationId;

    @Schema(description = "创建人ID")
    private String creatorId;

    @Schema(description = "创建人名称")
    private String creatorName;
}
