package cn.cordys.crm.contract;

import cn.cordys.common.constants.InternalUser;
import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.base.BaseTest;
import cn.cordys.crm.contract.constants.ContractPaymentPlanStatus;
import cn.cordys.crm.contract.domain.Contract;
import cn.cordys.crm.contract.domain.ContractPaymentPlan;
import cn.cordys.crm.contract.dto.request.ContractPaymentPlanAddRequest;
import cn.cordys.crm.contract.dto.request.ContractPaymentPlanPageRequest;
import cn.cordys.crm.contract.dto.request.ContractPaymentPlanUpdateRequest;
import cn.cordys.crm.contract.dto.response.ContractPaymentPlanListResponse;
import cn.cordys.crm.contract.dto.response.ContractPaymentPlanGetResponse;
import cn.cordys.common.util.JSON;
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
import java.util.Map;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContractPaymentPlanControllerTests extends BaseTest {
    private static final String BASE_PATH = "/contract/payment-plan/";
    private static final String TAB = "tab";

    private static ContractPaymentPlan addContractPaymentPlan;
    private static String amountCheckContractId;
    private static final BigDecimal CONTRACT_AMOUNT = BigDecimal.valueOf(1000);

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
    @Order(10)
    void delete() throws Exception {
        this.requestGetWithOk(DEFAULT_DELETE, addContractPaymentPlan.getId());
        ContractPaymentPlan contractPaymentPlan = contractPaymentPlanMapper.selectByPrimaryKey(addContractPaymentPlan.getId());
        Assertions.assertNull(contractPaymentPlan);
        // 校验权限
        requestGetPermissionTest(PermissionConstants.CONTRACT_PAYMENT_PLAN_DELETE, DEFAULT_DELETE, "1111");
    }

    private void createTestContract() {
        Contract contract = new Contract();
        contract.setId(IDGenerator.nextStr());
        contract.setName("PaymentPlanAmountTestContract");
        contract.setAmount(CONTRACT_AMOUNT);
        contract.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        contract.setOwner(InternalUser.ADMIN.getValue());
        contract.setCreateTime(System.currentTimeMillis());
        contract.setUpdateTime(System.currentTimeMillis());
        contract.setCreateUser(InternalUser.ADMIN.getValue());
        contract.setUpdateUser(InternalUser.ADMIN.getValue());
        contractMapper.insert(contract);
        amountCheckContractId = contract.getId();
    }

    @Test
    @Order(11)
    void testAddPlanAmountExceedContractAmount() throws Exception {
        createTestContract();

        ContractPaymentPlanAddRequest request1 = new ContractPaymentPlanAddRequest();
        request1.setPlanAmount(BigDecimal.valueOf(600));
        request1.setOwner(InternalUser.ADMIN.getValue());
        request1.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        request1.setContractId(amountCheckContractId);
        MvcResult mvcResult1 = this.requestPostWithOkAndReturn(DEFAULT_ADD, request1);
        ContractPaymentPlan plan1 = getResultData(mvcResult1, ContractPaymentPlan.class);
        Assertions.assertNotNull(plan1.getId());

        ContractPaymentPlanAddRequest request2 = new ContractPaymentPlanAddRequest();
        request2.setPlanAmount(BigDecimal.valueOf(500));
        request2.setOwner(InternalUser.ADMIN.getValue());
        request2.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        request2.setContractId(amountCheckContractId);
        ResultActions resultActions = this.requestPost(DEFAULT_ADD, request2);
        MvcResult mvcResult2 = resultActions.andReturn();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult2.getResponse().getStatus());
        String responseContent = mvcResult2.getResponse().getContentAsString();
        Map<String, Object> response = JSON.parseMap(responseContent);
        String messageDetail = (String) response.get("messageDetail");
        Assertions.assertTrue(messageDetail.contains("回款总额超出合同金额") || messageDetail.contains("exceeds contract amount"));

        contractPaymentPlanMapper.deleteByPrimaryKey(plan1.getId());
    }

    @Test
    @Order(12)
    void testUpdatePlanAmountExceedContractAmount() throws Exception {
        ContractPaymentPlanAddRequest addRequest = new ContractPaymentPlanAddRequest();
        addRequest.setPlanAmount(BigDecimal.valueOf(600));
        addRequest.setOwner(InternalUser.ADMIN.getValue());
        addRequest.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        addRequest.setContractId(amountCheckContractId);
        MvcResult mvcResult1 = this.requestPostWithOkAndReturn(DEFAULT_ADD, addRequest);
        ContractPaymentPlan plan = getResultData(mvcResult1, ContractPaymentPlan.class);
        Assertions.assertNotNull(plan.getId());

        ContractPaymentPlanAddRequest addRequest2 = new ContractPaymentPlanAddRequest();
        addRequest2.setPlanAmount(BigDecimal.valueOf(300));
        addRequest2.setOwner(InternalUser.ADMIN.getValue());
        addRequest2.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        addRequest2.setContractId(amountCheckContractId);
        MvcResult mvcResult2 = this.requestPostWithOkAndReturn(DEFAULT_ADD, addRequest2);
        ContractPaymentPlan plan2 = getResultData(mvcResult2, ContractPaymentPlan.class);
        Assertions.assertNotNull(plan2.getId());

        ContractPaymentPlanUpdateRequest updateRequest = new ContractPaymentPlanUpdateRequest();
        updateRequest.setId(plan.getId());
        updateRequest.setPlanAmount(BigDecimal.valueOf(800));
        ResultActions resultActions = this.requestPost(DEFAULT_UPDATE, updateRequest);
        MvcResult mvcResult3 = resultActions.andReturn();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult3.getResponse().getStatus());
        String responseContent = mvcResult3.getResponse().getContentAsString();
        Map<String, Object> response = JSON.parseMap(responseContent);
        String messageDetail = (String) response.get("messageDetail");
        Assertions.assertTrue(messageDetail.contains("回款总额超出合同金额") || messageDetail.contains("exceeds contract amount"));

        ContractPaymentPlan updatedPlan = contractPaymentPlanMapper.selectByPrimaryKey(plan.getId());
        Assertions.assertEquals(0, BigDecimal.valueOf(600).compareTo(updatedPlan.getPlanAmount()));

        contractPaymentPlanMapper.deleteByPrimaryKey(plan.getId());
        contractPaymentPlanMapper.deleteByPrimaryKey(plan2.getId());
    }

    @Test
    @Order(13)
    void testTotalAmountEqualToContractAmountSuccess() throws Exception {
        ContractPaymentPlanAddRequest addRequest1 = new ContractPaymentPlanAddRequest();
        addRequest1.setPlanAmount(BigDecimal.valueOf(600));
        addRequest1.setOwner(InternalUser.ADMIN.getValue());
        addRequest1.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        addRequest1.setContractId(amountCheckContractId);
        MvcResult mvcResult1 = this.requestPostWithOkAndReturn(DEFAULT_ADD, addRequest1);
        ContractPaymentPlan plan1 = getResultData(mvcResult1, ContractPaymentPlan.class);
        Assertions.assertNotNull(plan1.getId());

        ContractPaymentPlanAddRequest addRequest2 = new ContractPaymentPlanAddRequest();
        addRequest2.setPlanAmount(BigDecimal.valueOf(400));
        addRequest2.setOwner(InternalUser.ADMIN.getValue());
        addRequest2.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        addRequest2.setContractId(amountCheckContractId);
        MvcResult mvcResult2 = this.requestPostWithOkAndReturn(DEFAULT_ADD, addRequest2);
        ContractPaymentPlan plan2 = getResultData(mvcResult2, ContractPaymentPlan.class);
        Assertions.assertNotNull(plan2.getId());

        ContractPaymentPlan savedPlan1 = contractPaymentPlanMapper.selectByPrimaryKey(plan1.getId());
        ContractPaymentPlan savedPlan2 = contractPaymentPlanMapper.selectByPrimaryKey(plan2.getId());
        BigDecimal total = savedPlan1.getPlanAmount().add(savedPlan2.getPlanAmount());
        Assertions.assertEquals(0, CONTRACT_AMOUNT.compareTo(total));

        contractPaymentPlanMapper.deleteByPrimaryKey(plan1.getId());
        contractPaymentPlanMapper.deleteByPrimaryKey(plan2.getId());
        contractMapper.deleteByPrimaryKey(amountCheckContractId);
    }

    @Test
    @Order(14)
    void testAddPlanAmountMustBePositive() throws Exception {
        createTestContract();

        ContractPaymentPlanAddRequest addRequest = new ContractPaymentPlanAddRequest();
        addRequest.setPlanAmount(BigDecimal.ZERO);
        addRequest.setOwner(InternalUser.ADMIN.getValue());
        addRequest.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        addRequest.setContractId(amountCheckContractId);
        ResultActions resultActions = this.requestPost(DEFAULT_ADD, addRequest);
        MvcResult mvcResult = resultActions.andReturn();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        String responseContent = mvcResult.getResponse().getContentAsString();
        Map<String, Object> response = JSON.parseMap(responseContent);
        String messageDetail = (String) response.get("messageDetail");
        Assertions.assertTrue(messageDetail.contains("单期回款金额必须大于0") || messageDetail.contains("greater than 0"));

        contractMapper.deleteByPrimaryKey(amountCheckContractId);
    }

    @Test
    @Order(15)
    void testUpdatePlanAmountToZeroOrNegative() throws Exception {
        createTestContract();

        ContractPaymentPlanAddRequest addRequest = new ContractPaymentPlanAddRequest();
        addRequest.setPlanAmount(BigDecimal.valueOf(500));
        addRequest.setOwner(InternalUser.ADMIN.getValue());
        addRequest.setPlanStatus(ContractPaymentPlanStatus.PENDING.name());
        addRequest.setContractId(amountCheckContractId);
        MvcResult mvcResult1 = this.requestPostWithOkAndReturn(DEFAULT_ADD, addRequest);
        ContractPaymentPlan plan = getResultData(mvcResult1, ContractPaymentPlan.class);
        Assertions.assertNotNull(plan.getId());

        ContractPaymentPlanUpdateRequest updateRequestZero = new ContractPaymentPlanUpdateRequest();
        updateRequestZero.setId(plan.getId());
        updateRequestZero.setPlanAmount(BigDecimal.ZERO);
        ResultActions resultActions1 = this.requestPost(DEFAULT_UPDATE, updateRequestZero);
        MvcResult mvcResult2 = resultActions1.andReturn();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult2.getResponse().getStatus());
        String responseContent1 = mvcResult2.getResponse().getContentAsString();
        Map<String, Object> response1 = JSON.parseMap(responseContent1);
        String messageDetail1 = (String) response1.get("messageDetail");
        Assertions.assertTrue(messageDetail1.contains("单期回款金额必须大于0") || messageDetail1.contains("greater than 0"));

        ContractPaymentPlanUpdateRequest updateRequestNegative = new ContractPaymentPlanUpdateRequest();
        updateRequestNegative.setId(plan.getId());
        updateRequestNegative.setPlanAmount(BigDecimal.valueOf(-100));
        ResultActions resultActions2 = this.requestPost(DEFAULT_UPDATE, updateRequestNegative);
        MvcResult mvcResult3 = resultActions2.andReturn();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult3.getResponse().getStatus());
        String responseContent2 = mvcResult3.getResponse().getContentAsString();
        Map<String, Object> response2 = JSON.parseMap(responseContent2);
        String messageDetail2 = (String) response2.get("messageDetail");
        Assertions.assertTrue(messageDetail2.contains("单期回款金额必须大于0") || messageDetail2.contains("greater than 0"));

        ContractPaymentPlan unchangedPlan = contractPaymentPlanMapper.selectByPrimaryKey(plan.getId());
        Assertions.assertEquals(0, BigDecimal.valueOf(500).compareTo(unchangedPlan.getPlanAmount()));

        contractPaymentPlanMapper.deleteByPrimaryKey(plan.getId());
        contractMapper.deleteByPrimaryKey(amountCheckContractId);
    }
}