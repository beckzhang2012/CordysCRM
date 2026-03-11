package cn.cordys.crm.customer.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 客户跟进提醒
 *
 * @author system
 * @date 2025-03-11
 */
@Data
@Table(name = "customer_reminder")
public class CustomerReminder extends BaseModel {

    @Schema(description = "客户id")
    private String customerId;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒时间（时间戳）")
    private Long remindTime;

    @Schema(description = "负责人")
    private String owner;

    @Schema(description = "状态：PENDING-待提醒, TRIGGERED-已提醒, DISMISSED-已取消")
    private String status;

    @Schema(description = "组织id")
    private String organizationId;
}
