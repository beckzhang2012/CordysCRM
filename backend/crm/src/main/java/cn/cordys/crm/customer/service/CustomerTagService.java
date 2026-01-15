package cn.cordys.crm.customer.service;

import cn.cordys.common.response.CrmResponse;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import cn.cordys.crm.customer.dto.request.CustomerTagRelationRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagSaveRequest;
import cn.cordys.crm.customer.mapper.CustomerTagMapper;
import cn.cordys.crm.customer.mapper.CustomerTagRelationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户标签服务
 *
 * @author jianxing
 * @date 2025-01-15
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTagService {

    @Resource
    private CustomerTagMapper customerTagMapper;

    @Resource
    private CustomerTagRelationMapper customerTagRelationMapper;

    public CrmResponse<List<CustomerTag>> list(String organizationId) {
        List<CustomerTag> tags = customerTagMapper.selectByOrganizationId(organizationId);
        return CrmResponse.success(tags);
    }

    public CrmResponse<List<CustomerTag>> listByCustomerId(String customerId) {
        List<CustomerTag> tags = customerTagMapper.selectByCustomerId(customerId);
        return CrmResponse.success(tags);
    }

    public CrmResponse<CustomerTag> save(CustomerTagSaveRequest request, String organizationId, String userId) {
        CustomerTag existTag = customerTagMapper.selectByName(request.getName(), organizationId);
        if (existTag != null) {
            return CrmResponse.fail("标签名称已存在");
        }

        CustomerTag tag = new CustomerTag();
        tag.setId(IDGenerator.nextStr());
        tag.setName(request.getName());
        tag.setColor(StringUtils.isNotBlank(request.getColor()) ? request.getColor() : "#18a058");
        tag.setOrganizationId(organizationId);
        tag.setCreateUser(userId);
        tag.setUsageCount(0);
        tag.setCreateTime(System.currentTimeMillis());
        tag.setUpdateTime(System.currentTimeMillis());

        customerTagMapper.insert(tag);
        return CrmResponse.success(tag);
    }

    public CrmResponse<Void> delete(String tagId, String organizationId) {
        CustomerTag tag = customerTagMapper.selectById(tagId);
        if (tag == null) {
            return CrmResponse.fail("标签不存在");
        }
        if (!tag.getOrganizationId().equals(organizationId)) {
            return CrmResponse.fail("无权删除该标签");
        }

        customerTagMapper.deleteById(tagId);
        customerTagRelationMapper.deleteByTagId(tagId);
        return CrmResponse.success();
    }

    public CrmResponse<Void> updateCustomerTags(CustomerTagRelationRequest request, String organizationId) {
        customerTagRelationMapper.deleteByCustomerId(request.getCustomerId());

        if (CollectionUtils.isNotEmpty(request.getTagIds())) {
            List<CustomerTagRelation> relations = request.getTagIds().stream()
                .map(tagId -> {
                    CustomerTagRelation relation = new CustomerTagRelation();
                    relation.setId(IDGenerator.nextStr());
                    relation.setCustomerId(request.getCustomerId());
                    relation.setTagId(tagId);
                    relation.setOrganizationId(organizationId);
                    relation.setCreateTime(System.currentTimeMillis());
                    relation.setUpdateTime(System.currentTimeMillis());
                    return relation;
                })
                .collect(Collectors.toList());

            relations.forEach(customerTagRelationMapper::insert);

            request.getTagIds().forEach(customerTagMapper::incrementUsageCount);
        }

        return CrmResponse.success();
    }

    public CrmResponse<List<String>> getCustomerIdsByTagId(String tagId) {
        List<String> customerIds = customerTagRelationMapper.selectCustomerIdsByTagId(tagId);
        return CrmResponse.success(customerIds);
    }
}
