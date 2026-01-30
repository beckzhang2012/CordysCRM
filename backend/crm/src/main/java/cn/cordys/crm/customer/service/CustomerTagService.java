package cn.cordys.crm.customer.service;

import cn.cordys.common.dto.OptionDTO;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.mapper.ExtCustomerTagMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTagService {

    @Resource
    private BaseMapper<CustomerTag> customerTagMapper;

    @Resource
    private ExtCustomerTagMapper extCustomerTagMapper;

    public List<CustomerTag> list(String keyword, String orgId) {
        return extCustomerTagMapper.list(keyword, orgId);
    }

    public CustomerTag getById(String id) {
        return customerTagMapper.selectByPrimaryKey(id);
    }

    public void add(CustomerTag customerTag, String orgId) {
        if (StringUtils.isBlank(customerTag.getName())) {
            throw new GenericException(CrmHttpResultCode.VALIDATE_FAILED.getCode(), "标签名称不能为空");
        }
        if (extCustomerTagMapper.checkNameExists(customerTag.getName(), orgId, null)) {
            throw new GenericException(CrmHttpResultCode.VALIDATE_FAILED.getCode(), "标签名称已存在");
        }
        customerTag.setId(IDGenerator.nextStr());
        customerTag.setOrganizationId(orgId);
        customerTagMapper.insert(customerTag);
    }

    public void update(CustomerTag customerTag, String orgId) {
        if (StringUtils.isBlank(customerTag.getName())) {
            throw new GenericException(CrmHttpResultCode.VALIDATE_FAILED.getCode(), "标签名称不能为空");
        }
        if (extCustomerTagMapper.checkNameExists(customerTag.getName(), orgId, customerTag.getId())) {
            throw new GenericException(CrmHttpResultCode.VALIDATE_FAILED.getCode(), "标签名称已存在");
        }
        customerTagMapper.updateById(customerTag);
    }

    public void delete(String id) {
        customerTagMapper.deleteByPrimaryKey(id);
        extCustomerTagMapper.deleteRelByCustomerIdAndTagIds(null, List.of(id));
    }

    public void addTagsToCustomer(String customerId, List<String> tagIds, String orgId) {
        if (tagIds != null && !tagIds.isEmpty()) {
            extCustomerTagMapper.batchInsertRel(customerId, tagIds, orgId);
        }
    }

    public void removeTagsFromCustomer(String customerId, List<String> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            extCustomerTagMapper.deleteRelByCustomerIdAndTagIds(customerId, tagIds);
        }
    }

    public List<CustomerTag> getTagsByCustomerId(String customerId) {
        return extCustomerTagMapper.selectTagsByCustomerId(customerId);
    }

    public List<OptionDTO> getTagOptions(String keyword, String orgId) {
        return extCustomerTagMapper.getTagOptions(keyword, orgId);
    }

    public List<String> getCustomerIdsByTagIds(List<String> tagIds, String orgId) {
        return extCustomerTagMapper.selectCustomerIdsByTagIds(tagIds, orgId);
    }
}
