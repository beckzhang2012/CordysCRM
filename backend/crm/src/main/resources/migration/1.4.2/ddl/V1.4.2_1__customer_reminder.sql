-- 客户跟进提醒表
CREATE TABLE IF NOT EXISTS `crm_customer_reminder` (
    `id` varchar(64) NOT NULL COMMENT '主键ID',
    `customer_id` varchar(64) NOT NULL COMMENT '客户ID',
    `customer_name` varchar(255) DEFAULT NULL COMMENT '客户名称',
    `user_id` varchar(64) NOT NULL COMMENT '提醒接收人ID',
    `user_name` varchar(100) DEFAULT NULL COMMENT '提醒接收人名称',
    `reminder_time` datetime NOT NULL COMMENT '提醒时间',
    `content` varchar(500) DEFAULT NULL COMMENT '提醒内容',
    `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态：0-未提醒 1-已提醒 2-已关闭',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_reminder_time` (`reminder_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户跟进提醒表';
