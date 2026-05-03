package cn.cordys.crm.customer.service;

import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.JSON;
import cn.cordys.crm.customer.domain.CustomerMergeAudit;
import cn.cordys.crm.customer.dto.request.CustomerMergeExecuteRequest;
import cn.cordys.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerMergeAuditService {

    @Resource
    private BaseMapper<CustomerMergeAudit> customerMergeAuditMapper;

    public CustomerMergeAudit createAudit(
            CustomerMergeExecuteRequest request,
            String primaryCustomerName,
            List<String> secondaryCustomerNames,
            String ownerName,
            String operatorId,
            String operatorName,
            String orgId
    ) {
        CustomerMergeAudit audit = new CustomerMergeAudit();
        audit.setId(IDGenerator.nextStr());
        audit.setOrganizationId(orgId);
        audit.setPrimaryCustomerId(request.getToMergeId());
        audit.setPrimaryCustomerName(primaryCustomerName);
        audit.setSecondaryCustomerIds(JSON.toJSONString(request.getMergeIds()));
        audit.setSecondaryCustomerNames(JSON.toJSONString(secondaryCustomerNames));
        audit.setOwnerId(request.getOwnerId());
        audit.setOwnerName(ownerName);
        audit.setOperatorId(operatorId);
        audit.setOperatorName(operatorName);
        audit.setOperationTime(System.currentTimeMillis());
        audit.setCreateTime(System.currentTimeMillis());
        audit.setUpdateTime(System.currentTimeMillis());
        audit.setCreateUser(operatorId);
        audit.setUpdateUser(operatorId);
        audit.setStatus("IN_PROGRESS");
        
        customerMergeAuditMapper.insert(audit);
        return audit;
    }

    public void updateAuditSuccess(
            String auditId,
            Integer contactCount,
            Integer opportunityCount,
            Integer contractCount,
            Integer paymentPlanCount,
            Integer followRecordCount,
            Integer followPlanCount
    ) {
        CustomerMergeAudit audit = customerMergeAuditMapper.selectByPrimaryKey(auditId);
        if (audit != null) {
            audit.setStatus("SUCCESS");
            audit.setContactCount(contactCount);
            audit.setOpportunityCount(opportunityCount);
            audit.setContractCount(contractCount);
            audit.setPaymentPlanCount(paymentPlanCount);
            audit.setFollowRecordCount(followRecordCount);
            audit.setFollowPlanCount(followPlanCount);
            audit.setUpdateTime(System.currentTimeMillis());
            customerMergeAuditMapper.update(audit);
        }
    }

    public void updateAuditFailed(String auditId, String failReason) {
        CustomerMergeAudit audit = customerMergeAuditMapper.selectByPrimaryKey(auditId);
        if (audit != null) {
            audit.setStatus("FAILED");
            audit.setFailReason(failReason);
            audit.setUpdateTime(System.currentTimeMillis());
            customerMergeAuditMapper.update(audit);
        }
    }

    public void deleteAudit(String auditId) {
        customerMergeAuditMapper.deleteByPrimaryKey(auditId);
    }
}
