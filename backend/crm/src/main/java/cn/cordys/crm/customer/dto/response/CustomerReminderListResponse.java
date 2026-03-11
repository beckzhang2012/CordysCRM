package cn.cordys.crm.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户提醒列表响应
 *
 * @author system
 */
@Data
public class CustomerReminderListResponse {

    @Schema(description = "id")
    private String id;

    @Schema(description = "客户id")
    private String customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒时间（时间戳）")
    private Long remindTime;

    @Schema(description = "负责人")
    private String owner;

    @Schema(description = "负责人名称")
    private String ownerName;

    @Schema(description = "状态：PENDING-待提醒, TRIGGERED-已提醒, DISMISSED-已取消")
    private String status;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建人名称")
    private String createUserName;
}
