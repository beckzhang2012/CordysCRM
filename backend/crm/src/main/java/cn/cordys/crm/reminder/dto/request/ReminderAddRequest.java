package cn.cordys.crm.reminder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReminderAddRequest {

    @NotBlank(message = "业务类型不能为空")
    @Schema(description = "关联业务类型：CUSTOMER-客户, OPPORTUNITY-商机, CLUE-线索")
    private String businessType;

    @NotBlank(message = "业务ID不能为空")
    @Schema(description = "关联业务ID")
    private String businessId;

    @NotNull(message = "提醒时间不能为空")
    @Schema(description = "提醒时间")
    private Long remindTime;

    @Schema(description = "提醒内容")
    private String content;
}
