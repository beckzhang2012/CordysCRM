package cn.cordys.crm.customer.service;


import cn.cordys.common.uid.IDGenerator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.request.CustomerTagRequest;
import cn.cordys.crm.customer.dto.request.CustomerTagSearchRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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

    public CustomerTagResponse getCustomerTags(String customerId) {
        List<String> tags = extCustomerTagMapper.getTagsByCustomerId(customerId);
        CustomerTagResponse response = new CustomerTagResponse();
        response.setCustomerId(customerId);
        response.setTags(tags != null ? tags : new ArrayList<>());
        return response;
    }

    public void saveCustomerTags(CustomerTagRequest request) {
        String customerId = request.getCustomerId();
        List<String> newTags = request.getTags();

        List<String> existingTags = extCustomerTagMapper.getTagsByCustomerId(customerId);

        if (CollectionUtils.isNotEmpty(existingTags)) {
            extCustomerTagMapper.deleteByCustomerId(customerId);
        }

        if (CollectionUtils.isNotEmpty(newTags)) {
            List<CustomerTag> tagsToInsert = newTags.stream()
                    .filter(tag -> tag != null && !tag.trim().isEmpty())
                    .map(tag -> {
                        CustomerTag customerTag = new CustomerTag();
                        customerTag.setId(IDGenerator.nextStr());
                        customerTag.setCustomerId(customerId);
                        customerTag.setTagName(tag.trim());
                        customerTag.setOrganizationId(OrganizationContext.getOrganizationId());
                        customerTag.setCreateUser(SessionUtils.getUserId());
                        customerTag.setCreateTime(System.currentTimeMillis());
                        return customerTag;
                    })
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(tagsToInsert)) {
                extCustomerTagMapper.batchInsert(tagsToInsert);
            }
        }

    }

    public void deleteCustomerTags(CustomerTagRequest request) {
        String customerId = request.getCustomerId();
        List<String> tags = request.getTags();

        if (CollectionUtils.isNotEmpty(tags)) {
            extCustomerTagMapper.deleteByCustomerIdAndTags(customerId, tags);
        }

    }

    public List<String> searchTags(CustomerTagSearchRequest request) {
        String keyword = request.getKeyword();
        String orgId = request.getOrganizationId() != null ? request.getOrganizationId() : OrganizationContext.getOrganizationId();
        return extCustomerTagMapper.searchTags(orgId, keyword);
    }

    public List<String> getAllTags() {
        return extCustomerTagMapper.getAllTagsByOrganizationId(OrganizationContext.getOrganizationId());
    }

    public List<String> getCustomerIdsByTags(List<String> tags) {
        return extCustomerTagMapper.getCustomerIdsByTags(tags, OrganizationContext.getOrganizationId());
    }
}
