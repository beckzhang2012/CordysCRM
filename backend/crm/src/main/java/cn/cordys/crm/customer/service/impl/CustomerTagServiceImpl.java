package cn.cordys.crm.customer.service.impl;

import cn.cordys.aspectj.annotation.OperationLog;
import cn.cordys.aspectj.constants.LogModule;
import cn.cordys.aspectj.constants.LogType;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.service.BaseService;
import cn.cordys.common.service.DataScopeService;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.BeanUtils;
import cn.cordys.common.util.Strings;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import cn.cordys.crm.customer.dto.request.TagAddRequest;
import cn.cordys.crm.customer.dto.request.TagRelationRequest;
import cn.cordys.crm.customer.dto.request.TagUpdateRequest;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import cn.cordys.crm.customer.service.CustomerTagService;
import cn.cordys.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户标签服务实现
 *
 * @author jianxing
 * @date 2026-01-21 10:00:00
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTagServiceImpl implements CustomerTagService {

    @Resource
    private BaseMapper<CustomerTag> customerTagMapper;

    @Resource
    private BaseMapper<CustomerTagRelation> customerTagRelationMapper;

    @Resource
    private ExtCustomerTagMapper extCustomerTagMapper;

    @Resource
    private BaseService baseService;

    @Resource
    private DataScopeService dataScopeService;

    @Override
    @OperationLog(module = LogModule.CUSTOMER_INDEX, type = LogType.ADD, resourceName = "{#request.name}")
    public CustomerTag addTag(TagAddRequest request, String userId, String orgId) {
        // 检查标签名称是否已存在
        CustomerTag existingTag = extCustomerTagMapper.selectByName(request.getName(), orgId);
        if (existingTag != null) {
            throw new GenericException("标签名称已存在");
        }

        CustomerTag tag = BeanUtils.copyBean(new CustomerTag(), request);
        tag.setId(IDGenerator.nextStr());
        tag.setCreateTime(System.currentTimeMillis());
        tag.setUpdateTime(System.currentTimeMillis());
        tag.setCreateUser(userId);
        tag.setUpdateUser(userId);
        tag.setOrganizationId(orgId);
        tag.setIsSystem(false);

        customerTagMapper.insert(tag);
        return tag;
    }

    @Override
    @OperationLog(module = LogModule.CUSTOMER_INDEX, type = LogType.UPDATE, resourceId = "{#request.id}")
    public CustomerTag updateTag(TagUpdateRequest request, String userId, String orgId) {
        CustomerTag existingTag = customerTagMapper.selectByPrimaryKey(request.getId());
        if (existingTag == null) {
            throw new GenericException("标签不存在");
        }

        // 检查标签名称是否已存在（排除当前标签）
        if (!Strings.CS.equals(existingTag.getName(), request.getName())) {
            CustomerTag duplicateTag = extCustomerTagMapper.selectByName(request.getName(), orgId);
            if (duplicateTag != null) {
                throw new GenericException("标签名称已存在");
            }
        }

        // 系统标签不允许修改
        if (existingTag.getIsSystem()) {
            throw new GenericException("系统标签不允许修改");
        }

        CustomerTag tag = BeanUtils.copyBean(new CustomerTag(), request);
        tag.setUpdateTime(System.currentTimeMillis());
        tag.setUpdateUser(userId);

        customerTagMapper.update(tag);
        return customerTagMapper.selectByPrimaryKey(request.getId());
    }

    @Override
    @OperationLog(module = LogModule.CUSTOMER_INDEX, type = LogType.DELETE, resourceId = "{#tagId}")
    public void deleteTag(String tagId, String userId, String orgId) {
        CustomerTag tag = customerTagMapper.selectByPrimaryKey(tagId);
        if (tag == null) {
            throw new GenericException("标签不存在");
        }

        // 系统标签不允许删除
        if (tag.getIsSystem()) {
            throw new GenericException("系统标签不允许删除");
        }

        // 检查标签是否被使用
        int relationCount = extCustomerTagMapper.countRelationsByTagId(tagId);
        if (relationCount > 0) {
            throw new GenericException("标签已被使用，无法删除");
        }

        customerTagMapper.deleteByPrimaryKey(tagId);
    }

    @Override
    public CustomerTag getTag(String tagId) {
        return customerTagMapper.selectByPrimaryKey(tagId);
    }

    @Override
    public List<CustomerTag> getTagList(String orgId) {
        return extCustomerTagMapper.selectList(orgId);
    }

    @Override
    public void addCustomerTags(TagRelationRequest request, String userId, String orgId) {
        for (String tagId : request.getTagIds()) {
            // 检查标签是否存在
            CustomerTag tag = customerTagMapper.selectByPrimaryKey(tagId);
            if (tag == null) {
                throw new GenericException("标签不存在: " + tagId);
            }

            // 检查关联是否已存在
            CustomerTagRelation existingRelation = new CustomerTagRelation();
            existingRelation.setCustomerId(request.getCustomerId());
            existingRelation.setTagId(tagId);
            // 这里简化处理，直接插入，如果已存在会忽略
            CustomerTagRelation relation = new CustomerTagRelation();
            relation.setId(IDGenerator.nextStr());
            relation.setCustomerId(request.getCustomerId());
            relation.setTagId(tagId);
            relation.setOrganizationId(orgId);
            relation.setCreateTime(System.currentTimeMillis());
            relation.setUpdateTime(System.currentTimeMillis());
            relation.setCreateUser(userId);
            relation.setUpdateUser(userId);

            extCustomerTagMapper.insertRelation(relation);
        }
    }

    @Override
    public void removeCustomerTags(TagRelationRequest request, String userId, String orgId) {
        for (String tagId : request.getTagIds()) {
            extCustomerTagMapper.deleteRelation(request.getCustomerId(), tagId);
        }
    }

    @Override
    public List<CustomerTag> getCustomerTags(String customerId) {
        return extCustomerTagMapper.selectTagsByCustomerId(customerId);
    }
}
