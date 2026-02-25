package cn.cordys.crm.reminder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReminderListResponse {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "关联业务类型：CUSTOMER-客户, OPPORTUNITY-商机, CLUE-线索")
    private String businessType;

    @Schema(description = "关联业务ID")
    private String businessId;

    @Schema(description = "关联业务名称")
    private String businessName;

    @Schema(description = "提醒时间")
    private Long remindTime;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒状态：PENDING-待提醒, COMPLETED-已提醒, CANCELLED-已取消")
    private String status;

    @Schema(description = "创建人")
    private String owner;

    @Schema(description = "创建人名称")
    private String ownerName;

    @Schema(description = "创建时间")
    private Long createTime;
}
