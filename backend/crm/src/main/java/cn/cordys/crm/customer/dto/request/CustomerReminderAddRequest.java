package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建客户跟进提醒请求
 */
@Data
public class CustomerReminderAddRequest {

    @NotBlank
    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerId;

    @NotBlank
    @Schema(description = "客户名称")
    private String customerName;

    @Future
    @Schema(description = "提醒时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime reminderTime;

    @Size(max = 500)
    @Schema(description = "提醒内容")
    private String content;
}
