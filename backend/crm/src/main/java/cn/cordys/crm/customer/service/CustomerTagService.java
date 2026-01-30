package cn.cordys.crm.customer.service;

import cn.cordys.common.pager.Pager;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagRelationRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagListResponse;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;

import java.util.List;

/**
 * 客户标签服务接口
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
public interface CustomerTagService {

    /**
     * 添加标签
     *
     * @param request 添加标签请求
     * @param organizationId 组织ID
     * @param createBy 创建人
     * @return 标签ID
     */
    String addTag(CustomerTagAddRequest request, String organizationId, String createBy);

    /**
     * 更新标签
     *
     * @param request 更新标签请求
     * @return 是否成功
     */
    boolean updateTag(CustomerTagUpdateRequest request);

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 是否成功
     */
    boolean deleteTag(String id);

    /**
     * 获取标签列表
     *
     * @param request 查询请求
     * @param organizationId 组织ID
     * @return 标签列表
     */
    Pager<List<CustomerTagResponse>> getTagList(CustomerTagPageRequest request, String organizationId);

    /**
     * 获取标签详情
     *
     * @param id 标签ID
     * @return 标签详情
     */
    CustomerTagResponse getTag(String id);

    /**
     * 关联客户标签
     *
     * @param request 关联请求
     * @param organizationId 组织ID
     * @param createBy 创建人
     * @return 是否成功
     */
    boolean relateCustomerTags(CustomerTagRelationRequest request, String organizationId, String createBy);

    /**
     * 获取客户的标签列表
     *
     * @param customerId 客户ID
     * @return 客户标签列表
     */
    CustomerTagListResponse getCustomerTags(String customerId);

    /**
     * 根据标签ID获取客户ID列表
     *
     * @param tagId 标签ID
     * @param organizationId 组织ID
     * @return 客户ID列表
     */
    List<String> getCustomerIdsByTagId(String tagId, String organizationId);
}