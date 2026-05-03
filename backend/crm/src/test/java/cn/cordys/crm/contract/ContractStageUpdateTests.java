package cn.cordys.crm.contract;

import cn.cordys.common.constants.InternalUser;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.Translator;
import cn.cordys.crm.base.BaseTest;
import cn.cordys.crm.contract.constants.ContractApprovalStatus;
import cn.cordys.crm.contract.constants.ContractStage;
import cn.cordys.crm.contract.domain.Contract;
import cn.cordys.crm.contract.dto.request.ContractStageRequest;
import cn.cordys.crm.contract.service.ContractService;
import cn.cordys.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContractStageUpdateTests extends BaseTest {

    @Resource
    private ContractService contractService;

    @Resource
    private BaseMapper<Contract> contractMapper;

    private static String approvedContractId;
    private static String voidedContractId;
    private static String archivedContractId;

    @BeforeEach
    void setup() {
        if (approvedContractId == null) {
            approvedContractId = createTestContract(ContractStage.IN_PROGRESS.name(), ContractApprovalStatus.APPROVED.name());
        }
        if (voidedContractId == null) {
            voidedContractId = createTestContract(ContractStage.VOID.name(), ContractApprovalStatus.APPROVED.name());
        }
        if (archivedContractId == null) {
            archivedContractId = createTestContract(ContractStage.ARCHIVED.name(), ContractApprovalStatus.APPROVED.name());
        }
    }

    private String createTestContract(String stage, String approvalStatus) {
        Contract contract = new Contract();
        contract.setId(IDGenerator.nextStr());
        contract.setName("Test Contract - " + stage);
        contract.setCustomerId("test_customer");
        contract.setOwner(InternalUser.ADMIN.getValue());
        contract.setAmount(BigDecimal.valueOf(1000));
        contract.setStage(stage);
        contract.setApprovalStatus(approvalStatus);
        contract.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        contract.setCreateTime(System.currentTimeMillis());
        contract.setCreateUser(InternalUser.ADMIN.getValue());
        contract.setUpdateTime(System.currentTimeMillis());
        contract.setUpdateUser(InternalUser.ADMIN.getValue());
        contractMapper.insert(contract);
        return contract.getId();
    }

    @Test
    @Order(1)
    void testVoidWithoutReason_ShouldBeRejected() {
        ContractStageRequest request = new ContractStageRequest();
        request.setId(approvedContractId);
        request.setStage(ContractStage.VOID.name());
        request.setVoidReason(null);

        GenericException exception = assertThrows(GenericException.class, () -> {
            contractService.updateStage(request, InternalUser.ADMIN.getValue(), DEFAULT_ORGANIZATION_ID);
        });

        assertTrue(exception.getMessage().contains(Translator.get("contract.void.reason.required")),
                "应该提示作废弃因不能为空");
        assertEquals(CrmHttpResultCode.VALIDATE_FAILED, exception.getErrorCode(),
                "errorCode 应该是 VALIDATE_FAILED");

        Contract contractAfter = contractMapper.selectByPrimaryKey(approvedContractId);
        assertEquals(ContractStage.IN_PROGRESS.name(), contractAfter.getStage(),
                "被拒后合同阶段不应该被修改");
    }

    @Test
    @Order(2)
    void testChangeStageAfterVoid_ShouldBeRejected() {
        ContractStageRequest request = new ContractStageRequest();
        request.setId(voidedContractId);
        request.setStage(ContractStage.IN_PROGRESS.name());

        GenericException exception = assertThrows(GenericException.class, () -> {
            contractService.updateStage(request, InternalUser.ADMIN.getValue(), DEFAULT_ORGANIZATION_ID);
        });

        assertTrue(exception.getMessage().contains(Translator.get("contract.stage.cannot.change.after.void.or.archived")),
                "应该提示已作废的合同不允许修改阶段");
        assertEquals(CrmHttpResultCode.VALIDATE_FAILED, exception.getErrorCode(),
                "errorCode 应该是 VALIDATE_FAILED");

        Contract contractAfter = contractMapper.selectByPrimaryKey(voidedContractId);
        assertEquals(ContractStage.VOID.name(), contractAfter.getStage(),
                "被拒后合同阶段不应该被修改");
    }

    @Test
    @Order(3)
    void testChangeStageAfterArchived_ShouldBeRejected() {
        ContractStageRequest request = new ContractStageRequest();
        request.setId(archivedContractId);
        request.setStage(ContractStage.IN_PROGRESS.name());

        GenericException exception = assertThrows(GenericException.class, () -> {
            contractService.updateStage(request, InternalUser.ADMIN.getValue(), DEFAULT_ORGANIZATION_ID);
        });

        assertTrue(exception.getMessage().contains(Translator.get("contract.stage.cannot.change.after.void.or.archived")),
                "应该提示已归档的合同不允许修改阶段");
        assertEquals(CrmHttpResultCode.VALIDATE_FAILED, exception.getErrorCode(),
                "errorCode 应该是 VALIDATE_FAILED");

        Contract contractAfter = contractMapper.selectByPrimaryKey(archivedContractId);
        assertEquals(ContractStage.ARCHIVED.name(), contractAfter.getStage(),
                "被拒后合同阶段不应该被修改");
    }

    @Test
    @Order(4)
    void testArchiveFromCompletedPerformance_ShouldSucceed() {
        String completedContractId = createTestContract(ContractStage.COMPLETED_PERFORMANCE.name(), ContractApprovalStatus.APPROVED.name());

        ContractStageRequest request = new ContractStageRequest();
        request.setId(completedContractId);
        request.setStage(ContractStage.ARCHIVED.name());

        assertDoesNotThrow(() -> {
            contractService.updateStage(request, InternalUser.ADMIN.getValue(), DEFAULT_ORGANIZATION_ID);
        }, "从履行完毕状态归档应该成功");

        Contract updatedContract = contractMapper.selectByPrimaryKey(completedContractId);
        assertEquals(ContractStage.ARCHIVED.name(), updatedContract.getStage(), "合同阶段应该已更新为归档");
    }

    @Test
    @Order(5)
    void testArchiveFromVoid_ShouldSucceed() {
        String voidForArchiveId = createTestContract(ContractStage.VOID.name(), ContractApprovalStatus.APPROVED.name());

        ContractStageRequest request = new ContractStageRequest();
        request.setId(voidForArchiveId);
        request.setStage(ContractStage.ARCHIVED.name());

        assertDoesNotThrow(() -> {
            contractService.updateStage(request, InternalUser.ADMIN.getValue(), DEFAULT_ORGANIZATION_ID);
        }, "从作废状态归档应该成功");

        Contract updatedContract = contractMapper.selectByPrimaryKey(voidForArchiveId);
        assertEquals(ContractStage.ARCHIVED.name(), updatedContract.getStage(), "合同阶段应该已更新为归档");
    }

    @Test
    @Order(6)
    void testArchiveFromInProgress_ShouldBeRejected() {
        String inProgressContractId = createTestContract(ContractStage.IN_PROGRESS.name(), ContractApprovalStatus.APPROVED.name());

        ContractStageRequest request = new ContractStageRequest();
        request.setId(inProgressContractId);
        request.setStage(ContractStage.ARCHIVED.name());

        GenericException exception = assertThrows(GenericException.class, () -> {
            contractService.updateStage(request, InternalUser.ADMIN.getValue(), DEFAULT_ORGANIZATION_ID);
        });

        assertTrue(exception.getMessage().contains(Translator.get("contract.archive.only.from.completed.or.void")),
                "应该提示只能从履行完毕或作废状态归档");
        assertEquals(CrmHttpResultCode.VALIDATE_FAILED, exception.getErrorCode(),
                "errorCode 应该是 VALIDATE_FAILED");

        Contract contractAfter = contractMapper.selectByPrimaryKey(inProgressContractId);
        assertEquals(ContractStage.IN_PROGRESS.name(), contractAfter.getStage(),
                "被拒后合同阶段不应该被修改");
    }

    @Test
    @Order(7)
    void testVoidToSigned_ShouldBeRejected() {
        String voidedContractId = createTestContract(ContractStage.VOID.name(), ContractApprovalStatus.APPROVED.name());

        ContractStageRequest request = new ContractStageRequest();
        request.setId(voidedContractId);
        request.setStage(ContractStage.SIGNED.name());

        GenericException exception = assertThrows(GenericException.class, () -> {
            contractService.updateStage(request, InternalUser.ADMIN.getValue(), DEFAULT_ORGANIZATION_ID);
        });

        assertTrue(exception.getMessage().contains(Translator.get("contract.stage.cannot.change.after.void.or.archived")),
                "应该提示已作废的合同不允许修改阶段");
        assertEquals(CrmHttpResultCode.VALIDATE_FAILED, exception.getErrorCode(),
                "errorCode 应该是 VALIDATE_FAILED");

        Contract contractAfter = contractMapper.selectByPrimaryKey(voidedContractId);
        assertEquals(ContractStage.VOID.name(), contractAfter.getStage(),
                "被拒后合同阶段不应该被修改");
    }

    @Test
    @Order(8)
    void testArchiveFromSigned_ShouldBeRejected() {
        String signedContractId = createTestContract(ContractStage.SIGNED.name(), ContractApprovalStatus.APPROVED.name());

        ContractStageRequest request = new ContractStageRequest();
        request.setId(signedContractId);
        request.setStage(ContractStage.ARCHIVED.name());

        GenericException exception = assertThrows(GenericException.class, () -> {
            contractService.updateStage(request, InternalUser.ADMIN.getValue(), DEFAULT_ORGANIZATION_ID);
        });

        assertTrue(exception.getMessage().contains(Translator.get("contract.archive.only.from.completed.or.void")),
                "应该提示只能从履行完毕或作废状态归档");
        assertEquals(CrmHttpResultCode.VALIDATE_FAILED, exception.getErrorCode(),
                "errorCode 应该是 VALIDATE_FAILED");

        Contract contractAfter = contractMapper.selectByPrimaryKey(signedContractId);
        assertEquals(ContractStage.SIGNED.name(), contractAfter.getStage(),
                "被拒后合同阶段不应该被修改");
    }
}
