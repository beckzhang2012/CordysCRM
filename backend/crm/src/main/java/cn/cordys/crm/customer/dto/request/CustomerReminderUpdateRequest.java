package cn.cordys.crm.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 客户提醒更新请求
 *
 * @author system
 */
@Data
public class CustomerReminderUpdateRequest {

    @NotBlank
    @Schema(description = "id")
    private String id;

    @Length(max = 1000)
    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒时间（时间戳）")
    private Long remindTime;

    @Schema(description = "负责人")
    private String owner;
}
