package cn.cordys.crm.reminder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ReminderAddRequest {

    @Schema(description = "客户ID", required = true)
    @NotBlank(message = "客户ID不能为空")
    private String customerId;

    @Schema(description = "提醒时间", required = true)
    @NotNull(message = "提醒时间不能为空")
    private Long reminderTime;

    @Schema(description = "提醒内容", required = true)
    @NotBlank(message = "提醒内容不能为空")
    private String content;
}
