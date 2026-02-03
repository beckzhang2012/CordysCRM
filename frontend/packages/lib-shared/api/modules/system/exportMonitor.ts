import type { CordysAxios } from '@lib/shared/api/http/Axios';
import {
  CancelExportTaskUrl,
  GetExportQueueStatusUrl,
  GetExportTaskProgressUrl,
  GetUserExportStatsUrl,
} from '@lib/shared/api/requrls/system/exportMonitor';

// 队列状态响应
export interface QueueStatus {
  redisQueueSize: number;
  localQueueSize: number;
  activeCount: number;
  poolSize: number;
  maxConcurrent: number;
  queueCapacity: number;
  usageRate: string;
}

// 导出进度响应
export interface ExportProgress {
  taskId: string;
  status: 'PREPARED' | 'SUCCESS' | 'ERROR' | 'STOP';
  processedCount: number;
  totalCount: number;
  progressPercentage: number;
  errorMessage?: string;
  startTime: number;
  endTime?: number;
  duration: number;
}

export default function useExportMonitorApi(CDR: CordysAxios) {
  // 获取导出队列状态
  function getExportQueueStatus() {
    return CDR.get<QueueStatus>({ url: GetExportQueueStatusUrl });
  }

  // 获取导出任务进度
  function getExportTaskProgress(taskId: string) {
    return CDR.get<ExportProgress>({ url: `${GetExportTaskProgressUrl}/${taskId}` });
  }

  // 取消导出任务
  function cancelExportTask(taskId: string) {
    return CDR.get({ url: `${CancelExportTaskUrl}/${taskId}` });
  }

  // 获取当前用户导出统计
  function getUserExportStats() {
    return CDR.get({ url: GetUserExportStatsUrl });
  }

  return {
    getExportQueueStatus,
    getExportTaskProgress,
    cancelExportTask,
    getUserExportStats,
  };
}
