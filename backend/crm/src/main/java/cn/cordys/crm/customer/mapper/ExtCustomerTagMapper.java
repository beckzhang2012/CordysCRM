package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.dto.response.CustomerTagListResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户标签扩展 Mapper
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
public interface ExtCustomerTagMapper {

    /**
     * 获取标签列表
     *
     * @param organizationId 组织ID
     * @param name 标签名称
     * @return 标签列表
     */
    List<CustomerTagResponse> selectTagList(@Param("organizationId") String organizationId, @Param("name") String name);

    /**
     * 获取客户的标签列表
     *
     * @param customerId 客户ID
     * @return 客户标签列表
     */
    CustomerTagListResponse selectCustomerTags(@Param("customerId") String customerId);

    /**
     * 获取客户ID列表根据标签ID
     *
     * @param tagId 标签ID
     * @param organizationId 组织ID
     * @return 客户ID列表
     */
    List<String> selectCustomerIdsByTagId(@Param("tagId") String tagId, @Param("organizationId") String organizationId);
}