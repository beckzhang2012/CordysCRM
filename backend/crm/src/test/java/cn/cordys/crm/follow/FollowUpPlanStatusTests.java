package cn.cordys.crm.follow;

import cn.cordys.common.domain.BaseModuleFieldValue;
import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.common.util.Translator;
import cn.cordys.crm.base.BaseTest;
import cn.cordys.crm.follow.constants.FollowUpPlanStatusType;
import cn.cordys.crm.follow.domain.FollowUpPlan;
import cn.cordys.crm.follow.dto.request.FollowUpPlanAddRequest;
import cn.cordys.crm.follow.dto.request.FollowUpPlanStatusRequest;
import cn.cordys.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FollowUpPlanStatusTests extends BaseTest {

    private static final String BASE_PATH = "/opportunity/follow/plan/";
    private static final String CANCEL_PLAN = "cancel/{0}";
    private static final String STATUS_UPDATE = "status/update";

    private static FollowUpPlan completedPlan;
    private static FollowUpPlan cancelledPlan;
    private static FollowUpPlan preparedPlan;
    private static FollowUpPlan underwayPlan;

    @Resource
    private BaseMapper<FollowUpPlan> followUpPlanMapper;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(1)
    void testSetupTestData() throws Exception {
        long timestamp = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000;

        completedPlan = createTestPlan(FollowUpPlanStatusType.COMPLETED, timestamp);
        cancelledPlan = createTestPlan(FollowUpPlanStatusType.CANCELLED, timestamp);
        preparedPlan = createTestPlan(FollowUpPlanStatusType.PREPARED, timestamp);
        underwayPlan = createTestPlan(FollowUpPlanStatusType.UNDERWAY, timestamp);
    }

    private FollowUpPlan createTestPlan(FollowUpPlanStatusType status, long timestamp) throws Exception {
        FollowUpPlanAddRequest request = new FollowUpPlanAddRequest();
        request.setCustomerId("test_customer_" + IDGenerator.nextStr());
        request.setOpportunityId("test_opportunity_" + IDGenerator.nextStr());
        request.setOwner("admin");
        request.setContactId("test_contact_" + IDGenerator.nextStr());
        request.setType("CUSTOMER");
        request.setMethod("1");
        request.setContent("测试计划 - " + status.name());
        request.setEstimatedTime(timestamp);
        request.setModuleFields(List.of(new BaseModuleFieldValue("id", "value")));

        var mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        var resultData = getResultData(mvcResult, FollowUpPlan.class);
        var plan = followUpPlanMapper.selectByPrimaryKey(resultData.getId());

        if (status != FollowUpPlanStatusType.PREPARED) {
            plan.setStatus(status.name());
            followUpPlanMapper.update(plan);
            plan = followUpPlanMapper.selectByPrimaryKey(plan.getId());
        }

        return plan;
    }

    @Test
    @Order(2)
    void testCompletedPlanCannotBeCancelled() throws Exception {
        String originalStatus = completedPlan.getStatus();
        assertEquals(FollowUpPlanStatusType.COMPLETED.name(), originalStatus);

        ResultActions resultActions = this.requestGet(CANCEL_PLAN, completedPlan.getId());
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);
        assertResponseContains(resultActions, Translator.get("plan_already_completed_or_cancelled"));

        FollowUpPlan afterPlan = followUpPlanMapper.selectByPrimaryKey(completedPlan.getId());
        assertEquals(originalStatus, afterPlan.getStatus(), "已完成的计划状态不应被修改");
    }

    @Test
    @Order(3)
    void testCancelledPlanCannotUpdateStatus() throws Exception {
        String originalStatus = cancelledPlan.getStatus();
        assertEquals(FollowUpPlanStatusType.CANCELLED.name(), originalStatus);

        FollowUpPlanStatusRequest request = new FollowUpPlanStatusRequest();
        request.setId(cancelledPlan.getId());
        request.setStatus(FollowUpPlanStatusType.UNDERWAY.name());

        ResultActions resultActions = this.requestPost(STATUS_UPDATE, request);
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);
        assertResponseContains(resultActions, Translator.get("plan_already_completed_or_cancelled"));

        FollowUpPlan afterPlan = followUpPlanMapper.selectByPrimaryKey(cancelledPlan.getId());
        assertEquals(originalStatus, afterPlan.getStatus(), "已取消的计划状态不应被修改");
    }

    @Test
    @Order(4)
    void testPreparedPlanCannotBeDirectlyCompleted() throws Exception {
        String originalStatus = preparedPlan.getStatus();
        assertEquals(FollowUpPlanStatusType.PREPARED.name(), originalStatus);

        FollowUpPlanStatusRequest request = new FollowUpPlanStatusRequest();
        request.setId(preparedPlan.getId());
        request.setStatus(FollowUpPlanStatusType.COMPLETED.name());

        ResultActions resultActions = this.requestPost(STATUS_UPDATE, request);
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);
        assertResponseContains(resultActions, Translator.get("invalid_status_for_complete"));

        FollowUpPlan afterPlan = followUpPlanMapper.selectByPrimaryKey(preparedPlan.getId());
        assertEquals(originalStatus, afterPlan.getStatus(), "未开始的计划不应直接改为已完成");
    }

    @Test
    @Order(5)
    void testUnderwayPlanCanBeCompleted() throws Exception {
        String originalStatus = underwayPlan.getStatus();
        assertEquals(FollowUpPlanStatusType.UNDERWAY.name(), originalStatus);

        FollowUpPlanStatusRequest request = new FollowUpPlanStatusRequest();
        request.setId(underwayPlan.getId());
        request.setStatus(FollowUpPlanStatusType.COMPLETED.name());

        this.requestPostWithOk(STATUS_UPDATE, request);

        FollowUpPlan afterPlan = followUpPlanMapper.selectByPrimaryKey(underwayPlan.getId());
        assertEquals(FollowUpPlanStatusType.COMPLETED.name(), afterPlan.getStatus(), "进行中的计划应能成功改为已完成");
    }

    @Test
    @Order(6)
    void testCompletedPlanCannotUpdateStatus() throws Exception {
        String originalStatus = completedPlan.getStatus();
        assertEquals(FollowUpPlanStatusType.COMPLETED.name(), originalStatus);

        FollowUpPlanStatusRequest request = new FollowUpPlanStatusRequest();
        request.setId(completedPlan.getId());
        request.setStatus(FollowUpPlanStatusType.UNDERWAY.name());

        ResultActions resultActions = this.requestPost(STATUS_UPDATE, request);
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);
        assertResponseContains(resultActions, Translator.get("plan_already_completed_or_cancelled"));

        FollowUpPlan afterPlan = followUpPlanMapper.selectByPrimaryKey(completedPlan.getId());
        assertEquals(originalStatus, afterPlan.getStatus(), "已完成的计划状态不应被修改");
    }

    @Test
    @Order(7)
    void testPreparedPlanCanBeCancelled() throws Exception {
        String originalStatus = preparedPlan.getStatus();
        assertEquals(FollowUpPlanStatusType.PREPARED.name(), originalStatus);

        this.requestGetWithOk(CANCEL_PLAN, preparedPlan.getId());

        FollowUpPlan afterPlan = followUpPlanMapper.selectByPrimaryKey(preparedPlan.getId());
        assertEquals(FollowUpPlanStatusType.CANCELLED.name(), afterPlan.getStatus(), "未开始的计划应能成功取消");
    }

    @Test
    @Order(8)
    void testUnderwayPlanCanBeCancelled() throws Exception {
        FollowUpPlan newUnderwayPlan = createNewUnderwayPlanForCancelTest();

        String originalStatus = newUnderwayPlan.getStatus();
        assertEquals(FollowUpPlanStatusType.UNDERWAY.name(), originalStatus);

        this.requestGetWithOk(CANCEL_PLAN, newUnderwayPlan.getId());

        FollowUpPlan afterPlan = followUpPlanMapper.selectByPrimaryKey(newUnderwayPlan.getId());
        assertEquals(FollowUpPlanStatusType.CANCELLED.name(), afterPlan.getStatus(), "进行中的计划应能成功取消");

        deleteTestPlan(newUnderwayPlan);
    }

    private FollowUpPlan createNewUnderwayPlanForCancelTest() throws Exception {
        long timestamp = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000;

        FollowUpPlanAddRequest request = new FollowUpPlanAddRequest();
        request.setCustomerId("test_customer_" + IDGenerator.nextStr());
        request.setOpportunityId("test_opportunity_" + IDGenerator.nextStr());
        request.setOwner("admin");
        request.setContactId("test_contact_" + IDGenerator.nextStr());
        request.setType("CUSTOMER");
        request.setMethod("1");
        request.setContent("测试计划 - UNDERWAY for cancel test");
        request.setEstimatedTime(timestamp);
        request.setModuleFields(List.of(new BaseModuleFieldValue("id", "value")));

        var mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        var resultData = getResultData(mvcResult, FollowUpPlan.class);
        var plan = followUpPlanMapper.selectByPrimaryKey(resultData.getId());

        plan.setStatus(FollowUpPlanStatusType.UNDERWAY.name());
        followUpPlanMapper.update(plan);
        plan = followUpPlanMapper.selectByPrimaryKey(plan.getId());

        return plan;
    }

    private void assertResponseContains(ResultActions resultActions, String expectedText) throws Exception {
        String responseContent = resultActions.andReturn().getResponse().getContentAsString(Charset.defaultCharset());
        assertTrue(responseContent.contains(expectedText),
                "响应应包含文本: " + expectedText + ", 实际响应: " + responseContent);
    }

    @Test
    @Order(10)
    void testCleanupTestData() {
        deleteTestPlan(completedPlan);
        deleteTestPlan(cancelledPlan);
        deleteTestPlan(preparedPlan);
    }

    private void deleteTestPlan(FollowUpPlan plan) {
        if (plan != null && plan.getId() != null) {
            followUpPlanMapper.deleteByPrimaryKey(plan.getId());
        }
    }
}
