package cn.cordys.crm.contract;

import cn.cordys.common.constants.InternalUser;
import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.Translator;
import cn.cordys.crm.base.BaseTest;
import cn.cordys.crm.contract.constants.ContractPaymentPlanStatus;
import cn.cordys.crm.contract.constants.ContractStage;
import cn.cordys.crm.contract.domain.Contract;
import cn.cordys.crm.contract.domain.ContractPaymentPlan;
import cn.cordys.crm.contract.dto.request.ContractPaymentPlanAddRequest;
import cn.cordys.crm.contract.dto.request.ContractPaymentPlanPageRequest;
import cn.cordys.crm.contract.dto.request.ContractPaymentPlanUpdateRequest;
import cn.cordys.crm.contract.dto.response.ContractPaymentPlanListResponse;
import cn.cordys.crm.contract.dto.response.ContractPaymentPlanGetResponse;
import cn.cordys.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.*;
import cn.cordys.common.util.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContractPaymentPlanControllerTests extends BaseTest {
    private static final String BASE_PATH = "/contract/payment-plan/";
    private static final String TAB = "tab";

    private static ContractPaymentPlan addContractPaymentPlan;
    private static ContractPaymentPlan completedPlan;
    private static Contract archivedContract;
    private static ContractPaymentPlan planWithArchivedContract;

    @Resource
    private BaseMapper<ContractPaymentPlan> contractPaymentPlanMapper;
    @Resource
    private BaseMapper<Contract> contractMapper;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    void testPageEmpty() throws Exception {
        ContractPaymentPlanPageRequest request = new ContractPaymentPlanPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_PAGE, request);
        Pager<List<ContractPaymentPlanListResponse>> pageResult = getPageResult(mvcResult, ContractPaymentPlanListResponse.class);
        List<ContractPaymentPlanListResponse> contractPaymentPlanList = pageResult.getList();
        Assertions.assertTrue(CollectionUtils.isEmpty(contractPaymentPlanList));

        // 校验权限
        requestPostPermissionTest(PermissionConstants.CONTRACT_PAYMENT_PLAN_READ, DEFAULT_PAGE, request);
    }

    @Test
    @Order(0)
    void testTab() throws Exception {
        this.requestGetWithOkAndReturn(TAB);
        // 校验权限
        requestGetPermissionTest(PermissionConstants.CONTRACT_PAYMENT_PLAN_READ, TAB);
    }

    @Test
    @Order(1)
    void testAdd() throws Exception {
        // 请求成功
        ContractPaymentPlanAddRequest request = new ContractPaymentPlanAddRequest();
        request.setPlanAmount(BigDecimal.valueOf(111));
        request.setOwner(InternalUser.ADMIN.getValue());
        request.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        request.setContractId("test");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        ContractPaymentPlan resultData = getResultData(mvcResult, ContractPaymentPlan.class);
        ContractPaymentPlan contractPaymentPlan = contractPaymentPlanMapper.selectByPrimaryKey(resultData.getId());

        // 校验请求成功数据
        addContractPaymentPlan = contractPaymentPlan;
        Assertions.assertEquals(request.getPlanAmount().intValue(), contractPaymentPlan.getPlanAmount().intValue());

        // 校验权限
        requestPostPermissionTest(PermissionConstants.CONTRACT_PAYMENT_PLAN_ADD, DEFAULT_ADD, request);
    }

    @Test
    @Order(2)
    void testUpdate() throws Exception {
        // 请求成功
        ContractPaymentPlanUpdateRequest request = new ContractPaymentPlanUpdateRequest();
        request.setId(addContractPaymentPlan.getId());
        request.setPlanAmount(BigDecimal.valueOf(222));
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
        ContractPaymentPlan userContractPaymentPlanResult = contractPaymentPlanMapper.selectByPrimaryKey(request.getId());
        Assertions.assertEquals(request.getPlanAmount().intValue(), userContractPaymentPlanResult.getPlanAmount().intValue());

        // 不修改信息
        ContractPaymentPlanUpdateRequest emptyRequest = new ContractPaymentPlanUpdateRequest();
        emptyRequest.setId(addContractPaymentPlan.getId());
        this.requestPostWithOk(DEFAULT_UPDATE, emptyRequest);

        // 校验权限
        requestPostPermissionTest(PermissionConstants.CONTRACT_PAYMENT_PLAN_UPDATE, DEFAULT_UPDATE, request);
    }

    @Test
    @Order(3)
    void testGet() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(DEFAULT_GET, addContractPaymentPlan.getId());
        ContractPaymentPlanGetResponse getResponse = getResultData(mvcResult, ContractPaymentPlanGetResponse.class);

        // 校验请求成功数据
        ContractPaymentPlan contractPaymentPlan = contractPaymentPlanMapper.selectByPrimaryKey(addContractPaymentPlan.getId());
        ContractPaymentPlan responseContractPaymentPlan = BeanUtils.copyBean(new ContractPaymentPlan(), getResponse);
        Assertions.assertEquals(responseContractPaymentPlan, contractPaymentPlan);

        // 校验权限
        requestGetPermissionTest(PermissionConstants.CONTRACT_PAYMENT_PLAN_READ, DEFAULT_GET, addContractPaymentPlan.getId());
    }

    @Test
    @Order(4)
    void testPage() throws Exception {
        ContractPaymentPlanPageRequest request = new ContractPaymentPlanPageRequest();
        request.setCurrent(1);
        request.setPageSize(10);

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_PAGE, request);
        Pager<List<ContractPaymentPlanListResponse>> pageResult = getPageResult(mvcResult, ContractPaymentPlanListResponse.class);
        List<ContractPaymentPlanListResponse> contractPaymentPlanList = pageResult.getList();
        
        // 校验权限
        requestPostPermissionTest(PermissionConstants.CONTRACT_PAYMENT_PLAN_READ, DEFAULT_PAGE, request);
    }

    @Test
    @Order(5)
    void testDeleteCompletedPlan_ShouldFail() throws Exception {
        ContractPaymentPlanAddRequest request = new ContractPaymentPlanAddRequest();
        request.setPlanAmount(BigDecimal.valueOf(999));
        request.setOwner(InternalUser.ADMIN.getValue());
        request.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        request.setContractId("test_completed");
        MvcResult addMvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        ContractPaymentPlan resultData = getResultData(addMvcResult, ContractPaymentPlan.class);
        completedPlan = contractPaymentPlanMapper.selectByPrimaryKey(resultData.getId());

        completedPlan.setPlanStatus(ContractPaymentPlanStatus.COMPLETED.name());
        contractPaymentPlanMapper.update(completedPlan);
        completedPlan = contractPaymentPlanMapper.selectByPrimaryKey(completedPlan.getId());
        Assertions.assertEquals(ContractPaymentPlanStatus.COMPLETED.name(), completedPlan.getPlanStatus());

        ResultActions resultActions = this.requestGet(DEFAULT_DELETE, completedPlan.getId())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()));
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);
        MvcResult deleteMvcResult = resultActions.andReturn();
        String responseContent = deleteMvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(responseContent.contains(Translator.get("contract.payment.plan.completed.cannot.delete")), "Response should contain completed plan cannot delete message");

        ContractPaymentPlan stillExists = contractPaymentPlanMapper.selectByPrimaryKey(completedPlan.getId());
        Assertions.assertNotNull(stillExists);
    }

    private static ContractPaymentPlan normalDeletePlan;

    @Test
    @Order(6)
    void testDeleteNormalPlan_ShouldSuccess() throws Exception {
        ContractPaymentPlanAddRequest request = new ContractPaymentPlanAddRequest();
        request.setPlanAmount(BigDecimal.valueOf(777));
        request.setOwner(InternalUser.ADMIN.getValue());
        request.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        request.setContractId("test_normal_delete");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        ContractPaymentPlan resultData = getResultData(mvcResult, ContractPaymentPlan.class);
        normalDeletePlan = contractPaymentPlanMapper.selectByPrimaryKey(resultData.getId());

        Assertions.assertEquals(ContractPaymentPlanStatus.PENDING.name(), normalDeletePlan.getPlanStatus());

        this.requestGetWithOk(DEFAULT_DELETE, normalDeletePlan.getId());

        ContractPaymentPlan deletedPlan = contractPaymentPlanMapper.selectByPrimaryKey(normalDeletePlan.getId());
        Assertions.assertNull(deletedPlan);
    }

    @Test
    @Order(7)
    void testDeletePlanWithArchivedContract_ShouldFail() throws Exception {
        archivedContract = new Contract();
        archivedContract.setId(IDGenerator.nextStr());
        archivedContract.setName("Test Archived Contract");
        archivedContract.setStage(ContractStage.ARCHIVED.name());
        archivedContract.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        archivedContract.setCreateTime(System.currentTimeMillis());
        archivedContract.setUpdateTime(System.currentTimeMillis());
        archivedContract.setCreateUser(InternalUser.ADMIN.getValue());
        archivedContract.setUpdateUser(InternalUser.ADMIN.getValue());
        contractMapper.insert(archivedContract);

        ContractPaymentPlanAddRequest request = new ContractPaymentPlanAddRequest();
        request.setPlanAmount(BigDecimal.valueOf(888));
        request.setOwner(InternalUser.ADMIN.getValue());
        request.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        request.setContractId(archivedContract.getId());
        MvcResult addMvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        ContractPaymentPlan resultData = getResultData(addMvcResult, ContractPaymentPlan.class);
        planWithArchivedContract = contractPaymentPlanMapper.selectByPrimaryKey(resultData.getId());

        ResultActions resultActions = this.requestGet(DEFAULT_DELETE, planWithArchivedContract.getId())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()));
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);
        MvcResult deleteMvcResult = resultActions.andReturn();
        String responseContent = deleteMvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(responseContent.contains(Translator.get("contract.payment.plan.archived.contract.cannot.delete")), "Response should contain archived contract plan cannot delete message");

        ContractPaymentPlan stillExists = contractPaymentPlanMapper.selectByPrimaryKey(planWithArchivedContract.getId());
        Assertions.assertNotNull(stillExists);
    }

    @Test
    @Order(8)
    void testDeleteTestData() throws Exception {
        if (completedPlan != null) {
            completedPlan.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
            contractPaymentPlanMapper.update(completedPlan);
            this.requestGetWithOk(DEFAULT_DELETE, completedPlan.getId());
        }
        if (planWithArchivedContract != null) {
            Contract contract = contractMapper.selectByPrimaryKey(archivedContract.getId());
            if (contract != null) {
                contract.setStage(ContractStage.IN_PROGRESS.name());
                contractMapper.update(contract);
            }
            this.requestGetWithOk(DEFAULT_DELETE, planWithArchivedContract.getId());
        }
        if (archivedContract != null) {
            contractMapper.deleteByPrimaryKey(archivedContract.getId());
        }
    }

    @Test
    @Order(9)
    void delete() throws Exception {
        this.requestGetWithOk(DEFAULT_DELETE, addContractPaymentPlan.getId());
        ContractPaymentPlan contractPaymentPlan = contractPaymentPlanMapper.selectByPrimaryKey(addContractPaymentPlan.getId());
        Assertions.assertNull(contractPaymentPlan);
        // 校验权限
        requestGetPermissionTest(PermissionConstants.CONTRACT_PAYMENT_PLAN_DELETE, DEFAULT_DELETE, "1111");
    }
}