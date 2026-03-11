package cn.cordys.crm.customer.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户提醒分页请求
 *
 * @author system
 */
@Data
public class CustomerReminderPageRequest extends BasePageRequest {

    @Schema(description = "状态（可选）：PENDING-待提醒, TRIGGERED-已提醒, DISMISSED-已取消")
    private String status;
}
