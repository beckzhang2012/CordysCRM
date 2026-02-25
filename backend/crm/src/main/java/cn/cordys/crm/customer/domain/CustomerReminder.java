package cn.cordys.crm.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "customer_reminder")
public class CustomerReminder extends BaseModel {

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "提醒时间")
    private Long reminderTime;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒状态: PENDING-待提醒, NOTIFIED-已提醒, CANCELLED-已取消")
    private String status;

    @Schema(description = "组织ID")
    private String organizationId;

    @Schema(description = "提醒人ID")
    private String reminderUserId;
}
