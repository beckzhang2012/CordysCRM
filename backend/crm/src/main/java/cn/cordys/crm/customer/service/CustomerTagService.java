package cn.cordys.crm.customer.service;

import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.Translator;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagBatchAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户标签服务
 *
 * @author cordys
 * @date 2025-02-03
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTagService {

    @Resource
    private BaseMapper<CustomerTag> customerTagMapper;
    @Resource
    private BaseMapper<CustomerTagRelation> customerTagRelationMapper;
    @Resource
    private ExtCustomerTagMapper extCustomerTagMapper;


    /**
     * 添加标签
     *
     * @param request        请求
     * @param userId         用户ID
     * @param organizationId 组织ID
     * @return 标签ID
     */
    public String add(CustomerTagAddRequest request, String userId, String organizationId) {
        // 检查标签名称是否已存在
        checkTagNameExists(request.getName(), null, organizationId);

        CustomerTag tag = new CustomerTag();
        tag.setId(IDGenerator.nextStr());
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setOrganizationId(organizationId);
        customerTagMapper.insert(tag);
        return tag.getId();
    }

    /**
     * 更新标签
     *
     * @param request        请求
     * @param userId         用户ID
     * @param organizationId 组织ID
     */
    public void update(CustomerTagUpdateRequest request, String userId, String organizationId) {
        // 检查标签是否存在
        CustomerTag existingTag = customerTagMapper.selectByPrimaryKey(request.getId());
        if (existingTag == null) {
            throw new RuntimeException(Translator.get("customer.tag.not_found"));
        }

        // 检查标签名称是否已存在
        checkTagNameExists(request.getName(), request.getId(), organizationId);

        existingTag.setName(request.getName());
        existingTag.setColor(request.getColor());
        customerTagMapper.updateById(existingTag);
    }

    /**
     * 删除标签
     *
     * @param tagId          标签ID
     * @param organizationId 组织ID
     */
    public void delete(String tagId, String organizationId) {
        // 检查标签是否存在
        CustomerTag existingTag = customerTagMapper.selectByPrimaryKey(tagId);
        if (existingTag == null) {
            throw new RuntimeException(Translator.get("customer.tag.not_found"));
        }

        // 删除标签关联
        extCustomerTagMapper.deleteRelationsByTagId(tagId);

        // 删除标签
        customerTagMapper.deleteByPrimaryKey(tagId);
    }

    /**
     * 分页查询标签列表
     *
     * @param request        请求
     * @param organizationId 组织ID
     * @return 分页结果
     */
    public Pager<List<CustomerTagResponse>> list(CustomerTagPageRequest request, String organizationId) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<CustomerTagResponse> list = extCustomerTagMapper.selectTagsByOrganizationId(organizationId, request.getName());

        // 查询每个标签关联的客户数量
        for (CustomerTagResponse tag : list) {
            Integer count = extCustomerTagMapper.selectCustomerCountByTagId(tag.getId());
            tag.setCustomerCount(count);
        }

        return PageUtils.setPageInfo(page, list);
    }

    /**
     * 查询所有标签
     *
     * @param organizationId 组织ID
     * @param name           标签名称
     * @return 标签列表
     */
    public List<CustomerTagResponse> listAll(String organizationId, String name) {
        List<CustomerTagResponse> list = extCustomerTagMapper.selectTagsByOrganizationId(organizationId, name);

        // 查询每个标签关联的客户数量
        for (CustomerTagResponse tag : list) {
            Integer count = extCustomerTagMapper.selectCustomerCountByTagId(tag.getId());
            tag.setCustomerCount(count);
        }

        return list;
    }

    /**
     * 获取客户的标签列表
     *
     * @param customerId 客户ID
     * @return 标签列表
     */
    public List<CustomerTagResponse> getTagsByCustomerId(String customerId) {
        return extCustomerTagMapper.selectTagsByCustomerId(customerId);
    }

    /**
     * 批量添加标签到客户
     *
     * @param request 请求
     * @param userId  用户ID
     */
    public void batchAddTagsToCustomer(CustomerTagBatchAddRequest request, String userId) {
        if (CollectionUtils.isEmpty(request.getTagIds())) {
            return;
        }

        String customerId = request.getCustomerId();

        // 查询客户已有的标签
        List<CustomerTagResponse> existingTags = extCustomerTagMapper.selectTagsByCustomerId(customerId);
        List<String> existingTagIds = existingTags.stream()
                .map(CustomerTagResponse::getId)
                .collect(Collectors.toList());

        // 过滤出需要新增的标签
        List<String> newTagIds = request.getTagIds().stream()
                .filter(tagId -> !existingTagIds.contains(tagId))
                .collect(Collectors.toList());

        // 批量插入标签关联
        List<CustomerTagRelation> relations = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (String tagId : newTagIds) {
            CustomerTagRelation relation = new CustomerTagRelation();
            relation.setId(IDGenerator.nextStr());
            relation.setCustomerId(customerId);
            relation.setTagId(tagId);
            relation.setCreateTime(currentTime);
            relation.setCreateUser(userId);
            relations.add(relation);
        }

        if (CollectionUtils.isNotEmpty(relations)) {
            customerTagRelationMapper.batchInsert(relations);
        }
    }

    /**
     * 批量删除客户的标签
     *
     * @param customerId 客户ID
     * @param tagIds     标签ID列表
     */
    public void batchRemoveTagsFromCustomer(String customerId, List<String> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        extCustomerTagMapper.deleteRelations(customerId, tagIds);
    }

    /**
     * 设置客户的标签（全量更新）
     *
     * @param customerId 客户ID
     * @param tagIds     标签ID列表
     * @param userId     用户ID
     */
    public void setCustomerTags(String customerId, List<String> tagIds, String userId) {
        // 删除客户的所有标签关联
        extCustomerTagMapper.deleteAllRelationsByCustomerId(customerId);

        // 添加新的标签关联
        if (CollectionUtils.isNotEmpty(tagIds)) {
            CustomerTagBatchAddRequest request = new CustomerTagBatchAddRequest();
            request.setCustomerId(customerId);
            request.setTagIds(tagIds);
            batchAddTagsToCustomer(request, userId);
        }
    }

    /**
     * 检查标签名称是否已存在
     *
     * @param name           标签名称
     * @param excludeId      排除的标签ID
     * @param organizationId 组织ID
     */
    private void checkTagNameExists(String name, String excludeId, String organizationId) {
        LambdaQueryWrapper<CustomerTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerTag::getOrganizationId, organizationId)
                .eq(CustomerTag::getName, name);

        if (excludeId != null) {
                wrapper.nq(CustomerTag::getId, excludeId);
        }

        List<CustomerTag> list = customerTagMapper.selectListByLambda(wrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new RuntimeException(Translator.get("customer.tag.name.exists"));
        }
    }
}
