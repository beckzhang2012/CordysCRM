package cn.cordys.crm.customer.service.impl;

import cn.cordys.common.domain.BaseModel;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import cn.cordys.crm.customer.service.CustomerTagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * 客户标签服务实现
 *
 * @author jianxing
 * @date 2026-01-21 10:24:22
 */
@Slf4j
@Service
public class CustomerTagServiceImpl extends ServiceImpl<ExtCustomerTagMapper, CustomerTag> implements CustomerTagService {

    private final ExtCustomerTagMapper customerTagMapper;

    public CustomerTagServiceImpl(ExtCustomerTagMapper customerTagMapper) {
        this.customerTagMapper = customerTagMapper;
    }

    @Override
    public Page<CustomerTagResponse> getTagPage(CustomerTagPageRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("organizationId", request.getOrganizationId());
        params.put("name", request.getName());
        params.put("category", request.getCategory());
        params.put("isSystem", request.getIsSystem());

        List<CustomerTag> tagList = customerTagMapper.selectTagList(params);
        List<CustomerTagResponse> responseList = new ArrayList<>();

        for (CustomerTag tag : tagList) {
            CustomerTagResponse response = new CustomerTagResponse();
            BeanUtils.copyProperties(tag, response);
            responseList.add(response);
        }

        Page<CustomerTagResponse> page = new Page<>();
        page.setRecords(responseList);
        page.setTotal(responseList.size());
        page.setCurrent(request.getCurrent());
        page.setSize(request.getSize());

        return page;
    }

    @Override
    public List<CustomerTagResponse> getTagList(String organizationId, String category) {
        Map<String, Object> params = new HashMap<>();
        params.put("organizationId", organizationId);
        params.put("category", category);

        List<CustomerTag> tagList = customerTagMapper.selectTagList(params);
        List<CustomerTagResponse> responseList = new ArrayList<>();

        for (CustomerTag tag : tagList) {
            CustomerTagResponse response = new CustomerTagResponse();
            BeanUtils.copyProperties(tag, response);
            responseList.add(response);
        }

        return responseList;
    }

    @Override
    public CustomerTagResponse getTagById(String id) {
        CustomerTag tag = customerTagMapper.selectTagById(id);
        if (tag == null) {
            return null;
        }

        CustomerTagResponse response = new CustomerTagResponse();
        BeanUtils.copyProperties(tag, response);
        return response;
    }

    @Override
    @Transactional
    public String createTag(CustomerTagAddRequest request, String organizationId, String creator) {
        CustomerTag tag = new CustomerTag();
        tag.setId(UUID.randomUUID().toString());
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        tag.setColor(request.getColor());
        tag.setCategory(request.getCategory());
        tag.setOrganizationId(organizationId);
        tag.setCreator(creator);
        tag.setIsSystem(false);
        tag.setUsageCount(0);
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());

        customerTagMapper.insertTag(tag);
        return tag.getId();
    }

    @Override
    @Transactional
    public boolean updateTag(CustomerTagUpdateRequest request) {
        CustomerTag tag = customerTagMapper.selectTagById(request.getId());
        if (tag == null) {
            return false;
        }

        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        tag.setColor(request.getColor());
        tag.setCategory(request.getCategory());
        tag.setUpdatedAt(LocalDateTime.now());

        return customerTagMapper.updateTag(tag) > 0;
    }

    @Override
    @Transactional
    public boolean deleteTag(String id) {
        CustomerTag tag = customerTagMapper.selectTagById(id);
        if (tag == null) {
            return false;
        }

        if (tag.getIsSystem()) {
            return false;
        }

        customerTagMapper.deleteAllTagRelationsByTagId(id);
        return customerTagMapper.deleteTag(id) > 0;
    }

    @Override
    @Transactional
    public boolean addTagsToCustomer(String customerId, List<String> tagIds, String organizationId, String adder) {
        if (tagIds == null || tagIds.isEmpty()) {
            return true;
        }

        List<CustomerTagRelation> relations = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (String tagId : tagIds) {
            CustomerTagRelation relation = new CustomerTagRelation();
            relation.setId(UUID.randomUUID().toString());
            relation.setCustomerId(customerId);
            relation.setTagId(tagId);
            relation.setOrganizationId(organizationId);
            relation.setAdder(adder);
            relation.setAddTime(currentTime);
            relation.setCreatedAt(LocalDateTime.now());
            relation.setUpdatedAt(LocalDateTime.now());
            relations.add(relation);

            customerTagMapper.incrementTagUsageCount(tagId);
        }

        return customerTagMapper.batchInsertTagRelations(relations) > 0;
    }

    @Override
    @Transactional
    public boolean removeTagsFromCustomer(String customerId, List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return true;
        }

        for (String tagId : tagIds) {
            customerTagMapper.decrementTagUsageCount(tagId);
        }

        return customerTagMapper.batchDeleteTagRelations(customerId, tagIds) > 0;
    }

    @Override
    public List<CustomerTagResponse> getCustomerTags(String customerId) {
        List<CustomerTagRelation> relations = customerTagMapper.selectTagRelationsByCustomerId(customerId);
        List<CustomerTagResponse> tagList = new ArrayList<>();

        for (CustomerTagRelation relation : relations) {
            CustomerTag tag = customerTagMapper.selectTagById(relation.getTagId());
            if (tag != null) {
                CustomerTagResponse response = new CustomerTagResponse();
                BeanUtils.copyProperties(tag, response);
                tagList.add(response);
            }
        }

        return tagList;
    }

    @Override
    @Transactional
    public int batchAddTagsToCustomers(List<String> customerIds, List<String> tagIds, String organizationId, String adder) {
        if (customerIds == null || customerIds.isEmpty() || tagIds == null || tagIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;

        for (String customerId : customerIds) {
            if (addTagsToCustomer(customerId, tagIds, organizationId, adder)) {
                successCount++;
            }
        }

        return successCount;
    }

    @Override
    @Transactional
    public int batchRemoveTagsFromCustomers(List<String> customerIds, List<String> tagIds) {
        if (customerIds == null || customerIds.isEmpty() || tagIds == null || tagIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;

        for (String customerId : customerIds) {
            if (removeTagsFromCustomer(customerId, tagIds)) {
                successCount++;
            }
        }

        return successCount;
    }

    @Override
    public int countCustomersByTagId(String tagId) {
        return customerTagMapper.countCustomersByTagId(tagId);
    }
}