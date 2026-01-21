package cn.cordys.crm.customer.service;

import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.request.TagAddRequest;
import cn.cordys.crm.customer.dto.request.TagRelationRequest;
import cn.cordys.crm.customer.dto.request.TagUpdateRequest;

import java.util.List;

/**
 * 客户标签服务
 *
 * @author jianxing
 * @date 2026-01-21 10:00:00
 */
public interface CustomerTagService {

    /**
     * 新增标签
     *
     * @param request 新增标签请求
     * @param userId 当前用户ID
     * @param orgId 组织ID
     * @return 标签信息
     */
    CustomerTag addTag(TagAddRequest request, String userId, String orgId);

    /**
     * 更新标签
     *
     * @param request 更新标签请求
     * @param userId 当前用户ID
     * @param orgId 组织ID
     * @return 标签信息
     */
    CustomerTag updateTag(TagUpdateRequest request, String userId, String orgId);

    /**
     * 删除标签
     *
     * @param tagId 标签ID
     * @param userId 当前用户ID
     * @param orgId 组织ID
     */
    void deleteTag(String tagId, String userId, String orgId);

    /**
     * 获取标签详情
     *
     * @param tagId 标签ID
     * @return 标签信息
     */
    CustomerTag getTag(String tagId);

    /**
     * 获取标签列表
     *
     * @param orgId 组织ID
     * @return 标签列表
     */
    List<CustomerTag> getTagList(String orgId);

    /**
     * 为客户添加标签
     *
     * @param request 标签关联请求
     * @param userId 当前用户ID
     * @param orgId 组织ID
     */
    void addCustomerTags(TagRelationRequest request, String userId, String orgId);

    /**
     * 为客户移除标签
     *
     * @param request 标签关联请求
     * @param userId 当前用户ID
     * @param orgId 组织ID
     */
    void removeCustomerTags(TagRelationRequest request, String userId, String orgId);

    /**
     * 获取客户的标签列表
     *
     * @param customerId 客户ID
     * @return 标签列表
     */
    List<CustomerTag> getCustomerTags(String customerId);
}
