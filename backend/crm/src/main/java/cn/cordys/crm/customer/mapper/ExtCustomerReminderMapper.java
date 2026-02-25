package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerReminder;
import cn.cordys.crm.customer.dto.response.CustomerReminderResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCustomerReminderMapper {

    List<CustomerReminderResponse> listByCustomerId(@Param("customerId") String customerId, @Param("userId") String userId);

    List<CustomerReminderResponse> listPendingByUserId(@Param("userId") String userId, @Param("orgId") String orgId);

    List<CustomerReminderResponse> listAllByUserId(@Param("userId") String userId, @Param("orgId") String orgId);

    CustomerReminderResponse getById(@Param("id") String id, @Param("userId") String userId);

    int updateStatus(@Param("id") String id, @Param("status") String status);

    int deleteById(@Param("id") String id, @Param("userId") String userId);

    List<CustomerReminder> getDueReminders(@Param("currentTime") Long currentTime, @Param("orgId") String orgId);

    int countPendingByUserId(@Param("userId") String userId, @Param("orgId") String orgId);
}
