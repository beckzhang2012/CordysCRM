# 客户数据导出功能优化说明

## 一、优化背景

原有客户数据导出功能存在以下问题：
1. **内存溢出风险**：导出大量数据时，一次性加载所有数据到内存
2. **阻塞请求**：导出过程占用线程，影响其他请求处理
3. **无任务队列**：大量导出请求同时处理，导致系统资源耗尽
4. **缺乏进度通知**：用户无法实时了解导出进度

## 二、优化方案

### 2.1 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户导出优化架构                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │   Controller  │    │   Controller  │    │   Controller  │      │
│  │  (同步导出)   │    │ (异步队列导出) │    │  (监控接口)   │      │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘      │
│         │                   │                    │              │
│         ▼                   ▼                    ▼              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │CustomerExport │    │ExportTaskQueue│    │ExportMonitor │      │
│  │   Service    │    │   Service    │    │  Controller  │      │
│  └──────┬───────┘    └──────┬───────┘    └──────────────┘      │
│         │                   │                                   │
│         ▼                   ▼                                   │
│  ┌──────────────┐    ┌──────────────┐                          │
│  │  Streaming   │◄───│  Redis队列   │                          │
│  │ExportService │    │  (持久化)    │                          │
│  └──────┬───────┘    └──────┬───────┘                          │
│         │                   │                                   │
│         ▼                   ▼                                   │
│  ┌──────────────────────────────────────┐                     │
│  │         ThreadPoolExecutor            │                     │
│  │   (最大5个并发导出任务，有界队列)        │                     │
│  └──────────────────────────────────────┘                     │
│                              │                                  │
│                              ▼                                  │
│  ┌──────────────────────────────────────┐                     │
│  │      Virtual Thread (导出执行)        │                     │
│  │   - 流式分批处理数据                   │                     │
│  │   - 每批2000条                        │                     │
│  │   - 支持中断取消                       │                     │
│  └──────────────────────────────────────┘                     │
│                              │                                  │
│                              ▼                                  │
│  ┌──────────────────────────────────────┐                     │
│  │      ExportProgressService           │                     │
│  │   - 进度跟踪                          │                     │
│  │   - SSE实时通知                       │                     │
│  └──────────────────────────────────────┘                     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 核心组件

#### 1. ExportTaskQueueService（导出任务队列服务）
- **功能**：管理导出任务队列，控制并发数量
- **特点**：
  - 使用Redis队列持久化任务
  - 本地有界队列控制并发（最大5个并发）
  - 支持任务优先级
  - 支持任务取消

#### 2. StreamingExportService（流式导出服务）
- **功能**：大数据量流式导出
- **特点**：
  - 分批处理（每批2000条，可配置）
  - 流式写入Excel，避免内存溢出
  - 支持中断检查
  - 自动让出CPU

#### 3. ExportProgressService（导出进度服务）
- **功能**：实时跟踪和通知导出进度
- **特点**：
  - Redis缓存进度
  - SSE实时推送给用户
  - 支持集群环境

#### 4. CustomerExportQueueService（客户导出队列服务）
- **功能**：客户导出任务处理器
- **特点**：
  - 注册到任务队列服务
  - 处理条件导出和选中导出
  - 集成流式导出

## 三、API接口

### 3.1 新的异步导出接口

#### 1. 客户全部导出（异步队列）
```http
POST /account/export-all-async
Content-Type: application/json

{
  "fileName": "客户导出",
  "viewId": "ALL",
  "headList": [
    {"key": "name", "title": "客户名称"},
    {"key": "phone", "title": "电话"}
  ],
  "combineSearch": {}
}
```

**响应**：
```json
"export_task_123456789"
```

#### 2. 客户选中导出（异步队列）
```http
POST /account/export-select-async
Content-Type: application/json

{
  "fileName": "选中客户导出",
  "ids": ["id1", "id2", "id3"],
  "headList": [
    {"key": "name", "title": "客户名称"}
  ]
}
```

**响应**：
```json
"export_task_987654321"
```

### 3.2 监控接口

#### 1. 获取队列状态
```http
GET /export/monitor/queue/status
```

**响应**：
```json
{
  "redisQueueSize": 10,
  "localQueueSize": 2,
  "activeCount": 3,
  "poolSize": 5,
  "maxConcurrent": 5,
  "queueCapacity": 100,
  "usageRate": "60.00%"
}
```

#### 2. 获取任务进度
```http
GET /export/monitor/progress/{taskId}
```

**响应**：
```json
{
  "taskId": "export_task_123456789",
  "status": "PREPARED",
  "processedCount": 5000,
  "totalCount": -1,
  "progressPercentage": 0,
  "startTime": 1704067200000,
  "duration": 15000
}
```

#### 3. 取消任务
```http
GET /export/monitor/cancel/{taskId}
```

## 四、配置参数

### 4.1 队列配置（ExportTaskQueueService）
```java
// 导出任务队列最大容量
private static final int QUEUE_CAPACITY = 100;

// 最大并发导出任务数
private static final int MAX_CONCURRENT_EXPORTS = 5;
```

### 4.2 流式导出配置（StreamingExportService）
```java
// 默认批次大小
public static final int DEFAULT_BATCH_SIZE = 2000;

// 最小批次大小
public static final int MIN_BATCH_SIZE = 500;

// 最大批次大小
public static final int MAX_BATCH_SIZE = 5000;
```

### 4.3 进度过期时间（ExportProgressService）
```java
// 导出进度过期时间（小时）
private static final int PROGRESS_EXPIRE_HOURS = 24;
```

## 五、使用建议

### 5.1 何时使用异步导出

| 场景 | 推荐方式 |
|------|----------|
| 数据量 < 10000条 | 同步导出（原有接口） |
| 数据量 >= 10000条 | 异步导出（新接口） |
| 并发导出请求多 | 异步导出（新接口） |
| 需要实时进度 | 异步导出（新接口） |

### 5.2 前端集成建议

1. **调用异步导出接口**后，获取任务ID
2. **轮询进度接口**或使用**SSE连接**接收实时进度
3. 根据状态显示不同UI：
   - PREPARED：显示进度条
   - SUCCESS：显示下载按钮
   - ERROR：显示错误信息
   - STOP：显示已取消

### 5.3 监控建议

1. 定期监控队列状态，避免队列堆积
2. 设置告警阈值：
   - Redis队列大小 > 50
   - 并发使用率 > 80%

## 六、注意事项

1. **兼容性**：新接口与原接口并存，可平滑过渡
2. **资源占用**：异步导出使用独立线程池，不影响主业务
3. **任务持久化**：任务存储在Redis，重启后任务不丢失
4. **中断处理**：支持任务取消，已处理数据会被清理

## 七、后续优化方向

1. **任务优先级**：支持高优先级任务优先处理
2. **批量通知**：任务完成时发送站内信/邮件通知
3. **导出模板**：支持保存常用导出模板
4. **导出历史**：支持查看导出历史记录
5. **数据压缩**：大文件导出时自动压缩
