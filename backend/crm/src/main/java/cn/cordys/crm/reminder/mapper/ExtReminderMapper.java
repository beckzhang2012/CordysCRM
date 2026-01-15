package cn.cordys.crm.reminder.mapper;

import cn.cordys.common.dto.BasePageRequest;
import cn.cordys.crm.reminder.dto.request.ReminderPageRequest;
import cn.cordys.crm.reminder.dto.response.ReminderListResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtReminderMapper {

    List<ReminderListResponse> list(@Param("request") ReminderPageRequest request, @Param("orgId") String orgId, @Param("userId") String userId);

    int countByUserId(@Param("userId") String userId, @Param("orgId") String orgId);
}
