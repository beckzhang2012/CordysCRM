package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新客户跟进提醒状态请求
 */
@Data
public class CustomerReminderUpdateRequest {

 @NotNull
 @Schema(description = "提醒ID", requiredMode = Schema.RequiredMode.REQUIRED)
 private String id;

 @Schema(description = "状态：0-未提醒 1-已提醒 2-已关闭")
 private Integer status;
}
