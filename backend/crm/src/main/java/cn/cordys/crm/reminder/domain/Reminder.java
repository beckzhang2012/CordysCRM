package cn.cordys.crm.reminder.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "reminder")
public class Reminder extends BaseModel {

    @Schema(description = "关联业务类型：CUSTOMER-客户, OPPORTUNITY-商机, CLUE-线索")
    private String businessType;

    @Schema(description = "关联业务ID")
    private String businessId;

    @Schema(description = "提醒时间")
    private Long remindTime;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "组织ID")
    private String organizationId;

    @Schema(description = "创建人")
    private String owner;

    @Schema(description = "提醒状态：PENDING-待提醒, COMPLETED-已提醒, CANCELLED-已取消")
    private String status;
}
