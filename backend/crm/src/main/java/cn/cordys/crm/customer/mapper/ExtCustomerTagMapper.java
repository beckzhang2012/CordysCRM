package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户标签Mapper
 *
 * @author jianxing
 * @date 2025-02-26
 */
public interface ExtCustomerTagMapper {

    /**
     * 根据组织ID查询标签列表
     *
     * @param orgId 组织ID
     * @param keyword 搜索关键词
     * @return 标签列表
     */
    List<CustomerTagResponse> listByOrgId(@Param("orgId") String orgId, @Param("keyword") String keyword);

    /**
     * 根据客户ID查询标签列表
     *
     * @param customerId 客户ID
     * @return 标签列表
     */
    List<CustomerTagResponse> listByCustomerId(@Param("customerId") String customerId);

    /**
     * 批量插入客户标签关联
     *
     * @param customerId 客户ID
     * @param tagIds 标签ID列表
     * @param orgId 组织ID
     * @param userId 用户ID
     */
    void batchInsertRelations(@Param("customerId") String customerId, @Param("tagIds") List<String> tagIds,
                              @Param("orgId") String orgId, @Param("userId") String userId);

    /**
     * 删除客户标签关联
     *
     * @param customerId 客户ID
     * @param tagId 标签ID
     */
    void deleteRelation(@Param("customerId") String customerId, @Param("tagId") String tagId);

    /**
     * 删除客户的所有标签关联
     *
     * @param customerId 客户ID
     */
    void deleteRelationsByCustomerId(@Param("customerId") String customerId);

    /**
     * 检查标签名称是否存在
     *
     * @param name 标签名称
     * @param orgId 组织ID
     * @param excludeId 排除的标签ID
     * @return 数量
     */
    Long countByName(@Param("name") String name, @Param("orgId") String orgId, @Param("excludeId") String excludeId);
}
