package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户标签Mapper
 *
 * @author cordys
 * @date 2025-02-03
 */
public interface ExtCustomerTagMapper extends BaseMapper<CustomerTag> {

    /**
     * 根据客户ID查询标签列表
     *
     * @param customerId 客户ID
     * @return 标签列表
     */
    List<CustomerTagResponse> selectTagsByCustomerId(@Param("customerId") String customerId);

    /**
     * 根据组织ID查询标签列表
     *
     * @param organizationId 组织ID
     * @param name           标签名称（模糊查询）
     * @return 标签列表
     */
    List<CustomerTagResponse> selectTagsByOrganizationId(@Param("organizationId") String organizationId,
                                                         @Param("name") String name);

    /**
     * 查询标签关联的客户数量
     *
     * @param tagId 标签ID
     * @return 客户数量
     */
    Integer selectCustomerCountByTagId(@Param("tagId") String tagId);

    /**
     * 批量删除客户标签关联
     *
     * @param customerId 客户ID
     * @param tagIds     标签ID列表
     * @return 删除数量
     */
    int deleteRelations(@Param("customerId") String customerId,
                        @Param("tagIds") List<String> tagIds);

    /**
     * 删除客户的所有标签关联
     *
     * @param customerId 客户ID
     * @return 删除数量
     */
    int deleteAllRelationsByCustomerId(@Param("customerId") String customerId);

    /**
     * 根据标签ID删除所有关联
     *
     * @param tagId 标签ID
     * @return 删除数量
     */
    int deleteRelationsByTagId(@Param("tagId") String tagId);
}
