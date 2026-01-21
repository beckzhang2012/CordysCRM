package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 客户标签扩展Mapper
 *
 * @author jianxing
 * @date 2026-01-21 10:24:22
 */
@Mapper
public interface ExtCustomerTagMapper {

    /**
     * 查询标签列表
     * @param params 查询参数
     * @return 标签列表
     */
    List<CustomerTag> selectTagList(Map<String, Object> params);

    /**
     * 查询标签详情
     * @param id 标签ID
     * @return 标签详情
     */
    CustomerTag selectTagById(@Param("id") String id);

    /**
     * 新增标签
     * @param tag 标签信息
     * @return 影响行数
     */
    int insertTag(CustomerTag tag);

    /**
     * 更新标签
     * @param tag 标签信息
     * @return 影响行数
     */
    int updateTag(CustomerTag tag);

    /**
     * 删除标签
     * @param id 标签ID
     * @return 影响行数
     */
    int deleteTag(@Param("id") String id);

    /**
     * 增加标签使用次数
     * @param id 标签ID
     * @return 影响行数
     */
    int incrementTagUsageCount(@Param("id") String id);

    /**
     * 减少标签使用次数
     * @param id 标签ID
     * @return 影响行数
     */
    int decrementTagUsageCount(@Param("id") String id);

    /**
     * 查询客户标签关联
     * @param customerId 客户ID
     * @return 标签关联列表
     */
    List<CustomerTagRelation> selectTagRelationsByCustomerId(@Param("customerId") String customerId);

    /**
     * 查询标签关联的客户数量
     * @param tagId 标签ID
     * @return 客户数量
     */
    int countCustomersByTagId(@Param("tagId") String tagId);

    /**
     * 新增客户标签关联
     * @param relation 关联信息
     * @return 影响行数
     */
    int insertTagRelation(CustomerTagRelation relation);

    /**
     * 批量新增客户标签关联
     * @param relations 关联信息列表
     * @return 影响行数
     */
    int batchInsertTagRelations(List<CustomerTagRelation> relations);

    /**
     * 删除客户标签关联
     * @param customerId 客户ID
     * @param tagId 标签ID
     * @return 影响行数
     */
    int deleteTagRelation(@Param("customerId") String customerId, @Param("tagId") String tagId);

    /**
     * 批量删除客户标签关联
     * @param customerId 客户ID
     * @param tagIds 标签ID列表
     * @return 影响行数
     */
    int batchDeleteTagRelations(@Param("customerId") String customerId, @Param("tagIds") List<String> tagIds);

    /**
     * 删除客户的所有标签关联
     * @param customerId 客户ID
     * @return 影响行数
     */
    int deleteAllTagRelationsByCustomerId(@Param("customerId") String customerId);

    /**
     * 根据标签ID删除所有关联
     * @param tagId 标签ID
     * @return 影响行数
     */
    int deleteAllTagRelationsByTagId(@Param("tagId") String tagId);
}