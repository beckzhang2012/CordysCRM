package cn.cordys.crm.reminder.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提醒实体
 *
 * @author jianxing
 * @date 2025-02-15
 */
@Data
@Table(name = "reminder")
public class Reminder extends BaseModel {

    @Schema(description = "源ID（如客户ID、线索ID等）")
    private String sourceId;

    @Schema(description = "源名称（如客户名称、线索名称等）")
    private String sourceName;

    @Schema(description = "提醒时间")
    private LocalDateTime remindTime;

    @Schema(description = "提醒内容")
    private String remindContent;

    @Schema(description = "是否已读")
    @Column(name = "is_read")
    private Boolean isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
