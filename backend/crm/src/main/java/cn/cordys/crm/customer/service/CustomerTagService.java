package cn.cordys.crm.customer.service;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.dto.DeptDataPermissionDTO;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.pager.PageUtils;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.service.DataScopeService;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.BeanUtils;
import cn.cordys.common.util.Translator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import cn.cordys.crm.customer.dto.request.CustomerTagAddRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagRelationRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagUpdateRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import cn.cordys.security.SessionUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTagService {
    @Resource
    private BaseMapper<CustomerTag> customerTagMapper;
    @Resource
    private BaseMapper<CustomerTagRelation> customerTagRelationMapper;
    @Resource
    private ExtCustomerTagMapper extCustomerTagMapper;

    public Pager<CustomerTagResponse> list(String keyword, Integer current, Integer pageSize, String orgId) {
        Page<Object> page = PageHelper.startPage(current, pageSize);
        List<CustomerTag> list = extCustomerTagMapper.list(keyword, orgId);
        
        List<CustomerTagResponse> responseList = list.stream().map(tag -> {
            CustomerTagResponse response = BeanUtils.copyBean(new CustomerTagResponse(), tag);
            int customerCount = extCustomerTagMapper.countCustomerByTagId(tag.getId(), orgId);
            response.setCustomerCount(customerCount);
            return response;
        }).collect(Collectors.toList());
        
        return PageUtils.setPageInfo(page, responseList);
    }

    public List<CustomerTagResponse> listAll(String orgId) {
        LambdaQueryWrapper<CustomerTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerTag::getOrganizationId, orgId);
        queryWrapper.eq(CustomerTag::getEnable, true);
        queryWrapper.orderByDesc(CustomerTag::getCreateTime);
        
        List<CustomerTag> list = customerTagMapper.selectList(queryWrapper);
        
        return list.stream().map(tag -> {
            CustomerTagResponse response = BeanUtils.copyBean(new CustomerTagResponse(), tag);
            int customerCount = extCustomerTagMapper.countCustomerByTagId(tag.getId(), orgId);
            response.setCustomerCount(customerCount);
            return response;
        }).collect(Collectors.toList());
    }

    public List<CustomerTagResponse> getByCustomerId(String customerId, String orgId) {
        return extCustomerTagMapper.selectByCustomerId(customerId, orgId);
    }

    public CustomerTag add(CustomerTagAddRequest request, String userId, String orgId) {
        int existCount = extCustomerTagMapper.countByName(request.getName(), orgId, null);
        if (existCount > 0) {
            throw new GenericException(Translator.get("标签名称已存在"));
        }
        
        CustomerTag tag = BeanUtils.copyBean(new CustomerTag(), request);
        tag.setId(IDGenerator.nextStr());
        tag.setCreateTime(System.currentTimeMillis());
        tag.setUpdateTime(System.currentTimeMillis());
        tag.setCreateUser(userId);
        tag.setUpdateUser(userId);
        tag.setOrganizationId(orgId);
        tag.setEnable(true);
        
        if (StringUtils.isBlank(tag.getColor())) {
            tag.setColor("#1677ff");
        }
        
        customerTagMapper.insert(tag);
        return tag;
    }

    public CustomerTag update(CustomerTagUpdateRequest request, String userId, String orgId) {
        int existCount = extCustomerTagMapper.countByName(request.getName(), orgId, request.getId());
        if (existCount > 0) {
            throw new GenericException(Translator.get("标签名称已存在"));
        }
        
        CustomerTag tag = BeanUtils.copyBean(new CustomerTag(), request);
        tag.setUpdateTime(System.currentTimeMillis());
        tag.setUpdateUser(userId);
        
        customerTagMapper.updateById(tag);
        return tag;
    }

    public void delete(String id, String orgId) {
        CustomerTag tag = customerTagMapper.selectByPrimaryKey(id);
        if (tag == null) {
            return;
        }
        
        extCustomerTagMapper.deleteRelationByTagId(id, orgId);
        customerTagMapper.deleteByPrimaryKey(id);
    }

    public void setCustomerTags(CustomerTagRelationRequest request, String userId, String orgId) {
        String customerId = request.getCustomerId();
        List<String> tagIds = request.getTagIds();
        
        extCustomerTagMapper.deleteRelationByCustomerId(customerId, orgId);
        
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        
        for (String tagId : tagIds) {
            CustomerTagRelation relation = new CustomerTagRelation();
            relation.setId(IDGenerator.nextStr());
            relation.setCustomerId(customerId);
            relation.setTagId(tagId);
            relation.setOrganizationId(orgId);
            relation.setCreateTime(System.currentTimeMillis());
            relation.setUpdateTime(System.currentTimeMillis());
            relation.setCreateUser(userId);
            relation.setUpdateUser(userId);
            relation.setEnable(true);
            
            customerTagRelationMapper.insert(relation);
        }
    }

    public List<String> getCustomerIdsByTagIds(List<String> tagIds, String orgId) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return List.of();
        }
        return extCustomerTagMapper.selectCustomerIdsByTagIds(tagIds, orgId);
    }
}
