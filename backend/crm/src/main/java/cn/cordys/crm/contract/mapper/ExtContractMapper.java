package cn.cordys.crm.contract.mapper;

import cn.cordys.common.dto.DeptDataPermissionDTO;
import cn.cordys.crm.contract.domain.Contract;
import cn.cordys.crm.contract.dto.request.ContractPageRequest;
import cn.cordys.crm.contract.dto.response.ContractListResponse;
import cn.cordys.crm.contract.dto.response.ContractResponse;
import cn.cordys.crm.contract.dto.response.CustomerContractStatisticResponse;
import cn.cordys.crm.customer.dto.request.CustomerMergeRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtContractMapper {


    List<ContractListResponse> list(@Param("request") ContractPageRequest request, @Param("orgId") String orgId,
                                    @Param("userId") String userId, @Param("dataPermission") DeptDataPermissionDTO deptDataPermission, @Param("source") boolean source);

    ContractResponse getDetail(@Param("id") String id);

    List<ContractListResponse> getListByIds(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("orgId") String orgId, @Param("dataPermission") DeptDataPermissionDTO deptDataPermission);

    CustomerContractStatisticResponse calculateContractStatisticByCustomerId(@Param("customerId")  String customerId, @Param("userId")  String userId, @Param("orgId") String orgId, @Param("dataPermission") DeptDataPermissionDTO deptDataPermission);

    List<String> selectByStatusAndIds(@Param("ids") List<String> ids, @Param("approvalStatus") String approvalStatus);

    void updateStatus(@Param("id") String id, @Param("approvalStatus") String approvalStatus, @Param("userId") String userId, @Param("updateTime") long updateTime);

    /**
     * 批量合并客户合同
     *
     * @param request 请求参数
     * @param userId  用户ID
     * @param orgId   组织ID
     */
    void batchMerge(@Param("request") CustomerMergeRequest request, @Param("userId") String userId, @Param("orgId") String orgId);

    /**
     * 获取待合并的客户合同列表
     *
     * @param request 请求参数
     * @param orgId   组织ID
     *
     * @return 客户合同列表
     */
    List<Contract> getMergeContractList(@Param("request") CustomerMergeRequest request, @Param("orgId") String orgId);

    /**
     * 统计待合并客户的回款计划数量
     *
     * @param customerId 客户ID
     * @param orgId      组织ID
     *
     * @return 回款计划数量
     */
    int countPaymentPlansByCustomerId(@Param("customerId") String customerId, @Param("orgId") String orgId);
}
