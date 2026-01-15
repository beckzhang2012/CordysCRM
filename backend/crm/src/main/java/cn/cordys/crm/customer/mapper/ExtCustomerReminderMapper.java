package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.dto.response.CustomerReminderListResponse;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户跟进提醒Mapper
 */
public interface ExtCustomerReminderMapper {

    void insert(@Param("reminder") CustomerReminder reminder);

    void update(@Param("reminder") CustomerReminder reminder);

    void delete(@Param("id") String id);

    CustomerReminder selectById(@Param("id") String id);

    List<CustomerReminderListResponse> selectByUserId(@Param("userId") String userId);

    List<CustomerReminderListResponse> selectByCustomerId(@Param("customerId") String customerId);

    List<CustomerReminderListResponse> selectPendingReminders(@Param("now") LocalDateTime now);

    List<CustomerReminder> selectRemindersToProcess(@Param("now") LocalDateTime now);

    int countPendingByUserId(@Param("userId") String userId);
}
