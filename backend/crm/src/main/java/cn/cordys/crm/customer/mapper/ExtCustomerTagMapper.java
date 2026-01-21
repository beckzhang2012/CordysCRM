package cn.cordys.crm.customer.mapper;

import cn.cordys.common.dto.OptionDTO;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户标签Mapper
 *
 * @author jianxing
 * @date 2026-01-21 10:00:00
 */
public interface ExtCustomerTagMapper {

    /**
     * 新增标签
     *
     * @param tag 标签信息
     */
    void insert(@Param("tag") CustomerTag tag);

    /**
     * 更新标签
     *
     * @param tag 标签信息
     */
    void update(@Param("tag") CustomerTag tag);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void delete(@Param("id") String id);

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签信息
     */
    CustomerTag selectById(@Param("id") String id);

    /**
     * 查询标签列表
     *
     * @param orgId 组织ID
     * @return 标签列表
     */
    List<CustomerTag> selectList(@Param("orgId") String orgId);

    /**
     * 根据名称查询标签
     *
     * @param name 标签名称
     * @param orgId 组织ID
     * @return 标签信息
     */
    CustomerTag selectByName(@Param("name") String name, @Param("orgId") String orgId);

    /**
     * 获取标签选项
     *
     * @param orgId 组织ID
     * @return 标签选项列表
     */
    List<OptionDTO> selectTagOptions(@Param("orgId") String orgId);

    /**
     * 新增客户标签关联
     *
     * @param relation 关联信息
     */
    void insertRelation(@Param("relation") CustomerTagRelation relation);

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
     * 查询客户的标签列表
     *
     * @param customerId 客户ID
     * @return 标签列表
     */
    List<CustomerTag> selectTagsByCustomerId(@Param("customerId") String customerId);

    /**
     * 查询标签关联数量
     *
     * @param tagId 标签ID
     * @return 关联数量
     */
    int countRelationsByTagId(@Param("tagId") String tagId);
}
