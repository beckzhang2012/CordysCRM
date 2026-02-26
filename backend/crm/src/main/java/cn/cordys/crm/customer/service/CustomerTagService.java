package cn.cordys.crm.customer.service;

import cn.cordys.common.utils.BeanUtils;
import cn.cordys.common.utils.IDGenerator;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagBindRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTagService {

    @Resource
    private ExtCustomerTagMapper extCustomerTagMapper;

    public CustomerTag add(CustomerTagAddRequest request, String userId, String orgId) {
        CustomerTag tag = BeanUtils.copyBean(new CustomerTag(), request);
        tag.setId(IDGenerator.nextStr());
        tag.setOrganizationId(orgId);
        tag.setCreateTime(System.currentTimeMillis());
        tag.setUpdateTime(System.currentTimeMillis());
        tag.setCreateUser(userId);
        tag.setUpdateUser(userId);
        if (StringUtils.isBlank(tag.getColor())) {
            tag.setColor("#1890ff");
        }
        extCustomerTagMapper.insertTag(tag);
        return tag;
    }

    public CustomerTag update(CustomerTagUpdateRequest request, String userId, String orgId) {
        CustomerTag tag = extCustomerTagMapper.selectTagById(request.getId());
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        if (!tag.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("无权限操作此标签");
        }
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setUpdateTime(System.currentTimeMillis());
        tag.setUpdateUser(userId);
        extCustomerTagMapper.updateTag(tag);
        return tag;
    }

    public void delete(String id, String orgId) {
        CustomerTag tag = extCustomerTagMapper.selectTagById(id);
        if (tag == null) {
            return;
        }
        if (!tag.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("无权限操作此标签");
        }
        extCustomerTagMapper.deleteTag(id);
        extCustomerTagMapper.deleteRelationByTagId(id);
    }

    public Page<CustomerTagResponse> list(CustomerTagPageRequest request, String orgId) {
        Page<CustomerTagResponse> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        extCustomerTagMapper.selectTagList(orgId, request.getName());
        return page;
    }

    public List<CustomerTagResponse> listAll(String orgId) {
        List<CustomerTag> tags = extCustomerTagMapper.selectTagList(orgId, null);
        return tags.stream().map(tag -> BeanUtils.copyBean(new CustomerTagResponse(), tag)).collect(Collectors.toList());
    }

    public List<CustomerTagResponse> getTagsByCustomerId(String customerId) {
        List<CustomerTag> tags = extCustomerTagMapper.selectTagsByCustomerId(customerId);
        return tags.stream().map(tag -> BeanUtils.copyBean(new CustomerTagResponse(), tag)).collect(Collectors.toList());
    }

    public void bindTags(CustomerTagBindRequest request, String userId, String orgId) {
        String customerId = request.getCustomerId();
        List<String> tagIds = request.getTagIds();
        
        List<CustomerTagRelation> existingRelations = extCustomerTagMapper.selectRelationsByCustomerId(customerId);
        List<String> existingTagIds = existingRelations.stream()
                .map(CustomerTagRelation::getTagId)
                .collect(Collectors.toList());
        
        List<String> toAdd = tagIds.stream()
                .filter(tagId -> !existingTagIds.contains(tagId))
                .collect(Collectors.toList());
        
        List<String> toRemove = existingTagIds.stream()
                .filter(tagId -> !tagIds.contains(tagId))
                .collect(Collectors.toList());
        
        if (CollectionUtils.isNotEmpty(toAdd)) {
            for (String tagId : toAdd) {
                CustomerTag tag = extCustomerTagMapper.selectTagById(tagId);
                if (tag != null && tag.getOrganizationId().equals(orgId)) {
                    CustomerTagRelation relation = new CustomerTagRelation();
                    relation.setId(IDGenerator.nextStr());
                    relation.setCustomerId(customerId);
                    relation.setTagId(tagId);
                    relation.setCreateTime(System.currentTimeMillis());
                    relation.setCreateUser(userId);
                    extCustomerTagMapper.insertRelation(relation);
                }
            }
        }
        
        if (CollectionUtils.isNotEmpty(toRemove)) {
            extCustomerTagMapper.deleteRelationByCustomerIdAndTagIds(customerId, toRemove);
        }
    }

    public void unbindTag(String customerId, String tagId, String orgId) {
        extCustomerTagMapper.deleteRelationByCustomerIdAndTagId(customerId, tagId);
    }

    public List<String> getCustomerIdsByTagId(String tagId) {
        List<CustomerTagRelation> relations = extCustomerTagMapper.selectRelationsByTagId(tagId);
        return relations.stream().map(CustomerTagRelation::getCustomerId).collect(Collectors.toList());
    }
}
