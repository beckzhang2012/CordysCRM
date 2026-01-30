CREATE TABLE `cus_reminder` (
  `id` varchar(64) NOT NULL COMMENT 'ID',
  `customer_id` varchar(64) DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(255) DEFAULT NULL COMMENT '客户名称',
  `reminder_time` bigint(20) DEFAULT NULL COMMENT '提醒时间',
  `content` varchar(500) DEFAULT NULL COMMENT '提醒内容',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '提醒状态：PENDING-待提醒, COMPLETED-已提醒, CANCELLED-已取消',
  `receiver` varchar(64) DEFAULT NULL COMMENT '接收人ID',
  `organization_id` varchar(64) DEFAULT NULL COMMENT '组织ID',
  `create_user` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_user` varchar(64) DEFAULT NULL COMMENT '修改人',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver`,`organization_id`),
  KEY `idx_reminder_time` (`reminder_time`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户跟进提醒表';
