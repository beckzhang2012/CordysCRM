package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.Reminder;
import cn.cordys.crm.customer.dto.request.ReminderPageRequest;
import cn.cordys.crm.customer.dto.response.ReminderListResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtReminderMapper {

    List<ReminderListResponse> list(@Param("request") ReminderPageRequest request, @Param("orgId") String orgId, @Param("userId") String userId);

    List<Reminder> selectPendingReminders(@Param("currentTime") Long currentTime, @Param("orgId") String orgId);

    int countPendingByUserId(@Param("userId") String userId, @Param("orgId") String orgId);
}
