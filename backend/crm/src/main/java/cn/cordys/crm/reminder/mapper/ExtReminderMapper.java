package cn.cordys.crm.reminder.mapper;

import cn.cordys.crm.reminder.domain.Reminder;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒Mapper接口
 *
 * @author jianxing
 * @date 2025-02-15
 */
public interface ExtReminderMapper {

    /**
     * 根据ID查询提醒
     *
     * @param id 提醒ID
     * @return 提醒实体
     */
    Reminder selectById(@Param("id") String id);

    /**
     * 查询所有提醒
     *
     * @return 提醒列表
     */
    List<Reminder> selectAll();

    /**
     * 查询未读提醒
     *
     * @return 未读提醒列表
     */
    List<Reminder> selectUnread();

    /**
     * 查询即将到期的提醒
     *
     * @param currentTime 当前时间
     * @return 即将到期的提醒列表
     */
    List<Reminder> selectUpcoming(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 新增提醒
     *
     * @param reminder 提醒实体
     */
    void insert(@Param("reminder") Reminder reminder);

    /**
     * 更新提醒
     *
     * @param reminder 提醒实体
     */
    void update(@Param("reminder") Reminder reminder);

    /**
     * 标记为已读
     *
     * @param id 提醒ID
     */
    void markAsRead(@Param("id") String id);

    /**
     * 批量标记为已读
     *
     * @param ids 提醒ID列表
     */
    void batchMarkAsRead(@Param("ids") List<String> ids);

    /**
     * 删除提醒
     *
     * @param id 提醒ID
     */
    void deleteById(@Param("id") String id);

    /**
     * 批量删除提醒
     *
     * @param ids 提醒ID列表
     */
    void batchDeleteByIds(@Param("ids") List<String> ids);

    /**
     * 统计未读提醒数量
     *
     * @return 未读提醒数量
     */
    int countUnread();
}
