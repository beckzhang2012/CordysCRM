package cn.cordys.crm.customer.service;

import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;


/**
 * 客户标签服务
 *
 * @author jianxing
 * @date 2026-01-21 10:24:22
 */
public interface CustomerTagService {

    /**
     * 标签分页查询
     * @param request 查询参数
     * @return 标签分页结果
     */
    Page<CustomerTagResponse> getTagPage(CustomerTagPageRequest request);

    /**
     * 获取标签列表
     * @param organizationId 组织ID
     * @param category 标签分类
     * @return 标签列表
     */
    List<CustomerTagResponse> getTagList(String organizationId, String category);

    /**
     * 获取标签详情
     * @param id 标签ID
     * @return 标签详情
     */
    CustomerTagResponse getTagById(String id);

    /**
     * 创建标签
     * @param request 创建参数
     * @param organizationId 组织ID
     * @param creator 创建人
     * @return 标签ID
     */
    String createTag(CustomerTagAddRequest request, String organizationId, String creator);

    /**
     * 更新标签
     * @param request 更新参数
     * @return 是否成功
     */
    boolean updateTag(CustomerTagUpdateRequest request);

    /**
     * 删除标签
     * @param id 标签ID
     * @return 是否成功
     */
    boolean deleteTag(String id);

    /**
     * 为客户添加标签
     * @param customerId 客户ID
     * @param tagIds 标签ID列表
     * @param organizationId 组织ID
     * @param adder 添加人
     * @return 是否成功
     */
    boolean addTagsToCustomer(String customerId, List<String> tagIds, String organizationId, String adder);

    /**
     * 从客户移除标签
     * @param customerId 客户ID
     * @param tagIds 标签ID列表
     * @return 是否成功
     */
    boolean removeTagsFromCustomer(String customerId, List<String> tagIds);

    /**
     * 获取客户的标签列表
     * @param customerId 客户ID
     * @return 标签列表
     */
    List<CustomerTagResponse> getCustomerTags(String customerId);

    /**
     * 批量为客户添加标签
     * @param customerIds 客户ID列表
     * @param tagIds 标签ID列表
     * @param organizationId 组织ID
     * @param adder 添加人
     * @return 成功数量
     */
    int batchAddTagsToCustomers(List<String> customerIds, List<String> tagIds, String organizationId, String adder);

    /**
     * 批量从客户移除标签
     * @param customerIds 客户ID列表
     * @param tagIds 标签ID列表
     * @return 成功数量
     */
    int batchRemoveTagsFromCustomers(List<String> customerIds, List<String> tagIds);

    /**
     * 获取标签关联的客户数量
     * @param tagId 标签ID
     * @return 客户数量
     */
    int countCustomersByTagId(String tagId);
}