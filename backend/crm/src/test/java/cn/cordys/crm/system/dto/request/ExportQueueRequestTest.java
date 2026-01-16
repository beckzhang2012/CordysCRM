package cn.cordys.crm.system.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportQueueRequestTest {

    @Test
    void testExportQueueRequestBuilder() {
        ExportQueueRequest request = ExportQueueRequest.builder()
                .taskId("task-1")
                .userId("user-1")
                .orgId("org-1")
                .fileName("export.xlsx")
                .resourceType("CUSTOMER")
                .priority(1)
                .retryCount(0)
                .maxRetryCount(3)
                .build();

        assertNotNull(request);
        assertEquals("task-1", request.getTaskId());
        assertEquals("user-1", request.getUserId());
        assertEquals("org-1", request.getOrgId());
        assertEquals("export.xlsx", request.getFileName());
        assertEquals("CUSTOMER", request.getResourceType());
        assertEquals(1, request.getPriority());
        assertEquals(0, request.getRetryCount());
        assertEquals(3, request.getMaxRetryCount());
    }

    @Test
    void testExportQueueRequestGettersSetters() {
        ExportQueueRequest request = new ExportQueueRequest();
        request.setTaskId("task-2");
        request.setUserId("user-2");
        request.setOrgId("org-2");
        request.setFileName("export2.xlsx");
        request.setResourceType("CLUE");
        request.setExportType("CLUE");
        request.setLogModule("CLUE_INDEX");
        request.setLocale("en-US");
        request.setPriority(2);
        request.setRetryCount(1);
        request.setMaxRetryCount(5);
        request.setProgressCurrent(50);
        request.setProgressTotal(100);

        assertEquals("task-2", request.getTaskId());
        assertEquals("user-2", request.getUserId());
        assertEquals("org-2", request.getOrgId());
        assertEquals("export2.xlsx", request.getFileName());
        assertEquals("CLUE", request.getResourceType());
        assertEquals("CLUE", request.getExportType());
        assertEquals("CLUE_INDEX", request.getLogModule());
        assertEquals("en-US", request.getLocale());
        assertEquals(2, request.getPriority());
        assertEquals(1, request.getRetryCount());
        assertEquals(5, request.getMaxRetryCount());
        assertEquals(50, request.getProgressCurrent());
        assertEquals(100, request.getProgressTotal());
    }
}
