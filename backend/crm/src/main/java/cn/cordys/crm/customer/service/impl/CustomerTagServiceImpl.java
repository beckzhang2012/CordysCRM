package cn.cordys.crm.customer.service.impl;

import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.Pager;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagRelationRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagListResponse;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.mapper.CustomerTagMapper;
import cn.cordys.crm.customer.mapper.CustomerTagRelationMapper;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import cn.cordys.crm.customer.service.CustomerTagService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Date;

/**
 * 客户标签服务实现
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
@Service
public class CustomerTagServiceImpl implements CustomerTagService {

    @Resource
    private CustomerTagMapper customerTagMapper;

    @Resource
    private CustomerTagRelationMapper customerTagRelationMapper;

    @Resource
    private ExtCustomerTagMapper extCustomerTagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addTag(CustomerTagAddRequest request, String organizationId, String createBy) {
        CustomerTag customerTag = new CustomerTag();
        BeanUtils.copyProperties(request, customerTag);
        customerTag.setId(UUID.randomUUID().toString().replace("-", ""));
        customerTag.setOrganizationId(organizationId);
        customerTag.setCreateBy(createBy);
        customerTag.setCreateTime(System.currentTimeMillis());
        customerTag.setUpdateTime(System.currentTimeMillis());
        customerTagMapper.insertCustomerTag(customerTag);
        return customerTag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTag(CustomerTagUpdateRequest request) {
        CustomerTag customerTag = customerTagMapper.selectCustomerTagById(request.getId());
        if (customerTag == null) {
            return false;
        }
        
        if (request.getName() != null) {
            customerTag.setName(request.getName());
        }
        if (request.getColor() != null) {
            customerTag.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            customerTag.setDescription(request.getDescription());
        }
        
        customerTag.setUpdateTime(System.currentTimeMillis());
        return customerTagMapper.updateCustomerTag(customerTag) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTag(String id) {
        // 删除标签
        int result = customerTagMapper.deleteCustomerTagById(id);
        
        // 删除客户与标签的关联关系
        // 这里需要添加一个方法来删除所有与该标签关联的关系
        // 暂时跳过，需要在CustomerTagRelationMapper中添加相应方法
        
        return result > 0;
    }

    @Override
    public Pager<List<CustomerTagResponse>> getTagList(CustomerTagPageRequest request, String organizationId) {
        // 设置组织ID
        request.setOrganizationId(organizationId);
        
        PageHelper.startPage(request.getCurrent(), request.getPageSize());
        List<CustomerTagListResponse> list = customerTagMapper.selectCustomerTagList(request);
        List<CustomerTagResponse> responseList = list.stream().map(item -> {
            CustomerTagResponse response = new CustomerTagResponse();
            BeanUtils.copyProperties(item, response);
            return response;
        }).collect(Collectors.toList());
        
        // 使用PageUtils的setPageInfo方法
        Page<?> page = (Page<?>) list;
        return PageUtils.setPageInfo(page, responseList);
    }

    @Override
    public CustomerTagResponse getTag(String id) {
        CustomerTag customerTag = customerTagMapper.selectCustomerTagById(id);
        if (customerTag == null) {
            return null;
        }
        CustomerTagResponse response = new CustomerTagResponse();
        BeanUtils.copyProperties(customerTag, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean relateCustomerTags(CustomerTagRelationRequest request, String organizationId, String createBy) {
        // 先删除原有的关联关系
        customerTagRelationMapper.deleteCustomerTagRelationsByCustomerId(request.getCustomerId());
        
        // 添加新的关联关系
        if (!CollectionUtils.isEmpty(request.getTagIds())) {
            List<CustomerTagRelation> relations = new ArrayList<>();
            long now = System.currentTimeMillis();
            
            for (String tagId : request.getTagIds()) {
                CustomerTagRelation relation = new CustomerTagRelation();
                relation.setId(UUID.randomUUID().toString().replace("-", ""));
                relation.setCustomerId(request.getCustomerId());
                relation.setTagId(tagId);
                relation.setOrganizationId(organizationId);
                relation.setCreateBy(createBy);
                relation.setCreateTime(now);
                relation.setUpdateTime(now);
                relations.add(relation);
            }
            
            for (CustomerTagRelation relation : relations) {
                customerTagRelationMapper.insertCustomerTagRelation(relation);
            }
        }
        
        return true;
    }

    @Override
    public CustomerTagListResponse getCustomerTags(String customerId) {
        List<CustomerTagRelation> relations = customerTagRelationMapper.selectCustomerTagRelationsByCustomerId(customerId);
        CustomerTagListResponse response = new CustomerTagListResponse();
        // 这里需要根据tagId查询标签信息，暂时返回空列表
        response.setTags(new ArrayList<>());
        return response;
    }

    @Override
    public List<String> getCustomerIdsByTagId(String tagId, String organizationId) {
        return customerTagRelationMapper.selectCustomerIdsByTagId(tagId);
    }
}