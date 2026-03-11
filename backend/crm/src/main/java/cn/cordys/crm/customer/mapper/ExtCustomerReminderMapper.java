package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerReminder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户提醒扩展Mapper
 *
 * @author system
 */
public interface ExtCustomerReminderMapper {

    /**
     * 查询待触发的提醒（当前时间 <= remindTime 且状态为 PENDING）
     *
     * @param currentTime 当前时间戳
     * @return 待触发的提醒列表
     */
    List<CustomerReminder> selectPendingReminders(@Param("currentTime") Long currentTime);

    /**
     * 根据客户ID查询提醒列表
     *
     * @param customerId 客户ID
     * @param owner      负责人（可选）
     * @return 提醒列表
     */
    List<CustomerReminder> selectByCustomerId(@Param("customerId") String customerId, @Param("owner") String owner);

    /**
     * 查询用户的提醒列表
     *
     * @param owner     负责人
     * @param status    状态（可选）
     * @return 提醒列表
     */
    List<CustomerReminder> selectByOwner(@Param("owner") String owner, @Param("status") String status);

    /**
     * 批量更新提醒状态
     *
     * @param ids        ID列表
     * @param status     新状态
     * @param updateTime 更新时间
     * @param userId     更新人
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("ids") List<String> ids, @Param("status") String status,
                          @Param("updateTime") Long updateTime, @Param("userId") String userId);
}
