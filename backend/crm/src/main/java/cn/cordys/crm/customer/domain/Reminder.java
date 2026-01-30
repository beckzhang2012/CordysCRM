package cn.cordys.crm.customer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "cus_reminder")
public class Reminder {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "客户ID")
    private String customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "提醒时间")
    private Long reminderTime;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒状态：PENDING-待提醒, COMPLETED-已提醒, CANCELLED-已取消")
    private String status;

    @Schema(description = "接收人ID")
    private String receiver;

    @Schema(description = "组织ID")
    private String organizationId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
