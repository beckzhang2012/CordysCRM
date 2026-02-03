package cn.cordys.crm.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * 导出任务消息
 * 用于在消息队列中传递导出任务信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 导出类型（CUSTOMER, CLUE, OPPORTUNITY等）
     */
    private String exportType;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 语言环境
     */
    private Locale locale;

    /**
     * 日志模块
     */
    private String logModule;

    /**
     * 导出参数（JSON格式）
     */
    private String exportParams;

    /**
     * 额外参数映射
     */
    private Map<String, Object> extraParams;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;

    /**
     * 是否选中导出
     */
    private Boolean isSelectExport;

    /**
     * 选中导出的ID列表（JSON格式）
     */
    private String selectIds;
}
