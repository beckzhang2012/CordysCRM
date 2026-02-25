package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新客户提醒请求")
public class CustomerReminderUpdateRequest {

    @NotBlank(message = "提醒ID不能为空")
    @Schema(description = "提醒ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @NotNull(message = "提醒时间不能为空")
    @Schema(description = "提醒时间(时间戳)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reminderTime;

    @NotBlank(message = "提醒内容不能为空")
    @Schema(description = "提醒内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
