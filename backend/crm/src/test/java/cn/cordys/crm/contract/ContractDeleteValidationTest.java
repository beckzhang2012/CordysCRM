package cn.cordys.crm.contract;

import cn.cordys.common.constants.InternalUser;
import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.pager.Pager;
import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.base.BaseTest;
import cn.cordys.crm.contract.constants.ContractStage;
import cn.cordys.crm.contract.domain.Contract;
import cn.cordys.crm.contract.domain.ContractPaymentPlan;
import cn.cordys.crm.contract.dto.request.ContractPageRequest;
import cn.cordys.crm.contract.dto.response.ContractListResponse;
import cn.cordys.crm.customer.domain.Customer;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContractDeleteValidationTest extends BaseTest {
    private static final String BASE_PATH = "/contract/";
    private static final String DELETE_PATH = "delete/{0}";

    private static Customer testCustomer;
    private static Contract normalContract;
    private static Contract archivedContract;
    private static Contract contractWithPaymentPlan;
    private static ContractPaymentPlan paymentPlan;

    @Resource
    private BaseMapper<Customer> customerMapper;
    @Resource
    private BaseMapper<Contract> contractMapper;
    @Resource
    private BaseMapper<ContractPaymentPlan> contractPaymentPlanMapper;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    void testSetupTestData() {
        testCustomer = new Customer();
        testCustomer.setId(IDGenerator.nextStr());
        testCustomer.setName("test_customer_for_delete_validation");
        testCustomer.setOwner(InternalUser.ADMIN.getValue());
        testCustomer.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        testCustomer.setCreateTime(System.currentTimeMillis());
        testCustomer.setUpdateTime(System.currentTimeMillis());
        testCustomer.setCreateUser(InternalUser.ADMIN.getValue());
        testCustomer.setUpdateUser(InternalUser.ADMIN.getValue());
        testCustomer.setInSharedPool(false);
        customerMapper.insert(testCustomer);

        normalContract = createTestContract("normal_contract", ContractStage.SIGNED.name());
        archivedContract = createTestContract("archived_contract", ContractStage.ARCHIVED.name());
        contractWithPaymentPlan = createTestContract("contract_with_payment_plan", ContractStage.SIGNED.name());

        paymentPlan = new ContractPaymentPlan();
        paymentPlan.setId(IDGenerator.nextStr());
        paymentPlan.setContractId(contractWithPaymentPlan.getId());
        paymentPlan.setOwner(InternalUser.ADMIN.getValue());
        paymentPlan.setPlanAmount(BigDecimal.valueOf(1000));
        paymentPlan.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        paymentPlan.setCreateTime(System.currentTimeMillis());
        paymentPlan.setUpdateTime(System.currentTimeMillis());
        paymentPlan.setCreateUser(InternalUser.ADMIN.getValue());
        paymentPlan.setUpdateUser(InternalUser.ADMIN.getValue());
        contractPaymentPlanMapper.insert(paymentPlan);
    }

    private Contract createTestContract(String name, String stage) {
        Contract contract = new Contract();
        contract.setId(IDGenerator.nextStr());
        contract.setName(name);
        contract.setCustomerId(testCustomer.getId());
        contract.setOwner(InternalUser.ADMIN.getValue());
        contract.setStage(stage);
        contract.setAmount(BigDecimal.valueOf(10000));
        contract.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        contract.setCreateTime(System.currentTimeMillis());
        contract.setUpdateTime(System.currentTimeMillis());
        contract.setCreateUser(InternalUser.ADMIN.getValue());
        contract.setUpdateUser(InternalUser.ADMIN.getValue());
        contractMapper.insert(contract);
        return contract;
    }

    @Test
    @Order(1)
    void testDeleteArchivedContractShouldFail() throws Exception {
        ResultActions resultActions = this.requestGet(DELETE_PATH, archivedContract.getId());
        resultActions.andExpect(status().is4xxClientError());
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);

        Contract contract = contractMapper.selectByPrimaryKey(archivedContract.getId());
        Assertions.assertNotNull(contract, "Archived contract should not be deleted");
    }

    @Test
    @Order(2)
    void testDeleteContractWithPaymentPlanShouldFail() throws Exception {
        LambdaQueryWrapper<ContractPaymentPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractPaymentPlan::getContractId, contractWithPaymentPlan.getId());
        Long count = contractPaymentPlanMapper.countByExample(wrapper.getEntity());
        Assertions.assertTrue(count > 0, "Payment plan should exist");

        ResultActions resultActions = this.requestGet(DELETE_PATH, contractWithPaymentPlan.getId());
        resultActions.andExpect(status().is4xxClientError());
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);

        Contract contract = contractMapper.selectByPrimaryKey(contractWithPaymentPlan.getId());
        Assertions.assertNotNull(contract, "Contract with payment plan should not be deleted");
    }

    @Test
    @Order(3)
    void testDeleteNormalContractShouldSucceed() throws Exception {
        Contract contract = contractMapper.selectByPrimaryKey(normalContract.getId());
        Assertions.assertNotNull(contract, "Normal contract should exist before delete");

        this.requestGetWithOk(DELETE_PATH, normalContract.getId());

        Contract deletedContract = contractMapper.selectByPrimaryKey(normalContract.getId());
        Assertions.assertNull(deletedContract, "Normal contract should be deleted");
    }

    @Test
    @Order(10)
    void testCleanupTestData() {
        if (paymentPlan != null) {
            contractPaymentPlanMapper.deleteByPrimaryKey(paymentPlan.getId());
        }
        if (normalContract != null) {
            contractMapper.deleteByPrimaryKey(normalContract.getId());
        }
        if (archivedContract != null) {
            contractMapper.deleteByPrimaryKey(archivedContract.getId());
        }
        if (contractWithPaymentPlan != null) {
            contractMapper.deleteByPrimaryKey(contractWithPaymentPlan.getId());
        }
        if (testCustomer != null) {
            customerMapper.deleteByPrimaryKey(testCustomer.getId());
        }
    }
}
