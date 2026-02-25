package cn.cordys.crm.reminder.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReminderPageRequest extends BasePageRequest {

    @Schema(description = "关联业务类型：CUSTOMER-客户, OPPORTUNITY-商机, CLUE-线索")
    private String businessType;

    @Schema(description = "关联业务ID")
    private String businessId;

    @Schema(description = "提醒状态：PENDING-待提醒, COMPLETED-已提醒, CANCELLED-已取消")
    private String status;
}
