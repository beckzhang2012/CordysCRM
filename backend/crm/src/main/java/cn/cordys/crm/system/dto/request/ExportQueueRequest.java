package cn.cordys.crm.system.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportQueueRequest {

    private String taskId;

    private String userId;

    private String orgId;

    private String fileName;

    private String resourceType;

    private Integer priority;

    private Integer retryCount;

    private Integer maxRetryCount;

    private Integer progressCurrent;

    private Integer progressTotal;

    private String exportType;

    private String logModule;

    private String locale;

    private Object exportParams;
}
