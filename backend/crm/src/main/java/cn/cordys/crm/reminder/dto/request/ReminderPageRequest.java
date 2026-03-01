package cn.cordys.crm.reminder.dto.request;

import cn.cordys.common.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class ReminderPageRequest extends BasePageRequest {

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "客户ID")
    private String customerId;
}
