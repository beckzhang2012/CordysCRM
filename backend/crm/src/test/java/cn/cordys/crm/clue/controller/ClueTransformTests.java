package cn.cordys.crm.clue.controller;

import cn.cordys.common.constants.InternalUser;
import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.base.BaseTest;
import cn.cordys.crm.clue.constants.ClueStatus;
import cn.cordys.crm.clue.domain.Clue;
import cn.cordys.crm.clue.dto.request.ClueAddRequest;
import cn.cordys.crm.customer.dto.request.ClueTransformRequest;
import cn.cordys.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClueTransformTests extends BaseTest {

    private static final String TRANSFORM = "transform";
    private static final String BASE_PATH = "/lead/";

    @Resource
    private BaseMapper<Clue> clueMapper;

    private static Clue convertedClue;
    private static Clue successStatusClue;
    private static Clue failStatusClue;
    private static Clue normalClue;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @BeforeEach
    void setUp() throws Exception {
        if (convertedClue == null) {
            convertedClue = createClueWithTransitionId("converted_clue_test");
        }
        if (successStatusClue == null) {
            successStatusClue = createClueWithStage("success_status_clue_test", ClueStatus.SUCCESS.getKey());
        }
        if (failStatusClue == null) {
            failStatusClue = createClueWithStage("fail_status_clue_test", ClueStatus.FAIL.getKey());
        }
        if (normalClue == null) {
            normalClue = createClueWithStage("normal_clue_test", ClueStatus.INTERESTED.getKey());
        }
    }

    private Clue createClueWithTransitionId(String name) throws Exception {
        ClueAddRequest request = new ClueAddRequest();
        request.setName(name);
        request.setOwner(InternalUser.ADMIN.getValue());
        request.setContact("test_contact");
        request.setPhone("1875092" + System.currentTimeMillis() % 10000);
        request.setProducts(List.of("test_product"));

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        Clue resultData = getResultData(mvcResult, Clue.class);
        Clue clue = clueMapper.selectByPrimaryKey(resultData.getId());

        clue.setTransitionId(IDGenerator.nextStr());
        clue.setTransitionType("CUSTOMER");
        clueMapper.update(clue);

        return clueMapper.selectByPrimaryKey(clue.getId());
    }

    private Clue createClueWithStage(String name, String stage) throws Exception {
        ClueAddRequest request = new ClueAddRequest();
        request.setName(name);
        request.setOwner(InternalUser.ADMIN.getValue());
        request.setContact("test_contact");
        request.setPhone("1875092" + System.currentTimeMillis() % 10000);
        request.setProducts(List.of("test_product"));

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        Clue resultData = getResultData(mvcResult, Clue.class);
        Clue clue = clueMapper.selectByPrimaryKey(resultData.getId());

        clue.setStage(stage);
        clueMapper.update(clue);

        return clueMapper.selectByPrimaryKey(clue.getId());
    }

    @Test
    @Order(1)
    void testConvertedClueTransform_ShouldBeRejected() throws Exception {
        String originalTransitionId = convertedClue.getTransitionId();
        String originalStage = convertedClue.getStage();

        ClueTransformRequest request = new ClueTransformRequest();
        request.setClueId(convertedClue.getId());
        request.setOppCreated(false);

        ResultActions resultActions = this.requestPost(TRANSFORM, request);
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);

        Clue updatedClue = clueMapper.selectByPrimaryKey(convertedClue.getId());
        Assertions.assertEquals(originalTransitionId, updatedClue.getTransitionId());
        Assertions.assertEquals(originalStage, updatedClue.getStage());
    }

    @Test
    @Order(2)
    void testSuccessStatusClueTransform_ShouldBeRejected() throws Exception {
        String originalTransitionId = successStatusClue.getTransitionId();
        String originalStage = successStatusClue.getStage();

        ClueTransformRequest request = new ClueTransformRequest();
        request.setClueId(successStatusClue.getId());
        request.setOppCreated(false);

        ResultActions resultActions = this.requestPost(TRANSFORM, request);
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);

        Clue updatedClue = clueMapper.selectByPrimaryKey(successStatusClue.getId());
        Assertions.assertEquals(originalTransitionId, updatedClue.getTransitionId());
        Assertions.assertEquals(originalStage, updatedClue.getStage());
    }

    @Test
    @Order(3)
    void testFailStatusClueTransform_ShouldBeRejected() throws Exception {
        String originalTransitionId = failStatusClue.getTransitionId();
        String originalStage = failStatusClue.getStage();

        ClueTransformRequest request = new ClueTransformRequest();
        request.setClueId(failStatusClue.getId());
        request.setOppCreated(false);

        ResultActions resultActions = this.requestPost(TRANSFORM, request);
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);

        Clue updatedClue = clueMapper.selectByPrimaryKey(failStatusClue.getId());
        Assertions.assertEquals(originalTransitionId, updatedClue.getTransitionId());
        Assertions.assertEquals(originalStage, updatedClue.getStage());
    }

    @Test
    @Order(4)
    void testOppCreatedTrueButOppNameEmpty_ShouldBeRejected() throws Exception {
        String originalTransitionId = normalClue.getTransitionId();
        String originalStage = normalClue.getStage();

        ClueTransformRequest request = new ClueTransformRequest();
        request.setClueId(normalClue.getId());
        request.setOppCreated(true);
        request.setOppName("");

        ResultActions resultActions = this.requestPost(TRANSFORM, request);
        assertErrorCode(resultActions, CrmHttpResultCode.VALIDATE_FAILED);

        Clue updatedClue = clueMapper.selectByPrimaryKey(normalClue.getId());
        Assertions.assertEquals(originalTransitionId, updatedClue.getTransitionId());
        Assertions.assertEquals(originalStage, updatedClue.getStage());
    }

    @Test
    @Order(5)
    void testValidTransform_ShouldSucceed() throws Exception {
        Clue validClue = createClueWithStage("valid_transform_test_" + System.currentTimeMillis(), ClueStatus.INTERESTED.getKey());

        String originalTransitionId = validClue.getTransitionId();
        String originalStage = validClue.getStage();

        Assertions.assertNull(originalTransitionId);
        Assertions.assertEquals(ClueStatus.INTERESTED.getKey(), originalStage);

        ClueTransformRequest request = new ClueTransformRequest();
        request.setClueId(validClue.getId());
        request.setOppCreated(false);

        MvcResult mvcResult = this.requestPostWithOkAndReturn(TRANSFORM, request);
        String resultId = getResultData(mvcResult, String.class);

        Assertions.assertNotNull(resultId);

        Clue updatedClue = clueMapper.selectByPrimaryKey(validClue.getId());
        Assertions.assertNotNull(updatedClue.getTransitionId());
    }
}
