package cn.cordys.crm.customer.service;

import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.common.service.BaseService;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.Translator;
import cn.cordys.crm.customer.constants.CustomerResultCode;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagBindRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagQueryRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户标签服务
 *
 * @author jianxing
 * @date 2025-02-26
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
    @Resource
    private BaseService baseService;

    /**
     * 添加标签
     *
     * @param request 请求
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 标签ID
     */
    public String add(CustomerTagAddRequest request, String userId, String orgId) {
        // 检查标签名称是否已存在
        if (extCustomerTagMapper.countByName(request.getName(), orgId, null) > 0) {
            CustomerResultCode.TAG_NAME_EXISTS.throwException();
        }

        CustomerTag tag = new CustomerTag();
        tag.setId(IDGenerator.nextId());
        tag.setName(request.getName());
        tag.setColor(StringUtils.defaultIfBlank(request.getColor(), "#1677FF"));
        tag.setOrganizationId(orgId);
        baseService.insert(tag, userId);
        return tag.getId();
    }

    /**
     * 更新标签
     *
     * @param request 请求
     * @param userId 用户ID
     * @param orgId 组织ID
     */
    public void update(CustomerTagUpdateRequest request, String userId, String orgId) {
        // 检查标签是否存在
        CustomerTag existingTag = customerTagMapper.selectById(request.getId());
        if (existingTag == null || existingTag.getDeleted()) {
            CustomerResultCode.TAG_NOT_FOUND.throwException();
        }

        // 检查标签名称是否已存在
        if (extCustomerTagMapper.countByName(request.getName(), orgId, request.getId()) > 0) {
            CustomerResultCode.TAG_NAME_EXISTS.throwException();
        }

        CustomerTag tag = new CustomerTag();
        tag.setId(request.getId());
        tag.setName(request.getName());
        if (StringUtils.isNotBlank(request.getColor())) {
            tag.setColor(request.getColor());
        }
        baseService.updateByIdSelective(tag, userId);
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @param userId 用户ID
     */
    public void delete(String id, String userId) {
        CustomerTag tag = customerTagMapper.selectById(id);
        if (tag == null || tag.getDeleted()) {
            CustomerResultCode.TAG_NOT_FOUND.throwException();
        }
        baseService.deleteById(CustomerTag.class, id, userId);
    }

    /**
     * 获取标签列表
     *
     * @param request 请求
     * @param orgId 组织ID
     * @return 标签列表
     */
    public List<CustomerTagResponse> list(CustomerTagQueryRequest request, String orgId) {
        return extCustomerTagMapper.listByOrgId(orgId, request.getKeyword());
    }

    /**
     * 获取客户的标签列表
     *
     * @param customerId 客户ID
     * @return 标签列表
     */
    public List<CustomerTagResponse> listByCustomerId(String customerId) {
        return extCustomerTagMapper.listByCustomerId(customerId);
    }

    /**
     * 绑定标签到客户
     *
     * @param request 请求
     * @param userId 用户ID
     * @param orgId 组织ID
     */
    public void bindTags(CustomerTagBindRequest request, String userId, String orgId) {
        // 先删除原有标签关联
        extCustomerTagMapper.deleteRelationsByCustomerId(request.getCustomerId());

        // 插入新的标签关联
        if (CollectionUtils.isNotEmpty(request.getTagIds())) {
            extCustomerTagMapper.batchInsertRelations(request.getCustomerId(), request.getTagIds(), orgId, userId);
        }
    }

    /**
     * 添加标签到客户
     *
     * @param customerId 客户ID
     * @param tagId 标签ID
     * @param userId 用户ID
     * @param orgId 组织ID
     */
    public void addTagToCustomer(String customerId, String tagId, String userId, String orgId) {
        // 检查标签是否已关联
        LambdaQueryWrapper<CustomerTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerTagRelation::getCustomerId, customerId)
                .eq(CustomerTagRelation::getTagId, tagId)
                .eq(CustomerTagRelation::getDeleted, false);
        if (customerTagRelationMapper.selectCount(wrapper) > 0) {
            return; // 已存在，直接返回
        }

        CustomerTagRelation relation = new CustomerTagRelation();
        relation.setId(IDGenerator.nextId());
        relation.setCustomerId(customerId);
        relation.setTagId(tagId);
        relation.setOrganizationId(orgId);
        baseService.insert(relation, userId);
    }

    /**
     * 从客户移除标签
     *
     * @param customerId 客户ID
     * @param tagId 标签ID
     */
    public void removeTagFromCustomer(String customerId, String tagId) {
        extCustomerTagMapper.deleteRelation(customerId, tagId);
    }
}
